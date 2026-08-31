import json
from channels.generic.websocket import AsyncWebsocketConsumer
from channels.db import database_sync_to_async

class StreamConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        self.stream_id = self.scope['url_route']['kwargs']['stream_id']
        self.room_group_name = f'stream_{self.stream_id}'
        
        # Join room group
        await self.channel_layer.group_add(
            self.room_group_name,
            self.channel_name
        )
        
        await self.accept()
    
    async def disconnect(self, close_code):
        # Leave room group
        await self.channel_layer.group_discard(
            self.room_group_name,
            self.channel_name
        )
    
    # Receive message from WebSocket (Android client)
    async def receive(self, text_data=None, bytes_data=None):
        if bytes_data:
            # Send image data to all viewers in the group
            await self.channel_layer.group_send(
                self.room_group_name,
                {
                    'type': 'stream_frame',
                    'data': bytes_data
                }
            )
    
    # Receive message from room group
    async def stream_frame(self, event):
        # Send frame to WebSocket
        await self.send(bytes_data=event['data'])
