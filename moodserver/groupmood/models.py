# -*- coding: utf8 -*-
from django.db import models
from validators import validate_percent, validate_nonzeropositive

class BaseModel(models.Model):
    'Abstrakte Basisklasse für alle Models der Anwendung.'
    creation_date = models.DateTimeField('date created', auto_now_add=True)

    class Meta:
        abstract = True

class Meeting(BaseModel):
    """
    Meetings sind das übergeordnetes Model, unter dem alle anderen Daten organisiert sind.
    
    # Create a Meeting
    >>> meeting = Meeting.objects.create(name="Test")
    >>> meeting.name
    'Test'
    >>> meeting.numTopics()
    0
    >>> meeting.flags == None
    True
    >>> meeting.flagList()
    []
    
    """
    context = 'meeting'
    name = models.CharField(max_length=200)
    flags = models.CharField(max_length=200,null=True,blank=True)
    
    def __unicode__(self):
        return "Meeting #%d: %s" % (self.id, self.name)
      
    def topics(self):
        return Topic.objects.filter(meeting=self.id)
    
    def numTopics(self):
        return len(self.topics())
    
    def flagList(self):
        return self.flags.split("|") if self.flags != None else []
        
    def toJsonDict(self):
        return {
            'id': self.id, 
            'name': self.name, 
            'numTopics': self.numTopics(),
            'flags': self.flags, 
            'creationDate': self.creation_date.isoformat(),
        }
    
class User(BaseModel):
    """
    Definiert einen Nutzer, identifiziert anhand seiner IP-Adresses
    
    Aktuell wird keine Nutzer-Registrierung verwendet.
    
    """
    context = 'user'
    ip = models.IPAddressField(unique=True)
    
    def toJsonDict(self):
        return {'id': self.id, 'ip': self.ip}
    
class Topic(BaseModel):
    """
    Thema in einem Meeting. Damit können Nutzer interagieren.
    
    Ein Topic kann z.B. die Bewertung des Meetings selber sein.
    
    # Create a Meeting
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    
    # Create a slide topic
    >>> slideTopic = Topic.objects.create(meeting=meeting, name="Folie 1")
    >>> slideTopic.name
    'Folie 1'
    >>> slideTopic.image == None
    True

    # Create the vote topic
    >>> voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
    
    >>> meeting.numTopics()
    2
    
    >>> voteTopic.numQuestions()
    0
    
    >>> voteTopic.numComments()
    0
    
    """
    context = 'topic'
    meeting = models.ForeignKey(Meeting)
    name = models.CharField(max_length=200)
    image = models.URLField(null=True,blank=True)
    
    class Meta:
        unique_together = (("meeting", "name"),)
    
    def __unicode__(self):
        return "Topic #%d: %s of %s" % (self.id, self.name, unicode(self.meeting))
    
    def numQuestions(self):
        return len(self.questions())
    
    def questions(self):
        return Question.objects.filter(topic=self.id)
    
    def numComments(self):
        return len(self.comments())
    
    def comments(self):
        return Comment.objects.filter(topic=self.id)
    
    def toJsonDict(self):
        return {
            'id': self.id, 
            'name': self.name, 
            'image': self.image,
            'creationDate': self.creation_date.isoformat(),
        }
    
class Comment(BaseModel):
    """
    Kommentar zu einem Topic
    
    Neben Antworten, die mathematisch ausgewertet werden können, können auch einfache Kommentare zu Themen hinterlassen werden.
    
    >>> meeting = Meeting.objects.create(name="Meeting with Comments")
    >>> voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
    >>> user = User.objects.create(ip="127.0.0.1")
    >>> comment = Comment.objects.create(topic=voteTopic, user=user, comment="Kommentar zum Text.")
    >>> comment.comment
    'Kommentar zum Text.'
    >>> comment.creation_date != None
    True
    >>> comment = Comment.objects.create(topic=voteTopic, user=user, comment="Noch ein Kommentar zum Text.")
    >>> voteTopic.numComments()
    2
    
    """
    context = 'comment'
    topic = models.ForeignKey(Topic)
    user = models.ForeignKey(User)
    comment = models.CharField(max_length=200)
    
    def __unicode__(self):
        return "Comment #%d: %s on %s" % (self.id, self.comment, unicode(self.slide))
    
    def toJsonDict(self):
        return {
            'id': self.id, 
            'comment': self.comment, 
            'creationDate': self.creation_date.isoformat(),
            'user': self.user.ip
        }

