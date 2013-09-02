/*
 * ReportDOMFunctions.java
 *
 * Created on January 19, 2005, 8:58 AM
 */

package com.eidp.webctrl.modules.Report;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.eidp.xml.XMLDataAccess;

/**
 *
 * @author  rusch
 */
public class ReportDOMFunctions {
    
    /** Creates a new instance of ReportDOMFunctions */
    public ReportDOMFunctions() {
    }
    
    public Element createColumndefinition( String width, String attribute, XMLDataAccess xmld )throws java.rmi.RemoteException, java.io.IOException , org.xml.sax.SAXException{
        Element item,item1;
        item = xmld.createElement("column-def");
        item1 = xmld.createElement("column-attribute");
        item1.appendChild(xmld.createTextNode(String.valueOf(attribute)));
        item.appendChild(item1);
        item1 = xmld.createElement("width");
        item1.appendChild(xmld.createTextNode(String.valueOf(width)));
        item.appendChild(item1);
        return item;
    }
    
    public Element createColumnValue( String align, String attribute, String fontsize, String value, XMLDataAccess xmld )throws java.rmi.RemoteException, java.io.IOException , org.xml.sax.SAXException{
        Element item,item1,item2;
        item = xmld.createElement("column");
        item1 = xmld.createElement("column-align");
        item1.appendChild(xmld.createTextNode(align));
        item.appendChild(item1);
        item1 = xmld.createElement("contents");
        item.appendChild(item1);
        if( !attribute.trim().equals("") ){
            item2 = xmld.createElement("contents-attribute");
            item2.appendChild(xmld.createTextNode(attribute));
            item1.appendChild(item2);
        }
        if( !fontsize.trim().equals("") ){
            item2 = xmld.createElement("font-size");
            item2.appendChild(xmld.createTextNode(fontsize));
            item1.appendChild(item2);
        }
        item2 = xmld.createElement("text");
        item2.appendChild(xmld.createTextNode(value));
        item1.appendChild(item2);
        item.appendChild(item1);
        return item;
    }
    
    public NodeList createTableHeaderSectionNode( String strTableHeader , XMLDataAccess xmld ) throws org.xml.sax.SAXException{
        NodeList tableHeaderSectionNode;
        Element root,item,item2;
        root = xmld.createElement("section");
        item = xmld.createElement("section-type");
        item.appendChild(xmld.createTextNode("text"));
        root.appendChild(item);
        item = xmld.createElement("contents");
        item2 = xmld.createElement("contents-attribute");
        item2.appendChild(xmld.createTextNode("bold"));
        item.appendChild(item2);
        item2 = xmld.createElement("text");
        item2.appendChild(xmld.createTextNode(strTableHeader));
        item.appendChild(item2);
        root.appendChild(item);
        tableHeaderSectionNode = (NodeList)root;
        return tableHeaderSectionNode ;
    }
    
    public NodeList createEmptySectionNode( XMLDataAccess xmld ) throws org.xml.sax.SAXException{
        NodeList returnEmptyNode;
        Element root,item,item2;
        root = xmld.createElement("section");
        item = xmld.createElement("section-type");
        item.appendChild(xmld.createTextNode("text"));
        root.appendChild(item);
        item = xmld.createElement("contents");
        item2 = xmld.createElement("text");
        item2.appendChild(xmld.createTextNode(" "));
        item.appendChild(item2);
        root.appendChild(item);
        returnEmptyNode = (NodeList)root;
        return returnEmptyNode ;
    }
    
    
}
