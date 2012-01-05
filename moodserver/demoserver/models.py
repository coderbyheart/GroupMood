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
    Meetings sind das übergeordnete Model
    
    # Create a Meeting
    >>> meeting = Meeting.objects.create(name="Test")
    >>> meeting.name
    'Test'
    >>> meeting.numVotes()
    0
    >>> meeting.avgVote()
    50
    >>> meeting.numSlides()
    0
    
    # Add some votes
    >>> Vote.objects.create(meeting=meeting, vote=60, ip='127.0.0.1')
    >>> Vote.objects.create(meeting=meeting, vote=70, ip='127.0.0.2')
    >>> meeting.numVotes()
    2
    >>> meeting.avgVote()
    65
    
    """
    context = 'meeting'
    name = models.CharField(max_length=200)
    
    def __unicode__(self):
        return "Meeting #%d: " % (self.id, self.name)
      
    def numVotes(self):
        return len(self.votes())
    
    def avgVote(self):
        if self.numVotes() <= 0:
            return 50
        return reduce(lambda x, y: x + y.vote, self.votes(), 0) / self.numVotes()
    
    def votes(self):
        return Vote.objects.filter(meeting=self.id)
    
    def slides(self):
        return Slide.objects.filter(meeting=self.id)
    
    def numSlides(self):
        return len(self.slides())
        
    def toJsonDict(self):
        return {'id': self.id, 'name': self.name, 'date': str(self.creation_date), 'avgVote': self.avgVote(), 'numVotes': self.numVotes()}
    
class Vote(BaseModel):
    """
    Abstimmungen zur Stimmung eines Meetings
    
    # Create a Meeting
    >>> meeting = Meeting.objects.create(name="Meeting with Votes")
    
    # Add some votes
    >>> Vote.objects.create(meeting=meeting, vote=60, ip='127.0.0.1')
    >>> Vote.objects.create(meeting=meeting, vote=70, ip='127.0.0.2')
    >>> meeting.numVotes()
    2
    >>> meeting.avgVote()
    65
    
    """
    context = 'vote'
    meeting = models.ForeignKey(Meeting)
    vote = models.PositiveIntegerField(validators=[validate_percent])
    ip = models.IPAddressField()
    
    def __unicode__(self):
        return "Vote #%d: %d%% on %s" % (self.id, self.vote, unicode(self.meeting))
    
    def toJsonDict(self):
        return {'id': self.id, 'vote': self.vote, 'date': str(self.creation_date)}

class Slide(BaseModel):
    """
    Folie einer Präsentation. Präsentation sind ebenfalls Meetings
    
    # Create a Meeting
    >>> meeting = Meeting.objects.create(name="Meeting with Slides")
    
    # Add some slides
    >>> Slide.objects.create(meeting=meeting, number=1)
    >>> slide = Slide.objects.create(meeting=meeting, number=2)
    >>> meeting.numSlides()
    2
    
    >>> slide.numVotes()
    0
    
    >>> slide.avgVote()
    50
    
    """
    context = 'slide'
    meeting = models.ForeignKey(Meeting)
    number = models.PositiveIntegerField(validators=[validate_nonzeropositive])
    
    class Meta:
        unique_together = (("meeting", "number"),)
    
    def __unicode__(self):
        return "Slide #%d: #%d% of %s" % (self.id, self.number, unicode(self.meeting))
    
    def numVotes(self):
        return len(self.votes())
    
    def avgVote(self):
        if self.numVotes() <= 0:
            return 50
        return reduce(lambda x, y: x + y.vote, self.votes(), 0) / self.numVotes()
    
    def votes(self):
        return SlideVote.objects.filter(slide=self.id)
    
    
class SlideVote(BaseModel):
    """
    
    Bewertung einer einzelnen Folie
    
    # Create a Meeting
    >>> meeting = Meeting.objects.create(name="Meeting with Slides")
    
    # Add some slides
    >>> Slide.objects.create(meeting=meeting, number=1)
    >>> slide = Slide.objects.create(meeting=meeting, number=2)
    
    # Add some votes
    >>> Slide.objects.create(slide=slide, number=60)
    >>> Slide.objects.create(slide=slide, number=70)
    >>> slide.numVotes()
    2
    
    
    """
    context = 'slidevote'
    slide = models.ForeignKey(Slide)
    vote = models.PositiveIntegerField(validators=[validate_percent])
    ip = models.IPAddressField()
    
    def __unicode__(self):
        return "SlideVote #%d: #%d% on %s" % (self.id, self.vote, unicode(self.slide))

class SlideComment(BaseModel):
    'Kommentar zu einer Folie'
    context = 'slidecomment'
    slide = models.ForeignKey(Slide)
    comment = models.CharField(max_length=200)
    ip = models.IPAddressField()
    
    def __unicode__(self):
        return "SlideComment #%d: %s on %s" % (self.id, self.comment, unicode(self.slide))

class Question(BaseModel):
    'Eine Frage'
    context = 'question'
    question = models.CharField(max_length=200)

    class Meta:
        abstract = True

class ValueQuestion(Question):
    'Eine Frage nach einem Wertebereich'
    context = 'valuequestion'
    min_value = models.IntegerField()
    max_value = models.IntegerField()
    
    def clean(self):
        'Validiert das Model als Ganzes'
        from django.core.exceptions import ValidationError
        if self.min_value > self.max_value:
            raise ValidationError('min_value must be smaller than max_value.')
        
    def __unicode__(self):
        return "ValueQuestion #%d: %s between %d and %d on %s" % (self.id, self.question, self.min_value, self.max_value, unicode(self.question))

class ChoiceQuestion(Question):
    'Eine Frage mit mehreren Antwort-Optionen'
    context = 'choicequestion'
    TYPE_SINGLE_CHOICE = 1
    TYPE_MULTIPLE_CHOICE = 2
    CHOICE_TYPES = (
        (TYPE_SINGLE_CHOICE, 'Single-Choice'),
        (TYPE_MULTIPLE_CHOICE, 'Multiple-Choice')
    )
    type = models.IntegerField(choices=CHOICE_TYPES, default=TYPE_SINGLE_CHOICE)
    
    def __unicode__(self):
        return "ChoiceQuestion #%d: on %s" % (self.id, unicode(self.question))

class Choice(BaseModel):
    'Eine Antwort-Option'
    context = 'choice'
    question = models.ForeignKey(ChoiceQuestion)
    choice = models.CharField(max_length=200)
    
    def __unicode__(self):
        return "Choice #%d: %s on %s" % (self.id, self.choice, unicode(self.question))

class QuestionSlide(Slide):
    'Eine Folie, die eine Frage enthält'
    context = 'questionslide'
    question = models.CharField(max_length=200)
    
    def __unicode__(self):
        return "QuestionSlide #%d: #%d% of %s: %s" % (self.id, self.number, unicode(self.meeting), self.question)

class Answer(BaseModel):
    'Antwort zu einer Frage'
    context = 'answer'
    slide = models.ForeignKey(QuestionSlide)
    answer = models.CharField(max_length=200)
    ip = models.IPAddressField()

    def __unicode__(self):
        return "Answer #%d: %s on %s" % (self.id, self.answer, unicode(self.slide))
