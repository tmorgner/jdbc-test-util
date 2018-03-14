#!/bin/sh

if [ $# -ne 1 ]
then
  echo '*********************************************************************************************'
  echo '*'
  echo '*                              JDBC Test Utility'
  echo '*'
  echo '* 1- Copy your jdbc driver to the lib directory'
  echo '* 2- create a .properties file to match your configuration (see example.jdbc.properties)'
  echo '* 3- Run this test utility'
  echo '*'
  echo '* Usage:' `basename $0`' properties_file'
  echo '*'
  echo '* Example: '
  echo '*' `basename $0`' ./test.properties'
  echo '*'
  echo '*********************************************************************************************'
  exit 1
fi

BASEDIR=`dirname $0`
cd $BASEDIR

CLASSPATH=$BASEDIR

for f in `find $BASEDIR/lib -type f -name "*.jar"` `find $BASEDIR/lib -type f -name "*.zip"`
do
  CLASSPATH=$CLASSPATH:$f
done

echo $CLASSPATH

java -cp $CLASSPATH org.pentaho.jdbc.utilities.JdbcTestEnhanced "$1"
