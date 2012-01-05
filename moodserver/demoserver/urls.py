from django.conf.urls.defaults import *
from django.views.generic import DetailView, ListView
from demoserver.models import Meeting

urlpatterns = patterns('',
   url(r'^meeting$', 'demoserver.views.meeting_list'),
   url(r'^meeting/(?P<id>\d+)$', 'demoserver.views.meeting_entry'),
   url(r'^question/(?P<id>\d+)$', 'demoserver.views.question_entry'),
   url(r'^question/(?P<question_id>\d+)/answer$', 'demoserver.views.answer_create'),
)