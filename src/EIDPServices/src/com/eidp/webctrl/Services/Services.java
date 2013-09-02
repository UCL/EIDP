/*
 * Services.java
 *
 * Created on 30. August 2004, 16:31
 */

package com.eidp.webctrl.Services;

import com.eidp.xml.XMLDataAccess ;
import com.eidp.core.DB.DBMappingRemote ;
import com.eidp.core.DB.DBMappingHomeRemote ;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import javax.naming.NamingException;
import javax.ejb.CreateException;

import java.io.* ;
import java.text.* ;
import java.util.* ;
import javax.servlet.* ;
import javax.servlet.http.* ;
import org.w3c.dom.* ;
import org.apache.soap.util.xml.DOM2Writer ;
import javax.xml.parsers.DocumentBuilderFactory ;
import javax.xml.parsers.DocumentBuilder ;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

/** TwServices ist ein Standard-Web-Service Dienst. Web-Services werden per
 * XML (/com/eidp/PROJEKTCODE/resources/services/services.xml)
 * definiert. Filter fue einzelne Werte werden in filters.xml
 * angegeben.
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

public class Services extends HttpServlet {
    
    /** doGet ist eine illegale Anfrage an einen Service
     * und wird mit einer Fehlermeldung abgebrochen.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public void doGet( HttpServletRequest request , HttpServletResponse response )
    throws IOException , ServletException {
        System.out.println( "TwServices called with GET method." ) ;
        response.setStatus( HttpServletResponse.SC_BAD_REQUEST ) ;
    }
    
    /** doPost verarbeitet alle moeglichen Reqeusts an den
     * Web-Service-Dienst.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public void doPost( HttpServletRequest request , HttpServletResponse response ) throws IOException , ServletException {
        System.out.println( "____________________________" ) ;
        System.out.println( "EIDP Services (c) Dominic Veit" ) ;
        System.out.println( "____________________________" ) ;
        // HTTP-Header traversieren
        for ( Enumeration en = request.getHeaderNames() ; en.hasMoreElements() ; ) {
            String header = (String)en.nextElement() ;
            String value = request.getHeader( header ) ;
        }
        
        // Wenn Daten im Body sind, diese ebenfalls anzeigen
        String returnXMLObject = "" ;
        if ( request.getContentLength() > 0 ) {
            java.io.BufferedReader reader = request.getReader() ;
            
            // DocumentBuilder holen
            javax.xml.parsers.DocumentBuilder xdb = org.apache.soap.util.xml.XMLParserUtils.getXMLDocBuilder() ;
            
            // in einen DOM-Baum parsen
            Document doc ;
            try {
                doc = xdb.parse( new org.xml.sax.InputSource( reader ) ) ;
            } catch ( org.xml.sax.SAXException saxe ) {
                throw new javax.servlet.ServletException( "TwServices: SAXException when parsing InputSource: " + saxe ) ;
            }
            if ( doc == null ) {
                try {
                    // Ein Fehler ist aufgetreten
                    System.out.println( "TwServices: Could not access Contents-Structure." ) ;
                    throw new org.apache.soap.SOAPException( org.apache.soap.Constants.FAULT_CODE_CLIENT , "parsing error" ) ;
                } catch ( org.apache.soap.SOAPException soape ) {
                    throw new ServletException( "TwServices: SOAPException (doc = null ): " + soape ) ;
                }
            } else {
                
                // statische Methoden aufrufen, um den Envelope aus dem Dokument zu holen
                org.apache.soap.Envelope env = org.apache.soap.Envelope.unmarshall( doc.getDocumentElement() ) ;
                
                // Header
                org.apache.soap.Header header = env.getHeader() ;
                Vector headerEntries = header.getHeaderEntries() ;
                Element headerEl = (Element)headerEntries.get( 0 ) ;
                XMLDataAccess headerDataAccess ;
                HashMap headerData = new HashMap() ;
                try {
                    headerDataAccess = new XMLDataAccess( headerEl ) ;
                    headerData.put( "applicationContext" , (String)((Vector)headerDataAccess.getElementsByName( "Context" , (NodeList)headerEl )).get( 0 ) ) ;
                    headerData.put( "messageID" , (String)((Vector)headerDataAccess.getElementsByName( "MessageID" , (NodeList)headerEl )).get( 0 ) ) ;
                } catch ( org.xml.sax.SAXException saxe ) {
                    throw new ServletException( "SAXException when instantiating XMLDataAccess: " + saxe ) ;
                } catch ( javax.xml.parsers.ParserConfigurationException parse ) {
                    throw new ServletException( "ParserConfigurationException when instantiating XMLDataAccess: " + parse ) ;
                }
                // Header information:
                System.out.println( "Header: " ) ;
                System.out.println( headerData.toString() ) ;
                // Body
                HashMap loginData = new HashMap() ;
                org.apache.soap.Body body = env.getBody() ;
                Element exElement = (Element)((Vector)body.getBodyEntries()).get( 0 ) ;
                XMLDataAccess xmlCallerAccess ;
                try {
                    xmlCallerAccess = new XMLDataAccess( exElement ) ;
                    loginData.put( "loginName" , (String)((Vector)xmlCallerAccess.getElementsByName( "login,user" ) ).get( 0 ) ) ;
                    loginData.put( "password" , (String)((Vector)xmlCallerAccess.getElementsByName( "login,password" ) ).get( 0 ) ) ;
                    loginData.put( "serviceToCall" , (String)((Vector)xmlCallerAccess.getElementsByName( "login,service" ) ).get( 0 ) ) ;
                } catch ( org.xml.sax.SAXException saxe ) {
                    throw new ServletException( "SAXException when instantiating XMLDataAccess: " + saxe ) ;
                } catch ( javax.xml.parsers.ParserConfigurationException parse ) {
                    throw new ServletException( "ParserConfigurationException when instantiating XMLDataAccess: " + parse ) ;
                }
                
                try {
                    returnXMLObject = this.processService( xmlCallerAccess , loginData , (String)headerData.get( "applicationContext" ) ) ;
                } catch ( org.xml.sax.SAXException saxe ) {
                    throw new javax.servlet.ServletException( "SAXException when calling processExchange: " + saxe ) ;
                }
            }
        }
        response.setContentType( "text/xml" ) ;
        PrintWriter responseWriter = response.getWriter() ;
        responseWriter.println( returnXMLObject ) ;
        responseWriter.close() ;
        System.out.println( "____________________________" ) ;
    }
    
    private String processService( XMLDataAccess xmlCallerAccess , HashMap loginData , String applicationContext ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        HashMap localRefs = new HashMap() ;
        // 1. DBMapper connecten
        DBMappingRemote dbMapper = this.getDBMapper( applicationContext ) ;
        // Authentication
        HashMap userData = new HashMap() ;
        try {
            userData = this.getUserData( loginData , dbMapper ) ;
        } catch ( Exception rme ) {
            throw new javax.servlet.ServletException( "Authentication did not work: " + rme ) ;
        }
        // get ServiceMethod and check for correct User- and Center-Roles
        String serviceXMLFile = "/com/eidp/" + applicationContext + "/resources/services/services.xml" ;
        XMLDataAccess xmlServiceAccess ;
        try {
            xmlServiceAccess = new XMLDataAccess( serviceXMLFile ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException pce ) {
            throw new javax.servlet.ServletException( "Parser Configuration Exception when reading services.xml: " + pce ) ;
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "IOException when reading services.xml: " + ioe ) ;
        }
        // Logging flag:
        Vector loggingFlag = xmlServiceAccess.getElementsByName( "log-level" ) ;
        String logLevel = "" ;
        if ( loggingFlag.size() > 0 ) {
            logLevel = (String)loggingFlag.get( 0 ) ;
        }
        if ( logLevel.equals( "debug" ) ) {
            // === write body to standard-out
            java.io.StringWriter xmlWriter = new java.io.StringWriter() ;
            org.apache.soap.util.xml.DOM2Writer.serializeAsXML( (Node)xmlCallerAccess.getInitElement() , xmlWriter ) ;
            System.out.println( " ========= RECEIVED MESSAGE ======================================== " ) ;
            System.out.println( xmlWriter.toString() ) ;
            System.out.println( " =================================================================== " ) ;
            // ===
        }
        // load filters
        XMLDataAccess xmlFilterAccess ;
        String filtersXMLFile = "/com/eidp/" + applicationContext + "/resources/services/filters.xml" ;
        try {
            xmlFilterAccess = new XMLDataAccess( filtersXMLFile ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException pce ) {
            throw new javax.servlet.ServletException( "Parser Configuration Exception when reading services.xml: " + pce ) ;
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "IOException when reading services.xml: " + ioe ) ;
        }
        // get Service by name:
        Vector serviceVector = xmlServiceAccess.getNodeListsByName( "service" ) ;
        Iterator si = serviceVector.iterator() ;
        NodeList serviceNode = null ;
        boolean serviceNameExists = false ;
        while ( si.hasNext() ) {
            serviceNode = (NodeList)si.next() ;
            // get name
            String serviceName = (String)((Vector)xmlServiceAccess.getElementsByName( "name" , serviceNode )).get( 0 ) ;
            if ( serviceName.equals( loginData.get( "serviceToCall" ) ) ) {
                serviceNameExists = true ;
                // get role-permissions
                Vector rolePermissions = xmlServiceAccess.getElementsByName( "role-name" ) ;
                Iterator ri = rolePermissions.iterator() ;
                boolean successfulAuth = false ;
                while ( ri.hasNext() ) {
                    String rp = (String)ri.next() ;
                    Vector userRoles = (Vector)userData.get( "roles" ) ;
                    if ( userRoles.contains( rp ) ) {
                        // successfully authenticated
                        successfulAuth = true ;
                    }
                }
                if ( successfulAuth == false ) {
                    throw new javax.servlet.ServletException( "Role Authentication not successful for service: " + serviceName ) ;
                }
                break ;
            }
        }
        if ( serviceNameExists == false ) {
            throw new javax.servlet.ServletException( "Requested service name <" + (String)loginData.get( "serviceToCall" ) + "> does not exist." ) ;
        }
        // get service-calls from SOAP-Body and process these under controll
        // of the method-nodes.
        // process Method-Nodes
        Vector methodNodes = xmlServiceAccess.getNodeListsByName( "service-method" , serviceNode ) ;
        Vector methodNodeNames = xmlServiceAccess.getElementsByName( "service-method,name" , serviceNode ) ;
        if ( methodNodes.size() != methodNodeNames.size() ) {
            throw new javax.servlet.ServletException( "Service definitions are incorrect. Methods do not match method-names." ) ;
        }
        Vector methodCall = xmlCallerAccess.getNodeListsByName( "method-call" ) ;
        // initialize XML-Structure
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance() ;
        DocumentBuilder documentBuilder = null ;
        try {
            documentBuilder = builderFactory.newDocumentBuilder() ;
        } catch ( javax.xml.parsers.ParserConfigurationException pce ) {
            throw new javax.servlet.ServletException( "Could not initialize documentBuilder in processService: " + pce ) ;
        }
        Document doc = documentBuilder.newDocument() ;
        Element initElement = doc.createElement( "return" ) ;
        String returnXMLObject = "" ;
        Iterator mi = methodCall.iterator() ;
        while( mi.hasNext() ) {
            // get methodToCall, get the method-node
            NodeList methodToCall = (NodeList)mi.next() ;
            // get method Name to call
            String methodNameToCall = (String)((Vector)xmlCallerAccess.getAttributesByName( "name" , methodToCall ) ).get( 0 ) ;
            System.out.print( "Method to call: " + methodNameToCall + "; " ) ;
            if ( methodNodeNames.contains( methodNameToCall ) ) {
                int methodIndex = methodNodeNames.indexOf( methodNameToCall ) ;
                System.out.print( "Method-Index: " + methodIndex ) ;
                // Process Service-Method
                NodeList serviceMethod = (NodeList)methodNodes.get( methodIndex ) ;
                localRefs = this.doServiceMethod( xmlCallerAccess , xmlServiceAccess , methodToCall , serviceMethod , userData , dbMapper , xmlFilterAccess , logLevel ) ;
                // Return stuff for method
                Vector returnVector = new Vector() ;
                returnVector = xmlServiceAccess.getNodeListsByName( "return" , serviceMethod ) ;
                if ( returnVector.size() > 0 ) {
                    NodeList returnNode = (NodeList)returnVector.get( 0 ) ;
                    // Return Werte koennen aus local variablen oder resultSets stammen
                    // Rueckgabetypen:
                    // Compound (gemisch - String/Integer/Float/Whatever).
                    // ResultSet
                    Vector returnObject = xmlServiceAccess.getNodeListsByName( "return-object" , returnNode ) ;
                    Iterator ri = returnObject.iterator() ;
                    while( ri.hasNext() ) {
                        NodeList rObj = (NodeList)ri.next() ;
                        String returnType = (String)((Vector)xmlServiceAccess.getElementsByName( "type" , rObj ) ).get( 0 ) ;
                        if ( returnType.equals( "Compound" ) ) {
                            initElement = this.addCompoundObject( initElement , doc , xmlServiceAccess , rObj , localRefs ) ;
                        } else if ( returnType.equals( "ResultSet" ) ) {
                            initElement = this.addResultSetObject( initElement , doc , xmlServiceAccess , rObj , dbMapper ) ;
                        }
                    }
                }
            }
        }
        StringWriter xmlWriter = new StringWriter() ;
        DOM2Writer.serializeAsXML( (Node)initElement , xmlWriter ) ;
        returnXMLObject = xmlWriter.toString() ;
        try {
            dbMapper.remove() ;
        } catch ( java.rmi.RemoteException rme ) {
            throw new javax.servlet.ServletException( "Could not remove DBMapping Bean: " + rme ) ;
        } catch ( javax.ejb.RemoveException reme ) {
            throw new javax.servlet.ServletException( "Could not remove DBMapping Bean: " + reme ) ;
        }
        return returnXMLObject ;
    }
    
    private Element addCompoundObject( Element initElement , Document doc , XMLDataAccess xmlServiceAccess , NodeList returnObject , HashMap localRefs ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        // nur lokale Variablen
        Element ele = doc.createElement( "return-object" ) ;
        ele.setAttribute( "type" , "Compound" ) ;
        // get local reference and write to xml structure
        String localReference = (String)((Vector)xmlServiceAccess.getElementsByName( "local-ref" , returnObject ) ).get( 0 ) ;
        String returnID = (String)((Vector)xmlServiceAccess.getElementsByName( "return-id" , returnObject ) ).get( 0 ) ;
        ele.setAttribute( "id" , returnID ) ;
        ele.setAttribute( "value" , (String)localRefs.get( localReference ) ) ;
        initElement.appendChild( ele ) ;
        return initElement ;
    }
    
    private Element addResultSetObject( Element initElement , Document doc , XMLDataAccess xmlServiceAccess , NodeList returnObject , DBMappingRemote dbMapper ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        // nur lokale Variablen
        String returnID = (String)((Vector)xmlServiceAccess.getElementsByName( "return-id" , returnObject ) ).get( 0 ) ;
        Element ele = doc.createElement( "return-object" ) ;
        ele.setAttribute( "type" , "ResultSet" ) ;
        ele.setAttribute( "id" , returnID ) ;
        int resultSetSize = 0 ;
        try {
            resultSetSize = dbMapper.size() ;
        } catch ( Exception e ) {
            throw new javax.servlet.ServletException( "RemoteException when calling dbMapper.size() in addResultSetObject: " + e ) ;
        }
        for ( int i = 0 ; i < resultSetSize ; i++ ) {
            HashMap resultMap = new HashMap() ;
            try {
                resultMap = dbMapper.getRow( i ) ;
            } catch ( Exception e ) {
                throw new javax.servlet.ServletException( "RemoteException when retrieving resultMap in addResultSetObject: " + e ) ;
            }
            // new result-row element
            Element resultRowEle = doc.createElement( "result-row" ) ;
            // put together HashMapping
            Set resultKeySet = resultMap.keySet() ;
            Iterator ri = resultKeySet.iterator() ;
            while ( ri.hasNext() ) {
                String key = (String)ri.next() ;
                Element mapEle = doc.createElement( "result-map" ) ;
                mapEle.setAttribute( "id" , key ) ;
                mapEle.setAttribute( "value" , (String)resultMap.get( key ) ) ;
                resultRowEle.appendChild( mapEle ) ;
            }
            // finalize XML structure
            ele.appendChild( resultRowEle ) ;
        }
        initElement.appendChild( ele ) ;
        return initElement ;
    }
    
    private HashMap doServiceMethod( XMLDataAccess xmlCallerAccess , XMLDataAccess xmlServiceAccess , NodeList methodToCall , NodeList serviceMethod , HashMap userData , DBMappingRemote dbMapper , XMLDataAccess xmlFilterAccess , String logLevel ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        // get type of method
        String methodType = (String)((Vector)xmlServiceAccess.getElementsByName( "type" , serviceMethod ) ).get( 0 ) ;
        // get caller parameters
        // get parameter definitions
        HashMap callerMap = new HashMap() ;
        callerMap = this.getCallerParameters( xmlCallerAccess , xmlServiceAccess , methodToCall , serviceMethod ) ;
        // check values ( size and filters )
        // doFilters processes size and filters
        Vector paramVector = new Vector() ;
        paramVector = xmlServiceAccess.getNodeListsByName( "parameters,param" , serviceMethod ) ;
        // Filters muessen die callerMap zurueck geben!!!
        callerMap = this.doFilters( callerMap , xmlServiceAccess , paramVector , xmlFilterAccess , logLevel ) ;
        // Action stuff
        HashMap localRefs = new HashMap() ;
        Vector serviceActions = new Vector() ;
        serviceActions = xmlServiceAccess.getNodeListsByName( "action" , serviceMethod ) ;
        Iterator ai = serviceActions.iterator() ;
        while ( ai.hasNext() ) {
            NodeList action = (NodeList)ai.next() ;
            localRefs = this.doServiceAction( xmlServiceAccess , action , callerMap , localRefs , dbMapper ) ;
            System.out.println( "Local-Refs set to: " + localRefs.toString() ) ;
        }
        return localRefs ;
    }
    
    private HashMap doServiceAction( XMLDataAccess xmlServiceAccess , NodeList action , HashMap callerMap , HashMap localRefs , DBMappingRemote dbMapper ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        // Action ausfuehren und localRefs zurueckgeben.
        // parsen und zusammenbauen der references zur paramMap
        // localRefs bearbeiten.
        boolean databaseAction = true ;
        String dataSet = "" ;
        String method = "" ;
        try {
            dataSet = (String)((Vector)xmlServiceAccess.getElementsByName( "dataset" , action ) ).get( 0 ) ;
            method = (String)((Vector)xmlServiceAccess.getElementsByName( "method" , action ) ).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            databaseAction = false ;
        }
        if ( databaseAction == true ) {
            HashMap paramMap = new HashMap() ;
            paramMap = getParamMap( xmlServiceAccess , action , callerMap , localRefs ) ;
            // process action
            try {
                dbMapper.DBAction( dataSet , method , paramMap ) ;
                if ( dbMapper.getException() != null ) {
                    throw new java.rmi.RemoteException( "Services throws dbMapper Exception in ["+dataSet+"] ["+method+"] with ["+paramMap+"]:" + ((Exception)dbMapper.getException()) ) ;
                }
            } catch ( Exception e ) {
                throw new javax.servlet.ServletException( "RemoteException thrown when calling DBMapper in doService: " + e ) ;
            }
        }
        // check for set
        Vector setVector = (Vector)xmlServiceAccess.getNodeListsByName( "set" , action ) ;
        if ( setVector.size() > 0 ) {
            Iterator si = setVector.iterator() ;
            while ( si.hasNext() ) {
                NodeList setNode = (NodeList)si.next() ;
                String setType = (String)((Vector)xmlServiceAccess.getElementsByName( "type" , setNode ) ).get( 0 ) ;
                String setID = (String)((Vector)xmlServiceAccess.getElementsByName( "id" , setNode ) ).get( 0 ) ;
                String setRef = (String)((Vector)xmlServiceAccess.getElementsByName( "ref" , setNode ) ).get( 0 ) ;
                Object setValue ;
                if ( setType.equals( "local" ) ) {
                    setValue = new String() ;
                    setValue = "" ;
                    try {
                        try {
                            setValue = (String)((HashMap)dbMapper.getRow( 0 )).get( setRef ) ;
                        } catch ( Exception e ) {
                            throw new javax.servlet.ServletException( "RemoteException thrown when collecting data from DBMapper in doServiceAction: " + e ) ;
                        }
                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                        setValue = "" ;
                    }
                    if ( localRefs.containsKey( setID ) ) {
                        localRefs.remove( setID ) ;
                    }
                } else {
                    setValue = new String() ;
                    setValue = "" ;
                }
                localRefs.put( setID , setValue ) ;
            }
        }
        return localRefs ;
    }
    
    private HashMap getParamMap( XMLDataAccess xmlServiceAccess , NodeList action , HashMap callerMap , HashMap localRefs ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        HashMap paramMap = new HashMap() ;
        // 1. einlesen der references
        // 2. zusammenbauen der paramMap an Hand der references
        Vector refVector = new Vector() ;
        refVector = xmlServiceAccess.getNodeListsByName( "reference" , action ) ;
        Iterator ri = refVector.iterator() ;
        while ( ri.hasNext() ) {
            NodeList refNode = (NodeList)ri.next() ;
            String refType = (String)((Vector)xmlServiceAccess.getElementsByName( "type" , refNode ) ).get( 0 ) ;
            String refID = (String)((Vector)xmlServiceAccess.getElementsByName( "id" , refNode ) ).get( 0 ) ;
            if ( refType.equals( "parameter" ) ) {
                String refRef = (String)((Vector)xmlServiceAccess.getElementsByName( "ref" , refNode ) ).get( 0 ) ;
                String paramValue = (String)callerMap.get( refRef ) ;
                if ( paramValue == null ) {
                    paramValue = "" ;
                }
                paramMap.put( refID , paramValue ) ;
            } else if ( refType.equals( "local" ) ) {
                String refRef = (String)((Vector)xmlServiceAccess.getElementsByName( "ref" , refNode ) ).get( 0 ) ;
                String paramValue = (String)localRefs.get( refRef ) ;
                if ( paramValue == null ) {
                    paramValue = "" ;
                }
                paramMap.put( refID , paramValue ) ;
            } else if ( refType.equals( "value" ) ) {
                String paramValue = "" ;
                try {
                    paramValue = (String)((Vector)xmlServiceAccess.getElementsByName( "value" , refNode ) ).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException e ) {
                    paramValue = "" ;
                }
                paramMap.put( refID , paramValue ) ;
            } else if ( refType.equals( "timestamp" ) ) {
                Date timestamp = new Date() ;
                String timeStampString = String.valueOf( timestamp.getTime() ) ;
                paramMap.put( refID , timeStampString ) ;
            } else if ( refType.equals( "secGenKey" ) ) {
                Date timestamp = new Date() ;
                String timestampString = String.valueOf( timestamp.getTime() ) ;
                String secureString = "0-0-" + timestampString ;
                try {
                    secureString = this.encryptString( secureString ) ;
                } catch ( java.lang.Exception e ) {
                    throw new javax.servlet.ServletException( "Could not generate secure patient ID: " + e ) ;
                }
                paramMap.put( refID , secureString ) ;
                localRefs.put( refID , secureString ) ;
            } else if ( refType.equals("genDate") ) {
                DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = dformat.format(new Date());
                paramMap.put(refID , dateString);
            }
        }
        return paramMap ;
    }
    
    private HashMap doFilters( HashMap callerMap , XMLDataAccess xmlServiceAccess , Vector paramVector , XMLDataAccess xmlFilterAccess , String logLevel )  throws javax.servlet.ServletException , org.xml.sax.SAXException {
        System.out.println( " ======= Service Filters CallerMap: " + callerMap.toString() +" ========" ) ;
        Vector filterNodes = xmlFilterAccess.getNodeListsByName( "filter" ) ;
        Vector filterNames = xmlFilterAccess.getElementsByName( "filter,name" ) ;
        if ( filterNodes.size() != filterNames.size() ) {
            throw new javax.servlet.ServletException( "filters.xml incorrect" ) ;
        }
        Iterator pi = paramVector.iterator() ;
        while ( pi.hasNext() ) {
            NodeList paramNode = (NodeList)pi.next() ;
            // 0. name
            //String paramName = (String)((Vector)xmlServiceAccess.getElementsByName( "sender-name" , paramNode ) ).get( 0 ) ;
            //Modified by Guzman 28. Nov 2007
            String paramName = (String)((Vector)xmlServiceAccess.getElementsByName( "receiver-name" , paramNode ) ).get( 0 ) ;
            
            // 1. size
            String size = (String)((Vector)xmlServiceAccess.getElementsByName( "size" , paramNode ) ).get( 0 ) ;
            int intSize = Integer.parseInt( size ) ;
            String ifExceeds = (String)((Vector)xmlServiceAccess.getAttributesByName( "size,if-exceeds" , paramNode ) ).get( 0 ) ;
            if ( logLevel.equals( "debug" ) ) {
                System.out.println( "Filter-Processing: " + paramName ) ;
            }
            if ( ((String)callerMap.get( paramName )).length() > intSize ) {
                if ( ifExceeds.equals( "break" ) ) {
                    System.out.println( "Filter Exception: callerMap cleared. Parameter name: " + paramName ) ;
                    callerMap.clear() ;
                    return callerMap ;
                } else if ( ifExceeds.equals( "cut" ) ) {
                    String parameter = (String)callerMap.get( paramName ) ;
                    parameter = parameter.substring( 0 , intSize - 1 ) ;
                    callerMap.remove( paramName ) ;
                    callerMap.put( paramName , parameter ) ;
                }
            }
            // 2. filters
            Vector filtersToCall = xmlServiceAccess.getElementsByName( "filter" , paramNode ) ;
            Iterator fci = filtersToCall.iterator() ;
            while ( fci.hasNext() ) {
                String filterNameToCall = (String)fci.next() ;
                if ( filterNames.contains( filterNameToCall ) ) {
                    int filterIndex = filterNames.indexOf( filterNameToCall ) ;
                    NodeList filterNode = (NodeList)filterNodes.get( filterIndex ) ;
                    callerMap = this.processFilters( callerMap , filterNameToCall , (String)callerMap.get( paramName ) , xmlFilterAccess , filterNode ) ;
                }
            }
        }
        return callerMap ;
    }
    
    private HashMap processFilters( HashMap callerMap , String parameterName , String parameterValue , XMLDataAccess xmlFilterAccess , NodeList filterNode ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        boolean filterCheck = false ;
        // callerMap wird fuer crosscheck gebraucht
        String filterType = (String)((Vector)xmlFilterAccess.getElementsByName( "type" , filterNode ) ).get( 0 ) ;
        if ( filterType.equals( "Integer" ) ) {
            parameterValue = this.processIntFilter( filterNode , xmlFilterAccess , parameterValue ) ;
        } else if ( filterType.equals( "Float" ) ) {
            parameterValue = this.processFloatFilter( filterNode , xmlFilterAccess , parameterValue ) ;
        } else if ( filterType.equals( "Date" ) ) {
            parameterValue = this.processDateFilter( filterNode , xmlFilterAccess , parameterValue ) ;
        } else if ( filterType.equals( "RegExp" ) ) {
            
        } else if ( filterType.equals( "CrossInt" ) ) {
        } else if ( filterType.equals( "CrossFloat" ) ) {
        } else if ( filterType.equals( "CrossDate" ) ) {
        }
        callerMap.remove( parameterName ) ;
        callerMap.put( parameterName , parameterValue ) ;
        return callerMap ;
    }
    
    private String processIntFilter( NodeList filterNode , XMLDataAccess xmlFilterAccess , String paramValue ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        String filterSize = (String)((Vector)xmlFilterAccess.getElementsByName( "size" , filterNode ) ).get( 0 ) ;
        
        return paramValue ;
    }
    
    private String processFloatFilter( NodeList filterNode , XMLDataAccess xmlFilterAccess , String paramValue ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        
        
        return paramValue ;
    }
    
    private String processDateFilter( NodeList filterNode , XMLDataAccess xmlFilterAccess , String paramValue ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        
        return paramValue ;
    }
    
    private String processRegExpFilter( NodeList filterNode , XMLDataAccess xmlFilterAccess , String paramValue ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        
        return paramValue ;
    }
    
    private HashMap getCallerParameters( XMLDataAccess xmlCallerAccess , XMLDataAccess xmlServiceAccess, NodeList callerNode , NodeList serviceParamNode ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        HashMap paramMap = new HashMap() ;
        // get paramMatching
        HashMap paramMatching = new HashMap() ;
        Vector serviceCallerParamNames = new Vector() ;
        Vector serviceServiceParamNames = new Vector() ;
        serviceCallerParamNames = xmlServiceAccess.getElementsByName( "param,sender-name" , serviceParamNode ) ;
        serviceServiceParamNames = xmlServiceAccess.getElementsByName( "param,receiver-name" , serviceParamNode ) ;
        if ( serviceCallerParamNames.size() != serviceServiceParamNames.size() ) {
            System.out.println( "Exception: Reiceiver-Params and Sender-Params do not match" ) ;
            paramMap.clear() ;
            return paramMap ;
        }
        for ( int i = 0 ; i < serviceCallerParamNames.size() ; i++ ) {
            paramMatching.put( (String)serviceCallerParamNames.get( i ) , (String)serviceServiceParamNames.get( i ) ) ;
        }
        Vector paramValues = xmlCallerAccess.getAttributesByName( "parameter,value" , callerNode ) ;
        Vector paramNames = xmlCallerAccess.getAttributesByName( "parameter,name" , callerNode ) ;
        if ( paramNames.size() != paramValues.size() ) {
            System.out.println( "Exception: param-names and param-values do not match" ) ;
            paramMap.clear() ;
            return paramMap ;
        } else {
            for ( int i = 0 ; i < paramValues.size() ; i++ ) {
                paramMap.put( paramMatching.get( (String)paramNames.get( i ) ) , paramValues.get( i ) ) ;
            }
            return paramMap ;
        }
    }
    
    private DBMappingRemote getDBMapper( String applicationContext ) throws javax.servlet.ServletException {
        Properties prop = new Properties() ;
        prop.setProperty( "org.omg.CORBA.ORBInitialHost" , "localhost" ) ;
        prop.setProperty( "org.omg.CORBA.ORBInitialPort" , "33365" ) ;
        Context jndiContext ;
        Object ref ;
        try {
            jndiContext = new InitialContext( prop );
            ref = jndiContext.lookup("DBMapping");
        } catch ( javax.naming.NamingException ne ) {
            throw new javax.servlet.ServletException( "Naming Exception thrown when connecting to DBMapper: " + ne ) ;
        }
        
        DBMappingHomeRemote home = (DBMappingHomeRemote) PortableRemoteObject.narrow(ref, DBMappingHomeRemote.class);
        DBMappingRemote dbMapper ;
        try {
            dbMapper = home.create( applicationContext ) ;
        } catch ( java.sql.SQLException sqle ) {
            throw new javax.servlet.ServletException( "SQLException thrown when creating DBMapper Object: " + sqle ) ;
        } catch ( javax.ejb.CreateException ce ) {
            throw new javax.servlet.ServletException( "CreateException thrown when creating DBMapper Object: " + ce ) ;
        } catch ( java.rmi.RemoteException re ) {
            throw new javax.servlet.ServletException( "RemoteException thrown when creating DBMapper Object: " + re ) ;
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "IOException thrown when creating DBMapper Object: " + ioe ) ;
        }
        return dbMapper ;
    }
    
    private HashMap getUserData( HashMap headerData , DBMappingRemote dbMapper ) throws Exception , java.sql.SQLException , org.xml.sax.SAXException , java.io.IOException , javax.servlet.ServletException {
        HashMap returnAuthData = new HashMap() ;
        Vector userRoles = new Vector() ;
        HashMap userCenters = new HashMap() ;
        String userCenter = "" ;
        HashMap userData = new HashMap() ;
        // get user roles
        returnAuthData = dbMapper.Authenticate( (String)headerData.get( "loginName" ) , (String)headerData.get( "password" ) ) ;
        if ( dbMapper.isAuthenticated() ) {
            userData.put( "roles" , returnAuthData.get( "userRoles" ) ) ;  // Vector
            userData.put( "centers" , returnAuthData.get( "userCenters" ) ) ;  // HashMap
            userData.put( "user-center" , returnAuthData.get( "userCenter" ) ) ; // String
        } else {
            userData.clear() ;
            try {
                dbMapper.remove() ;
            } catch ( javax.ejb.RemoveException reme ) {
            }
        }
        return userData ;
    }
    
    private String encryptString( String encryptString ) throws java.lang.Exception {
        encryptString = new String( encrypt( encryptString ) ) ;
        BASE64Encoder encoder = new BASE64Encoder();
        encryptString = encoder.encode( encryptString.getBytes() ) ;
        return encryptString ;
    }
    
    private static byte[] encrypt(String inputString) throws java.lang.Exception {
        java.security.MessageDigest md =null;
        md = java.security.MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(inputString.getBytes( "ISO-8859-1" ));
        return md.digest();
    }
    
}
