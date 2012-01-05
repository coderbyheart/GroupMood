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
        response = self.client.post('/demoserver/meeting', {'name': 'Test-Meeting'}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        self.assertTrue('Location' in response)
        info = simplejson.loads(response.content)
        self.assertEquals(1, info['status']['code'])
        self.assertEquals('ok', info['status']['message'])
        self.assertEquals('http://groupmood.net/jsonld/meeting', info['result']['@context'][0]) 
        self.assertEquals('Test-Meeting', info['result']['name'])
        self.assertEquals(1, info['result']['numTopics'])
        
    def test_default_vote(self):
        response = self.client.post('/demoserver/meeting', {'name': 'Test-Meeting'}, Accept='application/json')
        info = simplejson.loads(response.content)
        question_url = info['result']['topics'][0]['questions'][0]['@id']
        
        response = self.client.get(question_url, Accept='application/json')
        info = simplejson.loads(response.content)
        self.assertEquals(50, info['result']['avg'])
        self.assertEquals(0, info['result']['numAnswers'])
        
        response = self.client.post(question_url + '/answer', {'answer': 60}, Accept='application/json')
        response = self.client.post(question_url + '/answer', {'answer': 70}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        
        response = self.client.get(question_url, Accept='application/json')
        info = simplejson.loads(response.content)
        self.assertEquals(65, info['result']['avg'])
        self.assertEquals(2, info['result']['numAnswers'])
        
