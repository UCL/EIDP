/*
 * MESSAGE_ReportExport.java
 *
 * Created on June 26, 2004, 10:26 AM
 */

package com.eidp.webctrl.modules.Report;

import com.eidp.UserScopeObject.UserScopeObject;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.rmi.RemoteException;
import java.sql.SQLException;
import org.xml.sax.SAXException;
import java.io.IOException;

/**
 *
 * @author  rusch
 */
public class MESSAGE_ReportExport {
    
    /** Creates a new instance of Labor */
    public MESSAGE_ReportExport( PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws RemoteException, SQLException, SAXException, IOException {
        // Stylesheet
        printWriter.println( "<style type=\"text/css\">" ) ;
        printWriter.println( "<!-- " ) ;
        printWriter.println( "  body { background-color:#FF8000;} " ) ;
        printWriter.println( "--> </style> " ) ;
        
        printWriter.println("   <div align=\"center\"><FONT face='Arial,Verdana,Helvetica'>");
        printWriter.println("       <H2>Arztbrief-Export durchgef&uuml;hrt.</H2>");
        printWriter.println("       Sie k&ouml;nnen dieses Fenster jetzt schlie&szlig;en.");
        printWriter.println("       </FONT>");
        printWriter.println("   </div>");
        // Seitenkopf
//        TwGenerator twGen = new TwGenerator("Arztbrief-Export durchgef&uuml;hrt.", printWriter, uso ) ;
//
//        twGen.CloseModule( printWriter , uso ) ;
    }
    
}
