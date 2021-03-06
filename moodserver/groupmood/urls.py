# -*- coding: utf8 -*-
"""
    Definiert das Mapping zwischen URLs und Views (siehe views.py)
    
    @author: Markus Tacker <m@coderbyheart.de>
"""
from django.conf.urls.defaults import *
from django.views.generic import DetailView, ListView
from groupmood.models import *
from django.views.generic.simple import redirect_to

urlpatterns = patterns('',
   url(r'^$', redirect_to, {'url': '/groupmood/meeting'}),
   url(r'^meeting$', 'groupmood.views.meeting_list'),
   url(r'^meeting/(?P<id>\d+)$', 'groupmood.views.meeting_entry'),
   url(r'^meeting/(?P<id>\d+)/topics$', 'groupmood.views.meeting_topics'),
   url(r'^meeting/wizard/(?P<type>\w+)', 'groupmood.views.meeting_wizard'),
   url(r'^topic/(?P<id>\d+)$', 'groupmood.views.topic_entry'),
   url(r'^topic/(?P<id>\d+)/questions$', 'groupmood.views.topic_questions'),
   url(r'^topic/(?P<id>\d+)/image$', 'groupmood.views.topic_image'),
   url(r'^topic/(?P<id>\d+)/comments$', 'groupmood.views.topic_comments'),
   url(r'^question/(?P<id>\d+)$', 'groupmood.views.question_entry'),
   url(r'^question/(?P<id>\d+)/answers$', 'groupmood.views.question_answers'),   
   url(r'^question/(?P<id>\d+)/options$', 'groupmood.views.question_options'),
   url(r'^question/(?P<id>\d+)/choices$', 'groupmood.views.question_choices'),
   url(r'^recursive/(?P<model>\w+)/(?P<id>\d+)$', 'groupmood.views.recursive'),
)
