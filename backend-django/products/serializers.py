from rest_framework import serializers
from products.models import Category, Product, ProductSize, ProductImage


class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = ('id', 'name', 'slug', 'description', 'image', 'created_at')
        read_only_fields = ('id', 'slug', 'created_at')

    def create(self, validated_data):
        from django.utils.text import slugify
        validated_data['slug'] = slugify(validated_data['name'])
        return super().create(validated_data)


class ProductSizeSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProductSize
        fields = ('id', 'size', 'stock')


class ProductImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProductImage
        fields = ('id', 'image', 'is_primary', 'order')


class ProductSerializer(serializers.ModelSerializer):
    sizes = ProductSizeSerializer(many=True, read_only=True)
    images = ProductImageSerializer(many=True, read_only=True)
    category_name = serializers.SerializerMethodField()
    store_name = serializers.SerializerMethodField()
    final_price = serializers.ReadOnlyField()
    has_discount = serializers.ReadOnlyField()

    class Meta:
        model = Product
        fields = (
            'id', 'store', 'store_name', 'category', 'category_name',
            'name', 'slug', 'description', 'price', 'discount_price',
            'final_price', 'has_discount', 'status', 'is_featured',
            'sizes', 'images', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'store', 'slug', 'created_at', 'updated_at')

    def get_category_name(self, obj):
        return obj.category.name if obj.category else None

    def get_store_name(self, obj):
        return obj.store.name


class ProductCreateSerializer(serializers.ModelSerializer):
    sizes = ProductSizeSerializer(many=True, required=False)

    class Meta:
        model = Product
        fields = ('name', 'category', 'description', 'price', 'discount_price', 'is_featured', 'sizes')

    def validate_price(self, value):
        if value <= 0:
            raise serializers.ValidationError('El precio debe ser mayor a 0.')
        return value

    def validate(self, attrs):
        discount_price = attrs.get('discount_price')
        price = attrs.get('price')
        if discount_price and discount_price >= price:
            raise serializers.ValidationError({'discount_price': 'El precio de descuento debe ser menor al precio original.'})
        return attrs

    def create(self, validated_data):
        from django.utils.text import slugify
        import uuid
        sizes_data = validated_data.pop('sizes', [])
        store = self.context['request'].user.store
        slug = slugify(validated_data['name'])
        if Product.objects.filter(store=store, slug=slug).exists():
            slug = f"{slug}-{str(uuid.uuid4())[:8]}"
        product = Product.objects.create(store=store, slug=slug, **validated_data)
        for size_data in sizes_data:
            ProductSize.objects.create(product=product, **size_data)
        return product


class ProductUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Product
        fields = ('name', 'category', 'description', 'price', 'discount_price', 'status', 'is_featured')

    def validate(self, attrs):
        discount_price = attrs.get('discount_price')
        price = attrs.get('price')
        if discount_price and price and discount_price >= price:
            raise serializers.ValidationError({'discount_price': 'El precio de descuento debe ser menor al precio original.'})
        return attrs


class ProductImageUploadSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProductImage
        fields = ('id', 'image', 'is_primary', 'order')

    def create(self, validated_data):
        product = self.context['product']
        if validated_data.get('is_primary'):
            ProductImage.objects.filter(product=product, is_primary=True).update(is_primary=False)
        return ProductImage.objects.create(product=product, **validated_data)