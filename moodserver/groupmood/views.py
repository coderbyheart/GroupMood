# -*- coding: utf8 -*-
from django.http import HttpResponseRedirect, HttpResponse, HttpResponseBadRequest
from django.core.urlresolvers import reverse
from django.template import RequestContext
from django.shortcuts import get_object_or_404, render_to_response
from django.utils import simplejson
from django.views.decorators.csrf import *
from django import forms
from models import *
import os
import subprocess
import re
import mimetypes

contexthref = 'http://groupmood.net/jsonld'
modelRelations = {
    Meeting: [(Topic, True, '@id/topics')],
    Topic: [(Question, True, '@id/questions')],
    Question: [(Answer, True, '@id/answers'), (QuestionOption, True, '@id/options')],
}

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
    return '%s/groupmood/%s/%d' % (getBaseHref(request), model.context, model.id)

def modelsToJson(request, models):
    data = []
    for model in models:
        data.append(modelToJson(request, model))
    return data

def modelToJson(request, model):
    data = model.toJsonDict()
    modelJson = {
        '@context': '%s/%s' % (contexthref, model.context),
        '@id': getModelUrl(request, model)
    }
    for k in data:
        modelJson[k] = data[k]
    if type(model) in modelRelations:
        modelJson['@relations'] = []
        for relation in modelRelations[type(model)]:
            relatedModel, isList, href = relation
            modelJson['@relations'].append({
                '@context': '%s/%s' % (contexthref, "relation"),
                'relatedcontext': '%s/%s' % (contexthref, relatedModel.context),
                'list': True if isList else False,
                'href': href.replace('@id', modelJson['@id'])
            })
    return modelJson

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
        return render_to_response('groupmood/meeting_list.html', {'latest_meeting_list': Meeting.objects.order_by('-creation_date')[:25]})
    elif request.method == 'POST':
        # Meeting anlegen
        meeting = Meeting.objects.create(name=request.POST['name'])
        # Standard-Topic zum Bewerten des Meetings anlegen
        voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
        question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
        questionOptionMin = QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MIN_VALUE, value="0")
        questionOptionMax = QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MAX_VALUE, value="100")
        
        resp = jsonResponse(request, modelToJson(request, meeting))
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
        attendeeAppURL = 'grpmd://%s/%d' % (request.META['HTTP_HOST'], meeting.id)
        return render_to_response('groupmood/meeting_detail.html', {'meeting': meeting, 'attendeeAppURL': attendeeAppURL})
    
def meeting_topics(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    meeting = get_object_or_404(Meeting, pk=id)
    topics = Topic.objects.filter(meeting=meeting)
    # Fixe Bild-URLs
    for topic in topics:
        if topic.image != None:
            topic.image = "%s/groupmood/topic/%d/image" % (getBaseHref(request), topic.id)
    return jsonResponse(request, modelsToJson(request, topics))

class PresentationWizardForm(forms.Form):
    name = forms.CharField(max_length=200)
    presentation  = forms.FileField()

@csrf_exempt
def meeting_wizard(request, type):
    """Mit dem Wizard können Meetings mit vorgegebenen Einstellungen angelegt werden."""
    if request.method != 'POST':
        return HttpResponseBadRequest()
    wizardTypes = ['presentation']
    if type not in wizardTypes:
        return HttpResponseBadRequest("Unknown wizard type %s" % type)
    
    form = PresentationWizardForm(request.POST, request.FILES)
    if not form.is_valid():
        return HttpResponseBadRequest()
    
    # Meeting anlegen
    meeting = Meeting.objects.create(name=form.cleaned_data['name'])
    # Standard-Topic zum Bewerten des Meetings anlegen
    voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
    question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MIN_VALUE, value="0")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MAX_VALUE, value="100")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MIN_VALUE, value="Schlecht")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MID_VALUE, value="Mittel")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MAX_VALUE, value="Gut")
    
    archiveFile = 'uploads/presentation-%d.tgz' % meeting.id
    destination = open(archiveFile, 'wb+')
    for chunk in request.FILES['presentation'].chunks():
        destination.write(chunk)
    destination.close()
    
    extractDir = 'uploads/presentation-%d' % meeting.id
    os.mkdir(extractDir)
    try:
        subprocess.check_call(["tar", "-x", "-z", "-f", archiveFile, '--overwrite-dir', '-C', extractDir])
    except CalledProcessError:
        return HttpResponseBadRequest("Failed to extra archive.") 
    os.remove(archiveFile)
    
    # Folie zählen
    slides = []
    for file in os.listdir(extractDir):
        slides.append(file)
    if (len(slides) == 0):
        return HttpResponseBadRequest("No slides found.")
    
    # Topics für alle Folien anlegen
    nslide = 0
    for slide in sorted(slides, key=lambda v: int(re.sub(r'[^0-9]', '', v))):
        nslide = nslide + 1
        slideTopic = Topic.objects.create(meeting=meeting, name="Folie #%d" % nslide, image="%s/%s" % (extractDir, slide))
        # Fragen für jede Folie
        question = Question.objects.create(topic=slideTopic, name="Wie bewerten Sie die Gestaltung dieser Folie?", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MIN_VALUE, value="0")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MAX_VALUE, value="100")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MIN_VALUE, value="Katastrophal")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MID_VALUE, value="In Ordnung")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MAX_VALUE, value="Sehr ansprechend")
        
        question = Question.objects.create(topic=slideTopic, name="Wie verständlich wurde diese Folie erläutert?", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MIN_VALUE, value="0")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MAX_VALUE, value="100")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MIN_VALUE, value="Überhaupt nicht")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MID_VALUE, value="So lala.")
        QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MAX_VALUE, value="Vollkommen")
    
    jsondata = modelToJson(request, meeting);
    resp = jsonResponse(request, jsondata)
    resp['Location'] = getModelUrl(request, meeting)
    resp.status_code = 201;
    return resp

def topic_questions(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    topic = get_object_or_404(Topic, pk=id)
    return jsonResponse(request, modelsToJson(request, Question.objects.filter(topic=topic)))

def topic_image(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    topic = get_object_or_404(Topic, pk=id)
    if topic.image == None:
        return HttpResponseNotFound()
    return HttpResponse(content=open(topic.image).read(), mimetype=mimetypes.guess_type(topic.image)[0])

def topic_entry(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    topic = get_object_or_404(Topic, pk=id)
    if 'json' in request.META.get("Accept", "") or 'json' in request.META.get("HTTP_ACCEPT", ""):
        return jsonResponse(request, modelToJson(request, topic))
    else:
        return render_to_response('groupmood/topic_detail.html', {'topic': topic})

def question_entry(request, id):
    if request.method == 'GET':
        question = get_object_or_404(Question, pk=id)
        if 'json' in request.META.get("Accept", "") or 'json' in request.META.get("HTTP_ACCEPT", ""):
            return jsonResponse(request, modelToJson(request, question))
        else:
            return render_to_response('groupmood/question_detail.html', {'question': question})    
    else:
        return HttpResponseBadRequest()
    
def question_options(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    question = get_object_or_404(Question, pk=id)
    return jsonResponse(request, modelsToJson(request, QuestionOption.objects.filter(question=question)))
    
@csrf_exempt
def answer_create(request, id):
    if request.method == 'POST':
        question = get_object_or_404(Question, pk=id)
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
        return HttpResponseRedirect("/groupmood/meeting/%s" % id)
