from django.urls import path
from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('stream/<str:stream_id>/', views.stream_view, name='stream'),
]
