/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.core.DB.modules;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author brit.sysman
 */
public class DatabaseMappingTestNGTest {
    
    public DatabaseMappingTestNGTest() {
    }

    @Test
    public static void testRaw() throws ParserConfigurationException, SAXException, IOException {
        InputStream xmlInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db-for.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlInputStream);
        String field = doc.getElementsByTagName("field").item(0).getTextContent();
        String operator = doc.getElementsByTagName("operator").item(0).getTextContent();
        String rawOperator = doc.getElementsByTagName("raw").item(0).getTextContent();
        String forValue = "'david'";     
        String rawStatement = "";
        if (operator.equals("raw")) {
            rawStatement = field + " " + rawOperator + " " + forValue;
        }
        Assert.assertEquals(rawStatement, "login similar to 'david'");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
}
