from django.contrib import admin
from orders.models import Order, OrderItem, Cart, CartItem


class OrderItemInline(admin.TabularInline):
    model = OrderItem
    extra = 0
    readonly_fields = ('subtotal',)


@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ('id', 'buyer', 'store', 'status', 'payment_status', 'total', 'created_at')
    list_filter = ('status', 'payment_status')
    search_fields = ('buyer__email', 'store__name')
    readonly_fields = ('subtotal', 'total', 'created_at', 'updated_at')
    inlines = [OrderItemInline]


@admin.register(Cart)
class CartAdmin(admin.ModelAdmin):
    list_display = ('buyer', 'store', 'total', 'created_at')
    search_fields = ('buyer__email',)