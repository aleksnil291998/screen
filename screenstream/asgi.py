"""
ASGI config for screenstream project.

It exposes the ASGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/6.1/howto/deployment/asgi/
"""

import os
from channels.routing import ProtocolTypeRouter, URLRouter
from channels.auth import AuthMiddlewareStack
from django.core.asgi import get_asgi_application
from django.urls import path
from stream import consumers

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'screenstream.settings')

application = ProtocolTypeRouter({
    'http': get_asgi_application(),
    'websocket': AuthMiddlewareStack(
        URLRouter([
            path('ws/stream/<str:stream_id>/', consumers.StreamConsumer.as_asgi()),
        ])
    ),
})
