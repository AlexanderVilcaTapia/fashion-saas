from django.contrib import admin
from products.models import Category, Product, ProductSize, ProductImage


class ProductSizeInline(admin.TabularInline):
    model = ProductSize
    extra = 0


class ProductImageInline(admin.TabularInline):
    model = ProductImage
    extra = 0


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ('name', 'slug', 'created_at')
    prepopulated_fields = {'slug': ('name',)}


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ('name', 'store', 'category', 'price', 'status', 'is_featured', 'created_at')
    list_filter = ('status', 'is_featured', 'category')
    search_fields = ('name', 'store__name')
    prepopulated_fields = {'slug': ('name',)}
    inlines = [ProductSizeInline, ProductImageInline]