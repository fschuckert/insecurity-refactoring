import os

path = os.path.expanduser("~/crawl_data/2020-09-07")
import re

S = ';'

tit = "Name" +S+    "LOC"+S+    "Time"+S+       "PIPs" +S+      "Paths" +S+     "Paths vuln" +S+    "Paths timeout" +S+     "XSS" +S+           "SQLi"+S+           "EVAL"  +S+ "SPECIFICS"

reports = []


for fil in os.listdir(path):
    try:
        types = {'xss':'0', 'eval':'0', 'sqli':'0'}
        specifics = []
        sinks = {}
        if not fil.endswith(".result"):
           continue 

        filePath = os.path.join(path, fil)
        f = open(filePath, "r")

        for line in f:
            line = line.strip()
            if line.startswith("Vulnerable: "):
                vulns = re.search('(?<=Vulnerable: )\w+', line).group(0)
            if line.startswith("FOUND PIPs: "):
                pips = re.search('(?<=FOUND PIPs: )\w+', line).group(0)
            
            if line.startswith("Path Amount: "):
                pathAmount = re.search('(?<=Path Amount: )\w+', line).group(0)
            if line.startswith("Path Vulns: "):
                pathVulns = re.search('(?<=Path Vulns: )\w+', line).group(0)
            if line.startswith("Path Timeouts: "):
                pathTimeouts = re.search('(?<=Path Timeouts: )\w+', line).group(0)

            if line.startswith("PIP TYPE "):
                pipType = re.search('(?<=TYPE).+', line).group(0).strip()
                typ, num = pipType.split(":")
                types[typ]=num
                
            if line.startswith("PIP SPECIFIC "):
                specific = re.search('(?<=SPECIFIC).+', line).group(0).strip()
                sink, num = specific.rsplit(":", 1)
                sinks[sink]=num

            if line.startswith("PHP "):
                loc = re.search('(?<=PHP).+', line).group(0).strip()
                loc = loc.split(" ")[-1]
            
            if line.startswith("Scan took:"):
                scanTime = re.search('(?<=Scan took: ).+', line).group(0)

            if line.startswith("PIP SPECIFIC"):
                specific = re.search('(?<=PIP SPECIFIC).+', line).group(0)
                specifics.append(specific)

            if line.startswith("Sink("):
                name, num = line.rsplit("):", 1)
                sinks[name] = num

        csv = fil +S+       loc+S+      scanTime+S+     pips +S+        pathAmount +S+  pathVulns +S+       pathTimeouts +S+        types['xss'] +S+    types['sqli']+S+    types['eval'] +S
        
        for specific in specifics:
            csv = csv + specific + ","
        csv = csv +S

        # for sink, num in sinks.items():
        #     tit = tit +S+ sink
        #     csv = csv +S+ num

        reports.append(csv)
    except e:
        print(e)
        continue

print(tit)
for report in reports:
    print(report)
