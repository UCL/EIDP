/*
 * UserScopeObject.java
 *
 * Created on May 13, 2004, 10:01 AM
 */

package com.eidp.UserScopeObject;

import javax.servlet.http.HttpSession;

import java.util.HashMap ;
import java.util.Vector ;
import com.eidp.xml.XMLDataAccess;
import com.eidp.core.DB.DBMappingRemote;
import com.eidp.webctrl.SharedMethods ;
import com.eidp.webctrl.WebAppCache.EIDPWebAppCache;

/**
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
public class UserScopeObject {
    public HttpSession session;
    public String applicationContext = "" ;
    public String userLogin = "" ;
    public String userID = "" ;
    public String userCenter = "" ;
    public Vector Filters = new Vector() ;
    public Vector FilterNames = new Vector() ;
    public HashMap localRefs = new HashMap() ;
    public HashMap dataMap = new HashMap() ;
    public HashMap preLoadData = new HashMap() ;
    public HashMap selectDBMap = new HashMap() ;
    public XMLDataAccess xmlDataAccess;
    public XMLDataAccess xmlWebMenu ;
    public XMLDataAccess xmlFilterAccess;
    public XMLDataAccess xmlColorAccess;
    public XMLDataAccess xmlApplication;
    public DBMappingRemote dbMapper = null ;
    public EIDPWebAppCache eidpWebAppCache = null ;
    public boolean preLoadFlag = false ;
    public boolean multiFlag = false ;
    public HashMap colorMap = new HashMap() ;
    public HashMap usedFilters = new HashMap() ;
    public SharedMethods sharedMethods = new SharedMethods() ;
    
    public UserScopeObject() {
    }
}
