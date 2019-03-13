::batch script for calling multiple processes
:: args for harms will determine port number

start cmd /K call test_batch/harm.bat 0
timeout /t 5
start cmd /K call test_batch/harm.bat 1
timeout /t 5
start cmd /K call test_batch/harm.bat 2
timeout /t 5
start cmd /K call test_batch/harm.bat 3
timeout /t 5

start cmd /K call test_batch/stalker.bat
timeout /t 4
start cmd /K call test_batch/jcp.bat



timeout /t 10

