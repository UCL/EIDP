/*
 * ERROR_ReportExists.java
 *
 * Created on June 26, 2004, 10:26 AM
 */

package com.eidp.webctrl.modules.Report;

import com.eidp.core.DB.DBMappingHomeRemote;
import com.eidp.core.DB.DBMappingRemote;
import com.eidp.UserScopeObject.UserScopeObject;
import com.eidp.webctrl.WebAppCache.EIDPWebAppCacheRemote ;

import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ejb.Handle;

import java.rmi.RemoteException;
import java.sql.SQLException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.sql.*;

/**
 *
 * @author  rusch
 */
public class ERROR_ReportExists {
    
    /** Creates a new instance of Labor */
    public ERROR_ReportExists( PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws RemoteException, SQLException, SAXException, IOException {
        // Stylesheet
        printWriter.println( "<style type=\"text/css\">" ) ;
        printWriter.println( "<!-- " ) ;
        printWriter.println( "  body { background-color:#FF8000;} " ) ;
        printWriter.println( "--> </style> " ) ;
        
        printWriter.println("   <div align=\"center\"><FONT face='Arial,Verdana,Helvetica'>");
        printWriter.println("       <H2>F&uuml;r diese Visite wurde bereits ein Arztbrief generiert.</H2>");
        printWriter.println("       Sie k&ouml;nnen dieses Fenster jetzt schlie&szlig;en.");
        printWriter.println("       </FONT>");
        printWriter.println("   </div>");
    }
    
}
