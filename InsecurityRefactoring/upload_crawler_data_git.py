import os
from runpath import call_on_path

# config
path = os.path.expanduser("~/pip_scans")
workDir = os.path.expanduser("~/crawl_data")
subDir = "2020-09-07"


#fixed commands
rmOldDir = "rm -rf {workDir}".format(workDir=workDir)
clone = "git clone gogs@blubbomat.de:blubbomat/Crawler_Data.git {workDir}".format(workDir=workDir)
mkSubDir = "mkdir {sub}".format(sub=subDir)
copy = "cp {path}/*.result {wD}/{sD}".format(path=path, wD=workDir, sD=subDir)
submit = "git add . ; git commit -m \"added automatically\"; git push"

# running
call_on_path(".", rmOldDir)
call_on_path(".", clone)
call_on_path(workDir, mkSubDir)
call_on_path(workDir, copy)

call_on_path(".", "git config --global user.email \"you@example.com\"")
call_on_path(".", "git config --global user.name \"Your Name\"")
call_on_path(workDir, submit)


