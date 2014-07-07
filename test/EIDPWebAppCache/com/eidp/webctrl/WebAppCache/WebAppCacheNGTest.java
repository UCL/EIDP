/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.webctrl.WebAppCache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author david
 */
public class WebAppCacheNGTest {
    
    private static EJBContainer ejbContainer;
    private static Context ctx;
    private final String webAppCacheJndi = "java:global/EIDPWebAppCache/EIDPWebAppCache!com.eidp.webctrl.WebAppCache.EIDPWebAppCache";
    
    @BeforeClass
    public static void setupClass() throws Exception {
        // Instantiate an embeddable EJB container and search the 
        // JVM classpath for eligible EJB modules
        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, new File("dist/EIDPWebAppCache.jar"));
        ejbContainer = javax.ejb.embeddable.EJBContainer.createEJBContainer(p);
        ctx = ejbContainer.getContext();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        // Shutdown the embeddable container
        ejbContainer.close();
    }
    
    @Test
    public void callEIDPWebAppCache() throws Exception {
        // Retrieve a reference  to the session bean using a portable
        // global JNDI name
        EIDPWebAppCache eidpWebAppCache = (EIDPWebAppCache) ctx.lookup(webAppCacheJndi);
        assertNotNull(eidpWebAppCache);
        String expected = "ucl-brit";
        eidpWebAppCache.setApplicationContext(expected);
        assertEquals(eidpWebAppCache.getApplicationContext(),expected);
    }
    
}
