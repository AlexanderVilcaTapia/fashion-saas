from django.urls import path
from products.views import (
    CategoryListView,
    CategoryDetailView,
    ProductListView,
    ProductDetailView,
    StoreProductListView,
    StoreProductDetailView,
    ProductSizeView,
    ProductSizeDetailView,
    ProductImageView,
    featured_products_view
)

urlpatterns = [
    path('', ProductListView.as_view(), name='product_list'),
    path('featured/', featured_products_view, name='featured_products'),
    path('categories/', CategoryListView.as_view(), name='category_list'),
    path('categories/<slug:slug>/', CategoryDetailView.as_view(), name='category_detail'),
    path('my-store/', StoreProductListView.as_view(), name='store_product_list'),
    path('my-store/<int:pk>/', StoreProductDetailView.as_view(), name='store_product_detail'),
    path('my-store/<int:pk>/sizes/', ProductSizeView.as_view(), name='product_sizes'),
    path('my-store/<int:pk>/sizes/<int:size_id>/', ProductSizeDetailView.as_view(), name='product_size_detail'),
    path('my-store/<int:pk>/images/', ProductImageView.as_view(), name='product_images'),
    path('<slug:store_slug>/<slug:product_slug>/', ProductDetailView.as_view(), name='product_detail'),
]