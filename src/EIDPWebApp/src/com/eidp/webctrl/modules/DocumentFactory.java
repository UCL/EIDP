/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.webctrl.modules;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author brit.sysman
 */
public class DocumentFactory {
    
    private DocumentFactory() {
        
    }
    
    public static Document getLetterHandler(String className) {
        Class<Document> cl = null;        
        try {
            cl = (Class<Document>) Class.forName(className);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DocumentFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document letter = null;        
        if (null != cl) {
            try {
                letter = (Document) cl.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(DocumentFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return letter;
    }
    
}
