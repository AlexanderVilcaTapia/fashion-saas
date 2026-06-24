from rest_framework import status, generics
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from django.shortcuts import get_object_or_404
from orders.models import Order, Cart, CartItem
from orders.serializers import (
    OrderSerializer,
    OrderCreateSerializer,
    OrderStatusUpdateSerializer,
    CartSerializer,
    CartItemSerializer
)


class CartView(APIView):
    permission_classes = (IsAuthenticated,)

    def get(self, request):
        store_id = request.query_params.get('store_id')
        if not store_id:
            return Response({'error': 'store_id es requerido.'}, status=status.HTTP_400_BAD_REQUEST)
        from stores.models import Store
        store = get_object_or_404(Store, id=store_id, status='active')
        cart, created = Cart.objects.get_or_create(buyer=request.user, store=store)
        serializer = CartSerializer(cart)
        return Response(serializer.data)

    def delete(self, request):
        store_id = request.query_params.get('store_id')
        if not store_id:
            return Response({'error': 'store_id es requerido.'}, status=status.HTTP_400_BAD_REQUEST)
        from stores.models import Store
        store = get_object_or_404(Store, id=store_id)
        cart = get_object_or_404(Cart, buyer=request.user, store=store)
        cart.cart_items.all().delete()
        return Response({'message': 'Carrito vaciado correctamente.'})


class CartItemView(APIView):
    permission_classes = (IsAuthenticated,)

    def post(self, request):
        from stores.models import Store
        from products.models import Product, ProductSize
        store_id = request.data.get('store_id')
        store = get_object_or_404(Store, id=store_id, status='active')
        cart, created = Cart.objects.get_or_create(buyer=request.user, store=store)
        serializer = CartItemSerializer(data=request.data)
        if serializer.is_valid():
            product = Product.objects.get(id=request.data['product_id'])
            size = ProductSize.objects.get(id=request.data['size_id'])
            existing_item = CartItem.objects.filter(cart=cart, product=product, size=size).first()
            if existing_item:
                existing_item.quantity += serializer.validated_data['quantity']
                existing_item.save()
                return Response(CartItemSerializer(existing_item).data)
            CartItem.objects.create(
                cart=cart,
                product=product,
                size=size,
                quantity=serializer.validated_data['quantity']
            )
            return Response(CartSerializer(cart).data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def patch(self, request, item_id):
        cart_item = get_object_or_404(CartItem, id=item_id, cart__buyer=request.user)
        quantity = request.data.get('quantity')
        if not quantity or int(quantity) < 1:
            return Response({'error': 'Cantidad inválida.'}, status=status.HTTP_400_BAD_REQUEST)
        if cart_item.size.stock < int(quantity):
            return Response({'error': f'Stock insuficiente. Solo hay {cart_item.size.stock} unidades.'}, status=status.HTTP_400_BAD_REQUEST)
        cart_item.quantity = int(quantity)
        cart_item.save()
        return Response(CartItemSerializer(cart_item).data)

    def delete(self, request, item_id):
        cart_item = get_object_or_404(CartItem, id=item_id, cart__buyer=request.user)
        cart_item.delete()
        return Response({'message': 'Item eliminado del carrito.'})


class OrderListView(generics.ListAPIView):
    serializer_class = OrderSerializer
    permission_classes = (IsAuthenticated,)

    def get_queryset(self):
        return Order.objects.filter(buyer=self.request.user)


class OrderCreateView(generics.CreateAPIView):
    serializer_class = OrderCreateSerializer
    permission_classes = (IsAuthenticated,)

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        order = serializer.save()
        return Response(OrderSerializer(order).data, status=status.HTTP_201_CREATED)


class OrderDetailView(generics.RetrieveAPIView):
    serializer_class = OrderSerializer
    permission_classes = (IsAuthenticated,)

    def get_object(self):
        return get_object_or_404(Order, id=self.kwargs['pk'], buyer=self.request.user)


class StoreOrderListView(generics.ListAPIView):
    serializer_class = OrderSerializer
    permission_classes = (IsAuthenticated,)

    def get_queryset(self):
        queryset = Order.objects.filter(store=self.request.user.store)
        order_status = self.request.query_params.get('status')
        payment_status = self.request.query_params.get('payment_status')
        if order_status:
            queryset = queryset.filter(status=order_status)
        if payment_status:
            queryset = queryset.filter(payment_status=payment_status)
        return queryset


class StoreOrderDetailView(generics.RetrieveUpdateAPIView):
    permission_classes = (IsAuthenticated,)

    def get_serializer_class(self):
        if self.request.method in ('PUT', 'PATCH'):
            return OrderStatusUpdateSerializer
        return OrderSerializer

    def get_object(self):
        return get_object_or_404(Order, id=self.kwargs['pk'], store=self.request.user.store)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def store_dashboard_stats_view(request):
    from django.db.models import Sum, Count
    from stores.models import Store
    store = get_object_or_404(Store, owner=request.user)
    total_revenue = Order.objects.filter(
        store=store, payment_status='paid'
    ).aggregate(Sum('total'))['total__sum'] or 0
    total_orders = Order.objects.filter(store=store).count()
    pending_orders = Order.objects.filter(store=store, status='pending').count()
    top_products = OrderSerializer
    return Response({
        'total_revenue': total_revenue,
        'total_orders': total_orders,
        'pending_orders': pending_orders,
        'total_products': store.total_products,
    })