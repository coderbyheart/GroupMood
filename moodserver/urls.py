from django.conf.urls.defaults import *
from django.views.generic.simple import redirect_to

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^$', redirect_to, {'url': '/groupmood/'}),
    url(r'^groupmood/', include('groupmood.urls')),
    url(r'^admin/', include(admin.site.urls))
)
