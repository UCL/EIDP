/*
 * EIDPServicesBuilder.java
 *
 * Created on October 5, 2004, 12:55 PM
 */

package com.eidp.Services;

import java.util.* ;

import java.io.BufferedReader ;
import java.io.FileReader ;
import java.io.File ;

import org.apache.soap.* ;
import com.eidp.xml.XMLDataAccess ;

import org.w3c.dom.* ;
import javax.xml.parsers.* ;

import java.io.InputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.OutputStreamWriter ;
import java.io.ByteArrayInputStream ;

import java.security.Security;
import javax.net.ssl.* ;
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


public class ServicesBuilder {
    
    private Envelope envelope = new org.apache.soap.Envelope() ;
    private XMLDataAccess xmlDataAccess ;
    private Element bodyElement ;
    private boolean messageClosed = false ;
    
    /** Creates a new instance of EIDPServicesBuilder */
    public ServicesBuilder( String applicationContext , String userName , String password , String serviceToCall ) throws javax.xml.parsers.ParserConfigurationException , org.xml.sax.SAXException , java.io.IOException {
        this.setHeader( applicationContext ) ;
        this.bodyElement = this.initBody( userName , password , serviceToCall ) ;
        // ==== Jetzt kommt der Message-Builder Kram auf this.bodyElement
    }
    
    public void closeBody() {
        Vector bodyElements = new Vector() ;
        bodyElements.add( bodyElement ) ;
        // SOAP-Element erzeugen
        org.apache.soap.Body body = new org.apache.soap.Body() ;
        body.setBodyEntries( bodyElements ) ;
        // SOAP-Body zum Envelope hinzufuegen
        this.envelope.setBody( body ) ;
        this.messageClosed = true ;
    }
    
    public void appendMethodCall( String methodName , HashMap paramMap ) throws org.xml.sax.SAXException {
        Element methodCallEl = xmlDataAccess.createElement( "method-call" ) ;
        methodCallEl.setAttribute( "name" , methodName ) ;
        Set paramMapKeySet = paramMap.keySet() ;
        Iterator ki = paramMapKeySet.iterator() ;
        if ( paramMap.isEmpty() ) {
            Element paramEl = xmlDataAccess.createElement( "parameter" ) ;
            paramEl.setAttribute( "name" , "" ) ;
            paramEl.setAttribute( "value" , "" ) ;
            methodCallEl.appendChild( paramEl ) ;
        } else {
            while ( ki.hasNext() ) {
                String paramKey = (String)ki.next() ;
                Element paramEl = xmlDataAccess.createElement( "parameter" ) ;
                paramEl.setAttribute( "name" , paramKey ) ;
                paramEl.setAttribute( "value" , (String)paramMap.get( paramKey ) ) ;
                methodCallEl.appendChild( paramEl ) ;
            }
        }
        this.bodyElement.appendChild( methodCallEl ) ;
    }
    
    public HashMap sendMessage( String ipAddress ) throws org.apache.soap.SOAPException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , Exception  {
        return this.sendMessage( ipAddress , false , "" , "" ) ;
    }
    
    public HashMap sendMessage( String ipAddress , boolean SSL , String trustStore , String trustStorePWD ) throws org.apache.soap.SOAPException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , Exception  {
        HashMap returnObjects = new HashMap() ;
        if ( this.messageClosed == true ) {
            this.messageClosed = false ;
            org.apache.soap.messaging.Message msg = new org.apache.soap.messaging.Message();
            if ( SSL == true ) {
                System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                System.setProperty( "javax.net.ssl.trustStore" , trustStore ) ;
                System.setProperty( "javax.net.ssl.trustStorePassword" , trustStorePWD ) ;
                /***/
                com.sun.net.ssl.HostnameVerifier hv=new com.sun.net.ssl.HostnameVerifier() {
                    public boolean verify(String urlHostname, String certHostname) {
                        return true;
                    }
                };
                com.sun.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(hv);
                /***/
                msg.send( new java.net.URL( "https://" + ipAddress + "/EIDPServices/servlet/com.eidp.webctrl.Services.Services" ) , "urn:eidp-services" , this.envelope ) ;
            } else {
                msg.send( new java.net.URL( "http://" + ipAddress + "/EIDPServices/servlet/com.eidp.webctrl.Services.Services" ) , "urn:eidp-services" , this.envelope ) ;
            }
            // Antwort erwarten
            org.apache.soap.transport.SOAPTransport st = msg.getSOAPTransport() ;
            BufferedReader br = st.receive() ;
            String messageReceived = "" ;
            String line = br.readLine() ;
            while ( line != null ) {
                messageReceived += line ;
                line = br.readLine() ;
            }
            returnObjects = this.buildReturnObjectsMap( messageReceived ) ;
        } else {
            throw new Exception( "MessageSendException thrown when calling sendMessage: SOAP-Body not closed." ) ;
        }
        return returnObjects ;
    }
    
