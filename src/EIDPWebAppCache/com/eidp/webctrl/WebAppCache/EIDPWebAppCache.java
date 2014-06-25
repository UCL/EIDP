
package com.eidp.webctrl.WebAppCache;

import com.eidp.logger.Logger;
import java.io.IOException ;
import java.util.HashMap;
import java.util.Vector;
import javax.ejb.EJBException;
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
public class EIDPWebAppCache implements HttpSessionListener {
    
    private javax.ejb.SessionContext context;
    private transient Logger logger;
    private int logLevel = 9 ;
    private HashMap sessionData = new HashMap() ;
    private Vector userRoles = new Vector() ;
    private HashMap centerRoles = new HashMap() ;
    private HashMap sessionRefs = new HashMap() ;
    private String sidePanelEntry = "" ;
    private String applicationContext = "" ;
    
    /**
     *
     * @param sessionKey
     * @param object
     * @throws RemoteException
     */
    public void sessionRef_set( String sessionKey , Object object ) throws java.rmi.RemoteException {
        this.sessionRefs.put( sessionKey , object ) ;
    }
    /**
     *
     * @param sessionKey
     * @throws RemoteException
     * @return
     */
    public Object sessionRef_get( String sessionKey ) throws java.rmi.RemoteException {
        return this.sessionRefs.get( sessionKey ) ;
    }
    /**
     *
     * @param sessionKey
     * @throws RemoteException
     */
    public void sessionRef_remove( String sessionKey ) throws java.rmi.RemoteException {
        this.sessionRefs.remove( sessionKey ) ;
    }
    
    /**
     *
     * @throws RemoteException
     */
    public void sessionRef_clear() throws java.rmi.RemoteException {
        this.sessionRefs.clear() ;
    }
    
    /**
     *
     * @param sessionDataKey
     * @param object
     * @throws RemoteException
     */
    public void sessionData_set( String sessionDataKey , Object object ) throws java.rmi.RemoteException {
        this.sessionData.put( sessionDataKey , object ) ;
    }
    /**
     *
     * @param sessionDataAll
     * @throws RemoteException
     */
    public void sessionData_setAll( HashMap sessionDataAll ) throws java.rmi.RemoteException {
        this.sessionData = sessionDataAll ;
    }
    /**
     *
     * @param sessionDataKey
     * @throws RemoteException
     * @return
     */
    public Object sessionData_get( String sessionDataKey ) throws java.rmi.RemoteException {
        return this.sessionData.get( sessionDataKey ) ;
    }
    /**
     *
     * @param sessionDataKey
     * @throws RemoteException
     */
    public void sessionData_remove( String sessionDataKey ) throws java.rmi.RemoteException {
        this.sessionData.remove( sessionDataKey ) ;
    }
    /**
     *
     * @param sessionDataKey
     * @throws RemoteException
     * @return
     */
    public boolean sessionData_containsKey( String sessionDataKey ) throws java.rmi.RemoteException {
        return this.sessionData.containsKey( sessionDataKey ) ;
    }
    /**
     *
     * @throws RemoteException
     * @return
     */
    public boolean sessionData_exists() throws java.rmi.RemoteException {
        if ( this.sessionData.isEmpty() || this.sessionData != null ) {
            return false ;
        } else return true ;
    }
    /**
     *
     * @throws RemoteException
     */
    public void sessionData_clear() throws java.rmi.RemoteException {
        this.sessionData.clear() ;
    }
    
    /**
     *
     * @param object
     * @throws RemoteException
     */
    public void userRoles_set( Object object ) throws java.rmi.RemoteException {
        this.userRoles.addElement( object ) ;
    }
    /**
     *
     * @param index
     * @throws RemoteException
     * @return
     */
    public Object userRoles_get( int index ) throws java.rmi.RemoteException {
        return this.userRoles.get( index ) ;
    }
    /**
     *
     * @throws RemoteException
     * @return
     */
    public int userRoles_size() throws java.rmi.RemoteException {
        return this.userRoles.size() ;
    }
    /**
     *
     * @throws RemoteException
     * @return
     */
    public boolean userRoles_exists() throws java.rmi.RemoteException {
        if ( this.userRoles.size() > 0 ) {
            return true ;
        } else return false ;
    }
    /**
     *
     * @param object
     * @return
     */
    public boolean userRoles_contains( Object object ) {
        return this.userRoles.contains( object ) ;
    }
    public void userRoles_clear() {
        this.userRoles.clear() ;
    }
    
