::batch script for calling multiple processes
::start cmd /K call test_batch/harm.bat 0
::start cmd /K call test_batch/harm.bat 1
::start cmd /K call test_batch/harm.bat 2
::start cmd /K call test_batch/harm.bat 3

start cmd /K call test_batch/stalker.bat
timeout /t 2
start cmd /K call test_batch/jcp.bat



timeout /t 10

