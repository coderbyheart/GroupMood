# -*- coding: utf8 -*-
from django.utils import unittest
from django.test.client import Client
from django.utils import simplejson

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
        self.assertEquals('http://groupmood.net/jsonld/meeting', info['result']['@context']) 
        self.assertEquals('Test-Meeting', info['result']['name'])
        self.assertEquals(0, info['result']['numTopics'])