class Question(BaseModel):
    """
    Eine Frage zu einem Topic
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
    >>> question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
    >>> question.name
    'Allgemeine Bewertung'
    >>> question.type == Question.TYPE_RANGE
    True
    >>> question.mode == Question.MODE_AVERAGE
    True
    >>> question.numAnswers()
    0
    >>> question.avg()
    0
    >>> question.averageAnswers()
    []
    
    """
    context = 'question'
    topic = models.ForeignKey(Topic)
    name = models.CharField(max_length=200)
    TYPE_CHOICE = 'choice'
    TYPE_RANGE = 'range'
    TYPES = (
        (TYPE_CHOICE, 'Choice'),
        (TYPE_RANGE, 'Range')
    )
    type = models.CharField(max_length=20, choices=TYPES, default=TYPE_CHOICE)
    MODE_SINGLE = 'single'
    MODE_AVERAGE = 'avg'
    MODES = (
        (MODE_SINGLE, 'Single-Vote'),
        (MODE_AVERAGE, 'Average-Vote')
    )
    mode = models.CharField(max_length=20, choices=MODES, default=MODE_AVERAGE)

    OPTION_RANGE_MIN_VALUE = "min_value"
    OPTION_RANGE_MAX_VALUE = "max_value"
    OPTION_RANGE_LABEL_MIN_VALUE = "label_min_value"
    OPTION_RANGE_LABEL_MID_VALUE = "label_mid_value"
    OPTION_RANGE_LABEL_MAX_VALUE = "label_max_value"

    OPTION_MIN_CHOICES = "min_choices"
    OPTION_MAX_CHOICES = "max_choices"
    
    def answers(self):
        return Answer.objects.filter(question=self.id)
    
    def numAnswers(self):
        return len(self.answers());
    
    def toJsonDict(self):
        type = filter(lambda t: t[0] == self.type, self.TYPES)[0][0]
        mode = filter(lambda m: m[0] == self.mode, self.MODES)[0][0]
        d = {
            'id': self.id, 
            'name': self.name, 
            'type': type, 
            'mode': mode, 
            'numAnswers': self.numAnswers(),
            'creationDate': self.creation_date.isoformat(),
            'avg': self.avg(),
            'averageAnswers': self.averageAnswers()
        }
        return d
    
    def avg(self):
        if self.mode != self.MODE_AVERAGE:
            return 0
        votes = self.answers()
        
        if len(votes) == 0:
            return (int(self.getMax(0)) - int(self.getMin(0))) / 2
        sum = 0
        for v in votes:
            sum += int(v.answer)
        return sum / len(votes)
    
    def averageAnswers(self):
        """Berechnet für alle Abgegebenen Antworten einer Choice-Question die Durchschnittswerte"""
        if self.type != self.TYPE_CHOICE:
            return []
        answerCount = []
        # Choices laden
        for choice in Choice.objects.filter(question=self):
            numVotes = Answer.objects.filter(question=self, answer=choice.name).count()
            answerCount.append({'name': choice.name, 'numVotes': numVotes})
        totalVotes = reduce(lambda total, avgAnswer: total + avgAnswer['numVotes'], answerCount, 0)
        avgAnswers = []
        for a in answerCount:
            avgAnswer = AnswerAverage()
            avgAnswer.answer = a['name']
            avgAnswer.numVotes = a['numVotes']
            avgAnswer.average = int(a['numVotes'] / float(totalVotes) * 100.0) if totalVotes > 0 else 0
            avgAnswer.question = self
            avgAnswers.append(avgAnswer)
            
        # Sort by average
        return sorted(avgAnswers, key=lambda avgAnswer: avgAnswer.average, reverse=True)

    def getMin(self, default=None):
        """Gibt den Min-Wert der Range zurück"""
        return self.getOption(self.OPTION_RANGE_MIN_VALUE, default);
        
    def getMax(self, default=None):
        """Gibt den Max-Wert der Range zurück"""
        return self.getOption(self.OPTION_RANGE_MAX_VALUE, default);
    
    def getMinLabel(self):
        """Gibt das Label für den Min-Wert zurück"""
        return self.getOption(self.OPTION_RANGE_LABEL_MIN_VALUE);
    
    def getMidLabel(self):
        """Gibt das Label für den Mittel-Wert zurück"""
        return self.getOption(self.OPTION_RANGE_LABEL_MID_VALUE);
        
    def getMaxLabel(self):
        """Gibt das Label für den Max-Wert zurück"""
        return self.getOption(self.OPTION_RANGE_LABEL_MAX_VALUE);
    
    def getMinChoices(self, default=None):
        """Gibt die Anzahl der Optionen zurück, die mindestens ausgewählt werden müssen"""
        return self.getOption(self.OPTION_MIN_CHOICES, default);
        
    def getMaxChoices(self, default=None):
        """Gibt die Anzahl der Optionen zurück, die maximal ausgewählt werden dürfen"""
        return self.getOption(self.OPTION_MAX_CHOICES, default);
        
    def getOption(self, name, default=None):
        """Gibt eine Option dieser Frage zurück"""
        v = QuestionOption.objects.filter(question=self, key=name)
        return v[0].value if v else default
    
    def __unicode__(self):
        return "Frage #%d: %s of %s" % (self.id, self.name, unicode(self.topic))
    
