from django.conf.urls.defaults import *
from django.views.generic import DetailView, ListView
from demoserver.models import *

urlpatterns = patterns('',
   url(r'^meeting$', 'demoserver.views.meeting_list'),
   url(r'^meeting/(?P<id>\d+)$', 'demoserver.views.meeting_entry'),
   url(r'^topic/(?P<pk>\d+)$', DetailView.as_view(model=Topic, template_name='demoserver/topic_detail.html')),
   url(r'^question/(?P<id>\d+)$', 'demoserver.views.question_entry'),
   url(r'^question/(?P<question_id>\d+)/answer$', 'demoserver.views.answer_create'),
   
)
