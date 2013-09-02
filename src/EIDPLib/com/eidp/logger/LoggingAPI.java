/*
 * LoggingAPI.java
 *
 * Created on June 10, 2003, 2:01 PM
 */

package com.eidp.logger;

/**
 * The EIDP Logging API for easy log-message handling.
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

public abstract interface LoggingAPI {
    
    /**
     * This method writes a log-message to the file the Logger has been
     * initialized with.
     * @param logMessage the log message to be written.
     * @throws IOException if io-routines fail.
     */
    public abstract void logMessage( String logMessage ) throws java.io.IOException ;
    
    /**
     * Close the logger-connection (file-connection).
     * @throws IOException
     */
    public abstract void close( ) throws java.io.IOException ;
    
}
