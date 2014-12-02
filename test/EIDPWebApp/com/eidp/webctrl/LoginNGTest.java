/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.webctrl;

import javax.servlet.http.HttpServletRequest;
import org.mockito.Mockito;
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
public class LoginNGTest {
    
    public LoginNGTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void createReloginURL() {
     HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
     Mockito.when(mockedRequest.getContextPath()).thenReturn("/EIDPWebApp");
     Mockito.when(mockedRequest.getServletPath()).thenReturn("/servlet/com.eidp.webctrl.Controller");
     String appURL = mockedRequest.getContextPath() + mockedRequest.getServletPath() +
                "?applicationContext=ucl-brit" ;
     System.out.println(appURL);
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
