# -*- coding: utf8 -*-
from django.http import HttpResponseRedirect, HttpResponse, HttpResponseBadRequest
from django.core.urlresolvers import reverse
from django.template import RequestContext
from django.shortcuts import get_object_or_404, render_to_response
from django.utils import simplejson
from django.views.decorators.csrf import *
from django import forms
from models import *
from PIL import Image
import os
import subprocess
from subprocess import CalledProcessError
import re
import mimetypes
import sys

contexthref = 'http://groupmood.net/jsonld'
modelRelations = {
    Meeting: [(Topic, True, '@id/topics')],
    Topic: [(Question, True, '@id/questions'), (Comment, True, '@id/comments')],
    Question: [(Answer, True, '@id/answers'), (QuestionOption, True, '@id/options'), (Choice, True, '@id/choices')],
}

class NoRelationException(Exception):
    pass

def getModelRelation(modelType, relatedType):
    for relation in modelRelations[modelType]:
        if relation[0] == relatedType:
            return relation
    raise NoRelationException("No relation of " + relatedType + " on " + modelType + " defined ")

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
    
    # Der @content-Parameter identifiziert das Model
    modelJson = {
        '@context': '%s/%s' % (contexthref, model.context),
    }
    # ID ist die konkrete URL unter dem dieses Entity abgerufen werden kann
    try:
        modelJson['@id'] = getModelUrl(request, model)
    except AttributeError:
        pass
    
    # Daten des Entities auslesen
    for k in data:
        if type(data[k]) == list:
            modelJson[k] = []
            for j in data[k]:
                modelJson[k].append(modelToJson(request, j))
        else:
            try:
                modelJson[k] = modelToJson(request, data[k])
            except AttributeError:
                modelJson[k] = data[k]
    
    # Bei Topics die Bild-URL erzeugen
    if type(model) == Topic:
        if model.image != None:
            modelJson['image'] = "%s/groupmood/topic/%d/image" % (getBaseHref(request), model.id)
    
    # Infos zu relation hinterlegen
    if type(model) in modelRelations:
        modelJson['@relations'] = []
        for relation in modelRelations[type(model)]:
            relatedModel, isList, href = relation
            relationInfo = {
                '@context': '%s/%s' % (contexthref, "relation"),
                'relatedcontext': '%s/%s' % (contexthref, relatedModel.context),
                'list': True if isList else False,
                'href': href.replace('@id', modelJson['@id'])
            };
            # Sind Daten zu dieser Relation hinterleg?
            try:
                if isList:
                    relationInfo['data'] = modelsToJson(request, model.relatedData[relatedModel])
                else:
                    # TODO: Implementieren
                    pass
            except AttributeError:
                pass
            except KeyError:
                pass
            modelJson['@relations'].append(relationInfo)
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
    if request.method != 'GET':
        return HttpResponseBadRequest()
    return render_to_response('groupmood/meeting_list.html', {'latest_meeting_list': Meeting.objects.order_by('-creation_date')[:25]})

