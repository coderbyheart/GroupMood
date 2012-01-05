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
        currentModel.extends = classres.group(2) if classres.group(2) != 'models.Model' else "object"
        currentModel.props = []
        currentModel.refs = [] 
        models.append(currentModel)
        classtomodel[currentModel.name] = currentModel
    
    propmatch = re.compile(r"^([a-z]+) *= model")
    propres = propmatch.match(line)
    if propres != None:
        currentModel.props.append(propres.group(1))
        
    refmatch = re.compile(r".*models\.ForeignKey\(([A-Za-z]+)\)$")
    refres = refmatch.match(line)
    if refres != None:
        currentModel.refs.append(refres.group(1))

def renderModel(model):
    modeldef = model.name
    if len(model.props) > 0:
        modeldef = "%s;--------------------;%s" % (modeldef, ";".join(map(lambda x: "+" + x, model.props)))
    return modeldef

for model in models:
    extendsdef = "" if model.extends == "object" else "^-[%s]" % renderModel(classtomodel[model.extends]) 
    print "[%s]%s" % (renderModel(model), extendsdef)

for model in models:
    if len(model.refs) == 0:
        continue
    for ref in model.refs:
        pass
        #print "[%s]1-0..n[%s]" % (renderModel(model), renderModel(classtomodel[ref]) )  
