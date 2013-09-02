

package com.eidp.webctrl.WebAppCache;

import javax.ejb.EJBHome;

/**
 * HomeRemote Interface
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

public interface EIDPWebAppCacheHomeRemote extends EJBHome {
    
    /**
     * Remote Interface for TwWebAppCache.
     * @param applicationname Application-Name
     * @throws CreateException if CreateException is thrown.
     * @throws RemoteException if RemoteException is thrown.
     * @throws IOException if IOException is thrown.
     * @return remote interface of TwWebAppCache.
     */
    public EIDPWebAppCacheRemote create( String applicationname ) throws javax.ejb.CreateException, java.rmi.RemoteException , java.io.IOException ;
    
}
