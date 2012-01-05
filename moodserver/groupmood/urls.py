from django.conf.urls.defaults import *
from django.views.generic import DetailView, ListView
from groupmood.models import *

urlpatterns = patterns('',
   url(r'^meeting$', 'groupmood.views.meeting_list'),
   url(r'^meeting/(?P<id>\d+)$', 'groupmood.views.meeting_entry'),
   url(r'^topic/(?P<pk>\d+)$', DetailView.as_view(model=Topic, template_name='groupmood/topic_detail.html')),
   url(r'^question/(?P<id>\d+)$', 'groupmood.views.question_entry'),
   url(r'^question/(?P<question_id>\d+)/answer$', 'groupmood.views.answer_create'),
   
)
