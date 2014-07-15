/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.core.DB.modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author david
 */
public class DataSourceDispatcherNGTest {
    
    public DataSourceDispatcherNGTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void readJndiParameters() throws IOException, NamingException {
         String id = "Phenotype";
         String initialHostProp = "org.omg.CORBA.ORBInitialHost";
         String initialPortProp = "org.omg.CORBA.ORBInitialPort";
         String propertiesFile = id.toLowerCase() + ".properties";
         InputStream propsInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile);
         Properties p = new Properties();
         p.load(propsInputStream);
         Context ctx;
         if (null != propsInputStream) {
            ctx = new InitialContext(p);
         } else {
            ctx = new InitialContext();
         }
         Map<?, ?> m = ctx.getEnvironment();
         Assert.assertTrue(m.containsKey(initialHostProp), "Assert org.omg.CORBA.ORBInitialHost in property file");
         Assert.assertTrue(m.containsKey(initialPortProp), "Assert org.omg.CORBA.ORBInitialPort in property file");
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
