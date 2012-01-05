from django.http import HttpResponseRedirect, HttpResponse, HttpResponseBadRequest
from django.core.urlresolvers import reverse
from django.template import RequestContext
from django.shortcuts import get_object_or_404, render_to_response
from django.utils import simplejson
from django.views.decorators.csrf import *
from models import *

contexthref = 'http://groupmood.net/jsonld'

def getUser(request):
    userMatch = User.objects.filter(ip=request.META['REMOTE_ADDR'])
    if userMatch:
        return userMatch[0]
    user = User.objects.create(ip=request.META['REMOTE_ADDR'])
    return user

def getBaseHref(request):
    hostname = request.META['HTTP_HOST'] if 'HTTP_HOST' in request.META else 'localhost'
    return 'http%s://%s' % (('s' if request.is_secure() else ''), hostname)

def getModelUrl(request, model):
    return '%s/demoserver/%s/%d' % (getBaseHref(request), model.context, model.id)

def modelToJson(request, model):
    data = model.toJsonDict()
    modeljson = {
        '@context': '%s/%s' % (contexthref, model.context),
        '@id': getModelUrl(request, model)
    }
    for k in data:
        modeljson[k] = data[k]
    return modeljson

def jsonResponse(request, result):
    resp = {}
    resp['status'] = {
        '@context': '%s/apistatus' % contexthref,
        'code': 1,
        'message': 'ok'}
    resp['result'] = result
    return HttpResponse(content=simplejson.dumps(resp), mimetype="application/json")   

def jsonRequest(request):
    jsondata = request.read()
    if not jsondata: 
        return {}
    return simplejson.loads(jsondata)

def meeting_list(request):
    if request.method == 'GET':
        return render_to_response('demoserver/meeting_list.html', {'latest_meeting_list': Meeting.objects.order_by('-creation_date')[:25]})
    elif request.method == 'POST':
        # Meeting anlegen
        meeting = Meeting.objects.create(name=request.POST['name'])
        # Standard-Topic zum Bewerten des Meetings anlegen
        voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
        question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
        questionOptionMin = QuestionOption.objects.create(question=question, key="min_value", value="0")
        questionOptionMax = QuestionOption.objects.create(question=question, key="max_value", value="100")
        
        meetingJson = modelToJson(request, meeting)
        topicJson = modelToJson(request, voteTopic)
        questionJson = modelToJson(request, question)
        questionOptionMaxJson = modelToJson(request, questionOptionMax)
        questionOptionMinJson = modelToJson(request, questionOptionMin)
        meetingJson['topics'] = [topicJson]
        topicJson['questions'] = [questionJson]
        questionJson['options'] = [questionOptionMinJson, questionOptionMaxJson]
        
        meetingJson['@context'] = [meetingJson['@context'], {'topics': topicJson['@context'], '@list': True}]
        topicJson['@context'] = [topicJson['@context'], {'questions': questionJson['@context'], '@list': True}]
        questionJson['@context'] = [questionJson['@context'], {'options': questionOptionMaxJson['@context'], '@list': True}]
        
        resp = jsonResponse(request, meetingJson)
        resp['Location'] = getModelUrl(request, meeting)
        resp.status_code = 201;
        return resp        
    else:
        return HttpResponseBadRequest()

def meeting_entry(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    meeting = get_object_or_404(Meeting, pk=id)
    if 'json' in request.META.get("Accept", "") or 'json' in request.META.get("HTTP_ACCEPT", ""):
        return jsonResponse(request, modelToJson(request, meeting))
    else:
        appURL = 'groupmood://%s/demoserver/meeting/%d' % (request.META['HTTP_HOST'], meeting.id)
        return render_to_response('demoserver/meeting_detail.html', {'meeting': meeting, 'appURL': appURL})

def question_entry(request, id):
    if request.method == 'GET':
        question = get_object_or_404(Question, pk=id)
        if 'json' in request.META.get("Accept", "") or 'json' in request.META.get("HTTP_ACCEPT", ""):
            return jsonResponse(request, modelToJson(request, question))
        else:
            return render_to_response('demoserver/question_detail.html', {'question': question})    
    else:
        return HttpResponseBadRequest()
    
@csrf_exempt
def answer_create(request, question_id):
    if request.method == 'POST':
        question = get_object_or_404(Question, pk=question_id)
        # Antwort dazu
        answer = Answer.objects.create(question=question, user=getUser(request), answer=request.POST['answer'])
        resp = jsonResponse(request, modelToJson(request, answer))
        resp['Location'] = getModelUrl(request, answer)
        resp.status_code = 201;
        return resp        
    else:
        return HttpResponseBadRequest()

@csrf_exempt
def meeting_vote(request, id):
    if request.method == 'GET':
        return HttpResponseBadRequest()
    meeting = get_object_or_404(Meeting, pk=id)
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
        return HttpResponseRedirect("/demoserver/meeting/%s" % id)
