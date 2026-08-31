from django.shortcuts import render

def index(request):
    return render(request, 'stream/index.html')

def stream_view(request, stream_id):
    return render(request, 'stream/stream.html', {'stream_id': stream_id})
