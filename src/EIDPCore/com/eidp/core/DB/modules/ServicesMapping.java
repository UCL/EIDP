/*
 * EIDPServicesMapping.java
 *
 * Created on 28. April 2005, 12:54
 */

package com.eidp.core.DB.modules;

import com.eidp.Services.ServicesBuilder ;
import com.eidp.xml.XMLDataAccess ;

import java.util.HashMap;
import java.util.Vector;
import java.util.Set ;
import java.util.Iterator ;
import java.io.InputStream ;
import org.w3c.dom.NodeList;
import java.util.logging.*;

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
public class ServicesMapping extends DataSourceMapping implements DataSourceAPI {
    
    Object exception = null ;
    
    /** Creates a new instance of TwServicesMapping */
    public ServicesMapping(  String appContext , NodeList dataSourceNode , XMLDataAccess xmlda , Logger logger ) throws java.io.IOException , java.sql.SQLException , org.xml.sax.SAXException {
        this.applicationContext = appContext ;
        this.xmlDataAccess = xmlda ;
        Vector dbTypeVector = (Vector)this.xmlDataAccess.getElementsByName( "type" , dataSourceNode ) ;
        if ( dbTypeVector.size() > 0 ) {
            this.database.put( "service-context" , (String)((Vector)this.xmlDataAccess.getElementsByName( "service-context" , dataSourceNode )).get( 0 ) ) ;
            this.database.put( "ip-address" , (String)((Vector)this.xmlDataAccess.getElementsByName( "ip-address" , dataSourceNode )).get( 0 ) ) ;
            this.database.put( "service-name" , (String)((Vector)this.xmlDataAccess.getElementsByName( "service-name" , dataSourceNode )).get( 0 ) ) ;
            this.database.put( "login" , (String)((Vector)this.xmlDataAccess.getElementsByName( "login" , dataSourceNode )).get( 0 ) ) ;
            this.database.put( "password" , (String)((Vector)this.xmlDataAccess.getElementsByName( "password" , dataSourceNode )).get( 0 ) ) ;
        }
    }
    
    public void ProcessDBAction(org.w3c.dom.NodeList dataSetNode, org.w3c.dom.NodeList methodNode, java.util.HashMap paramMap , Logger logger ) throws Exception, org.xml.sax.SAXException, java.io.IOException {
        System.out.println( "ProcessDBAction" ) ;
        Vector returnVector = new Vector();
        System.out.println( "ParamMap: " + paramMap.toString() ) ;
        Set paramKeys = paramMap.keySet() ;
        Iterator pi = paramKeys.iterator() ;
        while ( pi.hasNext() ) {
            String key = (String)pi.next() ;
            String value = (String)paramMap.get( key ) ;
            if ( value != null ) {
                value = value.replace( '\'' , '-' ) ;
                paramMap.put( key , value ) ;
            }
        }
        String tableName = (String)((Vector)this.xmlDataAccess.getElementsByName( "table,name" , dataSetNode )).get(0) ;
        // 2. get type of method and direct it to the matching method
        String methodType = (String)((Vector)this.xmlDataAccess.getElementsByName( "type" , methodNode )).get(0) ;
        // Method type!
        if ( methodType.equals( "get" ) ) { this.get( tableName , dataSetNode , methodNode , paramMap ) ; }
        else if ( methodType.equals( "set" ) ) { this.set( tableName , dataSetNode , methodNode , paramMap ) ; }
        else if ( methodType.equals( "remove" ) ) { this.remove( tableName , dataSetNode , methodNode , paramMap ) ; }
    }
    
    public void closeConnection( Logger logger ) throws Exception {
    }
    
    public java.util.HashMap getRow(Integer rowNumber) {
        return (HashMap)this.resultVector.get( rowNumber.intValue() ) ;
    }
    
    public java.util.Vector getRowRange(Integer rowNumber, Integer endRow) {
        if ( endRow.intValue() > this.resultVector.size() ) {
            throw new javax.ejb.EJBException( "DBMapping - get: endRow exceeds size of ResultVector" ) ;
        }
        Vector returnVector = new Vector() ;
        for ( int i = rowNumber.intValue() ; i < endRow.intValue() ; i++ ) {
            returnVector.addElement( (HashMap)this.resultVector.get(i) ) ;
        }
        return returnVector;
    }
    
