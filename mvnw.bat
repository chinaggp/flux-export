@echo off
set JAVA_HOME=D:\dev\tools\graalvm-jdk-17.0.12
set PATH=%JAVA_HOME%\bin;%PATH%
mvn %*
