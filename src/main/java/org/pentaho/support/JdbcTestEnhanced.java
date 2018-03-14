package org.pentaho.support;

/**
 JdbcTestEnhanced - Test a JDBC connection using properties file for arguments

 Usage: java JdbcTestRunAs afile.properties

 Example:
 java -Djdbc.properties="C:/test/jdbc.properties" JdbcTestEnhanced

 The properties file must contain the following entries:
 jdbc.driver=
 jdbc.password=
 jdbc.sql=
 jdbc.url=

 Optional Settings
 jdbc.user=
 jdbc.max.display=
 jdbc.enhance.class=
 jdbc.long.timing.test=

 Written by Marc Batchelor
 */

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.Properties;

public class JdbcTestEnhanced {

  public static void main( String argv[] ) {

    int rtnCode = 0;
    Connection con = null;
    Statement sql = null;
    ResultSet results = null;

    Properties props = new Properties();
    try {
      if ( argv.length < 1 ) {
        props.putAll( System.getProperties() );
      } else {
        props.load( new FileReader( new File( argv[0] ) ) );
      }
    } catch ( Exception ex ) {
      System.err.println( "Error loading properties. Aborting." );
      System.exit( -9 );
    }

    String sqlToExecute = props.getProperty( "jdbc.sql" );
    String url = props.getProperty( "jdbc.url" );
    String driver = props.getProperty( "jdbc.driver" );
    String userid = props.getProperty( "jdbc.user" );
    String password = props.getProperty( "jdbc.password" );
    String maxWidthStr = props.getProperty( "jdbc.max.display.width", "50" );
    String timingTestStr = props.getProperty( "jdbc.long.timing.test" );
    String showResultSetMetadata = props.getProperty( "jdbc.show.resultsetmetadata", "true" );
    boolean longTimingTest = ( timingTestStr != null ) && ( timingTestStr.equalsIgnoreCase( "true" ) );

    long startTime = 0;
    long postConnectTime = 0;
    long postEnhanceTime = 0;
    long postConnectionMetadataTime = 0;
    long postMetadataOutputTime = 0;
    long postStatementCreateTime = 0;
    long postStatementEnhanceTime = 0;
    long postExecuteTime = 0;
    long postRSMDTime = 0;
    long postIterateResultSetTime = 0;
    long postUnenhanceConnectionTime = 0;
    long completeTime = 0;

    try {
      int maxWidth = 50;
      if ( maxWidthStr != null ) {
        try {
          maxWidth = Integer.parseInt( maxWidthStr );
        } catch ( Exception ignored ) {
        }
      }

      if ( sqlToExecute == null ) {
        System.err.println( "SQL not specified - you must supply jdbc.sql=<some select> to specify your SQL." );
        rtnCode = 1;
        return;
      }
      if ( driver == null ) {
        System.err
            .println( "JDBC Driver not specified. You must supply jdbc.driver=<some driver> to specify your JDBC driver class in the properties file." );
        rtnCode = 2;
        return;
      }

      if ( url == null ) {
        System.err.println( "JDBC URL not specified. You must specify jdbc.url=< to specify your JDBC driver URL." );
        rtnCode = 3;
        return;
      }

      if ( userid == null ) { // No userid
        System.err.println( "Warning - userid not supplied. Will be trying system user." );
      }

      if ( password == null ) { // No password
        System.err.println( "Warning - password not supplied. Will pass null to jdbc driver" );
      }

      try {
        Class.forName( driver );
      } catch ( Exception e ) {
        System.err.println( "Failed to load driver " + driver );
        e.printStackTrace();
        rtnCode = 4;
        return;
      } // catch

      startTime = System.currentTimeMillis();

      try {
        if ( ( userid != null ) || ( password == null ) ) {
          con = DriverManager.getConnection( url, userid, password ); // use this one if either userid or password is
                                                                      // supplied.
        } else {
          con = DriverManager.getConnection( url ); // Neither userid or password were supplied.
        }
        System.out.println( "Connected" );
      } catch ( Exception e ) {
        System.err.println( "Failed to connect" );
        e.printStackTrace();
        rtnCode = 6;
        return;
      }

      postConnectTime = System.currentTimeMillis();
      postEnhanceTime = System.currentTimeMillis();

      DatabaseMetaData dmd = null;
      try {
        dmd = con.getMetaData();
      } catch ( Exception e ) {
        System.err.println( "Failed to obtain Database Metadata" );
        e.printStackTrace();
        rtnCode = 3;
        return;
      }
      postConnectionMetadataTime = System.currentTimeMillis();

      try {
        System.out.println( "JDBC Driver And Database Information" );
        System.out.println( "Driver Name: " + dmd.getDriverName() );
        System.out.println( "Driver Version: " + dmd.getDriverVersion() );
        System.out.println( "JDBC Version: " + dmd.getJDBCMajorVersion() + "." + dmd.getJDBCMinorVersion() );
        System.out.println( "Database Product Name: " + dmd.getDatabaseProductName() );
        System.out.println( "Database Product Version: " + dmd.getDatabaseProductVersion() );
      } catch ( Exception ex ) {
        System.err.println( "Failed to get JDBC Driver Information" );
        ex.printStackTrace();
        rtnCode = 4;
        return;
      }

      postMetadataOutputTime = System.currentTimeMillis();

      try {
        sql = con.createStatement();

        postStatementCreateTime = System.currentTimeMillis();
        postStatementEnhanceTime = postStatementCreateTime;

        System.out.println( "Executing query: " + sqlToExecute );
        results = sql.executeQuery( sqlToExecute );

        postExecuteTime = System.currentTimeMillis();
        if ( longTimingTest ) {
          ResultSetMetaData rsmd = results.getMetaData();
          boolean hasRows = false;
          int cols = rsmd.getColumnCount();
          if ( ( showResultSetMetadata != null ) && (showResultSetMetadata.equalsIgnoreCase("true") ) ) {
            System.out.println(" *** Display ResultSet Metadata ***" );
            int colNo = 0;
            String separator = "\n    ";
            for ( int i = 0; i < cols; i++ ) {
              colNo++;
              System.out.println(
                  String.format("    Column Number: %2$d%1$sName: %3$s%1$sLabel: %4$s%1$sType Number: %5$d%1$sType Class: %6$s%1$sType Name: %7$s%1$sDisplay Size: %8$d%1$sScale: %9$d%1$s--------\n",
                    separator, // 1 string
                    colNo, // 2 int
                    rsmd.getColumnName( colNo ), // 3 string
                    rsmd.getColumnLabel( colNo ), // 4 string
                    rsmd.getColumnType( colNo ), // 5 int
                    rsmd.getColumnTypeName( colNo ), // 6 string
                    String.valueOf(rsmd.getColumnClassName( colNo )), // 7 string
                    rsmd.getColumnDisplaySize( colNo ), // 8 int
                    rsmd.getScale( colNo ) // 9 int
                  )
                );
            }
          }
          postRSMDTime = System.currentTimeMillis();
          System.out.println( "Column Count: " + cols );
          while ( results.next() ) {
            hasRows = true;
            for ( int i = 0; i < cols; i++ ) {
              results.getObject( i + 1 );
            }
          }
          if ( !hasRows ) {
            System.out.println( "No rows!" );
          }
          postIterateResultSetTime = System.currentTimeMillis();
        } else {
          if ( results.next() == false ) {
            System.out.println( "No rows" );
          } else { // display first row
            System.out.println( "First row of data (max width=" + maxWidth + ")" );
            Object objValue = null;
            String value = null;
            ResultSetMetaData rsmd = results.getMetaData();
            int cols = rsmd.getColumnCount();
            System.out.println( "Column Count: " + cols );
            postRSMDTime = System.currentTimeMillis();
            for ( int i = 0; i < cols; i++ ) {
              objValue = results.getObject( i + 1 );
              value = ( objValue != null ) ? objValue.toString() : "null";
              if ( value.length() > maxWidth ) {
                value = value.substring( 0, maxWidth );
              }
              System.out.println( "Column Name: " + rsmd.getColumnName( i + 1 ) + " / Value: " + value );
            }
            postIterateResultSetTime = System.currentTimeMillis();
          }
        }
        postUnenhanceConnectionTime = postIterateResultSetTime;
      } catch ( Exception e ) {
        System.err.println( "Failed to execute" );
        e.printStackTrace();
        rtnCode = 9;
        return;
      } // catch

    } finally {
      if ( results != null ) {
        try {
          results.close();
        } catch ( Exception ignored ) {
        }
      }
      if ( sql != null ) {
        try {
          sql.close();
        } catch ( Exception ignored ) {
        }
      }
      if ( con != null ) {
        try {
          con.close();
        } catch ( Exception ignored ) {
        }
      }
    } // finally
    // Print Timings
    completeTime = System.currentTimeMillis();
    if ( longTimingTest ) {
      printElapsed( startTime, postConnectTime, "Time to Connect" );
      printElapsed( postEnhanceTime, postConnectionMetadataTime, "Time to get Connection Metadata" );
      printElapsed( postConnectionMetadataTime, postMetadataOutputTime, "Time to display Connection Metadata" );
      printElapsed( postMetadataOutputTime, postStatementCreateTime, "Time to create statement" );
      printElapsed( postStatementEnhanceTime, postExecuteTime, "Time to execute Query" );
      printElapsed( postExecuteTime, postRSMDTime, "Time to get Resultset Metadata" );
      printElapsed( postRSMDTime, postIterateResultSetTime, "Time to iterate over entire Resultset" );
      printElapsed( postIterateResultSetTime, postUnenhanceConnectionTime, "Time to unenhance connection" );
      printElapsed( startTime, completeTime, "Total Time" );
    } else {
      // Simple complete-time output
      printElapsed( startTime, completeTime, "Total Time" );
    }
    System.exit( rtnCode );
  } // main

  public static void printElapsed( long start, long stop, String desc ) {
    long timeInSeconds = Math.abs( stop - start ) / 1000;
    long hours, minutes, seconds;
    hours = timeInSeconds / 3600;
    timeInSeconds = timeInSeconds - ( hours * 3600 );
    minutes = timeInSeconds / 60;
    timeInSeconds = timeInSeconds - ( minutes * 60 );
    seconds = timeInSeconds % 60;
    System.out.println( String.format( "%s - %d minute(s), %d second(s)", desc, minutes, seconds ) );
  }


} // JdbcTest
