"""Compare the three controlled experiment phases; no third-party Python packages."""
from pathlib import Path
from collections import Counter
import hashlib,json,re,sys,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
BASE=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else ROOT/'ift3913/evidence'
CLASS='org.apache.tika.io.LookaheadInputStream'
PHASES=['baseline-java17','ai','final']
def key(m):
 return (m.findtext('mutatedClass'),m.findtext('mutatedMethod'),m.findtext('methodDescription'),tuple(x.text for x in m.findall('indexes/index')),m.findtext('mutator'))
def short_test(value):
 if not value:return ''
 method=re.search(r'\[method:([^]]+)\]',value)
 return value.split('.')[ -1] if not method else value.split('.[engine:')[0].split('.')[-1]+'.'+method.group(1)
summary={'class':CLASS,'jdk':'17','mutators':['DEFAULTS'],'phases':{},'mutants':[]}
for phase in PHASES:
 p=BASE/phase
 jac=ET.parse(p/'jacoco.xml').getroot()
 c=next(c for c in jac.iter('class') if c.attrib['name']==CLASS.replace('.','/'))
 counters={x.attrib['type']:{'covered':int(x.attrib['covered']),'missed':int(x.attrib['missed'])} for x in c.findall('counter')}
 tree=ET.parse(p/'pit-report/mutations.xml').getroot()
 muts={key(m):m for m in tree.findall('mutation') if m.findtext('mutatedClass')==CLASS}
 counts=Counter(m.attrib['status'] for m in muts.values())
 log=next(p.glob('*tests.log'),None) or p/'verify.log'
 totals=re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)',log.read_text())
 # Last core summary, not the sum of per-class lines and summaries.
 testcounts=dict(zip(['tests','failures','errors','skipped'],map(int,totals[-1])))
 summary['phases'][phase]={'coverage':counters,'mutations':dict(counts),'totalMutations':len(muts),'strictScore':round(100*counts['KILLED']/len(muts),2),'tests':testcounts}
 if phase==PHASES[0]:
  keys=list(muts)
  for k in keys:
   m=muts[k]
   summary['mutants'].append({'id':len(summary['mutants'])+1,'key':k,'line':int(m.findtext('lineNumber')),'method':m.findtext('mutatedMethod'),'signature':m.findtext('methodDescription'),'mutator':m.findtext('mutator').split('.')[-1],'description':m.findtext('description'),'phases':{}})
 else:
  assert set(keys)==set(muts),'Different mutants: stop the comparison.'
 for entry in summary['mutants']:
  m=muts[keys[entry['id']-1]]
  entry['phases'][phase]={'status':m.attrib['status'],'killingTest':m.findtext('killingTest'),'test':short_test(m.findtext('killingTest'))}
summary['productionSha256']=hashlib.sha256((ROOT/'tika-core/src/main/java/org/apache/tika/io/LookaheadInputStream.java').read_bytes()).hexdigest()
(BASE/'summary.json').write_text(json.dumps(summary,indent=2)+'\n')
for phase,data in summary['phases'].items():print(phase,json.dumps(data))
assert summary['phases']['final']['mutations'].get('KILLED')==summary['phases']['final']['totalMutations']
assert summary['phases']['final']['tests']['failures']==summary['phases']['final']['tests']['errors']==0
