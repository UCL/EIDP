/*
 * DataSourceSharedMethods.java
 *
 * Created on 5. April 2005, 10:36
 */

package com.eidp.core.DB.modules;

import com.eidp.xml.XMLDataAccess;
import com.eidp.logger.Logger;

import org.w3c.dom.NodeList;

import com.eidp.xml.XMLDataAccess ;

import java.util.HashMap ;
import java.util.Vector ;

/**
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
public class DataSourceMapping {
    
    protected HashMap database = new HashMap();
    protected XMLDataAccess xmlDataAccess;
    protected Vector resultVector = new Vector() ;
    
    protected DataSourceMapping dataSourceMapping = null ;
    
    protected String applicationContext = "" ;
    
    public DataSourceMapping() {
    }
    
    protected HashMap getFields(  Vector fieldIDs , Vector fieldNames ) throws org.xml.sax.SAXException , java.io.IOException {
        if ( fieldIDs.size() != fieldNames.size() ) { throw new org.xml.sax.SAXException( "DBMapping getFields throws XML Exception: fieldIDs and fieldNames do not match." ) ; }
        HashMap fieldMap = new HashMap() ;
        for ( int f_i = 0 ; f_i < fieldIDs.size() ; f_i++ ) {
            fieldMap.put( (String)fieldIDs.get( f_i ) , (String)fieldNames.get( f_i ) ) ;
        }
        return fieldMap;
    }
    
    protected HashMap getDataTypesForDataSet(  NodeList dataSetNode ) throws org.xml.sax.SAXException , java.io.IOException {
        HashMap dataTypes = new HashMap() ;
        // key = id ; type = value
        Vector ids = this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector types = this.xmlDataAccess.getElementsByName( "table,field,type" , dataSetNode ) ;
        if ( ids.size() != types.size() ) { throw new org.xml.sax.SAXException( "DBMapping getDataTypesForDataSet throws XML Exception: ids do not match size" ) ; }
        for ( int i = 0 ; i < ids.size() ; i++ ) {
            dataTypes.put( (String)ids.get(i) , (String)types.get(i) ) ;
        }
        return dataTypes;
    }
    
    protected HashMap getDataSizesForDataSet( NodeList dataSetNode ) throws org.xml.sax.SAXException , java.io.IOException {
        HashMap dataSizes = new HashMap() ;
        // key = id ; type = value
        Vector ids = this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector sizes = this.xmlDataAccess.getElementsByName( "table,field,size" , dataSetNode ) ;
        if ( ids.size() != sizes.size() ) { throw new org.xml.sax.SAXException( "DBMapping getDataSizesForDataSet throws XML Exception: ids do not match size" ) ; }
        for ( int i = 0 ; i < ids.size() ; i++ ) {
            dataSizes.put( (String)ids.get(i) , (String)sizes.get(i) ) ;
        }
        return dataSizes;
    }
    
}
