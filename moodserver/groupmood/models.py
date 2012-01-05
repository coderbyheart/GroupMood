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
        return {'id': self.id, 'name': self.name, 'numTopics': self.numTopics()}
    
class User(BaseModel):
    """
    Definiert einen Nutzer, identifiziert anhand seiner IP-Adresses
    
    Aktuell wird keine Nutzer-Registrierung verwendet.
    
    """
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
    image = models.URLField(null=True)
    
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
        return {'id': self.id, 'name': self.name, 'image': self.image}
    
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
    
    """
    context = 'question'
    topic = models.ForeignKey(Topic)
    name = models.CharField(max_length=200)
    TYPE_SINGLECHOICE = 'singlechoice'
    TYPE_MULTIPLECHOICE = 'multiplechoice'
    TYPE_RANGE = 'range'
    TYPES = (
        (TYPE_SINGLECHOICE, 'Single-Choice'),
        (TYPE_MULTIPLECHOICE, 'Multiple-Choice'),
        (TYPE_RANGE, 'Range')
    )
    type = models.CharField(max_length=20, choices=TYPES, default=TYPE_SINGLECHOICE)
    MODE_SINGLE = 'single'
    MODE_AVERAGE = 'avg'
    MODES = (
        (MODE_SINGLE, 'Single-Vote'),
        (MODE_AVERAGE, 'Average-Vote')
    )
    mode = models.CharField(max_length=20, choices=MODES, default=MODE_AVERAGE)
    
    def answers(self):
        return Answer.objects.filter(question=self.id)
    
    def numAnswers(self):
        return len(self.answers());
    
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
    
    def getMin(self, default=None):
        if self.mode != self.MODE_AVERAGE:
            return default
        qmin_value = QuestionOption.objects.filter(question=self, key="min_value")
        return qmin_value[0].value if qmin_value else default
        
    def getMax(self, default=None):
        if self.mode != self.MODE_AVERAGE:
            return default
        qmax_value = QuestionOption.objects.filter(question=self, key="max_value")
        return qmax_value[0].value if qmax_value else default
    
    def toJsonDict(self):
        type = filter(lambda t: t[0] == self.type, self.TYPES)[0][0]
        mode = filter(lambda m: m[0] == self.mode, self.MODES)[0][0]
        d = {'id': self.id, 'name': self.name, 'type': type, 'mode': mode, 'avg': self.avg(), 'numAnswers': self.numAnswers()}
        return d

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
        return {'id': self.id, 'key': self.key, 'value': self.value}

class Choice(BaseModel):
    """
    Definiert die Antwort-Option bei Single- und Multiple-Choice-Fragen
    
    >>> meeting = Meeting.objects.create(name="Meeting with Topics")
    >>> topic = Topic.objects.create(meeting=meeting, name="Frage 1")
    >>> question = Question.objects.create(topic=topic, name="Was ist blau?", type=Question.TYPE_SINGLECHOICE, mode=Question.MODE_SINGLE)
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
    >>> voteTopic = Topic.objects.create(meeting=meeting, name="Wie bewerten Sie dieses Meeting?")
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
    
    def toJsonDict(self):
        return {'id': self.id, 'answer': self.answer}
