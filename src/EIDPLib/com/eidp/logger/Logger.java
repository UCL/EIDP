/*
 * Logger.java
 *
 * Created on June 4, 2003, 4:32 PM
 */

package com.eidp.logger;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

/**
 * EIDP log-message writer.
 *
 * @author Dominic Veit
 * @version 3.0
 * @copyright Copyright (C) 2005 Dominic Veit (dominic.veit@eo-consulting.de)
 * Enterprise Integration and Development Platform
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License form ore details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 */


public class Logger {
    
    FileWriter logfile ;
    BufferedWriter logwriter ;
    PrintWriter logprinter ;
    
    /**
     * Creates a new instance of MedDataLogger.
     * @param filename filename of the log file to be written. New log messages are appended
     * to the file if there is an existing one.
     * @throws IOException
     */
    public Logger( String filename ) throws java.io.IOException {
        // open file:
        this.logfile = new FileWriter( filename , true ) ;
        this.logwriter = new BufferedWriter( logfile ) ;
        this.logprinter = new PrintWriter( logwriter ) ;
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
        this.logprinter.println( "-----------------" ) ;
        this.logprinter.println("[EIDP] Logprinter started at: " + sdf.format(cal.getTime()) + " / " + stf.format(cal.getTime()) + ".");
        this.logprinter.flush() ;
    }
    
    /**
     * send a message to the log-writer.
     * @param logmessage LogMessage to be written to log-file.
     * @throws IOException
     */
    public void logMessage( String logmessage ) throws java.io.IOException {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
        this.logprinter.println("[EIDP Log " + sdf.format(cal.getTime()) + " / " + stf.format(cal.getTime()) + "] " + logmessage );
        this.logprinter.flush() ;
    }
    
    /**
     * Close the log-writer connection.
     * @throws IOException if io-routines fail.
     */
    public void close() throws java.io.IOException {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
        this.logprinter.println( "[EIDP] Logprinter stoped at: " + sdf.format(cal.getTime()) + " / " + stf.format(cal.getTime()) + "." ) ;
        this.logprinter.println( "-----------------" ) ;
        this.logprinter.flush() ;
        this.logprinter.close() ;
    }
}