    public Integer size() {
        return new Integer( this.resultVector.size() ) ;
    }
    
    private void get(String tableName, NodeList dataSetNode, NodeList methodNode, HashMap paramMap ) throws Exception , org.xml.sax.SAXException , java.io.IOException {
        System.out.println( "Services (GET) paramMap = " + paramMap.toString() ) ;
        // this.DBLogger.info ( "Entering method DBMapping.get" ) ;
        HashMap sendParamMap = new HashMap() ;
        Vector resultSet = new Vector() ;
        // Service Method Name
        String methodName = (String)((Vector)this.xmlDataAccess.getElementsByName( "service-method" , methodNode ) ).get( 0 ) ;
        String resultObjectID = (String)((Vector)this.xmlDataAccess.getElementsByName( "result-object-id" , methodNode ) ).get( 0 ) ;
        // - selectIDs
        Vector selectIDs = (Vector)this.xmlDataAccess.getElementsByName( "query,field" , methodNode ) ;
        Vector fieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector fieldNames = (Vector)this.xmlDataAccess.getElementsByName( "table,field,name" , dataSetNode ) ;
        Vector fieldTypes = (Vector)this.xmlDataAccess.getElementsByName( "table,field,type" , dataSetNode ) ;
        HashMap fieldMap = this.getFields( fieldIDs , fieldNames ) ;
        HashMap typeMap = this.getFields( fieldIDs , fieldTypes ) ;
        HashMap selectFields = new HashMap();
        for ( int s_i = 0 ; s_i < selectIDs.size() ; s_i++ ) {
            selectFields.put( (String)selectIDs.get( s_i ) , (String)fieldMap.get( (String)selectIDs.get(s_i) ) ) ;
        }
        // - forFields
        Vector forVector = (Vector)this.xmlDataAccess.getNodeListsByName( "for" , (NodeList)methodNode ) ;
        Vector forFieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "for,field" , methodNode ) ;
        Vector forFields = new Vector() ;
        for ( int f_i = 0 ; f_i < forFieldIDs.size() ; f_i++ ) {
            String forKey = (String)fieldMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forFieldType = (String)typeMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forValue = new String();
            sendParamMap.put( forKey , (String)paramMap.get( (String)forFieldIDs.get(f_i) ) ) ;
        }
        this.ProcessSelect( methodName , resultObjectID , sendParamMap , selectIDs , fieldMap , typeMap ) ;
    }
    
    private void set(String tableName, NodeList dataSetNode, NodeList methodNode, HashMap paramMap ) throws Exception , org.xml.sax.SAXException , java.io.IOException {
        System.out.println( "Services (SET) paramMap = " + paramMap.toString() ) ;
        // this.DBLogger.info ( "Entering method DBMapping.get" ) ;
        HashMap sendParamMap = new HashMap() ;
        Vector resultSet = new Vector() ;
        // Service Method Name
        String methodName = (String)((Vector)this.xmlDataAccess.getElementsByName( "service-method" , methodNode ) ).get( 0 ) ;
        Vector resultObjectIDVector = (Vector)this.xmlDataAccess.getElementsByName( "result-object-id" , methodNode ) ;
        String resultObjectID = "" ;
        if ( resultObjectIDVector.size() > 0 ) {
            resultObjectID = (String)resultObjectIDVector.get( 0 ) ;
        }
        // - selectIDs
        Vector selectIDs = (Vector)this.xmlDataAccess.getElementsByName( "values,field" , methodNode ) ;
        Vector fieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector fieldNames = (Vector)this.xmlDataAccess.getElementsByName( "table,field,name" , dataSetNode ) ;
        Vector fieldTypes = (Vector)this.xmlDataAccess.getElementsByName( "table,field,type" , dataSetNode ) ;
        HashMap fieldMap = this.getFields( fieldIDs , fieldNames ) ;
        HashMap typeMap = this.getFields( fieldIDs , fieldTypes ) ;
        HashMap selectFields = new HashMap();
        for ( int s_i = 0 ; s_i < selectIDs.size() ; s_i++ ) {
            selectFields.put( (String)selectIDs.get( s_i ) , (String)fieldMap.get( (String)selectIDs.get(s_i) ) ) ;
        }
        // - forFields
        Vector forVector = (Vector)this.xmlDataAccess.getNodeListsByName( "for" , (NodeList)methodNode ) ;
        Vector forFieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "for,field" , methodNode ) ;
        Vector forFields = new Vector() ;
        for ( int f_i = 0 ; f_i < forFieldIDs.size() ; f_i++ ) {
            String forKey = (String)fieldMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forFieldType = (String)typeMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forValue = new String();
            sendParamMap.put( forKey , (String)paramMap.get( (String)forFieldIDs.get(f_i) ) ) ;
        }
        this.ProcessSet( methodName , sendParamMap , selectIDs , fieldMap , typeMap ) ;
    }
    
    private void remove(String tableName, NodeList dataSetNode, NodeList methodNode, HashMap paramMap ) throws Exception , org.xml.sax.SAXException , java.io.IOException {
        System.out.println( "Services (REMOVE) paramMap = " + paramMap.toString() ) ;
        // this.DBLogger.info ( "Entering method DBMapping.get" ) ;
        HashMap sendParamMap = new HashMap() ;
        Vector resultSet = new Vector() ;
        // Service Method Name
        String methodName = (String)((Vector)this.xmlDataAccess.getElementsByName( "service-method" , methodNode ) ).get( 0 ) ;
        Vector resultObjectIDVector = (Vector)this.xmlDataAccess.getElementsByName( "result-object-id" , methodNode ) ;
        String resultObjectID = "" ;
        if ( resultObjectIDVector.size() > 0 ) {
            resultObjectID = (String)resultObjectIDVector.get( 0 ) ;
        }
        // - selectIDs
        Vector fieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector fieldNames = (Vector)this.xmlDataAccess.getElementsByName( "table,field,name" , dataSetNode ) ;
        Vector fieldTypes = (Vector)this.xmlDataAccess.getElementsByName( "table,field,type" , dataSetNode ) ;
        HashMap fieldMap = this.getFields( fieldIDs , fieldNames ) ;
        HashMap typeMap = this.getFields( fieldIDs , fieldTypes ) ;
        HashMap selectFields = new HashMap();
        // - forFields
        Vector forVector = (Vector)this.xmlDataAccess.getNodeListsByName( "for" , (NodeList)methodNode ) ;
        Vector forFieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "for,field" , methodNode ) ;
        Vector forFields = new Vector() ;
        for ( int f_i = 0 ; f_i < forFieldIDs.size() ; f_i++ ) {
            String forKey = (String)fieldMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forFieldType = (String)typeMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forValue = new String();
            sendParamMap.put( forKey , (String)paramMap.get( (String)forFieldIDs.get(f_i) ) ) ;
        }
        this.ProcessRemove( methodName , sendParamMap , fieldMap , typeMap ) ;
    }
    
    private void ProcessSelect( String methodName , String resultObjectID , HashMap sendParamMap , Vector selectIDs , HashMap fieldMap , HashMap typeMap ) throws java.io.IOException {
        System.out.println( "Services (GET) sendParamMap = " + sendParamMap.toString() ) ;
        this.resultVector.clear() ;
        HashMap resultObjects = new HashMap() ;
        try {
            ServicesBuilder twb = new ServicesBuilder( (String)this.database.get( "service-context" ) , (String)this.database.get( "login" ) , (String)this.database.get( "password" ) , (String)this.database.get( "service-name" ) ) ;
            twb.appendMethodCall( methodName , sendParamMap ) ;
            twb.closeBody() ;
            resultObjects = twb.sendMessage( (String)this.database.get( "ip-address" ) ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException e ) {
            System.out.println( "ParserConfigurationException in GET method: " + e ) ;
        } catch ( org.xml.sax.SAXException e ) {
            System.out.println( "SAXException in GET method: " + e ) ;
        } catch ( java.io.IOException e ) {
            System.out.println( "IOException in GET method: " + e ) ;
        } catch ( org.apache.soap.SOAPException e ) {
            System.out.println( "SOAPException in GET method: " + e ) ;
        } catch ( java.lang.Exception e ) {
            System.out.println( "Exception in GET method: " + e ) ;
        }
        // get the first resultObject and put it in Vector:
        Vector resultObject = (Vector)resultObjects.get( resultObjectID ) ;
        Iterator ri = resultObject.iterator() ;
        while ( ri.hasNext() ) {
            HashMap resultMap = new HashMap() ;
            for ( int i = 0 ; i < selectIDs.size() ; i++ ) {
                resultMap.put( (String)selectIDs.get( i ) , (String)((HashMap)ri.next()).get( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ) ;
            }
            this.resultVector.addElement( resultMap ) ;
        }
        System.out.println( "Size of resultVector = " + this.resultVector.size() ) ;
        System.out.println( "   ------------------------   " ) ;
        System.out.println( "TwCheck: ResultVector: " + this.resultVector.toString() ) ;
        System.out.println( "   ------------------------   " ) ;
    }
    
    private void ProcessSet( String methodName , HashMap sendParamMap , Vector selectIDs , HashMap fieldMap , HashMap typeMap ) throws Exception , org.xml.sax.SAXException , java.io.IOException {
        System.out.println( "Services (SET) sendParamMap = " + sendParamMap.toString() ) ;
        this.resultVector.clear() ;
        try {
            ServicesBuilder twb = new ServicesBuilder( (String)this.database.get( "service-context" ) , (String)this.database.get( "login" ) , (String)this.database.get( "password" ) , (String)this.database.get( "service-name" ) ) ;
            twb.appendMethodCall( methodName , sendParamMap ) ;
            twb.closeBody() ;
            HashMap resultObjects = twb.sendMessage( (String)this.database.get( "ip-address" ) ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException e ) {
            System.out.println( "ParserConfigurationException in SET method: " + e ) ;
        } catch ( org.xml.sax.SAXException e ) {
            System.out.println( "SAXException in SET method: " + e ) ;
        } catch ( java.io.IOException e ) {
            System.out.println( "IOException in SET method: " + e ) ;
        } catch ( org.apache.soap.SOAPException e ) {
            System.out.println( "SOAPException in SET method: " + e ) ;
        } catch ( java.lang.Exception e ) {
            System.out.println( "Exception in SET method: " + e ) ;
        }
    }
    
    private void ProcessRemove( String methodName , HashMap sendParamMap , HashMap fieldMap , HashMap typeMap ) throws Exception , org.xml.sax.SAXException , java.io.IOException {
        System.out.println( "Services (REMOVE) ParamMap = " + sendParamMap.toString() ) ;
        this.resultVector.clear() ;
        try {
            ServicesBuilder twb = new ServicesBuilder( (String)this.database.get( "service-context" ) , (String)this.database.get( "login" ) , (String)this.database.get( "password" ) , (String)this.database.get( "service-name" ) ) ;
            twb.appendMethodCall( methodName , sendParamMap ) ;
            twb.closeBody() ;
            HashMap resultObjects = twb.sendMessage( (String)this.database.get( "ip-address" ) ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException e ) {
            System.out.println( "ParserConfigurationException in REMOVE method: " + e ) ;
        } catch ( org.xml.sax.SAXException e ) {
            System.out.println( "SAXException in REMOVE method: " + e ) ;
        } catch ( java.io.IOException e ) {
            System.out.println( "IOException in REMOVE method: " + e ) ;
        } catch ( org.apache.soap.SOAPException e ) {
            System.out.println( "SOAPException in REMOVE method: " + e ) ;
        } catch ( java.lang.Exception e ) {
            System.out.println( "Exception in REMOVE method: " + e ) ;
        }
    }
    
    public Object getException() {
        Object e = this.exception ;
        this.exception = null ;
        return e ;
    }
    
    public void setException( Object o ) {
        this.exception = o ;
    }
    
    public void resetException() {
        this.exception = null ;
    }
    
}
