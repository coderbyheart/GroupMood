from django.db import models
from validators import validate_percent

class Meeting(models.Model):
    'Meetings sind das uebergeordnete Model'
    context = 'meeting'
    name = models.CharField(max_length=200)
    creation_date = models.DateTimeField('date created', auto_now_add=True)
    votesList = None
    
    def numVotes(self):
        return len(self.votes())
    
    def avgVote(self):
        if self.numVotes() <= 0:
            return 0
        return reduce(lambda x,y: x+y.vote, self.votes(), 0) / self.numVotes()
    
    def votes(self):
        if self.votesList == None:
            self.votesList = self.moodvote_set.all()
        return self.votesList
        
    def toJsonDict(self):
        return {'id': self.id, 'name': self.name, 'date': str(self.creation_date), 'avgVote': self.avgVote(), 'numVotes': self.numVotes()}
    
class MoodVote(models.Model):
    'Abstimmungen zur Stimmung eines Meetings'
    context = 'vote'
    meeting = models.ForeignKey(Meeting)
    vote = models.PositiveIntegerField(validators=[validate_percent])
    creation_date = models.DateTimeField('date created', auto_now_add=True)
    
    def toJsonDict(self):
        return {'id': self.id, 'vote': self.vote, 'date': str(self.creation_date)}
