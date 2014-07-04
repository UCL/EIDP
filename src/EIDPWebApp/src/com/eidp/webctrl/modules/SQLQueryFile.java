/*
 * ServletQueryFile.java
 *
 * Created on January 13, 2006, 3:12 PM
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
 * @author rusch
 * @version
 */
public class SQLQueryFile{
    
    public SQLQueryFile( PrintWriter stream , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws  javax.xml.parsers.ParserConfigurationException, RemoteException, SQLException, SAXException, IOException  {
        try{
            response.setContentType("application/xm sdownload;charset=ISO-8859-15");
            response.setHeader("Content-disposition", "attachment; filename=\"" + "Abfrage.csv" + "\"");
            response.addHeader("Content-description", "Abfrage");
            Hilf helpFormatter = new Hilf();
            
            String strSQL = (String)uso.eidpWebAppCache.sessionData_get("SQLQuery");
            String strDBHost = "";
            String strDBInstance = "";
            String strDBUser = "";
            String strDBPassword = "";
            Connect connect = new Connect();
            Db db = new Db();
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
            ResultSet rs = null ;
            try{
                conn = connect.getConn(strDBUser, strDBPassword, strDBHost, strDBInstance);
                Statement stmt = conn.createStatement();
                rs = stmt.executeQuery(strSQL);
                ResultSetMetaData rsmd = rs.getMetaData() ;
                int intAnzahlSpalten = rsmd.getColumnCount() ;
                // print SQL
                stream.println(strSQL);
                // print header
                String strHeader = "";
                for( int i = 1 ; i <= intAnzahlSpalten ; i++ ){
                    strHeader += rsmd.getColumnName(i).toUpperCase() + ";";
                }
                
                stream.println(strHeader);
                
                // Tabelleninhalt
                while(rs.next()){
                    String strColumn = "";
                    for( int i = 1 ; i <= intAnzahlSpalten ; i++ ){
                        strColumn += (String)rs.getString(i) + ";";
                    }
                    stream.println(strColumn);
                }
                rs.close() ;
            }catch(SQLException e){
                e.printStackTrace();
            }
            
            stream.println("");
            stream.flush();
            stream.close();
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
