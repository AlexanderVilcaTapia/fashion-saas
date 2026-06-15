from rest_framework import serializers
from stores.models import Store, StoreSchedule


class StoreScheduleSerializer(serializers.ModelSerializer):
    class Meta:
        model = StoreSchedule
        fields = ('id', 'day', 'opening_time', 'closing_time', 'is_closed')


class StoreSerializer(serializers.ModelSerializer):
    schedules = StoreScheduleSerializer(many=True, read_only=True)
    total_products = serializers.ReadOnlyField()
    owner_name = serializers.SerializerMethodField()

    class Meta:
        model = Store
        fields = (
            'id', 'name', 'slug', 'description', 'logo', 'banner',
            'address', 'city', 'phone', 'email', 'status',
            'total_products', 'owner_name', 'schedules', 'created_at'
        )
        read_only_fields = ('id', 'slug', 'status', 'created_at')

    def get_owner_name(self, obj):
        return obj.owner.full_name


class StoreCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Store
        fields = ('name', 'description', 'logo', 'banner', 'address', 'city', 'phone', 'email')

    def validate_name(self, value):
        if Store.objects.filter(name__iexact=value).exists():
            raise serializers.ValidationError('Ya existe una tienda con ese nombre.')
        return value

    def create(self, validated_data):
        from django.utils.text import slugify
        import uuid
        user = self.context['request'].user
        slug = slugify(validated_data['name'])
        if Store.objects.filter(slug=slug).exists():
            slug = f"{slug}-{str(uuid.uuid4())[:8]}"
        store = Store.objects.create(owner=user, slug=slug, **validated_data)
        return store


class StoreUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Store
        fields = ('name', 'description', 'logo', 'banner', 'address', 'city', 'phone', 'email')


class StoreDashboardSerializer(serializers.ModelSerializer):
    total_products = serializers.ReadOnlyField()
    total_orders = serializers.SerializerMethodField()
    total_revenue = serializers.SerializerMethodField()
    pending_orders = serializers.SerializerMethodField()

    class Meta:
        model = Store
        fields = ('id', 'name', 'slug', 'status', 'total_products', 'total_orders', 'total_revenue', 'pending_orders')

    def get_total_orders(self, obj):
        return obj.orders.count()

    def get_total_revenue(self, obj):
        from django.db.models import Sum
        result = obj.orders.filter(payment_status='paid').aggregate(Sum('total'))
        return result['total__sum'] or 0

    def get_pending_orders(self, obj):
        return obj.orders.filter(status='pending').count()