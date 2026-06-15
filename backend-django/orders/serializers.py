from rest_framework import serializers
from orders.models import Order, OrderItem, Cart, CartItem
from products.serializers import ProductSerializer, ProductSizeSerializer


class CartItemSerializer(serializers.ModelSerializer):
    product = ProductSerializer(read_only=True)
    size = ProductSizeSerializer(read_only=True)
    subtotal = serializers.ReadOnlyField()
    product_id = serializers.IntegerField(write_only=True)
    size_id = serializers.IntegerField(write_only=True)

    class Meta:
        model = CartItem
        fields = ('id', 'product', 'product_id', 'size', 'size_id', 'quantity', 'subtotal')

    def validate(self, attrs):
        from products.models import Product, ProductSize
        try:
            product = Product.objects.get(id=attrs['product_id'])
        except Product.DoesNotExist:
            raise serializers.ValidationError({'product_id': 'Producto no encontrado.'})
        try:
            size = ProductSize.objects.get(id=attrs['size_id'], product=product)
        except ProductSize.DoesNotExist:
            raise serializers.ValidationError({'size_id': 'Talla no encontrada para este producto.'})
        if size.stock < attrs['quantity']:
            raise serializers.ValidationError({'quantity': f'Stock insuficiente. Solo hay {size.stock} unidades disponibles.'})
        return attrs


class CartSerializer(serializers.ModelSerializer):
    cart_items = CartItemSerializer(many=True, read_only=True)
    total = serializers.ReadOnlyField()
    store_name = serializers.SerializerMethodField()

    class Meta:
        model = Cart
        fields = ('id', 'store', 'store_name', 'cart_items', 'total', 'created_at', 'updated_at')
        read_only_fields = ('id', 'store', 'created_at', 'updated_at')

    def get_store_name(self, obj):
        return obj.store.name


class OrderItemSerializer(serializers.ModelSerializer):
    product_name = serializers.SerializerMethodField()
    size_name = serializers.SerializerMethodField()

    class Meta:
        model = OrderItem
        fields = ('id', 'product', 'product_name', 'size', 'size_name', 'quantity', 'unit_price', 'subtotal')
        read_only_fields = ('id', 'subtotal')

    def get_product_name(self, obj):
        return obj.product.name

    def get_size_name(self, obj):
        return obj.size.size if obj.size else None


class OrderSerializer(serializers.ModelSerializer):
    items = OrderItemSerializer(many=True, read_only=True)
    buyer_name = serializers.SerializerMethodField()
    store_name = serializers.SerializerMethodField()

    class Meta:
        model = Order
        fields = (
            'id', 'buyer', 'buyer_name', 'store', 'store_name',
            'status', 'payment_status', 'payment_intent_id',
            'subtotal', 'shipping_cost', 'total',
            'shipping_address', 'shipping_city', 'shipping_phone',
            'notes', 'items', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'buyer', 'store', 'subtotal', 'total', 'payment_status', 'created_at', 'updated_at')

    def get_buyer_name(self, obj):
        return obj.buyer.full_name

    def get_store_name(self, obj):
        return obj.store.name


class OrderCreateSerializer(serializers.Serializer):
    store_id = serializers.IntegerField()
    shipping_address = serializers.CharField(max_length=255)
    shipping_city = serializers.CharField(max_length=100)
    shipping_phone = serializers.CharField(max_length=20)
    notes = serializers.CharField(required=False, allow_blank=True)

    def validate_store_id(self, value):
        from stores.models import Store
        try:
            Store.objects.get(id=value, status='active')
        except Store.DoesNotExist:
            raise serializers.ValidationError('Tienda no encontrada o inactiva.')
        return value

    def create(self, validated_data):
        from stores.models import Store
        from products.models import ProductSize
        user = self.context['request'].user
        store = Store.objects.get(id=validated_data['store_id'])
        cart = Cart.objects.filter(buyer=user, store=store).first()
        if not cart or not cart.cart_items.exists():
            raise serializers.ValidationError('El carrito está vacío.')
        order = Order.objects.create(
            buyer=user,
            store=store,
            shipping_address=validated_data['shipping_address'],
            shipping_city=validated_data['shipping_city'],
            shipping_phone=validated_data['shipping_phone'],
            notes=validated_data.get('notes', ''),
            subtotal=0,
            total=0,
        )
        for cart_item in cart.cart_items.all():
            size = cart_item.size
            if size.stock < cart_item.quantity:
                order.delete()
                raise serializers.ValidationError(f'Stock insuficiente para {cart_item.product.name} talla {size.size}.')
            OrderItem.objects.create(
                order=order,
                product=cart_item.product,
                size=size,
                quantity=cart_item.quantity,
                unit_price=cart_item.product.final_price,
            )
            size.stock -= cart_item.quantity
            size.save()
        order.calculate_total()
        cart.cart_items.all().delete()
        return order


class OrderStatusUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Order
        fields = ('status',)

    def validate_status(self, value):
        valid_transitions = {
            'pending': ['confirmed', 'cancelled'],
            'confirmed': ['shipped', 'cancelled'],
            'shipped': ['delivered'],
            'delivered': [],
            'cancelled': [],
        }
        current_status = self.instance.status
        if value not in valid_transitions.get(current_status, []):
            raise serializers.ValidationError(f'No se puede cambiar de {current_status} a {value}.')
        return value