def meeting_entry(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    meeting = get_object_or_404(Meeting, pk=id)
    if 'json' in request.META.get("Accept", "") or 'json' in request.META.get("HTTP_ACCEPT", ""):
        return jsonResponse(request, modelToJson(request, meeting))
    else:
        attendeeAppURL = 'grpmd://%s/%d' % (request.META['HTTP_HOST'], meeting.id)
        return render_to_response('groupmood/meeting_detail.html', {'meeting': meeting, 'attendeeAppURL': attendeeAppURL})
    
def createFotoVoteTopic(meeting, request):
    numTopics = Topic.objects.filter(meeting=meeting).count()
    voteTopic = Topic.objects.create(meeting=meeting, name="Photo #%d" % (numTopics + 1))
    question = Question.objects.create(topic=voteTopic, name="Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MIN_VALUE, value="0")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MAX_VALUE, value="100")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MIN_VALUE, value="Schlecht")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MID_VALUE, value="Mittel")
    QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_LABEL_MAX_VALUE, value="Gut")
    
    # TODO: Datei überprüfen
    topicFile = 'uploads/fotovote/%d.jpg' % voteTopic.id
    destination = open(topicFile, 'wb+')
    for chunk in request.FILES['photo'].chunks():
        destination.write(chunk)
    destination.close()

    # Wenigstens die maximale Größe erzwingen    
    image = Image.open(topicFile)
    if image.mode not in ('L', 'RGB'):
        image = image.convert('RGB')
    image.thumbnail((800, 800), Image.ANTIALIAS)
    image.save(topicFile)
    
    voteTopic.image = topicFile
    voteTopic.save()
    
    return voteTopic
    
@csrf_exempt
def meeting_topics(request, id):
    if request.method not in ('GET', 'POST'):
        return HttpResponseBadRequest()
    meeting = get_object_or_404(Meeting, pk=id)
    if request.method == 'GET':
        topics = Topic.objects.filter(meeting=meeting)
        return jsonResponse(request, modelsToJson(request, topics))
    else: # POST
        if 'fotovote' not in meeting.flagList():
            return HttpResponseBadRequest('Creation of topics only allowed for fotovote meetings.')
        voteTopic = createFotoVoteTopic(meeting, request)
        
        jsondata = modelToJson(request, voteTopic);
        resp = jsonResponse(request, jsondata)
        resp['Location'] = getModelUrl(request, voteTopic)
        resp.status_code = 201;
        return resp

class PresentationWizardForm(forms.Form):
    name = forms.CharField(max_length=200)
    presentation = forms.FileField()
    
class FotoVoteWizardForm(forms.Form):
    name = forms.CharField(max_length=200)
    photo = forms.FileField()

@csrf_exempt
def meeting_wizard(request, type):
    """Mit dem Wizard können Meetings mit vorgegebenen Einstellungen angelegt werden."""
    if request.method != 'POST':
        return HttpResponseBadRequest('Invalid method.')
    wizardTypes = ['presentation', 'fotovote', 'test1', 'test2']
    if type not in wizardTypes:
        return HttpResponseBadRequest("Unknown wizard type %s" % type)
    
    if type == 'test1':
        meeting = Meeting.objects.create(name="Test-Meeting")
        # Standard-Topic zum Bewerten des Meetings anlegen
        voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
        question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
        questionOptionMin = QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MIN_VALUE, value="0")
        questionOptionMax = QuestionOption.objects.create(question=question, key=Question.OPTION_RANGE_MAX_VALUE, value="100")
    elif type == 'test2':
        meeting = Meeting.objects.create(name="Choice-Test")
        # Standard-Topic zum Bewerten des Meetings anlegen
        voteTopic = Topic.objects.create(meeting=meeting, name="Choices")
        question = Question.objects.create(topic=voteTopic, name="Single-Choice", type=Question.TYPE_CHOICE, mode=Question.MODE_SINGLE)
        QuestionOption.objects.create(question=question, key=Question.OPTION_MIN_CHOICES, value="1")
        QuestionOption.objects.create(question=question, key=Question.OPTION_MAX_CHOICES, value="1")
        Choice.objects.create(question=question, name="Rot")
        Choice.objects.create(question=question, name="Gelb")
        Choice.objects.create(question=question, name="Grün")
    elif type == 'fotovote':
        form = FotoVoteWizardForm(request.POST, request.FILES)
        if not form.is_valid():
            return HttpResponseBadRequest('Invalid data.')
        
        meeting = Meeting.objects.create(name=request.POST['name'], flags='fotovote')
        voteTopic = createFotoVoteTopic(meeting, request)
        
    elif type == 'presentation':
        form = PresentationWizardForm(request.POST, request.FILES)
        if not form.is_valid():
            print request.POST
            print request.FILES
            print form.errors
            return HttpResponseBadRequest('Invalid data.')
        
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

@csrf_exempt
def topic_comments(request, id):
    if request.method not in ('GET', 'POST'):
        return HttpResponseBadRequest()
    topic = get_object_or_404(Topic, pk=id)
    if request.method == 'GET':
        return jsonResponse(request, modelsToJson(request, Comment.objects.filter(topic=topic).order_by('-creation_date')))
    else: # if request.method == 'POST':
        comment = Comment.objects.create(topic=topic, user=getUser(request), comment=request.POST['comment'])
        resp = jsonResponse(request, modelToJson(request, comment))
        resp['Location'] = getModelUrl(request, comment)
        resp.status_code = 201;
        return resp        

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
        return render_to_response('groupmood/topic_detail.html', {'topic': topic, 'comments': Comment.objects.filter(topic=topic).order_by('-creation_date')})

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
    
