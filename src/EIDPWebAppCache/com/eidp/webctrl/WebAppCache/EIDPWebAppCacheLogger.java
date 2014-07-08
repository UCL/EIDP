/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.webctrl.WebAppCache;

import com.eidp.logger.Logger;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 *
 * @author david
 */
public class EIDPWebAppCacheLogger {

    private static Logger logger;
    private String ctx = "";
    
    @AroundInvoke
    public Object createLogger(InvocationContext invocationContext) throws Exception {
        Object[] parameters = invocationContext.getParameters();
        if (null != parameters && parameters.length == 1) {
            ctx = (String) parameters[0];
        }
        if (!ctx.equals("")) {
            EIDPWebAppCacheLogger.logger = new Logger( "/com/eidp/" + ctx + "/WebAppCache.log" );
            EIDPWebAppCacheLogger.logger.logMessage( "Initializing WebAppCacheLog with Context: " + ctx + "." );
        }
        return invocationContext.proceed();
    }
    
    
    
    @PrePassivate
    @PreDestroy
    public Object closeLogger(InvocationContext invocationContext) throws Exception {
        logger.close();
        return invocationContext.proceed();
    }

}
