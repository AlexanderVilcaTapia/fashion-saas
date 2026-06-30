from django.db import models
from users.models import User


class Store(models.Model):
    class Status(models.TextChoices):
        ACTIVE = 'active', 'Activa'
        INACTIVE = 'inactive', 'Inactiva'
        SUSPENDED = 'suspended', 'Suspendida'

    owner = models.OneToOneField(User, on_delete=models.CASCADE, related_name='store')
    name = models.CharField(max_length=100)
    slug = models.SlugField(unique=True)
    description = models.TextField(blank=True, null=True)
    logo = models.ImageField(upload_to='stores/logos/', blank=True, null=True)
    banner = models.ImageField(upload_to='stores/banners/', blank=True, null=True)
    address = models.CharField(max_length=255, blank=True, null=True)
    city = models.CharField(max_length=100, blank=True, null=True)
    phone = models.CharField(max_length=20, blank=True, null=True)
    email = models.EmailField(blank=True, null=True)
    latitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    longitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    status = models.CharField(max_length=20, choices=Status.choices, default=Status.ACTIVE)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Tienda'
        verbose_name_plural = 'Tiendas'

    def __str__(self):
        return self.name

    @property
    def total_products(self):
        return self.products.count()

    @property
    def is_active(self):
        return self.status == self.Status.ACTIVE


class StoreSchedule(models.Model):
    class Day(models.TextChoices):
        MONDAY = 'monday', 'Lunes'
        TUESDAY = 'tuesday', 'Martes'
        WEDNESDAY = 'wednesday', 'Miércoles'
        THURSDAY = 'thursday', 'Jueves'
        FRIDAY = 'friday', 'Viernes'
        SATURDAY = 'saturday', 'Sábado'
        SUNDAY = 'sunday', 'Domingo'

    store = models.ForeignKey(Store, on_delete=models.CASCADE, related_name='schedules')
    day = models.CharField(max_length=10, choices=Day.choices)
    opening_time = models.TimeField()
    closing_time = models.TimeField()
    is_closed = models.BooleanField(default=False)

    class Meta:
        verbose_name = 'Horario'
        verbose_name_plural = 'Horarios'
        unique_together = ('store', 'day')

    def __str__(self):
        return f'{self.store.name} - {self.get_day_display()}'