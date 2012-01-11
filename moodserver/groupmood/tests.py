# -*- coding: utf8 -*-
from django.utils import unittest
from django.test.client import Client
from django.utils import simplejson
import pprint

class Base(unittest.TestCase):
    def setUp(self):
        self.client = Client()
        
class MeetingTest(Base):
    
    def test_create(self):
        response = self.client.post('/groupmood/meeting', {'name': 'Test-Meeting'}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        self.assertTrue('Location' in response)
        info = simplejson.loads(response.content)
        self.assertEquals(1, info['status']['code'])
        self.assertEquals('ok', info['status']['message'])
        self.assertEquals('http://groupmood.net/jsonld/meeting', info['result']['@context']) 
        self.assertEquals('Test-Meeting', info['result']['name'])
        self.assertEquals(1, info['result']['numTopics'])
        
    def test_default_vote(self):
        response = self.client.post('/groupmood/meeting', {'name': 'Test-Meeting'}, Accept='application/json')
        info = simplejson.loads(response.content)
        meeting = info['result']
        
        response = self.client.get(meeting['@id'] + '/topics', Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        topics = info['result']
        self.assertEquals('http://groupmood.net/jsonld/topic', topics[0]['@context'])
        
        response = self.client.get(topics[0]['@id'] + '/questions', Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        questions = info['result']
        self.assertEquals('http://groupmood.net/jsonld/question', questions[0]['@context'])
        
        self.assertEquals(50, questions[0]['avg'])
        self.assertEquals(0, questions[0]['numAnswers'])
        
        response = self.client.post(questions[0]['@id'] + '/answer', {'answer': 60}, Accept='application/json')
        response = self.client.post(questions[0]['@id'] + '/answer', {'answer': 70}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        
        response = self.client.get(questions[0]['@id'], Accept='application/json')
        info = simplejson.loads(response.content)
        self.assertEquals(65, info['result']['avg'])
        self.assertEquals(2, info['result']['numAnswers'])
        
