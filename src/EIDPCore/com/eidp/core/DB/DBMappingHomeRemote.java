/*
 * DBHomeRemote.java
 *
 * Created on April 28, 2003, 10:46 AM
 */

package com.eidp.core.DB;

import javax.ejb.EJBHome;

/**
 * HomeRemote Interface of DBMapping.
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
public interface DBMappingHomeRemote extends EJBHome {
    
    // queryID: ID specified in the db.xml file
    // queryStmnt: field=value
    /**
     * Remote Interface for the DBMapper.
     * @param applicationname Requires the Context the application shall act on.
     * @throws SQLException
     * @throws CreateException
     * @throws RemoteException
     * @throws IOException
     * @return the remote interface.
     */
    public DBMappingRemote create( String applicationname ) throws java.sql.SQLException , javax.ejb.CreateException , java.io.IOException ;
    
}
