
package com.eidp.webctrl.WebAppCache;

import com.eidp.xml.XMLDataAccess;
import com.eidp.logger.Logger ;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import java.util.HashMap;
import java.util.Vector;
import java.util.Set ;

import java.io.InputStream ;
import javax.ejb.Stateful;

import org.w3c.dom.NodeList;

import javax.servlet.http.HttpSessionListener ;
import javax.servlet.http.HttpSessionEvent ;

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
public class EIDPWebAppCache implements javax.ejb.SessionBean , HttpSessionListener {
    
    private javax.ejb.SessionContext context;
    //    private XMLDataAccess xmlDataAccess ;
    private Logger logger ;
    private int logLevel = 9 ;
    
    private HashMap sessionData = new HashMap() ;
    
    private Vector userRoles = new Vector() ;
    private HashMap centerRoles = new HashMap() ;
    
    private HashMap sessionRefs = new HashMap() ;
    
    private String sidePanelEntry = "" ;
    
    private String applicationContext = "" ;
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * @param applicationname
     * @throws CreateException
     * @throws RemoteException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void ejbCreate( String applicationname ) throws javax.ejb.CreateException, java.rmi.RemoteException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException {
        this.applicationContext = applicationname ;
        this.logger = new Logger( "/com/eidp/" + applicationname + "/WebAppCache.log" ) ;
        this.logger.logMessage( "Initializing WebAppCacheLog with Context: " + applicationname + "." ) ;
        //        String xmlfile = "/org/eidp/" + applicationname + "/resources/webctrl/controller.xml" ;
        //        this.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        //        this.logger.logMessage( "xmlfile read, Bean successfully created." ) ;
    }
    
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
    
    //
    //    public Vector ctrlGetElementsByName( String requestString , NodeList requestList ) throws java.rmi.RemoteException , org.xml.sax.SAXException , java.io.IOException {
    //        this.logger.logMessage( "ctrlGetElementsByName( String , NodeList )" ) ;
    //        Vector resultVector = new Vector() ;
    //        this.logger.logMessage( ">>> processing xml..." ) ;
    //        resultVector = this.xmlDataAccess.getElementsByName( requestString , requestList ) ;
    //        this.logger.logMessage( ">>> xml successessfully processed, returning Vector." ) ;
    //        return resultVector ;
    //    }
    //
    //    public Vector ctrlGetNodeListsByName( String requestString , NodeList requestList ) throws java.rmi.RemoteException , org.xml.sax.SAXException , java.io.IOException {
    //        this.logger.logMessage( "ctrlGetNodeListsByName( String , NodeList )" ) ;
    //        Vector resultVector = new Vector() ;
    //        this.logger.logMessage( ">>> processing xml..." ) ;
    //        resultVector = this.xmlDataAccess.getNodeListsByName( requestString , requestList ) ;
    //        this.logger.logMessage( ">>> xml successessfully processed, returning Vector." ) ;
    //        return resultVector ;
    //    }
    //
    //    public void ctrlChangeContext( String applicationname ) throws java.rmi.RemoteException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException {
    //        String xmlfile = "/org/eidp/" + applicationname + "/resources/webctrl/controller.xml" ;
    //        this.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
    //    }
    
    // === Other application specific data structures
    
    
    
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
            this.logger = new Logger( "/com/eidp/" + this.applicationContext + "/WebAppCache.log" ) ;
        } catch ( java.io.IOException ioe ) {
        }
    }
    
    
    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        try {
            this.logger.close() ;
        } catch ( java.io.IOException ioe ) {
        }
        this.logger = null ;
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
    
}
