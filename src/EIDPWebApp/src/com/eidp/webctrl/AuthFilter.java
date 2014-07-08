/**	
 *	
 * @author  veit	
 * @version	
 */	
	
/*
 * authfilter.java
 *
 * Created on July 18, 2003, 2:15 PM
 */

package com.eidp.webctrl;

import com.eidp.core.DB.DBMapping;
import com.eidp.webctrl.WebAppCache.EIDPWebAppCache;
import com.eidp.logger.Logger;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext ;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.util.Set ;

/**
 * Authentication Filter for Single-Sign-On functionality.
 * The Authentication Filter performs username/password Authentication
 * and sets the authorization data to EIDPWebAppCache.
 * Authorization data are: user-roles, center-roles. The AuthFilter
 * further instantiates the DBMapper and EIDPWebAppCache and stores
 * the Handles in session variables.
 * AuthFilter needs special structures in the contexts db.xml to work.
 * For further information see the Reference Manual of the Enterprise
 * Integration and Development Platform.
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

public class AuthFilter implements Filter {
    
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured.
    private FilterConfig filterConfig = null;
    
    private String loginConfig = "EIDPLoginConfig" ;
    
    private Logger logger ;
    private int logLevel = 9 ;
    private boolean hasTooManyLoginTrys = false;
    private boolean hasLoginError = false;
    
    /**
     *
     * @param request The servlet request we are processing
     * @param result The servlet response we are creating
     * @param chain The filter chain we are processing
     * @exception IOException
     * @exception ServletException
     */
    
    public void init(FilterConfig config) throws javax.servlet.ServletException {
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException, javax.servlet.ServletException {
        HttpSession session;
        boolean initSession = false ;
        UserScopeObject uso = new UserScopeObject() ;
        boolean sessionUpdate = true ;
        this.logger = new Logger( "/com/eidp/AuthFilter.log" ) ;
        // 0. init Session
        session = ((HttpServletRequest)request).getSession() ;
        if ( request.getParameter( "applicationContext" ) != null ) {
            // kill all SessionData if an applicationContext is manually given:
            this.logger.logMessage( "   --> applicationContext manually given. Kill all SessionData..." ) ;
            this.sessionInvalidate( session , uso ) ;
            initSession = true ;
        }
        if ( session.isNew() ) {
            initSession = true ;
        }
        // 1. check if Session exists (isNew)
        if ( initSession == true ) {
            this.killAllSessionData( session , uso ) ;
            uso.initializeApplication = true ;
            this.logger.logMessage( "Session is new (no session was available)." ) ;
            // get applicationContext:
            this.logger.logMessage( "   --> applicationContext set by request: " + request.getParameter( "applicationContext" ) ) ;
            uso.applicationContext = request.getParameter( "applicationContext" ) ;
            // Session was not set
            // get request Data and try to start a new Session
            if ( request.getParameter( "eidp_login" ) != null ) {
                try {
                    uso = this.setSession( request , response , session , uso ) ;
                } catch (java.lang.Exception e) {
                    throw new javax.servlet.ServletException(e.getMessage());
                }
                if ( uso.checkAuth == false ) {
                    this.logger.logMessage( "--> Session couldn't be set" ) ;
                    this.sessionInvalidate( session , uso ) ;
                    //session.invalidate() ;
                    sessionUpdate = false ;
                } else sessionUpdate = true ;
            } else {
                // no login given, sessionUpdate = false, but applicationContext must be set:
                if ( this.logLevel > 5 ) this.logger.logMessage( "checking for applicationContext" ) ;
                if ( request.getParameter( "applicationContext" ) != null ) {
                    sessionUpdate = false ;
                } else {
                    this.sessionInvalidate( session ,  uso ) ;
                    //session.invalidate() ;
                    sessionUpdate = false ;
                }
            }
            try {
                Properties cacheProp = new Properties() ;
//                cacheProp.setProperty( "org.omg.CORBA.ORBInitialHost" , "localhost" ) ;
//                cacheProp.setProperty( "org.omg.CORBA.ORBInitialPort" , "33365" ) ;
                Context cacheJndiContext = new InitialContext( cacheProp );
                EIDPWebAppCache cacheRef = (EIDPWebAppCache) cacheJndiContext.lookup("EIDPWebAppCache");
                cacheRef.setApplicationContext(uso.applicationContext);
            } catch ( javax.naming.NamingException ne ) {
                throw new javax.servlet.ServletException( "AuthFilter throws NamingException when calling EIDPWebAppCache: " + ne ) ;
            }  
            session.setAttribute( "eidpWebAppCacheHandle" , uso.eidpWebAppCache ) ;
        } else {
            uso.eidpWebAppCache = (EIDPWebAppCache) session.getAttribute( "eidpWebAppCacheHandle" ) ;
            
            // SESSION EXISTS!
            this.logger.logMessage( "Existing Session was available." ) ;
            // get applicationContext
            try {
                if ( this.logLevel > 5 ) this.logger.logMessage( "checking in Session for applicationContext" ) ;
                uso.applicationContext = (String)uso.eidpWebAppCache.sessionData_get( "applicationContext" ) ;
            } catch ( java.lang.NullPointerException ne ) {
                if ( this.logLevel > 5 ) this.logger.logMessage( "there was no applicationContext in Session. Getting it from request" ) ;
                uso.applicationContext = (String)request.getParameter( "applicationContext" ) ;
            }
            if ( request.getParameter( "eidp_login" ) != null ) {
                this.logger.logMessage( "--> !!! request contains login !!!" ) ;
                // a login has been initialized, but the session exists.
                // now we have to change the existing session (renew it!).
                // this is completely the same procedure as initialized if
                // no session was available ( the session was new! ).
                this.logger.logMessage( "--> !!! invalidate Session !!! ") ;
                this.sessionInvalidate( session , uso ) ;
                //session.invalidate() ;
                this.logger.logMessage( "--> set a new Session" ) ;
                this.logger.logMessage( "--> first init new Session, then call setSession" ) ;
                // session = ((HttpServletRequest)request).getSession() ;
                try {
                uso = this.setSession( request , response , session , uso ) ;
                } catch (java.lang.Exception e) {
                    throw new javax.servlet.ServletException(e.getMessage());
                }
                if ( uso.checkAuth == false ) {
                    this.logger.logMessage( "--> !!! couldn't set a new Session !!!" ) ;
                    sessionUpdate = false ;
                }
            }
            if ( sessionUpdate == true ) {
                this.logger.logMessage( "--> Checking data structure..." ) ;
                if ( ! uso.eidpWebAppCache.sessionData_exists() ) {
                    if ( this.logLevel > 5 ) this.logger.logMessage( "--> sessionData exists" ) ;
                    if ( uso.eidpWebAppCache.sessionData_containsKey( "userLogin" ) && uso.eidpWebAppCache.sessionData_containsKey( "userID" ) ) {
                        if ( this.logLevel > 5 ) this.logger.logMessage( "--> Data structure OK." ) ;
                        sessionUpdate = true ;
                    } else sessionUpdate = false ;
                } else sessionUpdate = false ;
                this.logger.logMessage( "--> Data is being read in." ) ;
                // 1. move requests into sessionData (module , primaryApplicationKey , secondaryApplicationKey
                this.logger.logMessage( "--> temporary session data is being read in." ) ;
                if ( request.getParameter( "module" ) != null ) {
                    if ( ((String)request.getParameter( "module" )).equals( "Function;logout;show" ) ) {
                        this.logger.logMessage( "<-- " + uso.eidpWebAppCache.sessionData_get("userLogin") + " has logged out !" ) ;
                        this.sessionInvalidate( session , uso ) ;
                        //session.invalidate() ;
                        sessionUpdate = false ;
                    } else if ( ((String)request.getParameter( "module" )).equals( "PlugIn" ) ) {
                        String[] pluginArray = ((String)request.getParameter( "PlugIn" )).split( ";" ) ;
                        uso.eidpWebAppCache.sessionData_set( "module" , pluginArray[0] ) ;
                        uso.eidpWebAppCache.sessionData_set( "moduleParameter" , pluginArray[1] ) ;
                        uso.eidpWebAppCache.sessionData_set( "moduleAction" , pluginArray[2] ) ;
                        uso.eidpWebAppCache.sessionData_set( "xmlFile" , "controller" ) ;
                    } else {
                        String [] moduleArray = ((String)request.getParameter( "module" )).split( ";" ) ;
                        // set report-html-urls
                        if ( moduleArray[0].equals("Report") ) {	
                            uso.eidpWebAppCache.sessionData_set( "waiturl" , (String)request.getParameter( "waiturl" ) ) ;	
                            uso.eidpWebAppCache.sessionData_set( "errorurl" , (String)request.getParameter( "errorurl" ) ) ;	
                        }
                        if ( moduleArray.length == 3 ) {
                            uso.eidpWebAppCache.sessionData_set( "module" , moduleArray[0] ) ;
                            uso.eidpWebAppCache.sessionData_set( "moduleParameter" , moduleArray[1] ) ;
                            String moduleAction = moduleArray[2] ;
                            String[] ma = moduleAction.split( "#" ) ;
                            moduleAction = ma[0] ;
                            uso.eidpWebAppCache.sessionData_set( "moduleAction" , moduleAction ) ;
                            uso.eidpWebAppCache.sessionData_set( "xmlFile" , "controller" ) ;
                            // clear Group-ID
                            if ( !uso.eidpWebAppCache.sessionData_get("module").equals("Function") && !uso.eidpWebAppCache.sessionData_get("moduleParameter").equals("SidePanel") ) {
                                uso.eidpWebAppCache.sessionData_set( "groupID" , "" ) ;
                            }
                        } else if ( moduleArray.length == 5 ) {
                            uso.eidpWebAppCache.sessionData_set( "module" , moduleArray[0] ) ;
                            uso.eidpWebAppCache.sessionData_set( "xmlFile" , moduleArray[1] ) ;
                            uso.eidpWebAppCache.sessionData_set( "moduleParameter" , moduleArray[2] ) ;
                            // set Group-ID
                            uso.eidpWebAppCache.sessionData_set( "groupID" , moduleArray[3] ) ;
                            String moduleAction = moduleArray[4] ;
                            String[] ma = moduleAction.split( "#" ) ;
                            moduleAction = ma[0] ;
                            uso.eidpWebAppCache.sessionData_set( "moduleAction" , moduleAction ) ;
                        } else {
                            uso.eidpWebAppCache.sessionData_set( "module" , moduleArray[0] ) ;
                            uso.eidpWebAppCache.sessionData_set( "xmlFile" , moduleArray[1] ) ;
                            uso.eidpWebAppCache.sessionData_set( "moduleParameter" , moduleArray[2] ) ;
                            String moduleAction = moduleArray[3] ;
                            String[] ma = moduleAction.split( "#" ) ;
                            moduleAction = ma[0] ;
                            uso.eidpWebAppCache.sessionData_set( "moduleAction" , moduleAction ) ;
//                            System.out.println("moduleArray.length == else - moduleParameter " + uso.eidpWebAppCache.sessionData_get("moduleParameter"));
                            // clear Group-ID
                            uso.eidpWebAppCache.sessionData_set( "groupID" , "" ) ;
                        }
                    }
                } else {
                    uso.eidpWebAppCache.sessionData_set( "module" , "Function" ) ;
                    uso.eidpWebAppCache.sessionData_set( "moduleParameter" , "init" ) ;
                    uso.eidpWebAppCache.sessionData_set( "moduleAction" , "" ) ;
                    uso.eidpWebAppCache.sessionData_set( "xmlFile" , "controller" ) ;
                }
                if ( request.getParameter( "primaryApplicationKey" ) != null ) { uso.eidpWebAppCache.sessionData_set( "primaryApplicationKey" , request.getParameter( "primaryApplicationKey" ) ) ; }
                if ( request.getParameter( "secondaryApplicationKey" ) != null ) { uso.eidpWebAppCache.sessionData_set( "secondaryApplicationKey" , request.getParameter( "secondaryApplicationKey" ) ) ; }
            } else {
                this.logger.logMessage( "<-- " + request.getParameter( "eidp_login" ) + " session timeout." ) ;
                // the session is not valid!
                sessionUpdate = false ;
            }
        }
        // Session is OK or has been successfully initialized.
        // UPDATE Session:
        if ( sessionUpdate == true ) {
            this.logger.logMessage( "-------> UPDATE SESSION <---------" ) ;
            if ( this.logLevel > 5 ) {
                this.logger.logMessage( "--> module: " + uso.eidpWebAppCache.sessionData_get( "module" ) ) ;
                this.logger.logMessage( "--> moduleParameter: " + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ) ;
                this.logger.logMessage( "--> moduleAction: " + uso.eidpWebAppCache.sessionData_get( "moduleAction" ) ) ;
                this.logger.logMessage( "--> xmlFile: " + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) ) ;
            }
            if ( this.logLevel > 5 ) { this.logger.logMessage( "... Checking for Secondary Key Change ... " ) ; }
            if ( request.getParameter( "SecondaryKey" ) != null ) {
                uso.eidpWebAppCache.sessionData_set( (String)uso.eidpWebAppCache.sessionData_get( "SecondaryKeySessionRef" ) , request.getParameter( "SecondaryKey" ) ) ;
                if ( this.logLevel > 5 ) { this.logger.logMessage( "... Secondary Key has changed to " + request.getParameter( "SecondaryKey" ) + " on sessionData." + uso.eidpWebAppCache.sessionData_get( "SecondaryKeySessionRef" ) + " ... " ) ; }
            }
            if ( request.getParameter( "userObject" ) != null ) {
                if ( this.logLevel > 5 ) { this.logger.logMessage( ">>> userObject set." ) ; } ;
                uso.eidpWebAppCache.sessionData_set( "userObject" , request.getParameter( "userObject" ) ) ;
            }
            this.logger.logMessage( "--------> Check uso: " ) ;
            this.logger.logMessage( "--------> appContext: " + uso.applicationContext ) ;
            uso.eidpWebAppCache.sessionData_set( "applicationContext" , uso.applicationContext ) ;
            this.logger.logMessage( "***** Session: ***** " ) ;
            this.logger.logMessage( session.toString() ) ;
            this.logger.logMessage( "******************** " ) ;
        } else {
            if ( this.logLevel > 7 ) this.logger.logMessage( "setting Session only for applicationContext: " + uso.applicationContext + "." ) ;
            // session = ((HttpServletRequest)request).getSession() ;
            String strMessage = "";
            if ( this.hasTooManyLoginTrys ) {
                strMessage = "?tooManyTrysMessage=true";
            }
            if ( this.hasLoginError ) {
                strMessage = "?hasLoginError=true";
            }
            uso.eidpWebAppCache.sessionData_clear() ;
            uso.eidpWebAppCache.userRoles_clear() ;
            uso.eidpWebAppCache.centerRoles_clear() ;
            uso.eidpWebAppCache.sessionRef_clear() ;
            uso.eidpWebAppCache.sidePanelEntry_set( "" ) ;
            uso.eidpWebAppCache.sessionData_set( "applicationContext" , uso.applicationContext ) ;
            this.logger.logMessage( "--> Request will be dispatched to /servlet/com.eidp.webctrl.Login" + strMessage ) ;
            RequestDispatcher requestDispatcher = request.getRequestDispatcher( "/servlet/com.eidp.webctrl.Login" + strMessage ) ;
            requestDispatcher.forward( request , response ) ;
        }
        this.hasTooManyLoginTrys = false;
        this.hasLoginError = false;
        chain.doFilter( request , response ) ;
    }
    
    private UserScopeObject setSession( ServletRequest request , ServletResponse response , HttpSession session , UserScopeObject uso ) throws Exception, java.io.IOException, javax.servlet.ServletException {
        // Login/PWD Check
        // get Session data and store it in this.sessionData
        // return true if Session has been successfully initialized
        // return false else.
        if ( this.logLevel > 5 ) {
            this.logger.logMessage( "   --- setSession ---" ) ;
            if ( request.getParameter( "eidp_login" ) == null ) {
                uso.checkAuth = false ;
                return uso ;
            }
            this.logger.logMessage( "   --> Login: " + request.getParameter( "eidp_login" ) ) ;
            this.logger.logMessage( "   --> applicationContext: " + uso.applicationContext ) ;
        }
        // login request exists.
        // get applicationContext from Login-Request
        if ( uso.applicationContext == null ) {
            uso.checkAuth = false ;
            return uso ;
        }
        uso = this.comparePasswordsAndSetSession( (String)request.getParameter("eidp_login") , (String)request.getParameter("eidp_password") , session , uso ) ;
        // Session will be set in comparePasswordsAndSetSession because then we only need one
        // connection to the EJB-Container (dbBean is instantiated in comparePasswordsAndSetSession).
        return uso ;
    }
    
    // compare Passwords and set Session vars
    private UserScopeObject comparePasswordsAndSetSession( String eidpLogin , String eidpPassword , HttpSession session , UserScopeObject uso ) throws Exception, java.io.IOException, javax.servlet.ServletException {
        DBMapping dbMapper = null;
        HashMap authData;
        try {
            if ( this.logLevel > 5 ) this.logger.logMessage( "!!! comparePasswordsAndSetSession called with:" ) ;
            if ( this.logLevel > 5 ) this.logger.logMessage( "!!! login: " + eidpLogin ) ;
            // Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() ) ;
            // With the dbMapper Attribute in the Session we are inside an ApplicationContext.
            // We !must! remove all contexts in case of incorrect login.
            if ( uso.initializeApplication == true ) {
                Properties prop = new Properties() ;
//                prop.setProperty( "org.omg.CORBA.ORBInitialHost" , "localhost" ) ;
//                prop.setProperty( "org.omg.CORBA.ORBInitialPort" , "3700" ) ;
                Context jndiContext = new InitialContext( prop ) ;
                dbMapper = (DBMapping) jndiContext.lookup("DBMapping");
                dbMapper.setApplicationContext(uso.applicationContext);
            }
            authData = dbMapper.Authenticate(eidpLogin, eidpPassword);
            
            String loginTooOften = (String)authData.get("loginTooOften");
            hasTooManyLoginTrys = loginTooOften != null && loginTooOften.equals("true");
            String loginError = (String)authData.get("loginPassError");
            hasLoginError = loginError != null && loginError.equals("true");
            
            if ( dbMapper.isAuthenticated() ) {
                uso.eidpWebAppCache.sessionData_set( "userLogin" , authData.get( "userLogin" ) ) ;
                uso.eidpWebAppCache.sessionData_set( "userID" , authData.get( "userID" ) ) ;
                uso.eidpWebAppCache.sessionData_set( "passwordExpired" , authData.get( "passwordExpired" ) ) ;
                // roles
                Vector userRoles = new Vector() ;
                userRoles = (Vector)authData.get( "userRoles" ) ;
                Iterator ur = userRoles.iterator() ;
                while ( ur.hasNext() ) {
                    String uRole = (String)ur.next() ;
                    uso.eidpWebAppCache.userRoles_set( uRole ) ;
                    
                }
                // 3. get Center Data
                HashMap centerRoles = new HashMap() ;
                centerRoles = (HashMap)authData.get( "userCenters" ) ;
                Set ck = centerRoles.keySet() ;
                Iterator ci = ck.iterator() ;
                while ( ci.hasNext() ) {
                    String cKey = (String)ci.next() ;
                    uso.eidpWebAppCache.centerRoles_set( cKey , centerRoles.get( cKey ) ) ;
                }
                uso.eidpWebAppCache.sessionData_set( "userCenter" , authData.get( "userCenter" ) ) ;
                uso.checkAuth = true ;
                session.setAttribute( "dbMapperHandle" , dbMapper ) ;
            } else {
                uso.checkAuth = false ;
                    dbMapper.remove() ;
                
            }
            return uso ;
        } catch ( javax.naming.NamingException ne ) {
            throw new javax.servlet.ServletException( "Naming Exception by AuthFilter: " + ne ) ;
        } catch ( java.rmi.RemoteException re ) {
            throw new javax.servlet.ServletException( "RemoteException by AuthFilter: " + re ) ;
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "IOException by AuthFilter: " + ioe ) ;
        }
    }
    
    private static byte[] encrypt(String inputString) throws java.security.DigestException , java.security.InvalidAlgorithmParameterException , java.security.NoSuchAlgorithmException , java.io.IOException, javax.servlet.ServletException {
        java.security.MessageDigest md = null;
        md = java.security.MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(inputString.getBytes( "ISO-8859-1" ));
        return  md.digest();
    }
    
    private void sessionInvalidate( HttpSession session , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException {
        // since all session data is stored in eidpWebAppCache,
        // it is not necessary to kill the session at all if the
        // dbMapper Object is removed.
        // The EIDPSessionListener can then kill all objects (dbMapper, eidpWebAppCache)
        // if the session is invalidated by other processes.
        // session.invalidate() ;
        try {
            uso.eidpWebAppCache.sessionData_clear() ;
            uso.eidpWebAppCache.userRoles_clear() ;
            uso.eidpWebAppCache.centerRoles_clear() ;
            uso.eidpWebAppCache.sessionRef_clear() ;
            uso.eidpWebAppCache.sidePanelEntry_set( "" ) ;
            uso.eidpWebAppCache.sessionData_set( "applicationContext" , uso.applicationContext ) ;
        } catch ( java.lang.NullPointerException ne ) {
        }
    }
    
    private void killAllSessionData( HttpSession session , UserScopeObject uso ) {
        ((DBMapping) session.getAttribute("dbMapperHandle")).remove();
        ((EIDPWebAppCache) session.getAttribute("eidpWebAppCacheHandle")).remove();   
        session.removeAttribute("eidpWebAppCacheHandle");
        session.removeAttribute( "dbMapperHandle" ) ;
    }
    
    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }
    
    
    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
    
    /**
     * Destroy method for this filter
     *
     */
    public void destroy() {
    }
    
    
    private class UserScopeObject {
        public ServletContext context;
        public String applicationContext ;
        public DBMapping dbMapper ;
        public EIDPWebAppCache eidpWebAppCache ;
        public boolean checkAuth = false ;
        
        public boolean initializeApplication = false ;
        
        public UserScopeObject() {
        }
    }
    
    public class UsernamePasswordHandler implements CallbackHandler {
        
        private String username = "webctrl";
        private String password = "webctrl";
        
        public void handle(Callback[] callbacks) throws javax.security.auth.callback.UnsupportedCallbackException {
            try {
                for (int i = 0; i <callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        NameCallback nc = (NameCallback)callbacks[i];
                        nc.setName(username);
                    } else if(callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback pc = (PasswordCallback)callbacks[i];
                        pc.setPassword(password.toCharArray());
                    }
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
