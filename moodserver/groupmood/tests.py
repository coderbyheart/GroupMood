# -*- coding: utf8 -*-
"""
    Testet die API.
    
    @author: Markus Tacker <m@coderbyheart.de>
"""
from django.utils import unittest
from django.test.client import Client
from django.utils import simplejson
import pprint

class Base(unittest.TestCase):
    def setUp(self):
        self.client = Client()

def get_related(context, result):
    contextUri = 'http://groupmood.net/jsonld/' + context
    return filter(lambda relation: context in relation['relatedcontext'], result['@relations'])[0]
        
class MeetingTest(Base):
    
    def test_create(self):
        """Test für den Meeting-Wizard"""
        response = self.client.post('/groupmood/meeting/wizard/test1', Accept='application/json')
        self.assertEqual(response.status_code, 201)
        self.assertTrue('Location' in response)
        info = simplejson.loads(response.content)
        self.assertEquals(1, info['status']['code'])
        self.assertEquals('ok', info['status']['message'])
        self.assertEquals('http://groupmood.net/jsonld/meeting', info['result']['@context']) 
        self.assertEquals('Test-Meeting', info['result']['name'])
        self.assertEquals(1, info['result']['numTopics'])
        
    def test_default_vote(self):
        """Test für das Abgeben von Antworten"""
        response = self.client.post('/groupmood/meeting/wizard/test1', Accept='application/json')
        info = simplejson.loads(response.content)
        meeting = info['result']
        
        response = self.client.get(get_related('topic', meeting)['href'], Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        topics = info['result']
        self.assertEquals('http://groupmood.net/jsonld/topic', topics[0]['@context'])
        
        response = self.client.get(get_related('question', topics[0])['href'], Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        questions = info['result']
        self.assertEquals('http://groupmood.net/jsonld/question', questions[0]['@context'])
        
        self.assertEquals(50, questions[0]['avg'])
        self.assertEquals(0, questions[0]['numAnswers'])
        
        response = self.client.post(get_related('answer', questions[0])['href'], {'answer': 60}, Accept='application/json')
        response = self.client.post(get_related('answer', questions[0])['href'], {'answer': 70}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        
        response = self.client.get(questions[0]['@id'], Accept='application/json')
        info = simplejson.loads(response.content)
        self.assertEquals(65, info['result']['avg'])
        self.assertEquals(2, info['result']['numAnswers'])
        
    def test_topic_comment(self):
        """Test für das Abgeben von Kommentaren"""
        response = self.client.post('/groupmood/meeting/wizard/test1', Accept='application/json')
        info = simplejson.loads(response.content)
        meeting = info['result']
        
        response = self.client.get(get_related('topic', meeting)['href'], Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        topics = info['result']
        self.assertEquals('http://groupmood.net/jsonld/topic', topics[0]['@context'])
        
        response = self.client.post(get_related('comment', topics[0])['href'], {'comment': "Test-Comment"}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        response = self.client.post(get_related('comment', topics[0])['href'], {'comment': "Test-Comment 2"}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        
        response = self.client.get(get_related('comment', topics[0])['href'], Accept='application/json')
        info = simplejson.loads(response.content)
        
        self.assertEquals(2, len(info['result']))
        # Neueste Kommentare zuerst
        self.assertEquals("Test-Comment 2", info['result'][0]['comment'])
        self.assertEquals("Test-Comment", info['result'][1]['comment'])

    def test_choice_vote(self):
        """Test für Antworten und Ergebnisse von Choice-Votes""" 
        
        response = self.client.post('/groupmood/meeting/wizard/test2', Accept='application/json')
        info = simplejson.loads(response.content)
        meeting = info['result']
        
        response = self.client.get(get_related('topic', meeting)['href'], Accept='application/json')
        info = simplejson.loads(response.content)
        topic = info['result'][0]
        
        response = self.client.get(get_related('question', topic)['href'], Accept='application/json')
        info = simplejson.loads(response.content)
        question = info['result'][0]
        
        response = self.client.get(get_related('choice', question)['href'], Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        choices = info['result']
        self.assertEquals(3, len(choices))
        self.assertEquals('http://groupmood.net/jsonld/choice', choices[0]['@context'])
        
        answerRelationHref = get_related('answer', question)['href']
        
        votes = [
         'Rot',
         'Rot',
         'Rot',
         'Gelb',
         'Gelb',
         'Grün',
        ]
        
        for a in votes:
            response = self.client.post(answerRelationHref, {'answer': a}, Accept='application/json')
            self.assertEqual(response.status_code, 201)
            info = simplejson.loads(response.content)
            answer = info['result']
            self.assertEquals('http://groupmood.net/jsonld/answer', answer['@context'])
            
        # Question neu laden
        response = self.client.get(question['@id'], Accept='application/json')
        info = simplejson.loads(response.content)
        question = info['result']
        self.assertEquals(6, question['numAnswers'])
        averageAnswers = question['averageAnswers']
        self.assertEquals(3, len(averageAnswers))
        self.assertEquals('Rot', averageAnswers[0]['answer'])
        self.assertEquals(3, averageAnswers[0]['numVotes'])
        self.assertEquals(50, averageAnswers[0]['average'])
        self.assertEquals('Gelb', averageAnswers[1]['answer'])
        self.assertEquals(2, averageAnswers[1]['numVotes'])
        self.assertEquals(33, averageAnswers[1]['average'])
        self.assertEquals(1, averageAnswers[2]['numVotes'])
        self.assertEquals(16, averageAnswers[2]['average'])

    def test_recursive(self):
        """Test für das Serverseite zusammenbauen von Datenstrukturen aus API-Antworten."""
        response = self.client.get('/groupmood/recursive/question/3', Accept='application/json')
        info = simplejson.loads(response.content)
        question = info['result']
        
        self.assertEquals(2, len(get_related('questionoption', question)['data']))
        self.assertEquals('http://groupmood.net/jsonld/questionoption', get_related('questionoption', question)['data'][0]['@context'])
        self.assertEquals(3, len(get_related('choice', question)['data']))
        self.assertEquals('http://groupmood.net/jsonld/choice', get_related('choice', question)['data'][0]['@context'])
        
