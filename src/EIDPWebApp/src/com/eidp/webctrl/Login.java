/*
 * login.java
 *
 * Created on July 18, 2003, 10:52 AM
 */

package com.eidp.webctrl;

import com.eidp.webctrl.WebAppCache.EIDPWebAppCacheRemote ;
import javax.ejb.Handle ;

import com.eidp.xml.XMLDataAccess;
import com.eidp.logger.Logger;

import org.xml.sax.SAXException ;

import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;

import java.util.Vector ;
import java.util.HashMap ;

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


public class Login extends HttpServlet {
    
    private String applicationContext ;
    private String xmlfile ;
    private XMLDataAccess xmlDataAccess ;
    
    private String applicationName ;
    private String applicationLogo ;
    private String applicationInfo = "";
    private Vector sponsorName = new Vector() ;
    private Vector sponsorLogo = new Vector() ;
    private Vector sponsorLogoHeight = new Vector() ;
    private HashMap metaInformation = new HashMap() ;
    private int sponsorsRowNumber = 5 ;
    private String loginErrMessage = "";
    private String loginDisableMessage = "";
    private String loginAutoLogoutMessage = "";
    
    
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws javax.servlet.ServletException {
        super.init(config);
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, java.io.IOException {
        if ( request.getParameter( "applicationContext" ) != null ) {
            this.applicationContext = (String)request.getParameter( "applicationContext" ) ;
        } else {
            HttpSession session = request.getSession() ;
            EIDPWebAppCacheRemote eidpWebAppCache = (EIDPWebAppCacheRemote)((Handle)session.getAttribute( "eidpWebAppCacheHandle" )).getEJBObject() ;
            this.applicationContext = (String)eidpWebAppCache.sessionData_get( "applicationContext" ) ;
        }
        try {
            this.xmlfile = "/com/eidp/" + this.applicationContext + "/resources/webctrl/login.xml" ;
            this.xmlDataAccess = new XMLDataAccess( this.xmlfile ) ;
            // Meta Information
            this.metaInformation.put( "project" , (String)((Vector)this.xmlDataAccess.getElementsByName( "meta-information,project" )).get( 0 ) ) ;
            this.metaInformation.put( "system-name" , (String)((Vector)this.xmlDataAccess.getElementsByName( "meta-information,system-name" )).get( 0 ) ) ;
            this.metaInformation.put( "institution" , (String)((Vector)this.xmlDataAccess.getElementsByName( "meta-information,institution" )).get( 0 ) ) ;
            this.metaInformation.put( "department" , (String)((Vector)this.xmlDataAccess.getElementsByName( "meta-information,department" )).get( 0 ) ) ;
            this.metaInformation.put( "contact-mail" , (String)((Vector)this.xmlDataAccess.getElementsByName( "meta-information,contact-mail" )).get( 0 ) ) ;
            this.metaInformation.put( "www" , "" ) ;
            try {
                this.metaInformation.put( "www" , (String)((Vector)this.xmlDataAccess.getElementsByName( "meta-information,www" )).get( 0 ) ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                this.metaInformation.put( "www" , "" ) ;
            }

            if ( request.getParameter( "hasLoginError" ) != null && request.getParameter( "hasLoginError" ).equals("true") ) {
                try {
                    this.loginErrMessage = (String)((Vector)this.xmlDataAccess.getElementsByName( "login-configuration,login-err-message" )).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) { }
            } else {
                this.loginErrMessage = "";
            }
            if ( request.getParameter( "tooManyTrysMessage" ) != null && request.getParameter( "tooManyTrysMessage" ).equals("true") ) {
                try {
                    this.loginDisableMessage = (String)((Vector)this.xmlDataAccess.getElementsByName( "login-configuration,login-disable-message" )).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) { }
            } else {
                this.loginDisableMessage = "";
            }
            if ( request.getParameter( "autologout" ) != null && request.getParameter( "autologout" ).equals("true") ) {
                try {
                    this.loginAutoLogoutMessage = (String)((Vector)this.xmlDataAccess.getElementsByName( "login-configuration,auto-logout-message" )).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) { }
            } else {
                this.loginAutoLogoutMessage = "";
            }
            if (!this.loginDisableMessage.equals("")){
                this.loginErrMessage = this.loginDisableMessage;
            } else if (!this.loginAutoLogoutMessage.equals("")){
                this.loginErrMessage = this.loginAutoLogoutMessage;
            }

            this.metaInformation.put( "copyright" , (String)((Vector)this.xmlDataAccess.getElementsByName( "meta-information,copyright" )).get( 0 ) ) ;
            this.applicationName = (String)((Vector)this.xmlDataAccess.getElementsByName( "login-configuration,name" )).get( 0 ) ;            
            if (!this.xmlDataAccess.getElementsByName("login-configuration,app-logo").isEmpty()) {
                this.applicationLogo = (String)((Vector)this.xmlDataAccess.getElementsByName( "login-configuration,app-logo" )).get( 0 ) ;
            } else {
                this.applicationLogo = "eidp_logo.jpg";
            }
            this.sponsorName = (Vector)this.xmlDataAccess.getElementsByName( "login-configuration,sponsor,name" ) ;
            this.sponsorLogo = (Vector)this.xmlDataAccess.getElementsByName( "login-configuration,sponsor,logo" ) ;
            this.sponsorLogoHeight = (Vector)this.xmlDataAccess.getElementsByName( "login-configuration,sponsor,height" ) ;
            Vector srn = new Vector() ;
            srn = (Vector)this.xmlDataAccess.getElementsByName( "login-configuration,sponsor-row-number" ) ;
            if ( srn.size() > 0 ) {
                this.sponsorsRowNumber = Integer.parseInt( (String)srn.get( 0 ) ) ;
            }
        } catch ( org.xml.sax.SAXException se ) {
            throw new javax.servlet.ServletException( "SAXException thrown by org.eidp.webctrl.Login: " + se ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException pce ) {
            throw new javax.servlet.ServletException( "ParserConfigurationException thrown by org.eidp.webctrl.Login: " + pce ) ;
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "IOException thrown by org.eidp.webctrl.Login: " + ioe ) ;
        }
        this.generateLoginView( request , response ) ;
    }
    
    protected void generateLoginView( HttpServletRequest request , HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<html>");       
        out.println("<head>");
        out.println("<script type=\"text/javascript\">" ) ;
        out.println("<!--");
        out.println( "if (top != self)" ) ;
        out.println( "  top.location = self.location;" ) ;
        String appURL = request.getRequestURL().substring(0,request.getRequestURL().lastIndexOf(".")) +
                ".Controller?applicationContext=" + ((String)this.metaInformation.get("project")).toLowerCase();
        out.println( "function relogin(){ " ) ;
        out.println( "  self.location='" + appURL + "';" ) ;
        out.println( "}" ) ;
        out.println( "function getCookie(name){ " ) ;
        out.println( "  var pos;" ) ;
        out.println( "  var token = name + \"=\";" ) ;
        out.println( "  var tnlen = token.length;" ) ;
        out.println( "  var cklen = document.cookie.length;" ) ;
        out.println( "  var i = 0;" ) ;
        out.println( "  var j;" ) ;
        out.println( "  while (i < cklen) {" ) ;
        out.println( "    j = i + tnlen;" ) ;
        out.println( "    if (document.cookie.substring(i, j) == token){" ) ;
        out.println( "        pos = document.cookie.indexOf (\";\", j);" ) ;
        out.println( "        if (pos == -1)" ) ;
        out.println( "            pos = document.cookie.length;" ) ;
        out.println( "        return unescape(document.cookie.substring(j, pos));" ) ;
        out.println( "    }" ) ;
        out.println( "    i = document.cookie.indexOf(\" \", i) + 1;" ) ;
        out.println( "    if (i == 0) break;" ) ;
        out.println( "  }" ) ;
        out.println( "  return null;" ) ;
        out.println( "}" ) ;
        out.println( "function setCookie(name, value){ " ) ;
        out.println( "    document.cookie = name + \"=\" + escape(value);" ) ;
        out.println( "}" ) ;
        out.println( "function deleteCookie(name){ " ) ;
        out.println( "  var exp = new Date();" ) ;
        out.println( "  exp.setTime (exp.getTime() - 1);" ) ;
        out.println( "  var cval = getCookie (name);" ) ;
        out.println( "  document.cookie = name + \"=\" + cval + \"; expires=\" + exp.toGMTString();" ) ;
        out.println( "}" ) ;
        out.println( "var cookieName=\"COOKCOCKCOOKIETEST\";" ) ;
        out.println( "var cookieValue=\"TEST_COOKIE\";" ) ;
        out.println( "setCookie(cookieName,cookieValue,1);" ) ;
        out.println( "var cookieReturnValue=getCookie(cookieName);" ) ;
        out.println( "    var alertMess=\"\";" ) ;
        out.println( "    if (cookieReturnValue == null) {" ) ;
        out.println( "      alertMess=\"<br>Your browser does not support Cookies!<br>This application needs Cookies to work!<br>Please check your Browser-Settings<br>or contact your support!<br><br>\";" ) ;
        out.println( "    }" ) ;
        out.println( "deleteCookie(cookieName);" ) ;
        out.println("// -->");
        out.println( "</script>" ) ;
        
        out.println("<title>" + this.applicationName + " Login </title>");
        
        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >");
        out.println( "<meta name=\"robots\" content=\"index,nofollow\">" ) ;
        out.println( "<meta name=\"keywords\" content=\" " + this.metaInformation.get( "project" ) + " \"> " ) ;
        out.println( "<meta name=\"description\" content=\" " + this.metaInformation.get( "project" ) + " \"> " ) ;
        out.println( "<meta name=\"author\" content=\" " + this.metaInformation.get( "project" ) + " \"> " ) ;
        out.println( "<meta name=\"date\" content=\"2004-01-01\"> " ) ;
        
        out.println( "<meta name=\"DC.Title\" content=\" " + this.metaInformation.get( "project" ) + " \"> " ) ;
        out.println( "<meta name=\"DC.Creator\" content=\" " + this.metaInformation.get( "project" ) + " \"> " ) ;
        out.println( "<meta name=\"DC.Subject\" content=\" " + this.metaInformation.get( "project" ) + " \"> " ) ;
        out.println( "<meta name=\"DC.Description\" content=\" " + this.metaInformation.get( "project" ) + " \"> " ) ;
        out.println( "<meta name=\"DC.Publisher\" content=\" " + this.metaInformation.get( "institution" ) + " \"> " ) ;
        out.println( "<meta name=\"DC.Contributor\" content=\" " + this.metaInformation.get( "institution" ) + " \"> " ) ;
        
        out.println( "<meta name=\"DC.Date\" content=\"2003-01-01\"> " ) ;
        out.println( "<meta name=\"DC.Language\" content=\"en\"> " ) ;
        out.println( "<meta name=\"DC.Rights\" content=\"All rights reserved.\"> " ) ;
        
        out.println( "<link rel=stylesheet href=\"/EIDPWebApp/stylesheets/eidp.css\"> " ) ;
        
        out.println("<script language=\"JavaScript\" type=\"text/javascript\">");
        out.println("   function openGPL() {");
        out.println("       url = \"/EIDPWebApp/gpl.html\" ; ");
        out.println("       windowOpenFeatures = \"height=350,width=650,scrollbars=yes\" ; ");
        out.println("       windowName= \"GPL\" ; ");
        out.println("       var nwindow = window.open( url,windowName,windowOpenFeatures ) ; ");
        out.println("       nwindow.focus() ; ");
        out.println("   } ");
        out.println("</script>");
        
        out.println("</head>");
        out.println("<body>");
        out.println( "<div align=\"center\">" ) ;
        out.println( "<img src=\"/EIDPWebApp/images/" + this.applicationLogo + "\" border=\"0\" alt=\"DB logo\">" ); // onerror=\"this.src='/EIDPWebApp/images/eidp_logo.jpg'\">" ) ;
        out.println( "<h1>Login to: " + this.applicationName + "</h1>");
        out.println( "<h3>" + this.metaInformation.get( "institution" ) + "</h3>" ) ;
        out.println( "Contact mail: <a href=\"mailto:" + this.metaInformation.get( "contact-mail" ) + "\">" + this.metaInformation.get( "contact-mail" ) + "</a>" ) ;
        if ( ! ((String)this.metaInformation.get( "www" )).equals( "" ) ) {
            out.println( "<br><br><a href=\"http://" + this.metaInformation.get( "www" ) + "\">" + this.metaInformation.get( "www" ) + "</a>" ) ;
        }
        out.println( "<br><br>" ) ;
        out.println( "<form name=\"EIDPLoginForm\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller\" method=\"POST\"> " ) ;
        out.println( "<table id=\"login\" border=\"0\">" ) ;
        if(!this.loginErrMessage.trim().equals("")){
            out.println(" <tr><td id=\"login-msg\" colspan=\"2\">" + this.loginErrMessage + "</td></tr> " ) ;
        }
        if(!this.loginAutoLogoutMessage.equals("")){
            out.println(" <tr><td colspan=\"2\" align=\"center\"><input type=\"button\" value=\"RELOGIN\" onClick=\"relogin();\"></td></tr> " ) ;
            out.println(" </table> " ) ;
            out.println(" </form>" ) ;
        } else {
            out.println(" <tr><td align=\"right\">Username:</td><td><input type=\"text\" name=\"eidp_login\" value=\"\"></td></tr> " ) ;
            out.println(" <tr><td align=\"right\">Password:</td><td><input type=\"password\" name=\"eidp_password\" value=\"\"></td></tr> " ) ;
            out.println(" <tr><td colspan=\"2\" align=\"center\"><input type=\"submit\" value=\"Login\"></td></tr> " ) ;
            out.println(" </table> " ) ;
            out.println(" </form>" ) ;
        }
        if(!this.applicationInfo.trim().equals("")){
            out.println( "<div id=\"appinfo\" align=\"center\">" ) ;
            out.println( applicationInfo ) ;
            out.println(" </div> " ) ;
        }
        out.println( "<script type=\"text/javascript\">" ) ;
        out.println( "    if(alertMess!=\"\")" ) ;
        out.println( "        document.writeln(\"<font color='red' face='Arial,Helvetica' size='5'>\" + alertMess + \"<\\/font>\");" ) ;
        out.println( "</script>" ) ;
        out.println( "<noscript>" ) ;
        out.println( "    <font color='red' face='Arial,Helvetica' size='5'><br>Your browser does not support JavaScript!<br>" ) ;
        out.println( "    This application needs JavaScript to work!<br>" ) ;
        out.println( "    Please check your Browser-Settings<br>" ) ;
        out.println( "    or contact your support!<br><br></font>" ) ;
        out.println( "</noscript>" ) ;
        out.println(" <br> " ) ;
        
        // Logo area:
        
        out.println( " <table border=\"0\"><tr><td align=\"center\" valign=\"middle\"> " ) ;
        
        int ii = 1 ;
        
        for ( int i = 0 ; i < this.sponsorName.size() ; i++ ) {
            
            if ( ii > this.sponsorsRowNumber ) { out.println( " <br> " ) ; ii = 1 ; }
            out.println( " <img src=\"/EIDPWebApp/images/" + this.sponsorLogo.get( i ) + "\" border=\"0\" height=\"" + this.sponsorLogoHeight.get( i ) + "\" title=\"" + this.sponsorName.get( i ) + "\" alt=\"" + this.sponsorName.get( i ) + "\">" ) ;
            ii++ ;
        }
        out.println( "</td></tr></table>" ) ;
        
        out.println( "<font size=\"-1\">Enterprise Integration and Development Platform (EIDP) Version 3</font><br> " ) ;
        out.println( "<font size=\"-2\"><a href=\"javascript:openGPL();\">Copyright (c) 2005 Dominic Veit</a></font>" );
        //out.println("<p>");
        //out.println("<a href=\"http://validator.w3.org/check?uri=referer\"><img src=\"/EIDPWebApp/images/valid-html401-blue.png\" alt=\"Valid HTML 4.01 Transitional\" height=\"31\" width=\"88\" border=\"0\"></a>");
        //out.println("</p>");
        
        out.println( "</div>");
        out.println( "</body>");
        out.println( "</html>");
        
        out.close();
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost( HttpServletRequest request , HttpServletResponse response ) throws javax.servlet.ServletException , java.io.IOException {
        processRequest(request, response) ;
    }
    
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws javax.servlet.ServletException, java.io.IOException {
        processRequest(request, response) ;
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "TOOLwerk Login Servlet.";
    }
}
