/*
 * EIDPModuleLoader
 *
 * Created on May 27, 2004, 9:13 AM
 */

package com.eidp.webctrl.modules;

import com.eidp.core.DB.DBMappingHomeRemote;
import com.eidp.core.DB.DBMappingRemote;
import com.eidp.webctrl.WebAppCache.EIDPWebAppCacheRemote ;

import java.io.PrintWriter;

import com.eidp.UserScopeObject.UserScopeObject ;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ejb.Handle ;

import java.util.HashMap ;
import java.util.Vector ;

import java.lang.reflect.* ;

// Inserte your AddOn-Classes here:

// import MyAddOnClass ;
// import MyFurtherAddOnClass ;

/**
 * Controller is the main entry-point for EIDPWebApp. From
 * this servlet any action is dispatched to further classes.
 * Furthermore the Controller offers special functionalities to
 * be used for developing a Web Application with the Enterprise
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

public class EIDPModuleLoader extends EIDPModuleLoaderAPI {
    
    /** Creates a new instance of EIDPModuleLoader */
    public EIDPModuleLoader( String loadAddOnClass , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws java.rmi.RemoteException , java.io.IOException , java.sql.SQLException , org.xml.sax.SAXException {
        this.EIDPModuleLoaderINIT( loadAddOnClass , request , response , uso ) ;
    }
    
    public void EIDPLoadClass( String loadAddOnClass , PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso )  {
        try{
            String AddOnClassName = "com.eidp.webctrl.modules." + loadAddOnClass ;
            Class klasse = Class.forName( AddOnClassName ) ;
            Class[] paramClasses = { PrintWriter.class , HttpServletRequest.class , HttpServletResponse.class , UserScopeObject.class } ;
            Constructor constr = klasse.getConstructor( paramClasses ) ;
            Object[] paramObjects = { printWriter , request , response , uso } ;
            Object object = constr.newInstance( paramObjects ) ;
        } catch ( java.lang.ClassNotFoundException e ) {
        } catch ( java.lang.NoSuchMethodException e ) {
        } catch ( java.lang.InstantiationException e ) {
        } catch ( java.lang.IllegalAccessException e ) {
        } catch ( java.lang.reflect.InvocationTargetException e ) {
        }
        
    }
    
    public void EIDPScripts( PrintWriter printWriter ) {
    }
    
}