class QuestionOption(BaseModel):
    """
    Definiert die Details einer Frage, in Form von Key/Value-Paare
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
    >>> question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
    >>> questionOptionMin = QuestionOption.objects.create(question=question, key="min_value", value="0")
    >>> questionOptionMax = QuestionOption.objects.create(question=question, key="max_value", value="100")
    >>> questionOptionMax.key
    'max_value'
    >>> questionOptionMax.value
    '100'
    
    """
    context = 'questionoption'
    question = models.ForeignKey(Question)
    key = models.CharField(max_length=255)
    value = models.CharField(max_length=255)
    
    class Meta:
        unique_together = (("question", "key"),)
    
    def __unicode__(self):
        return "QuestionOption #%d: %s = %s for %s" % (self.id, self.key, self.value, unicode(self.question))
    
    def toJsonDict(self):
        return {
            'id': self.id, 
            'key': self.key, 
            'value': self.value,
            'creationDate': self.creation_date.isoformat(),
        }

class Choice(BaseModel):
    """
    Definiert die Antwort-Option bei Choice-Fragen
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> topic = Topic.objects.create(meeting=meeting, name="Frage 1")
    >>> question = Question.objects.create(topic=topic, name="Was ist blau?", type=Question.TYPE_CHOICE, mode=Question.MODE_SINGLE)
    >>> choice1 = Choice.objects.create(question=question, name="Feuer")
    >>> choice1.name
    'Feuer'
    >>> choice2 = Choice.objects.create(question=question, name="Himmel")
    
    """
    context = 'choice'
    question = models.ForeignKey(Question)
    name = models.CharField(max_length=200)
    
    class Meta:
        unique_together = (("question", "name"),)
    
    def __unicode__(self):
        return "Choice #%d: %s on %s" % (self.id, self.name, unicode(self.question))

    def toJsonDict(self):
        return {
            'id': self.id, 
            'name': self.name,
            'creationDate': self.creation_date.isoformat(),
        }

