/*
 * SQLQueryTool.java
 *
 * Created on August 2, 2004, 8:47 AM
 */

package com.eidp.webctrl.modules;

import com.eidp.UserScopeObject.UserScopeObject;
import com.eidp.xml.XMLDataAccess;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.rmi.RemoteException;
import java.sql.*;
import org.xml.sax.SAXException;
import java.io.IOException;


import java.util.*;

/**
 *
 * @author  Rusch
 * @version
 */
public class SQLQueryTool{
    final String strFontDef = "<font face=\"Arial,helvetica\" color=\"#FFFFFF\">" ;
    
    public SQLQueryTool( PrintWriter out , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws  javax.xml.parsers.ParserConfigurationException, RemoteException, SQLException, SAXException, IOException  {
        
        Connect connect = new Connect();
        Db db = new Db();
        String strSql = "";
        String strTextAreaContent = "";
        String strSqlTrenner = "";
        String strSqlKommentar = "";
        String strToken = "" ;
        String strAnzeigen = "";
        String strDefinition = "";
        String strTableNameValue = "";
        String strDBHost = "";
        String strDBInstance = "";
        String strDBUser = "";
        String strDBPassword = "";
        Connection conn = null ;
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/db.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        Vector DBNodeResponseVector = new Vector();
        DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "database,host" );
        if(DBNodeResponseVector.size() > 0){
            strDBHost = (String)DBNodeResponseVector.get(0);
            StringTokenizer strtoURL = new StringTokenizer(strDBHost,"/");
            while(strtoURL.hasMoreTokens()){
                strDBHost = strtoURL.nextToken();
            }
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "database,instance-name" );
            strDBInstance = (String)DBNodeResponseVector.get(0);
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "database,user" );
            strDBUser = (String)DBNodeResponseVector.get(0);
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "database,password" );
            strDBPassword = (String)DBNodeResponseVector.get(0);
        }else{
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "querytool,host" );
            strDBHost = (String)DBNodeResponseVector.get(0);
            StringTokenizer strtoURL = new StringTokenizer(strDBHost,"/");
            while(strtoURL.hasMoreTokens()){
                strDBHost = strtoURL.nextToken();
            }
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "querytool,instance-name" );
            strDBInstance = (String)DBNodeResponseVector.get(0);
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "querytool,user" );
            strDBUser = (String)DBNodeResponseVector.get(0);
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "querytool,password" );
            strDBPassword = (String)DBNodeResponseVector.get(0);
        }
        
        // Spruch holen
        String strXMLFilePath = "/com/eidp/" + uso.applicationContext + "/resources/wisdom.xml" ;
        String strWisdomOFTheMoment = "";
        
        try{
            java.util.Date timestamp = new java.util.Date() ;
            long seed = timestamp.getTime() ;
            uso.xmlDataAccess = new XMLDataAccess( strXMLFilePath ) ;
            DBNodeResponseVector = new Vector();
            DBNodeResponseVector = uso.xmlDataAccess.getElementsByName( "truth" );
            if(DBNodeResponseVector.size() > 0){
                strWisdomOFTheMoment = (String)DBNodeResponseVector.get(getRandomNumber(seed , 0, DBNodeResponseVector.size()-1));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        try{
            strSqlTrenner = request.getParameter( "Trenner" ) ;
            strSqlTrenner = strSqlTrenner.trim() ;
        }catch(NullPointerException e){
            strSqlTrenner = "";
        }
        try{
            strSqlKommentar = request.getParameter( "Kommentar" ) ;
            strSqlKommentar = strSqlKommentar.trim() ;
        }catch(NullPointerException e){
            strSqlKommentar = "";
        }
        try{
            strTextAreaContent =  request.getParameter( "sqlstring" ) ;
            StringTokenizer strtoKommentarFilter = new StringTokenizer(strTextAreaContent.trim(), "\n");
            while(strtoKommentarFilter.hasMoreTokens()){
                strToken = strtoKommentarFilter.nextToken().trim();
                if( strToken.length() > 0 ){
                    if( strToken.length() >= strSqlKommentar.length() ){
                        if( ! strToken.substring( 0, strSqlKommentar.length() ).equals( strSqlKommentar ) ){
                            if(strToken.indexOf(strSqlKommentar) != -1)
                                strSql += strToken.substring(0, strToken.indexOf(strSqlKommentar)) + " ";
                            else
                                strSql += strToken + " ";
                            System.out.println(strToken);
                        }
                    }else{
                        strSql += strToken + " ";
                    }
                }
            }
        }catch(NullPointerException e){
            strSql = "";
            strTextAreaContent = "";
        }
        try{
            strAnzeigen = request.getParameter( "anzeigen" ) ;
            strAnzeigen = strAnzeigen.trim() ;
        }catch(NullPointerException e){
            strAnzeigen = "";
        }
        try{
            strDefinition = request.getParameter( "viewdef" ) ;
            strDefinition = strDefinition.trim() ;
        }catch(NullPointerException e){
            strDefinition = "";
        }
        try{
            strTableNameValue = request.getParameter( "Auswahl" ) ;
            strTableNameValue = strTableNameValue.trim() ;
        }catch(NullPointerException e){
            strTableNameValue = "";
        }
        
        // Stylesheet
        out.println("<style type=\"text/css\">");
        out.println("td.zelle { background-color:#000000; }");
        out.println("</style>");
        // JavaScript
        out.println( "<script language=\"JavaScript\"> " ) ;
        out.println( "function submitCSV(){" ) ;
        out.println( "  url = \"/TwWebApp/servlet/com.eidp.webctrl.Controller?module=AddOn;SQLQueryFile;show\" ; ");
        out.println( "  windowOpenFeatures = \"height=200,width=450,scrollbars=auto\" ; " ) ;
        out.println( "  windowName= \"Report\" ; " ) ;
        out.println( "  var nwindow = window.open( null ,windowName,windowOpenFeatures ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Frameset//EN' 'http://www.w3.org/TR/html4/frameset.dtd'>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"<html>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"<head>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"<title>Abfrage</title>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"</head>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"<frameset rows='400,*'>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"  <frame src='/TwWebApp/ReportHinweis.html' name='Hinweis'>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"  <frame src='/TwWebApp/Dummy.html' name='Report' id='Report'>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"</frameset>\" ) ; " ) ;
        out.println( "  nwindow.document.writeln( \"</html>\" ) ; " ) ;
        out.println( "  nwindow.focus() ; " ) ;
        out.println( "  nwindow.document.getElementById('Report').src=url ; " ) ;
        out.println( "  // Fenster schliesst automatisch nach 1/2 Minute" ) ;
        out.println( "  nwindow.setTimeout(\"self.close();\", 30000); " ) ;
        out.println( "}" ) ;
        
        out.println( "function bibberDingens(){" ) ;
        out.println( "  var topi=10;" ) ;
        out.println( "  var lefti=550;" ) ;
        out.println( "  var pausi=50;" ) ;
        out.println( "  for (var i = 20; i > 0; i--) {" ) ;
        out.println( "      for (var j = 2; j > 0; j--) {" ) ;
        out.println( "          var dingsi=topi+(i*10);" ) ;
        out.println( "          var dongsi=lefti+(i*10);" ) ;
        out.println( "          var subdingsi=topi+(i*(-10));" ) ;
        out.println( "          var subdongsi=lefti+(i*(-10));" ) ;
        out.println( "          if(i%2==0){ " ) ;
        out.println( "              window.setTimeout(\"moveImage(\" + dongsi + \", \" + dingsi + \")\",pausi*i); " ) ;
        out.println( "          }else{ " ) ;
        out.println( "              window.setTimeout(\"moveImage(\" + subdongsi + \", \" + subdingsi + \")\",pausi*i); " ) ;
        out.println( "          }" ) ;
        out.println( "      }" ) ;
        out.println( "  }" ) ;
        out.println( "  window.setTimeout(\"moveImage(\" + lefti + \", \" + topi + \")\",1050); " ) ;
        out.println( "}" ) ;
        out.println( "function moveImage(x,y){ " ) ;
        out.println( "  document.getElementsByTagName(\"body\")[0].style.backgroundPosition = x + 'px ' + y + 'px'; " ) ;
        out.println( "}") ;
        out.println( "</script>" ) ;
        out.println("</head>");
        out.println("<body style=\"background-image:url(/TwWebApp/images/skull.jpg); background-repeat:no-repeat; background-position:560px 10px;\" bgcolor=\"#000000\">");
//        out.println("<div id=\"bgim\" style=\"background-image:url(/TwWebApp/images/skull.jpg); background-repeat:no-repeat; background-position:550px 10px;\">");
        out.println("<br>");
        out.println("<br>");
        out.println("<h2>" + strFontDef + "SQL-Query-Tool</font></h2>");
        if(!strWisdomOFTheMoment.equals("")){
            out.println("<h4>" + strFontDef + strWisdomOFTheMoment + "</font></h4>");
        }
        out.println("<hr>");
        out.println("<br>");
        out.println("<form name=\"eingabe\" method=\"POST\" action=\"/TwWebApp/servlet/com.eidp.webctrl.Controller\">");
        out.println("<table border=\"0\">");
        out.println("<tr>");
        out.println("<td><nobr>");
        out.println("<select name=\"Auswahl\">");
        ResultSet rs = null ;
        
        try{
            conn = connect.getConn(strDBUser, strDBPassword, strDBHost, strDBInstance);
            rs = db.getAllTableNames(conn);
            String strTableName = "";
            String strSelected = "";
            // Tabellen in SelectBox schreiben
            while(rs.next()){
                strSelected = "";
                strTableName = rs.getString(1);
                if(strTableName.equals(strTableNameValue))
                    strSelected = "selected";
                out.println("<option value=\"" + strTableName + "\" " + strSelected + " >" + strTableName + "</option>");
            }
            rs.close() ;
            rs = db.getAllViewNames(conn);
            String strViewName = "";
            // Viewnamen in SelectBox schreiben
            while(rs.next()){
                strSelected = "";
                strViewName = rs.getString("viewname");
                if(strViewName.equals(strTableNameValue))
                    strSelected = "selected";
                out.println("<option value=\"" + strViewName + "\" " + strSelected + " >" + strViewName + "</option>");
            }
            rs.close() ;
        }catch(SQLException e){
            out.println( strFontDef + e + "</font><br>") ;
        }
        
        out.println("</select>");
        out.println("<input type=\"submit\" name=\"anzeigen\" value=\"ANZEIGEN\" onClick=\"javascript:bibberDingens();\">");
        out.println("<input type=\"submit\" name=\"viewdef\" value=\"VIEWDEFINITION\" onClick=\"javascript:bibberDingens();\">");
        out.println("</nobr></td>");
        
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>");
        out.println("<input type=\"hidden\" name=\"module\" value=\"AddOn;SQLQueryTool;show\">");
        out.println("<textarea cols=\"80\" rows=\"10\" name=\"sqlstring\">");
        out.println(strTextAreaContent);
        out.println("</textarea>");
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>");
        out.println("<input type=\"submit\" value=\"AUSF&Uuml;HREN\" onClick=\"javascript:bibberDingens();\">");
        out.println("<b>SQL-Trennzeichen" + "</b>");
        out.println("<select name=\"Trenner\">");
        out.println("<option value=\";\">;</option>");
        out.println("<option value=\"//\">//</option>");
        out.println("</select>");
        out.println("<b>&nbsp;SQL-Kommentar" + "</b>");
        out.println("<select name=\"Kommentar\">");
        out.println("<option value=\"--\">--</option>");
        out.println("<option value=\"//\">//</option>");
        out.println("</select>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</form>");
        
        if( !strSql.equals("") && strSql.length() > 6 && (strAnzeigen.equals("") || strAnzeigen == null ) ){
            
            out.println("<hr>");
            out.println("<br>");
            
            try{
                StringTokenizer strto = new StringTokenizer(strSql, strSqlTrenner);
                String strBefehlsArt = "";
                
                while(strto.hasMoreTokens()){
                    
                    strSql = strto.nextToken().trim();
                    StringTokenizer strto2 = new StringTokenizer(strSql," ");
                    
                    try{
                        strBefehlsArt = strto2.nextToken().toUpperCase();
                        
                        if(strBefehlsArt.equals("SELECT")){
                            showTableContent(strSql, conn, out, uso);
                            out.println("<br><br>");
                        } else{
                            out.println(strFontDef + strBefehlsArt + "</font>");
                            executeSQL(strSql, conn, out);
                            out.println("<br>");
                        }
                    }catch(NoSuchElementException e){
                    }
                    
                }
                
                conn.close() ;
                
            }catch(SQLException e){
                out.println( strFontDef + e + "</font><br>");
            }
        }
        
        if(!strAnzeigen.equals("")){
            out.println("<hr>");
            out.println("<br>");
            showTableContent("SELECT * FROM " + strTableNameValue, conn, out, uso);
        }
        
        if(!strDefinition.equals("")){
            out.println("<hr>");
            out.println("<br>");
            showViewDefinition(strTableNameValue, conn, out);
        }
//        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
        
        out.close();
    }
    
    public void showViewDefinition(String viewName, Connection conn, PrintWriter out){
        
        Db db = new Db();
        String strViewDefinition = db.getViewDefinition( conn, viewName );
        
        if( strViewDefinition.equals("") )
            strViewDefinition = viewName + " ist keine VIEW! - Trottel...";
        
        out.println("<table border=\"1\">") ;
        // &Uuml;berschrift anzeigen
        out.println("<tr>") ;
        out.println( "<th align=\"left\"><font face=\"Arial,Helvetica\" color=\"#AAAACF\">Definition: " + viewName.toUpperCase() + "</font></th>" ) ;
        out.println("</tr>") ;
        
        // Tabelleninhalt
        out.println("<tr>") ;
        out.println( "<td class=\"zelle\">" + strFontDef + writeEmptyField(strViewDefinition) + "</font></td>" ) ;
        out.println("</tr>") ;
        
        out.println("</table>") ;
        
    }
    
    public void showTableContent(String sql, Connection conn, PrintWriter out, UserScopeObject uso) throws RemoteException{
        
        Statement stmt = null ;
        ResultSet rs = null ;
        
        try{
            
            stmt = conn.createStatement() ;
            rs = stmt.executeQuery( sql ) ;
            ResultSetMetaData rsmd = rs.getMetaData() ;
            int intAnzahlDatensaetze = 0 ;
            int intAnzahlSpalten = rsmd.getColumnCount() ;
            
            // Query in die Session schreiben
            uso.eidpWebAppCache.sessionData_set("SQLQuery", sql);
            
            out.println("<table border=\"1\">") ;
            // SQL-Befehl anzeigen
            out.println("<tr>") ;
            out.println( "<th align=\"left\" colspan=" + intAnzahlSpalten + "><input type=\"button\" title=\"CSV-File generieren\" name=\"csv\" value=\"CSV-File\" onclick=\"javascript:bibberDingens();submitCSV();\">&nbsp;&nbsp;<font face=\"Arial,Helvetica\" color=\"#AAAACF\">" + sql.toUpperCase() + "</font></th>" ) ;
            out.println("</tr>") ;
            // Tabellen&uuml;berschriften
            out.println("<tr>") ;
            
            for( int i = 1 ; i <= intAnzahlSpalten ; i++ ){
                out.println( "<th> " + strFontDef + rsmd.getColumnName(i).toUpperCase() + "</font></th>" ) ;
            }
            
            out.println("</tr>") ;
            
            // Tabelleninhalt
            while(rs.next()){
                out.println("<tr>") ;
                for( int i = 1 ; i <= intAnzahlSpalten ; i++ ){
                    out.println( "  <td class=\"zelle\">" + strFontDef + writeEmptyField((String)rs.getString(i)) + "</font></td>" ) ;
                }
                out.println("</tr>") ;
                intAnzahlDatensaetze++;
            }
            rs.close() ;
            out.println("<tr>") ;
            out.println( "  <th align=\"left\" colspan=" + intAnzahlSpalten + "><font face=\"Arial,Helvetica\" color=\"#AAAACF\">" + intAnzahlDatensaetze + " Datens&auml;tze" + "</font></td>" ) ;
            out.println("</tr>") ;
            out.println("</table>") ;
            
        }catch(SQLException e){
            out.println( strFontDef + e + "</font><br>") ;
        }
    }
    
    public void executeSQL(String sql, Connection conn, PrintWriter out){
        
        Statement stmt = null ;
        
        try{
            stmt = conn.createStatement() ;
            int intAnzahlDatensaetze = stmt.executeUpdate( sql ) ;
            out.println( strFontDef + "-Befehl erfolgreich ausgef&uuml;hrt. [" + intAnzahlDatensaetze + " Datens&auml;tze betroffen.]</font>") ;
        }catch(SQLException e){
            out.println( strFontDef + e + "</font><br>") ;
            out.println( strFontDef + "Fehler: " + sql + "</font><br>") ;
        }
    }
    
    public String writeEmptyField(String str){
        try{
            if( str.trim().equals("") || str == null )
                str = "&nbsp;" ;
        }catch(NullPointerException e){
            str = "&nbsp;" ;
        }
        return str ;
    }
    
    public static int getRandomNumber(long seed, long LowerLimit, long UpperLimit){
        Random generator = new Random(seed);
        // get the range, casting to long to avoid overflow problems
        long range = UpperLimit - LowerLimit + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * generator.nextDouble());
        
        return (int)(fraction + LowerLimit);
    }
}
