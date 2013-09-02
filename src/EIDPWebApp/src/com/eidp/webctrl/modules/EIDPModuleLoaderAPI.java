/*
 * EIDPModuleLoaderAPI.java
 *
 * Created on May 27, 2004, 9:27 AM
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

public abstract class EIDPModuleLoaderAPI {
    
    /** Creates a new instance of EIDPModuleLoaderAPI */
    public void EIDPModuleLoaderINIT( String loadAddOnClass , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws java.rmi.RemoteException , java.io.IOException , java.sql.SQLException , org.xml.sax.SAXException {
        // initHTML sets the TOOLWERK layout and calls EIDPScripts
        // User-specific functionality is in EIDPScripts and from <body> to </body>
        // <body> and </body> have both to be managed by the user class
        uso.dbMapper = (DBMappingRemote)((Handle)uso.session.getAttribute( "dbMapperHandle" )).getEJBObject() ;
        uso.eidpWebAppCache = (EIDPWebAppCacheRemote)((Handle)uso.session.getAttribute( "eidpWebAppCacheHandle" )).getEJBObject() ;
        PrintWriter printWriter = this.initHTML( request , response , loadAddOnClass ) ;
        EIDPLoadClass( loadAddOnClass , printWriter , request , response , uso ) ;
        // closeHTML only closes <html>
        this.closeHTML( printWriter ) ;
    }
    
    public abstract void EIDPLoadClass( String loadAddOnClass , PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso )  ;
    
    public abstract void EIDPScripts( PrintWriter printWriter ) ;
    
    private PrintWriter initHTML( HttpServletRequest request , HttpServletResponse response , String loadAddOnClass ) throws java.io.IOException {
        response.setContentType( "text/html" ) ;
        PrintWriter printWriter = response.getWriter() ;
        boolean boolJavaScript = true ;
        if( loadAddOnClass.trim().equals("SearchModule") || loadAddOnClass.trim().equals("TransplantSearchModule") ){
            boolJavaScript = false ;
        }
        if( !loadAddOnClass.trim().equals("SQLQueryFile") ){
            printWriter.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"");
            printWriter.println("   http://www.w3.org/TR/html4/loose.dtd\">");
            printWriter.println( "<html>");
            printWriter.println( "<head>");
            printWriter.println( "  <meta http-equiv=\"pragma\" content=\"no-cache\"> " ) ;
            printWriter.println( "  <meta http-equiv=\"expires\" content=\"0\"> " ) ;
            printWriter.println( "  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >");
            printWriter.println( "  <title>EIDP AddOn</title> " ) ;
            if(boolJavaScript){
                printWriter.println( "  <style type=\"text/css\"> " ) ;
                printWriter.println( "  <!-- " ) ;
                printWriter.println( "      body { font-family:Arial,sans-serif; color:black; font-size:10pt ; } " ) ;
                printWriter.println( "      a:link { text-decoration:none; color:black; } " ) ;
                printWriter.println( "      a:visited { text-decoration:none; color:black ; } " ) ;
                printWriter.println( "      a:hover { text-decoration:none; color:yellow ; } " ) ;
                printWriter.println( "      a:active { text-decoration:none; color:black ; } " ) ;
                // printWriter.println( "  table { border-style:solid;border-color:#333333;border-width:2px;border-spacing:1px ; } " ) ;
                // printWriter.println( "  td { background-color:#DDDDDD;color:#000000 ; font-size:10pt ; } " ) ;
                printWriter.println( "      td.label { background-color:#DDDDDD;color:#000000 ; font-size:10pt ; } " ) ;
                printWriter.println( "      td.inputNEW { background-color:#EEEEEE;color:#000000 ; font-size:10pt ; } " ) ;
                printWriter.println( "      td.input { background-color:#CCCCCC;color:#000000 ; font-size:10pt ; } " ) ;
                printWriter.println( "      td.inputContrast { background-color:#999999;color:#000000 ; font-size:10pt ; } " ) ;
                printWriter.println( "      td.white { background-color:#FFFFFF;color:#000000 ; font-size:10pt ; } " ) ;
                printWriter.println( "  --> " ) ;
                printWriter.println( "  </style>" ) ;
                // Calendar // David
                printWriter.println("   <link rel=\"stylesheet\" type=\"text/css\" href=\"/EIDPWebApp/stylesheets/calendar.css\">");
                printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/CalendarPopup.js\"></script>");
                printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/date.js\"></script>");
                printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/AnchorPosition.js\"></script>");
                printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/PopupWindow.js\"></script>");
                //
                printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
                printWriter.println( "  <!--" ) ;
                printWriter.println( "      var userChanges = 0 ; " ) ;
                printWriter.println( "      var exceptionMap = new Array() ; " ) ;
                printWriter.println( "      var commaSeparation = \", \" ; " ) ;
                printWriter.println( "      history.forward(-1) ; " ) ;
                
                printWriter.println( "      function CreateNewX( formName , fieldName , Module ) {" ) ;
                printWriter.println( "          url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + Module + \";show;&formName=\" + formName + \"&formField=\" + fieldName ; " ) ;
                printWriter.println( "          windowOpenFeatures = \"height=375,width=475,scrollbars=yes\" ; " ) ;
                printWriter.println( "          windowName= \"NEWXWINDOW\" ; " ) ;
                printWriter.println( "          var nwindow = window.open( url,windowName,windowOpenFeatures ) ; " ) ;
                printWriter.println( "          nwindow.focus() ; " ) ;
                printWriter.println( "      } " ) ;
                printWriter.println( "      function TOOLTip( TOOLTIP ) {" ) ;
                printWriter.println( "          url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=TOOLTip;\" + TOOLTIP + \";show\" ; " ) ;
                printWriter.println( "          windowOpenFeatures = \"height=175,width=450,scrollbars=yes\" ; " ) ;
                printWriter.println( "          windowName= \"TOOLTIP\" ; " ) ;
                printWriter.println( "          var nwindow = window.open( url,windowName,windowOpenFeatures ) ; " ) ;
                printWriter.println( "          nwindow.focus() ; " ) ;
                printWriter.println( "      } " ) ;

                printWriter.println( "      // set global help-variables" ) ;
                printWriter.println( "      var helpAvailable=true;" ) ;
                printWriter.println( "      var helpActivated=0;" ) ;
                printWriter.println( "      var xpos=0;" ) ;
                printWriter.println( "      var ypos=0;" ) ;
                printWriter.println( "      var isNav = (navigator.appName.indexOf(\"Netscape\") !=-1);" ) ;
                printWriter.println( "      function getFrameSize(frameID) {" ) ;
                printWriter.println( "          var result = {height:0, width:0};" ) ;
                printWriter.println( "          if (document.getElementById) {" ) ;
                printWriter.println( "              var frame = parent.document.getElementById(frameID);" ) ;
                printWriter.println( "              if (frame.scrollWidth) {" ) ;
                printWriter.println( "                  result.height = frame.scrollHeight;" ) ;
                printWriter.println( "                  result.width = frame.scrollWidth;" ) ;
                printWriter.println( "              }" ) ;
                printWriter.println( "          }" ) ;
                printWriter.println( "          return result;" ) ;
                printWriter.println( "      }" ) ;
                printWriter.println( "      function handlerMM(e){" ) ;
                printWriter.println( "      // Plazierung neben Mauszeiger" ) ;
                printWriter.println( "          xpos = (isNav) ? e.pageX : event.clientX + document.body.scrollLeft;" ) ;
                printWriter.println( "          ypos = (isNav) ? e.pageY : event.clientY + document.body.scrollTop;" ) ;
                printWriter.println( "          var frameWidth = getFrameSize('Data').width;" ) ;
                // damit der Dokta sich nich den Schaedel anstoesst
                printWriter.println( "          if (ypos-90<0)" ) ;
                printWriter.println( "          ypos=90;" ) ;
                printWriter.println( "          else" ) ;
                printWriter.println( "          ypos-=5;" ) ;
                // damit der Dokta nich ausm Fenster faellt
                printWriter.println( "          if (xpos+300>frameWidth)" ) ;
                printWriter.println( "          xpos=frameWidth-330;" ) ;
                printWriter.println( "          else" ) ;
                printWriter.println( "          xpos+=15;" ) ;
                printWriter.println( "          if (xpos<0)" ) ;
                printWriter.println( "          xpos=0;" ) ;
                printWriter.println( "      }" ) ;
                printWriter.println( "      if (isNav){document.captureEvents(Event.MOUSEMOVE);}" ) ;
                printWriter.println( "      document.onmousemove = handlerMM;" ) ;
                
                printWriter.println( "      function warnarztRise(imgname,infotext){" ) ;
                printWriter.println( "          var showWarnarzt=false;" ) ;
                printWriter.println( "          try{" ) ;
                printWriter.println( "              if(parent.TopPanel.animation==1)" ) ;
                printWriter.println( "              showWarnarzt=true;" ) ;
                printWriter.println( "          }catch(e){" ) ;
                printWriter.println( "              if(opener.parent.TopPanel.animation==1)" ) ;
                printWriter.println( "              showWarnarzt=true;" ) ;
                printWriter.println( "          }" ) ;
                printWriter.println( "          if(showWarnarzt) {" ) ;
                printWriter.println( "          if (document.layers) {navigator.family = \"nn4\"}" ) ;
                printWriter.println( "          if (document.all) {navigator.family = \"ie4\"}" ) ;
                printWriter.println( "          if (window.navigator.userAgent.toLowerCase().match(\"gecko\")) {navigator.family = \"gecko\"}" ) ;
                printWriter.println( "          if(navigator.family ==\"nn4\") {" ) ;
                printWriter.println( "              document.warnarzt.left=xpos;" ) ;
                printWriter.println( "              document.warnarzt.top=ypos;" ) ;
                printWriter.println( "          }" ) ;
                printWriter.println( "          else if(navigator.family ==\"ie4\"){" ) ;
                printWriter.println( "              warnarzt.style.pixelLeft=xpos;" ) ;
                printWriter.println( "              warnarzt.style.pixelTop=ypos;" ) ;
                printWriter.println( "          }" ) ;
                printWriter.println( "          else if(navigator.family ==\"gecko\"){" ) ;
                printWriter.println( "              document.getElementById(\"warnarzt\").style.left=xpos;" ) ;
                printWriter.println( "              document.getElementById(\"warnarzt\").style.top=ypos;" ) ;
                printWriter.println( "          }" ) ;
                printWriter.println( "              startWarnarzt(imgname,infotext);" ) ;
                printWriter.println( "          }else{" ) ;
                printWriter.println( "              TOOLTip(infotext);" ) ;
                printWriter.println( "          }" ) ;
                printWriter.println( "      }" ) ;
                printWriter.println( "      function killWarnarzt(){" ) ;
                printWriter.println( "          document.getElementById('warnarzt').style.visibility=\"hidden\";" ) ;
                printWriter.println( "          document.getElementById('warnarztinfo').style.visibility=\"hidden\";" ) ;
                printWriter.println( "      }" ) ;
                printWriter.println( "      function startWarnarzt(imgname,infotext){" ) ;
                printWriter.println( "          document.getElementById('warnimg').src=\"/EIDPWebApp/images/WarnArzt/\"+imgname;" ) ;
                printWriter.println( "          document.getElementById('warnarztinfoarea').src=\"javascript:document.open();document.write('<html><head><title>ToolTip<\\/title><\\/head><body bgcolor=#FF8000><font face=Arial,Helvetica,sans-serif>\" + infotext + \"<\\/font><\\/body><\\/html>');document.close();\";" ) ;
                printWriter.println( "          document.getElementById('warnarzt').style.visibility=\"visible\";" ) ;
                printWriter.println( "          document.getElementById('warnarztinfo').style.visibility=\"visible\";" ) ;
                printWriter.println( "      }" ) ;
                printWriter.println( "  // -->") ;
                printWriter.println( "  </script> " ) ;
            }
        }
        
        this.EIDPScripts( printWriter ) ;
        return printWriter ;
    }
    
    private void closeHTML( PrintWriter printWriter ) {
        printWriter.println( "</html> " ) ;
    }
    
}