def question_choices(request, id):
    if request.method != 'GET':
        return HttpResponseBadRequest()
    question = get_object_or_404(Question, pk=id)
    return jsonResponse(request, modelsToJson(request, Choice.objects.filter(question=question)))

def createSingleAnswerResponse(request, answer):
    """Gibt einen HTTP-Code 201 mit Daten zu einer Antwort zurück"""
    resp = jsonResponse(request, modelToJson(request, answer))
    resp['Location'] = getModelUrl(request, answer)
    resp.status_code = 201;
    return resp  

def createMultipleAnswerResponse(request, question, answers):
    """Gibt einen HTTP-Code 201 mit Daten zu mehreren Antwort zurück"""
    resp = jsonResponse(request, modelsToJson(request, answers))
    relatedModel, isList, href = getModelRelation(Question, Answer)
    resp['Location'] = href.replace('@id', getModelUrl(request, question))
    resp.status_code = 201;
    return resp  

@csrf_exempt
def question_answers(request, id):
    if request.method not in ('GET', 'POST'):
        return HttpResponseBadRequest()
    question = get_object_or_404(Question, pk=id)
    if request.method == 'POST':
        if (question.type == Question.TYPE_RANGE):
            answerValue = request.POST['answer']
            minValue = question.getMin()
            maxValue = question.getMax()
            if minValue != None and int(minValue) > int(answerValue):
                return HttpResponseBadRequest("%d is greater than %d" % (int(answerValue), int(minValue)))                    
            if maxValue != None and int(maxValue) < int(answerValue):
                return HttpResponseBadRequest("%d is smaller than %d" % (int(answerValue), int(maxValue)))
            answer = Answer.objects.create(question=question, user=getUser(request), answer=answerValue)
            return createSingleAnswerResponse(request, answer)
        else: # (question.type == Question.TYPE_CHOICE):
            minChoices = question.getMinChoices()
            maxChoices = question.getMaxChoices()
            if maxChoices != None and int(maxChoices) == 1: # Single-Choice
                answer = Answer.objects.create(question=question, user=getUser(request), answer=request.POST['answer'])
                return createSingleAnswerResponse(request, answer)
            else: # Multiple-Choice
                answerValues = []
                for answerValue in request.POST.getlist('answer[]'):
                    answerValues.append(answerValue)
                if minChoices != None and int(minChoices) > len(answerValues):
                    return HttpResponseBadRequest("Too few choices. %d given, %d required" % (len(answerValues), int(minChoices)))
                if maxChoices != None and int(maxChoices) < len(answerValues):
                    return HttpResponseBadRequest("Too many choices. %d given, %d allowed" % (len(answerValues), int(maxChoices)))
                answers = []
                for answerValue in answerValues:
                    answer = Answer.objects.create(question=question, user=getUser(request), answer=answerValue)
                    answers.append(answer)
                return createMultipleAnswerResponse(request, question, answers)
    else:
        return jsonResponse(request, modelsToJson(request, Answer.objects.filter(question=question)))

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

def recursive(request, model, id):
    """Diese View verfolgt alle Child-Elemente und gibt diese 
    als eine große Antwort zurück. Das spart HTTP-Requests."""
    if request.method != ('GET'):
        return HttpResponseBadRequest('Only GET supported.')
    
    try:
        modelClass = getattr(sys.modules[__name__], model.capitalize())
    except AttributeError:
        return HttpResponseBadRequest('Unknown model %s' % model.capitalize())
    
    entity = get_object_or_404(modelClass, pk=id)
    entity = recursiveFetch(request, entity)
    return jsonResponse(request, modelToJson(request, entity))

ignoreInRecursion = [Answer, Comment]
    
def recursiveFetch(request, entity):
    if not type(entity) in modelRelations:
        return entity
    entity.relatedData = {} 
    for relation in modelRelations[type(entity)]:
        relatedModel, isList, href = relation
        if relatedModel in ignoreInRecursion: 
            continue
        if isList:
            entityList = []
            kwargs = {entity.context: entity}
            for relatedEntity in relatedModel.objects.filter(**kwargs):
                entityList.append(recursiveFetch(request, relatedEntity))
            entity.relatedData[relatedModel] = entityList
    return entity