    /**
     *
     * @param centerRolesKey
     * @param object
     * @throws RemoteException
     */
    public void centerRoles_set( String centerRolesKey , Object object ) throws java.rmi.RemoteException {
        this.centerRoles.put( centerRolesKey , object ) ;
    }
    /**
     *
     * @param centerRolesAll
     * @throws RemoteException
     */
    public void centerRoles_setAll( HashMap centerRolesAll ) throws java.rmi.RemoteException {
        this.centerRoles = centerRolesAll ;
    }
    /**
     *
     * @param centerRolesKey
     * @throws RemoteException
     * @return
     */
    public Object centerRoles_get( String centerRolesKey ) throws java.rmi.RemoteException {
        return this.centerRoles.get( centerRolesKey ) ;
    }
    /**
     *
     * @param centerRolesKey
     * @throws RemoteException
     */
    public void centerRoles_remove( String centerRolesKey ) throws java.rmi.RemoteException {
        this.centerRoles.remove( centerRolesKey ) ;
    }
    /**
     *
     * @param centerRolesKey
     * @throws RemoteException
     * @return
     */
    public boolean centerRoles_containsKey( String centerRolesKey ) throws java.rmi.RemoteException {
        return this.centerRoles.containsKey( centerRolesKey ) ;
    }
    /**
     *
     * @throws RemoteException
     * @return
     */
    public boolean centerRoles_exists() throws java.rmi.RemoteException {
        if ( this.centerRoles.isEmpty() || this.centerRoles != null ) {
            return false ;
        } else return true ;
    }
    /**
     *
     * @throws RemoteException
     */
    public void centerRoles_clear() throws java.rmi.RemoteException {
        this.centerRoles.clear() ;
    }
    /**
     *
     * @throws RemoteException
     * @return
     */
    public HashMap centerRoles_getAll() throws java.rmi.RemoteException {
        return this.centerRoles ;
    }
    
    /**
     *
     * @param spe
     * @throws RemoteException
     */
    public void sidePanelEntry_set( String spe ) throws java.rmi.RemoteException {
        this.sidePanelEntry = spe ;
    }
    /**
     *
     * @throws RemoteException
     * @return
     */
    public String sidePanelEntry_get() throws java.rmi.RemoteException {
        return this.sidePanelEntry ;
    }
    /**
     *
     * @throws RemoteException
     * @return
     */
    public boolean sidePanelEntry_exists() throws java.rmi.RemoteException {
        if ( this.sidePanelEntry.equals( "" ) || this.sidePanelEntry == null ) {
            return false ;
        } else return true ;
    }
    
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(javax.ejb.SessionContext aContext) {
        context=aContext;
    }
    
    
    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        try {
            this.logger = new Logger( "/com/eidp/" + this.getApplicationContext() + "/WebAppCache.log" ) ;
        } catch ( java.io.IOException ioe ) {
        }
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
    }
    
    /**
     *
     * @param httpSessionEvent
     */
    public void sessionCreated( HttpSessionEvent httpSessionEvent ) {
    }
    
    /**
     *
     * @param httpSessionEvent
     */
    public void sessionDestroyed( HttpSessionEvent httpSessionEvent ) {
        this.ejbRemove() ;
    }

    /**
     * @return the applicationContext
     */
    public String getApplicationContext() {
        return applicationContext;
    }

    /**
     * @param applicationContext the applicationContext to set
     */
    public void setApplicationContext(String applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        this.logger = new Logger( "/com/eidp/" + applicationContext + "/WebAppCache.log" ) ;
        this.logger.logMessage( "Initializing WebAppCacheLog with Context: " + applicationContext + "." ) ;
    }
    
    @PrePassivate
    public void prePassivate() {
        try {
            this.logger.close();
        } catch (IOException ex) {
            throw new EJBException("[EIDPWebAppCache] : @PrePassivate error; cannot close logger");
        }
        this.logger = null;
    }
}
