/*
 * XMLApi.java
 */

package com.eidp.xml;

import java.util.Vector;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Attr;

/**
 * This interface describes the aplication programmers interface
 * for the EIDP XMLApi. The EIDP XMLApi offers a
 * simplified layer for XML-processing.
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
public abstract interface XMLApi {
    
    /**
     * This method retrieves a <B>Vector</B> of
     * <CODE>TEXT_NODEs</CODE>. The String that specifies the
     * <B>nodes</B> to retrieve consists of the linear order
     * of the nodes to be processed, seperated by comma.
     * <p>
     * E.g. if NAME in the node
     * <p>
     * <CODE>&lt;test&gt;&lt;name&gt;NAME&lt;/name&gt;&lt;/test&gt;</CODE> shall
     * <p>
     * beretrieved, the String shall look like:
     * <p>
     * <CODE>String requestString = test,name" ;</CODE>
     * <p>
     * The <B>root element</B> of the xml-file <B>must not</B> be put into the
     * requestString. The first element in the requestString is the element under the root
     * element.
     * @param requestString Strings that defines the TEXT_NODE to be retrieved; hierarchichal nodes are
     * specified by a comma-seperated list
     * @return a Vector consisting of the TEXT_NODES requested by the requestArray
     * @throws SAXException
     */
    public abstract Vector getElementsByName( String requestString ) throws org.xml.sax.SAXException ;
    
    /**
     * This method retrieves a <B>Vector</B> that consists NodeLists.
     * <p>
     * E.g. if the nodes under <CODE>&lt;test&gt;&lt;name&gt;&lt;</CODE> in the structure
     * <p>
     * <CODE>&lt;root&gt;<br>
     * &lt;test&gt;&lt;name&gt;&lt;<br>
     * firstname&gt;&lt;/firstname&gt;<br>
     * &lt;lastname&gt;&lt;/lastname&gt;<br>
     * &lt;title&gt;&lt;/title&gt;<br>
     * &lt;/name&gt;&lt;/test&gt;<br>
     * &lt;/root&gt;</CODE>
     * <p>
     * the String shall look like:
     * <p>
     * <CODE>String requestString = "test,name" ;</CODE>
     * <p>
     * The <B>root element</B> of the xml-file <B>must not</B> be put into the
     * requestString. The first element in the requestString is the
     * element under the root element.
     *     * @param requestArray Array of strings that defines the TEXT_NODE to be retrieved.
     *     * @return array of int. [0]: ID; [1]: Number of found elements
     *     * @throws RemoteException throws RemoteException
     * @param requestString Strings that defines the TEXT_NODE to be retrieved; hierarchichal nodes are
     * specified by a comma-seperated list
     * @return a Vector consisting of the NodeLists
     * @throws SAXException
     */
    public abstract Vector getNodeListsByName( String requestString ) throws org.xml.sax.SAXException ;
    
    /**
     * The method <B>getElementsByName</B> requests the <B>String-Elements</B> under <B>TEXT_NODES</B>
     * stored in an <B>internal element Vector</B> produced by <B>getNodeListsByName</B>. The
     * function is similar to the method <B>getElementsByName</B> except that the
     * requested NodeList is given to this method.
     * @param requestString String specifying the Node to be retrieved
     * @param requestList the NodeList handed over to the method to be searched for the requestString
     * @return a Vector consisting of the values (TEXT_NODES) retrieved
     * @throws SAXException
     */
    public abstract Vector getElementsByName( String requestString, NodeList requestList ) throws org.xml.sax.SAXException ;
    
    /**
     * The method <B>getNodeListsByName</B> requests the <B>NodeLists</B> under
     * <B>requestList</B>. The function is similar to the method
     * <B>getElementsByName</B> except that NodeList to be searched has to given to
     * the method.
     * @param requestString String specifying the Node to be retrieved
     * @param requestList the NodeList handed over to the method to be searched for the requestArray
     * @return a Vector consisting of NodeLists
     * @throws SAXException
     */
    public Vector getNodeListsByName ( String requestString , NodeList requestList ) throws org.xml.sax.SAXException ;
    
    /**
     * Returns the root element of the DOM-tree.
     * @return Root element
     * @throws SAXException
     */
    public Element getInitElement() throws org.xml.sax.SAXException ;
    
    /**
     * Create a DOM element for the instantiated DOM-tree.
     * @param name Name of the element to be created
     * @return Element for the DOM-tree
     * @throws SAXException
     */  
    public Element createElement( String name ) throws org.xml.sax.SAXException ;
    
    /**
     * Create a text-node for the instantiated DOM-tree.
     * @param name Name of the text-node to be created
     * @return the text node for the DOM-tree is returned
     * @throws SAXException throws an exception if SAX is failing
     */
    public Text createTextNode( String name ) throws org.xml.sax.SAXException ;

}
