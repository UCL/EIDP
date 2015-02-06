/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.webctrl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author brit.sysman
 */
public class XMLDispatcherNGTest {
    
    public XMLDispatcherNGTest() {
    }

    @Test
    public void createListSelectURLEncode() throws UnsupportedEncodingException {
        String setPrimaryFieldValue = "Primary Barret's oesophagus > 2cm & <10cm" ;
        String expectedPrimary = "Primary Barret&#39;s oesophagus &#62; 2cm &#38; &#60;10cm";
        Assert.assertEquals(expectedPrimary, encodeHTML(setPrimaryFieldValue));
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
    
    private static String encodeHTML(String s) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '\'' || c == '&') {
                out.append("&#" + (int) c + ";");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
