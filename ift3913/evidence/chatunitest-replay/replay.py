from pathlib import Path
import json,re,subprocess,hashlib
root=Path('/Users/anasys/Desktop/tika')
tmp=Path('/private/tmp/ift3913-recovery')
cp=str(root/'tika-core/target/classes')+':'+Path('/private/tmp/ift3913-recovery-classpath.txt').read_text().strip()
jdk='/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home/bin/'
subprocess.run([jdk+'javac','-cp',cp,'-d',str(tmp),str(tmp/'ReplayTests.java')],check=True)
history=root/'ift3913/generated/logs/tika-parent/tika-core/history2026_10_06_14_44_17/class139'
results=[]
for p in sorted(history.glob('method*/attempt0/records.json')):
 record=json.loads(p.read_text())[-1]
 code=record['code']; name=re.search(r'public class (\w+)',code).group(1)
 work=tmp/name;work.mkdir(exist_ok=True)
 java=work/(name+'.java');java.write_text(code)
 compile=subprocess.run([jdk+'javac','-cp',cp,'-d',str(work),str(java)],capture_output=True,text=True,timeout=30)
 (work/'compile.log').write_text(compile.stdout+compile.stderr)
 item={'class':name,'sourceRecord':str(p.relative_to(root)),'round':record['round'],'codeSha256':hashlib.sha256(code.encode()).hexdigest(),'compileSuccess':compile.returncode==0}
 if compile.returncode==0:
  run=subprocess.run([jdk+'java','-cp',str(work)+':'+str(tmp)+':'+cp,'ReplayTests','org.apache.tika.io.'+name],capture_output=True,text=True,timeout=30)
  (work/'test.log').write_text(run.stdout+run.stderr)
  item['executionSuccess']=run.returncode==0
  item['testSummary']=run.stdout
 else:
  item['compilerOutput']=compile.stderr
 results.append(item)
 print(name,'compile=',item['compileSuccess'],'pass=',item.get('executionSuccess'))
(tmp/'results.json').write_text(json.dumps(results,indent=2,ensure_ascii=False)+'\n')