    private HashMap buildReturnObjectsMap( String returnMessage ) throws java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException {
        HashMap returnObjects = new HashMap() ;
        InputStream rmInputStream = this.convert( returnMessage ) ;
        XMLDataAccess rxmla = new XMLDataAccess( rmInputStream ) ;
        Vector exceptionCodes = rxmla.getAttributesByName( "exception,code" ) ;
        Vector exceptionMessages = rxmla.getAttributesByName( "exception,message" ) ;
        Vector exceptions = new Vector() ;
        for ( int ei = 0 ; ei < exceptionCodes.size() ; ei++ ) {
            String eC = (String)exceptionCodes.get( ei ) ;
            String eM = (String)exceptionMessages.get( ei ) ;
            exceptions.add( eC+": "+eM ) ;
        }
        returnObjects.put( "Exceptions" , exceptions ) ;
        Vector returnObjectTypeVector = rxmla.getAttributesByName( "return-object,type" ) ;
        Vector returnObjectIDVector = rxmla.getAttributesByName( "return-object,id" ) ;
        /*
         * returnObjectVectorNodes sind alle return-object Nodes abzuelgich
         * der returnObjectTypeVector-Groesse. Deshalb muessen die Compounds
         * mitgezaehlt werden und bei ResultSet der return-object-Knoten
         * durch i - countCompound errechnet werden.
         */
        Vector returnObjectVectorNodes = rxmla.getNodeListsByName( "return-object" ) ;
        if ( returnObjectTypeVector.size() != returnObjectIDVector.size() ) {
            throw new org.xml.sax.SAXException( "SAXException thrown when reading Return Objects." ) ;
        }
        int countCompounds = 0 ;
        for ( int i = 0 ; i < returnObjectTypeVector.size() ; i++ ) {
            String roType = (String)returnObjectTypeVector.get( i ) ;
            if ( roType.equals( "Compound" ) ) {
                // s.o. returnObjectVectorNodes
                String roID = (String)returnObjectIDVector.get( i ) ;
                String roValue = (String)((Vector)rxmla.getAttributesByName( "return-object,value" ) ).get( i ) ;
                returnObjects.put( roID , roValue ) ;
            } else if ( roType.equals( "ResultSet" ) ) {
                Vector resultVector = new Vector() ;
                // s.o. returnObjectVectorNodes
                NodeList returnObjectNode = null ;
                boolean returnObjectOK = true ;
                try {
                    returnObjectNode = (NodeList)returnObjectVectorNodes.get( countCompounds ) ;
                    returnObjectOK = true ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException e ) {
                    returnObjectOK = false ;
                }
                if ( returnObjectOK == true ) {
                    String roID = "" ;
                    roID = (String)returnObjectIDVector.get( i ) ;
                    // put vector of hashes in returnObjects:
                    // returnObjects.put( "ResultSet" , <Vector of Hashes> ) ;
                    Vector rRow = rxmla.getNodeListsByName( "result-row" , returnObjectNode ) ;
                    for ( int rowi = 0 ; rowi < rRow.size() ; rowi++ ) {
                        Vector rMapVector = rxmla.getAttributesByName( "result-map,id" , (NodeList)rRow.get( rowi ) ) ;
                        HashMap rowMap = new HashMap() ;
                        for ( int mapi = 0 ; mapi < rMapVector.size() ; mapi++ ) {
                            String mapValue = (String)((Vector)rxmla.getAttributesByName( "result-map,value" , (NodeList)rRow.get( rowi ) ) ).get( mapi ) ;
                            String mapID = (String)((Vector)rxmla.getAttributesByName( "result-map,id" , (NodeList)rRow.get( rowi ) ) ).get( mapi ) ;
                            rowMap.put( mapID , mapValue ) ;
                        }
                        resultVector.add( rowMap ) ;
                    }
                    returnObjects.put( roID , resultVector ) ;
                    countCompounds++ ;
                }
            }
        }
        return returnObjects ;
    }
    
