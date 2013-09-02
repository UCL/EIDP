/*
 * TwReportModuleLoader.java
 *
 * Created on January 17, 2005, 3:25 PM
 */

package com.eidp.webctrl.modules.Report;

import com.eidp.xml.XMLDataAccess;
import com.eidp.core.DB.DBMappingHomeRemote;
import com.eidp.core.DB.DBMappingRemote;
import com.eidp.webctrl.WebAppCache.EIDPWebAppCacheRemote ;

import com.eidp.UserScopeObject.UserScopeObject ;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ejb.Handle ;
import org.w3c.dom.NodeList ;
/**
 *
 * @author  rusch
 */
public class EIDPReportModuleLoader {
    
    public EIDPReportModuleLoader()  {
    }
    
    public NodeList LoadClass( String loadClass , NodeList  reportNode , UserScopeObject uso ) throws javax.xml.parsers.ParserConfigurationException {
        
        NodeList nodelist = null;
        
//        try{
//            if ( loadClass.equals( "HIV_Verlauf" ) ) {
//                HIV_Verlauf verlaufClass = new HIV_Verlauf( uso,  reportNode ) ;
//                nodelist = verlaufClass.createHIV_Verlauf();
//            }
//            else
//                if ( loadClass.equals( "RHEUMA_Verlauf" ) ) {
//                    RHEUMA_Verlauf verlaufClass = new RHEUMA_Verlauf( uso,  reportNode ) ;
//                    nodelist = verlaufClass.createRHEUMA_Verlauf();
//                }
//                else
//                    if ( loadClass.equals( "RHEUMA_NZ_Verlauf" ) ) {
//                        RHEUMA_NZ_Verlauf verlaufClass = new RHEUMA_NZ_Verlauf( uso,  reportNode ) ;
//                        nodelist = verlaufClass.createRHEUMA_NZ_Verlauf();
//                    }
//        }catch( java.rmi.RemoteException e ){
//        }catch( java.sql.SQLException e ){
//        }catch( java.io.IOException e ){
//        }catch( org.xml.sax.SAXException e ){
//        }
        
        return nodelist;
    }
    
}
