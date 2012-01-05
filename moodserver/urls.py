from django.conf.urls.defaults import *

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^groupmood/', include('groupmood.urls')),
    url(r'^admin/', include(admin.site.urls))
)
