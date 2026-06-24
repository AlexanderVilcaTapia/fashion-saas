from django.urls import path
from orders.views import (
    CartView,
    CartItemView,
    OrderListView,
    OrderCreateView,
    OrderDetailView,
    StoreOrderListView,
    StoreOrderDetailView,
    store_dashboard_stats_view
)

urlpatterns = [
    path('cart/', CartView.as_view(), name='cart'),
    path('cart/items/', CartItemView.as_view(), name='cart_items'),
    path('cart/items/<int:item_id>/', CartItemView.as_view(), name='cart_item_detail'),
    path('', OrderListView.as_view(), name='order_list'),
    path('create/', OrderCreateView.as_view(), name='order_create'),
    path('<int:pk>/', OrderDetailView.as_view(), name='order_detail'),
    path('my-store/', StoreOrderListView.as_view(), name='store_order_list'),
    path('my-store/<int:pk>/', StoreOrderDetailView.as_view(), name='store_order_detail'),
    path('my-store/stats/', store_dashboard_stats_view, name='store_stats'),
]