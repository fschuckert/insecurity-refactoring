from github import Github
from runpath import call_on_path
import shutil
import os.path
import time
from time import gmtime, strftime

character = "hprose/hprose-php"
character2 = "phpbb_phpbb"

g = Github("277ffeb5a718ed5aa481cf7bd1a82665d0da4947")

work_dir='/home/blubbomat/pip_scans/'
repoDir="{}source".format(work_dir)

# repos = g.search_repositories(query='language:php', sort='stars')
repos = g.search_repositories(query=character)

# Then play with your Github objects:
i=0
maxRepos=10000000
for repo in repos:
    uniqueName = repo.full_name
    cloneUrl = "https://github.com/{}.git".format(uniqueName)
    cloneCmd = "git clone {url} {target}".format(url=cloneUrl, target=repoDir)
    outputFile = "{}/{}.result".format(work_dir, uniqueName.replace('/', "_"))
    scan = "java -jar target/InsecurityRefactoring*.jar -o -p {} >> {}".format(repoDir, outputFile)
    loc = "cloc --quiet {} >> {}".format(repoDir, outputFile)

    if repo.full_name.lower().startswith(character.lower()) or repo.full_name.lower().startswith(character2.lower()):
        print(repo.full_name + " " + str(repo.stargazers_count))
    else:
        print("SKIP " + uniqueName)
        continue

    if os.path.isfile(outputFile):
        # Already scanned
        print("Skipping {}".format(uniqueName))
        continue
    
    # Remove dir
    o, e = call_on_path(work_dir, "rm -rf {}".format(repoDir))
    # Clone repo
    o, e = call_on_path(work_dir, cloneCmd)
    # Count lines of code
    o, e = call_on_path(work_dir, loc)
    # Scan
    startTime = time.time()
    print("Starting time: {}".format( strftime("%Y-%m-%d %H:%M:%S", gmtime())   ))
    o, e = call_on_path('.', scan)
    endTime = time.time()
   
    elapsedTime = endTime - startTime
    niceTime = strftime("%H:%M:%S", time.gmtime(elapsedTime))
    print("It took: {}".format(niceTime))
    o, e = call_on_path('target', "echo 'Scan took: {}' >> {}".format(niceTime, outputFile))


    i=i+1
    if i >= maxRepos:
        break
