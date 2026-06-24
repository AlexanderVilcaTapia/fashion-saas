from django.urls import path
from stores.views import (
    StoreListView,
    StoreDetailView,
    StoreCreateView,
    StoreUpdateView,
    StoreDashboardView,
    StoreScheduleView,
    featured_stores_view,
    store_products_view
)

urlpatterns = [
    path('', StoreListView.as_view(), name='store_list'),
    path('create/', StoreCreateView.as_view(), name='store_create'),
    path('my-store/', StoreUpdateView.as_view(), name='store_update'),
    path('my-store/dashboard/', StoreDashboardView.as_view(), name='store_dashboard'),
    path('my-store/schedules/', StoreScheduleView.as_view(), name='store_schedules'),
    path('featured/', featured_stores_view, name='featured_stores'),
    path('<slug:slug>/', StoreDetailView.as_view(), name='store_detail'),
    path('<slug:slug>/products/', store_products_view, name='store_products'),
]