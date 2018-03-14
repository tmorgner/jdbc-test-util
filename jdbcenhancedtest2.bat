@echo off
setlocal EnableDelayedExpansion

if @%1@ == @@ goto USAGE

set CLASSPATH=.
FOR %%F IN (target\*.jar) DO (set CLASSPATH=!CLASSPATH!;%%F)
FOR %%F IN (lib\*.jar) DO (set CLASSPATH=!CLASSPATH!;%%F)
FOR %%F IN (lib\*.zip) DO (set CLASSPATH=!CLASSPATH!;%%F)

java -cp %CLASSPATH% org.pentaho.support.App %1

GOTO EOF

:USAGE
CLS
ECHO *********************************************************************************************
ECHO *
ECHO *                              JDBC Enhanced Test Utility
ECHO *
ECHO * 1- Copy your jdbc driver to the lib directory
ECHO * 2- create a .properties file to match your configuration (see example.jdbc.properties)
ECHO * 3- Run this test utility
ECHO *
ECHO * Usage: %0 afile.properties
ECHO *
ECHO * Example: 
ECHO * %0 example.jdbc.properties
ECHO *
ECHO *********************************************************************************************
:EOF
