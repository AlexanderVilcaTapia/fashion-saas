from rest_framework import status, generics
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView
from django.shortcuts import get_object_or_404
from products.models import Category, Product, ProductSize, ProductImage
from products.serializers import (
    CategorySerializer,
    ProductSerializer,
    ProductCreateSerializer,
    ProductUpdateSerializer,
    ProductSizeSerializer,
    ProductImageUploadSerializer
)


class CategoryListView(generics.ListCreateAPIView):
    queryset = Category.objects.all()
    serializer_class = CategorySerializer

    def get_permissions(self):
        if self.request.method == 'GET':
            return [AllowAny()]
        return [IsAuthenticated()]


class CategoryDetailView(generics.RetrieveUpdateDestroyAPIView):
    queryset = Category.objects.all()
    serializer_class = CategorySerializer
    lookup_field = 'slug'

    def get_permissions(self):
        if self.request.method == 'GET':
            return [AllowAny()]
        return [IsAuthenticated()]


class ProductListView(generics.ListAPIView):
    serializer_class = ProductSerializer
    permission_classes = (AllowAny,)

    def get_queryset(self):
        queryset = Product.objects.filter(status='active')
        category = self.request.query_params.get('category')
        search = self.request.query_params.get('search')
        store = self.request.query_params.get('store')
        featured = self.request.query_params.get('featured')
        min_price = self.request.query_params.get('min_price')
        max_price = self.request.query_params.get('max_price')
        if category:
            queryset = queryset.filter(category__slug=category)
        if search:
            queryset = queryset.filter(name__icontains=search)
        if store:
            queryset = queryset.filter(store__slug=store)
        if featured:
            queryset = queryset.filter(is_featured=True)
        if min_price:
            queryset = queryset.filter(price__gte=min_price)
        if max_price:
            queryset = queryset.filter(price__lte=max_price)
        return queryset


class ProductDetailView(generics.RetrieveAPIView):
    serializer_class = ProductSerializer
    permission_classes = (AllowAny,)

    def get_object(self):
        store_slug = self.kwargs['store_slug']
        product_slug = self.kwargs['product_slug']
        return get_object_or_404(Product, store__slug=store_slug, slug=product_slug, status='active')


class StoreProductListView(generics.ListCreateAPIView):
    permission_classes = (IsAuthenticated,)

    def get_serializer_class(self):
        if self.request.method == 'POST':
            return ProductCreateSerializer
        return ProductSerializer

    def get_queryset(self):
        return Product.objects.filter(store=self.request.user.store)

    def perform_create(self, serializer):
        serializer.save()


class StoreProductDetailView(generics.RetrieveUpdateDestroyAPIView):
    permission_classes = (IsAuthenticated,)

    def get_serializer_class(self):
        if self.request.method in ('PUT', 'PATCH'):
            return ProductUpdateSerializer
        return ProductSerializer

    def get_object(self):
        return get_object_or_404(
            Product,
            id=self.kwargs['pk'],
            store=self.request.user.store
        )


class ProductSizeView(APIView):
    permission_classes = (IsAuthenticated,)

    def get(self, request, pk):
        product = get_object_or_404(Product, id=pk, store=request.user.store)
        sizes = ProductSize.objects.filter(product=product)
        serializer = ProductSizeSerializer(sizes, many=True)
        return Response(serializer.data)

    def post(self, request, pk):
        product = get_object_or_404(Product, id=pk, store=request.user.store)
        serializer = ProductSizeSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save(product=product)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ProductSizeDetailView(APIView):
    permission_classes = (IsAuthenticated,)

    def patch(self, request, pk, size_id):
        product = get_object_or_404(Product, id=pk, store=request.user.store)
        size = get_object_or_404(ProductSize, id=size_id, product=product)
        serializer = ProductSizeSerializer(size, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, size_id):
        product = get_object_or_404(Product, id=pk, store=request.user.store)
        size = get_object_or_404(ProductSize, id=size_id, product=product)
        size.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class ProductImageView(APIView):
    permission_classes = (IsAuthenticated,)

    def get(self, request, pk):
        product = get_object_or_404(Product, id=pk, store=request.user.store)
        images = ProductImage.objects.filter(product=product)
        serializer = ProductImageUploadSerializer(images, many=True)
        return Response(serializer.data)

    def post(self, request, pk):
        product = get_object_or_404(Product, id=pk, store=request.user.store)
        serializer = ProductImageUploadSerializer(data=request.data, context={'product': product})
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
@permission_classes([AllowAny])
def featured_products_view(request):
    products = Product.objects.filter(status='active', is_featured=True)[:12]
    serializer = ProductSerializer(products, many=True)
    return Response(serializer.data)