    private void setHeader( String applicationContext ) throws javax.xml.parsers.ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance() ;
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder() ;
        Document doc = documentBuilder.newDocument() ;
        Element initElement = doc.createElement( "init-element" ) ;
        // einen Vektor f?r die Header Elemente
        Vector headerElements = new Vector() ;
        // ein Header-Element in einem Namensraum erzeugen
        org.w3c.dom.Element headerElement = doc.createElementNS( "urn:eidp-services" ,  "jaws:MessageHeader" ) ;
        // Unterknoten im Message-Header erzeugen
        
        org.w3c.dom.Element ele = doc.createElement( "Context" ) ;
        org.w3c.dom.Node textNode = doc.createTextNode( applicationContext ) ;
        ele.appendChild( textNode ) ;
        
        headerElement.appendChild( ele ) ;
        
        Date timestamp = new Date() ;
        String timeStampString = String.valueOf( timestamp.getTime() ) ;
        
        ele = doc.createElement( "MessageID" ) ;
        textNode = doc.createTextNode( timeStampString ) ;
        ele.appendChild( textNode ) ;
        
        headerElement.appendChild( ele ) ;
        
        headerElements.add( headerElement ) ;
        
        // Header hinzufuegen
        org.apache.soap.Header header = new org.apache.soap.Header() ;
        header.setHeaderEntries( headerElements ) ;
        this.envelope.setHeader( header ) ;
    }
    
    private Element initBody( String userName , String password , String serviceToCall ) throws org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        // 1. Body zusammenbauen
        this.xmlDataAccess = new XMLDataAccess() ;
        Element initElement = xmlDataAccess.getInitElement() ;
        initElement = this.setMetaInformation( initElement) ;
        // Login
        Element loginEl = xmlDataAccess.createElement( "login" ) ;
        Element userNameEl = xmlDataAccess.createElement( "user" ) ;
        Text userText = xmlDataAccess.createTextNode( userName ) ;
        userNameEl.appendChild( userText ) ;
        loginEl.appendChild( userNameEl ) ;
        Element pwdEl = xmlDataAccess.createElement( "password" ) ;
        Text pwdText = xmlDataAccess.createTextNode( password ) ;
        pwdEl.appendChild( pwdText ) ;
        loginEl.appendChild( pwdEl ) ;
        Element serviceEl = xmlDataAccess.createElement( "service" ) ;
        Text serviceText = xmlDataAccess.createTextNode( serviceToCall ) ;
        serviceEl.appendChild( serviceText ) ;
        loginEl.appendChild( serviceEl ) ;
        initElement.appendChild( loginEl ) ;
        return initElement ;
    }
    
    private Element setMetaInformation( Element initElement ) throws org.xml.sax.SAXException  {
        Element metaElement = xmlDataAccess.createElement( "meta-information" ) ;
        // ==============
        // name
        Element infoElement = xmlDataAccess.createElement( "name" ) ;
        Text infoText = xmlDataAccess.createTextNode( "EIDPServices" ) ;
        infoElement.appendChild( (Node)infoText ) ;
        metaElement.appendChild( (Node)infoElement ) ;
        // copyright
        infoElement = xmlDataAccess.createElement( "copyright" ) ;
        infoText = xmlDataAccess.createTextNode( "Toolwerk GmbH" ) ;
        infoElement.appendChild( (Node)infoText ) ;
        metaElement.appendChild( (Node)infoElement ) ;
        // append meta-information
        initElement.appendChild( (Node)metaElement ) ;
        return initElement ;
    }
    
    public static InputStream convert( String str ) throws java.io.IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OutputStreamWriter ow = new OutputStreamWriter( os, "utf-8" );
        
        ow.write( str , 0 , str.length() ) ;
        ow.flush() ;
        
        return new ByteArrayInputStream(os.toByteArray());
    }
    
}
