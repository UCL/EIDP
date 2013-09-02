/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.Generator;

import com.eidp.xml.XMLDataAccess;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author david
 */
class ReportContentReplace {
    
    Map<String, String> caseMap = new HashMap<String, String>();
    String defaultValue = "";

    ReportContentReplace(String rep, Vector repDef) {
        try {
            XMLDataAccess xda = new XMLDataAccess();
            for (int i = 0; i < repDef.size(); i++) {
                NodeList repList = (NodeList) repDef.get(i);
                String name = (String) ((Vector) xda.getElementsByName("name",
                        repList)).get(0);
                if (name.equals(rep)) {                    
                    defaultValue = (String)((Vector)xda.getElementsByName("default-value", 
                            repList)).get(0);
                    Vector cases = (Vector) xda.getNodeListsByName("case", 
                            repList);
                    for (int j = 0; j < cases.size(); j++) {
                        String ifValue = (String) 
                                ((Vector) xda.getElementsByName("if", 
                                (NodeList) cases.get(j))).get(0);
                        String returnValue = (String) 
                                ((Vector) xda.getElementsByName("return", 
                                (NodeList) cases.get(j))).get(0);
                        caseMap.put(ifValue, returnValue);
                    }
                    break;
                }
            }
        } catch (SAXException ex) {
            Logger log = Logger.getLogger(ReportContentReplace.class.getName());
            log.log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger log = Logger.getLogger(ReportContentReplace.class.getName());
            log.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger log = Logger.getLogger(ReportContentReplace.class.getName());
            log.log(Level.SEVERE, null, ex);
        }
    }

    String processReplace(String cont) {
        if (!caseMap.isEmpty()) {
            return caseMap.get(cont);
        } else {
            return defaultValue;
        }
    }

}
