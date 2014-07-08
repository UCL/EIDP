/*
 * DBRemote.java
 *
 * Created on April 28, 2003, 10:47 AM
 */

package com.eidp.core.DB;

import java.io.IOException;
import java.util.Vector;
import java.util.HashMap;
import javax.ejb.CreateException;
import javax.ejb.Remote;

/**
 * Remote Interface for DBMapping.
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
@Remote
public interface DBMappingRemote {
    
    /**
     * DBAction reads in the necessary information to process the required SQL
     * querying. DBAction reads in the file
     * <CODE>com.eidp.&lt;applicationname&gt;resources.db.xml</CODE> and
     * performs under the specifications given in this file the necessary actions.
     * "dataset" and "method" specify the table and the method described in the db.xml
     * file. The paramMap contains the data that is needed by dataset-method to perform
     * the required action. paramMap is a HashMap that contains the data by field-ids.
     * @param dataset the name of the dataset to be processed
     * @param method the name of the method within dataset to be processed
     * @param paramMap the parameters given
     * @throws org.xml.sax.SAXException
     */
    public void DBAction( String dataset , String method , HashMap paramMap ) throws org.xml.sax.SAXException ;
    
    /**
     * Returns data in form of a HashMap kept by the given rowNumber. The keys are ids
     * and not columnnames.
     * @param rowNumber specifies the rowNumber to be retrieved
     * @return the HashMap that is keeped by the specified rowNumber
     */
    public HashMap getRow( int rowNumber );
    
    /**
     * Returns a vector that holds the data in the requested range. Data is represented
     * as HashMaps. The keys are ids, not columnnames.
     * @param rowNumber retrieve data from this rowNumber
     * @param endRowNumber retrieve data until this endRowNumber
     * @return a vector that consists of the requested data (HashMaps)
     */
    public Vector getRowRange( int rowNumber , int endRowNumber );
    
    /**
     * Returns the size of the result set Vector.
     * @return size of the result set vector
     */
    public int size();
    
    public HashMap Authenticate( String TW_PRINCIPAL , String TW_CREDENTIALS ) ;
    
    public boolean isAuthenticated() ;
    
    public void setApplicationContext(String applicationContext) throws IOException;

    public void resetException() throws Exception;

    public void setException(Object o) throws Exception;

    public Object getException() throws Exception;

    public void remove();
}
