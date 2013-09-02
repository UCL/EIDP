

package com.eidp.webctrl.WebAppCache;

import javax.ejb.EJBObject;
import java.util.Vector;
import java.util.HashMap;
import java.util.Set ;

import org.w3c.dom.NodeList;

/**
 * Remote-Interface for EIDPWebAppCache.
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

public interface EIDPWebAppCacheRemote extends EJBObject {
    
    /**
     * Set SessionReferences. Session References are not managed by the
     * application (see sessionData).
     * @param sessionKey Key to store the reference.
     * @param object Object to be stored.
     * @throws RemoteException Throws RemoteException
     */
    public void sessionRef_set( String sessionKey , Object object ) throws java.rmi.RemoteException ;
    /**
     * Retrieve a session Reference.
     * @param sessionKey Session Key to be retrieved.
     * @throws RemoteException Throws RemoteException.
     */
    public Object sessionRef_get( String sessionKey ) throws java.rmi.RemoteException ;
    /**
     * Remove Session Reference.
     * @param sessionKey Session Key to be removed.
     * @throws RemoteException Throws Remote Exception.
     */
    public void sessionRef_remove( String sessionKey ) throws java.rmi.RemoteException ;
    /**
     * Clear All Session References.
     * @throws RemoteException Throws RemoteException.
     */
    public void sessionRef_clear() throws java.rmi.RemoteException ;
    
    /**
     * Set SessionData. SessionData is primarily used by the application.
     * @param sessionDataKey Key to store and retrieve an object.
     * @param object Object to be stored.
     * @throws RemoteException Throws RemoteException.
     */
    public void sessionData_set( String sessionDataKey , Object object ) throws java.rmi.RemoteException ;
    /**
     * Set complete sessionData HashMap.
     * @param sessionDataAll The complete sessionData Map.
     * @throws RemoteException Throws RemoteException.
     */
    public void sessionData_setAll( HashMap sessionDataAll ) throws java.rmi.RemoteException ;
    /**
     * Retrieve object out of sessionData
     * @param sessionDataKey Key to be retrieved.
     * @return Returns Object associated with the key.
     * @throws RemoteException Throws RemoteException.
     */
    public Object sessionData_get( String sessionDataKey ) throws java.rmi.RemoteException ;
    /**
     * Remove Object from sessionData.
     * @param sessionDataKey Object to be removed.
     * @throws RemoteException Throws RemoteException.
     */
    public void sessionData_remove( String sessionDataKey ) throws java.rmi.RemoteException ;
    /**
     * Check if sessionData contains a key.
     * @param sessionDataKey Key to be checked.
     * @return true or false.
     * @throws RemoteException Throws RemoteException.
     */
    public boolean sessionData_containsKey( String sessionDataKey ) throws java.rmi.RemoteException ;
    /**
     * Check if any object is stored in sessionData.
     * @return true or false.
     * @throws RemoteException Throws RemoteException.
     */
    public boolean sessionData_exists() throws java.rmi.RemoteException ;
    /**
     * Clear all sessionData.
     * @throws RemoteException Throws RemoteException.
     */
    public void sessionData_clear() throws java.rmi.RemoteException ;
    
    public Object userRoles_get( int index ) throws java.rmi.RemoteException ;
    public void userRoles_set( Object object ) throws java.rmi.RemoteException ;
    public int userRoles_size() throws java.rmi.RemoteException ;
    public boolean userRoles_exists() throws java.rmi.RemoteException ;
    public boolean userRoles_contains( Object object ) throws java.rmi.RemoteException ;
    public void userRoles_clear() throws java.rmi.RemoteException  ;
    
    public void centerRoles_set( String centerRolesKey , Object object ) throws java.rmi.RemoteException ;
    public void centerRoles_setAll( HashMap centerRolesAll ) throws java.rmi.RemoteException ;
    public Object centerRoles_get( String centerRolesKey ) throws java.rmi.RemoteException ;
    public void centerRoles_remove( String centerRolesKey ) throws java.rmi.RemoteException ;
    public boolean centerRoles_containsKey( String centerRolesKey ) throws java.rmi.RemoteException ;
    public boolean centerRoles_exists() throws java.rmi.RemoteException ;
    public void centerRoles_clear() throws java.rmi.RemoteException ;
    public HashMap centerRoles_getAll() throws java.rmi.RemoteException ;
    
    public void sidePanelEntry_set( String spe ) throws java.rmi.RemoteException ;
    public String sidePanelEntry_get() throws java.rmi.RemoteException ;
    public boolean sidePanelEntry_exists() throws java.rmi.RemoteException ;
    
    // === XML Controller (controller.xml)
    
//    public Vector ctrlGetElementsByName ( String requestString ) throws java.rmi.RemoteException , org.xml.sax.SAXException ;
//
//    public Vector ctrlGetNodeListsByName ( String requestString ) throws java.rmi.RemoteException , org.xml.sax.SAXException ;
//
//    public Vector ctrlGetElementsByName ( String requestString , NodeList requestList ) throws java.rmi.RemoteException , org.xml.sax.SAXException ;
//
//    public Vector ctrlGetNodeListsByName ( String requestString , NodeList requestList ) throws java.rmi.RemoteException , org.xml.sax.SAXException ;
//
//    public void ctrlChangeContext ( String appclitionname ) throws java.rmi.RemoteException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException ;
//
    // Further Variables for caching
    
}

