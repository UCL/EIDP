/*
 * GeneratorAPI.java
 */

package com.eidp.Generator;

import com.eidp.xml.XMLDataAccess ;
import org.w3c.dom.Element ;
import java.util.HashMap ;
import java.util.Vector ;

/**
 * The EIDP Generator API is the standard interface for
 * building Generators for the Enterprise Integration
 * and Development Platform. Generators are to be plugged in into
 * the reporting mechanism of the EIDPWebApp.
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
public interface GeneratorAPI {
    /**
     * returns the generated document.
     * @return return the generated document.
     */
    public Object getGeneratedDocument() ;
    
    /**
     * retrieves the root element of the XML document to be processed.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @return the root element of the xml document to be processed.
     */
    public Element getInitElement() throws javax.xml.parsers.ParserConfigurationException , org.xml.sax.SAXException , java.io.IOException ;
    
    /**
     * Retrieve the xml document to be processed.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @return the xml document as String.
     */
    public String getXMLDocument() throws org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException ;
    
}
