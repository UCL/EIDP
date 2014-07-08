/*
 * xmlDispatcher.java
 *
 * Created on October 21, 2003, 10:11 AM
 */

package com.eidp.webctrl;

import com.eidp.core.DB.DBMapping;
import java.io.IOException;
import java.io.PrintWriter;
import com.eidp.webctrl.modules.EIDPAddInLoader;
import com.eidp.xml.XMLDataAccess;
import com.eidp.UserScopeObject.UserScopeObject ;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.NodeList;
import java.util.HashMap ;
import java.util.Vector ;
import java.util.Set ;
import java.util.Iterator ;
import java.util.StringTokenizer;
import java.util.Date ;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.xml.sax.SAXException;
import java.util.regex.Pattern ;
import java.util.regex.Matcher ;

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
public class XMLDispatcher {
    
    final static String strErrorBackgroundColor = "#FF9933";
    final static String strMandatoryBackgroundColor = "#FF2233";
    private boolean isGerman = false;
    
    public XMLDispatcher(HttpServletRequest request, HttpServletResponse response, UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException {
        try {
            String strLanguage = (String)uso.eidpWebAppCache.sessionData_get( "Language" ) ;
            if(strLanguage != null && strLanguage.equals("german")){
                isGerman = true;
            }
            processRequest(request, response , uso );
        } catch ( org.xml.sax.SAXException saxe ) {
            throw new javax.servlet.ServletException( saxe ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException pce ) {
            throw new javax.servlet.ServletException( pce ) ;
        } catch ( java.sql.SQLException se ) {
            throw new javax.servlet.ServletException( se ) ;
        }
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        //        uso.session = request.getSession() ;
        //        uso.sessionData = (HashMap)uso.session.getAttribute( "sessionData" ) ;
        //        uso.applicationContext = (String)uso.eidpWebAppCache.sessionData_get( "applicationContext" ) ;
        //        uso.userLogin = (String)uso.eidpWebAppCache.sessionData_get( "userLogin" ) ;
        //        uso.userID = (String)uso.eidpWebAppCache.sessionData_get( "userID" ) ;
        uso.userCenter = (String)uso.eidpWebAppCache.sessionData_get( "userCenter" ) ;
        //        uso.userRoles = (Vector)uso.session.getAttribute( "userRoles" ) ;
        //        uso.centerRoles = (HashMap)uso.session.getAttribute( "centerRoles" ) ;
        //synchronized( this.session.getId() ) {
        uso.dbMapper = (DBMapping)uso.session.getAttribute( "dbMapperHandle" ) ;
        // this.dbMapper.applicationConnect( this.applicationContext ) ;
        //}
        // get controller.xml
        String moduleXMLFile = (String)uso.eidpWebAppCache.sessionData_get( "xmlFile" ) ;
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/" + moduleXMLFile + ".xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        NodeList TopLevelControllerNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "web-controller" )).get( 0 ) ;
        String colorFile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/colorcodes.xml" ;
        uso.xmlColorAccess = new XMLDataAccess( colorFile ) ;
        NodeList TopLevelColorNode = (NodeList)((Vector)uso.xmlColorAccess.getNodeListsByName( "colors" )).get( 0 ) ;
        uso.colorMap = this.getColorMap( TopLevelColorNode , uso ) ;
        // Filters only if methodAction = show
        if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "show" ) ) {
            String filterfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/filters.xml" ;
            uso.xmlFilterAccess = new XMLDataAccess( filterfile ) ;
            System.out.println("---7");
            uso.Filters = (Vector)uso.xmlFilterAccess.getNodeListsByName( "filter" ) ;
            uso.FilterNames = (Vector)uso.xmlFilterAccess.getElementsByName( "filter,name" ) ;
            if ( uso.Filters.size() != uso.FilterNames.size() ) {
                throw new org.xml.sax.SAXException( "Exception thrown while parsing filters.xml. filter names do not match filters in number." ) ;
            }
        }
        // goto XMLModule Processing
        this.xmlModuleProcessing( request , response , TopLevelControllerNode , uso ) ;
    }
    
    // All method-stuff...
    
    // ===================
    // additional Methods:
    
    protected boolean checkPermissions( Vector rolePermissions , UserScopeObject uso ) throws java.rmi.RemoteException {
        for ( int i = 0 ; i < uso.eidpWebAppCache.userRoles_size() ; i++ ) {
            if ( rolePermissions.contains( (String)uso.eidpWebAppCache.userRoles_get( i ) ) ) {
                // CENTER_PREMISSIONS
                // Jetzt muss gecheckt werden, ob der Patient in einem Zentrum ist,
                // in dem der Benutzer RW-Berechtigung hat
                try {
                    HashMap centers = uso.eidpWebAppCache.centerRoles_getAll();
                    String patientCenter = (String)uso.eidpWebAppCache.sessionData_get("PkCenter");
                    System.out.println("----centerRoles: "+centers.toString() + 
                            " ----V PAT_CENTER: " + patientCenter);
                    //System.out.println("----V PAT_CENTER: "+patientCenter);
                    // Wenn keine rw Berechtigung da ist, dann FALSE!
                    if (!((String)centers.get(patientCenter)).equals("rw")) return false;
                } catch (java.lang.NullPointerException e) {
                    // Nothing to be done, since not implemented in the
                    // db.xml .
                }
                return true ;
            }
        }
        throw new java.security.AccessControlException( "You are not authorized to access the requested function. Please call the administrator for details." ) ;
    }
    
    protected HashMap getColorMap(NodeList TopLevelColorNode, UserScopeObject uso) throws org.xml.sax.SAXException {
        // 1. get color defintions:
        Vector ColorCodeLabels = (Vector)uso.xmlColorAccess.getElementsByName( "code,id" , TopLevelColorNode ) ;
        Vector ColorCodeColors = (Vector)uso.xmlColorAccess.getElementsByName( "code,color" , TopLevelColorNode ) ;
        if ( ColorCodeLabels.size() != ColorCodeColors.size() ) {
            throw new org.xml.sax.SAXException( "XMLDispatcher throws SAXException: ColorCode IDs and Colors do not match." ) ;
        }
        // build HashMap of Colors:
        HashMap colorCodeMap = new HashMap() ;
        for ( int i = 0 ; i < ColorCodeLabels.size() ; i++ ) {
            colorCodeMap.put( ColorCodeLabels.get( i ) , ColorCodeColors.get( i ) ) ;
        }
        // build form/label->color map:
        HashMap FormLabelColorMap = new HashMap() ;
        Vector ColorViews = (Vector)uso.xmlColorAccess.getElementsByName( "color-filter,view" , TopLevelColorNode ) ;
        Vector ColorLabels = (Vector)uso.xmlColorAccess.getElementsByName( "color-filter,label" , TopLevelColorNode ) ;
        Vector ColorColorCodes = (Vector)uso.xmlColorAccess.getElementsByName( "color-filter,color-code" , TopLevelColorNode ) ;
        if ( ColorViews.size() != ColorLabels.size() && ColorLabels.size() != ColorColorCodes.size() ) {
            throw new org.xml.sax.SAXException( "XMLDispatcher throws SAXException: Color-Code definitions do not match." ) ;
        }
        // HashMap:
        for ( int i = 0 ; i < ColorViews.size() ; i++ ) {
            String FormLabel = ColorViews.get( i ) + "/" + ColorLabels.get( i ) ;
            FormLabelColorMap.put( FormLabel , (String)colorCodeMap.get( (String)ColorColorCodes.get( i ) ) ) ;
        }
        return FormLabelColorMap ;
    }
    
    // ===============================
    // TOOLTip Window Opener
    
    protected void createToolTipWindowOpener( PrintWriter printWriter ) throws org.xml.sax.SAXException {
        printWriter.println( "  function TOOLTip( TOOLTIP ) {" ) ;
        printWriter.println( "      var showWarnarzt=false;" ) ;
        printWriter.println( "      try{" ) ;
        printWriter.println( "          if(parent.TopPanel.animation==1)" ) ;
        printWriter.println( "              showWarnarzt=true;" ) ;
        printWriter.println( "      }catch(e){" ) ;
        printWriter.println( "          if(opener.parent.TopPanel.animation==1)" ) ;
        printWriter.println( "              showWarnarzt=true;" ) ;
        printWriter.println( "      }" ) ;
        printWriter.println( "      if(showWarnarzt){" ) ;
        printWriter.println( "          warnarztRise('info_doctor.gif',TOOLTIP);" ) ;
        printWriter.println( "      }else{" ) ;
        printWriter.println( "          url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=TOOLTip;\" + TOOLTIP + \";show\" ; " ) ;
        printWriter.println( "          windowOpenFeatures = \"height=175,width=450,scrollbars=yes\" ; " ) ;
        printWriter.println( "          windowName= \"TOOLTIP\" ; " ) ;
        printWriter.println( "          var nwindow = window.open( url,windowName,windowOpenFeatures ) ; " ) ;
        printWriter.println( "          nwindow.focus() ; " ) ;
        printWriter.println( "      }" ) ;
        printWriter.println( "  } " ) ;
    }
    
    // ===============================
    // AutoLogout //Stephan
    
    protected void createAutoLogout( PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException , java.rmi.RemoteException {
        String autologoutinterval = (String)uso.eidpWebAppCache.sessionData_get( "auto-logout-interval" );
        if(autologoutinterval != null && !autologoutinterval.equals("")){
            printWriter.println( "  function autoLogout(){" ) ;
            printWriter.println( "      if(activeView != \"(null)\"){" ) ;
            printWriter.println( "          eval(\"document.\" + activeView + \".autologout.value='true'\");" ) ;
            printWriter.println( "          if(!eval(activeView + '_CheckAndSubmit()')){" ) ;
            printWriter.println( "              var url=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;logout;show&autologout=true\";" ) ;
            printWriter.println( "              top.location.href=url;" ) ;
            printWriter.println( "          }" ) ;
            printWriter.println( "      }else{" ) ;
            printWriter.println( "          var url=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;logout;show&autologout=true\";" ) ;
            printWriter.println( "          top.location.href=url;" ) ;
            printWriter.println( "      }" ) ;
            printWriter.println( "  }" ) ;
            // Auto-logout nach der in der application.xml definierten Anzahl von Minuten
            printWriter.println( "  window.setTimeout(\"autoLogout()\", " + autologoutinterval + "*60000);" ) ;
        }
    }

    protected void createNewXWindowOpener( PrintWriter printWriter ) throws org.xml.sax.SAXException {
        printWriter.println( "  // there is no way back... " ) ;
        printWriter.println( "  history.forward(-1);" ) ;
        printWriter.println( "   " ) ;
        printWriter.println( "  function CreateNewX( formName , fieldName , Module ) {" ) ;
        printWriter.println( "      url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + Module + \";show;&formName=\" + formName + \"&formField=\" + fieldName ; " ) ;
        printWriter.println( "      windowOpenFeatures = \"height=375,width=475,scrollbars=yes\" ; " ) ;
        printWriter.println( "      windowName= \"NEWXWINDOW\" ; " ) ;
        printWriter.println( "      var nwindow = window.open( url,windowName,windowOpenFeatures ) ; " ) ;
        printWriter.println( "      nwindow.focus() ; " ) ;
        printWriter.println( "  } " ) ;
    }
    
    // for TextAreas -> Max-Input-Size // Stephan
    protected void createTextAreaMaxInputFilter( PrintWriter printWriter ) throws org.xml.sax.SAXException {
        printWriter.println( "function checkMaxInput(formName,fieldName,fieldMaxSize,filterMessage)" ) ;
        printWriter.println( "{" ) ;
        printWriter.println( "    exception = 0 ; " ) ;
        printWriter.println( "    var len = document.forms[formName].elements[fieldName].value.length;" ) ;
        printWriter.println( "    var exceptionMsg = \"Exception occured during Check:\" ; " ) ;
        printWriter.println( "    if (len > fieldMaxSize){ " ) ;
        printWriter.println( "       exceptionMsg = exceptionMsg + \"\\nValue exceeds permitted size.\" ;" ) ;
        printWriter.println( "       exception = 1 ;" ) ;
        printWriter.println( "    }" ) ;
        printWriter.println( "    if ( exception == 1 ) { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 1 ; " ) ;
        printWriter.println( "      if ( filterMessage == '' ) { " ) ;
        printWriter.println( "          alert ( exceptionMsg ) ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "          alert ( filterMessage ) ; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"" + strErrorBackgroundColor + "\"; " ) ;
        printWriter.println( "  } else { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 0 ; " ) ;
        printWriter.println( "      if( document.forms[formName].elements[fieldName].disabled == false  ) " ) ;
        printWriter.println( "          document.forms[formName].elements[fieldName].style.backgroundColor = \"#FFFFFF\"; " ) ;
        printWriter.println( "  } " ) ;
        printWriter.println( "} " ) ;
    }
    
    // for RichTextAreas  // Stephan
    protected void createRichTextAreaFunctions( PrintWriter printWriter ) throws org.xml.sax.SAXException {
        printWriter.println( "var command = \"\";" ) ;
        printWriter.println( "var richtextareas = new Array();" ) ;
        printWriter.println( "function InitToolbarButtons() {" ) ;
        printWriter.println( "  var kids = document.getElementsByTagName('DIV');" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  for (var i=0; i < kids.length; i++) {" ) ;
        printWriter.println( "    var clName=kids[i].className;" ) ;
        printWriter.println( "    clName=clName.substring(0,clName.indexOf('_'));" ) ;
        printWriter.println( "    if ( clName == \"imagebutton\" ) {" ) ;
        printWriter.println( "      kids[i].onmouseover = tbmouseover;" ) ;
        printWriter.println( "      kids[i].onmouseout = tbmouseout;" ) ;
        printWriter.println( "      kids[i].onmousedown = tbmousedown;" ) ;
        printWriter.println( "      kids[i].onmouseup = tbmouseup;" ) ;
        printWriter.println( "      kids[i].onclick = tbclick;" ) ;
        printWriter.println( "    }" ) ;
        printWriter.println( "  }" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function tbmousedown(e)" ) ;
        printWriter.println( "{" ) ;
        printWriter.println( "  this.firstChild.style.left = 2;" ) ;
        printWriter.println( "  this.firstChild.style.top = 2;" ) ;
        printWriter.println( "  this.style.border=\"inset 2px\";" ) ;
        printWriter.println( "  e.preventDefault();" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function tbmouseup()" ) ;
        printWriter.println( "{" ) ;
        printWriter.println( "  this.firstChild.style.left = 1;" ) ;
        printWriter.println( "  this.firstChild.style.top = 1;" ) ;
        printWriter.println( "  this.style.border=\"outset 2px\";" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function tbmouseout()" ) ;
        printWriter.println( "{" ) ;
        printWriter.println( "  this.style.border=\"solid 2px #C0C0C0\";" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function tbmouseover()" ) ;
        printWriter.println( "{" ) ;
        printWriter.println( "  this.style.border=\"outset 2px\";" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  function insertNodeAtSelection(win, insertNode)" ) ;
        printWriter.println( "  {" ) ;
        printWriter.println( "      // get current selection" ) ;
        printWriter.println( "      var sel = win.getSelection();" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      // get the first range of the selection" ) ;
        printWriter.println( "      // (there's almost always only one range)" ) ;
        printWriter.println( "      var range = sel.getRangeAt(0);" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      // deselect everything" ) ;
        printWriter.println( "      sel.removeAllRanges();" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      // remove content of current selection from document" ) ;
        printWriter.println( "      range.deleteContents();" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      // get location of current selection" ) ;
        printWriter.println( "      var container = range.startContainer;" ) ;
        printWriter.println( "      var pos = range.startOffset;" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      // make a new range for the new selection" ) ;
        printWriter.println( "      range=document.createRange();" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      if (container.nodeType==3 && insertNode.nodeType==3) {" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "        // if we insert text in a textnode, do optimized insertion" ) ;
        printWriter.println( "        container.insertData(pos, insertNode.nodeValue);" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "        // put cursor after inserted text" ) ;
        printWriter.println( "        range.setEnd(container, pos+insertNode.length);" ) ;
        printWriter.println( "        range.setStart(container, pos+insertNode.length);" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      } else {" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "        var afterNode;" ) ;
        printWriter.println( "        if (container.nodeType==3) {" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "          // when inserting into a textnode" ) ;
        printWriter.println( "          // we create 2 new textnodes" ) ;
        printWriter.println( "          // and put the insertNode in between" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "          var textNode = container;" ) ;
        printWriter.println( "          container = textNode.parentNode;" ) ;
        printWriter.println( "          var text = textNode.nodeValue;" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "          // text before the split" ) ;
        printWriter.println( "          var textBefore = text.substr(0,pos);" ) ;
        printWriter.println( "          // text after the split" ) ;
        printWriter.println( "          var textAfter = text.substr(pos);" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "          var beforeNode = document.createTextNode(textBefore);" ) ;
        printWriter.println( "          afterNode = document.createTextNode(textAfter);" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "          // insert the 3 new nodes before the old one" ) ;
        printWriter.println( "          container.insertBefore(afterNode, textNode);" ) ;
        printWriter.println( "          container.insertBefore(insertNode, afterNode);" ) ;
        printWriter.println( "          container.insertBefore(beforeNode, insertNode);" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "          // remove the old node" ) ;
        printWriter.println( "          container.removeChild(textNode);" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "        } else {" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "          // else simply insert the node" ) ;
        printWriter.println( "          afterNode = container.childNodes[pos];" ) ;
        printWriter.println( "          container.insertBefore(insertNode, afterNode);" ) ;
        printWriter.println( "        }" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "        range.setEnd(afterNode, 0);" ) ;
        printWriter.println( "        range.setStart(afterNode, 0);" ) ;
        printWriter.println( "      }" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "      sel.addRange(range);" ) ;
        printWriter.println( "  };" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function getOffsetTop(elm) {" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  var mOffsetTop = elm.offsetTop;" ) ;
        printWriter.println( "  var mOffsetParent = elm.offsetParent;" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  while(mOffsetParent){" ) ;
        printWriter.println( "    mOffsetTop += mOffsetParent.offsetTop;" ) ;
        printWriter.println( "    mOffsetParent = mOffsetParent.offsetParent;" ) ;
        printWriter.println( "  }" ) ;
        printWriter.println( " " ) ;
        printWriter.println( "  return mOffsetTop;" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function getOffsetLeft(elm) {" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  var mOffsetLeft = elm.offsetLeft;" ) ;
        printWriter.println( "  var mOffsetParent = elm.offsetParent;" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  while(mOffsetParent){" ) ;
        printWriter.println( "    mOffsetLeft += mOffsetParent.offsetLeft;" ) ;
        printWriter.println( "    mOffsetParent = mOffsetParent.offsetParent;" ) ;
        printWriter.println( "  }" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  return mOffsetLeft;" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function tbclick()" ) ;
        printWriter.println( "{" ) ;
        printWriter.println( "    var clName=this.className;" ) ;
        printWriter.println( "    clName=clName.substring(clName.indexOf('_')+1,clName.length);" ) ;
        printWriter.println( "    document.getElementById(clName).contentWindow.document.execCommand(this.id, false, null);" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function StartRichTextEditing(iFrameID) {" ) ;
        printWriter.println( "  document.getElementById(iFrameID).contentWindow.document.designMode = \"on\";" ) ;
        printWriter.println( "  try {" ) ;
        printWriter.println( "    document.getElementById(iFrameID).contentWindow.document.execCommand(\"undo\", false, null);" ) ;
        printWriter.println( "  }  catch (e) {" ) ;
        printWriter.println( "    alert(\"This demo is not supported on your level of Mozilla.\");" ) ;
        printWriter.println( "  }" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "  InitToolbarButtons();" ) ;
        printWriter.println( "  document.getElementById(iFrameID).contentWindow.document.execCommand(\"useCSS\", false, true);" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "" ) ;
        printWriter.println( "function LoadRichTextFields() {" ) ;
        printWriter.println( "  for( var i=0 ; i < richtextareas.length ; i++ ){" ) ;
        printWriter.println( "  	StartRichTextEditing(richtextareas[i]);" ) ;
        printWriter.println( "  }" ) ;
        printWriter.println( "}" ) ;
        
    }
    
    // Javascript for WarnArzt  // Stephan
    protected void createWarnArztFunctions( PrintWriter printWriter ) throws org.xml.sax.SAXException {
        printWriter.println( "// set global help-variables" ) ;
        printWriter.println( "var helpAvailable=true;" ) ;
        printWriter.println( "var helpActivated=0;" ) ;
        printWriter.println( "var xpos=0;" ) ;
        printWriter.println( "var ypos=0;" ) ;
        printWriter.println( "var frameWidth=0;" ) ;
        printWriter.println( "var isNav = (navigator.appName.indexOf(\"Netscape\") !=-1);" ) ;
        printWriter.println( "function getFrameSize(frameID) {" ) ;
        printWriter.println( "    var result = {height:0, width:0};" ) ;
        printWriter.println( "    if (document.getElementById) {" ) ;
        printWriter.println( "        var frame = parent.document.getElementById(frameID);" ) ;
        printWriter.println( "        if (frame.scrollWidth) {" ) ;
        printWriter.println( "            result.height = frame.scrollHeight;" ) ;
        printWriter.println( "            result.width = frame.scrollWidth;" ) ;
        printWriter.println( "        }" ) ;
        printWriter.println( "    }" ) ;
        printWriter.println( "    return result;" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "function handlerMM(e){" ) ;
        printWriter.println( "  // Plazierung neben Mauszeiger" ) ;
        printWriter.println( "  xpos = (isNav) ? e.pageX : event.clientX + document.body.scrollLeft;" ) ;
        printWriter.println( "  ypos = (isNav) ? e.pageY : event.clientY + document.body.scrollTop;" ) ;
        printWriter.println( "  frameWidth = getFrameSize('Data').width;" ) ;
        // damit der Dokta sich nich den Schaedel anstoesst
        printWriter.println( "  if (ypos-90<0)" ) ;
        printWriter.println( "      ypos=90;" ) ;
        printWriter.println( "   else" ) ;
        printWriter.println( "      ypos-=5;" ) ;
        // damit der Dokta nich ausm Fenster faellt
        printWriter.println( "  if (xpos+300>frameWidth)" ) ;
        printWriter.println( "      xpos=frameWidth-330;" ) ;
        printWriter.println( "   else" ) ;
        printWriter.println( "      xpos+=15;" ) ;
        printWriter.println( "  if (xpos<0)" ) ;
        printWriter.println( "      xpos=0;" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "if (isNav){document.captureEvents(Event.MOUSEMOVE);}" ) ;
        printWriter.println( "document.onmousemove = handlerMM;" ) ;
        
        printWriter.println( "function warnarztRise(imgname,infotext){" ) ;
        printWriter.println( "  var showWarnarzt=false;" ) ;
        printWriter.println( "  try{" ) ;
        printWriter.println( "      if(parent.TopPanel.animation==1)" ) ;
        printWriter.println( "          showWarnarzt=true;" ) ;
        printWriter.println( "  }catch(e){" ) ;
        printWriter.println( "      if(opener.parent.TopPanel.animation==1)" ) ;
        printWriter.println( "          showWarnarzt=true;" ) ;
        printWriter.println( "  }" ) ;
        printWriter.println( "  if(showWarnarzt) {" ) ;
        printWriter.println( "      if (document.layers) {navigator.family = \"nn4\"}" ) ;
        printWriter.println( "      if (document.all) {navigator.family = \"ie4\"}" ) ;
        printWriter.println( "      if (window.navigator.userAgent.toLowerCase().match(\"gecko\")) {navigator.family = \"gecko\"}" ) ;
        printWriter.println( "      if(navigator.family ==\"nn4\") {" ) ;
        printWriter.println( "          document.warnarzt.left=xpos;" ) ;
        printWriter.println( "          document.warnarzt.top=ypos;" ) ;
        printWriter.println( "      }" ) ;
        printWriter.println( "      else if(navigator.family ==\"ie4\"){" ) ;
        printWriter.println( "          warnarzt.style.pixelLeft=xpos;" ) ;
        printWriter.println( "          warnarzt.style.pixelTop=ypos;" ) ;
        printWriter.println( "      }" ) ;
        printWriter.println( "      else if(navigator.family ==\"gecko\"){" ) ;
        printWriter.println( "          document.getElementById(\"warnarzt\").style.left=xpos;" ) ;
        printWriter.println( "          document.getElementById(\"warnarzt\").style.top=ypos;" ) ;
        printWriter.println( "      }" ) ;
        printWriter.println( "      startWarnarzt(imgname,infotext);" ) ;
        printWriter.println( "  }else{" ) ;
        printWriter.println( "      TOOLTip(infotext);" ) ;
        printWriter.println( "  }" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "function killWarnarzt(){" ) ;
        printWriter.println( " 	document.getElementById('warnarzt').style.visibility=\"hidden\";" ) ;
        printWriter.println( "	document.getElementById('warnarztinfo').style.visibility=\"hidden\";" ) ;
        printWriter.println( "}" ) ;
        printWriter.println( "function startWarnarzt(imgname,infotext){" ) ;
        printWriter.println( "  document.getElementById('warnimg').src=\"/EIDPWebApp/images/WarnArzt/\"+imgname;" ) ;
        printWriter.println( "  document.getElementById('warnarztinfoarea').src=\"javascript:document.open();document.write('<html><head><title>ToolTip</title></head><body bgcolor=#FF8000><font face=Arial,Helvetica,sans-serif>\" + infotext + \"</font></body></html>');document.close();\";" ) ;
        printWriter.println( "  document.getElementById('warnarzt').style.visibility=\"visible\";" ) ;
        printWriter.println( "  document.getElementById('warnarztinfo').style.visibility=\"visible\";" ) ;
        printWriter.println( "}" ) ;
    }
    
    // HTML for WarnArzt  // Stephan
    protected void createWarnArztHTML( PrintWriter printWriter ) throws org.xml.sax.SAXException {
        printWriter.println( "<div name=\"warnarzt\" id=\"warnarzt\" style=\"position: absolute; top: 100px; left: 0px; width: 0px; height: 0px; background-color: transparent; visibility: hidden;\">" ) ;
        printWriter.println( "<img style=\"cursor: pointer;\" title=\"click to close\" height=\"130\" id='warnimg' onclick=\"javascript:killWarnarzt();\" src=\"/EIDPWebApp/images/WarnArzt/info_doctor30.gif\" border=\"0\">" ) ;
        printWriter.println( "<div align=\"center\" id=\"warnarztinfo\" style=\"position: absolute; top: -85px; left: 60px; width: 0; height: 0; background-color: '#FF8000'; visibility: hidden;\">" ) ;
//        printWriter.println( "<font face=Arial,Helvetica,sans-serif><table align=right border=0 width=10><tr onclick=javascript:killWarnarzt(); title=\"click to close\"><td align=center style=cursor:pointer;background-color:black;color:red;><b>X</b></td></tr></table></font><br>" ) ;
        printWriter.println( "<iframe style=\"border: solid; background-color: '#FF8000'; font: 10px Arial, Helvetica, sans-serif;\" id=\"warnarztinfoarea\" height=\"100\" width=\"250\"></iframe> </div>" ) ;
        printWriter.println( "</div>");
        //printWriter.println( "<div align=\"center\" id=\"helpdiv\" style=\"position: absolute; top: -365px; left: 320px; width: 0; height: 0; background-color: '#FF8000'; visibility: hidden;\">" ) ;
        //printWriter.println( "<iframe style=\"border: solid; background-color: '#FF8000'; \" name=\"helpframe\" id=\"helpframe\" height=\"450\" width=\"350\"></iframe> </div></div>" ) ;
    }
    
    // to import JavaScript-ScriptFiles // Stephan
    protected void createJavaScriptImport( PrintWriter printWriter , NodeList xmlModuleNode , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        Vector javascriptNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "javascript" , xmlModuleNode ) ;
        if( javascriptNodes.size() > 0 ){
            String strJavaScriptFilePath = (String)((Vector)uso.xmlDataAccess.getElementsByName( "javascript,source" , xmlModuleNode )).get( 0 ) ;
            BufferedReader br = null;
            br = openFileReader( strJavaScriptFilePath );
            String line = "";
            printWriter.println( "<script type=\"text/javascript\">" ) ;
            while( ( line = br.readLine() ) != null ) {
                printWriter.println( line );
            }
            printWriter.println( "</script>" ) ;
        }
    }
    
    private BufferedReader openFileReader( String file ) throws java.io.IOException {
        BufferedReader fileReader ;
        try {
            fileReader = new BufferedReader( new FileReader( new File( file ) ) ) ;
        } catch ( java.io.FileNotFoundException fnfe ) {
            throw new java.io.IOException( "File not found: " + fnfe ) ;
        }
        return fileReader ;
    }
    
    // to check the form with a individual check // Stephan
    protected String getJavaScriptFormCheck( NodeList xmlViewNode , UserScopeObject uso ) throws org.xml.sax.SAXException {
        Vector javascriptNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "javascript" , xmlViewNode ) ;
        String strErg = "";
        if( javascriptNodes.size() > 0 ){
            String strJavaScriptFormCheck = (String)((Vector)uso.xmlDataAccess.getElementsByName( "javascript,formcheck" , xmlViewNode )).get( 0 ) ;
            if( strJavaScriptFormCheck != null )
                strErg = strJavaScriptFormCheck;
        }
        return strErg;
    }
    
    // ===============================
    // Check for view change
    // 1 = active View == requested View ;
    // 0 = active View != requested View ;
    protected void createViewActiveCheck( PrintWriter printWriter ) {
        printWriter.println( "  var activeView = \"(null)\" ;" ) ;
        printWriter.println( "  function checkForActiveView( requestedView ) {" ) ;
        printWriter.println( "      if ( requestedView != activeView && activeView != \"(null)\" ) { " ) ;
        if(isGerman)
            printWriter.println( "          alertMsg = \"Sie haben Ihre Eingaben in <\" + activeView + \"> noch nicht gespeichert.\\nBitte speichern Sie zuerst Ihre Eingaben.\\nOder druecken Sie den CLEAR-Button in <\" + activeView + \">.\" ; " ) ;
        else
            printWriter.println( "          alertMsg = \"You have not submitted your data in <\" + activeView + \">.\\nPlease SUBMIT your changes.\\nOr press CLEAR in <\" + activeView + \">.\" ; " ) ;
        printWriter.println( "          alert( alertMsg ) ;" ) ;
        printWriter.println( "          eval(\"document.\" + requestedView + \".reset()\") ; " ) ;
        printWriter.println( "          return 0 ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "          activeView = requestedView ;" ) ;
        printWriter.println( "          return 1 ; " ) ;
        printWriter.println( "      } ") ;
        printWriter.println( "      return 1 ; " ) ;
        printWriter.println( "  }" ) ;
    }
    
    protected void createClearCheck( PrintWriter printWriter ) {
        printWriter.println( "  function clearCheckForView( viewName ) {" ) ;
        printWriter.println( "      activeView = \"(null)\" ;" ) ;
        printWriter.println( "      for(var i = 0 ; i < document.forms[viewName].elements.length ; i++){" ) ;
        printWriter.println( "          if( document.forms[viewName].elements[i].type == \"text\" || document.forms[viewName].elements[i].type == \"textarea\"  || document.forms[viewName].elements[i].tagName == \"SELECT\" ) " ) ;
        printWriter.println( "              if( document.forms[viewName].elements[i].disabled == false  ) " ) ;
        printWriter.println( "                  document.forms[viewName].elements[i].style.backgroundColor = \"#FFFFFF\"; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "  }" ) ;
    }
    
    // Stephan
    // changes the tablebordercolor of the view with the values of the alternate query
    protected void processTableBorderColorStatic(PrintWriter printWriter , UserScopeObject uso, String formName) throws java.rmi.RemoteException {
        Boolean changeColor = (Boolean)uso.eidpWebAppCache.sessionData_get( "useBorderColorForView" );
        if( changeColor != null && changeColor.booleanValue() ){
            String borderColor = (String)uso.eidpWebAppCache.sessionData_get( "BorderColorForView" );
            printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
            printWriter.println( "  document.getElementById('" + formName + "_Table').style.border = \"solid 3px #" + borderColor + "\";" ) ;
            printWriter.println( "</script> " ) ;
            uso.eidpWebAppCache.sessionData_set( "useBorderColorForView", new Boolean(false) );
            uso.eidpWebAppCache.sessionData_set( "BorderColorForView", "" );
        }
    }
    
    // Stephan
    // creates a blinking span with info for the user about the alternate query in use
    protected void processCreateUserMessageForAlternateQuery(PrintWriter printWriter , UserScopeObject uso, String formName) throws java.rmi.RemoteException {
        Boolean createSpan = (Boolean)uso.eidpWebAppCache.sessionData_get( "showAlternateQueryInfo" );
        if( createSpan != null && createSpan.booleanValue() ){
            Boolean createTooltip = (Boolean)uso.eidpWebAppCache.sessionData_get( "useAlternateQueryTooltip" );
            String strToolTip = "";
            if( createTooltip != null && createTooltip.booleanValue() ){
                strToolTip = (String)uso.eidpWebAppCache.sessionData_get( "AlternateQueryTooltip" );
                strToolTip = "&nbsp;<a href=\"javascript:TOOLTip('" + strToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + strToolTip + "\"></a>";
            }
            String textColor1 = (String)uso.eidpWebAppCache.sessionData_get( "AlternateQueryInfoColor1" );
            String textColor2 = (String)uso.eidpWebAppCache.sessionData_get( "AlternateQueryInfoColor2" );
            printWriter.println( "<span id=\"" + formName + "_Span\"><nobr>" + uso.eidpWebAppCache.sessionData_get("AlternateQueryInfo") + strToolTip + "</nobr></span>" ) ;
            printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
            printWriter.println( "  function blinkFor" + formName + "(span_id){" ) ;
            printWriter.println( "	if(blink" + formName + "==1){" ) ;
            printWriter.println( "		document.getElementById(span_id).style.color = \"" + textColor1 + "\";" ) ;
            printWriter.println( "		blink" + formName + " = 0;" ) ;
            printWriter.println( "	}else{" ) ;
            printWriter.println( "		document.getElementById(span_id).style.color = \"" + textColor2 + "\";" ) ;
            printWriter.println( "		blink" + formName + " = 1;" ) ;
            printWriter.println( "	}" ) ;
            printWriter.println( "      setTimeout(\"blinkFor" + formName + "('" + formName + "_Span');\" , 500);" ) ;
            printWriter.println( "  }" ) ;
            printWriter.println( "  var blink" + formName + " = 0;" ) ;
            printWriter.println( "  blinkFor" + formName + "('" + formName + "_Span');" ) ;
            printWriter.println( "</script> " ) ;
            uso.eidpWebAppCache.sessionData_set( "showAlternateQueryInfo", new Boolean(false) );
            uso.eidpWebAppCache.sessionData_set( "AlternateQueryInfoColor1", "" );
            uso.eidpWebAppCache.sessionData_set( "AlternateQueryInfoColor2", "" );
        }
    }
    
    // ===============================
    // Submit check for mandatory data
    
    // ===============================
    // Submit check for mandatory data
    
    protected void createSubmitCheck( NodeList xmlModuleNode , PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException {
        Vector viewNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "view" , xmlModuleNode ) ;
        for ( int vi = 0 ; vi < viewNodes.size() ; vi++ ) {
            String formName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)viewNodes.get( vi ) )).get( 0 ) ;
            String formType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)viewNodes.get( vi ) )).get( 0 ) ;
            // 2. get mandatory store fields
            // get fields:
            Vector fieldVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "field" , (NodeList)viewNodes.get( vi ) ) ;
            // check if type = label and build mandatoryFields, mandatoryFieldsType
            Vector mandatoryFields = new Vector() ;
            for ( int i = 0 ; i < fieldVector.size() ; i++ ) {
                String fieldType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)fieldVector.get( i ) )).get( 0 ) ;
                if ( ! ( fieldType.equals( "link" ) || fieldType.equals( "label" ) || fieldType.equals( "hidden" ) || fieldType.equals( "listSelect" ) ) ) {
                    String fieldFlag = (String)((Vector)uso.xmlDataAccess.getElementsByName( "store-option" , (NodeList)fieldVector.get( i ) )).get( 0 ) ;
                    if ( fieldFlag.equals( "mandatory" ) ) {
                        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)fieldVector.get( i ) )).get( 0 ) ;
                        // build Vectors
                        mandatoryFields.addElement( fieldName ) ;
                    }
                }
            }
            // write Checks
            printWriter.println( "  exceptionMap[ \"" + formName + "\" ] = new Array() ; " ) ;
            printWriter.println( "function " + formName + "_CheckAndSubmit () {" ) ;
            printWriter.println( "  if ( checkForActiveView( \"" + formName + "\" ) == 1 ) { " ) ;
            printWriter.println( "      exception = 0 ; " ) ;
            if(isGerman)
                printWriter.println( "      exceptionMsg = \"Bei der Ueberpruefung Ihrer Eingabe trat ein Fehler auf:\" ; " ) ;
            else
                printWriter.println( "      exceptionMsg = \"Exception thrown during validity Checks:\" ; " ) ;
            if ( formType.equals( "list" ) || formType.equals( "matrix-list" ) ) {
                printWriter.println( "      if ( document." + formName + ".NEW.checked == true ) {" ) ;
            }
            // === MAIN Part for all store procedures:
            for ( int mi = 0 ; mi < mandatoryFields.size() ; mi++ ) {
                printWriter.println( "              if ( document." + formName + "." + (String)mandatoryFields.get( mi ) + ".value == \"\" ) { " ) ;
                if(isGerman)
                    printWriter.println( "                  exceptionMsg = exceptionMsg + \"\\nDas Pflichtfeld: <" + (String)mandatoryFields.get( mi ) + "> hat keinen Eintrag.\" ; " ) ;
                else
                    printWriter.println( "                  exceptionMsg = exceptionMsg + \"\\nMandatory field: <" + (String)mandatoryFields.get( mi ) + "> has no value.\" ; " ) ;
                printWriter.println( "                  document." + formName + "." + (String)mandatoryFields.get( mi ) + ".style.backgroundColor = \"" + strMandatoryBackgroundColor + "\"; " ) ;
                printWriter.println( "                  exception = 1 ; " ) ;
                printWriter.println( "              } " ) ;
            }
            // ===
            if ( formType.equals( "list" ) || formType.equals( "matrix-list" ) ) {
                printWriter.println( "  } " ) ;
            }
            printWriter.println( "      for ( var FormException in exceptionMap[ \"" + formName + "\" ] ) { " ) ;
            printWriter.println( "          if ( exceptionMap[ \"" + formName + "\" ][FormException] == 1 ) {" ) ;
            printWriter.println( "              exception = 1 ; " ) ;
            if(isGerman)
                printWriter.println( "              exceptionMsg = \"Bei der Ueberpruefung eines Eingabefeldes trat ein Fehler auf: \" + FormException + \".\\n\" ; " ) ;
            else
                printWriter.println( "              exceptionMsg = \"Exception thrown in field: \" + FormException + \".\\n\" ; " ) ;
            printWriter.println( "          } " ) ;
            printWriter.println( "      } " ) ;
            printWriter.println( "      if ( exception == 1 ) { " ) ;
            printWriter.println( "          alert ( exceptionMsg ) ; " ) ;
            printWriter.println( "      } else { " ) ;
            printWriter.println( "          for ( var FormException in exceptionMap[ \"" + formName + "\" ] ) { " ) ;
            printWriter.println( "              if( document.forms[\"" + formName + "\"].elements[FormException].type == \"text\" || document.forms[\"" + formName + "\"].elements[FormException].type == \"textarea\" ) " ) ;
            printWriter.println( "                  if( document.forms[\"" + formName + "\"].elements[FormException].disabled == false  ) " ) ;
            printWriter.println( "                      document.forms[\"" + formName + "\"].elements[FormException].style.backgroundColor = \"#FFFFFF\"; " ) ;
            printWriter.println( "          } " ) ;
            // write RichTextArea-Values to hidden-fields // Stephan
//            printWriter.println( "          for( var i=0 ; i < richtextareas.length ; i++ ){" ) ;
//            printWriter.println( "              var hiddenFieldValue = document.getElementById(richtextareas[i]).contentWindow.document.body.innerHTML;" ) ;
//            printWriter.println( "              hiddenFieldValue = hiddenFieldValue.replace(/<f6>/g,'');" ) ;
//            printWriter.println( "              hiddenFieldValue = hiddenFieldValue.replace(/<\\/f6>/g,'');" ) ;
//            printWriter.println( "              hiddenFieldValue = hiddenFieldValue.replace(/<(\"[^\"]*\"|'[^']*'|[^'\">])*>/g,'');" ) ;
//            printWriter.println( "              hiddenFieldValue = hiddenFieldValue.replace(/&nbsp;/g,' ');" ) ;
//            printWriter.println( "              document.forms[\"" + formName + "\"].elements[richtextareas[i]].value = hiddenFieldValue;" ) ;
//            printWriter.println( "          }" ) ;
            // insert individual FormCheck // Stephan
            String FormCheck = getJavaScriptFormCheck( (NodeList)viewNodes.get( vi ) , uso );
            if( !FormCheck.equals("") ){
                printWriter.println( "          if ( " + FormCheck + " )  " ) ;
            }
            //////////// Stephan Ende
            printWriter.println( "              document." + formName + ".submit() ;" ) ;
            printWriter.println( "      } " ) ;
            printWriter.println( "  } " ) ;
            printWriter.println( " } " ) ;
        }
    }
    
// ================
// FILTER FUNCTIONS
    
    protected void IntegerFilter( NodeList filterNode , PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException {
        String filterName = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "name" , filterNode )).get( 0 ) ;
        String filterSize = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "size" , filterNode )).get( 0 ) ;
        String filterMin = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "min" , filterNode )).get( 0 ) ;
        String filterMax = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "max" , filterNode )).get( 0 ) ;
        
        printWriter.println( "function " + filterName + " ( value , formName , fieldName , filterMessage ) { " ) ;
        printWriter.println( "  exception = 0 ; " ) ;
        printWriter.println( "  var test = value.replace(/\\s/g,'') ; " ) ;
        if(isGerman){
            printWriter.println( "  exceptionMsg = \"Bei der Ueberpruefung eines Eingabefeldes trat ein Fehler auf:\" ; " ) ;
            printWriter.println( "      if ( value.match( /\\D/ ) ) { exceptionMsg = exceptionMsg + \"\\nSie haben KEINE ganze Zahl eingegeben.\" ; exception = 1 ; }" ) ;
            printWriter.println( "      if ( value.length > " + filterSize + " ) { exceptionMsg = exceptionMsg + \"\\nDie Zahl ist zu gross.\" ; exception = 1 ; } " ) ;
            printWriter.println( "      if ( value > " + filterMax + " ) { exceptionMsg = exceptionMsg + \"\\nDie Zahl ist zu gross.\" ; exception = 1 ; }" ) ;
            printWriter.println( "      if ( value < " + filterMin + " ) { exceptionMsg = exceptionMsg + \"\\nDie Zahl ist zu klein.\" ; exception = 1 ; }" ) ;
        }else{
            printWriter.println( "  exceptionMsg = \"Exception occured during Check:\" ; " ) ;
            printWriter.println( "      if ( value.match( /\\D/ ) ) { exceptionMsg = exceptionMsg + \"\\nNo Non-Decimal Number given.\" ; exception = 1 ; }" ) ;
            printWriter.println( "      if ( value.length > " + filterSize + " ) { exceptionMsg = exceptionMsg + \"\\nValue exceeds permitted size.\" ; exception = 1 ; } " ) ;
            printWriter.println( "      if ( value > " + filterMax + " ) { exceptionMsg = exceptionMsg + \"\\nValue too big.\" ; exception = 1 ; }" ) ;
            printWriter.println( "      if ( value < " + filterMin + " ) { exceptionMsg = exceptionMsg + \"\\nValue too low.\" ; exception = 1 ; }" ) ;
        }
        printWriter.println( "      if ( test == \"\" ) { exception = 0 ; }" ) ;
        printWriter.println( "  if ( exception == 1 ) { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 1 ; " ) ;
        printWriter.println( "      if ( filterMessage == '' ) { " ) ;
        printWriter.println( "          alert ( exceptionMsg ) ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "          alert ( filterMessage ) ; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"" + strErrorBackgroundColor + "\"; " ) ;
        printWriter.println( "  } else { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 0 ; " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"#FFFFFF\"; " ) ;
        printWriter.println( "  } " ) ;
        printWriter.println( "} " ) ;
    }
    
    protected void FloatFilter( NodeList filterNode , PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException {
        String filterName = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "name" , filterNode )).get( 0 ) ;
        String filterSize = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "size" , filterNode )).get( 0 ) ;
        String filterMin = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "min" , filterNode )).get( 0 ) ;
        String filterMax = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "max" , filterNode )).get( 0 ) ;
        printWriter.println( "function " + filterName + " ( value , formName , fieldName , filterMessage ) { " ) ;
        printWriter.println( "  exception = 0 ; " ) ;
        printWriter.println( "  var test = value.replace(/\\s/g,'') ; " ) ;
        if(isGerman){
            printWriter.println( "  exceptionMsg = \"Bei der Ueberpruefung eines Eingabefeldes trat ein Fehler auf:\" ; " ) ;
            printWriter.println( "      if ( value.match( /\\D/ ) ) { " ) ;
            printWriter.println( "          if ( ! value.match( /\\d+\\.\\d+/ ) ) { exceptionMsg = exceptionMsg + \"\\nSie haben keine ganze Zahl oder Fliesskommazahl eingegeben. Format: <XXXX.YYYY>\" ; exception = 1 ; }" ) ;
            printWriter.println( "          if ( value.match( /\\d+\\.\\d+\\D/ ) ) { exceptionMsg = exceptionMsg + \"\\nSie haben keine ganze Zahl oder Fliesskommazahl eingegeben. Format: <XXXX.YYYY>\" ; exception = 1 ; } " ) ;
            printWriter.println( "      } " ) ;
            printWriter.println( "      if ( value.length > " + filterSize + " ) { exceptionMsg = exceptionMsg + \"\\nDie Anzahl der Ziffern ist zu gross.\" ; exception = 1 ; } " ) ;
            printWriter.println( "      if ( value > " + filterMax + " ) { exceptionMsg = exceptionMsg + \"\\nDie Zahl ist zu gross.\" ; exception = 1 ; }" ) ;
            printWriter.println( "      if ( value < " + filterMin + " ) { exceptionMsg = exceptionMsg + \"\\nDie Zahl ist zu klein.\" ; exception = 1 ; }" ) ;
        }else{
            printWriter.println( "  exceptionMsg = \"Exception occured during Check:\" ; " ) ;
            printWriter.println( "      if ( value.match( /\\D/ ) ) { " ) ;
            printWriter.println( "          if ( ! value.match( /\\d+\\.\\d+/ ) ) { exceptionMsg = exceptionMsg + \"\\nNo Decimal Number given. Format: <XXXX.YYYY>\" ; exception = 1 ; }" ) ;
            printWriter.println( "          if ( value.match( /\\d+\\.\\d+\\D/ ) ) { exceptionMsg = exceptionMsg + \"\\nNo Decimal Number given. Format: <XXXX.YYYY>\" ; exception = 1 ; } " ) ;
            printWriter.println( "      } " ) ;
            printWriter.println( "      if ( value.length > " + filterSize + " ) { exceptionMsg = exceptionMsg + \"\\nValue exceeds permitted size.\" ; exception = 1 ; } " ) ;
            printWriter.println( "      if ( value > " + filterMax + " ) { exceptionMsg = exceptionMsg + \"\\nValue too big.\" ; exception = 1 ; }" ) ;
            printWriter.println( "      if ( value < " + filterMin + " ) { exceptionMsg = exceptionMsg + \"\\nValue too low.\" ; exception = 1 ; }" ) ;
        }
        printWriter.println( "      if ( test == \"\" ) { exception = 0 ; }" ) ;
        printWriter.println( "  if ( exception == 1 ) { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 1 ; " ) ;
        printWriter.println( "      if ( filterMessage == '' ) { " ) ;
        printWriter.println( "          alert ( exceptionMsg ) ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "          alert ( filterMessage ) ; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"" + strErrorBackgroundColor + "\"; " ) ;
        printWriter.println( "  } else { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 0 ; " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"#FFFFFF\"; " ) ;
        printWriter.println( "  } " ) ;
        printWriter.println( "} " ) ;
    }
    
    protected void DateFilter( NodeList filterNode , PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException , java.rmi.RemoteException {
        String filterName = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "name" , filterNode )).get( 0 ) ;
        String filterSize = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "size" , filterNode )).get( 0 ) ;
        String strFormatMessage = "" ;
        printWriter.println( "function " + filterName + " ( value , formName , fieldName , filterMessage ) { " ) ;
        printWriter.println( "  exception = 0 ; " ) ;
        printWriter.println( "  var test = value.replace(/\\s/g,'') ; " ) ;
        if(isGerman)
            printWriter.println( "  exceptionMsg = \"Bei der Ueberpruefung eines Datumeingabefeldes trat ein Fehler auf:\" ; " ) ;
        else
            printWriter.println( "  exceptionMsg = \"Exception occured during Check:\" ; " ) ;
        if( ((String)uso.eidpWebAppCache.sessionData_get( "DateFormat" )).equals( "german" ) ){
            strFormatMessage = "Date-format: DD.MM.YYYY" ;
            printWriter.println( "  dateMatch = value.match( /\\d{2}\\.\\d{2}\\.\\d{4}/ ) ; " ) ;
        }else{
            strFormatMessage = "No ISO-Date given. Format: <YYYY-MM-DD>." ;
            printWriter.println( "  dateMatch = value.match( /\\d{4}-\\d{2}-\\d{2}/ ) ; " ) ;
        }
        printWriter.println( "  if ( ! dateMatch ) { exceptionMsg = exceptionMsg + \"\\n" + strFormatMessage + "\" ; exception = 1 ; }" ) ;
        if(isGerman)
            printWriter.println( "  if ( value.length > " + filterSize + " ) { exceptionMsg = exceptionMsg + \"\\nSie haben zu viele Zeichen in das Feld eingegeben.\" ; exception = 1 ; } " ) ;
        else
            printWriter.println( "  if ( value.length > " + filterSize + " ) { exceptionMsg = exceptionMsg + \"\\nValue exceeds permitted size.\" ; exception = 1 ; } " ) ;
        printWriter.println( "  if ( test == \"\" ) { exception = 0 ; }" ) ;
        printWriter.println( "  if ( exception == 1 ) { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 1 ; " ) ;
        printWriter.println( "      if ( filterMessage == '' ) { " ) ;
        printWriter.println( "          alert ( exceptionMsg ) ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "          alert ( filterMessage ) ; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"" + strErrorBackgroundColor + "\"; " ) ;
        printWriter.println( "  } else { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 0 ; " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"#FFFFFF\"; " ) ;
        printWriter.println( "  } " ) ;
        printWriter.println( "} " ) ;
    }
    
    protected void TimeFilter( NodeList filterNode , PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException {
        String filterName = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "name" , filterNode )).get( 0 ) ;
        printWriter.println( "function " + filterName + " ( value , formName , fieldName , filterMessage ) { " ) ;
        printWriter.println( "  exception = 0 ; " ) ;
        printWriter.println( "  var test = value.replace(/\\s/g,'') ; " ) ;
        if(isGerman){
            printWriter.println( "  exceptionMsg = \"Bei der Ueberpruefung eines Zeiteingabefeldes trat ein Fehler auf:\" ; " ) ;
            printWriter.println( "  if ( !value.match( /^([01]?[0-9]|[2][0-3])(:[0-5][0-9])?$/ ) && !value.match( /^([01]?[0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9])$/ ) ) { exceptionMsg = exceptionMsg + \"\\nSie haben kein korrektes Format fuer die Eingabe gewaehlt -> hh:mm:ss ODER hh:mm ODER h:mm\" ; exception = 1 ; } " ) ;
        }else{
            printWriter.println( "  exceptionMsg = \"Exception occured during Check:\" ; " ) ;
            printWriter.println( "  if ( !value.match( /^([01]?[0-9]|[2][0-3])(:[0-5][0-9])?$/ ) && !value.match( /^([01]?[0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9])$/ ) ) { exceptionMsg = exceptionMsg + \"\\nNo valid time-format given -> hh:mm:ss OR hh:mm OR h:mm\" ; exception = 1 ; } " ) ;
        }
        printWriter.println( "      if ( test == \"\" ) { exception = 0 ; }" ) ;
        printWriter.println( "  if ( exception == 1 ) { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 1 ; " ) ;
        printWriter.println( "      if ( filterMessage == '' ) { " ) ;
        printWriter.println( "          alert ( exceptionMsg ) ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "          alert ( filterMessage ) ; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"" + strErrorBackgroundColor + "\"; " ) ;
        printWriter.println( "  } else { " ) ;
        printWriter.println( "      exceptionMap[ formName ][ fieldName ] = 0 ; " ) ;
        printWriter.println( "      document.forms[formName].elements[fieldName].style.backgroundColor = \"#FFFFFF\"; " ) ;
        printWriter.println( "  } " ) ;
        printWriter.println( "} " ) ;
    }
    
    protected void createFilterFunctions( Vector filterNames , PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException , java.rmi.RemoteException {
        uso.usedFilters.clear() ;
        for ( int i = 0 ; i < uso.FilterNames.size() ; i++ ) {
            if ( filterNames.indexOf( (String)uso.FilterNames.get( i ) ) != -1 ) {
                if ( ! uso.usedFilters.containsKey( (String)uso.FilterNames.get( i ) ) ) {
                    int filterID = uso.FilterNames.indexOf( (String)uso.FilterNames.get( i ) ) ;
                    this.processFilterFunctions( filterID , printWriter , uso ) ;
                    uso.usedFilters.put( (String)uso.FilterNames.get( i ) , "1" ) ;
                }
            }
        }
    }
    
    protected void processFilterFunctions( int filterID , PrintWriter printWriter , UserScopeObject uso ) throws org.xml.sax.SAXException , java.rmi.RemoteException {
        // 1. get all the filter specs and write the stuff:
        NodeList filterNode = (NodeList)uso.Filters.get( filterID ) ;
        String filterType = (String)((Vector)uso.xmlFilterAccess.getElementsByName( "type" , filterNode )).get( 0 ) ;
        if ( filterType.equals( "Integer" ) ) {
            this.IntegerFilter( filterNode , printWriter , uso ) ;
        } else if ( filterType.equals( "Float" ) ) {
            this.FloatFilter( filterNode , printWriter , uso ) ;
        } else if ( filterType.equals( "Date" ) ) {
            this.DateFilter( filterNode , printWriter , uso ) ;
        } else if ( filterType.equals( "Time" ) ) {
            this.TimeFilter( filterNode , printWriter , uso ) ;
        }
        
    }
    
    protected void createListSelect( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String setFormName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,form-name" , fieldNode )).get( 0 ) ;
        String setPrimaryFieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,primary-field,form-field" , fieldNode )).get( 0 ) ;
        String setPrimaryFieldDBRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,primary-field,db-ref" , fieldNode )).get( 0 ) ;
        String setSecondaryFieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,secondary-field,form-field" , fieldNode )).get( 0 ) ;
        String setSecondaryFieldDBRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,secondary-field,db-ref" , fieldNode )).get( 0 ) ;
        String setPrimaryFieldValue = "" ;
        String setSecondaryFieldValue = "" ;
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        NodeList setPrimaryNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "set-opener,primary-field" , fieldNode )).get( 0 ) ;
        NodeList setSecondaryNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "set-opener,secondary-field" , fieldNode )).get( 0 ) ;
        setPrimaryFieldValue = uso.sharedMethods.getReferenceValue( setPrimaryNode , formName , row , uso ) ;
        setSecondaryFieldValue = uso.sharedMethods.getReferenceValue( setSecondaryNode , formName , row , uso ) ;
        if ( fieldValue.equals( "9999-12-31" ) ) { fieldValue = "" ; }
        if ( setPrimaryFieldValue.equals( "9999-12-31" ) ) { setPrimaryFieldValue = "" ; }
        if ( setSecondaryFieldValue.equals( "9999-12-31" ) ) { setSecondaryFieldValue = "" ; }
        if ( row == 0 ) {
            printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
            printWriter.println( " function " + formName + "_" + fieldName + "_setOption( primaryValue , secondaryValue ) { " ) ;
            printWriter.println( "  opener.document." + setFormName + "." + setPrimaryFieldName + ".value = primaryValue ; " ) ;
            printWriter.println( "  opener.document." + setFormName + "." + setSecondaryFieldName + ".value = secondaryValue ; " ) ;
            printWriter.println( "  window.close() ; " ) ;
            printWriter.println( " } " ) ;
            printWriter.println( "</script> " ) ;
        }
        printWriter.print( "<nobr><p><a href='javascript:" + formName + "_" + fieldName + "_setOption( \"" + setPrimaryFieldValue + "\" , \"" + setSecondaryFieldValue + "\" )'>" + fieldValue + "</a></p>" ) ;
    }
    
    // include calculate values
    protected void createTextInputField( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String onChange = "" ;
        String onClick = " onClick=\"javascript:checkForActiveView( '" + formName + "' ) ; \" " ;
        if ( uso.multiFlag == true ) {
            if ( row == -1 ) {
                onChange = "this.form.NEW.checked=true;" ;
            } else {
                fieldName =  fieldName + "_" + (String)Integer.toString( row ) ;
                onChange = "this.form.UPDATE_" + row + ".checked=true;" ;
            }
        } else {
            row = 0 ;
        }
        String fieldSize = (String)((Vector)uso.xmlDataAccess.getElementsByName( "size" , fieldNode )).get( 0 ) ;
        String fieldMaxLength = "" ;
        try {
            fieldMaxLength = (String)((Vector)uso.xmlDataAccess.getElementsByName( "max-input-size" , fieldNode )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            fieldMaxLength = fieldSize ;
        }
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        Vector filterNames = (Vector)uso.xmlDataAccess.getElementsByName( "filter" , fieldNode ) ;
        Vector filterMessage = (Vector)uso.xmlDataAccess.getElementsByName( "filter-message" , fieldNode ) ;
        String fieldToolTip = "" ;
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        // Format date
        String strDateFormat = (String)uso.eidpWebAppCache.sessionData_get( "DateFormat" );
        if( !strDateFormat.equals( "" ) ){
            if( strDateFormat.equals( "german" ) ){
                if( this.isISODate(fieldValue) ){
                    fieldValue = this.convertISODateToEUDate( fieldValue ) ;
                }
            } else if (strDateFormat.equals("en_US")){
                if(this.isISODate(fieldValue)){
                    fieldValue = this.dateFormatter(fieldValue, "yyyy-MM-dd", "MMM dd, yyyy");
                }
            } else if (strDateFormat.equals("en_GB")){
                if(this.isISODate(fieldValue)){
                    fieldValue = this.dateFormatter(fieldValue, "yyyy-MM-dd", "dd.MM.yyyy");
                }
            }
        }
        // calculate field value
        Vector fieldCalculateVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "calculate" , (NodeList)fieldNode ) ;
        String strJavaScriptValue = "";
        String strJavaScriptOnLoadValue = "";
        String strJavaScriptOnChangeValue = "";
        String strCalcFunctionType = "";
        String strDisabled = "";
        
        if( fieldCalculateVector.size() > 0 ){
            Vector fieldFormulaVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,formula" , fieldNode ) ;
            Vector fieldFormulaFunctionStyleVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,style" , fieldNode ) ;
            strJavaScriptValue = parseJavaScriptFormula( (String)fieldFormulaVector.get(0) , formName );
            Vector fieldFormulaFunctionTypeVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,function" , fieldNode ) ;
            
            for( int i = 0 ; i < fieldFormulaFunctionTypeVector.size() ; i++ ) {
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onLoad") ){
                    strJavaScriptOnLoadValue = strJavaScriptValue ;
                    strDisabled = " disabled";
                }
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onChange") ){
                    strJavaScriptOnChangeValue = strJavaScriptValue ;
                    strDisabled = "";
                }
            }
            // Default: onChange
            if( (strJavaScriptOnLoadValue + strJavaScriptOnChangeValue).trim().equals("") ){
                strJavaScriptOnChangeValue = strJavaScriptValue ;
                strDisabled = "";
            }
            if( fieldFormulaFunctionStyleVector.size() > 0 ){
                if( ((String)fieldFormulaFunctionStyleVector.get(0)).equals( "enabled" ) ){
                    strDisabled = "";
                }else if( ((String)fieldFormulaFunctionStyleVector.get(0)).equals( "disabled" ) ){
                    strDisabled = " disabled";
                }else if( ((String)fieldFormulaFunctionStyleVector.get(0)).equals( "readonly" ) ){
                    strDisabled = " readonly";
                }
            }
        }
        
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        if ( fieldValue.equals( "9999-12-31" ) ) { fieldValue = "" ; }
        String inputField = "<input type=\"text\" name=\"" + fieldName + "\" value=\"" + fieldValue + "\" size=\"" + fieldSize + "\" maxlength=\"" + fieldMaxLength + "\" " + strDisabled + " ";
        
        // Filters:
        inputField += "onChange=\"javascript:if ( checkForActiveView( '" + formName + "' ) == 1 ) { userChanges=1;";
        if ( filterNames.size() > 0 ) {
            for ( int fi = 0 ; fi < filterNames.size() ; fi++ ) {
                if ( filterMessage.size() > 0 ) {
                    inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , \'" +  filterMessage.get( 0 ) + "\' );" + strJavaScriptOnChangeValue + ";";
                }else{
                    inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , '');" + strJavaScriptOnChangeValue + ";" ;
                }
            }
        }
        inputField += onChange + " }\" " ;
        inputField += onClick ;
        inputField += " >" ;
        
        // calendar button
        // author: David
        String fieldCalendar = new String();
        try {
            fieldCalendar = (String) ((Vector)uso.xmlDataAccess.getElementsByName("add-button", fieldNode)).get(0);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            fieldCalendar = "";
        }
        //
        if ( !fieldCalendar.equals("") && fieldCalendar.equals("calendar") ) {
            inputField += "<a href=\"#\" onClick=\"javascript:cal_" + fieldName + ".select(document.forms['" + formName + "']." + fieldName + ",'AN" + fieldName + "','yyyy-MM-dd'); return false;\" name=\"AN" + fieldName + "\" id=\"AN" + fieldName + "\"><img src=\"/EIDPWebApp/images/calendar.jpg\" height=\"\" width=\"\" border=\"0\" alt=\"\" /></a>";
            printWriter.println("<script language=\"JavaScript\" type=\"text/javascript\">");
            printWriter.println("   var cal_" + fieldName + " = new CalendarPopup('CAL" + fieldName + "');");
            printWriter.println("   cal_" + fieldName + ".showYearNavigation();");
            printWriter.println("   cal_" + fieldName + ".showYearNavigationInput();");
            printWriter.println("   cal_" + fieldName + ".offsetX = 14;");
            printWriter.println("   cal_" + fieldName + ".offsetY = 0;");
            printWriter.println("</script>");
            printWriter.println("<div id=\"CAL" + fieldName + "\" style=\"position:absolute;visibility:hidden;font-family:Arial,sans-serif;text-align:center;background-color:#c0c0c0;layer-background-color:white;\"></div>");
        }
        
        // here comes the manage-module
        String manageModule = "" ;
        try {
            manageModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "manage-module" , fieldNode ) ).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            manageModule = "" ;
        }
        if ( ! manageModule.equals( "" ) && ( row == -1 || uso.multiFlag == false ) ) {
            inputField += "<a href=\"javascript:CreateNewX( '" + formName + "' , '" + fieldName + "' , '" + manageModule + "' );\"><img src=\"/EIDPWebApp/images/new.jpg\" border=\"0\" title=\"Change Entry.\"></a>" ;
        }
        if ( ! fieldToolTip.equals( "" ) ) {
            inputField += "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" ;
        }
        printWriter.print( inputField ) ;
        printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\">" ) ;
        printWriter.println( " exceptionMap[ \"" + formName + "\" ][ \"" + fieldName + "\" ] = 0 ; " ) ;
        // insert the JavaSript for the destination-field: onLoad
        printWriter.println( strJavaScriptOnLoadValue ) ;
        printWriter.println( " </script>" ) ;
        // ===
        // Option-Set-Function
        printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        printWriter.println( "  function " + formName + "_" + fieldName + "_setOption( fieldValue ) { " ) ;
        printWriter.println( "      document." + formName + "." + fieldName + ".value=fieldValue ; " ) ;
        printWriter.println("  } " ) ;
        printWriter.println( "</script> " ) ;
        // ===
        //        printWriter.print( "</nobr> " ) ;
    }
    
    private String parseJavaScriptFormula( String formula , String formName ){
        String retString = "";
        Pattern p = Pattern.compile("\\$\\w*\\$");
        Matcher m = p.matcher(formula);
        StringBuffer stb = new StringBuffer();
        while(m.find()){
            m.appendReplacement(stb, "document." + formName + ".$0.value");
        }
        m.appendTail(stb);
        retString = stb.toString().replaceAll("\\$","");
        retString = this.replaceAll( retString , "&lt;" , "<" );
        retString = this.replaceAll( retString , "&gt;" , ">" );
        return retString;
    }
    
    protected void createTextArea( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String onClick = " onClick=\"javascript:checkForActiveView( '" + formName + "' ) ; \" " ;
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String fieldMaxLength = "0" ;
        try {
            fieldMaxLength = (String)((Vector)uso.xmlDataAccess.getElementsByName( "max-input-size" , fieldNode )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
        }
        
        String onChange = "" ;
        if ( uso.multiFlag == true ) {
            if ( row == -1 ) {
                onChange = "this.form.NEW.checked=true;" ;
            } else {
                fieldName =  fieldName + "_" + (String)Integer.toString( row ) ;
                onChange = "this.form.UPDATE_" + row + ".checked=true;" ;
            }
        } else {
            row = 0 ;
        }
        String areaColumns = (String)((Vector)uso.xmlDataAccess.getElementsByName( "area-columns" , fieldNode )).get( 0 ) ;
        String areaRows = (String)((Vector)uso.xmlDataAccess.getElementsByName( "area-rows" , fieldNode )).get( 0 ) ;
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        Vector filterNames = (Vector)uso.xmlDataAccess.getElementsByName( "filter" , fieldNode ) ;
        Vector filterMessage = (Vector)uso.xmlDataAccess.getElementsByName( "filter-message" , fieldNode ) ;
        String filterMess = "";
        String fieldToolTip = "" ;
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        // calculate field value
        Vector fieldCalculateVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "calculate" , (NodeList)fieldNode ) ;
        String strJavaScriptValue = "";
        String strJavaScriptOnLoadValue = "";
        String strJavaScriptOnChangeValue = "";
        String strCalcFunctionType = "";
        String strDisabled = "";
        if( fieldCalculateVector.size() > 0 ){
            Vector fieldFormulaVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,formula" , fieldNode ) ;
            Vector fieldFormulaFunctionStyleVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,style" , fieldNode ) ;
            strJavaScriptValue = parseJavaScriptFormula( (String)fieldFormulaVector.get(0) , formName );
            Vector fieldFormulaFunctionTypeVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,function" , fieldNode ) ;
            
            for( int i = 0 ; i < fieldFormulaFunctionTypeVector.size() ; i++ ) {
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onLoad") ){
                    strJavaScriptOnLoadValue = strJavaScriptValue ;
                    strDisabled = " disabled";
                }
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onChange") ){
                    strJavaScriptOnChangeValue = strJavaScriptValue ;
                    strDisabled = "";
                }
            }
            // Default: onChange
            if( (strJavaScriptOnLoadValue + strJavaScriptOnChangeValue).trim().equals("") ){
                strJavaScriptOnChangeValue = strJavaScriptValue ;
                strDisabled = "";
            }
            if( fieldFormulaFunctionStyleVector.size() > 0 ){
                if( ((String)fieldFormulaFunctionStyleVector.get(0)).equals( "enabled" ) ){
                    strDisabled = "";
                }else if( ((String)fieldFormulaFunctionStyleVector.get(0)).equals( "disabled" ) ){
                    strDisabled = " disabled";
                }else if( ((String)fieldFormulaFunctionStyleVector.get(0)).equals( "readonly" ) ){
                    strDisabled = " readonly";
                }
            }
        }
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        if ( fieldValue.equals( "9999-12-31" ) ) { fieldValue = "" ; }
        String inputField = "<textarea name=\"" + fieldName + "\" cols=\"" + areaColumns + "\" rows=\"" + areaRows + "\" " ;
        // Filters:
        if ( filterMessage.size() > 0 ) {
            filterMess = (String)filterMessage.get( 0 );
        }
        // Filters:
        inputField += "onChange=\"javascript:if ( checkForActiveView( '" + formName + "' ) == 1 ) { userChanges=1;checkMaxInput( \'" + formName + "\' , \'" + fieldName + "\' , " + fieldMaxLength + " , \'" +  filterMess + "\' );";
        if ( filterNames.size() > 0 ) {
            for ( int fi = 0 ; fi < filterNames.size() ; fi++ ) {
                inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , \'" +  filterMess + "\' );";
            }
        }
        inputField += strJavaScriptOnChangeValue + ";";
        inputField += onChange + "}\" " ;
        inputField += onClick ;
        inputField += " >" + fieldValue ;
        inputField += "</textarea> " ;
        // here comes the manage-module
        String manageModule = "" ;
        try {
            manageModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "manage-module" , fieldNode ) ).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            manageModule = "" ;
        }
        if ( ! manageModule.equals( "" ) && row == -1 ) {
            inputField += "<a href=\"javascript:CreateNewX( '" + formName + "' , '" + fieldName + "' , '" + manageModule + "' );\"><img src=\"/EIDPWebApp/images/new.jpg\" border=\"0\" title=\"Change Entry.\"></a>" ;
        }
        if ( ! fieldToolTip.equals( "" ) ) {
            inputField += "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" ;
        }
        printWriter.print( inputField ) ;
        printWriter.print( " <script language=\"JavaScript\" type=\"text/javascript\">exceptionMap[ \"" + formName + "\" ][ \"" + fieldName + "\" ] = 0 ;" ) ;
        // insert the JavaSript for the destination-field: onLoad
        printWriter.println( strJavaScriptOnLoadValue ) ;
        printWriter.println( "</script>" ) ;
        
    }
    
    // Stephans Texteditor
    protected void createRichTextArea( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String onClick = " onClick=\"javascript:checkForActiveView( '" + formName + "' ) ; \" " ;
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String fieldMaxLength = "0" ;
        try {
            fieldMaxLength = (String)((Vector)uso.xmlDataAccess.getElementsByName( "max-input-size" , fieldNode )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
        }
        
        String onChange = "" ;
        if ( uso.multiFlag == true ) {
            if ( row == -1 ) {
                onChange = "this.form.NEW.checked=true;" ;
            } else {
                fieldName =  fieldName + "_" + (String)Integer.toString( row ) ;
                onChange = "this.form.UPDATE_" + row + ".checked=true;" ;
            }
        } else {
            row = 0 ;
        }
        String areaHeight = (String)((Vector)uso.xmlDataAccess.getElementsByName( "area-height" , fieldNode )).get( 0 ) ;
        String areaWidth = (String)((Vector)uso.xmlDataAccess.getElementsByName( "area-width" , fieldNode )).get( 0 ) ;
        String areaNumber = (String)((Vector)uso.xmlDataAccess.getElementsByName( "area-number" , fieldNode )).get( 0 ) ;
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        Vector filterNames = (Vector)uso.xmlDataAccess.getElementsByName( "filter" , fieldNode ) ;
        Vector filterMessage = (Vector)uso.xmlDataAccess.getElementsByName( "filter-message" , fieldNode ) ;
        String filterMess = "";
        String fieldToolTip = "" ;
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        if(fieldValue.indexOf("<br>") == -1){
            Pattern p = Pattern.compile("\\r");
            Matcher m = p.matcher(fieldValue);
            fieldValue = m.replaceAll("<br>");
            p = Pattern.compile("\\n");
            m = p.matcher(fieldValue);
            fieldValue = m.replaceAll("<br>");
        }
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        if ( fieldValue.equals( "9999-12-31" ) ) { fieldValue = "" ; }
//        String inputField = "<textarea name=\"" + fieldName + "\" cols=\"" + areaColumns + "\" rows=\"" + areaRows + "\" " ;
        String inputField = "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + fieldValue + "\"><iframe style=\"BACKGROUND-COLOR:#ffffff;\" id=\"" + fieldName + "\" height=\"" + areaHeight + "\" width=\"" + areaWidth + "\" " ;
        // Filters:
        if ( filterMessage.size() > 0 ) {
            filterMess = (String)filterMessage.get( 0 );
        }
        inputField += "onChange=\"javascript:alert(document." + formName + "." + fieldName + ".value); document." + formName + "." + fieldName + ".value=document.getElementById('" + fieldName + "').contentWindow.document.body.innerHTML;if ( checkForActiveView( '" + formName + "' ) == 1 ) { userChanges=1;checkMaxInput( \'" + formName + "\' , \'" + fieldName + "\' , " + fieldMaxLength + " , \'" +  filterMess + "\' );" ;
//        if ( filterNames.size() > 0 ) {
//            for ( int fi = 0 ; fi < filterNames.size() ; fi++ ) {
//                inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , \'" +  filterMess + "\' );" ;
//            }
//        }
        inputField += onChange + "}\" " ;
        inputField += onClick ;
        inputField += " ></iframe>" ;
        
        // here comes the manage-module
        String manageModule = "" ;
        try {
            manageModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "manage-module" , fieldNode ) ).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            manageModule = "" ;
        }
        if ( ! manageModule.equals( "" ) && row == -1 ) {
            inputField += "<a href=\"javascript:CreateNewX( '" + formName + "' , '" + fieldName + "' , '" + manageModule + "' );\"><img src=\"/EIDPWebApp/images/new.jpg\" border=\"0\" title=\"Change Entry.\"></a>" ;
        }
        if ( ! fieldToolTip.equals( "" ) ) {
            inputField += "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" ;
        }
        //Toolbar zum editieren
        printWriter.println( "<table id=\"toolbar_" + fieldName + "\" bgcolor=\"#c0c0c0\">" ) ;
        printWriter.println( "<tbody><tr>" ) ;
        printWriter.println( "<td>" ) ;
        printWriter.println( "<div style=\"border: 2px solid rgb(192, 192, 192);\" class=\"imagebutton_" + fieldName + "\" id=\"bold\"><img style=\"left: 1px; top: 1px;\" class=\"image\" src=\"/EIDPWebApp/images/bold.gif\" alt=\"Bold\" title=\"Bold\"></div>" ) ;
        printWriter.println( "</td>" ) ;
        printWriter.println( "<td>" ) ;
        printWriter.println( "<div style=\"border: 2px solid rgb(192, 192, 192);\" class=\"imagebutton_" + fieldName + "\" id=\"italic\"><img style=\"left: 1px; top: 1px;\" class=\"image\" src=\"/EIDPWebApp/images/italic.gif\" alt=\"Italic\" title=\"Italic\"></div>" ) ;
        printWriter.println( "</td>" ) ;
        printWriter.println( "<td>" ) ;
        printWriter.println( "<div style=\"border: 2px solid rgb(192, 192, 192);\" class=\"imagebutton_" + fieldName + "\" id=\"underline\"><img style=\"left: 1px; top: 1px;\" class=\"image\" src=\"/EIDPWebApp/images/underline.gif\" alt=\"Underline\" title=\"Underline\"></div>" ) ;
        printWriter.println( "</td>" ) ;
        printWriter.println( "</tr>" ) ;
        printWriter.println( "</tbody></table>" ) ;
        printWriter.print( inputField ) ;
        printWriter.print( " <script language=\"JavaScript\" type=\"text/javascript\">exceptionMap[ \"" + formName + "\" ][ \"" + fieldName + "\" ] = 0 ; richtextareas[" + areaNumber + "]=\"" + fieldName + "\"; document.getElementById('" + fieldName + "').contentWindow.document.open(); document.getElementById('" + fieldName + "').contentWindow.document.write(\"" + fieldValue + "\"); document.getElementById('" + fieldName + "').contentWindow.document.close();</script>" ) ;
    }
    
    protected void createRadioButton( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String onChange = "" ;
        if ( uso.multiFlag == true ) {
            if ( row == -1 ) {
                onChange = "this.form.NEW.checked=true;" ;
            } else {
                fieldName =  fieldName + "_" + (String)Integer.toString( row ) ;
                onChange = "this.form.UPDATE_" + row + ".checked=true;" ;
            }
        } else {
            row = 0 ;
        }
        String buttonValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "button-value" , fieldNode )).get( 0 ) ;
        String buttonLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "button-label" , fieldNode )).get( 0 ) ;
        
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        Vector filterNames = (Vector)uso.xmlDataAccess.getElementsByName( "filter" , fieldNode ) ;
        Vector filterMessage = (Vector)uso.xmlDataAccess.getElementsByName( "filter-message" , fieldNode ) ;
        String fieldToolTip = "" ;
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        
        // calculate field value
        Vector fieldCalculateVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "calculate" , (NodeList)fieldNode ) ;
        
        String strJavaScriptValue = "";
        String strJavaScriptOnLoadValue = "";
        String strJavaScriptOnChangeValue = "";
        if( fieldCalculateVector.size() > 0 ){
            Vector fieldFormulaVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,formula" , fieldNode ) ;
            strJavaScriptValue = parseJavaScriptFormula( (String)fieldFormulaVector.get(0) , formName );
            Vector fieldFormulaFunctionTypeVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,function" , fieldNode ) ;
            for( int i = 0 ; i < fieldFormulaFunctionTypeVector.size() ; i++ ) {
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onLoad") ){
                    strJavaScriptOnLoadValue = strJavaScriptValue ;
                }
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onChange") ){
                    strJavaScriptOnChangeValue = strJavaScriptValue ;
                }
            }
            // Wenn keine Funktion angegeben wurde, wird als default onChange genommen
            if( (strJavaScriptOnLoadValue + strJavaScriptOnChangeValue).trim().equals("") ){
                strJavaScriptOnChangeValue = strJavaScriptValue ;
            }
        }
        
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        String inputField = "<input type=\"radio\" name=\"" + fieldName + "\" value=\"" + buttonValue + "\" " ;
        // Filters:
        inputField += "onChange=\"javascript:if ( checkForActiveView( '" + formName + "' ) == 1 ) { userChanges=1;" + strJavaScriptOnChangeValue;
        
        if ( filterNames.size() > 0 ) {
            for ( int fi = 0 ; fi < filterNames.size() ; fi++ ) {
                if ( filterMessage.size() > 0 ) {
                    inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , \'" +  filterMessage.get( 0 ) + "\' );" ; ;
                }else{
                    inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , '');" ;
                }
            }
        }
        inputField += onChange + "}\"" ;
        if ( buttonValue.equals( fieldValue ) ) {
            inputField += " checked " ;
        }
        inputField += " >" + buttonLabel ;
        if ( ! fieldToolTip.equals( "" ) ) {
            inputField += "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" ;
        }
        printWriter.print( inputField ) ;
        printWriter.print( " <script language=\"JavaScript\" type=\"text/javascript\">exceptionMap[ \"" + formName + "\" ][ \"" + fieldName + "\" ] = 0 ;" + strJavaScriptOnLoadValue + "</script>" ) ;
        //        printWriter.print( "</nobr> " ) ;
    }
    
    protected void createCheckBox( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        // calculate field value
        Vector fieldCalculateVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "calculate" , (NodeList)fieldNode ) ;
        String strJavaScriptValue = "";
        String strJavaScriptOnLoadValue = "";
        String strJavaScriptOnChangeValue = "";
        if( fieldCalculateVector.size() > 0 ){
            Vector fieldFormulaVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,formula" , fieldNode ) ;
            strJavaScriptValue = parseJavaScriptFormula( (String)fieldFormulaVector.get(0) , formName );
            Vector fieldFormulaFunctionTypeVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,function" , fieldNode ) ;
            for( int i = 0 ; i < fieldFormulaFunctionTypeVector.size() ; i++ ) {
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onLoad") ){
                    strJavaScriptOnLoadValue = strJavaScriptValue ;
                }
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onChange") ){
                    strJavaScriptOnChangeValue = strJavaScriptValue ;
                }
            }
            // Wenn keine Funktion angegeben wurde, wird als default onChange genommen
            if( (strJavaScriptOnLoadValue + strJavaScriptOnChangeValue).trim().equals("") ){
                strJavaScriptOnChangeValue = strJavaScriptValue ;
            }
        }
        
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String onChange = "" ;
        if ( uso.multiFlag == true ) {
            if ( row == -1 ) {
                onChange = "this.form.NEW.checked=true;" ;
            } else {
                fieldName =  fieldName + "_" + (String)Integer.toString( row ) ;
                onChange = "this.form.UPDATE_" + row + ".checked=true;" ;
            }
        } else {
            row = 0 ;
        }
        String buttonValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "button-value" , fieldNode )).get( 0 ) ;
        String buttonLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "button-label" , fieldNode )).get( 0 ) ;
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        Vector filterNames = (Vector)uso.xmlDataAccess.getElementsByName( "filter" , fieldNode ) ;
        Vector filterMessage = (Vector)uso.xmlDataAccess.getElementsByName( "filter-message" , fieldNode ) ;
        String fieldToolTip = "" ;
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        String inputField = "<input type=\"checkbox\" name=\"" + fieldName + "\" value=\"" + buttonValue + "\" " ;
        // Filters:
        inputField += "onChange=\"javascript:if ( checkForActiveView( '" + formName + "' ) == 1 ) { userChanges=1;" ;
        if ( filterNames.size() > 0 ) {
            for ( int fi = 0 ; fi < filterNames.size() ; fi++ ) {
                if ( filterMessage.size() > 0 ) {
                    inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , \'" +  filterMessage.get( 0 ) + "\' );" ; ;
                }else{
                    inputField += filterNames.get( fi ) + "( this.form." + fieldName + ".value , \'" + formName + "\' , \'" + fieldName + "\' , '');" ;
                }
            }
        }
        inputField += onChange + strJavaScriptOnChangeValue + ";}\"" ;
        if ( buttonValue.equals( fieldValue ) ) {
            inputField += " checked " ;
        }
        inputField += " >" + buttonLabel ;
        if ( ! fieldToolTip.equals( "" ) ) {
            inputField += "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" ;
        }
        printWriter.print( inputField ) ;
        printWriter.print( " <script language=\"JavaScript\" type=\"text/javascript\">exceptionMap[ \"" + formName + "\" ][ \"" + fieldName + "\" ] = 0 ; " + strJavaScriptOnLoadValue + "</script>" ) ;
        //        printWriter.print( "</nobr> " ) ;
    }
    
    // Button - Creator // Stephan
    protected String createButton( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        String fieldToolTip = "" ;
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        
        String buttonLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label" , fieldNode )).get( 0 ) ;
        String buttonColor = (String)((Vector)uso.xmlDataAccess.getElementsByName( "color" , fieldNode )).get( 0 ) ;
        String buttonBackColor = (String)((Vector)uso.xmlDataAccess.getElementsByName( "backcolor" , fieldNode )).get( 0 ) ;
        String buttonAction = (String)((Vector)uso.xmlDataAccess.getElementsByName( "action" , fieldNode )).get( 0 ) ;
        
        String inputField = "<input type=\"button\" name=\"" + fieldName + "\" value=\"" + buttonLabel + "\" ";
        
        String strStyle = "";
        if( buttonColor != null ){
            strStyle = "style=\"";
            if( buttonColor != null ){
                strStyle += "COLOR:#" + buttonColor + ";" ;
            }
            if( buttonBackColor != null ){
                strStyle += "BACKGROUND-COLOR:#" + buttonBackColor + ";" ;
            }
            strStyle += "\"";
            inputField += strStyle;
        }
        String strAction = "";
        if( buttonAction != null ){
            strAction = "onClick=\"javascript:" + buttonAction + ";\"";
            inputField += " " + strAction;
        }
        
        inputField += " >";
        if ( ! fieldToolTip.equals( "" ) ) {
            inputField += "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" ;
        }
        
        return inputField;
    }
    
    protected void createMultiHiddenField( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String dbRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "db-ref" , fieldNode )).get( 0 ) ;
        if ( uso.multiFlag == true ) {
            if ( row != -1 ) {
                fieldName =  fieldName + "_" + (String)Integer.toString( row ) ;
            }
        } else {
            row = 0 ;
        }
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        if ( fieldValue.equals( "9999-12-31" ) ) { fieldValue = "" ; }
        printWriter.print( "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + fieldValue + "\" >" ) ;
    }
    
    protected void createSelectBox( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        // calculate field value
        Vector fieldCalculateVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "calculate" , (NodeList)fieldNode ) ;
        String strJavaScriptValue = "";
        String strJavaScriptOnLoadValue = "";
        String strJavaScriptOnChangeValue = "";
        if( fieldCalculateVector.size() > 0 ){
            Vector fieldFormulaVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,formula" , fieldNode ) ;
            strJavaScriptValue = parseJavaScriptFormula( (String)fieldFormulaVector.get(0) , formName );
            Vector fieldFormulaFunctionTypeVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,function" , fieldNode ) ;
            for( int i = 0 ; i < fieldFormulaFunctionTypeVector.size() ; i++ ) {
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onLoad") ){
                    strJavaScriptOnLoadValue = strJavaScriptValue ;
                }
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onChange") ){
                    strJavaScriptOnChangeValue = strJavaScriptValue ;
                }
            }
            // Wenn keine Funktion angegeben wurde, wird als default onChange genommen
            if( (strJavaScriptOnLoadValue + strJavaScriptOnChangeValue).trim().equals("") ){
                strJavaScriptOnChangeValue = strJavaScriptValue ;
            }
        }
        
        String fieldName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        String onChange = "" ;
        int getRow = 0 ;
        if ( uso.multiFlag == true ) {
            if ( row == -1 ) {
                onChange = "this.form.NEW.checked=true;" ;
            } else {
                fieldName =  fieldName + "_" + (String)Integer.toString( row ) ;
                onChange = "this.form.UPDATE_" + row + ".checked=true;" ;
            }
        } else {
            row = 0 ;
        }
        String fieldSize = (String)((Vector)uso.xmlDataAccess.getElementsByName( "size" , fieldNode )).get( 0 ) ;
        Vector filterNames = (Vector)uso.xmlDataAccess.getElementsByName( "filter" , fieldNode ) ;
        Vector filterMessage = (Vector)uso.xmlDataAccess.getElementsByName( "filter-message" , fieldNode ) ;
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        String optionsType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "optionsType" , fieldNode )).get( 0 ) ;
        String fieldToolTip = "" ;
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName , row , uso ) ;
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        onChange = "onChange=\"javascript:if ( checkForActiveView( " + formName + "_fn ) == 1 ) { userChanges=1;" + onChange + strJavaScriptOnChangeValue;
        String fieldAddContents = "" ;
        try {
            fieldAddContents = (String)((Vector)uso.xmlDataAccess.getElementsByName( "field-add-contents" , fieldNode )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            fieldAddContents = "" ;
        }
        if ( ! fieldAddContents.equals( "" ) ) {
            String cutEndSeperatorForCompability = "if( document." + formName + "." + fieldAddContents + ".value.substr(document." + formName + "." + fieldAddContents + ".value.length - commaSeparationTrim.length,commaSeparationTrim.length)==commaSeparationTrim )document." + formName + "." + fieldAddContents + ".value=document." + formName + "." + fieldAddContents + ".value.substring(0, document." + formName + "." + fieldAddContents + ".value.length - commaSeparationTrim.length);" ;
            String addContentsOnChange = "document." + formName + "." + fieldAddContents + ".value=document." + formName + "." + fieldAddContents + ".value+commaSeparation+this.options[this.selectedIndex].text;" ;
            String cutFirstSeperator = "if( document." + formName + "." + fieldAddContents + ".value.substr(0,commaSeparation.length)==commaSeparation )document." + formName + "." + fieldAddContents + ".value=document." + formName + "." + fieldAddContents + ".value.substring(commaSeparation.length, document." + formName + "." + fieldAddContents + ".value.length);" ;
            onChange += cutEndSeperatorForCompability + addContentsOnChange + cutFirstSeperator ;
        }
        onChange += "} \"" ;
        printWriter.println( " <script name=\"JavaScript\"> " ) ;
        printWriter.println( "       if ( navigator.appName == \"Microsoft Internet Explorer\" ) { " ) ;
        printWriter.println( "          fieldSize = " + fieldSize + " * 6 ; " ) ;
        printWriter.println( "          document.writeln( ' <select id=\"" + fieldName + "\" name=\"" + fieldName + "\" " + onChange + "> ' ) ; " ) ;
        printWriter.println( "       } else { " ) ;
        printWriter.println( "          fieldSize = " + fieldSize + " ; " ) ;
        printWriter.println( "          document.writeln( ' <select id=\"" + fieldName + "\" name=\"" + fieldName + "\" style=\"width=' + fieldSize + ';\" " + onChange + "> ' ) ; " ) ;
        printWriter.println( "       } " ) ;
        printWriter.println( "       " + strJavaScriptOnLoadValue ) ;
        printWriter.println( " </script> " ) ;
//        printWriter.println( " <select id=\"" + fieldName + "\" name=\"" + fieldName + "\" style=\"width=" + fieldSize + ";\" " + onChange + "> " ) ;
        if ( optionsType.equals( "static" ) ) {
            // ===
            // optionsType = static ( non-db-managed!!! )
            Vector optionLabel = (Vector)uso.xmlDataAccess.getElementsByName( "option,print-label" , fieldNode ) ;
            Vector optionValue = (Vector)uso.xmlDataAccess.getElementsByName( "option,value" , fieldNode ) ;
            if ( optionLabel.size() != optionValue.size() ) {
                throw new org.xml.sax.SAXException( "XMLDispatcher throws SAXException: createSelectBox examines invalid number of Option/Label and Option/Value fields." );
            }
            for ( int oi = 0 ; oi < optionLabel.size() ; oi++ ) {
                String selectedFlag = "" ;
                if ( ((String)optionValue.get( oi )).equals( fieldValue ) ) {
                    selectedFlag = "selected" ;
                }
                printWriter.println( "  <option value=\"" + (String)optionValue.get( oi ) + "\" " + selectedFlag + ">" + (String)optionLabel.get( oi ) + "</option>" ) ;
            }
        } else if ( optionsType.equals( "db-managed" ) ) {
            // ===
            // optionsType = db-managed
            // 1. get Database data
            String dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , fieldNode ) ).get( 0 ) ;
            String method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , fieldNode ) ).get( 0 ) ;
            String categoryID = "category" ;
            Vector categoryIDVector = (Vector)uso.xmlDataAccess.getElementsByName( "category-id" , fieldNode ) ;
            if ( categoryIDVector.size() > 0 ) {
                categoryID = (String)categoryIDVector.get( 0 ) ;
            }
            String category = (String)((Vector)uso.xmlDataAccess.getElementsByName( "category" , fieldNode ) ).get( 0 ) ;
            // multiple label-fields addable with seperator
            Vector labelFieldVector = (Vector)uso.xmlDataAccess.getElementsByName( "label-field" , fieldNode ) ;
            String fieldSeperator = "" ;
            if ( labelFieldVector.size() > 1 ) {
                fieldSeperator = (String)((Vector)uso.xmlDataAccess.getElementsByName( "seperator" , fieldNode ) ).get( 0 ) ;
            }
            String labelID = "id" ;
            Vector labelIDVector = (Vector)uso.xmlDataAccess.getElementsByName( "label-id" , fieldNode ) ;
            if ( labelIDVector.size() > 0 ) {
                labelID = (String)labelIDVector.get( 0 ) ;
            }
            HashMap catParamMap = uso.sharedMethods.getParams( fieldNode , uso ) ;
            boolean nps = true ;
            try {
                String nullPreSelect = (String)((Vector)uso.xmlDataAccess.getElementsByName( "null-pre-select" , fieldNode ) ).get( 0 ) ;
                if ( nullPreSelect.equals( "false" ) ) {
                    nps = false ;
                } else nps = true ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                nps = true ;
            }
            if ( ! uso.selectDBMap.containsKey( category ) ) {
                HashMap paramMap = new HashMap() ;
                paramMap.put( categoryID , category ) ;
                paramMap.putAll( catParamMap ) ;
                uso.dbMapper.DBAction( dataset , method , paramMap ) ;
                Vector selectVector = new Vector() ;
                selectVector = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;
                uso.selectDBMap.put( category , selectVector ) ;
            }
            boolean somethingSelected = false ;
            for ( int oi = 0 ; oi < ((Vector)uso.selectDBMap.get( category )).size() ; oi++ ) {
                String selectedFlag = "" ;
                String value = (String)((HashMap)((Vector)uso.selectDBMap.get( category )).get( oi ) ).get( labelID ) ;
                // multiple label-fields.
                String label = "" ;
                for ( int li = 0 ; li < labelFieldVector.size() ; li++ ) {
                    label += (String)((HashMap)((Vector)uso.selectDBMap.get( category )).get( oi ) ).get( (String)labelFieldVector.get( li ) ) ; ;
                    if ( li < labelFieldVector.size() - 1 ) {
                        label += fieldSeperator ;
                    }
                }
                if ( value.equals( fieldValue ) ) {
                    selectedFlag = "selected" ;
                    somethingSelected = true ;
                }
                printWriter.println( "  <option value=\"" + value + "\" " + selectedFlag + ">" + label + "</option>" ) ;
            }
            if ( somethingSelected == false ) {
                printWriter.print( "  <option value=\"\"" ) ;
                if ( nps == true ) printWriter.print( " selected" ) ;
                printWriter.print( ">---</option>" ) ;
            } else {
                printWriter.println( "  <option value=\"\">---</option>" ) ;
            }
        }
        printWriter.println( "</select>" ) ;
        if ( optionsType.equals( "db-managed" ) ) {
            // >>>>
            // JavaScript function to add option
            printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
            printWriter.println( "  function " + formName + "_" + fieldName + "_setOption( fieldValue , fieldHiddenValue ) { " ) ;
            printWriter.println( "    NewOption = new Option( fieldValue , fieldHiddenValue , false,true); " ) ;
            printWriter.println( "    document." + formName + "." + fieldName + ".options[document." + formName + "." + fieldName + ".length] = NewOption ; " ) ;
            printWriter.println("  } " ) ;
            printWriter.println( "</script> " ) ;
            // <<<<
            if ( ( uso.multiFlag == true && row == -1 ) || uso.multiFlag == false ) {
                boolean checkAddModule = false ;
                String addModule = "";
                try {
                    addModule= (String)((Vector)uso.xmlDataAccess.getElementsByName( "add-module" , fieldNode )).get( 0 ) ;
                    Vector editRole = (Vector)uso.xmlDataAccess.getElementsByName( "edit-role" , fieldNode) ;
                    if( editRole.size() > 0 ){
                        boolean editField = false ;
                        for ( int bi = 0 ; bi < editRole.size() ; bi++ ) {
                            if ( uso.eidpWebAppCache.userRoles_contains( (String)editRole.get( bi ) ) ) {
                                checkAddModule = true ;
                                break;
                            }
                        }
                    }else{
                        checkAddModule = true ;
                    }
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                    
                }
                if ( checkAddModule ) {
                    printWriter.println( "<a href=\"javascript:CreateNewX( '" + formName + "' , '" + fieldName + "' , '" + addModule + "' );\"><img src=\"/EIDPWebApp/images/new.jpg\" border=\"0\" title=\"Enter new Entry.\"></a>" ) ;
                }
            }
        }
        if ( ! fieldToolTip.equals( "" ) ) {
            printWriter.print( "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" );
        }
        //        printWriter.println( "</nobr> " ) ;
    }
    
    protected void createLabel( PrintWriter printWriter , NodeList fieldNode , String formName , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String label = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label" , fieldNode )).get( 0 ) ;
        // get ColorCodes:
        String labelColor = "" ;
        String formLabel = formName + "/" + label ;
        if ( uso.colorMap.containsKey( formLabel ) ) {
            labelColor = "#" + uso.colorMap.get( formLabel ) ;
        } else {
            labelColor = "#000000" ;
        }
        // calculate field value
        Vector fieldCalculateVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "calculate" , (NodeList)fieldNode ) ;
        String strJavaScriptValue = "";
        String strJavaScriptOnLoadValue = "";
        String strCalcFunctionType = "";
        
        if( fieldCalculateVector.size() > 0 ){
            Vector fieldFormulaVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,formula" , fieldNode ) ;
            strJavaScriptValue = parseJavaScriptFormula( (String)fieldFormulaVector.get(0) , formName );
            Vector fieldFormulaFunctionTypeVector = (Vector)uso.xmlDataAccess.getElementsByName( "calculate,function" , fieldNode ) ;
            for( int i = 0 ; i < fieldFormulaFunctionTypeVector.size() ; i++ ) {
                if( ((String)fieldFormulaFunctionTypeVector.get(i)).equals("onLoad") ){
                    strJavaScriptOnLoadValue = strJavaScriptValue ;
                }
            }
        }
        String strToolTip = "";
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        String fieldToolTip = "" ;
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        if ( ! fieldToolTip.equals( "" ) ) {
            strToolTip = "<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>&nbsp;" ;
        }
        printWriter.print( "<font color=\"" + labelColor + "\">" + label + "</font>" + strToolTip + "&nbsp;" ) ;
        printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\">" ) ;
        // insert the JavaSript for the destination-field: onLoad
        printWriter.println( strJavaScriptOnLoadValue ) ;
        printWriter.println( " </script>" ) ;
    }
    
    protected void createLink( PrintWriter printWriter , NodeList fieldNode , String formName , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String strDestination = (String)((Vector)uso.xmlDataAccess.getElementsByName( "destination" , fieldNode )).get( 0 ) ;
        String strLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label" , fieldNode )).get( 0 ) ;
        String strStyle = "";
        try{
            strStyle = " style=\"" +(String)((Vector)uso.xmlDataAccess.getElementsByName( "style" , fieldNode )).get( 0 ) + "\"" ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
        }
        // get ColorCodes:
        String labelColor = "" ;
        String formLabel = formName + "/" + strLabel ;
        if ( uso.colorMap.containsKey( formLabel ) ) {
            labelColor = "#" + uso.colorMap.get( formLabel ) ;
        } else {
            labelColor = "#000000" ;
        }
        printWriter.print( "<a href=\"" + strDestination + "\""  + strStyle + "><font color=\"" + labelColor + "\">" + strLabel + "</font></a>&nbsp;" ) ;
    }
    
    protected void createHiddenFields( PrintWriter printWriter , Vector hiddenFields , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        for ( int i = 0 ; i < hiddenFields.size() ; i++ ) {
            String fieldValue = "" ;
            String fieldLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)hiddenFields.get( i ) )).get( 0 ) ;
            fieldValue = uso.sharedMethods.getReferenceValue( (NodeList)hiddenFields.get( i ) , formName ,  row , uso ) ;
            printWriter.println( "<input type=\"hidden\" name=\"" + fieldLabel + "\" value=\"" + fieldValue + "\">" ) ;
        }
    }
    
    protected void createROHidden( PrintWriter printWriter , NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException, javax.servlet.ServletException {
        String fieldLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , fieldNode )).get( 0 ) ;
        if ( uso.multiFlag == true && row != -1 ) {
            fieldLabel = fieldLabel + "_" + (String)Integer.toString( row ) ;
        } else {
            row = 0 ;
        }
        Vector fieldToolTipVector = (Vector)uso.xmlDataAccess.getElementsByName( "tooltip" , fieldNode ) ;
        String fieldToolTip = "" ;
        if ( fieldToolTipVector.size() > 0 ) {
            fieldToolTip = (String)fieldToolTipVector.get( 0 ) ;
        }
        String fieldValue = uso.sharedMethods.getReferenceValue( fieldNode , formName ,  row , uso ) ;
        String showValue = fieldValue ;
        
        // Get data item from DB : David
        Vector dbManagedVector = (Vector)uso.xmlDataAccess.getElementsByName("source-type", fieldNode) ;
        if ( dbManagedVector.size() > 0 ) {
            String dbManaged = (String)dbManagedVector.get(0);
            if (dbManaged.equals("db-managed")) {
                String dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , fieldNode ) ).get( 0 ) ;
                String method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , fieldNode ) ).get( 0 ) ;
                String categoryID = "category" ;
                Vector categoryIDVector = (Vector)uso.xmlDataAccess.getElementsByName( "category-id" , fieldNode ) ;
                if ( categoryIDVector.size() > 0 ) {
                    categoryID = (String)categoryIDVector.get( 0 ) ;
                }
                String category = (String)((Vector)uso.xmlDataAccess.getElementsByName( "category" , fieldNode ) ).get( 0 ) ;
                // fix...
                String labelField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label-field" , fieldNode )).get(0) ;
                String labelID = "id" ;
                Vector labelIDVector = (Vector)uso.xmlDataAccess.getElementsByName( "label-id" , fieldNode ) ;
                if ( labelIDVector.size() > 0 ) {
                    labelID = (String)labelIDVector.get( 0 ) ;
                }
                HashMap catParamMap = uso.sharedMethods.getParams( fieldNode , uso ) ;
                if ( ! uso.selectDBMap.containsKey( category ) ) {
                    HashMap paramMap = new HashMap() ;
                    paramMap.put( categoryID , category ) ;
                    paramMap.putAll( catParamMap ) ;
                    uso.dbMapper.DBAction( dataset , method , paramMap ) ;
                    Vector selectVector = new Vector() ;
                    selectVector = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;
                    for (int i = 0; i < selectVector.size(); i++) {
                        String selectID = (String)((HashMap)selectVector.get(i)).get(labelID);
                        if (selectID.equals(fieldValue)) {
                            showValue = (String)((HashMap)selectVector.get(i)).get(labelField);
                            break;
                        }
                    }
                    
                }
            }
        }
        
        // Format date
        String strDateFormat = (String)uso.eidpWebAppCache.sessionData_get( "DateFormat" );
        if( !strDateFormat.equals( "" ) ){
            if( strDateFormat.equals( "german" ) ){
                if( this.isISODate(showValue) ){
                    showValue = this.convertISODateToEUDate( showValue ) ;
                }
            } else if (strDateFormat.equals("en_US")){
                if(this.isISODate(showValue)){
                    showValue = this.dateFormatter(showValue, "yyyy-MM-dd", "MMM dd, yyyy");
                }
            } else if (strDateFormat.equals("en_GB")){
                if(this.isISODate(showValue)){
                    showValue = this.dateFormatter(showValue, "yyyy-MM-dd", "dd.MM.yyyy");
                }
            }
        }
        //<editor-fold desc="CSS style support" defaultstate="collapsed">
        // css style in <span>
        Vector spanvec = (Vector)uso.xmlDataAccess.getElementsByName("span-css", fieldNode) ;
        if (spanvec.size() > 0) {
            String span = (String)spanvec.get(0);
            showValue = "<span style=\"" + span + "\">" + showValue + "</span>";
        }
        //</editor-fold>
        
        Vector repvec = (Vector)uso.xmlDataAccess.getElementsByName("replace", fieldNode);
        if (repvec.size() > 0) {
            String target = (String) (uso.xmlDataAccess.getElementsByName("replace,target", fieldNode)).get(0);
            String newstr = (String) (uso.xmlDataAccess.getElementsByName("replace,newstring", fieldNode)).get(0);        
            showValue = showValue.replaceAll(target, newstr);
        }
        
        Vector prevec = (Vector)uso.xmlDataAccess.getElementsByName("pre", fieldNode);
        if (prevec.size() > 0 && !(spanvec.size() > 0)) {
            showValue = "<pre>" + showValue + "</pre>";
        }
        
        printWriter.print( "<input type=\"hidden\" name=\"" + fieldLabel + "\" value=\"" + fieldValue + "\">" + showValue ) ;
        if ( ! fieldToolTip.equals( "" ) ) {
            printWriter.print( "&nbsp;&nbsp;<a href=\"javascript:TOOLTip('" + fieldToolTip + "')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" title=\"" + fieldToolTip + "\"></a>" );
        }
    }
    
    protected void createMatrix( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , NodeList viewNode , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        uso.multiFlag = false ;
        // variable for Buttons // Stephan
        String strButtons = "";
        // read data from controller.xml
        String formName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , viewNode ) ).get( 0 ) ;
        Vector writePermissions = (Vector)uso.xmlDataAccess.getElementsByName( "write-permission" , viewNode );
        // rows and columns:
        int rows = Integer.parseInt( (String)((Vector)uso.xmlDataAccess.getElementsByName( "rows" , viewNode )).get( 0 ) ) ;
        int cols = Integer.parseInt( (String)((Vector)uso.xmlDataAccess.getElementsByName( "columns" , viewNode )).get( 0 ) ) ;
        Vector fieldRows = (Vector)uso.xmlDataAccess.getElementsByName( "field,row" , viewNode ) ;
        Vector fieldCols = (Vector)uso.xmlDataAccess.getElementsByName( "field,column" , viewNode ) ;
        Vector fieldType = (Vector)uso.xmlDataAccess.getElementsByName( "field,type" , viewNode ) ;
        Vector fieldNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "field" , viewNode ) ;
        if ( fieldRows.size() != fieldCols.size() || fieldRows.size() != fieldType.size() ) {
            throw new org.xml.sax.SAXException( "XMLDispatcher throws SAXException: fieldRows, fieldCols and fieldType are not the same size." ) ;
        }
        // combined fieldRowsCols
        Vector fieldRowsCols = new Vector() ;
        for ( int i = 0 ; i < fieldRows.size() ; i++ ) {
            String colRow = (String)fieldRows.get( i ) + "," + (String)fieldCols.get( i ) ;
            fieldRowsCols.addElement( colRow ) ;
        }
        
        // Vectors that have same indexes: 1. viewNodes; 2. fieldType; 3. fieldRowCols
        // table:
        printWriter.println( "<form name=\"" + formName + "\" id=\"" + formName + "\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + uso.eidpWebAppCache.sessionData_get( "module" ) + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) + ";store&formName=" + formName + "\" method=\"POST\"> " ) ;
        printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\">" + formName + "_fn = \"" + formName + "\" ; </script> " ) ;
        // hidden fields:
        Vector hiddenFields = (Vector)uso.xmlDataAccess.getNodeListsByName( "hidden-field" , viewNode ) ;
        this.createHiddenFields( printWriter , hiddenFields , formName , 0 , uso ) ;
        // printWriter.println( "<div align=\"center\"> " ) ;
        boolean wP = true ;
        try {
            wP = this.checkPermissions( writePermissions , uso ) ;
        } catch ( java.security.AccessControlException sae ) {
            wP = false ;
        }
        // if ( wP == true ) {
        //    printWriter.println( "<input type=\"button\" value=\"Submit\" onClick=\"javascript:" + formName + "_CheckAndSubmit()\" >&nbsp;<input type=\"reset\" value=\"Clear\"> " ) ;
        // }
        
        printWriter.println( "  <table border=\"0\" cellspacing=\"0\" id=\"" + formName + "_Table\"> " ) ;
        
        
        for ( int row = 1 ; row <= rows ; row++ ) {
            printWriter.println( " <tr> " ) ;
            String fieldGroupPoint = "nogroup" ;
            int fieldGroupCellCounter = 0 ;
            
            for ( int col = 1 ; col <= cols ; col++ ) {
                String rc = row + "," + col ;
                boolean fieldGroup = false ;
                
                if ( fieldRowsCols.contains( rc ) ) {
                    int fieldIndex = fieldRowsCols.indexOf( rc ) ;
                    String fieldTypeForIndex = (String)fieldType.get( fieldIndex ) ;
                    NodeList fieldNode = (NodeList)fieldNodes.get( fieldIndex ) ;
                    String fieldColSpan = "1" ;
                    try {
                        fieldColSpan = (String)((Vector)uso.xmlDataAccess.getElementsByName( "colspan" , fieldNode )).get( 0 ) ;
                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                        fieldColSpan = "1" ;
                    }
                    // fieldGroups
                    try {
                        fieldGroupPoint = (String)((Vector)uso.xmlDataAccess.getElementsByName( "group" , fieldNode )).get( 0 ) ;
                        fieldGroupCellCounter++ ;
                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                        fieldGroupPoint = "nogroup" ;
                        fieldGroupCellCounter = 0 ;
                    }
                    
                    String fieldAlign = "left" ;
                    Vector fieldAlignVector = (Vector)uso.xmlDataAccess.getElementsByName( "align" , fieldNode );
                    // Block-roles
                    Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , fieldNode) ;
                    boolean blockField = false ;
                    for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                        if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                            blockField = true ;
                        }
                    }
                    if ( fieldAlignVector.size()  > 0 ) {
                        fieldAlign = (String)fieldAlignVector.get( 0 ) ;
                    }
                    
                    String cellOpener = "" ;
                    if ( fieldGroupPoint.equals( "start" ) || fieldGroupPoint.equals( "nogroup" ) ){
                        cellOpener = " <td class=\"input\" align=\"" + fieldAlign + "\" valign=\"middle\" colspan=\"" + fieldColSpan + "\"><nobr>" ;
                        fieldGroup = false ;
                    } else {
                        fieldGroup = true ;
                    }
                    
                    if ( blockField == false ) {
                        if ( fieldTypeForIndex.equals( "textInput" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createTextInputField( printWriter , fieldNode , formName , -1 , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "roHidden" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createROHidden( printWriter , fieldNode , formName , -1 , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "selectBox" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createSelectBox( printWriter , fieldNode , formName , -1 , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "label" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createLabel( printWriter , fieldNode , formName , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "link" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createLink( printWriter , fieldNode , formName , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "radioButton" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createRadioButton( printWriter , fieldNode , formName , -1 , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "checkBox" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createCheckBox( printWriter , fieldNode , formName , -1 , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "textArea" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "richTextArea" ) ) {
                            printWriter.print( cellOpener ) ;
                            this.createRichTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                        } else if ( fieldTypeForIndex.equals( "button" ) ) {
                            // Stephan
                            String buttonPosition = (String)((Vector)uso.xmlDataAccess.getElementsByName( "after-form" , fieldNode )).get( 0 ) ;
                            String buttondef = this.createButton( printWriter , fieldNode , formName , -1 , uso ) ;
                            printWriter.print( cellOpener ) ;
                            if( buttonPosition.equals("true") ){
                                strButtons += buttondef ;
                            }else if( buttonPosition.equals("false") ){
                                printWriter.print( buttondef ) ;
                            }
                        } else {
                            if ( !fieldGroup )
                                printWriter.print( " <td align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                        }
                    } else {
                        if ( !fieldGroup )
                            printWriter.print( " <td class=\"input\" align=\"center\" valign=\"middle\" cellpadding=\"0\" colspan=\"" + fieldColSpan + "\"><nobr>&nbsp;" ) ;
                    }
                    if ( ! fieldColSpan.equals( "1" ) ) {
                        col = col + Integer.parseInt( fieldColSpan ) - 1 ;
                    }
                    
                } else {
                    if ( !fieldGroup )
                        printWriter.println( "<td class=\"input\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                }
                if( fieldGroupPoint.equals( "end" ) || fieldGroupPoint.equals( "nogroup" ) ){
                    printWriter.println( "</nobr></td> " ) ;
                }
                // fill in empty cells for the grouped ones
                if( fieldGroupPoint.equals( "end" ) ){
                    for( int i = 0 ; i < fieldGroupCellCounter - 1 ; i++){
                        printWriter.println( "<td class=\"input\" align=\"center\" valign=\"middle\" cellpadding=\"0\">&nbsp;</td>" ) ;
                    }
                    fieldGroupCellCounter = 0 ;
                    fieldGroupPoint = "nogroup" ;
                    fieldGroup = false ;
                }
                
            }
            printWriter.println( "</tr> " ) ;
        }
        printWriter.println( "</table> " ) ;
        // Write-Permissions-Check
        if ( wP == true ) {
            printWriter.println( "<input type=\"button\" value=\"Submit\" style=\"BACKGROUND-COLOR: #DC143C; COLOR: #FFFF00\" onClick=\"javascript:" + formName + "_CheckAndSubmit()\" >&nbsp;<input type=\"reset\" value=\"Clear\" onClick=\"javascript:clearCheckForView('" + formName + "');\"> " ) ;
        }
        // Add the new defined Buttons at the end of the Table // Stephan
        printWriter.println( strButtons );
        // printWriter.println( " </div></form> " ) ;
        printWriter.println( "</form> " ) ;
        
        // change the formbordercolor of the actual view // Stephan
        this.processTableBorderColorStatic(printWriter,uso,formName);
    }
    
    protected void createMatrixList( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , NodeList viewNode , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        // MatrixList works the same way as List, just that MatrixList completely implements the
        // Structure of the List-module and subprocesses a matrix as contents.
        uso.multiFlag = true ;
        // read data from controller.xml
        String formName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , viewNode ) ).get( 0 ) ;
        Vector writePermissions = (Vector)uso.xmlDataAccess.getElementsByName( "write-permission" , viewNode );
        // rows and columns:
        int rows = Integer.parseInt( (String)((Vector)uso.xmlDataAccess.getElementsByName( "rows" , viewNode )).get( 0 ) ) ;
        int cols = Integer.parseInt( (String)((Vector)uso.xmlDataAccess.getElementsByName( "columns" , viewNode )).get( 0 ) ) ;
        Vector fieldRows = (Vector)uso.xmlDataAccess.getElementsByName( "field,row" , viewNode ) ;
        Vector fieldCols = (Vector)uso.xmlDataAccess.getElementsByName( "field,column" , viewNode ) ;
        Vector fieldType = (Vector)uso.xmlDataAccess.getElementsByName( "field,type" , viewNode ) ;
        Vector fieldNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "field" , viewNode ) ;
        if ( fieldRows.size() != fieldCols.size() || fieldRows.size() != fieldType.size() ) {
            throw new org.xml.sax.SAXException( "XMLDispatcher throws SAXException: fieldRows, fieldCols and fieldType are not the same size." ) ;
        }
        // combined fieldRowsCols
        Vector fieldRowsCols = new Vector() ;
        for ( int i = 0 ; i < fieldRows.size() ; i++ ) {
            String colRow = (String)fieldRows.get( i ) + "," + (String)fieldCols.get( i ) ;
            fieldRowsCols.addElement( colRow ) ;
        }
        if ( fieldType.size() != fieldNodes.size() ) {
            throw new org.xml.sax.SAXException( "XMLDispatcher throws SAXException (createList): fieldType, fieldLabels and fieldNodes are not the same size." ) ;
        }
        String primaryKeyID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-id,id" , viewNode ) ).get( 0 ) ;
        String primaryKeyDBRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-id,db-ref" , viewNode ) ).get( 0 ) ;
        String primaryKeyNewValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-id,new-value" , viewNode ) ).get( 0 ) ;
        // create form and table
        printWriter.println( "<form name=\"" + formName + "\" id=\"" + formName + "\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + uso.eidpWebAppCache.sessionData_get( "module" ) + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) + ";store&formName=" + formName + "\" method=\"POST\"> " ) ;
        printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\">" + formName + "_fn = \"" + formName + "\" ; </script> " ) ;
        // >>>>>>>>>>>>>>>>>>>>>>>
        // hidden fields:
        Vector hiddenFields = (Vector)uso.xmlDataAccess.getNodeListsByName( "hidden-field" , viewNode ) ;
        this.createHiddenFields( printWriter , hiddenFields , formName , -1 , uso ) ;
        // <<<<<<<<<<<<<<<<<<<<<<<
        boolean wP = true ;
        try {
            wP = this.checkPermissions( writePermissions , uso ) ;
        } catch ( java.security.AccessControlException sae ) {
            wP = false ;
        }
        if ( wP == true ) {
            printWriter.println( "<br>" ) ;
            printWriter.println( "<input type=\"button\" value=\"Submit\" style=\"BACKGROUND-COLOR: #DC143C; COLOR: #FFFF00\" onClick=\"javascript:" + formName + "_CheckAndSubmit()\" >&nbsp;<input type=\"reset\" value=\"Clear\" onClick=\"javascript:clearCheckForView('" + formName + "');\"> " ) ;
        }
        // >>>>>>>>>>>>>>>>>>>>>>>>>
        // write matrix
        printWriter.println( "  <table border=\"0\" cellspacing=\"0\"> " ) ;
        printWriter.println( "      <input type=\"hidden\" name=\"" + primaryKeyID + "\" value=\"" + primaryKeyNewValue + "\"> " ) ;
        
        String fieldGroupPoint = "nogroup" ;
        int fieldGroupCellCounter = 0 ;
        
        for ( int row = 1 ; row <= rows ; row++ ) {
            
            for ( int col = 0 ; col <= cols ; col++ ) {
                boolean fieldGroup = false ;
                if ( col == 0 ) {
                    if ( row == 1 ) {
                        printWriter.println( "      <tr><td class=\"inputNEW\" align=\"center\" valign=\"middle\"><input type=\"checkbox\" name=\"NEW\" value=\"true\"></td> " ) ;
                    } else {
                        printWriter.println( "      <tr><td class=\"inputNEW\">&nbsp;</td> " ) ;
                    }
                } else {
                    String rc = row + "," + col ;
                    if ( fieldRowsCols.contains( rc ) ) {
                        int fieldIndex = fieldRowsCols.indexOf( rc ) ;
                        String fieldTypeForIndex = (String)fieldType.get( fieldIndex ) ;
                        NodeList fieldNode = (NodeList)fieldNodes.get( fieldIndex ) ;
                        String fieldColSpan = "1" ;
                        try {
                            fieldColSpan = (String)((Vector)uso.xmlDataAccess.getElementsByName( "colspan" , fieldNode )).get( 0 ) ;
                        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                            fieldColSpan = "1" ;
                        }
                        String fieldAlign = "left" ;
                        Vector fieldAlignVector = (Vector)uso.xmlDataAccess.getElementsByName( "align" , fieldNode );
                        if ( fieldAlignVector.size()  > 0 ) {
                            fieldAlign = (String)fieldAlignVector.get( 0 ) ;
                        }
                        // fieldGroups
                        try {
                            fieldGroupPoint = (String)((Vector)uso.xmlDataAccess.getElementsByName( "group" , fieldNode )).get( 0 ) ;
                            fieldGroupCellCounter++ ;
                        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                            fieldGroupPoint = "nogroup" ;
                            fieldGroupCellCounter = 0 ;
                        }
                        
                        // Block-roles
                        Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , fieldNode) ;
                        boolean blockField = false ;
                        for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                            if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                                blockField = true ;
                            }
                        }
                        if ( fieldAlignVector.size()  > 0 ) {
                            fieldAlign = (String)fieldAlignVector.get( 0 ) ;
                        }
                        String cellOpener = "" ;
                        if ( fieldGroupPoint.equals( "start" ) || fieldGroupPoint.equals( "nogroup" ) ){
                            cellOpener = " <td class=\"inputNEW\" align=\"" + fieldAlign + "\" valign=\"middle\" colspan=\"" + fieldColSpan + "\"><nobr>" ;
                            fieldGroup = false ;
                        } else {
                            fieldGroup = true ;
                        }
                        if ( blockField == false ) {
                            if ( fieldTypeForIndex.equals( "textInput" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createTextInputField( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "roHidden" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createROHidden( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "selectBox" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createSelectBox( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "label" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createLabel( printWriter , fieldNode , formName , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "link" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createLink( printWriter , fieldNode , formName , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "radioButton" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createRadioButton( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "checkBox" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createCheckBox( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "textArea" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "richTextArea" ) ) {
                                printWriter.print( cellOpener ) ;
                                this.createRichTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "hidden" ) ) {
                                this.createMultiHiddenField( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else {
                                if ( !fieldGroup )
                                    printWriter.print( " <td class=\"inputNEW\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                            }
                        } else {
                            if ( !fieldGroup )
                                printWriter.print( " <td class=\"inputNEW\" align=\"center\" valign=\"middle\" cellpadding=\"0\"  colspan=\"" + fieldColSpan + "\"><nobr>&nbsp;" ) ;
                        }
                        if ( ! fieldColSpan.equals( "1" ) ) {
                            col = col + Integer.parseInt( fieldColSpan ) - 1 ;
                        }
                    } else {
                        if ( !fieldGroup )
                            printWriter.println( "<td class=\"inputNEW\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                    }
                    if( fieldGroupPoint.equals( "end" ) || fieldGroupPoint.equals( "nogroup" ) ){
                        printWriter.println( "</nobr></td> " ) ;
                    }
                    // fill in empty cells for the grouped ones
                    if( fieldGroupPoint.equals( "end" ) ){
                        for( int i = 0 ; i < fieldGroupCellCounter - 1 ; i++){
                            printWriter.println( "<td class=\"inputNEW\" align=\"center\" valign=\"middle\" cellpadding=\"0\">&nbsp;</td>" ) ;
                        }
                        fieldGroupCellCounter = 0 ;
                        fieldGroupPoint = "nogroup" ;
                        fieldGroup = false ;
                    }
                }
            }
            if ( row > 0 ) {
                printWriter.println( "</tr> " ) ;
            }
        }
        printWriter.println( "</table> " ) ;
        // <<<<<<<<<<<<<<<<<<<<<<<<<
        
        // printWriter.println( " </div>" ) ;
        // ===
        // Create Multi-Table
        // ===
        // printWriter.println( "<div align=\"center\"> " ) ;
        printWriter.println( "<br> " ) ;
        // catch NullPointerException if preLoadData isNull
        boolean plDataIsNull = false ;
        Vector plData = (Vector)uso.preLoadData.get( formName ) ;
        
        try{
            plData.get(0);
        }catch(java.lang.NullPointerException e){
            plDataIsNull = true ;
        }catch(java.lang.ArrayIndexOutOfBoundsException e){
            plDataIsNull = true ;
        }
        //        if ( plData.size() == 0 ) {
        //            printWriter.println( " <table border=\"0\" cellspacing=\"0\"><tr><td class=\"input\" align=\"center\" valign=\"middle\">No data available</td></tr></table> " ) ;
        //        } else {
        if ( !plDataIsNull ) {
            
            if ( plData.size() != 0 ) {
                
                printWriter.println( " <table border=\"0\" cellspacing=\"0\"> " ) ;
                int pli ;
                for ( pli = 0 ; pli < plData.size() ; pli++ ) {
                    String colorPicker = "inputContrast" ;
                    float testEven = pli % 2 ;
                    if ( testEven == 0 ) { colorPicker = "inputContrast" ; } else { colorPicker = "input" ; }
                    printWriter.println( "      <tr> " ) ;
                    printWriter.println( "      <input type=\"hidden\" name=\"" + primaryKeyID + "_" + pli + "\" value=\"" + (String)((HashMap)plData.get( pli )).get( primaryKeyDBRef ) + "\"> " ) ;
                    
                    fieldGroupPoint = "nogroup" ;
                    fieldGroupCellCounter = 0 ;
                    
                    for ( int row = 1 ; row <= rows ; row++ ) {
                        printWriter.println( " <tr> " ) ;
                        for ( int col = 0 ; col <= cols ; col++ ) {
                            boolean fieldGroup = false ;
                            if ( col == 0 ) {
                                if ( row == 1 ) {
                                    printWriter.println( "      <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\"><input type=\"checkbox\" name=\"UPDATE_" + pli + "\" value=\"true\"></td> " ) ;
                                } else {
                                    printWriter.println( "<td class=\"" + colorPicker + "\">&nbsp;</td> " ) ;
                                }
                            } else {
                                String rc = row + "," + col ;
                                if ( fieldRowsCols.contains( rc ) ) {
                                    int fieldIndex = fieldRowsCols.indexOf( rc ) ;
                                    String fieldTypeForIndex = (String)fieldType.get( fieldIndex ) ;
                                    NodeList fieldNode = (NodeList)fieldNodes.get( fieldIndex ) ;
                                    String fieldColSpan = "1" ;
                                    try {
                                        fieldColSpan = (String)((Vector)uso.xmlDataAccess.getElementsByName( "colspan" , fieldNode )).get( 0 ) ;
                                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                                        fieldColSpan = "1" ;
                                    }
                                    // fieldGroups
                                    try {
                                        fieldGroupPoint = (String)((Vector)uso.xmlDataAccess.getElementsByName( "group" , fieldNode )).get( 0 ) ;
                                        fieldGroupCellCounter++ ;
                                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                                        fieldGroupPoint = "nogroup" ;
                                        fieldGroupCellCounter = 0 ;
                                    }
                                    String fieldAlign = "left" ;
                                    Vector fieldAlignVector = (Vector)uso.xmlDataAccess.getElementsByName( "align" , fieldNode );
                                    if ( fieldAlignVector.size()  > 0 ) {
                                        fieldAlign = (String)fieldAlignVector.get( 0 ) ;
                                    }
                                    // Block-roles
                                    Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , fieldNode) ;
                                    boolean blockField = false ;
                                    for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                                        if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                                            blockField = true ;
                                        }
                                    }
                                    if ( fieldAlignVector.size()  > 0 ) {
                                        fieldAlign = (String)fieldAlignVector.get( 0 ) ;
                                    }
                                    String cellOpener = "" ;
                                    if ( fieldGroupPoint.equals( "start" ) || fieldGroupPoint.equals( "nogroup" ) ){
                                        cellOpener = " <td class=\"" + colorPicker + "\" align=\"" + fieldAlign + "\" valign=\"middle\" colspan=\"" + fieldColSpan + "\"><nobr>" ;
                                        fieldGroup = false ;
                                    } else {
                                        fieldGroup = true ;
                                    }
                                    if ( blockField == false ) {
                                        if ( fieldTypeForIndex.equals( "textInput" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createTextInputField( printWriter , fieldNode , formName , pli , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "roHidden" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createROHidden( printWriter , fieldNode , formName , pli , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "selectBox" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createSelectBox( printWriter , fieldNode , formName , pli , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "label" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createLabel( printWriter , fieldNode , formName , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "link" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createLink( printWriter , fieldNode , formName , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "radioButton" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createRadioButton( printWriter , fieldNode , formName , pli , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "checkBox" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createCheckBox( printWriter , fieldNode , formName , pli , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "textArea" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createTextArea( printWriter , fieldNode , formName , pli , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "richTextArea" ) ) {
                                            printWriter.print( cellOpener ) ;
                                            this.createRichTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                                        } else if ( fieldTypeForIndex.equals( "hidden" ) ) {
                                            this.createMultiHiddenField( printWriter , fieldNode , formName , pli , uso ) ;
                                        } else {
                                            if ( !fieldGroup )
                                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                                        }
                                    } else {
                                        if ( !fieldGroup )
                                            printWriter.print( " <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                                    }
                                    if ( ! fieldColSpan.equals( "1" ) ) {
                                        col = col + Integer.parseInt( fieldColSpan ) - 1 ;
                                    }
                                } else {
                                    if ( !fieldGroup )
                                        printWriter.println( "<td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                                }
                                if( fieldGroupPoint.equals( "end" ) || fieldGroupPoint.equals( "nogroup" ) ){
                                    printWriter.println( "</nobr></td> " ) ;
                                }
                                // fill in empty cells for the grouped ones
                                if( fieldGroupPoint.equals( "end" ) ){
                                    for( int i = 0 ; i < fieldGroupCellCounter - 1 ; i++){
                                        printWriter.println( "<td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\" cellpadding=\"0\">&nbsp;</td>" ) ;
                                    }
                                    fieldGroupCellCounter = 0 ;
                                    fieldGroupPoint = "nogroup" ;
                                    fieldGroup = false ;
                                }
                            }
                        }
                        printWriter.println( "</tr> " ) ;
                    }
                    
                    
                }
                printWriter.println( "  <input type=\"hidden\" name=\"MAXROWS\" value=\"" + pli + "\" >" ) ;
            }
            printWriter.println( "  </table> " ) ;
            //        if ( wP == true ) {
            //            printWriter.println( "<input type=\"button\" value=\"Submit\" onClick=\"javascript:" + formName + "_CheckAndSubmit()\" >&nbsp;<input type=\"reset\" value=\"Clear\"> " ) ;
            //        }
            // printWriter.println( " </div>" ) ;
            
        }
        printWriter.println( "</form>" ) ;
    }
    
    protected void loadAddIn( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , NodeList viewNode , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        String className = (String)((Vector)uso.xmlDataAccess.getElementsByName( "class-name" , viewNode ) ).get( 0 );
        try {
            EIDPAddInLoader eidpAddInLoader = new EIDPAddInLoader( className , printWriter , request , response , uso ) ;
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
    }
    
    protected void createList( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , NodeList viewNode , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        uso.multiFlag = true ;
        // LIST_OPTIONS STEPHAN
        boolean createCheckBox = true ;
        String strEmptyMessage = "";
        String strRedirectModule = "";
        String test = "";
        // read data from controller.xml
        try{
            test = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-options,checkbox" , viewNode ) ).get( 0 );
            if(test.equals("false")){
                createCheckBox = false;
            }
        }catch(ArrayIndexOutOfBoundsException e){
        }
        try{
            strEmptyMessage = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-options,empty-message" , viewNode ) ).get( 0 ) ;
        }catch(ArrayIndexOutOfBoundsException e){
        }
        try{
            strRedirectModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-options,redirect-module" , viewNode ) ).get( 0 ) ;
        }catch(ArrayIndexOutOfBoundsException e){
        }
        String formName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , viewNode ) ).get( 0 ) ;
        Vector writePermissions = (Vector)uso.xmlDataAccess.getElementsByName( "write-permission" , viewNode );
        Vector fieldType = (Vector)uso.xmlDataAccess.getElementsByName( "field,type" , viewNode ) ;
        Vector fieldNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "field" , viewNode ) ;
        Vector fieldLabels = (Vector)uso.xmlDataAccess.getNodeListsByName( "field,label" , viewNode ) ;
        // fieldLabels cannot be included in the size-check since hidden fields do not
        // contain labels
        if ( fieldType.size() != fieldNodes.size() ) {
            throw new org.xml.sax.SAXException( "XMLDispatcher throws SAXException (createList): fieldType, fieldLabels and fieldNodes are not the same size." ) ;
        }
        String primaryKeyID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-id,id" , viewNode ) ).get( 0 ) ;
        String primaryKeyDBRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-id,db-ref" , viewNode ) ).get( 0 ) ;
        String primaryKeyNewValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-id,new-value" , viewNode ) ).get( 0 ) ;
        // create form and table
        printWriter.println( "<form name=\"" + formName + "\" id=\"" + formName + "\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + uso.eidpWebAppCache.sessionData_get( "module" ) + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) + ";store&formName=" + formName + "\" method=\"POST\"> " ) ;
        printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\">" + formName + "_fn = \"" + formName + "\" ; </script> " ) ;
        // create NEW table
        // printWriter.println( "<div align=\"center\"> " ) ;
        boolean wP = true ;
        try {
            wP = this.checkPermissions( writePermissions , uso ) ;
        } catch ( java.security.AccessControlException sae ) {
            wP = false ;
        }
        boolean noNew = false ;
        Vector noNewVector = (Vector)uso.xmlDataAccess.getElementsByName( "no-new" , viewNode ) ;
        if ( noNewVector.size() > 0 ) {
            if ( ((String)noNewVector.get( 0 )).equals( "true" ) ) {
                noNew = true ;
            }
        }
        if ( noNew == false ) {
            if ( wP == true ) {
                printWriter.println( "<br>" ) ;
                printWriter.println( "<input type=\"button\" value=\"Submit\" style=\"BACKGROUND-COLOR: #DC143C; COLOR: #FFFF00\" onClick=\"javascript:" + formName + "_CheckAndSubmit()\" >&nbsp;<input type=\"reset\" value=\"Clear\" onClick=\"javascript:clearCheckForView('" + formName + "');\"> " ) ;
            }
            printWriter.println( " <table border=\"0\" cellspacing=\"0\"> " ) ;
            printWriter.println( "      <tr>" ) ;
            printWriter.println( "      <td class=\"inputNEW\" align=\"center\" valign=\"middle\">&nbsp;</td> " ) ;
            for ( int i = 0 ; i < fieldLabels.size() ; i++ ) {
                String labelName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)fieldLabels.get( i ) ) ).get( 0 ) ;
                String colSpan = "" ;
                try {
                    colSpan = (String)((Vector)uso.xmlDataAccess.getElementsByName( "colspan" , (NodeList)fieldLabels.get( i ) ) ).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                    colSpan = "" ;
                }
                String labelColor = "" ;
                String formLabel = formName + "/" + labelName ;
                if ( uso.colorMap.containsKey( formLabel ) ) {
                    labelColor = "#" + uso.colorMap.get( formLabel ) ;
                } else {
                    labelColor = "#000000" ;
                }
                if ( colSpan.equals( "" ) ) {
                    printWriter.println( "      <td class=\"inputNEW\" align=\"center\" valign=\"middle\"> " ) ;
                } else {
                    printWriter.println( "      <td class=\"inputNEW\" align=\"center\" valign=\"middle\" colspan=\"" + colSpan + "\"> " ) ;
                }
                printWriter.println( "          <font color=\"" + labelColor + "\">&nbsp;" + labelName + "&nbsp;</font>" ) ;
                printWriter.println( "      </td> " ) ;
            }
            printWriter.println( "      </tr>" ) ;
            printWriter.println( "      <input type=\"hidden\" name=\"" + primaryKeyID + "\" value=\"" + primaryKeyNewValue + "\"> " ) ;
            
            printWriter.print( "      <td class=\"inputNEW\" align=\"center\" valign=\"middle\">" ) ;
            if(createCheckBox)
                printWriter.print( "<input type=\"checkbox\" style=\"visibility: visible;\" name=\"NEW\" value=\"true\">" ) ;
            else
                printWriter.print( "<input type=\"checkbox\" style=\"visibility: hidden;\" name=\"NEW\" value=\"true\">" ) ;
            printWriter.println( "      </td> " ) ;
            for ( int i = 0 ; i < fieldNodes.size() ; i++ ) {
                String fieldTypeForIndex = (String)fieldType.get( i ) ;
                NodeList fieldNode = (NodeList)fieldNodes.get( i ) ;
                // Block-roles
                Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , fieldNode) ;
                boolean blockField = false ;
                for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                    if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                        blockField = true ;
                    }
                }
                String cellOpener = " <td class=\"inputNEW\" align=\"left\" valign=\"middle\"><nobr>" ;
                if ( blockField == false ) {
                    if ( fieldTypeForIndex.equals( "listSelect" ) ) {
                        printWriter.println( cellOpener ) ;
                        this.createListSelect( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "textInput" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createTextInputField( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "roHidden" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createROHidden( printWriter , fieldNode , formName ,  -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "selectBox" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createSelectBox( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "label" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createLabel( printWriter , fieldNode , formName , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "link" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createLink( printWriter , fieldNode , formName , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "radioButton" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createRadioButton( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "checkBox" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createCheckBox( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "textArea" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "richTextArea" ) ) {
                        printWriter.print( cellOpener ) ;
                        this.createRichTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else if ( fieldTypeForIndex.equals( "hidden" ) ) {
                        this.createMultiHiddenField( printWriter , fieldNode , formName , -1 , uso ) ;
                    } else {
                        printWriter.print( " <td class=\"inputNEW\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                    }
                } else {
                    printWriter.print( " <td class=\"inputNEW\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                }
                String newLine = "" ;
                try {
                    newLine = (String)((Vector)uso.xmlDataAccess.getElementsByName( "newline" , fieldNode )).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                    newLine = "" ;
                }
                printWriter.println( " </nobr></td>" ) ;
                if ( newLine.equals( "true" ) ) {
                    printWriter.println(" </tr> <tr> " ) ;
                }
            }
            
            printWriter.println( "</tr>" ) ;
            printWriter.println( "</table>" ) ;
            
            // printWriter.println( " </div>" ) ;
            // ===
            // Create Multi-Table
            // ===
            // printWriter.println( "<div align=\"center\"> " ) ;
            printWriter.println( "<br> " ) ;
        }
        // catch NullPointerException if preLoadData isNull
        boolean plDataIsNull = false ;
        Vector plData = (Vector)uso.preLoadData.get( formName ) ;
        try{
            plData.get(0);
        }catch(java.lang.NullPointerException e){
            plDataIsNull = true ;
        }catch(java.lang.ArrayIndexOutOfBoundsException e){
            plDataIsNull = true ;
        }
        //        if ( plData.size() == 0 ) {
        //            printWriter.println( " <table border=\"0\" cellspacing=\"0\"><tr><td class=\"input\" align=\"center\" valign=\"middle\">No data available</td></tr></table> " ) ;
        //        } else {
        if ( !plDataIsNull ) {
            if ( plData.size() != 0 ) {
                printWriter.println( " <table border=\"0\" cellspacing=\"0\"> " ) ;
                printWriter.println( "      <tr>" ) ;
                printWriter.println( "      <td class=\"inputNEW\" align=\"center\" valign=\"middle\">&nbsp;</td>" ) ;
                // for ( int i = 0 ; i < fieldLabels.size() ; i++ ) {
                //    String labelColor = "" ;
                //    String formLabel = formName + "/" + (String)fieldLabels.get( i ) ;
                //    if ( uso.colorMap.containsKey( formLabel ) ) {
                //        labelColor = "#" + uso.colorMap.get( formLabel ) ;
                //    } else {
                //        labelColor = "#000000" ;
                //    }
                //    printWriter.println( "      <td class=\"input\" align=\"center\" valign=\"middle\"> " ) ;
                //    printWriter.println( "          <font color=\"" + labelColor + "\">&nbsp;" + (String)fieldLabels.get( i ) + "&nbsp;</font>" ) ;
                //    printWriter.println( "      </td> " ) ;
                // }
                for ( int i = 0 ; i < fieldLabels.size() ; i++ ) {
                    String labelName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)fieldLabels.get( i ) ) ).get( 0 ) ;
                    String colSpan = "" ;
                    try {
                        colSpan = (String)((Vector)uso.xmlDataAccess.getElementsByName( "colspan" , (NodeList)fieldLabels.get( i ) ) ).get( 0 ) ;
                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                        colSpan = "" ;
                    }
                    String labelColor = "" ;
                    String formLabel = formName + "/" + labelName ;
                    if ( uso.colorMap.containsKey( formLabel ) ) {
                        labelColor = "#" + uso.colorMap.get( formLabel ) ;
                    } else {
                        labelColor = "#000000" ;
                    }
                    if ( colSpan.equals( "" ) ) {
                        printWriter.println( "      <td class=\"inputNEW\" align=\"center\" valign=\"middle\"> " ) ;
                    } else {
                        printWriter.println( "      <td class=\"inputNEW\" align=\"center\" valign=\"middle\" colspan=\"" + colSpan + "\"> " ) ;
                    }
                    printWriter.println( "          <font color=\"" + labelColor + "\">&nbsp;" + labelName + "&nbsp;</font>" ) ;
                    printWriter.println( "      </td> " ) ;
                }
                printWriter.println( "      </tr>" ) ;
                int pli ;
                for ( pli = 0 ; pli < plData.size() ; pli++ ) {
                    String colorPicker = "inputContrast" ;
                    float testEven = pli % 2 ;
                    if ( testEven == 0 ) { colorPicker = "inputContrast" ; } else { colorPicker = "input" ; }
                    printWriter.println( "      <tr> " ) ;
                    printWriter.println( "      <input type=\"hidden\" name=\"" + primaryKeyID + "_" + pli + "\" value=\"" + (String)((HashMap)plData.get( pli )).get( primaryKeyDBRef ) + "\"> " ) ;
                    if(createCheckBox)
                        printWriter.println( "        <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\"><input type=\"checkbox\" style=\"visibility: visible;\" name=\"UPDATE_" + pli + "\" value=\"true\"></td>");
                    else
                        printWriter.println( "        <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\"><input type=\"checkbox\" style=\"visibility: hidden;\" name=\"UPDATE_" + pli + "\" value=\"true\"></td>");
                    for ( int i = 0 ; i < fieldNodes.size() ; i++ ) {
                        String fieldTypeForIndex = (String)fieldType.get( i ) ;
                        NodeList fieldNode = (NodeList)fieldNodes.get( i ) ;
                        String align = "";
                        try {
                            align = (String) ((Vector)uso.xmlDataAccess.getElementsByName("align", fieldNode)).get(0);
                        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {}
                        // Block-roles
                        Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , fieldNode) ;
                        boolean blockField = false ;
                        for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                            if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                                blockField = true ;
                            }
                        }
                        if ( blockField == false ) {
                            if ( fieldTypeForIndex.equals( "listSelect" ) ) {
                                String textalign = "left";
                                if (!align.equals("")) textalign = align;
                                printWriter.println( " <td class=\"" + colorPicker + "\" align=\"" + textalign + "\" valign=\"middle\"><nobr>" ) ;
                                this.createListSelect( printWriter , fieldNode , formName , pli , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "textInput" ) ) {
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\"><nobr>" ) ;
                                this.createTextInputField( printWriter , fieldNode , formName , pli , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "roHidden" ) ) {
                                String textalign = "center";
                                if (!align.equals("")) textalign = align;
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"" + textalign + "\" valign=\"middle\"><nobr>" ) ;
                                this.createROHidden( printWriter , fieldNode , formName , pli , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "selectBox" ) ) {
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\"><nobr>" ) ;
                                this.createSelectBox( printWriter , fieldNode , formName , pli , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "label" ) ) {
                                String textalign = "center";
                                if (!align.equals("")) textalign = align;
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"" + textalign + "\" valign=\"middle\"><nobr>" ) ;
                                this.createLabel( printWriter , fieldNode , formName , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "link" ) ) {
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\"><nobr>" ) ;
                                this.createLink( printWriter , fieldNode , formName , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "radioButton" ) ) {
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"left\" valign=\"middle\"><nobr>" ) ;
                                this.createRadioButton( printWriter , fieldNode , formName , pli , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "checkBox" ) ) {
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"left\" valign=\"middle\"><nobr>" ) ;
                                this.createCheckBox( printWriter , fieldNode , formName , pli , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "textArea" ) ) {
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"left\" valign=\"middle\"><nobr>" ) ;
                                this.createTextArea( printWriter , fieldNode , formName , pli , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "richTextArea" ) ) {
                                printWriter.print( " <td class=\"" + colorPicker + "\" align=\"left\" valign=\"middle\"><nobr>" ) ;
                                this.createRichTextArea( printWriter , fieldNode , formName , -1 , uso ) ;
                            } else if ( fieldTypeForIndex.equals( "hidden" ) ) {
                                this.createMultiHiddenField( printWriter , fieldNode , formName , pli , uso ) ;
                            } else {
                                printWriter.println( " <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                            }
                        } else {
                            printWriter.println( " <td class=\"" + colorPicker + "\" align=\"center\" valign=\"middle\" cellpadding=\"0\"><nobr>&nbsp;" ) ;
                        }
                        //printWriter.println( " </nobr></td>" ) ;
                        
                        // -------- START NEW BLOCK 
                        String newLine = "" ;
                try {
                    newLine = (String)((Vector)uso.xmlDataAccess.getElementsByName( "newline" , fieldNode )).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                    newLine = "" ;
                }
                printWriter.println( " </nobr></td>" ) ;
                if ( newLine.equals( "true" ) ) {
                    printWriter.println(" </tr> <tr> <td></td>" ) ;
                }
                
                        // ----------- END NEW BLOCK
                
                    }
                    printWriter.println( "      </tr> " ) ;
                }
                printWriter.println( "  <input type=\"hidden\" name=\"MAXROWS\" value=\"" + pli + "\" >" ) ;
            }else{
                if(noNew){
                    if(strEmptyMessage.trim().equals(""))
                        printWriter.print( "<h2><a href=\"javascript:self.close();\" title=\"click to close window\">No data found.</a></h2>" ) ;
                    else
                        printWriter.print( "<h2><a href=\"javascript:self.close();\" title=\"click to close window\">" + strEmptyMessage + "</a></h2>" ) ;
                    
                    //printWriter.print( "<table border=\"0\"><tr><td><nobr><input type=\"button\" onClick=\"javascript:self.close();\" title=\"click to close window\" value=\"Fenster schlie&szlig;en\" >" ) ;
                    if(!strRedirectModule.trim().equals("")){
                        printWriter.print( "<input type=\"button\" style=\"BACKGROUND-COLOR: #006666; COLOR: #FFFF00\" onClick=\"javascript:window.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + strRedirectModule + ";show';\" title=\"neue Abfrage starten\" value=\"Neue Abfrage\" >" ) ;
                    }
                    printWriter.print( "</nobr></td></tr></table>");
                }
            }
            printWriter.println( "  </table> " ) ;
            
            //        if ( wP == true ) {
            //            printWriter.println( "<input type=\"button\" value=\"Submit\" onClick=\"javascript:" + formName + "_CheckAndSubmit()\" >&nbsp;<input type=\"reset\" value=\"Clear\"> " ) ;
            //        }
            // printWriter.println( " </div>" ) ;
            
        }else{
            if(noNew){
                if(strEmptyMessage.trim().equals(""))
                    printWriter.print( "<h2><a href=\"javascript:self.close();\" title=\"click to close window\">No data found.</a></h2>" ) ;
                else
                    printWriter.print( "<h2><a href=\"javascript:self.close();\" title=\"click to close window\">" + strEmptyMessage + "</a></h2>" ) ;
                
                //printWriter.print( "<table border=\"0\"><tr><td><nobr><input type=\"button\" onClick=\"javascript:self.close();\" title=\"click to close window\" value=\"Fenster schlie&szlig;en\" >" ) ;
                if(!strRedirectModule.trim().equals("")){
                    printWriter.print( "<input type=\"button\" style=\"BACKGROUND-COLOR: #006666; COLOR: #FFFF00\" onClick=\"javascript:window.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + strRedirectModule + ";show';\" title=\"neue Abfrage starten\" value=\"Neue Abfrage\" >" ) ;
                }
                printWriter.print( "</nobr></td></tr></table>");
            }
        }
        printWriter.println( "</form>" ) ;
    }
    
    protected void createText( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , NodeList viewNode , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        // read data from controller.xml
        Vector writePermissions = (Vector)uso.xmlDataAccess.getElementsByName( "write-permission" , viewNode );
        String text = (String)((Vector)uso.xmlDataAccess.getElementsByName( "text" , viewNode ) ).get( 0 ) ;
        String setOpenerType = "" ;
        try {
            setOpenerType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,type" , viewNode ) ) .get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            setOpenerType = "" ;
        }
        String formName = "" ;
        String formHiddenField = "" ;
        String formField = "" ;
        String fieldHiddenValue = "" ;
        String fieldValue = "" ;
        if ( ! setOpenerType.equals( "" ) ) {
            if ( setOpenerType.equals( "selectBox" ) ) {
                formName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,form-name" , viewNode ) ).get( 0 ) ;
                formField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,form-field" , viewNode ) ).get( 0 ) ;
                // multiple options with seperator:
                // ACHTUNG: VALUE = LABEL UND OPTION = HIDDEN !!!
                Vector labelValueVector = (Vector)uso.xmlDataAccess.getElementsByName( "set-opener,value" , viewNode ) ;
                String labelValueSeperator = "" ;
                if ( labelValueVector.size() > 1 ) {
                    labelValueSeperator = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,seperator" , viewNode ) ).get( 0 ) ;
                }
                for ( int oi = 0 ; oi < labelValueVector.size() ; oi++ ) {
                    fieldValue += (String)uso.eidpWebAppCache.sessionData_get( (String)labelValueVector.get( oi ) ) ;
                    if ( oi < labelValueVector.size() - 1 ) {
                        fieldValue += labelValueSeperator ;
                    }
                }
                fieldHiddenValue = (String)uso.eidpWebAppCache.sessionData_get( (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,option" , viewNode ) ).get( 0 ) ) ;
                for ( int oi = 0 ; oi < labelValueVector.size() ; oi++ ) {
                    uso.eidpWebAppCache.sessionData_remove( (String)labelValueVector.get( oi ) ) ;
                }
                uso.eidpWebAppCache.sessionData_remove( (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,value" , viewNode ) ).get( 0 ) ) ;
            } else if ( setOpenerType.equals( "text" ) ) {
                formName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,form-name" , viewNode ) ).get( 0 ) ;
                formHiddenField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,form-primary-field" , viewNode ) ).get( 0 ) ;
                formField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,form-secondary-field" , viewNode ) ).get( 0 ) ;
                fieldHiddenValue = (String)uso.eidpWebAppCache.sessionData_get( (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,hidden-value" , viewNode ) ).get( 0 ) ) ;
                fieldValue = (String)uso.eidpWebAppCache.sessionData_get( (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,value" , viewNode ) ).get( 0 ) ) ;
                uso.eidpWebAppCache.sessionData_remove( (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,hidden-value" , viewNode ) ).get( 0 ) ) ;
                uso.eidpWebAppCache.sessionData_remove( (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-opener,value" , viewNode ) ).get( 0 ) ) ;
            }
        }
        boolean close = false ;
        try {
            String closeThisWindow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "close" , viewNode ) ).get( 0 ) ;
            close = true ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            close = false ;
        }
        // 1. Write the text
        printWriter.println( "<p>" + text + "</p>" ) ;
        // 2. set opener window options
        if ( ! setOpenerType.equals( "" ) ) {
            if ( setOpenerType.equals( "selectBox" ) ) {
                printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
                printWriter.println( "  opener." + formName + "_" + formField + "_setOption( \"" + fieldValue + "\" , \"" + fieldHiddenValue + "\" ) ; " ) ;
                // printWriter.println( "  NewOption = new Option( \"" + fieldValue + "\" , \"" + fieldHiddenValue + "\" , false,true); " ) ;
                // printWriter.println( "  opener.document." + formName + "." + formField + ".options[opener.document." + formName + "." + formField + ".length] = NewOption ; " ) ;
                printWriter.println( " window.close() ; " ) ;
                printWriter.println( "</script> " ) ;
            } else if ( setOpenerType.equals( "text" ) ) {
                printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
                printWriter.println( "  opener.document." + formName + "." + formHiddenField + ".value = \"" + fieldHiddenValue + "\" ; " ) ;
                printWriter.println( "  opener.document." + formName + "." + formField + ".value = \"" + fieldValue + "\" ; " ) ;
                printWriter.println( "  window.close() ; " ) ;
                printWriter.println( "</script> " ) ;
            } else if ( setOpenerType.equals( "none" ) ) {
                printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
                printWriter.println( "  window.close() ; " ) ;
                printWriter.println( "</script> " ) ;
            }
        }
        // 3. close if close = true ;
    }
    
    // true wenn eine Exception vorhanden ist!!!
    // Deshalb in if (row != -1) und maxrows != -1
    // (UPDATE/NEW) die trues in false umgetauscht.
    
    protected boolean Store( HttpServletRequest request , HttpServletResponse response , NodeList storeNode , String dataset , String method , int row , int maxRows , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        // build paramMap
        HashMap paramMap = new HashMap() ;
        Vector references = (Vector)uso.xmlDataAccess.getNodeListsByName( "reference" , storeNode ) ;
        if ( row != -1 ) {
            // UPDATE!!!
            try {
                String updateEntry = "UPDATE_" + Integer.toString( row ) ;
                if ( request.getParameter( updateEntry ) != null ) {
                    String updateFormFlag = (String)request.getParameter( updateEntry ) ;
                    if ( ! updateFormFlag.equals( "true" ) ) { return false ; }
                } else {
                    return false ;
                }
            } catch ( java.lang.NullPointerException ne ) {
                return false ;
            }
        } else if ( maxRows != -1 ) {
            // NEW!!!
            try {
                if ( request.getParameter( "NEW" ) != null ) {
                    String newFormFlag = (String)request.getParameter( "NEW" ) ;
                    if ( ! newFormFlag.equals( "true" ) ) { return false ; }
                } else {
                    return false ;
                }
            } catch ( java.lang.NullPointerException ne ) {
                return false ;
            }
        }
        for ( int i = 0 ; i < references.size() ; i++ ) {
            String referenceType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)references.get( i ) )).get( 0 ) ;
            if ( referenceType.equals( "session" ) ) {
                String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
                String ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)references.get( i ) )).get( 0 ) ;
                if ( ref.equals( "userID" ) ) {
                    uso.localRefs.put( id , uso.userID ) ;
                    paramMap.put( id , uso.userID ) ;
                } else if ( ref.equals( "userCenter" ) ) {
                    uso.localRefs.put( id , uso.userCenter ) ;
                    paramMap.put( id , uso.userCenter ) ;
                } else if ( ref.equals( "userLogin" ) ) {
                    uso.localRefs.put( id , uso.userLogin ) ;
                    paramMap.put( id , uso.userLogin ) ;
                } else if ( ref.equals( "centerRoles" ) ) {
                    String cR = "" ;
                    HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
                    Object [] centerRoles = ((Set)cRoles.keySet()).toArray() ;
                    for ( int ci = 0 ; ci < centerRoles.length ; ci++ ) {
                        if ( ci > 0 ) { cR += " , " ; }
                        cR += (String)centerRoles[ci] ;
                    }
                    uso.localRefs.put( id , cR ) ;
                    paramMap.put( id , cR ) ;
                } else {
                    uso.localRefs.put( id , uso.eidpWebAppCache.sessionData_get( ref ) ) ;
                    paramMap.put( id , (String)uso.eidpWebAppCache.sessionData_get( ref ) ) ;
                }
            } else if ( referenceType.equals( "form" ) ) {
                String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
                String ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)references.get( i ) )).get( 0 ) ;
                boolean encrypt = false ;
                try {
                    String encryptString = (String)((Vector)uso.xmlDataAccess.getElementsByName( "encrypt" , (NodeList)references.get( i ) )).get( 0 ) ;
                    if ( encryptString.equals( "true" ) ) {
                        encrypt = true ;
                    } else { encrypt = false ; }
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                    encrypt = false ;
                }
                if ( row != -1 ) {
                    ref = ref + "_" + row ;
                }
                String requestValue = (String)request.getParameter( ref ) ;
                if ( encrypt == true ) {
                    try {
                        requestValue = this.encryptString( requestValue ) ;
                    } catch ( java.lang.Exception lange ) {
                        throw new javax.servlet.ServletException( "Could not encrypt String in Module Store: " + lange ) ;
                    }
                }
                if( requestValue != null ){
                    String strDateFormat = (String)uso.eidpWebAppCache.sessionData_get( "DateFormat" );
                    if( !strDateFormat.equals( "" ) ){
                        if( strDateFormat.equals( "german" ) ){
                            if( this.isEUDate(requestValue) ){
                                requestValue = this.convertEUDateToISODate( requestValue ) ;
                            }
                        } else if (strDateFormat.equals("en_US")){
                            if(this.isUSDate(requestValue)){
                                requestValue = this.dateFormatter(requestValue, "MMM dd, yyyy", "yyyy-MM-dd");
                            }
                        } else if (strDateFormat.equals("en_GB")){
                            if(this.isUKDate(requestValue)){
                                requestValue = this.dateFormatter(requestValue, "dd.MM.yyyy", "yyyy-MM-dd");
                            }
                        }
                    }
                }
                uso.localRefs.put( id , requestValue ) ;
                paramMap.put( id ,  requestValue ) ;
            } else if ( referenceType.equals( "timestamp" ) ) {
                String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
                Date timestamp = new Date() ;
                String timeStampString = String.valueOf( timestamp.getTime() ) ;
                uso.localRefs.put( id , timeStampString ) ;
                paramMap.put( id , timeStampString ) ;
            } else if (referenceType.equals("sqltimestamp")) { // David
                String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
                Date timestamp = new Date() ;
                Timestamp timestampSql = new Timestamp(timestamp.getTime());
                String timeStampString = timestampSql.toString();
                uso.localRefs.put( id , timeStampString ) ;
                paramMap.put( id , timeStampString ) ;
            } else if ( referenceType.equals( "local" ) ) {
                String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
//                String ref =  (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
                String ref =  (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)references.get( i ) )).get( 0 ) ;
                uso.localRefs.put( id , uso.localRefs.get( ref ) ) ;
                paramMap.put( id , uso.localRefs.get( ref ) ) ;
            } else if ( referenceType.equals( "value" ) ) {
                String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
                String value = "" ;
                try {
                    value =  (String)((Vector)uso.xmlDataAccess.getElementsByName( "value" , (NodeList)references.get( i ) )).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                    value = "" ;
                }
                uso.localRefs.put( id , value ) ;
                paramMap.put( id , value ) ;
            } else if ( referenceType.equals( "sqltimestamp") ) {
                String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)references.get( i ) )).get( 0 ) ;
                Date timestamp = new Date() ;
                Timestamp tstamp = new Timestamp( timestamp.getTime() );
                String timeStampString = tstamp.toString();
                uso.localRefs.put( id , timeStampString ) ;
                paramMap.put( id , timeStampString ) ;
            }
        }
        // call DBMapper to store the data
        try {
            uso.dbMapper.DBAction( dataset , method , paramMap ) ;
            if ( uso.dbMapper.getException() != null ) {
                this.checkException( uso.dbMapper.getException() , storeNode , uso , request , response ) ;
                // resetException ist hier aus Lesbarkeitsgründen drin.
                // resetException wird auch in XMLModuleProcessing_Store aufgerufen, so dass
                // immer ein reset gemacht wird.
                uso.dbMapper.resetException() ;
                return true ;
            }
        } catch ( java.sql.SQLException se ) {
            throw new javax.servlet.ServletException( "XMLDispatcher throws SQLException in processWriteAction (DataSet: " + dataset + "; method: " + method + "; " + se ) ;
        } catch ( Exception se) {
            throw new javax.servlet.ServletException( "XMLDispatcher throws Exception in processWriteAction (DataSet: " + dataset + "; method: " + method + "; " + se ) ;
        }
        return false ;
    }
    
    private void checkException( Object e , NodeList storeNode ,  UserScopeObject uso ,  HttpServletRequest request , HttpServletResponse response ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        boolean exceptionHandled = false ;
        String eName = ( (Exception) e).getClass().getName() ;
        System.out.println( "MyException name: " + eName ) ;
        Vector eNodes = uso.xmlDataAccess.getNodeListsByName( "exception" , storeNode ) ;
        if ( eNodes.size() == 0 ) {
            throw new javax.servlet.ServletException( "Exception thrown during Store: " + (Exception)e ) ;
        } else {
            Iterator ei = eNodes.iterator() ;
            while ( ei.hasNext() ) {
                NodeList eNode = (NodeList)ei.next() ;
                // en = Exception name from xml-file (mandatory)
                // ec = exception code from xml-file (optional)
                // em = module to call from xml-file (mandatory)
                String en = (String)( (Vector)uso.xmlDataAccess.getElementsByName( "name" , eNode ) ).get( 0 ) ;
                String em = (String)( (Vector)uso.xmlDataAccess.getElementsByName( "call-module" , eNode ) ).get( 0 ) ;
                if ( en.equals( eName ) ) {
                    Integer exceptionCode = new Integer( 0 ) ;
                    try {
                        Class[] paramClasses = { } ;
                        java.lang.reflect.Method getCodeMethod = e.getClass().getMethod( "getErrorCode" , paramClasses  ) ;
                        Object[] paramObjects = { } ;
                        exceptionCode = (Integer)getCodeMethod.invoke( e , paramObjects ) ;
                    } catch ( java.lang.NoSuchMethodException ie ) {
                        System.out.println( ">>> NoSuchMethodException" ) ;
                        exceptionCode = new Integer( 0 ) ;
                    } catch ( java.lang.IllegalAccessException ie ) {
                        System.out.println( ">>> IllegalAccessException" ) ;
                        exceptionCode = new Integer( 0 ) ;
                    } catch ( java.lang.reflect.InvocationTargetException ie ) {
                        System.out.println( ">>> InvocationTargetException" ) ;
                        exceptionCode = new Integer( 0 ) ;
                    }
                    Vector ecV = (Vector)uso.xmlDataAccess.getElementsByName( "code" , eNode ) ;
                    if ( ecV.size() > 0 ) {
                        String ec =  (String) ecV.get( 0 ) ;
                        System.out.println( "MyException code: " + exceptionCode ) ;
                        int eC = Integer.parseInt( ec ) ;
                        int eCode = Integer.parseInt( exceptionCode.toString() ) ;
                        System.out.println( "Code from xml: " + eC + "; from Method: " + eCode ) ;
                        if ( eC == eCode ) {
                            exceptionHandled = true ;
                            String url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + em + ";show" ;
                            response.sendRedirect( url ) ;
                            break ;
                        }
                    } else {
                        exceptionHandled = true ;
                        String url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + em + ";show" ;
                        response.sendRedirect( url ) ;
                    }
                }
            }
            if ( exceptionHandled == false ) throw new javax.servlet.ServletException( "Exception thrown in Store: " + (Exception)e ) ;
        }
    }
    
    protected boolean processStoreActions( HttpServletRequest request , HttpServletResponse response , NodeList storeNode , String storeType , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        boolean storeException = false ;
        boolean noStore = false ;
        Vector noStoreVector = (Vector)uso.xmlDataAccess.getElementsByName( "no-store" , storeNode ) ;
        if ( noStoreVector.size() > 0 ) {
            if ( ((String)noStoreVector.get( 0 )).equals( "true" ) ) {
                noStore = true ;
            }
        }
        if ( noStore == false ) {
            String dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , storeNode )).get( 0 ) ;
            String method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , storeNode )).get( 0 ) ;
            // CALL Storage Procedures:
            if ( storeType.equals( "list" ) || storeType.equals( "matrix-list" ) ) {
                int maxRows ;
                try {
                    maxRows = Integer.parseInt( (String)request.getParameter( "MAXROWS" ) ) + 1 ;
                } catch ( java.lang.NumberFormatException nfe ) {
                    maxRows = 0 ;
                }
                // 1. set NEW:
                storeException = this.Store( request , response , storeNode , dataset , method , -1 , maxRows , uso ) ;
                // 2. set UPDATE:
                for ( int row = 0 ; row < maxRows ; row++ ) {
                    storeException = this.Store( request , response , storeNode , dataset , method , row , maxRows , uso ) ;
                }
            } else {
                storeException = this.Store( request , response , storeNode , dataset , method , -1 , -1 , uso ) ;
            }
        }
        // Session settings:
        if ( storeException == false ) {
            Vector setVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "set" , storeNode ) ;
            boolean sessionSet = false ;
            for ( int i = 0 ; i < setVector.size() ; i++ ) {
                String setType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref-type" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String value = new String() ;
                if ( refType.equals( "resultSet" ) ) {
                    HashMap row = uso.dbMapper.getRow( 0 ) ;
                    value = (String)row.get( refValue ) ;
                } else if ( refType.equals( "local" ) ) {
                    value = (String)uso.localRefs.get( refValue ) ;
                } else if ( refType.equals( "session" ) ) {
                    if ( refValue.equals( "userID" ) ) {
                        value =  uso.userID ;
                    } else if ( refValue.equals( "userCenter" ) ) {
                        value =  uso.userCenter ;
                    } else if ( refValue.equals( "userLogin" ) ) {
                        value = uso.userLogin ;
                    } else {
                        value = (String)uso.eidpWebAppCache.sessionData_get( refValue ) ;
                    }
                } else if ( refType.equals( "form" ) ) {
                    value = (String)request.getParameter( refValue ) ;
                }
                if ( setType.equals( "session" ) ) {
                    if ( refID.equals( "userID" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userID" , value ) ;
                    } else if ( refID.equals( "userCenter" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userCenter" , value ) ;
                    } else if ( refID.equals( "userLogin" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userLogin" , value ) ;
                    } else {
                        sessionSet = true ;
                        uso.eidpWebAppCache.sessionData_set( refID , value ) ;
                    }
                } else if ( setType.equals( "local" ) ) {
                    uso.localRefs.put( refID , value ) ;
                } else if ( setType.equals( "form" ) ) {
                    sessionSet = true ;
                    uso.eidpWebAppCache.sessionData_set( refID , value ) ;
                }
            }
            NodeList secondaryKey = null ;
            boolean updateSecondaryKeyList = true ;
            try {
                secondaryKey = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "secondary-ref-list" , storeNode )).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aioobe ) {
                updateSecondaryKeyList = false ;
            }
            if ( updateSecondaryKeyList == true ) {
                String secRefDataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref-list,dataset" , storeNode ) ).get( 0 ) ;
                String secRefMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref-list,method" , storeNode ) ).get( 0 ) ;
                String secRefSessionListName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref-list,session-list-name" , storeNode ) ).get( 0 ) ;
                HashMap secParamMap = new HashMap() ;
                secParamMap = uso.sharedMethods.getParams( secondaryKey , uso ) ;
                uso.dbMapper.DBAction( secRefDataset , secRefMethod , secParamMap ) ;
                Vector sessionListEntry = new Vector() ;
                String sessionRefEntry = "" ;
                if ( uso.dbMapper.size() > 0 ) {
                    sessionListEntry = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;
                }
                uso.eidpWebAppCache.sessionData_remove( secRefSessionListName ) ;
                uso.eidpWebAppCache.sessionData_set( secRefSessionListName , sessionListEntry ) ;
            }
        }
        return storeException ;
    }
    
// ===================
// xmlModule:
// ===================
    
    protected void xmlModuleProcessing( HttpServletRequest request , HttpServletResponse response , NodeList topLevelControllerNode , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException , org.xml.sax.SAXException , java.io.IOException {
        Vector xmlModuleVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "module" , topLevelControllerNode ) ;
        for ( int i = 0 ; i < xmlModuleVector.size() ; i++ ) {
            String xmlModuleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)xmlModuleVector.get( i ) )).get( 0 ) ;
            if ( xmlModuleName.equals( (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ) ) {
                if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "show" ) ) {
                    this.xmlModuleProcessing_Show( request , response , (NodeList)xmlModuleVector.get( i ) , uso ) ;
                    break ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "store" ) ) {
                    this.xmlModuleProcessing_Store( request , response , (NodeList)xmlModuleVector.get( i ) , uso ) ;
                    break ;
                }
            }
        }
    }
    
    protected void xmlModuleProcessing_Show( HttpServletRequest request , HttpServletResponse response , NodeList xmlModuleNode , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException , org.xml.sax.SAXException , java.io.IOException {
        // Clear this.selectDBMap
        uso.selectDBMap.clear() ;
        // 0. check permissions
        Vector rolePermissions = new Vector() ;
        rolePermissions = uso.xmlDataAccess.getElementsByName( "role-name" , xmlModuleNode ) ;
        this.checkPermissions( rolePermissions , uso ) ;
        // get Views Vector:
        Vector viewVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "view" , xmlModuleNode ) ;
        // get label of the module
        String moduleLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label" , xmlModuleNode ) ).get( 0 ) ;
        // ======> initHTML:
        PrintWriter printWriter = uso.sharedMethods.initHTML( request , response ) ;
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\">" ) ;
        printWriter.println( "  <!--");
        // 1. read filters and create filters as JavaScript (check if they are needed - only create filters needed!!! )
        // get used filternames from controller.xml
        Vector filterNames = uso.xmlDataAccess.getElementsByName( "view,field,filter" , xmlModuleNode ) ;
        this.createFilterFunctions( filterNames , printWriter , uso ) ;
        // 2. create Submit function (JavaScript)
        this.createViewActiveCheck( printWriter ) ;
        this.createClearCheck( printWriter ) ;
        this.createSubmitCheck( xmlModuleNode , printWriter , uso ) ;        
        this.createToolTipWindowOpener( printWriter ) ;
        // for Calculation // Stephan
//        this.createRadioButtonCalcFunction( printWriter ) ;
        // RichTextFunctions // Stephan
//        this.createRichTextAreaFunctions( printWriter ) ;
        
        // WarnArztFunctions // Stephan
        String strHelpAni = (String)uso.eidpWebAppCache.sessionData_get( "AnimatedHelp" ) ;
        if(strHelpAni != null && strHelpAni.toLowerCase().equals("yes")){
            this.createWarnArztFunctions(printWriter);
        }
        
        // for TextAreaCheck // Stephan
        this.createTextAreaMaxInputFilter( printWriter ) ;
        this.createNewXWindowOpener( printWriter ) ;

        // for Autologout // Stephan
        this.createAutoLogout( printWriter , uso ) ;
 
        // 3. read fields to create input-area
        printWriter.println( "  --> " ) ;
        printWriter.println( "  </script>");
        // to import JavaScript-ScriptFiles // Stephan
        this.createJavaScriptImport( printWriter , xmlModuleNode , uso ) ;
//        printWriter.println( "</head><body  onload=\"LoadRichTextFields()\">" ) ;
        printWriter.println( "</head><body>" ) ;
        // WarnArztHTML // Stephan
        if(strHelpAni != null && strHelpAni.toLowerCase().equals("yes")){
            this.createWarnArztHTML(printWriter);
        }
        printWriter.println( "<table border=\"0\" width=\"100%\"><tr><td align=\"left\" valign=\"top\" class=\"white\" width=\"*\"> " ) ;
        printWriter.println( " <nobr> " ) ;
        //<editor-fold defaultstate="collapsed" desc="onerror replacement">    
        String applicationLogo = new String();
        if (!uso.xmlApplication.getElementsByName("application-data,app-logo").isEmpty()) {
            applicationLogo = (String)((Vector)uso.xmlApplication.getElementsByName( "application-data,app-logo" )).get( 0 ) ;
        } else {
            applicationLogo = "eidp_logo.jpg";
        }
        //</editor-fold>
        printWriter.println( "<img src=\"/EIDPWebApp/images/" + applicationLogo + "\" border=\"0\" width=\"40\" height=\"20\" alt=\"app_logo\"><font size=\"+1\">&nbsp;&nbsp;<b>" + moduleLabel + "</b></font>" ) ;
        // printWriter.println( "</td> " ) ;
        // Secondary key if accessible:
        boolean validSecondaryKey = true ;
        boolean showSecondaryKey = true ;
        String keyListName = "" ;
        String listValue = "" ;
        Vector listShowVector = new Vector() ;
        String listShow = "" ;
        String keyValue = "" ;
        boolean keyValueShowCheck = false ;
        String keyValueShow = "" ;
        Vector keyList = new Vector() ;
        NodeList secondaryKey = null ;
        try {
            secondaryKey = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "secondary-key" , xmlModuleNode )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiob ) {
            validSecondaryKey = false ;
        }
        try {
            keyListName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "key-list" , secondaryKey )).get( 0 ) ;
            listValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-value" , secondaryKey )).get( 0 ) ;
            listShowVector = (Vector)uso.xmlDataAccess.getElementsByName( "list-show" , secondaryKey ) ;
            listShow = (String)listShowVector.get( 0 ) ;
            keyValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "key-value" , secondaryKey )).get( 0 ) ;
            keyList = (Vector)uso.eidpWebAppCache.sessionData_get( keyListName ) ;
            try {
                keyValueShow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "key-value-show" , secondaryKey )).get( 0 ) ;
                keyValueShowCheck = true ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                keyValueShowCheck = false ;
            }
        } catch ( java.lang.NullPointerException ne ) {
            showSecondaryKey = false ;
        }
        if ( showSecondaryKey == true ) {
            String groupID =  (String)uso.eidpWebAppCache.sessionData_get( "groupID" );
            if(groupID != null && !groupID.equals("")){
                groupID = ";" + groupID;
            }else{
                groupID = "";
            }
            printWriter.println( "<td align=\"left\" valign=\"top\" class=\"white\"> " ) ;
            printWriter.println( "<form name=\"changeSecondaryKey\" method=\"POST\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller\"> " ) ;
            printWriter.println( "<input type=\"hidden\" name=\"module\" value=\"" + uso.eidpWebAppCache.sessionData_get( "module" )  + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) + groupID + ";show\"> " ) ;
            printWriter.println( "<i>" + uso.eidpWebAppCache.sessionData_get( "SecondaryKeyLabel" ) + ":&nbsp;</i><select name=\"SecondaryKey\" onChange=\"javascript:document.changeSecondaryKey.submit();\" > " ) ;
            if ( validSecondaryKey == true ) {
                for ( int li = 0 ; li < keyList.size() ; li++ ) {
                    String checkedFlag = "" ;
                    if ( ((String)((HashMap)keyList.get( li )).get( listValue )).equals( (String)uso.eidpWebAppCache.sessionData_get( keyValue ) ) ) {
                        if ( keyValueShowCheck == true ) {
                            uso.eidpWebAppCache.sessionData_remove( keyValueShow ) ;
                            uso.eidpWebAppCache.sessionData_set( keyValueShow ,  ((HashMap)keyList.get( li )).get( listShow ) ) ;
                        }
                        checkedFlag = "selected" ;
                    }
                    String combinedShow = "" ;
                    Iterator lsi = listShowVector.iterator() ;
                    while ( lsi.hasNext() ) {
                        String strNextListEntry = (String)((HashMap)keyList.get( li )).get( (String)lsi.next() ) ;
                        String strDateFormat = (String)uso.eidpWebAppCache.sessionData_get( "DateFormat" );
                        if( !strDateFormat.equals( "" ) ){
                            if( strDateFormat.equals( "german" ) ){
                                if( this.isISODate( strNextListEntry ) ){
                                    strNextListEntry = convertISODateToEUDate(strNextListEntry);
                                }
                            } else if (strDateFormat.equals("en_US")){
                                if(this.isISODate(strNextListEntry)){
                                    strNextListEntry = this.dateFormatter(strNextListEntry, "yyyy-MM-dd", "MMM dd, yyyy");
                                }
                            } else if (strDateFormat.equals("en_GB")){
                                if(this.isISODate(strNextListEntry)){
                                    strNextListEntry = this.dateFormatter(strNextListEntry, "yyyy-MM-dd", "dd.MM.yyyy");
                                }
                            }
                        }
                        combinedShow += strNextListEntry + " " ;
                    }
                    printWriter.println( "<option value=\"" + (String)((HashMap)keyList.get( li )).get( listValue ) + "\" " + checkedFlag + ">" + combinedShow + "</option>" ) ;
                }
            }
            printWriter.print( "</select> " ) ;
            printWriter.print( "<a href=\"javascript:CreateNewX( 'changeSecondaryKey' , 'SecondaryKey' , 'XMLDispatcher;NewSecondaryKey' );\"><img src=\"/EIDPWebApp/images/new.jpg\" border=\"0\" title=\"Enter new Secondary Key.\" alt=\"seckey_button\"></a>" ) ;
            printWriter.println( " </form></td> " ) ;
            printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
            printWriter.println( "  function changeSecondaryKey_SecondaryKey_setOption( fieldValue , fieldHiddenValue ) { " ) ;
            printWriter.println( "      NewOption = new Option( fieldValue , fieldHiddenValue , false , true ) ; " ) ;
            printWriter.println( "      document.changeSecondaryKey.SecondaryKey.options[document.changeSecondaryKey.SecondaryKey.length] = NewOption ; " ) ;
            // printWriter.println( "      parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + uso.eidpWebAppCache.sessionData_get( "module" ) + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) + ";show'; " ) ;
            printWriter.println("       document.changeSecondaryKey.submit();");
            printWriter.println( "  } " ) ;
            printWriter.println( " </script> " ) ;
        }
        
        // David: request scope sec-key
        Vector reqSecKey = uso.xmlDataAccess.getNodeListsByName("request-sec-key" , xmlModuleNode);
        if (!reqSecKey.isEmpty()) {
            Vector reqSecPreload = uso.xmlDataAccess.getNodeListsByName("preload" , (NodeList) reqSecKey.get(0));
            Vector formName = uso.xmlDataAccess.getElementsByName("form-name", (NodeList) reqSecKey.get(0));
            Vector keyReqName = uso.xmlDataAccess.getElementsByName("key-name", (NodeList) reqSecKey.get(0));
            Vector keyReqLabel = uso.xmlDataAccess.getElementsByName("key-label", (NodeList) reqSecKey.get(0));
            Vector keyRefName = uso.xmlDataAccess.getElementsByName("key-ref-name", (NodeList) reqSecKey.get(0));
            if (!reqSecPreload.isEmpty() && !formName.isEmpty() && !keyReqName.isEmpty() && !keyRefName.isEmpty() && !keyReqLabel.isEmpty()) {
                uso.sharedMethods.PreLoad((NodeList) reqSecPreload.get(0), (String) formName.get(0), uso, request);
                String keyReqValue = request.getParameter((String) keyReqName.get(0));
                if (null == keyReqValue) keyReqValue = "";
                
                String groupID =  (String)uso.eidpWebAppCache.sessionData_get( "groupID" );
                if(groupID != null && !groupID.equals("")){
                    groupID = ";" + groupID;
                }else{
                    groupID = "";
                }

                printWriter.println("<td align=\"left\" valign=\"top\" class=\"white\">");
                printWriter.println("<form name=\"" + (String) formName.get(0) + "\" method=\"POST\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller\">");
                printWriter.println("<input type=\"hidden\" name=\"module\" value=\"" + uso.eidpWebAppCache.sessionData_get( "module" )  + ";" + uso.eidpWebAppCache.sessionData_get( "xmlFile" ) + ";" + uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) + groupID + ";show\"> " ) ;
                printWriter.println("<i>" + (String) keyReqLabel.get(0) + ":&nbsp;</i>");
                printWriter.println("<select name=\"" + (String) keyReqName.get(0) + "\" onChange=\"javascript:document." + (String) formName.get(0) + ".submit();\">");
                Iterator rsk = ((Vector) uso.preLoadData.get((String) formName.get(0))).iterator();
                while (rsk.hasNext()) {
                    String selected = "";
                    String value = (String)((HashMap) rsk.next()).get((String) keyRefName.get(0));
                    if (keyReqValue.equals("")) keyReqValue = value;
                    if (keyReqValue.equals(value)) {
                        uso.localRefs.put((String) keyReqName.get(0), value);
                        selected = " selected";
                    }
                    printWriter.println( "<option value=\"" + value + "\"" + selected + ">" + value + "</option>" );
                }
                printWriter.println("</select>");
                printWriter.println("</form></td>");
            }
        }
                    
        printWriter.println( " </tr></table> " ) ;
        printWriter.println( "<hr> " ) ;
        // ===> Generate Views:
        for ( int vi = 0 ; vi < viewVector.size() ; vi++ ) {
            Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "view-block-role" , (NodeList)viewVector.get( vi )) ;
            boolean blockField = false ;
            for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                    blockField = true ;
                }
            }
            if ( blockField == false ) {
                if ( vi > 0 ) {
                    printWriter.println( " <hr> " ) ;
                }
                boolean multiPreload = false;
                
                // get form-name
                String formName = "";
                
                // MultiPreloads STEPHANs
                Vector preloadNode = uso.xmlDataAccess.getNodeListsByName( "preload" , (NodeList)viewVector.get( vi ) ) ;
                if(preloadNode.size()>1){
                    multiPreload = true;
                    for ( int pi = 0 ; pi < preloadNode.size() ; pi++ ) {
                        String preloadName = "" ;
                        try {
                            preloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)preloadNode.get( pi ) ) ).get( 0 ) ;
                        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                            throw new javax.servlet.ServletException( "No preload name given in xmlModuleProcessing_Show: " + aiobe ) ;
                        }
                        try {
                            uso.sharedMethods.PreLoad( (NodeList)preloadNode.get( pi ) , preloadName , uso , request ) ;
                        } catch ( java.sql.SQLException sqle ) {
                            throw new javax.servlet.ServletException( "xmlModuleProcessing_Show/Preload throws SQLExeption (E200020): " + sqle ) ;
                        } catch ( java.io.IOException ioe ) {
                            throw new javax.servlet.ServletException( "xmlModuleProcessing_Show/Preload throws IOException (E200021): " + ioe ) ;
                        }
                        ((org.w3c.dom.Element)((NodeList)viewVector.get( vi ))).removeChild( (org.w3c.dom.Node)preloadNode.get( pi ) ) ;
                    }
                } else {
                    // Pre-Load DATA:
                    NodeList preLoad = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "preload" , (NodeList)viewVector.get( vi ) )).get( 0 ) ;
                    // get form-name
                    formName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)viewVector.get( vi ) ) ).get( 0 ) ;// get type of module
                    uso.sharedMethods.PreLoad( preLoad , formName , uso , request ) ;
                }
                
                // get type of module
                if ( viewVector.size() > 1 ) {
                    String viewLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "view-label" , (NodeList)viewVector.get( vi ) ) ).get( 0 ) ;
                    printWriter.println( "<b><a name=\"" + formName + "\">" + viewLabel + "</a></b><br>" ) ;
                }
                String viewType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)viewVector.get( vi ) ) ).get( 0 ) ;
                
                if ( viewType.equals( "matrix" ) ) {
                    // createUserMessage because of use of alternate-query // Stephan
                    this.processCreateUserMessageForAlternateQuery(printWriter,uso,formName);
                    this.createMatrix( request , response , printWriter , (NodeList)viewVector.get( vi ) , uso ) ;
                } else if ( viewType.equals( "matrix-list" ) ) {
                    this.createMatrixList( request , response , printWriter , (NodeList)viewVector.get( vi ) , uso ) ;
                } else if ( viewType.equals( "list" ) ) {
                    this.createList( request , response , printWriter , (NodeList)viewVector.get( vi ) , uso ) ;
                } else if ( viewType.equals( "text" ) ) {
                    this.createText( request , response , printWriter , (NodeList)viewVector.get( vi ) , uso ) ;
                } else if ( viewType.equals( "addin" ) ) {
                    this.loadAddIn( request , response , printWriter , (NodeList)viewVector.get( vi ) , uso ) ;
                }
            }
        }
        uso.sharedMethods.closeHTML( printWriter , uso ) ;
    }
    
    protected void xmlModuleProcessing_Store( HttpServletRequest request , HttpServletResponse response , NodeList xmlModuleNode , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException , java.io.IOException {
        // 0. check permissions
        boolean storeException = false ;
        try {
            // Hier muss die Core-Exception auch zurückgesetzt werden,
            // da ansonsten eine Exception aus dem normalen Read-Out (show)
            // noch vorhanden sein kann.
            uso.dbMapper.resetException() ;
        } catch (Exception ex) {
            throw new javax.servlet.ServletException( "XMLDispatcher throws Exception in xmlModuleProcessing_Store " + ex ) ;
        }
        Vector rolePermissions = new Vector() ;
        rolePermissions = uso.xmlDataAccess.getElementsByName( "role-name" , xmlModuleNode ) ;
        this.checkPermissions( rolePermissions , uso ) ;
        // get the approriate view and storeVector
        Vector viewNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "view" , xmlModuleNode ) ;
        String requestedFormName = (String)request.getParameter( "formName" ) ;
        Vector storeNodes = new Vector() ;
        String storeType = "" ;
        String finalModule = "" ;
        String viewNode = "" ;
        for ( int vi = 0 ; vi < viewNodes.size() ; vi++ ) {
            String viFormName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)viewNodes.get( vi ) ) ).get( 0 ) ;
            if ( requestedFormName.equals( viFormName ) ) {
                storeType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)viewNodes.get( vi ) ) ).get( 0 ) ;
                storeNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "store" , (NodeList)viewNodes.get( vi ) ) ;
                finalModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "final" , (NodeList)viewNodes.get( vi ) ) ).get( 0 ) ;
                viewNode = viFormName ;
                break;
            }
        }
        for ( int i = 0 ; i < storeNodes.size() ; i++ ) {
            storeException = this.processStoreActions( request , response , (NodeList)storeNodes.get( i ) , storeType , uso ) ;
            // Wenn eine Exception waehrend des Speicherns aufgetreten ist,
            // macht es keinen Sinn die folgenden store-nodes (falls vorhanden) abzuarbeiten
            // oder doch?! STEPHAN
            if(storeException){
                break;
            }
        }
        try {
            // wenn hier nicht resetException gemacht wird, bleibt diese stehen
            // aus Lesbarkeitsgründen ist diese resetException auch in Store drin.
            uso.dbMapper.resetException() ;
        } catch (Exception ex) {
            throw new javax.servlet.ServletException( "XMLDispatcher throws SQLException in xmlModuleProcessing_Store: " + ex ) ;
        }
        // last but not lease: dispatch to new module
        if ( storeException == false ) {
            String url = "";
            if(finalModule.substring( finalModule.lastIndexOf(';') + 1, finalModule.length() ).equals("NEW") ){
                String strFinalURL = finalModule.substring( 0 , finalModule.lastIndexOf(';') );
                url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;Find;store&selectValue=" + uso.eidpWebAppCache.sessionData_get("PublicPatientID") + "&finalFinalModule=" + strFinalURL;
            }else{
                url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + finalModule + ";show#" + viewNode ;
            }
            response.sendRedirect( url ) ;
        }
    }
    
    private String encryptString( String encryptString ) throws java.lang.Exception {
        encryptString = new String( encrypt( encryptString ) ) ;
        encryptString = javax.xml.bind.DatatypeConverter.printBase64Binary(encryptString.getBytes());
        return encryptString ;
    }
    
    private static byte[] encrypt(String inputString) throws Exception {
        java.security.MessageDigest md =null;
        md = java.security.MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(inputString.getBytes( "ISO-8859-1" ));
        return md.digest();
    }
    
    // Helper-Function : Stephan
    private String replaceAll( String s, String search, String replace ) {
        StringBuilder s2 = new StringBuilder();
        int i = 0, j = 0;
        int len = search.length();
        
        while ( j > -1 ) {
            j = s.indexOf( search, i );
            
            if ( j > -1 ) {
                s2.append( s.substring(i,j) );
                s2.append( replace );
                i = j + len;
            }
        }
        s2.append( s.substring(i, s.length()) );
        
        return s2.toString();
    }
    
    // Helper-Function : Stephan
    protected boolean isISODate( String strValue ){
        if( strValue.trim().length() == 10 ){
            Pattern p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
            Matcher m = p.matcher( strValue.trim() );
            return m.find();
        }else{
            return false;
        }
    }
    
    // Helper-Function : Stephan
    protected boolean isEUDate( String strValue ){
        if( strValue.trim().length() == 10 ){
            Pattern p = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");
            Matcher m = p.matcher( strValue.trim() );
            return m.find();
        }else{
            return false;
        }
    }
    
    // Helper-Function : David
    protected boolean isUSDate(String strValue){
        if(strValue.trim().length() == 12){
            Pattern p = Pattern.compile("(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (\\d{2}), (19|20)[0-9]{2}");
            Matcher m = p.matcher(strValue.trim());
            return m.find();
        } else {
            return false;
        }
    }
    
    protected boolean isUKDate(String strValue){
        if(strValue.trim().length() == 12){
            Pattern p = Pattern.compile("(\\d(2))\\.(\\d{2})\\.(19|20)[0-9]{2}");
            Matcher m = p.matcher(strValue.trim());
            return m.find();
        } else {
            return false;
        }
    }
    
    // Helper-Function : David
    protected String dateFormatter(String strValue, String inpattern, String outpattern){
        SimpleDateFormat informatter = new SimpleDateFormat(inpattern);
        Date date = null;
        try {
        date = informatter.parse(strValue);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        SimpleDateFormat outformatter = new SimpleDateFormat(outpattern);
        String output = outformatter.format(date);
        return output;
    }
    
    // Helper-Function : Stephan
    protected String convertISODateToEUDate( String strValue ){
        StringTokenizer strto = new StringTokenizer(strValue,"-");
        String year = strto.nextToken();
        String month = strto.nextToken();
        String day = strto.nextToken();
        return day + "." + month + "." + year;
    }
    
    // Helper-Function : Stephan
    protected String convertEUDateToISODate( String strValue ){
        StringTokenizer strto = new StringTokenizer(strValue,".");
        String day = strto.nextToken();
        String month = strto.nextToken();
        String year = strto.nextToken();
        return year + "-" + month + "-" + day;
    }
    
}

