/*
 * DataSourceAPI.java
 *
 * Created on 5. April 2005, 10:41
 */

package com.eidp.core.DB.modules;

import java.util.HashMap ;
import java.util.Vector ;
import org.w3c.dom.NodeList ;
import java.util.logging.*;

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
public interface DataSourceAPI {
    
    public abstract void ProcessDBAction( NodeList dataSetNode , NodeList methodNode , HashMap paramMap , Logger logger ) throws Exception , org.xml.sax.SAXException , java.io.IOException ;
    
    public abstract Vector getRowRange( Integer rowNumber , Integer endRow ) ;
    
    public abstract HashMap getRow( Integer rowNumber ) ;
    
    public abstract Integer size() ;
    
    public abstract void closeConnection( Logger logger ) throws Exception ;
    
    public abstract Object getException() ;
    
    public abstract void setException( Object o ) ;
    
    public abstract void resetException() ;
    
}
