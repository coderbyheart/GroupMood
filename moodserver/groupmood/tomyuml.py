# -*- coding: utf8 -*-
import re

currentModel = None

models = []
classtomodel = {}

class Model(object):
    name = None
    extends = None
    props = None
    refs = None
    
for line in open("models.py").readlines():
    line = line.strip()
    
    classmatch = re.compile(r"^class ([A-Z][A-Za-z]+)\(([A-Za-z\.]+)\)")
    classres = classmatch.match(line)
    if classres != None:
        currentModel = Model()
        currentModel.name = classres.group(1)
        currentModel.extends = classres.group(2) if classres.group(2) not in ('models.Model') else "object"
        currentModel.props = []
        currentModel.refs = [] 
        models.append(currentModel)
        classtomodel[currentModel.name] = currentModel
    
    propmatch = re.compile(r"^([a-z_]+) *= model")
    propres = propmatch.match(line)
    if propres != None:
        currentModel.props.append(propres.group(1))
        
    refmatch = re.compile(r".*models\.ForeignKey\(([A-Za-z]+)\)$")
    refres = refmatch.match(line)
    if refres != None:
        currentModel.refs.append(refres.group(1))

print "digraph G {"
print "    graph [ rankdir=BT ]"
print "    node [ shape=record ]"

for model in models:
    if model.name == "BaseModel":
        continue
    props = model.props
    if model.extends != 'object':
        for prop in classtomodel[model.extends].props:
            props.append(prop)
        
    print '    %s [label="{%s%s}"]' % (model.name, model.name, "" if len(model.props) == 0 else "|" + "".join(map(lambda x: "+%s\\l" % (x), props)))   

print '    edge [ arrowhead=none headlabel="1" taillabel="0..n" fontsize=10 ]'
for model in models:
    if len(model.refs) == 0:
        continue
    for ref in model.refs:
        print "    %s -> %s " % (model.name, ref)  

print "}"