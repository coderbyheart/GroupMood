from django.conf.urls.defaults import *
from django.views.generic import DetailView, ListView
from demoserver.models import Meeting

urlpatterns = patterns('',
   url(r'^meeting$', 'demoserver.views.meeting_list'),
   url(r'^meeting/(?P<meeting_id>\d+)$', 'demoserver.views.meeting_view'),
   url(r'^meeting/(?P<meeting_id>\d+)/vote$', 'demoserver.views.meeting_vote'),
)