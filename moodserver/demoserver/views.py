from django.http import HttpResponseRedirect, HttpResponse, HttpResponseBadRequest
from django.core.urlresolvers import reverse
from django.template import RequestContext
from django.shortcuts import get_object_or_404, render_to_response
from django.utils import simplejson
from django.views.decorators.csrf import *
from models import *

def getBaseHref(request):
    hostname = request.META['HTTP_HOST'] if 'HTTP_HOST' in request.META else 'localhost'
    return 'http%s://%s' % (('s' if request.is_secure() else ''), hostname)

def getModelUrl(request, model):
    return '%s/demoserver/%s/%d' % (getBaseHref(request), model.context, model.id) 

def jsonResponse(request, model):
    context = model.context
    contexthref = 'http://groupmood.net/jsonld'
    resp = {}
    resp['status'] = {
        '@context': '%s/apistatus' % contexthref,
        'code': 1,
        'message': 'ok'}
    resp['result'] = {
        '@context': '%s/%s' % (contexthref, context),
        '@subject': getModelUrl(request, model)
    }
    data = model.toJsonDict()
    for k in data:
        resp['result'][k] = data[k]
    return HttpResponse(content=simplejson.dumps(resp), mimetype="application/json")   

def jsonRequest(request):
    jsondata = request.read()
    if not jsondata: 
        return {}
    return simplejson.loads(jsondata)

def meeting_list(request):
    if request.method == 'GET':
        pass
    elif request.method == 'POST':
        meeting = Meeting.objects.create(name=request.POST['name'])
        meeting.save()
        resp = jsonResponse(request, meeting)
        resp['Location'] = getModelUrl(request, meeting)
        resp.status_code = 201;
        return resp        
    else:
        return HttpResponseBadRequest()

def meeting_entry(request, meeting_id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    meeting = get_object_or_404(Meeting, pk=meeting_id)
    if 'json' in request.META.get("HTTP_ACCEPT", ""):
        return jsonResponse(request, meeting)
    else:
        return render_to_response('demoserver/meeting_detail.html', {'meeting': meeting})

@csrf_exempt
def meeting_vote(request, meeting_id):
    if request.method == 'GET':
        return HttpResponseBadRequest()
    meeting = get_object_or_404(Meeting, pk=meeting_id)
    if 'json' in request.META['CONTENT_TYPE']:
        data = jsonRequest(request)
    else:
        data = request.POST
    if not 'vote' in data:
        return HttpResponseBadRequest("Missing vote")
    v = Vote()
    v.vote = int(data['vote'])
    v.meeting = meeting
    v.save()
    if 'json' in request.META['CONTENT_TYPE']:
        return jsonResponse(request, meeting)
    else:
        return HttpResponseRedirect("/demoserver/meeting/%s" % meeting_id)