class Answer(BaseModel):
    """
    
    Definiert die Antwort zu einer Frage
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
    >>> question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
    >>> o = QuestionOption.objects.create(question=question, key="min_value", value="0")
    >>> o = QuestionOption.objects.create(question=question, key="max_value", value="100")
    >>> user = User.objects.create(ip="127.0.0.2")
    >>> answer = Answer.objects.create(question=question, user=user, answer="50")
    >>> answer.answer
    '50' 
    
    """
    context = 'answer'
    question = models.ForeignKey(Question)
    user = models.ForeignKey(User)
    answer = models.CharField(max_length=200)

    def __unicode__(self):
        return "Answer #%d: %s on %s" % (self.id, self.answer, unicode(self.question))
    
    def toJsonDict(self):
        return {'id': self.id, 'answer': self.answer, 'user': self.user,  'creationDate': self.creation_date.isoformat()}

class AnswerAverage(object):
    """
    
    Gibt an, wieviele abgebene Stimmen zu einer Frage diese Antwort hatte
    
    >>> meeting = Meeting.objects.create(name="Choice-Meeting")
    >>> voteTopic = Topic.objects.create(meeting=meeting, name="Choices")
    >>> singleChoiceQuestion = Question.objects.create(topic=voteTopic, name="Single-Choice", type=Question.TYPE_CHOICE, mode=Question.MODE_SINGLE)
    >>> o = QuestionOption.objects.create(question=singleChoiceQuestion, key="min_choices", value="1")
    >>> o = QuestionOption.objects.create(question=singleChoiceQuestion, key="max_choices", value="1")
    >>> c = Choice.objects.create(question=singleChoiceQuestion, name="Rot")
    >>> c = Choice.objects.create(question=singleChoiceQuestion, name="Gelb")
    >>> c = Choice.objects.create(question=singleChoiceQuestion, name="Grün") 
    >>> multipleChoiceQuestion = Question.objects.create(topic=voteTopic, name="Multiple-Choice", type=Question.TYPE_CHOICE, mode=Question.MODE_SINGLE)
    >>> o = QuestionOption.objects.create(question=multipleChoiceQuestion, key="min_choices", value="1")
    >>> o = QuestionOption.objects.create(question=multipleChoiceQuestion, key="max_choices", value="2")
    >>> c = Choice.objects.create(question=multipleChoiceQuestion, name="Rot")
    >>> c = Choice.objects.create(question=multipleChoiceQuestion, name="Gelb")
    >>> c = Choice.objects.create(question=multipleChoiceQuestion, name="Grün")
    >>> user = User.objects.create(ip="127.0.0.3")
    >>> a = Answer.objects.create(question=singleChoiceQuestion, user=user, answer="Rot")
    >>> a = Answer.objects.create(question=singleChoiceQuestion, user=user, answer="Rot")
    >>> a = Answer.objects.create(question=singleChoiceQuestion, user=user, answer="Rot")
    >>> a = Answer.objects.create(question=singleChoiceQuestion, user=user, answer="Gelb")
    >>> a = Answer.objects.create(question=singleChoiceQuestion, user=user, answer="Gelb")
    >>> a = Answer.objects.create(question=singleChoiceQuestion, user=user, answer="Grün")
    >>> singleChoiceQuestion.numAnswers()
    6
    >>> avgAnswers = singleChoiceQuestion.averageAnswers()
    >>> len(avgAnswers)
    3
    >>> avgAnswers[0].answer
    u'Rot'
    >>> avgAnswers[0].numVotes
    3
    >>> avgAnswers[0].average
    50
    >>> avgAnswers[0].question.id == singleChoiceQuestion.id
    True
    >>> avgAnswers[1].answer
    u'Gelb'
    >>> avgAnswers[1].numVotes
    2
    >>> avgAnswers[1].average
    33
    >>> avgAnswers[2].numVotes
    1
    >>> avgAnswers[2].average
    16
    
    """
    context = 'answeraverage'
    question = None
    answer = None
    numVotes = None
    average = None

    def __unicode__(self):
        return "AverageAnswer: %s (Votes: %d, Average: %d) on %s" % (self.answer, self.numVotes, self.average, unicode(self.question))
    
    def __repr__(self, *args, **kwargs):
        return self.__unicode__()
    
    def toJsonDict(self):
        return {'answer': self.answer, 'numVotes': self.numVotes, 'average': self.average}
