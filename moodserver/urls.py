from django.conf.urls.defaults import *

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^demoserver/', include('demoserver.urls')),
    url(r'^admin/', include(admin.site.urls))
)
