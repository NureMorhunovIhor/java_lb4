@echo off

start cmd /k "java -cp target/classes org.example.Server"
start cmd /k "java -cp target/classes org.example.Chat"
start cmd /k "java -cp target/classes org.example.Chat"
start cmd /k "java -cp target/classes org.example.Client"
start cmd /k "java -cp target/classes org.example.Client"

pause
