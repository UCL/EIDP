package com.eidp.webctrl.WebAppCache;

import com.eidp.logger.Logger;
import java.io.IOException ;
import java.util.HashMap;
import java.util.Vector;
import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Stateful;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * EIDPWebAppCache.
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

@Stateful
@LocalBean
public class EIDPWebAppCache implements HttpSessionListener {
    
    private javax.ejb.SessionContext context;
    private transient Logger logger;
    private HashMap sessionData = new HashMap() ;
    private final Vector userRoles = new Vector() ;
    private HashMap centerRoles = new HashMap() ;
    private final HashMap sessionRefs = new HashMap() ;
    private String sidePanelEntry = "" ;
    private String applicationContext = "" ;

    public EIDPWebAppCache() {
    }
    
    public EIDPWebAppCache(String applicationname) {
        this.applicationContext = applicationname ;
        try {
            this.logger = new Logger( "/com/eidp/" + applicationname + "/WebAppCache.log" ) ;
            this.logger.logMessage( "Initializing WebAppCacheLog with Context: " + applicationname + "." ) ;
        } catch (IOException e) {
            throw new EJBException("[EIDPWebAppCache] : Cannot create instance of EIDPWebAppCache");
        }
    }
    
    public void sessionRef_set( String sessionKey , Object object ) {
        this.sessionRefs.put( sessionKey , object ) ;
    }

    public Object sessionRef_get( String sessionKey ) {
        return this.sessionRefs.get( sessionKey ) ;
    }
    
    public void sessionRef_remove( String sessionKey ) {
        this.sessionRefs.remove( sessionKey ) ;
    }
    
    public void sessionRef_clear() {
        this.sessionRefs.clear() ;
    }
    
    public void sessionData_set( String sessionDataKey , Object object ) {
        this.sessionData.put( sessionDataKey , object ) ;
    }
    
    public void sessionData_setAll( HashMap sessionDataAll ) {
        this.sessionData = sessionDataAll ;
    }
    
    public Object sessionData_get( String sessionDataKey ) {
        return this.sessionData.get( sessionDataKey ) ;
    }
    
    public void sessionData_remove( String sessionDataKey ) {
        this.sessionData.remove( sessionDataKey ) ;
    }
    
    public boolean sessionData_containsKey( String sessionDataKey ) {
        return this.sessionData.containsKey( sessionDataKey ) ;
    }
    
    public boolean sessionData_exists() {
        return !this.sessionData.isEmpty() && this.sessionData == null;
    }
    
    public void sessionData_clear() {
        this.sessionData.clear() ;
    }
    
    public void userRoles_set( Object object ) {
        this.userRoles.addElement( object ) ;
    }

    public Object userRoles_get( int index ) {
        return this.userRoles.get( index ) ;
    }

    public int userRoles_size() {
        return this.userRoles.size() ;
    }
    
    public boolean userRoles_exists() {
        return this.userRoles.size() > 0;
    }

    public boolean userRoles_contains( Object object ) {
        return this.userRoles.contains( object ) ;
    }
    
    public void userRoles_clear() {
        this.userRoles.clear() ;
    }
    
    public void centerRoles_set( String centerRolesKey , Object object ) {
        this.centerRoles.put( centerRolesKey , object ) ;
    }

    public void centerRoles_setAll( HashMap centerRolesAll ) {
        this.centerRoles = centerRolesAll ;
    }

    public Object centerRoles_get( String centerRolesKey ) {
        return this.centerRoles.get( centerRolesKey ) ;
    }

    public void centerRoles_remove( String centerRolesKey ) {
        this.centerRoles.remove( centerRolesKey ) ;
    }

    public boolean centerRoles_containsKey( String centerRolesKey ) {
        return this.centerRoles.containsKey( centerRolesKey ) ;
    }

    public boolean centerRoles_exists() {
        return !this.centerRoles.isEmpty() && this.centerRoles == null;
    }

    public void centerRoles_clear() {
        this.centerRoles.clear() ;
    }

    public HashMap centerRoles_getAll() {
        return this.centerRoles ;
    }
    
    public void sidePanelEntry_set( String spe ) {
        this.sidePanelEntry = spe ;
    }

    public String sidePanelEntry_get() {
        return this.sidePanelEntry ;
    }

    public boolean sidePanelEntry_exists() {
        return !this.sidePanelEntry.equals( "" ) && this.sidePanelEntry != null;
    }
    
    /**
     * @param aContext
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(javax.ejb.SessionContext aContext) {
        context=aContext;
    }
    
    @PostActivate
    public void activate() {
        try {
            this.logger = new Logger( "/com/eidp/" + this.getApplicationContext() + "/WebAppCache.log" ) ;
        } catch ( java.io.IOException e ) {
            throw new EJBException("[EIDPWebAppCache] : Cannot set applicationContext");
        }
    }
    
    @PreDestroy
    public void remove() {
    }
    
    @Override
    public void sessionCreated( HttpSessionEvent httpSessionEvent ) {
    }
    
    @Override
    public void sessionDestroyed( HttpSessionEvent httpSessionEvent ) {
        this.remove() ;
    }

    public String getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(String applicationContext) {
        this.applicationContext = applicationContext;
        try {
            this.logger = new Logger( "/com/eidp/" + applicationContext + "/WebAppCache.log" ) ;
            this.logger.logMessage( "Initializing WebAppCacheLog with Context: " + applicationContext + "." ) ;
        } catch (IOException e) {
            throw new EJBException("[EIDPWebAppCache] : Cannot set applicationContext");
        }
    }
    
    @PrePassivate
    public void prePassivate() {
        try {
            this.logger.close();
        } catch (IOException ex) {
            this.logger = null;
        }
        this.logger = null;
    }
}
