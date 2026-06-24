from rest_framework import status, generics
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView
from django.shortcuts import get_object_or_404
from stores.models import Store, StoreSchedule
from stores.serializers import (
    StoreSerializer,
    StoreCreateSerializer,
    StoreUpdateSerializer,
    StoreScheduleSerializer,
    StoreDashboardSerializer
)


class StoreListView(generics.ListAPIView):
    serializer_class = StoreSerializer
    permission_classes = (AllowAny,)

    def get_queryset(self):
        queryset = Store.objects.filter(status='active')
        city = self.request.query_params.get('city')
        search = self.request.query_params.get('search')
        if city:
            queryset = queryset.filter(city__icontains=city)
        if search:
            queryset = queryset.filter(name__icontains=search)
        return queryset


class StoreDetailView(generics.RetrieveAPIView):
    serializer_class = StoreSerializer
    permission_classes = (AllowAny,)
    lookup_field = 'slug'

    def get_queryset(self):
        return Store.objects.filter(status='active')


class StoreCreateView(generics.CreateAPIView):
    serializer_class = StoreCreateSerializer
    permission_classes = (IsAuthenticated,)

    def perform_create(self, serializer):
        if hasattr(self.request.user, 'store'):
            from rest_framework.exceptions import ValidationError
            raise ValidationError('Ya tienes una tienda registrada.')
        serializer.save()
        self.request.user.role = 'store_owner'
        self.request.user.save()


class StoreUpdateView(generics.RetrieveUpdateAPIView):
    serializer_class = StoreUpdateSerializer
    permission_classes = (IsAuthenticated,)

    def get_object(self):
        return get_object_or_404(Store, owner=self.request.user)


class StoreDashboardView(generics.RetrieveAPIView):
    serializer_class = StoreDashboardSerializer
    permission_classes = (IsAuthenticated,)

    def get_object(self):
        return get_object_or_404(Store, owner=self.request.user)


class StoreScheduleView(APIView):
    permission_classes = (IsAuthenticated,)

    def get(self, request):
        store = get_object_or_404(Store, owner=request.user)
        schedules = StoreSchedule.objects.filter(store=store)
        serializer = StoreScheduleSerializer(schedules, many=True)
        return Response(serializer.data)

    def post(self, request):
        store = get_object_or_404(Store, owner=request.user)
        serializer = StoreScheduleSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save(store=store)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
@permission_classes([AllowAny])
def featured_stores_view(request):
    stores = Store.objects.filter(status='active').order_by('-created_at')[:6]
    serializer = StoreSerializer(stores, many=True)
    return Response(serializer.data)


@api_view(['GET'])
@permission_classes([AllowAny])
def store_products_view(request, slug):
    store = get_object_or_404(Store, slug=slug, status='active')
    from products.models import Product
    from products.serializers import ProductSerializer
    products = Product.objects.filter(store=store, status='active')
    category = request.query_params.get('category')
    search = request.query_params.get('search')
    if category:
        products = products.filter(category__slug=category)
    if search:
        products = products.filter(name__icontains=search)
    serializer = ProductSerializer(products, many=True)
    return Response(serializer.data)