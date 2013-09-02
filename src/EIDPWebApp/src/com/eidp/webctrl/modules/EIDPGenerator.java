/*
 * EIDPGenerator.java
 *
 * Created on June 18, 2004, 9:01 AM
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
import java.util.StringTokenizer ;
import java.util.regex.Pattern ;
import java.util.regex.Matcher ;
import org.xml.sax.SAXException;

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

public class EIDPGenerator {
    
    public void SecondaryKey( String keyListName , String listValue , String listShow , String keyValue , String keyValueShow , PrintWriter printWriter , UserScopeObject uso ) throws java.rmi.RemoteException , java.sql.SQLException {
        String[] listShowArray = listShow.split( "," ) ;
        listShow = listShowArray[0] ;
        Vector keyList = new Vector() ;
        keyList = (Vector)uso.eidpWebAppCache.sessionData_get( keyListName ) ;
        String groupID = (String)uso.eidpWebAppCache.sessionData_get( "groupID" ) ;
        String sessionModuleString = (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ;
        if( groupID != null && !groupID.equals("") ){
            sessionModuleString =  sessionModuleString + ";" + groupID;
        }
        printWriter.println( "<td align=\"left\" valign=\"top\" class=\"white\"> " ) ;
        printWriter.println( "<form name=\"changeSecondaryKey\" method=\"POST\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller\"> " ) ;
        printWriter.println( "<input type=\"hidden\" name=\"module\" value=\"" + uso.eidpWebAppCache.sessionData_get( "module" ) + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + sessionModuleString + ";show\"> " ) ;
        printWriter.println( "<i>" + uso.eidpWebAppCache.sessionData_get( "SecondaryKeyLabel" ) + ":&nbsp;</i><select name=\"SecondaryKey\" onChange=\"javascript:document.changeSecondaryKey.submit();\" > " ) ;
        for ( int li = 0 ; li < keyList.size() ; li++ ) {
            String checkedFlag = "" ;
            if ( ((String)((HashMap)keyList.get( li )).get( listValue )).equals( (String)uso.eidpWebAppCache.sessionData_get( keyValue ) ) ) {
                uso.eidpWebAppCache.sessionData_remove( keyValueShow ) ;
                uso.eidpWebAppCache.sessionData_set( keyValueShow ,  ((HashMap)keyList.get( li )).get( listShow ) ) ;
                checkedFlag = "selected" ;
            }
            String combinedShow = "" ;
            for ( int lsi = 0 ; lsi < listShowArray.length ; lsi++ ) {
                String strNextListEntry = (String)((HashMap)keyList.get( li )).get( listShowArray[ lsi ] ) ;
                String strDateFormat = (String)uso.eidpWebAppCache.sessionData_get( "DateFormat" );
                if( !strDateFormat.equals( "" ) ){
                    if( strDateFormat.equals( "german" ) ){
                        if( this.isISODate( strNextListEntry ) ){
                            strNextListEntry = convertISODateToEUDate(strNextListEntry); 
                        }
                    }
                    if( strDateFormat.equals( "american" ) ) { 
                        if ( this.isISODate( strNextListEntry ) ) {
                            strNextListEntry = convertISODateToUSDate(strNextListEntry);
                        }
                    }
                }
                combinedShow += strNextListEntry + " " ;
            }
            printWriter.println( "<option value=\"" + (String)((HashMap)keyList.get( li )).get( listValue ) + "\" " + checkedFlag + ">" + combinedShow + "</option>" ) ;
        }
        printWriter.print( "</select> " ) ;
        printWriter.print( "<a href=\"javascript:CreateNewX( 'changeSecondaryKey' , 'SecondaryKey' , 'XMLDispatcher;NewSecondaryKey' );\"><img src=\"/EIDPWebApp/images/new.jpg\" border=\"0\" alt=\"Enter new Secondary Key.\"></a>" ) ;
        printWriter.println( " </form></td> " ) ;
        printWriter.println( " <script language=\"JavaScript\"> " ) ;
        printWriter.println( "  function changeSecondaryKey_SecondaryKey_setOption( fieldValue , fieldHiddenValue ) { " ) ;
        printWriter.println( "      NewOption = new Option( fieldValue , fieldHiddenValue , false , true ) ; " ) ;
        printWriter.println( "      document.changeSecondaryKey.SecondaryKey.options[document.changeSecondaryKey.SecondaryKey.length] = NewOption ; " ) ;
//        printWriter.println( "      parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + uso.eidpWebAppCache.sessionData_get( "module" ) + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) + ";show'; " ) ;
        printWriter.println("       document.changeSecondaryKey.submit();");
        printWriter.println( "  } " ) ;
        printWriter.println( " </script> " ) ;
        printWriter.println( " </tr></table> " ) ;
        printWriter.println( "<hr> " ) ;
    }
    
    public void CloseModule( PrintWriter printWriter , UserScopeObject uso ) throws java.rmi.RemoteException {
        String actualModule = (String)uso.eidpWebAppCache.sessionData_get( "module" ) ;
        actualModule += ";" + "controller" ;
        actualModule += ";" + (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ;
        uso.eidpWebAppCache.sidePanelEntry_set( actualModule ) ;
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        printWriter.println( "  <!--") ;
        printWriter.println( "      if ( this.name == 'Data' ) { " ) ;
        printWriter.println( "          parent.SidePanel.location.href=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show\"; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "  // -->") ;
        printWriter.println( "  </script> " ) ;
        printWriter.println( " </body> " ) ;
        printWriter.println( "</html>" ) ;
        printWriter.close() ;
    }
    
    /** Creates a new instance of TwGenerator */
    public EIDPGenerator( String moduleLabel , PrintWriter printWriter , UserScopeObject uso ) {
        printWriter.println( "</head>" ) ;
        printWriter.println( "<body>" ) ;
        try{
            String strHelpAni = (String)uso.eidpWebAppCache.sessionData_get( "AnimatedHelp" ) ;
            if(strHelpAni != null && strHelpAni.toLowerCase().equals("yes")){
                printWriter.println( "<div name=\"warnarzt\" id=\"warnarzt\" style=\"cursor: pointer; position: absolute; top: 100px; left: 0px; width: 0px; height: 0px; background-color: transparent; visibility: hidden;\">" ) ;
                printWriter.println( "<img title=\"click to close\" height=\"130\" id='warnimg' onclick=\"javascript:killWarnarzt();\" src=\"/EIDPWebApp/images/WarnArzt/info_doctor30.gif\" border=\"0\">" ) ;
                printWriter.println( "<div align=\"center\" id=\"warnarztinfo\" style=\"position: absolute; top: -85px; left: 60px; width: 0; height: 0; background-color: '#FF8000'; visibility: hidden;\">" ) ;
                printWriter.println( "<iframe style=\"border: solid; background-color: '#FF8000'; font: 10px Arial, Helvetica, sans-serif;\" id=\"warnarztinfoarea\" height=\"100\" width=\"250\"></iframe> </div>" ) ;
                printWriter.println( "</div>");
            }
        }catch(java.rmi.RemoteException e){
            System.out.println("RemoteException thrown in TwGenerator (AnimatedHelp)");
        }
        String applicationLogo = new String();
        try {
            if (!uso.xmlApplication.getElementsByName("application-data,app-logo").isEmpty()) {
                applicationLogo = (String)((Vector)uso.xmlApplication.getElementsByName( "application-data,app-logo" )).get( 0 ) ;
            } else {
                applicationLogo = "eidp_logo.jpg";
            }
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
        printWriter.println( "  <table border=\"0\" width=\"100%\"><tr><td align=\"left\" valign=\"top\" class=\"white\" width=\"*\"> " ) ;
        //printWriter.println( "    <nobr> " ) ;
        //printWriter.println( "<img src=\"/EIDPWebApp/images/" + uso.applicationContext + ".jpg\" border=\"0\" width=\"40\" height=\"20\"><font size=\"+1\">&nbsp;&nbsp;<b>" + moduleLabel + "</b></font>" ) ;
        printWriter.println( "    <img src=\"/EIDPWebApp/images/" + applicationLogo + "\" alt=\"eidp_logo\" border=\"0\" width=\"40\" height=\"20\"><font size=\"+1\">&nbsp;&nbsp;<b>" + moduleLabel + "</b></font>" ) ;
    }
    
    // Helper-Function : Stephan
    protected boolean isISODate( String strValue ){
        if( strValue.trim().length() == 10 ){
            Pattern p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
            Matcher m = p.matcher( strValue.trim() );
            if( m.find() ){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    
    // Helper-Function : Stephan
    protected String convertISODateToEUDate( String strValue ){
        StringTokenizer strto = new StringTokenizer(strValue,"-");
        String year = strto.nextToken();
        String month = strto.nextToken();
        String day = strto.nextToken();
        return day + "." + month + "." + year;
    }
    
    //Helper-Function : David
    protected String convertISODateToUSDate( String strValue ){
        StringTokenizer strto = new StringTokenizer(strValue,"-");
        String year = strto.nextToken();
        String month = strto.nextToken();
        String day = strto.nextToken();
        return month + "/" + day + "/" + year;
    }
    
}
