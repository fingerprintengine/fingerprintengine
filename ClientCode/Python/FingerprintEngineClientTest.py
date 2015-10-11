# python 3.3

import FingerprintEngineClient as efe

client = efe.FingerprintEngineClient('https://fingerprintengine.ptgels.com/TACO7600/TacoService.svc/', '#########', '********')

fp = client.index('MeSHXml', 'malaria', 'bad disease').toFingerprint()
for concept in fp:
    print('ConceptID: ' + concept.conceptid + ', rank: ' + concept.rank + ', name: ' + concept.name + ', AFreq: ' + concept.afreq)


fp = client.index('SingleFeatureAnalysisXml', 'malaria', 'bad disease').toFeatureFingerprint()
for concept in fp:
    print('ConceptID: ' + concept.conceptid + ', rank: ' + concept.rank + ', name: ' + concept.name + ', AFreq: ' + concept.afreq)

## print raw FP for XML indexing
# print(client.index('MeSHXml', 'malaria', 'bad disease'))

## print raw FP for plain text indexing
# print(client.plaintextindex('MeSH', 'malaria'))

