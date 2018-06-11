# python 3.3

import FingerprintEngineClient as efe

client = efe.FingerprintEngineClient('https://fingerprintengine.scivalcontent.com/taco7800/TacoService.svc/', 'your_username', 'your_password')

fp = client.index('MeSHXml', 'malaria', 'bad disease').toFingerprint()
for concept in fp:
    print('ConceptID: ' + concept.conceptid + ', rank: ' + concept.rank + ', name: ' + concept.name + ', AFreq: ' + concept.afreq)

fp = client.index('SingleFeatureAnalysisXml', 'malaria', 'bad disease').toFeatureFingerprint()
for concept in fp:
    print('ConceptID: ' + concept.conceptid + ', rank: ' + concept.rank + ', name: ' + concept.name + ', AFreq: ' + concept.afreq)

cr = client.classify('DomainClassifier', 'Clemmensen Reduction converts aldehydes and ketones to corresponding hydrocarbons.').toRanking()
for category in cr:
    print('Category: ' + category.name + ', Rank: ' + category.rank)


## print raw FP for XML indexing
# print(client.index('MeSHXml', 'malaria', 'bad disease'))

## print raw FP for plain text indexing
# print(client.plaintextindex('MeSH', 'malaria'))

