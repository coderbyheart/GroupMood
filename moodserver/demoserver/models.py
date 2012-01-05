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
    
    """
    context = 'meeting'
    name = models.CharField(max_length=200)
    
    def __unicode__(self):
        return "Meeting #%d: %s" % (self.id, self.name)
      
    def topics(self):
        return Topic.objects.filter(meeting=self.id)
    
    def numTopics(self):
        return len(self.topics())
        
    def toJsonDict(self):
        return {'id': self.id, 'name': self.name, 'date': str(self.creation_date), 'numTopics': self.numTopics()}
    
class User(BaseModel):
    """
    Definiert einen Nutzer, identifiziert anhand seiner IP-Adresses
    
    Aktuell wird keine Nutzer-Registrierung verwendet.
    
    """
    ip = models.IPAddressField(unique=True)
    
class Topic(BaseModel):
    """
    Thema in einem Meeting. Damit können Nutzer interagieren.
    
    Ein Topic kann z.B. die Bewertung des Meetings selber sein.
    
    # Create a Meeting
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    
    # Create a slide topic
    >>> slideTopic = Topic.objects.create(meeting=meeting, identifier="slide-1", name="Folie 1", order=1)
    >>> slideTopic.identifier
    'slide-1'
    >>> slideTopic.name
    'Folie 1'
    >>> slideTopic.order
    1
    >>> slideTopic.image == None
    True

    # Create the vote topic
    >>> voteTopic = Topic.objects.create(meeting=meeting, identifier="vote", name="Wie bewerten Sie dieses Meeting?", order=2)
    
    >>> meeting.numTopics()
    2
    
    >>> voteTopic.numQuestions()
    0
    
    >>> voteTopic.numComments()
    0
    
    """
    context = 'topic'
    meeting = models.ForeignKey(Meeting)
    identifier = models.SlugField()
    name = models.CharField(max_length=200)
    order = models.PositiveIntegerField(validators=[validate_nonzeropositive])
    image = models.URLField(null=True)
    
    class Meta:
        unique_together = (("meeting", "order"),("meeting", "identifier"),("meeting", "name"))
    
    def __unicode__(self):
        return "Topic #%d: %s/#%d%: %s of %s" % (self.id, self.identifier, self.order, self.name, unicode(self.meeting))
    
    def numQuestions(self):
        return len(self.questions())
    
    def questions(self):
        return Question.objects.filter(topic=self.id)
    
    def numComments(self):
        return len(self.comments())
    
    def comments(self):
        return Comment.objects.filter(topic=self.id)
    
class Comment(BaseModel):
    """
    Kommentar zu einem Topic
    
    Neben Antworten, die mathematisch ausgewertet werden können, können auch einfache Kommentare zu Themen hinterlassen werden.
    
    >>> meeting = Meeting.objects.create(name="Meeting with Comments")
    >>> voteTopic = Topic.objects.create(meeting=meeting, identifier="vote", name="Wie bewerten Sie dieses Meeting?", order=1)
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
    context = 'topiccomment'
    topic = models.ForeignKey(Topic)
    user = models.ForeignKey(User)
    comment = models.CharField(max_length=200)
    
    def __unicode__(self):
        return "Comment #%d: %s on %s" % (self.id, self.comment, unicode(self.slide))

class Question(BaseModel):
    """
    Eine Frage zu einem Topic
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> voteTopic = Topic.objects.create(meeting=meeting, identifier="vote", name="Wie bewerten Sie dieses Meeting?", order=2)
    >>> question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
    >>> question.name
    'Allgemeine Bewertung'
    >>> question.type == Question.TYPE_RANGE
    True
    >>> question.mode == Question.MODE_AVERAGE
    True
    
    """
    context = 'question'
    topic = models.ForeignKey(Topic)
    name = models.CharField(max_length=200)
    TYPE_SINGLE_CHOICE = 1
    TYPE_MULTIPLE_CHOICE = 2
    TYPE_RANGE = 3
    QUESTION_TYPES = (
        (TYPE_SINGLE_CHOICE, 'Single-Choice'),
        (TYPE_MULTIPLE_CHOICE, 'Multiple-Choice'),
        (TYPE_MULTIPLE_CHOICE, 'Range')
    )
    type = models.IntegerField(choices=QUESTION_TYPES, default=TYPE_SINGLE_CHOICE)
    MODE_SINGLE = 1
    MODE_AVERAGE = 2
    QUESTION_MODES = (
        (MODE_SINGLE, 'Single-Vote'),
        (MODE_AVERAGE, 'Average-Vote')
    )
    mode = models.IntegerField(choices=QUESTION_MODES, default=MODE_AVERAGE)

class QuestionOption(BaseModel):
    """
    Definiert die Details einer Frage, in Form von Key/Value-Paare
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> voteTopic = Topic.objects.create(meeting=meeting, identifier="vote", name="Wie bewerten Sie dieses Meeting?", order=2)
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

class Choice(BaseModel):
    """
    Definiert die Antwort-Option bei Single- und Multiple-Choice-Fragen
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> topic = Topic.objects.create(meeting=meeting, identifier="question-1", name="Frage 1", order=1)
    >>> question = Question.objects.create(topic=topic, name="Was ist blau?", type=Question.TYPE_SINGLE_CHOICE, mode=Question.MODE_SINGLE)
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

class Answer(BaseModel):
    """
    
    Definiert die Antwort zu einer Frage
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> voteTopic = Topic.objects.create(meeting=meeting, identifier="vote", name="Wie bewerten Sie dieses Meeting?", order=2)
    >>> question = Question.objects.create(topic=voteTopic, name="Allgemeine Bewertung", type=Question.TYPE_RANGE, mode=Question.MODE_AVERAGE)
    >>> questionOptionMin = QuestionOption.objects.create(question=question, key="min_value", value="0")
    >>> questionOptionMax = QuestionOption.objects.create(question=question, key="max_value", value="100")
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
        return "Answer #%d: %s on %s" % (self.id, self.answer, unicode(self.slide))
