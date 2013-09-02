/*
 * XMLDataAccess.java
 */

package com.eidp.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Vector;

/**
 * XMLDataAccess implements the EIDP XMLApi. All get methods
 * take a comma-seperated list as their first argument. This list
 * specifies the tags to be retrieved in sequential order. The last
 * element in the list is the element with the return-type to be
 * retrieved in a Vector (e.g. getElementsByName returns a Vector of
 * Strings, getAttributesByName returns a Vector of Strings,
 * getNodeListsByName returns a Vector of NodeLists).
 *
 * The create-methods take only a String argument. In case of
 * createElement the argument specifies the name (tag) of the
 * Element to be created. In case of createTextNode the argument
 * specifies the contents of the TextNode. The created nodes are
 * returned and can be set into the tree by using the
 * appendChild( Node ) method.
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
public class XMLDataAccess implements XMLApi {
    
    private Document document;
    private Element initElement;
    
    /**
     * Create a new XML-Document.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    
    public XMLDataAccess() throws org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance() ;
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder() ;
        this.document = documentBuilder.newDocument() ;
        this.initElement = document.createElement( "tw-gen" ) ;
    }
    /**
     * Instantiate an XML document by filename. The filename
     * is specified as String.
     * @param xmlfile filename to be accessed.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public XMLDataAccess( String xmlfile ) throws org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        // 1. initialize XML-document-parser:
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        try {
            // 2. parse the XML-stream
            this.document = documentBuilder.parse( new File(new URI("file:" + xmlfile)) ); // passing a String directly may throw a MalformedURLException: no protocol. mod. by David Guzman
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } 
        // 3. get the initial element of the parsed XML-document
        this.initElement = document.getDocumentElement();
    }
    
    /**
     * Instantiate a XML document by InputSource.
     * @param inputSource the xml-tree to be instantiated.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public XMLDataAccess( org.xml.sax.InputSource inputSource ) throws org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        // 1. initialize XML-document-parser:
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        // 2. parse the XML-InputSource
        this.document = documentBuilder.parse( inputSource );
        // 3. get the initial element of the parsed XML-document
        this.initElement = document.getDocumentElement();
    }
    
    /**
     * Instantiates an XML document by InputStream.
     * @param inputStream the xml-document as InputStream.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public XMLDataAccess( InputStream inputStream ) throws org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        // 1. initialize XML-document-parser:
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        // 2. parse the XML-stream
        this.document = documentBuilder.parse( inputStream );
        // 3. get the initial element of the parsed XML-document
        this.initElement = document.getDocumentElement();
    }
    
    /**
     * Instantiates an XML document by Element.
     * @param initElement the initElement to be instantiated.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public XMLDataAccess( Element initElement ) throws org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        this.initElement = initElement ;
    }
    
    // public get-methods
    
    /**
     * Retrieve Elements from an XML DOM tree.
     * @param requestString Comma-seperated list of elements to be retrieved
     * from the DOM-tree. The last element of the list
     * has to be an Element-tag.
     * @throws SAXException
     * @return a Vector of Strings.
     */
    public Vector getElementsByName( String requestString ) throws org.xml.sax.SAXException {
        requestString = requestString.replaceAll( " " , "" ) ;
        String [] requestArray = requestString.split(",") ;
        Vector responseVector = new Vector();
        responseVector = this.ProcessgetElementsByName( this.initElement, requestArray, responseVector, 0 ) ;
        return responseVector;
    }
    
    /**
     * Retrieve Attributes from an XML DOM tree.
     * @param requestString Comma-seperated list of elements to be retrieved
     * from the DOM-tree.
     * The last tag has to be an Attribute-Node.
     * @throws SAXException
     * @return Vector of Strings.
     */
    public Vector getAttributesByName( String requestString ) throws org.xml.sax.SAXException {
        requestString = requestString.replaceAll( " " , "" ) ;
        String [] requestArray = requestString.split(",") ;
        Vector responseVector = new Vector();
        responseVector = this.ProcessgetAttributesByName( this.initElement, requestArray, responseVector, 0 ) ;
        return responseVector;
    }
    
    /**
     * Retrieve NodeLists from an XML DOM tree.
     * @param requestString Comma-seperated list of elements to be retrieved
     * from the DOM-tree.
     * The last tag has to be a NodeList-Node.
     * @throws SAXException
     * @return Vector of NodeLists.
     */
    public Vector getNodeListsByName( String requestString ) throws org.xml.sax.SAXException {
        requestString = requestString.replaceAll( " " , "" ) ;
        String [] requestArray = requestString.split(",") ;
        Vector responseVector = new Vector();
        responseVector = this.ProcessgetNodeListsByName( this.initElement , requestArray , responseVector, 0 ) ;
        return responseVector;
    }
    
    // ===> getElementsFromNodeList uses getElementsByName !!!
    /**
     * Retrieve Elements from an XML DOM tree.
     * @param requestString Comma-seperated list of elements to be retrieved
     * from the DOM-tree.
     * The last tag has to be an Element-Node.
     * @param requestList NodeList to be requested.
     * @throws SAXException
     * @return Vector of Strings.
     */
    public Vector getElementsByName( String requestString , NodeList requestList ) throws org.xml.sax.SAXException {
        requestString = requestString.replaceAll( " " , "" ) ;
        String [] requestArray = requestString.split(",") ;
        Vector responseVector = new Vector();
        Element element = (Element)requestList ;
        responseVector = this.ProcessgetElementsByName( element , requestArray, responseVector, 0 ) ;
        return responseVector;
    }
    
    /**
     * Retrieve Attributes from an XML DOM tree.
     * @param requestString Comma-seperated list of elements to be retrieved
     * from the DOM-tree.
     * The last tag has to be an Attribute-Node.
     * @param requestList NodeList to be requested.
     * @throws SAXException
     * @return Vector of Strings.
     */
    public Vector getAttributesByName ( String requestString , NodeList requestList ) throws org.xml.sax.SAXException {
        requestString = requestString.replaceAll( " " , "" ) ;
        String [] requestArray = requestString.split(",") ;
        Vector responseVector = new Vector();
        Element element = (Element)requestList ;
        responseVector = this.ProcessgetAttributesByName( element , requestArray, responseVector, 0 ) ;
        return responseVector;
    }
    
    /**
     * Retrieve NodeLists from an XML DOM tree.
     * @param requestString Comma-seperated list of elements to be retrieved
     * from the DOM-tree.
     * The last tag has to be an Attribute-Node.
     * @param requestList NodeList to be requested.
     * @throws SAXException
     * @return Vector of NodeLists.
     */
    public Vector getNodeListsByName( String requestString , NodeList requestList ) throws org.xml.sax.SAXException {
        requestString = requestString.replaceAll( " " , "" ) ;
        String [] requestArray = requestString.split(",") ;
        Vector responseVector = new Vector();
        Element element = (Element)requestList ;
        responseVector = this.ProcessgetNodeListsByName( element , requestArray, responseVector, 0 ) ;
        return responseVector ;
    }
    
    // private process-get-methods
    
    private Vector ProcessgetElementsByName( Element element, String [] requestArray, Vector responseVector, int requestArrayPos ) {
        if ( requestArrayPos == requestArray.length ) {
            if ( element.hasChildNodes() ) {
                NodeList childNodes = element.getChildNodes();
                if ( childNodes.item(0).getNodeType() == Node.TEXT_NODE ) {
                    responseVector.addElement( (String)childNodes.item(0).getNodeValue() );
                } else {
                    responseVector.addElement( "" );
                }
            }
        } else {
            NodeList xmlnodes = element.getElementsByTagName( requestArray[requestArrayPos] ) ;
            for ( int i = 0 ; i < xmlnodes.getLength() ; i++ ) {
                this.ProcessgetElementsByName( (Element)xmlnodes.item(i), requestArray, responseVector, requestArrayPos+1 ) ;
            }
        }
        return responseVector;
    }
    
    private Vector ProcessgetNodeListsByName( Element element, String [] requestArray, Vector responseVector, int requestArrayPos ) {
        if ( requestArrayPos == requestArray.length ) {
            if ( element.hasChildNodes() ) {
                // NodeList childNodes = element.getChildNodes();
                // responseVector.addElement( (NodeList)childNodes );
                responseVector.addElement( (Element)element );
            }
        } else {
            NodeList xmlnodes = element.getElementsByTagName( requestArray[requestArrayPos] ) ;
            for ( int i = 0 ; i < xmlnodes.getLength() ; i++ ) {
                this.ProcessgetNodeListsByName( (Element)xmlnodes.item(i), requestArray, responseVector, requestArrayPos+1 ) ;
            }
        }
        return responseVector;
    }
    
    private Vector ProcessgetAttributesByName( Element element, String [] requestArray, Vector responseVector, int requestArrayPos ) {
        if ( requestArrayPos == requestArray.length - 1 ) {
            NamedNodeMap attributes = element.getAttributes() ;
            boolean nodefound = false ;
            for ( int i = 0 ; i < attributes.getLength() ; i++ ) {
                Node currentAttribute = attributes.item( i ) ;
                if ( currentAttribute.getNodeName().equals( requestArray[ requestArrayPos ] ) ) {
                    responseVector.add( currentAttribute.getNodeValue() ) ;
                    nodefound = true ;
                }
            }
            if ( nodefound == false ) {
                responseVector.add( "" ) ;
            }
        } else {
            NodeList xmlnodes = element.getElementsByTagName( requestArray[requestArrayPos] ) ;
            for ( int i = 0 ; i < xmlnodes.getLength() ; i++ ) {
                this.ProcessgetAttributesByName( (Element)xmlnodes.item(i), requestArray, responseVector, requestArrayPos+1 ) ;
            }
        }
        return responseVector;
    }
    
    /**
     * Retrieve the root Element of the DOM tree.
     * @throws SAXException
     * @return root Element of the XML DOM tree.
     */
    public Element getInitElement() throws org.xml.sax.SAXException {
        return this.initElement ;
    }
    
    /**
     * Create a new Element in the instantiated XML DOM tree.
     * @param name name of the Element to be created.
     * @throws SAXException
     * @return the newly created Element for the instantiated
     * XML DOM tree.
     */
    public Element createElement( String name ) throws org.xml.sax.SAXException {
        return this.document.createElement( name ) ;
    }
    
    /**
     * Create a text node for the instantiated XML DOM tree.
     * @param textValue contents of the TextNode to be created.
     * @throws SAXException
     * @return the text node created.
     */
    public Text createTextNode( String textValue ) throws org.xml.sax.SAXException {
        return this.document.createTextNode( textValue ) ;
    }
    
}
