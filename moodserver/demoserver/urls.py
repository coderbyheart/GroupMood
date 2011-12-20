from django.conf.urls.defaults import *
from django.views.generic import DetailView, ListView
from demoserver.models import Meeting

urlpatterns = patterns('',
    url(r'^meeting/$',
        ListView.as_view(
            queryset=Meeting.objects.order_by('-creation_date')[:25],
            context_object_name='latest_meeting_list',
            template_name='demoserver/meeting_list.html')),
   url(r'^meeting/(?P<meeting_id>\d+)$', 'demoserver.views.meeting_view'),
   url(r'^meeting/(?P<meeting_id>\d+)/vote$', 'demoserver.views.meeting_vote'),
)