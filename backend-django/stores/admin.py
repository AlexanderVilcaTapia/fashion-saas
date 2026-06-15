from django.contrib import admin
from stores.models import Store, StoreSchedule


class StoreScheduleInline(admin.TabularInline):
    model = StoreSchedule
    extra = 0


@admin.register(Store)
class StoreAdmin(admin.ModelAdmin):
    list_display = ('name', 'owner', 'city', 'status', 'total_products', 'created_at')
    list_filter = ('status', 'city')
    search_fields = ('name', 'owner__email')
    prepopulated_fields = {'slug': ('name',)}
    inlines = [StoreScheduleInline]