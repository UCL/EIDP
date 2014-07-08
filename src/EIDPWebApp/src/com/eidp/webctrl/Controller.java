/*
 * controller.java
 *
 * Created on July 18, 2003, 2:02 PM
 */

package com.eidp.webctrl;

import com.eidp.UserScopeObject.UserScopeObject ;
import com.eidp.core.DB.DBMapping;
import com.eidp.webctrl.modules.EIDPModuleLoader ;
import com.eidp.webctrl.modules.EIDPAddInLoader;
import java.io.PrintWriter;
import com.eidp.xml.XMLDataAccess;
import com.eidp.webctrl.WebAppCache.EIDPWebAppCache;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import java.lang.reflect.Constructor;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.util.HashMap ;
import java.util.Vector ;
import java.util.Set ;
import java.util.Iterator ;
import java.util.Date ;


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

public class Controller extends HttpServlet {
    private boolean isGerman = false;
    
    /**
     * Initializes the servlet.
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws javax.servlet.ServletException {
        super.init(config);
    }
    
    /** Destroys the servlet.
     */
    @Override
    public void destroy() {
        
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SQLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException , java.sql.SQLException {
        UserScopeObject uso = new UserScopeObject() ;
        uso.session = request.getSession() ;
        this.setUserScopeObjectBeans( uso ) ;
        //        uso.sessionData = (HashMap)uso.session.getAttribute( "sessionData" ) ;
        uso.applicationContext = (String)uso.eidpWebAppCache.sessionData_get( "applicationContext" ) ;
        this.setApplicationVariables( uso ) ;
        String strLanguage = (String)uso.eidpWebAppCache.sessionData_get( "Language" ) ;
        if(strLanguage != null && strLanguage.equals("german")){
            isGerman = true;
        }
        uso.userLogin = (String)uso.eidpWebAppCache.sessionData_get( "userLogin" ) ;
        uso.userID = (String)uso.eidpWebAppCache.sessionData_get( "userID" ) ;
        uso.userCenter = (String)uso.eidpWebAppCache.sessionData_get( "userCenter" ) ;
        if ( request.getParameter( "TopPanelModule" ) != null ) {
            this.topPanelModule( request , response , uso ) ;
        } else if ( request.getParameter( "SidePanelModule" ) != null ) {
            this.sidePanelModule( request , response , uso ) ;
        } else if ( request.getParameter( "BottomPanelModule" ) != null ) {
            this.sponsorsModule( request , response , uso ) ;
        } else {
            if ( ((String)uso.eidpWebAppCache.sessionData_get( "module" )).equals( "Function" ) ) {
                if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "init" ) ) {
                    this.setUserScopeObjectBeans( uso ) ;
                    // no SponsorFrame // Stephan
                    String sponsorFrame = (String)uso.eidpWebAppCache.sessionData_get( "SponsorFrame" );
                    if( sponsorFrame != null && sponsorFrame.equals("no") ){
                        this.initModuleWithInvisibleSponsorFrame( request , response ) ;
                    } else {
                        this.initModule( request , response ) ;
                    }
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "TopPanel" ) ) {
                    this.topPanelModule( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "SidePanel" ) ) {
                    this.sidePanelModule( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "EntryPage" ) ) {
                    this.entryPageModule( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "Sponsors" ) ) {
                    this.sponsorsModule( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "SponsorsPage" ) ) {
                    this.sponsorsPageModule( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "Help" ) ) {
                    this.helpModule( request , response ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "HelpWizzard" ) ) {
                    this.helpWizzardModule( request , response , uso) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "ErrorPage" ) ) {
                    this.ErrorPageModule( request , response ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "Find" ) ) {
                    this.FindFunction( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "NewPrimaryKey" ) ) {
                    this.NewPrimaryKeyFunction( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "SelectPrimaryKey" ) ) {
                    this.SelectPrimaryKeyFunction( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "SearchPrimaryKey" ) ) {
                    this.SearchPrimaryKeyFunction( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "LoadPrimaryKey" ) ) {
                    this.LoadPrimaryKeyFunction( request , response , uso ) ;
                } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" )).equals( "PasswordModule" ) ) {
                    this.passwordModule( request , response , uso ) ;
                }
            } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "module" ) ).equals( "XMLDispatcher" ) ) {
                XMLDispatcher xmlDispatcher = new XMLDispatcher( request , response , uso ) ;
            } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "module" ) ).equals( "TOOLTip" ) ) {
                this.createTOOLTip( request , response , uso ) ;
            } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "module" ) ).equals( "AddOn" ) ) {
                String loadAddOnClass = (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ;
                EIDPModuleLoader eidpModuleLoader = new EIDPModuleLoader( loadAddOnClass , request , response , uso ) ;
            } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "module" ) ).equals( "AddIn" ) ) {
                String loadAddInClass = (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ;
                EIDPAddInLoader eidpAddInLoader = new EIDPAddInLoader( loadAddInClass , response.getWriter() , request , response , uso ) ;
            } else if (((String)uso.eidpWebAppCache.sessionData_get( "module" ) ).equals( "AddMod" ) ) {
                String loadAddModClass = (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ;
                try{
                    Class klasse = Class.forName( loadAddModClass ) ;
                    Class[] paramClasses = { HttpServletRequest.class , HttpServletResponse.class , UserScopeObject.class } ;
                    Constructor constr = klasse.getConstructor( paramClasses ) ;
                    Object[] paramObjects = { request , response , uso } ;
                    Object object = constr.newInstance( paramObjects ) ;
                } catch ( java.lang.ClassNotFoundException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.NoSuchMethodException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.InstantiationException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.IllegalAccessException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.reflect.InvocationTargetException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body>");
                    e.getCause().printStackTrace(out);
                    out.println("</html>");
                }
            } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "module" ) ).equals( "Report" ) ) {
                String reportName = (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ;
                String reportType = (String)request.getParameter( "reportType" ) ;
//                try{
                Report report = new Report( reportName , reportType , request , response , uso ) ;
//                } catch ( javax.ejb.CreateException ce ) {
//                    throw new javax.servlet.ServletException( "org.eidp.webctrl.Controller throws Exception: " + ce ) ;
//                } catch ( javax.naming.NamingException ne ) {
//                    throw new javax.servlet.ServletException( "org.eidp.webctrl.Controller throws Exception: " + ne ) ;
//                }
            } else if ( ((String)uso.eidpWebAppCache.sessionData_get("module")).equals("Fetch") ) {
//                FetchCourier courier = new FetchCourier(request, response, uso);
                String loadFetchClass = (String) uso.eidpWebAppCache.sessionData_get("moduleParameter") ;                
//                uso.dbMapper = (DBMappingRemote)((Handle)uso.session.getAttribute( "dbMapperHandle" )).getEJBObject();
//                uso.eidpWebAppCache = (EIDPWebAppCacheRemote)((Handle)uso.session.getAttribute( "eidpWebAppCacheHandle" )).getEJBObject();
                try{
                    String AddOnClassName = "com.eidp.webctrl.modules." + loadFetchClass ;
                    Class klasse = Class.forName( AddOnClassName ) ;
                    Class[] paramClasses = { HttpServletRequest.class , HttpServletResponse.class , UserScopeObject.class } ;
                    Constructor constr = klasse.getConstructor( paramClasses ) ;
                    Object[] paramObjects = { request , response , uso } ;
                    Object object = constr.newInstance( paramObjects ) ;
                } catch ( java.lang.ClassNotFoundException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.NoSuchMethodException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.InstantiationException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.IllegalAccessException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.toString() + "</h1></html>");
                } catch ( java.lang.reflect.InvocationTargetException e ) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><head></head><body><h1>" + e.getCause().getMessage() + "</h1></html>");
                }
            }
        }
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException , java.io.IOException {
        try {
            processRequest(request, response);
        } catch ( java.sql.SQLException | org.xml.sax.SAXException | javax.xml.parsers.ParserConfigurationException se ) {
            throw new javax.servlet.ServletException( "org.eidp.webctrl.Controller throws Exception: " + se ) ;
        }
    }
    
    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException , java.io.IOException {
        try {
            processRequest(request, response);
        } catch ( java.sql.SQLException | org.xml.sax.SAXException | javax.xml.parsers.ParserConfigurationException se ) {
            throw new javax.servlet.ServletException( "org.eidp.webctrl.Controller throws Exception: " + se ) ;
        }
    }
    
    /**
     * Returns a short description of the servlet.
     * @return
     */
    @Override
    public String getServletInfo() {
        return "EIDP Web Controller Servlet.";
    }
    
    /**
     * Create a javascript function to open new browser-popups.
     * @param printWriter
     * @throws SAXException
     */
    protected void createXWindowOpener( PrintWriter printWriter ) throws org.xml.sax.SAXException {
        printWriter.println( "    function XWindowOpener( module ) {" ) ;
        printWriter.println( "      url = '/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=' + module + ';show' ; " ) ;
        printWriter.println( "      windowOpenFeatures = \"height=350,width=350,scrollbars=auto\" ; " ) ;
        printWriter.println( "      windowName= \"XWindow\" ; " ) ;
        printWriter.println( "      var nwindow = window.open( url,windowName,windowOpenFeatures ) ; " ) ;
        printWriter.println( "      nwindow.focus() ; " ) ;
        printWriter.println( "    } " ) ;
    }
    
    /**
     * Initialize the HTML-structures.
     * @param response
     * @throws IOException
     * @throws SAXException
     * @return
     */
    protected PrintWriter initHTML( HttpServletResponse response ) throws java.io.IOException , org.xml.sax.SAXException {        
        response.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"");
        printWriter.println("   http://www.w3.org/TR/html4/frameset.dtd\">");
        printWriter.println( "<html>");
        printWriter.println( "<head>");
        printWriter.println( "  <meta http-equiv=\"pragma\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"cache-control\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >");
        printWriter.println( "  <style type=\"text/css\"> " ) ;
        printWriter.println( "  <!-- " ) ;
        printWriter.println( "      body { font-family:Arial,sans-serif; color:black;font-size:11pt } " ) ;
        printWriter.println( "      td.hint { color:#000000 ; font-size:8pt ; } " ) ;
        printWriter.println( "  --> " ) ;
        printWriter.println( "  </style> " );
        printWriter.println( "<title>EIDP Web Controller</title>") ;
        return printWriter ;
    }
    
    protected PrintWriter initFrameHTML( HttpServletResponse response ) throws java.io.IOException , org.xml.sax.SAXException {       
        response.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"");
        printWriter.println("   http://www.w3.org/TR/html4/loose.dtd\">");
        printWriter.println( "<html>");
        printWriter.println( "<head>");
        printWriter.println( "  <meta http-equiv=\"pragma\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"cache-control\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >");
        printWriter.println( "  <style type=\"text/css\"> " ) ;
        printWriter.println( "  <!-- " ) ;
        printWriter.println( "      body { font-family:Arial,sans-serif; color:black;font-size:11pt } " ) ;
        printWriter.println( "      td.hint { color:#000000 ; font-size:8pt ; } " ) ;
        printWriter.println( "  --> " ) ;
        printWriter.println( "  </style> " );
        printWriter.println( "  <title>EIDP Web Controller</title>") ;
        return printWriter ;
    }
    
    /**
     * Initialize HTML-structures of modules.
     * @param response
     * @throws IOException
     * @throws SAXException
     * @return
     */
    protected PrintWriter initModuleHTML( HttpServletResponse response ) throws java.io.IOException , org.xml.sax.SAXException {
        response.setContentType("text/html");
        PrintWriter printWriter = response.getWriter();
        // SidePanel has to called seperately due to concurrent request exceptions.
        printWriter.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"");
        printWriter.println("   http://www.w3.org/TR/html4/loose.dtd\">");
        printWriter.println( "<html>");
        printWriter.println( "<head>");
        printWriter.println( "  <meta http-equiv=\"pragma\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"cache-control\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >");
        printWriter.println( "  <title>EIDP Web Controller</title> " ) ;
        printWriter.println( "  <style type=\"text/css\"> " ) ;
        printWriter.println( "  <!-- " ) ;
        printWriter.println( "    body { font-family:Arial,sans-serif; color:black;font-size:11pt } " ) ;
        printWriter.println( "    a:link { text-decoration:none; color:black; } " ) ;
        printWriter.println( "    a:visited { text-decoration:none; color:black ; } " ) ;
        printWriter.println( "    a:hover { text-decoration:none; color:yellow ; } " ) ;
        printWriter.println( "    a:active { text-decoration:none; color:black ; } " ) ;
        // printWriter.println( "  table { border-style:solid;border-color:#333333;border-width:2px;border-spacing:1px ; } " ) ;
        printWriter.println( "  td { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "  td.label { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "  td.input { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "  td.white { background-color:#FFFFFF;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "--> </style> " ) ;
        printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        this.createXWindowOpener( printWriter ) ;
        printWriter.println( " </script> " ) ;
        return printWriter ;
    }
    
    /**
     * Initialize the stylesheet code.
     * @param printWriter
     * @throws IOException
     */
    protected void initStyleHTML( PrintWriter printWriter ) throws java.io.IOException {
        printWriter.println( "<style type=\"text/css\" >" ) ;
        printWriter.println( "<!--" ) ;
        printWriter.println( "body, button" ) ;
        printWriter.println( "{ font-family:Arial,sans-serif; color:black; } " ) ;
        printWriter.println( "a:link { text-decoration:none; color:black; } " ) ;
        printWriter.println( "a:visited { text-decoration:none; color:black; } " ) ;
        printWriter.println( "a:hover { text-decoration:none; color:black; } " ) ;
        printWriter.println( "a:active { text-decoration:none; color:black; } " ) ;
        printWriter.println( "-->" ) ;
        printWriter.println( "</style> " ) ;
    }
    
    /**
     * Close the HTML stream.
     * @param printWriter
     */
    protected void closeHTML( PrintWriter printWriter ) {
        printWriter.println("</body>");
        printWriter.println( "</html>" ) ;
        printWriter.close() ;
    }
    
    // ===================================================
    // Modules:
    // ===================================================
    
    /**
     * Build the frameset of the initial module to be loaded.
     * @param request
     * @param response
     * @throws IOException
     * @throws SAXException
     */
    protected void initModule( HttpServletRequest request , HttpServletResponse response ) throws java.io.IOException , org.xml.sax.SAXException {
        PrintWriter printWriter = this.initHTML( response ) ;
        printWriter.println( "</head>" ) ;

        // General Frameset defintion
        printWriter.println( "  <frameset rows=\"25,*\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // Top-Panel definition (no scrolling/no spaces
        printWriter.println( "      <frame id=\"TopPanel\" name=\"TopPanel\" src=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?TopPanelModule=Function;TopPanel;show\" scrolling=\"no\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // Frameset for SidePanel and Data
        printWriter.println( "      <frameset cols=\"154,*\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // SidePanel
        printWriter.println( "          <frame id=\"SidePanel\" name=\"SidePanel\" src=\"/EIDPWebApp/empty.html\" scrolling=\"auto\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // Data
        printWriter.println( "          <frameset rows=\"*,50\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        printWriter.println( "              <frame id=\"Data\" name=\"Data\" src=\"/EIDPWebApp/empty.html\" scrolling=\"auto\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"10\" marginheight=\"0\" >" ) ;
        // Logo Area
        printWriter.println( "              <frame id=\"Sponsors\" name=\"Sponsors\" src=\"/EIDPWebApp/empty.html\" scrolling=\"no\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        printWriter.println( "          </frameset> " ) ;
        // Close Frameset for SidePanel and Data
        printWriter.println( "      </frameset> " ) ;
        // NoFrames Area
        printWriter.println( "      <noframes> " ) ;
        printWriter.println( "          <h1>EIDP Web Controller</h1> " ) ;
        printWriter.println( "          <p>Your browser does not support FRAMES. Please update your browser to view the requested application.</p> " ) ;
        printWriter.println( "      </noframes> " ) ;
        // Close Framset Definitions
        printWriter.println( "  </frameset> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Build the frameset of the initial module to be loaded with invisible sponsor-bottom-frame => frame-rows=0
     * @param request
     * @param response
     * @throws IOException
     * @throws SAXException
     */
    protected void initModuleWithInvisibleSponsorFrame( HttpServletRequest request , HttpServletResponse response ) throws java.io.IOException , org.xml.sax.SAXException {
        PrintWriter printWriter = this.initHTML( response ) ;
        printWriter.println( "</head>" ) ;

        // General Frameset defintion
        printWriter.println( "  <frameset rows=\"25,*\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // Top-Panel definition (no scrolling/no spaces
        printWriter.println( "      <frame id=\"TopPanel\" name=\"TopPanel\" src=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?TopPanelModule=Function;TopPanel;show\" scrolling=\"no\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // Frameset for SidePanel and Data
        printWriter.println( "      <frameset cols=\"154,*\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // SidePanel
        printWriter.println( "          <frame id=\"SidePanel\" name=\"SidePanel\" src=\"/EIDPWebApp/empty.html\" scrolling=\"auto\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        // Data
        printWriter.println( "          <frameset rows=\"*,0\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        printWriter.println( "              <frame id=\"Data\" name=\"Data\" src=\"/EIDPWebApp/empty.html\" scrolling=\"auto\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"10\" marginheight=\"0\" >" ) ;
        // Logo Area
        printWriter.println( "              <frame id=\"Sponsors\" name=\"Sponsors\" src=\"/EIDPWebApp/empty.html\" scrolling=\"no\" border=\"0\" frameborder=\"0\" framespacing=\"0\" marginwidth=\"0\" marginheight=\"0\" >" ) ;
        printWriter.println( "          </frameset> " ) ;
        // Close Frameset for SidePanel and Data
        printWriter.println( "      </frameset> " ) ;
        // NoFrames Area
        printWriter.println( "      <noframes> " ) ;
        printWriter.println( "          <h1>EIDP Web Controller</h1> " ) ;
        printWriter.println( "          <p>Your browser does not support FRAMES. Please update your browser to view the requested application.</p> " ) ;
        printWriter.println( "      </noframes> " ) ;
        // Close Framset Definitions
        printWriter.println( "  </frameset> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Build the top-panel module.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    protected void topPanelModule( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        PrintWriter printWriter = this.initFrameHTML( response ) ;
        // set the top panel due to the role membership of the user.
        // 1. read in webctrl.xml
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/webmenu.xml" ;
        uso.xmlWebMenu = new XMLDataAccess( xmlfile ) ;
        Vector TopPanelResponseVector = new Vector();
        TopPanelResponseVector = uso.xmlWebMenu.getNodeListsByName( "menu,toppanel,entry" );
        this.processTopPanelEntries( response , printWriter , TopPanelResponseVector , uso ) ;
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\">" ) ;
        printWriter.println( "  <!--" ) ;
        printWriter.println( "    parent.Sponsors.location.href=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?BottomPanelModule=Function;Sponsors;show\" ; " ) ;
        printWriter.println( "  // -->" ) ;
        printWriter.println( "  </script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Build the sidePanel module.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void sidePanelModule( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initFrameHTML( response ) ;
        // set the side panel due to the role membership of the user.
        // 1. read in webmenu.xml
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/webmenu.xml" ;
        uso.xmlWebMenu = new XMLDataAccess( xmlfile ) ;
        // open PasswordForm
        Vector SidePanelResponseVector = new Vector() ;
        SidePanelResponseVector = uso.xmlWebMenu.getNodeListsByName( "menu,sidepanel" ) ;
        this.processSidePanel( response , printWriter , (NodeList)SidePanelResponseVector.get( 0 ) , uso ) ;
        Vector blockPasswordRoles = (Vector)uso.xmlWebMenu.getElementsByName( "menu,toppanel,block-password-role" ) ;
        Iterator bpi = blockPasswordRoles.iterator() ;
        boolean blockPassword = false ;
        while( bpi.hasNext() ) {
            if ( uso.eidpWebAppCache.userRoles_contains( (String)bpi.next() ) || blockPasswordRoles.contains( "ALL" ) ) {
                blockPassword = true ;
            }
        }
        String passwordExpired = (String)uso.eidpWebAppCache.sessionData_get( "passwordExpired" ) ;
        if ( passwordExpired.equals( "true" ) && blockPassword == false ) {
            printWriter.println( " <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
            this.createXWindowOpener(printWriter);
            printWriter.println( "   XWindowOpener( 'Function;PasswordModule' ) ; " ) ;
            printWriter.println( " </script> " ) ;
            // variable zuruecksetzen, damit form nicht immer wieder aufgeht
            uso.eidpWebAppCache.sessionData_set("passwordExpired", "false");
        }
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Sets the value of all Application-Variables.
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void setApplicationVariables( UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        // 1. read in application.xml
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/application.xml" ;
        uso.xmlApplication = new XMLDataAccess( xmlfile ) ;
        Vector ApplicationResponseVector = new Vector() ;
        ApplicationResponseVector = uso.xmlApplication.getNodeListsByName( "application-data" ) ;
        String strVar = this.getApplicationVariableValue(ApplicationResponseVector, "date-format", uso);
        uso.eidpWebAppCache.sessionData_set( "DateFormat" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "sponsor-frame", uso);
        uso.eidpWebAppCache.sessionData_set( "SponsorFrame" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "sponsor-button", uso);
        uso.eidpWebAppCache.sessionData_set( "SponsorButton" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "animated-help", uso);
        uso.eidpWebAppCache.sessionData_set( "AnimatedHelp" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "help-wizzard", uso);
        uso.eidpWebAppCache.sessionData_set( "HelpWizzard" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "askforvisit", uso);
        uso.eidpWebAppCache.sessionData_set( "AskForVisit" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "language", uso);
        uso.eidpWebAppCache.sessionData_set( "Language" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "keysubmit", uso); //Check
        uso.eidpWebAppCache.sessionData_set( "KeySubmit" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "logo-width", uso);
        uso.eidpWebAppCache.sessionData_set( "LogoWidth" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "logo-height", uso);
        uso.eidpWebAppCache.sessionData_set( "LogoHeight" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "logo-name", uso);
        uso.eidpWebAppCache.sessionData_set( "LogoName" , strVar ) ;        
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "submit-button-label", uso);
        uso.eidpWebAppCache.sessionData_set( "SubmitButtonLabel" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "reset-button-label", uso);
        uso.eidpWebAppCache.sessionData_set( "ResetButtonLabel" , strVar ) ;
        strVar = this.getApplicationVariableValue(ApplicationResponseVector, "auto-logout-interval", uso);
        uso.eidpWebAppCache.sessionData_set( "auto-logout-interval" , strVar ) ;       
    }
    
    /**
     * Gets the value of a Application-Variable.
     * @param ApplicationResponseVector
     * @param tag
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected String getApplicationVariableValue( Vector ApplicationResponseVector , String tag , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        String retString = "";
        try{
            retString = (String)((Vector)uso.xmlApplication.getElementsByName( tag , (NodeList)ApplicationResponseVector.get( 0 ) )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            retString = "" ;
        }
        return retString;
    }
    
    /**
     * Build the sponsors module.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void sponsorsModule( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initFrameHTML( response ) ;
        // set the side panel due to the role membership of the user.
        // 1. read in webmenu.xml
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/webmenu.xml" ;
        uso.xmlWebMenu = new XMLDataAccess( xmlfile ) ;
        Vector SponsorsResponseVector = new Vector() ;
        SponsorsResponseVector = uso.xmlWebMenu.getNodeListsByName( "sponsors,entry" ) ;
        this.processSponsors( response , printWriter , SponsorsResponseVector , uso ) ;
        printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\">" ) ;
        printWriter.println( "  parent.Data.location.href=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;EntryPage;show\";" ) ;
        printWriter.println( "</script>" ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Build the sponsores page module.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void sponsorsPageModule( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initModuleHTML( response ) ;
        // set the side pane due to the role membership of the user.
        // 1. read in webmenu.xml
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/webmenu.xml" ;
        uso.xmlWebMenu = new XMLDataAccess( xmlfile ) ;
        Vector SponsorsResponseVector = new Vector() ;
        SponsorsResponseVector = uso.xmlWebMenu.getNodeListsByName( "sponsors,entry" ) ;
        this.processSponsorsPage( response , printWriter , SponsorsResponseVector , uso ) ;
        printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Create the entry page.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void entryPageModule( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/controller.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/webmenu.xml" ;
        uso.xmlWebMenu = new XMLDataAccess( xmlfile ) ;
        //<editor-fold defaultstate="collapsed" desc="onerror replacement">
        xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/application.xml" ;
        uso.xmlApplication = new XMLDataAccess( xmlfile ) ;
        //</editor-fold>
        PrintWriter printWriter = this.initFrameHTML( response ) ; //prev initModuleHTML
        this.processEntryPage( response , printWriter , uso ) ;
        printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Module to change the password.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void passwordModule( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initHTML( response ) ;
        try {
            this.processPasswordModule( request , response , printWriter , uso ) ;
        } catch ( java.lang.Exception e ) {
            throw new javax.servlet.ServletException( "PassswordModule throws Exception: " + e ) ;
        }
        this.closeHTML( printWriter ) ;
    }
    
    protected void helpWizzardModule( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initHTML( response ) ;
        try {
            this.processHelpWizzardModule( request , response , printWriter , uso ) ;
        } catch ( java.lang.Exception e ) {
            throw new javax.servlet.ServletException( "HelpWizzardModule throws Exception: " + e ) ;
        }
        this.closeHTML( printWriter ) ;
    }
    /**
     * Find function. This is a special function that can be used in
     * the controller.xml file.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void FindFunction( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initFrameHTML( response ) ;
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/controller.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        Vector controllerModules = uso.xmlDataAccess.getNodeListsByName( "web-controller,module" ) ;
        NodeList findNode = null ;
        for ( int i = 0 ; i < controllerModules.size() ; i++ ) {
            String moduleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)controllerModules.get( i ) ) ).get( 0 ) ;
            if ( moduleName.equals( "Find" ) ) {
                findNode = (NodeList)controllerModules.get( i ) ;
            }
        }
        this.processFindFunction( request , response , printWriter , findNode , uso ) ;
        printWriter.println( "<script language=\"JavaScript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * NewPrimaryKey function. This is a special function that can be used in
     * the controller.xml file.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void NewPrimaryKeyFunction( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initModuleHTML( response ) ;
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/controller.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        Vector controllerModules = uso.xmlDataAccess.getNodeListsByName( "web-controller,module" ) ;
        NodeList primaryKeyNode = null ;
        for ( int i = 0 ; i < controllerModules.size() ; i++ ) {
            String moduleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)controllerModules.get( i ) ) ).get( 0 ) ;
            if ( moduleName.equals( "NewPrimaryKey" ) ) {
                primaryKeyNode = (NodeList)controllerModules.get( i ) ;
            }
        }
        this.processNewPrimaryKeyFunction( request , response , printWriter , primaryKeyNode , uso ) ;
        printWriter.println( "<script language=\"JavaScript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * SelectPrimaryKey function. This is a special function that can be used in
     * the controller.xml file.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void SelectPrimaryKeyFunction( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/controller.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/application.xml" ;
        uso.xmlApplication = new XMLDataAccess( xmlfile ) ;
        Vector controllerModules = uso.xmlDataAccess.getNodeListsByName( "web-controller,module" ) ;
        NodeList primaryKeyNode = null ;
        for ( int i = 0 ; i < controllerModules.size() ; i++ ) {
            String moduleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)controllerModules.get( i ) ) ).get( 0 ) ;
            if ( moduleName.equals( "SelectPrimaryKey" ) ) {
                primaryKeyNode = (NodeList)controllerModules.get( i ) ;
                break ;
            }
        }
        String moduleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , primaryKeyNode ) ).get( 0 ) ;
        String primaryKeyLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-key-label" , primaryKeyNode ) ).get( 0 ) ;
        String primaryKey = "" ;
        String publicPrimaryKey = "" ;
        try {
            publicPrimaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-primary-key" , primaryKeyNode ) ).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            primaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-key" , primaryKeyNode ) ).get( 0 ) ;
        }
        //<editor-fold defaultstate="collapsed" desc="onerror replacement">
        String applicationLogo = new String();
        if (!uso.xmlApplication.getElementsByName("application-data,app-logo").isEmpty()) {
            applicationLogo = (String)((Vector)uso.xmlApplication.getElementsByName( "application-data,app-logo" )).get( 0 ) ;
        } else {
            applicationLogo = "eidp_logo.jpg";
        }
        //</editor-fold>
        PrintWriter printWriter = this.initFrameHTML( response ) ;
        printWriter.println( "  <style type=\"text/css\"> " ) ;
        printWriter.println( "  <!-- " ) ;
        printWriter.println( "    a:link { text-decoration:none; color:black; } " ) ;
        printWriter.println( "    a:visited { text-decoration:none; color:black ; } " ) ;
        printWriter.println( "    a:hover { text-decoration:none; color:yellow ; } " ) ;
        printWriter.println( "    a:active { text-decoration:none; color:black ; } " ) ;
        printWriter.println( "    td { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "    td.label { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "    td.input { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "    td.data { background-color:#EEEEEE;color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "  --> " ) ;
        printWriter.println( "  </style>" ) ;
        printWriter.println( "</head>" ) ;
        printWriter.println( "<body>") ;
        printWriter.println( "<img src=\"/EIDPWebApp/images/" + applicationLogo + "\" border=\"0\" width=\"40\" height=\"20\" alt=\"app_logo\"><font size=\"+1\">&nbsp;&nbsp;<b>Select</b></font>" ) ;
        printWriter.println( "<hr> " ) ;
        printWriter.println( "<div align=\"center\"> " ) ;
        printWriter.println( "  <form name=\"" + moduleName + "\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller\" method=\"POST\"> " ) ;
        printWriter.println( "  <input type=\"hidden\" name=\"module\" value=\"Function;LoadPrimaryKey;store\"> " ) ;
        printWriter.println( "<table border=\"0\"> " ) ;
        printWriter.println( "  <tr><td class=\"label\">" + primaryKeyLabel + "</td> " ) ;
        if ( ! publicPrimaryKey.equals( "" ) ) {
            printWriter.println( "  <td class=\"input\"><input type=\"text\" name=\"" + publicPrimaryKey + "\"></td></tr> " ) ;
        } else {
            printWriter.println( "  <td class=\"input\"><input type=\"text\" name=\"" + primaryKey + "\"></td></tr> " ) ;
        }
        printWriter.println( "</table> " ) ;
        printWriter.println( "  <input type=\"submit\" value=\"Select\">&nbsp;<input type=\"reset\" value=\"Clear\"> " ) ;
        printWriter.println( "  </form> " ) ;
        printWriter.println( "  </div> " ) ;
        printWriter.println( "<script language=\"JavaScript\" type=\"text/javascript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }

    /**
     * LoadPrimaryKey function. This is a special function that can be used in
     * the controller.xml file.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void LoadPrimaryKeyFunction( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/controller.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        Vector controllerModules = uso.xmlDataAccess.getNodeListsByName( "web-controller,module" ) ;
        NodeList primaryKeyNode = null ;
        for ( int i = 0 ; i < controllerModules.size() ; i++ ) {
            String moduleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)controllerModules.get( i ) ) ).get( 0 ) ;
            if ( moduleName.equals( "SelectPrimaryKey" ) ) {
                primaryKeyNode = (NodeList)controllerModules.get( i ) ;
                break ;
            }
        }
        this.processLoadPrimaryKeyFunction( request , response , primaryKeyNode , uso ) ;
    }
    
    /**
     * SearchPrimaryKey function. This is a special function that can be used in
     * the controller.xml file.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void SearchPrimaryKeyFunction( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/controller.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/application.xml" ;
        uso.xmlApplication = new XMLDataAccess( xmlfile ) ;
        Vector controllerModules = uso.xmlDataAccess.getNodeListsByName( "web-controller,module" ) ;
        NodeList searchKeyNode = null ;
        for ( int i = 0 ; i < controllerModules.size() ; i++ ) {
            String moduleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)controllerModules.get( i ) ) ).get( 0 ) ;
            if ( moduleName.equals( "SearchPrimaryKey" ) ) {
                searchKeyNode = (NodeList)controllerModules.get( i ) ;
                break ;
            }
        }
        String moduleName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , searchKeyNode ) ).get( 0 ) ;
        
        /*
        String primaryKeyLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-key-label" , primaryKeyNode ) ).get( 0 ) ;
        String primaryKey = "" ;
        String publicPrimaryKey = "" ;
        try {
            publicPrimaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-primary-key" , primaryKeyNode ) ).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            primaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-key" , primaryKeyNode ) ).get( 0 ) ;
        }
         **/
        NodeList searchNode = (NodeList) uso.xmlDataAccess.getNodeListsByName( "search", searchKeyNode ).get( 0 ) ;
        Vector searchFields = uso.xmlDataAccess.getNodeListsByName("field", searchNode) ;
        //<editor-fold defaultstate="collapsed" desc="onerror replacement">    
        String applicationLogo = new String();
        if (!uso.xmlApplication.getElementsByName("application-data,app-logo").isEmpty()) {
            applicationLogo = (String)((Vector)uso.xmlApplication.getElementsByName( "application-data,app-logo" )).get( 0 ) ;
        } else {
            applicationLogo = "eidp_logo.jpg";
        }
        //</editor-fold>
        PrintWriter printWriter = this.initFrameHTML( response ) ;
        printWriter.println( "  <style type=\"text/css\"> " ) ;
        printWriter.println( "  <!-- " ) ;
        printWriter.println( "    a:link { text-decoration:none ; color:black ; } " ) ;
        printWriter.println( "    a:visited { text-decoration:none ; color:black ; } " ) ;
        printWriter.println( "    a:hover { text-decoration:none ; color:yellow ; } " ) ;
        printWriter.println( "    a:active { text-decoration:none ; color:black ; } " ) ;
        printWriter.println( "    table#results { border: 1px solid #dddddd ; }") ;
        printWriter.println( "    tr#reshead { background-color:#dddddd ; color:#000000 ; font-size:11pt ; text-align:center ; } " ) ;
        printWriter.println( "    td { background-color:#dddddd ; color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "    td.input { background-color:#dddddd ; color:#000000 ; font-size:11pt ; } " ) ;
        printWriter.println( "    td.data { background-color:#eeeeee ; color:#000000 ; font-size:11pt ; text-align:center ; } " ) ;
        printWriter.println( "  --> " ) ;
        printWriter.println( "  </style>" ) ;
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\">") ;
        printWriter.println( "  <!--") ;
        printWriter.println( "    var hinttxt = new Array();") ;
        for (int i = 0; i < searchFields.size(); i++) {
            NodeList item = (NodeList) searchFields.get(i);
            String itemHint = (String) ((Vector)uso.xmlDataAccess.getElementsByName( "hint" , item )).get( 0 ) ;
            printWriter.println( "    hinttxt[" + i + "] = '" + itemHint + "';") ;
        }
        printWriter.println( "    function displayHint(select){") ;
        printWriter.println( "      var option = select.options[select.selectedIndex];") ;
        printWriter.println( "      var hint = document.getElementById('hintmsg');") ;
        printWriter.println( "      hint.innerHTML = hinttxt[option.index];") ;
        printWriter.println( "    }") ;
        printWriter.println( "    linkClicked = 0 ; " ) ;
        printWriter.println( "    function markRow(rowid){ " ) ;
        printWriter.println( "      document.getElementById(rowid).style.backgroundColor='#ffffff'; " ) ;
        printWriter.println( "      document.getElementById(rowid).style.fontWeight='bold'; " ) ;
        printWriter.println( "    } " ) ;
        printWriter.println( "    function demarkRow(rowid){ " ) ;
        printWriter.println( "      document.getElementById(rowid).style.backgroundColor='#eeeeee'; " ) ;
        printWriter.println( "      document.getElementById(rowid).style.fontWeight='normal'; " ) ;
        printWriter.println( "    } " ) ;
        printWriter.println( "    function getPatient(patid){ " ) ;
        printWriter.println( "      parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;Find;store&selectValue=' + patid; " ) ;
        printWriter.println( "    }") ;
        printWriter.println( "  // -->") ;
        printWriter.println( "  </script>") ;
        printWriter.println( "</head>" ) ;
        printWriter.println( "<body>") ;
        printWriter.println( "  <img src=\"/EIDPWebApp/images/" + applicationLogo + "\" border=\"0\" width=\"40\" height=\"20\" alt=\"app_logo\"><font size=\"+1\">&nbsp;&nbsp;<b>Search</b></font>" ) ;
        printWriter.println( "  <hr> " ) ;
        printWriter.println( "  <div align=\"center\"> " ) ;
        printWriter.println( "    <form name=\"" + moduleName + "\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller\" method=\"POST\"> " ) ;
        printWriter.println( "      <input type=\"hidden\" name=\"module\" value=\"Function;SearchPrimaryKey;display\"> " ) ;
        printWriter.println( "        <table>" ) ;
        printWriter.println( "          <tr>" ) ;
        printWriter.println( "            <td>") ;
        printWriter.println( "              <select name=\"searchId\" onChange=\"displayHint(this);\">") ;
        for (int i = 0; i < searchFields.size(); i++) {
            NodeList item = (NodeList) searchFields.get(i);
            //String itemID = (String) ((Vector)uso.xmlDataAccess.getElementsByName( "id" , item ) ).get( 0 ) ;
            String itemLabel = (String) ((Vector)uso.xmlDataAccess.getElementsByName( "label" , item ) ).get( 0 ) ;
            printWriter.println( "                <option value=\"" + i + "\">" + itemLabel + "</option>") ;
        }
        printWriter.println( "              </select>") ;
        printWriter.println( "              <input name=\"searchTxt\" type=\"text\" size=\"25\">") ;
        printWriter.println( "            </td>") ;
        printWriter.println( "            <td id=\"hintmsg\" class=\"hint\">") ;
        printWriter.println( "            </td>") ;
        printWriter.println( "          </tr>") ;
        printWriter.println( "        </table>" ) ;
        printWriter.println( "      <input type=\"submit\" value=\"Select\">&nbsp;<input type=\"reset\" value=\"Clear\"> " ) ;
        printWriter.println( "    </form> " ) ;
        printWriter.println( "  </div> " ) ;
        if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "display" ) ) {
            //<editor-fold desc="DB query methods">
            // get parameters for querying
            String searchId = (String)request.getParameter("searchId");
            String searchTxt = (String)request.getParameter("searchTxt");
            NodeList queryNode = (NodeList)searchFields.get(Integer.parseInt(searchId));
            String searchID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , queryNode ) ).get( 0 ) ;
            String searchMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , queryNode) ).get( 0 ) ;
            String centerID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "center-id" , searchKeyNode ) ).get( 0 ) ;
            String dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , searchKeyNode ) ).get( 0 ) ;
            HashMap paramMap = new HashMap() ;
            String cR = new String() ;
            HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
            Object [] centerRoles = ((Set)cRoles.keySet()).toArray() ;
            for ( int ci = 0 ; ci < centerRoles.length ; ci++ ) {
                if ( ci > 0 ) { cR += " , " ; }
                cR += (String)centerRoles[ci] ;
            }
            paramMap.put( centerID , cR ) ;
            paramMap.put( "application" , uso.applicationContext ) ;
            paramMap.put( searchID , searchTxt ) ;
            uso.dbMapper.DBAction( dataset , searchMethod , paramMap ) ;
            //</editor-fold>
            String publicSelectField = new String() ;
            try {
                publicSelectField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-select-field" , searchKeyNode ) ).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                publicSelectField = "" ;
            }            
            NodeList displayNode = (NodeList) uso.xmlDataAccess.getNodeListsByName( "display", searchKeyNode ).get( 0 ) ;
            Vector displayFields = uso.xmlDataAccess.getNodeListsByName("field", displayNode) ;
            printWriter.println( "  <hr> " ) ;
            printWriter.println( "  <div align=\"center\"> " ) ;
            printWriter.println( "    <table id=\"results\">") ;
            printWriter.println( "      <tr id=\"reshead\">") ;
            for (int i = 0; i < displayFields.size(); i++) {
                NodeList item = (NodeList) displayFields.get(i);
                String itemID = (String) ((Vector)uso.xmlDataAccess.getElementsByName( "id" , item ) ).get( 0 ) ;
                String itemLabel = (String) ((Vector)uso.xmlDataAccess.getElementsByName( "label" , item ) ).get( 0 ) ;
                printWriter.println( "        <td>" + itemLabel + "</td>") ;
            }
            printWriter.println( "      </tr>") ;
            // table rows            
            for ( int i = 0 ; i < uso.dbMapper.size() ; i++ ) {
                String strID = (String)((HashMap)uso.dbMapper.getRow( i )).get( publicSelectField );              
                printWriter.println( "      <tr style=\"cursor:pointer;\" onClick=\"javascript:if ( linkClicked == 0 ) { linkClicked = 1 ; getPatient(" + strID + ") ; } \" bgcolor=\"#eeeeee\" id=\"" + strID + "\" onMouseover=\"javascript:markRow(" + strID + ")\" onMouseout=\"javascript:demarkRow(" + strID + ")\"> " ) ;
                HashMap rowvalue = (HashMap)uso.dbMapper.getRow( i ) ;
                for ( int j = 0; j < displayFields.size(); j++) {
                    NodeList item = (NodeList) displayFields.get(j);
                    String itemID = (String) ((Vector)uso.xmlDataAccess.getElementsByName( "id" , item ) ).get( 0 ) ;
                    String itemValue = (String)rowvalue.get( itemID );
                    printWriter.println( "        <td class=\"data\">" + itemValue + "</td>") ;
                }
                printWriter.println( "      </tr>") ;
            }
            if (uso.dbMapper.size() == 0) {
                printWriter.println( "      <tr>") ;
                printWriter.println( "        <td style=\"border:1px solid #ff0000;text-align:center;color:#ff0000;background-color:#ffd4d4;\" colspan=\"" + displayFields.size() + "\">No entries found</td>") ;
                printWriter.println( "      </tr>") ;
            }
            printWriter.println( "    </table>") ;
            printWriter.println( "  </div> " ) ;
        }
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected void helpModule( HttpServletRequest request , HttpServletResponse response ) throws javax.servlet.ServletException , java.io.IOException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.sql.SQLException {
        PrintWriter printWriter = this.initModuleHTML( response) ;
        this.processHelpPage( response , printWriter ) ;
        printWriter.println( "<script language=\"JavaScript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    // Process methods:    
    /**
     *
     * @param response
     * @param printWriter
     * @param topPanelResponseVector
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processTopPanelEntries( HttpServletResponse response , PrintWriter printWriter, Vector topPanelResponseVector , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        Vector noPrimaryMsgResponseVector = uso.xmlWebMenu.getNodeListsByName( "menu,toppanel" );
        String noPrimaryMsg = "";
        try {
            noPrimaryMsg = (String)((Vector)uso.xmlWebMenu.getElementsByName( "no-primary-msg" , (NodeList)noPrimaryMsgResponseVector.get( 0 ) )).get( 0 ) ;
        } catch (java.lang.ArrayIndexOutOfBoundsException e){}
        Vector blockPasswordRoles = (Vector)uso.xmlWebMenu.getElementsByName( "menu,toppanel,block-password-role" ) ;
        
        String stylesheet = getCssTheme(uso);
        String bgcolor = new String();
        if(stylesheet.length() > 0){
            bgcolor = "";
            printWriter.println("  <link href=\"/EIDPWebApp/stylesheets/" + stylesheet + ".css\" rel=\"stylesheet\" type=\"text/css\">");
        } else {
            bgcolor = " bgcolor=\"#c0c0c0\"";
        }
        
        printWriter.println( "  <style type=\"text/css\" media=\"screen\">" ) ;
        printWriter.println( "  <!--" ) ;
        printWriter.println( "    body, button { font-family:Arial,sans-serif; color:black; } " ) ;
        printWriter.println( "    a:link { text-decoration:none; color:black; } " ) ;
        printWriter.println( "    a:visited { text-decoration:none; color:black; } " ) ;
        printWriter.println( "    a:hover { text-decoration:none; color:black; } " ) ;
        printWriter.println( "    a:active { text-decoration:none; color:black; } " ) ;
        printWriter.println( "  -->" ) ;
        printWriter.println( "  </style> " ) ;
        
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        printWriter.println( "  <!--" ) ;
        printWriter.println( "    function postPlugin( plugin ) { " ) ;
        printWriter.println( "    parent.Data.location.href=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=PlugIn&PlugIn=\" + plugin ; " ) ;
        printWriter.println( "    } " ) ;
        this.createXWindowOpener( printWriter ) ;
        printWriter.println( "  // -->" ) ;
        printWriter.println( "  </script> " ) ;        
        this.createJavaScriptTopPanelCheck( printWriter, noPrimaryMsg, uso );
        
        printWriter.println( "</head>" ) ;
        printWriter.println( "<body>" ) ;
        printWriter.println( "  <form name=\"ModuleSelect\" action=\"\"> " ) ;
        
        printWriter.println( "    <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"> " ) ;
        printWriter.println( "      <tr id=\"toppanel\"" + bgcolor + " valign=\"top\" > " ) ;
        printWriter.println( "        <td align=\"left\" valign=\"top\"> " ) ;
        for ( int i = 0 ; i < topPanelResponseVector.size() ; i++ ) {
            // get entry data
            Vector rolePermissions = new Vector() ;
            rolePermissions = (Vector)(uso.xmlWebMenu.getElementsByName( "role-name" , (NodeList)topPanelResponseVector.get( i ) )) ;
            String type = (String)((Vector)(uso.xmlWebMenu.getElementsByName( "type" , (NodeList)topPanelResponseVector.get( i ) ) )).get( 0 ) ;
            String check = "";
            try{
                check = (String)((Vector)(uso.xmlWebMenu.getElementsByName( "check" , (NodeList)topPanelResponseVector.get( i ) ) )).get( 0 ) ;
            }catch(java.lang.ArrayIndexOutOfBoundsException e){
            }
            
            for ( int rp = 0 ; rp < rolePermissions.size() ; rp++ ) {
                if ( uso.eidpWebAppCache.userRoles_contains( (String)rolePermissions.get( rp ) ) || rolePermissions.get( rp ).equals( "ALL" ) ) {
                    if ( type.equals( "single" ) ) {
                        String label = (String)((Vector)(uso.xmlWebMenu.getElementsByName( "label" , (NodeList)topPanelResponseVector.get( i ) ) )).get( 0 ) ;
                        String module = (String)((Vector)(uso.xmlWebMenu.getElementsByName( "module" , (NodeList)topPanelResponseVector.get( i ) ) )).get( 0 ) ;
                        String moduleAction = ";show" ;
                        String[] moduleArray = module.split( ";" ) ;
                        boolean repCheck = false ;
                        if ( moduleArray.length == 3 && moduleArray[0].equals( "Report" ) ) {
                            moduleAction = "" ;
                        }
                        if ( moduleArray[0].equals( "Report" ) ) repCheck = true ;
                        // Check if requested function is logout, then forward the logout (Login) servlet to the complete Contents-Area.
                        if ( module.equals( "Function;logout" ) ) {
                            printWriter.println( "          <button type=\"button\" onClick=\"parent.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + module + ";show'\"><font size=\"-1\"><b>" + label + "</b></font></button> " ) ;
                        } else {
                            if ( module.equals( "Function;NewPrimaryKey" ) ) {
                                printWriter.println( "          <button type=\"button\" onClick=\"check=confirm('Create NEW?');if ( check==true ) {parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + module + ";show'; }\"><font size=\"-1\"><b>" + label + "</b></font></button> " ) ;
                            } else {
                                // old
                                //                                printWriter.println( "<button type=\"button\" onClick=\"parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + module + ";show'\"><font size=\"-1\"><b>" + label + "</b></font></button> " ) ;
                                // new
                                if( check.equals("true") ) {
                                    if ( repCheck ) printWriter.println( "          <button type=\"button\" onClick=\"LoadReport('" + module + moduleAction + "')\"><font size=\"-1\"><b>" + label + "</b></font></button> " ) ;
                                    else printWriter.println( "          <button type=\"button\" onClick=\"LoadModule('" + module + moduleAction + "')\"><font size=\"-1\"><b>" + label + "</b></font></button> " ) ;
                                } else
                                    printWriter.println( "          <button type=\"button\" onClick=\"parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + module + moduleAction + "'\"><font size=\"-1\"><b>" + label + "</b></font></button> " ) ;
                            }
                        }
                    }
                    if ( type.equals( "PlugIns" ) ) {
                        printWriter.println( "          <select name=\"PlugIn\" onChange=\"javascript:postPlugin(this.options[this.selectedIndex].value);\"> " ) ;
                        printWriter.println( "            <option value=\"\">PlugIns</option> " ) ;
                        Vector labels = new Vector() ;
                        Vector modules = new Vector() ;
                        labels = (Vector)(uso.xmlWebMenu.getElementsByName( "label" , (NodeList)topPanelResponseVector.get( i ) )) ;
                        modules = (Vector)(uso.xmlWebMenu.getElementsByName( "module" , (NodeList)topPanelResponseVector.get( i ) )) ;
                        if ( labels.size() != modules.size() ) {
                            throw new javax.servlet.ServletException( "Exception thrown by org.eidp.webctrl.Controller: Label URL Definitions in webctrl.xml do not match." ) ;
                        }
                        for ( int pi = 0 ; pi < labels.size() ; pi++ ) {
                            printWriter.println( "            <option value=\"" + modules.get( pi ) + ";load\">" + labels.get( pi ) + "</option> " ) ;
                        }
                        printWriter.println( "          </select> " ) ;
                    }
                    break ; // breaks out of the rolePermission loop if a permission as already been detected.
                }
            }
            
        }
        printWriter.println( "        </td> " ) ;
        printWriter.println( "        <td align=\"right\" valign=\"top\"> " ) ;
        Iterator bpi = blockPasswordRoles.iterator() ;
        boolean blockPassword = false ;
        while( bpi.hasNext() ) {
            if ( uso.eidpWebAppCache.userRoles_contains( (String)bpi.next() ) || blockPasswordRoles.contains( "ALL" ) ) {
                blockPassword = true ;
            }
        }
        if ( blockPassword == false ) {
            printWriter.println( "          <button type=\"button\" onClick=\"XWindowOpener( 'Function;PasswordModule' );\"><font size=\"-1\"><b>Change Password</b></font></button> " ) ;
        }
        printWriter.println( "          <button type=\"button\" onClick=\"parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SponsorsPage;show'\"><font size=\"-1\"><b>Sponsors</b></font></button> " ) ;
        printWriter.println( "        </td>" ) ;
        printWriter.println( "      </tr> " ) ;
        printWriter.println( "    </table> " ) ;
        printWriter.println( "  </form> " ) ;
    }
    
    /**
     * Create specific javascript structures for the topPanel.
     * @param printWriter
     * @param noPrimaryMsg
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void createJavaScriptTopPanelCheck( PrintWriter printWriter, String noPrimaryMsg , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        printWriter.println( "  <!--" ) ;
        printWriter.println( "    var loading = 0 ; " ) ;
        printWriter.println( "    function LoadModule( module ) {" ) ;
        printWriter.println( "      userChanges = parent.Data.userChanges ; " ) ;
        printWriter.println( "      if ( loading == 1 ) { " ) ;
        printWriter.println( "        alert( \"Loading another module. Please wait.\" ) ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "        if ( userChanges == 1 ) { " ) ;
        if(isGerman)
            printWriter.println( "          confirmLoadModule = confirm( \"Sie haben Aenderungen vorgenommen, ohne diese zu speichern!\\nWollen Sie diese Aenderungen verwerfen? \" ) ; " ) ;
        else
            printWriter.println( "          confirmLoadModule = confirm( \"There have been changes made to your data. Do you want to discard changes? \" ) ; " ) ;
        printWriter.println( "          if ( confirmLoadModule == true ) { " ) ;
        printWriter.println( "            loading = 1 ; " ) ;
        printWriter.println( "            url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
        printWriter.println( "            parent.Data.location.href=url ;" ) ;
        printWriter.println( "          } " ) ;
        printWriter.println( "        } else if ( userChanges == 0 ) { " ) ;
        //        printWriter.println( "                      loading = 1 ; " ) ;
        printWriter.println( "          url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
        printWriter.println( "          parent.Data.location.href=url ;" ) ;
        printWriter.println( "        } else { " ) ;
        printWriter.println( "          noPrimaryMsg = \"" + noPrimaryMsg + "\" " ) ;
        printWriter.println( "          alert( noPrimaryMsg ) ; " ) ;
        printWriter.println( "        } " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "    } " ) ;
        
        printWriter.println( "    function LoadReport( module ) {" ) ;
        printWriter.println( "      userChanges = parent.Data.userChanges ; " ) ;
        printWriter.println( "      if ( loading == 1 ) { " ) ;
        printWriter.println( "        alert( \"Loading another module. Please wait.\" ) ; " ) ;
        printWriter.println( "      } else { " ) ;
        printWriter.println( "        if ( module.match( /.+;.+;show/ ) || module.match( /.+;.+;.+;show/ ) ) { " ) ;
        printWriter.println( "          if ( userChanges == 1 ) { " ) ;
        if(isGerman)
            printWriter.println( "            confirmLoadModule = confirm( \"Sie haben �nderungen vorgenommen, ohne diese zu speichern!\\nWollen Sie diese �nderungen verwerfen? \" ) ; " ) ;
        else
            printWriter.println( "            confirmLoadModule = confirm( \"There have been changes made to your data. Do you want to discard changes? \" ) ; " ) ;
        printWriter.println( "            if ( confirmLoadModule == true ) { " ) ;
        printWriter.println( "              loading = 1 ; " ) ;
        printWriter.println( "              url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
        printWriter.println( "              windowOpenFeatures = \"height=200,width=450,scrollbars=auto\" ; " ) ;
        printWriter.println( "              windowName= \"Report\" ; " ) ;
        printWriter.println( "              var nwindow = window.open( null ,windowName,windowOpenFeatures ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Frameset//EN' 'http://www.w3.org/TR/html4/frameset.dtd'>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<html>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<head>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<title>Report<\\/title>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<\\/head>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<frameset rows='400,*'>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"  <frame src='/EIDPWebApp/ReportHinweis.html' name='Hinweis'>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"  <frame src='/EIDPWebApp/Dummy.html' name='Report' id='Report'>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<\\/frameset>\" ) ; " ) ;
        printWriter.println( "              nwindow.document.writeln( \"<\\/html>\" ) ; " ) ;
        printWriter.println( "              nwindow.focus() ; " ) ;
        printWriter.println( "              nwindow.document.getElementById('Report').src=url ; " ) ;
        printWriter.println( "              // Fenster schliesst automatisch nach 5 Minuten" ) ;
        printWriter.println( "              nwindow.setTimeout(\"self.close();\", 300000); " ) ;
        printWriter.println( "            } " ) ;
        printWriter.println( "          } else if ( userChanges == 0 ) { " ) ;
        //        printWriter.println( "                      loading = 1 ; " ) ;
        printWriter.println( "            url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
        printWriter.println( "            windowOpenFeatures = \"height=200,width=450,scrollbars=auto\" ; " ) ;
        printWriter.println( "            windowName= \"Report\" ; " ) ;
        printWriter.println( "            var nwindow = window.open( null ,windowName,windowOpenFeatures ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Frameset//EN' 'http://www.w3.org/TR/html4/frameset.dtd'>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<html>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<head>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<title>Report<\\/title>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<\\/head>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<frameset rows='400,*'>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"  <frame src='/EIDPWebApp/ReportHinweis.html' name='Hinweis'>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"  <frame src='/EIDPWebApp/Dummy.html' name='Report' id='Report'>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<\\/frameset>\" ) ; " ) ;
        printWriter.println( "            nwindow.document.writeln( \"<\\/html>\" ) ; " ) ;
        printWriter.println( "            nwindow.focus() ; " ) ;
        printWriter.println( "            nwindow.document.getElementById('Report').src=url ; " ) ;
        printWriter.println( "            // Fenster schliesst automatisch nach 5 Minuten" ) ;
        printWriter.println( "            nwindow.setTimeout(\"self.close();\", 300000); " ) ;
        printWriter.println( "          } else { " ) ;
        printWriter.println( "            noPrimaryMsg = \"" + noPrimaryMsg + "\" " ) ;
        printWriter.println( "            alert( noPrimaryMsg ) ; " ) ;
        printWriter.println( "          } " ) ;
        printWriter.println( "        } else { " ) ;
        printWriter.println( "          url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
        printWriter.println( "          windowOpenFeatures = \"height=300,width=450,scrollbars=auto\" ; " ) ;
        printWriter.println( "          windowName= \"Report\" ; " ) ;
        printWriter.println( "          var nwindow = window.open( null ,windowName,windowOpenFeatures ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Frameset//EN' 'http://www.w3.org/TR/html4/frameset.dtd'>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<html>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<head>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<title>Report<\\/title>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<\\/head>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<frameset rows='150,*'>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"  <frame style='border-style:solid; border-color:#FF8000;' frameborder='no' scrolling='no' src='/EIDPWebApp/ReportExportHinweis.html' name='Hinweis' id='Hinweis'>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"  <frame style='border-style:solid; border-color:#FF8000;' frameborder='no' scrolling='no' src='/EIDPWebApp/Dummy.html' name='Report' id='Report'>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<\\/frameset>\" ) ; " ) ;
        printWriter.println( "          nwindow.document.writeln( \"<\\/html>\" ) ; " ) ;
        printWriter.println( "          nwindow.focus() ; " ) ;
        printWriter.println( "          nwindow.document.getElementById('Report').src=url ; " ) ;
        printWriter.println( "          // Fenster schliesst automatisch nach 5 Minuten" ) ;
        printWriter.println( "          nwindow.setTimeout(\"self.close();\", 300000); " ) ;
        
//        printWriter.println( "                     url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
//        printWriter.println( "                     windowOpenFeatures = \"height=200,width=450,scrollbars=auto\" ; " ) ;
//        printWriter.println( "                     windowName= \"Report\" ; " ) ;
//        printWriter.println( "                     var nwindow = window.open( url ,windowName,windowOpenFeatures ) ; " ) ;
//        printWriter.println( "                     // Fenster schliesst automatisch nach 5 Minuten" ) ;
//        printWriter.println( "                     nwindow.setTimeout(\"self.close();\", 300000); " ) ;
        printWriter.println( "        } " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "    } " ) ;
        printWriter.println( "  // -->" ) ;
        printWriter.println( "  </script>" ) ;
    }
    
    /**
     *
     * @param response
     * @param printWriter
     * @param sidePanelResponseNode
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processSidePanel( HttpServletResponse response , PrintWriter printWriter, NodeList sidePanelResponseNode , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        String noPrimaryMsg = (String)((Vector)uso.xmlWebMenu.getElementsByName( "no-primary-msg" , sidePanelResponseNode )).get( 0 ) ;
        String mandatoryModuleMsg = "";
        try{
            mandatoryModuleMsg = (String)((Vector)uso.xmlWebMenu.getElementsByName( "madatory-module-msg" , sidePanelResponseNode )).get( 0 ) ;
        }catch(Exception e){
            mandatoryModuleMsg = "The data on this page is mandatory.\\nYou have to fill out the form correctly and click on \\\"Submit\\\" first.\\nOr you can choose one of the Buttons at the Top-Panel.";
        }
        // CSS-support
        String stylesheet = this.getCssTheme(uso);
        String bgcolor = new String();
        boolean ifstyle = false;
        if(stylesheet.length() > 0){
            ifstyle = true;
            printWriter.println( "  <link href=\"/EIDPWebApp/stylesheets/" + stylesheet + ".css\" rel=\"stylesheet\" type=\"text/css\">");
        } 
        printWriter.println( "  <style type=\"text/css\" media=\"screen\">" ) ;
        printWriter.println( "  <!--" ) ;
        printWriter.println( "      body, button { font-family:Arial,sans-serif; color:black; font-size:11pt ; } " ) ;
        printWriter.println( "      a:link { text-decoration:none; color:black; } " ) ;
        printWriter.println( "      a:visited { text-decoration:none; color:black; } " ) ;
        printWriter.println( "      a:hover { text-decoration:none; color:yellow; font-weight:bold; } " ) ;
        printWriter.println( "      a:active { text-decoration:none; color:black; } " ) ;
        printWriter.println( "      td { font-size:11pt ; } " ) ;
        printWriter.println( "      div.label { margin-top:1px; font-weight:bold; border-width:thin; border-style:solid; width:150px; text-align:center; vertical-align:middle; cursor:pointer;} " ) ;
        printWriter.println( "      div.content { margin-top: 0px; margin-bottom: 1px; -moz-box-sizing: border-box;	box-sizing: border-box;border:2px solid; width:152px; text-align:center; vertical-align:middle; } " ) ;
        printWriter.println( "      div.single { border-width:thin; border-color:#c0c0c0; border-style:solid; width:150px; text-align:center; vertical-align:middle; cursor:pointer;} " ) ;
        printWriter.println( "      div.group { border-width:thin; border-color:#c0c0c0; border-style:solid; width:146px; text-align:center; vertical-align:middle; cursor:pointer;} " ) ;
        printWriter.println( "  -->" ) ;
        printWriter.println( "  </style> " ) ;
        
        // Import Navi-JavaScript
        printWriter.println( "  <script src=\"/EIDPWebApp/javascript/navi.js\" type=\"text/javascript\"></script>" ) ;        
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        printWriter.println( "  <!--" ) ;
        printWriter.println( "      var loading = 0 ; " ) ;
        printWriter.println( "      function LoadModule( module ) {" ) ;
        printWriter.println( "          userChanges = parent.Data.userChanges ; " ) ;
        printWriter.println( "          mandatoryModule = parent.Data.mandatoryModule ; " ) ;
        printWriter.println( "          if ( loading == 1 ) { " ) ;
        printWriter.println( "              alert( \"Loading another module. Please wait.\" ) ; " ) ;
        printWriter.println( "          } else { " ) ;
        printWriter.println( "              if ( mandatoryModule == 1 ) { " ) ;
        printWriter.println( "                  alert( \"" + mandatoryModuleMsg + "\" ) ; " ) ;
        printWriter.println( "              } else { " ) ;
        printWriter.println( "                  if ( userChanges == 1 ) { " ) ;
        if(isGerman)
            printWriter.println( "                  confirmLoadModule = confirm( \"Sie haben Aenderungen vorgenommen, ohne diese zu speichern!\\nWollen Sie diese Aenderungen verwerfen? \" ) ; " ) ;
        else
            printWriter.println( "                  confirmLoadModule = confirm( \"There have been changes made to your data. Do you want to discard changes? \" ) ; " ) ;
        printWriter.println( "                      if ( confirmLoadModule == true ) { " ) ;
        printWriter.println( "                          loading = 1 ; " ) ;
        printWriter.println( "                          url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
        printWriter.println( "                          parent.Data.location.href=url ;" ) ;
        printWriter.println( "                      } " ) ;
        printWriter.println( "                  } else if ( userChanges == 0 ) { " ) ;
        printWriter.println( "                      loading = 1 ; " ) ;
        printWriter.println( "                      url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
        printWriter.println( "                      parent.Data.location.href=url ;" ) ;
        printWriter.println( "                  } else { " ) ;
        printWriter.println( "                      noPrimaryMsg = \"" + noPrimaryMsg + "\" " ) ;
        printWriter.println( "                      alert( noPrimaryMsg ) ; " ) ;
        printWriter.println( "                  } " ) ;
        printWriter.println( "              } " ) ;
        
//        printWriter.println( "                          if ( confirmLoadModule == true ) { " ) ;
//        printWriter.println( "                              loading = 1 ; " ) ;
//        printWriter.println( "                              url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
//        printWriter.println( "                           parent.Data.location.href=url ;" ) ;
//        printWriter.println( "                          } " ) ;        
//        printWriter.println( "                      loading = 1 ; " ) ;
//        printWriter.println( "                     url = \"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=\" + module ; " ) ;
//        printWriter.println( "                     parent.Data.location.href=url ;" ) ;
//        printWriter.println( "                  }" ) ;
//        printWriter.println( "                      noPrimaryMsg = \"" + noPrimaryMsg + "\" " ) ;
//        printWriter.println( "                      alert( noPrimaryMsg ) ; " ) ;
//        printWriter.println( "                  } " ) ;
        printWriter.println( "          } " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "  // -->" ) ;
        printWriter.println( "  </script>" ) ;
        
        String bgcolor_b = new String();
        if (!ifstyle) {
            bgcolor_b = " bgcolor=\"#c0c0c0\"";
        } else {
            bgcolor_b = "";
        }
        
        printWriter.println( "</head>" ) ;
        printWriter.println("<body id=\"sidepanel\"" + bgcolor_b + ">") ;
        // 1. Get, retrieve and create the panel information fields
        // 1.1 Context label
        String contextLabel = (String)((Vector)uso.xmlWebMenu.getElementsByName( "projectinfo,label" , sidePanelResponseNode )).get( 0 ) ;
        String userInfoLabel = (String)((Vector)uso.xmlWebMenu.getElementsByName( "userinfo,label" , sidePanelResponseNode )).get( 0 ) ;
        // get Primary Key Session Refs
        Vector primaryLabels = new Vector() ;
        primaryLabels = (Vector)uso.xmlWebMenu.getElementsByName( "primaryinfo,label" , sidePanelResponseNode ) ;
        Vector breaks = new Vector() ;
        breaks = (Vector)uso.xmlWebMenu.getElementsByName( "primaryinfo,break" , sidePanelResponseNode ) ;
        Vector primarySessionRefs = new Vector() ;
        primarySessionRefs = (Vector)uso.xmlWebMenu.getElementsByName( "primaryinfo,session-ref" , sidePanelResponseNode ) ;
        // Block-roles
        Vector blockRoles = (Vector)uso.xmlWebMenu.getElementsByName( "primaryinfo,block-role" , sidePanelResponseNode) ;
        boolean blockField = false ;
        for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
            if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                blockField = true ;
            }
        }
        String primaryInfo = "" ;
        if ( blockField == true ) {
            primaryInfo += "xxx" ;
        } else {
            for ( int sr=0 ; sr < primarySessionRefs.size() ; sr++ ) {
                if ( sr > 0 ) {
                    primaryInfo += " " ;
                }
                String strPrimaryInfo = (String)uso.eidpWebAppCache.sessionData_get( (String)primarySessionRefs.get( sr ) ) ;
                if ( strPrimaryInfo == null ) {
                    primaryInfo = "none selected" ;
                    break;
                }
                primaryInfo += strPrimaryInfo;
            }
        }
        
        HashMap hmPrimaryInfo = new HashMap();
        HashMap hmBreaks = new HashMap();
        for ( int pl=0 ; pl < primaryLabels.size() ; pl++ ) {
            hmPrimaryInfo.put( (String)primaryLabels.get( pl ) , (String)uso.eidpWebAppCache.sessionData_get( (String)primarySessionRefs.get( pl ) ) );
            if( breaks.size() > 0 ){
                hmBreaks.put( String.valueOf( pl ) , (String)breaks.get( pl ) ) ;
            }
        }
        Set setPrimaryInfoKeySet = hmPrimaryInfo.keySet();
        Iterator iterPrimaryInfo = setPrimaryInfoKeySet.iterator();
        
        String bgcolor_a = new String();
        if (!ifstyle) {
            bgcolor_a = " bgcolor=\"#a0c0c0\"";
        } else {
            bgcolor_a = "";
        }
        // Create the SidePanel Info Entries:
        printWriter.println( "  <table border=\"0\" width=\"154px\">" ) ;
        printWriter.println( "    <tr class=\"even\"" + bgcolor_a + ">" ) ;
        printWriter.println( "      <td width=\"154px\">" ) ;
        printWriter.println( "        <font size=\"+1\"><b>" + contextLabel + "</b></font>" ) ;
        printWriter.println( "      </td>" ) ;
        printWriter.println( "    </tr> " ) ;
        printWriter.println( "    <tr>" ) ;
        printWriter.print( "      <td width=\"154px\">" ) ;
        printWriter.print( userInfoLabel + ": " + (String)uso.eidpWebAppCache.sessionData_get( "userID" ) + "<br>" ) ;
        printWriter.print( "Login: " + (String)uso.eidpWebAppCache.sessionData_get( "userLogin" ) ) ;
        printWriter.println( "</td>" ) ;
        printWriter.println( "    </tr>" ) ;
        if ( primaryInfo.equals( "none selected" ) || primaryInfo.equals( "xxx" ) || primaryLabels.size() == 1 ) {
            printWriter.println( "    <tr class=\"even\"" + bgcolor_a + ">" ) ;
            printWriter.println( "      <td width=\"154px\">" + (String)iterPrimaryInfo.next() + 
                    ":<br><span style=\"font-weight:bold;\">" + primaryInfo + "</span></td>" ) ;
            printWriter.println( "    </tr> " ) ;
        } else {
            int intCounter = 0;
            
            while(iterPrimaryInfo.hasNext()){
                String strLabel = (String)iterPrimaryInfo.next();
                printWriter.println( "    <tr class=\"even\"" + bgcolor_a + ">" ) ;
                printWriter.println( "      <td width=\"154px\">" + strLabel + ": " ) ;
                if ( breaks.size() > 0 ){
                    if ( ( (String)hmBreaks.get( String.valueOf( intCounter ) ) ).equals("true") ){
                        printWriter.print( "<br>" ) ;
                    }
                } else {
                    printWriter.print( "<br>" ) ;
                }
                printWriter.println( "          <span style=\"font-weight:bold;\">" + 
                        (String)hmPrimaryInfo.get( strLabel ) + "</span>") ;
                printWriter.println( "          </td></tr> " ) ;
                intCounter++;
            }
        }
        // 2. Get and create the SidePanel Entries
        printWriter.println( "    <tr> " ) ;
        printWriter.println( "      <td width=\"154px\">Menu:</td>" ) ;
        printWriter.println( "    </tr> " ) ;
        printWriter.println( "  </table> " ) ;
        Vector sidePanelGroupEntryResponseVector = new Vector() ;
        sidePanelGroupEntryResponseVector = uso.xmlWebMenu.getNodeListsByName( "group" , sidePanelResponseNode ) ;
        Vector sidePanelEntryResponseVector = new Vector() ;
        sidePanelEntryResponseVector = uso.xmlWebMenu.getNodeListsByName( "entry" , sidePanelResponseNode ) ;
        if( sidePanelGroupEntryResponseVector.size() == 0 ){
            processSidePanelEntrys( printWriter , sidePanelEntryResponseVector , uso , "single" ) ;
        } else {
            for(int ni = 0 ; ni < sidePanelResponseNode.getLength() ; ni++ ){
                Node nextNode = sidePanelResponseNode.item( ni ) ;
                if( nextNode.getNodeName().equals("entry") ){
                    processSidePanelEntrys( printWriter , (NodeList)nextNode , uso ) ;
                }if( nextNode.getNodeName().equals("group") ){
                    processSidePanelGroupEntrys( printWriter , (NodeList)nextNode , uso ) ;
                }
            }
        }
        
        
    }
    
    protected void processSidePanelGroupEntrys( PrintWriter printWriter , NodeList sidePanelGroupEntryResponseVector , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        // 1. check for rolePermissions
        Vector rolePermissions = new Vector() ;
        rolePermissions = (Vector)uso.xmlWebMenu.getElementsByName( "group-role-name" , sidePanelGroupEntryResponseVector ) ;
        for ( int rp = 0 ; rp < rolePermissions.size() ; rp++ ) {
            if ( uso.eidpWebAppCache.userRoles_contains( (String)rolePermissions.get( rp ) ) || rolePermissions.get( rp ).equals( "ALL" ) ) {
                Vector sidePanelEntryResponseVector = new Vector() ;
                sidePanelEntryResponseVector = (Vector)uso.xmlWebMenu.getNodeListsByName( "entry" , sidePanelGroupEntryResponseVector ) ;
                String sessionModuleString = (String)uso.eidpWebAppCache.sidePanelEntry_get() ;
                String groupStatus = "closed" ;
                String groupLabel = (String)uso.xmlWebMenu.getElementsByName( "group-label" , sidePanelGroupEntryResponseVector ).get( 0 ) ;
                String groupID = "";
                String sessionGroupID = (String)uso.eidpWebAppCache.sessionData_get( "groupID" ) ;
                
                try{
                    groupID = (String)uso.xmlWebMenu.getElementsByName( "group-id" , sidePanelGroupEntryResponseVector ).get( 0 ) ;
                }catch(java.lang.ArrayIndexOutOfBoundsException e){
                    
                }
                if(!groupID.equals("")){
                    if( groupID.equals(sessionGroupID) ){
                        groupStatus = "open" ;
                    }
                }
                String groupLabelColor = (String)uso.xmlWebMenu.getElementsByName( "group-label-color" , sidePanelGroupEntryResponseVector ).get( 0 ) ;
                String groupColor = (String)uso.xmlWebMenu.getElementsByName( "group-color" , sidePanelGroupEntryResponseVector ).get( 0 ) ;
                if( groupStatus.equals("open") ){
                    printWriter.println( "<div class=\"label\" style=\"border-color:#" + groupColor + "; background-color:#" + groupColor + "; color:#" + groupLabelColor + ";\" TITLE=\"Men&uuml; schliessen\">" + groupLabel + "</div>" ) ;
                    printWriter.println( "<div class=\"content\" id=\"open\" style=\"border-color:#" + groupColor + "; background-color:#" + groupColor + ";\">" ) ;
                }else{
                    printWriter.println( "<div class=\"label\" style=\"border-color:#" + groupColor + "; background-color:#" + groupColor + "; color:#" + groupLabelColor + ";\" TITLE=\"Men&uuml; erweitern\">" + groupLabel + "</div>" ) ;
                    printWriter.println( "<div class=\"content\" id=\"closed\" style=\"border-color:#" + groupColor + "; background-color:#" + groupColor + ";\">" ) ;
                }
                processSidePanelEntrys( printWriter , sidePanelEntryResponseVector , uso , "group" ) ;
                printWriter.println( "</div>" );
                break ;
            }
        }
    }
    
    protected void processSidePanelEntrys( PrintWriter printWriter , NodeList sidePanelEntryResponseVector , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        // 1. check for rolePermissions
        Vector rolePermissions = new Vector() ;
        rolePermissions = (Vector)uso.xmlWebMenu.getElementsByName( "role-name" , sidePanelEntryResponseVector ) ;
        for ( int rp = 0 ; rp < rolePermissions.size() ; rp++ ) {
            if ( uso.eidpWebAppCache.userRoles_contains( (String)rolePermissions.get( rp ) ) || rolePermissions.get( rp ).equals( "ALL" ) ) {
                String entryLabel = (String)uso.xmlWebMenu.getElementsByName( "label" , sidePanelEntryResponseVector ).get( 0 ) ;
                String entryModule = "";
                try {
                    entryModule = (String)uso.xmlWebMenu.getElementsByName( "module" , sidePanelEntryResponseVector ).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                }
//                if ( entryModule.equals( "" ) ) {
//                }
                String sessionModuleString = "" ;
                if ( uso.eidpWebAppCache.sidePanelEntry_exists() ) {
                    sessionModuleString = (String)uso.eidpWebAppCache.sidePanelEntry_get() ;
                    // uso.session.removeAttribute( "sidePanelEntry" ) ;
                }
                String entryColor = "" ;
                try {
                    entryColor = (String)uso.xmlWebMenu.getElementsByName( "color" , sidePanelEntryResponseVector ).get( 0 ) ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                    entryColor = "" ;
                }
                
                if ( sessionModuleString.equals( entryModule ) ) {
                    printWriter.println( "  <div class=\"single\" style=\"background-color:#d2d2d2;\"><a href=\"javascript:LoadModule( '" + entryModule + ";show' );\">" + entryLabel + "</a></div>" ) ;
                } else {
                    if ( entryColor.equals( "" ) ) {
                        entryColor = "a2a2a2" ;
                    }
                    if (entryModule.equals("")) {
                        printWriter.println( "  <div class=\"single\" style=\"background-color:#" + entryColor + ";\"><a href=\"#\">" + entryLabel + "</a></div>" ) ;
                    } else {
                        printWriter.println( "  <div class=\"single\" style=\"background-color:#" + entryColor + ";\"><a href=\"javascript:LoadModule( '" + entryModule + ";show' ); \">" + entryLabel + "</a></div>" ) ;
                    }
                }
                break ;
            }
        }
    }
    
    protected void processSidePanelEntrys( PrintWriter printWriter , Vector sidePanelEntryResponseVector , UserScopeObject uso , String strDivClass ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        // 1. check for rolePermissions
        for ( int si = 0 ; si < sidePanelEntryResponseVector.size() ; si++ ) {
            Vector rolePermissions = new Vector() ;
            rolePermissions = (Vector)uso.xmlWebMenu.getElementsByName( "role-name" , (NodeList)sidePanelEntryResponseVector.get( si ) ) ;
            for ( int rp = 0 ; rp < rolePermissions.size() ; rp++ ) {
                if ( uso.eidpWebAppCache.userRoles_contains( (String)rolePermissions.get( rp ) ) || rolePermissions.get( rp ).equals( "ALL" ) ) {
                    String entryLabel = (String)((Vector)uso.xmlWebMenu.getElementsByName( "label" , (NodeList)sidePanelEntryResponseVector.get( si ) )).get( 0 ) ;
                    String entryModule = "";
                    try {
                        entryModule = (String)((Vector)uso.xmlWebMenu.getElementsByName( "module" , (NodeList)sidePanelEntryResponseVector.get( si ) )).get( 0 ) ;
                    } catch (java.lang.ArrayIndexOutOfBoundsException aiobe) {
                    }
//                    if ( entryModule.equals( "" ) ) {
//                    }
                    String sessionModuleString = "" ;
                    if ( uso.eidpWebAppCache.sidePanelEntry_exists() ) {
                        sessionModuleString = (String)uso.eidpWebAppCache.sidePanelEntry_get() ;
                        // uso.session.removeAttribute( "sidePanelEntry" ) ;
                    }
                    String entryColor = "" ;
                    try {
                        entryColor = (String)((Vector)uso.xmlWebMenu.getElementsByName( "color" , (NodeList)sidePanelEntryResponseVector.get( si ) )).get( 0 ) ;
                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                        entryColor = "" ;
                    }
                    String groupID = (String)uso.eidpWebAppCache.sessionData_get( "groupID" ) ;
                    if( groupID != null && !groupID.equals("") ){
                        sessionModuleString =  sessionModuleString + ";" + groupID;
                    }
                    
                    if ( sessionModuleString.equals( entryModule ) ) {
                        printWriter.println( "  <div class=\"" + strDivClass + "\" style=\"background-color:#d2d2d2;\"><a href=\"javascript:LoadModule( '" + entryModule + ";show' ); \">" + entryLabel + "</a></div>" ) ;
                    } else {
                        if ( entryColor.equals( "" ) ) {
                            entryColor = "a2a2a2" ;
                        }
                        if (entryModule.equals("")) {
                            printWriter.println( "  <div class=\"" + strDivClass + "\" style=\"background-color:#" + entryColor + ";\"><a href=\"#\">" + entryLabel + "</a></div>" ) ;
                        } else {
                            printWriter.println( "  <div class=\"" + strDivClass + "\" style=\"background-color:#" + entryColor + ";\"><a href=\"javascript:LoadModule( '" + entryModule + ";show' ); \">" + entryLabel + "</a></div>" ) ;
                        }
                    }
                    break ;
                }
            }
        }
    }
    
    /**
     *
     * @param response
     * @param printWriter
     * @param sponsorsResponseVector
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processSponsors( HttpServletResponse response , PrintWriter printWriter , Vector sponsorsResponseVector , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {

        String stylesheet = getCssTheme(uso);
        String bgcolor = new String();
        if(stylesheet.length() > 0){
            bgcolor = "";
            printWriter.println("<link href=\"/EIDPWebApp/stylesheets/" + stylesheet + ".css\" rel=\"stylesheet\" type=\"text/css\">");
        } else {
            bgcolor = " bgcolor=\"#c0c0c0\"";
        }
        
        printWriter.println( "<style type=\"text/css\" media=\"screen\">" ) ;
        printWriter.println( "<!--" ) ;
        printWriter.println( "body { margin: 2px 0px 2px 0px; font-family:Arial,sans-serif; color:black; } " ) ;
        printWriter.println( "button { font-family:Arial,sans-serif; color:black; } " ) ;
        printWriter.println( "a:link { text-decoration:none; color:black; } " ) ;
        printWriter.println( "a:visited { text-decoration:none; color:black; } " ) ;
        printWriter.println( "a:hover { text-decoration:none; color:yellow; } " ) ;
        printWriter.println( "a:active { text-decoration:none; color:black; } " ) ;
        printWriter.println( "-->" ) ;
        printWriter.println( "</style> " ) ;
        printWriter.println( "</head><body id=\"sponsors\"" + bgcolor + ">" ) ; // marginwidth=\"0\" marginheight=\"2\" topmargin=\"2\" leftmargin=\"0\"> " ) ;
        printWriter.println( "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" ><tr align=\"center\" valign=\"middle\">" ) ;
        // printWriter.println( "<td align=\"center\" valign=\"center\" width=\"50\"><img src=\"/EIDPWebApp/images/eidp.jpg\" height=\"35\" width=\"145\" title=\"EIDP - Customized Middleware and Consulting\" border=\"0\"></td> " ) ;
        // get Sponsor's logos and data for them and write the sponsor's area
        printWriter.println( "<td align=\"center\" valign=\"middle\"> " ) ;
        String imagePosition = "left" ;
        String sponsorsString = "" ;
        for ( int i = 0 ; i < sponsorsResponseVector.size() ; i++ ) {
            String sponsorsLabel = "" ;
            String sponsorsImage = "" ;
            String sponsorsHeight = "" ;
            sponsorsLabel = (String)((Vector)uso.xmlWebMenu.getElementsByName( "label" , (NodeList)sponsorsResponseVector.get( i ) )).get( 0 ) ;
            sponsorsImage = (String)((Vector)uso.xmlWebMenu.getElementsByName( "image" , (NodeList)sponsorsResponseVector.get( i ) )).get( 0 ) ;
            sponsorsHeight = (String)((Vector)uso.xmlWebMenu.getElementsByName( "height" , (NodeList)sponsorsResponseVector.get( i ) )).get( 0 ) ;
            sponsorsString += ( "&nbsp;<img src=\"/EIDPWebApp/images/" + sponsorsImage + "\" height=\"" + sponsorsHeight + "\" title=\"" + sponsorsLabel + "\" border=\"0\" alt=\"" + sponsorsLabel + "\"> " ) ;
        }
        printWriter.println( sponsorsString ) ;
        printWriter.println( "</td></tr></table>" ) ;
    }
    
    /**
     *
     * @param response
     * @param printWriter
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void createSponsors( HttpServletResponse response , PrintWriter printWriter , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/webmenu.xml" ;
        try {
            uso.xmlWebMenu = new XMLDataAccess( xmlfile ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException pae ) {
            throw new javax.servlet.ServletException( "CreateSponsors could not access webmenu.xml: " + pae ) ;
        }
        Vector sponsorsResponseVector = new Vector() ;
        sponsorsResponseVector = uso.xmlWebMenu.getNodeListsByName( "sponsors,entry" ) ;
        printWriter.println( "<td align=\"center\" valign=\"center\"> " ) ;
        printWriter.println( "<br><br><br><br><h2>This Project is supported by:</h2> " ) ;
        printWriter.println( "<br><br> " ) ;
        String imagePosition = "left" ;
        int count = 0 ;
        for ( int i = 0 ; i < sponsorsResponseVector.size() ; i++ ) {
            count++ ;
            String sponsorsLabel = "" ;
            String sponsorsImage = "" ;
            String sponsorsHeight = "" ;
            sponsorsLabel = (String)((Vector)uso.xmlWebMenu.getElementsByName( "label" , (NodeList)sponsorsResponseVector.get( i ) )).get( 0 ) ;
            sponsorsImage = (String)((Vector)uso.xmlWebMenu.getElementsByName( "image" , (NodeList)sponsorsResponseVector.get( i ) )).get( 0 ) ;
            sponsorsHeight = (String)((Vector)uso.xmlWebMenu.getElementsByName( "height" , (NodeList)sponsorsResponseVector.get( i ) )).get( 0 ) ;
            printWriter.println( "<img src=\"/EIDPWebApp/images/" + sponsorsImage + "\" height=\"" + sponsorsHeight + "\" title=\"" + sponsorsLabel + "\" border=\"0\" >&nbsp; " ) ;
            if ( count > 4 ) {
                printWriter.println( "<br><br> " ) ;
                count = 0 ;
            }
        }
        printWriter.println( "</td></tr></table>" ) ;
    }
    
    /**
     *
     * @param response
     * @param printWriter
     * @param sponsorsResponseVector
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processSponsorsPage( HttpServletResponse response , PrintWriter printWriter, Vector sponsorsResponseVector , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        this.initStyleHTML( printWriter ) ;
        printWriter.println( "</head><body> " ) ;
        printWriter.println( " <div align=\"center\" > " ) ;
        this.createSponsors( response , printWriter , uso ) ;
        printWriter.println( " </div> " ) ;
    }
    
    /**
     *
     * @param response
     * @param printWriter
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processEntryPage( HttpServletResponse response , PrintWriter printWriter , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        String MetaInformationContext = (String)((Vector)uso.xmlDataAccess.getElementsByName( "meta-information,name" ) ).get( 0 ) ;
        String MetaInformationInstitution = (String)((Vector)uso.xmlDataAccess.getElementsByName( "meta-information,institution" ) ).get( 0 ) ;
        String MetaInformationDepartment = (String)((Vector)uso.xmlDataAccess.getElementsByName( "meta-information,department" ) ).get( 0 ) ;
        
        String autologoutinterval = (String)uso.eidpWebAppCache.sessionData_get( "auto-logout-interval" );
        if(autologoutinterval != null && !autologoutinterval.equals("")){
            printWriter.println( "<script language=\"JavaScript\"> " ) ;
            printWriter.println( "  function autoLogout(){" ) ;
            printWriter.println( "      var url=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;logout;show&autologout=true\";" ) ;
            printWriter.println( "      top.location.href=url;" ) ;
            printWriter.println( "  }" ) ;
            // Auto-logout nach der in der application.xml definierten Anzahl von Minuten
            printWriter.println( "  window.setTimeout(\"autoLogout()\", " + autologoutinterval + "*60000);" ) ;
            printWriter.println( "</script> " ) ;
        }
        //<editor-fold defaultstate="collapsed" desc="onerror replacement">
        String applicationLogo = new String();
        if (!uso.xmlApplication.getElementsByName("application-data,app-logo").isEmpty()) {
            applicationLogo = (String)((Vector)uso.xmlApplication.getElementsByName( "application-data,app-logo" )).get( 0 ) ;
        } else {
            applicationLogo = "eidp_logo.jpg";
        }
        //</editor-fold>
        printWriter.println( "</head>" ) ;
        printWriter.println( "<body>") ;
        printWriter.println( " <div align=\"center\"> " ) ;
        printWriter.println( " <br><br> " ) ;
        //printWriter.println( "  <img src=\"/EIDPWebApp/images/" + uso.applicationContext + ".jpg\" onerror=\"this.src='/EIDPWebApp/images/eidp_logo.jpg'\" border=\"0\" alt=\"eidp_logo\"> " ) ;
        printWriter.println( "  <img id=\"logo\" src=\"/EIDPWebApp/images/" + applicationLogo + "\" border=\"0\" alt=\"eidp_logo\">");
        printWriter.println( "<br><br>" ) ;
        printWriter.println( "<h1>Welcome to: " + MetaInformationContext + "</h1> " ) ;
        printWriter.println( "<b>Provided by: " + MetaInformationInstitution + ", " + MetaInformationDepartment + "</b><br> " ) ;
    }
    
    /**
     *
     * @param request
     * @param response
     * @param printWriter
     * @param passwordErrorFlag
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void writePasswordModule( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , boolean passwordErrorFlag ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        printWriter.println( "<body><b>Your Password has expired - set a new one:</b> " ) ;
        printWriter.println( "<hr> " ) ;
        if ( passwordErrorFlag == true ) {
            printWriter.println( "<b>You typed in an incorrect password. Please try again.</b><br>" ) ;
        }
        printWriter.println( "<script language=\"JavaScript\"> " ) ;
        printWriter.println( "      function PasswordTransaction() { " ) ;
        printWriter.println( "          PasswordTransactionError = 0 ; " ) ;
        printWriter.println( "          PWDTMsg = \"Exception thrown during password check:\\n\" ; " ) ;
        printWriter.println( "          if ( document.PASSWORD.newpwd01.value != document.PASSWORD.newpwd02.value ) { PasswordTransactionError = 1 ; PWDTMsg += \"The NEW Passwords do not match.\\n\" ; } " ) ;
        printWriter.println( "          if ( document.PASSWORD.newpwd01.value == \"\" ) { PasswordTransactionError = 1 ; PWDTMsg += \"Empty passwords are not allowed.\\n\" ; } " ) ;
        printWriter.println( "          if ( document.PASSWORD.newpwd01.value.length < 6 ) { PasswordTransactionError = 1 ; PWDTMsg += \"The password is too short (min. 6 characters).\\n\" } ; " ) ;
        printWriter.println( "          if ( document.PASSWORD.oldpwd.value == document.PASSWORD.newpwd01.value ) { PasswordTransactionError = 1 ; PWDTMsg += \"Old and New passwords are equal.\\n\" ; } " ) ;
        // ===> RegExp:
        // Whitespaces:
        printWriter.println( "          if ( document.PASSWORD.newpwd01.value.search( /\\s/ ) > -1 ) { PasswordTransactionError = 1 ; PWDTMsg += \"Password contains whitespace characters.\\n\" ; } " ) ;
        // lower Characters:
        printWriter.println( "          if ( document.PASSWORD.newpwd01.value.search( /[a-z]/ ) == -1 ) { PasswordTransactionError = 1 ; PWDTMsg += \"Password contains no lower characters.\\n\" ; } " ) ;
        // upper Characters:
        printWriter.println( "          if ( document.PASSWORD.newpwd01.value.search( /[A-Z]/ ) == -1 ) { PasswordTransactionError = 1 ; PWDTMsg += \"Password contains no upper characters.\\n\" ; } " ) ;
        // numbers:
        printWriter.println( "          if ( document.PASSWORD.newpwd01.value.search( /\\d/ ) == -1 ) { PasswordTransactionError = 1 ; PWDTMsg += \"Password contains no numbers.\\n\" ; } " ) ;
        printWriter.println( "          if ( PasswordTransactionError == 1 ) { alert( PWDTMsg ) ; } " ) ;
        printWriter.println( "          else { document.PASSWORD.submit() ; } " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "</script> " ) ;
        printWriter.println( " <div align=\"center\" > " ) ;
        printWriter.println( " <form name=\"PASSWORD\" method\"POST\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller\" > " ) ;
        printWriter.println( "  <input type=\"hidden\" name=\"module\" value=\"Function;PasswordModule;store\"> " ) ;
        printWriter.println( " <table border=\"0\"> " ) ;
        printWriter.println( "  <tr><td colspan=\"2\"><hr></td></tr>" ) ;
        printWriter.println( "  <tr><td>Old Password</td><td><input type=\"password\" name=\"oldpwd\" size=\"15\" ></td></tr>" ) ;
        printWriter.println( "  <tr><td>New Password</td><td><input type=\"password\" name=\"newpwd01\" size=\"15\" ></td></tr>" ) ;
        printWriter.println( "  <tr><td>Repeat new Password</td><td><input type=\"password\" name=\"newpwd02\" size=\"15\" ></td></tr>" ) ;
        printWriter.println( "  <tr> <td class=\"hint\" colspan=\"2\">Hint: Please type in the old password correctly (small vs. capital letters).</td> </tr> " ) ;
        printWriter.println( "  <tr> <td class=\"hint\" colspan=\"2\">The new password has to consist of a minimum of 6 characters.</td></tr> " ) ;
        printWriter.println( "  <tr> <td class=\"hint\" colspan=\"2\">Each password has to contain small, capital letters and numbers.</td></tr> " ) ;
        printWriter.println( " </table> " ) ;
        printWriter.println( "  <input type=\"button\" value=\"Submit Password\" onClick=\"javascript:PasswordTransaction();\">&nbsp;<input type=\"reset\" value=\"Clear\" > " ) ;
        printWriter.println( " </div> " ) ;
        printWriter.println( " </body> " ) ;
    }
    
    /**
     *
     * @param request
     * @param response
     * @param printWriter
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws Exception
     */
    protected void processPasswordModule( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException , java.lang.Exception {
        if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "show" ) ) {
            this.writePasswordModule( request , response , printWriter , false ) ;
        } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "store" ) ) {
            // 1.1 get old password to SHA1-hash (BASE64Encoded)
            String formPassword = (String)request.getParameter( "oldpwd" ) ;
            // 1.2 encode new password to SHA-1 hash( BASE64Encoded )
            String formNewPWD = (String)request.getParameter( "newpwd01" ) ;
            String encryptedNewPWD = new String( encrypt( formNewPWD ) ) ;
            encryptedNewPWD = javax.xml.bind.DatatypeConverter.printBase64Binary(encryptedNewPWD.getBytes());
            // 2. get Password from database
            HashMap paramMap = new HashMap() ;
            paramMap.put( "login" , uso.userLogin ) ;
            uso.dbMapper.DBAction( "USERS" , "getUserDataForLogin" , paramMap ) ;
            String dbPassword = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( "password" ) ;
            if ( ! dbPassword.equals( "START_PASSWORD" ) ) {
                //                userInputPassword = new String( encrypt( eidpPassword ) ) ;
                //                    BASE64Encoder encoder = new BASE64Encoder() ;
                //                    userInputPassword = new String( encoder.encode( userInputPassword.getBytes() ).toString() ) ;
                formPassword = new String( encrypt( formPassword ) ) ;
                // 2.1 decode DB-PWD
                //                BASE64Decoder decoder = new BASE64Decoder() ;
                //                dbPassword = new String( decoder.decodeBuffer( dbPassword ) ) ;
                formPassword = javax.xml.bind.DatatypeConverter.printBase64Binary(formPassword.getBytes());
            }
            // 3. compare password to db-password
            if ( dbPassword.equals( formPassword ) ) {
                // 4. store password in db and exit this window
                paramMap.clear() ;
                paramMap.put( "id" , uso.userID ) ;
                paramMap.put( "password" , encryptedNewPWD ) ;
                Date timestamp = new Date() ;
                String timestampString = String.valueOf( timestamp.getTime() ) ;
                paramMap.put( "modify_timestamp" , timestampString ) ;
                uso.dbMapper.DBAction( "USERS" , "setPassword" , paramMap ) ;
                printWriter.println( " <html> " ) ;
                printWriter.println( "  <head></head><body> " ) ;
                printWriter.println( " <script language=\"JavaScript\"> " ) ;
                printWriter.println( " window.close() ; </script></body>" ) ;
                printWriter.println( " </html>" ) ;
            } else {
                // Call writePasswordModule with PasswordError flag:
                this.writePasswordModule( request , response , printWriter , true ) ;
            }
        }
    }
    
    protected void processHelpWizzardModule( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException , java.lang.Exception {
        String searchtext = (String)request.getParameter( "searchtext" ) ;
        HashMap paramMap = new HashMap() ;
        paramMap.put("kurz", searchtext);
        uso.dbMapper.DBAction( "HELP" , "getHelp" , paramMap ) ;
        Vector allRowsOfHelp = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;
        printWriter.println( "<body><b>Eintr&auml;ge f&uuml;r \"" + searchtext + "\":</b> " ) ;
        printWriter.println( "<hr> " ) ;
        
        printWriter.println( " <div align=\"left\" >" ) ;
        printWriter.println( " <table border=\"0\"> " ) ;
        if(allRowsOfHelp.size()==0){
            printWriter.println( "  <tr><td>Leider konnten keine Eintr&auml;ge gefunden werden...</td></tr>" ) ;
        }else{
            for(int i=0 ; i<allRowsOfHelp.size() ; i++){
                String strKurzBez = (String)((HashMap)allRowsOfHelp.get( i )).get( "beschreibung" ) ;
                printWriter.println( "  <tr><td>" + (i+1) + ".&nbsp;<a href=\"#eintrag_" + i + "\">" + strKurzBez + "</a></td></tr>" ) ;
            }
        }
        printWriter.println( " </table> " ) ;
        printWriter.println( " </div> " ) ;
        
        if(allRowsOfHelp.size() > 0){
            printWriter.println( "<hr> " ) ;
            
            printWriter.println( " <div align=\"left\">" ) ;
            printWriter.println( " <table border=\"1\"> " ) ;
            
            for(int i=0 ; i<allRowsOfHelp.size() ; i++){
                String strKurzBez = (String)((HashMap)allRowsOfHelp.get( i )).get( "text" ) ;
                printWriter.println( "  <tr><td>" + (i+1) + ".&nbsp;<a name=\"eintrag_" + i + "\">" + strKurzBez + "</a></td></tr>" ) ;
            }
            
            printWriter.println( " </table> " ) ;
        }
        printWriter.println( " </div> " ) ;
        printWriter.println( " </body> " ) ;
    }
    
    /**
     *
     * @param request
     * @param response
     * @param printWriter
     * @param findNode
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processFindFunction( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , NodeList findNode , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "show" ) ) {
            String label = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label" , findNode ) ).get( 0 ) ;
            String dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , findNode ) ).get( 0 ) ;
            String method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , findNode ) ).get( 0 ) ;
            String centerID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "center-id" , findNode ) ).get( 0 ) ;
            String selectField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "select-field" , findNode ) ).get( 0 ) ;
            String publicSelectField = "" ;
            try {
                publicSelectField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-select-field" , findNode ) ).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                publicSelectField = "" ;
            }
            
            Vector fieldNodes = (Vector)uso.xmlDataAccess.getNodeListsByName( "field" , findNode ) ;
            
            //<editor-fold defaultstate="collapsed" desc="SortBy Methods // David">
            boolean sortBy = false;
            try {
                String sortEnabled = (String)((Vector)uso.xmlDataAccess.getElementsByName( "sort-enabled" , findNode ) ).get( 0 ) ;
                if (sortEnabled.equals("true")) {
                    printWriter.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/EIDPWebApp/stylesheets/find.css\">");
                    sortBy = true;
                }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                sortBy = false;
            }
            
            String sortByField = new String();
            int sortID = -1;
            try {
                sortByField = request.getParameter("sortByField");
                if (sortByField != null || sortByField.length() > 0) {
                    sortID = Integer.parseInt(sortByField);
                    method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "sort-method" , (NodeList)fieldNodes.get( sortID )) ).get( 0 ) ;
                } 
            } catch ( java.lang.ArrayIndexOutOfBoundsException e ){
            } catch ( java.lang.NumberFormatException f) {
            } catch ( java.lang.NullPointerException g) {
            }
            //</editor-fold>
                  
            HashMap paramMap = new HashMap() ;
            String cR = "" ;
            HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
            Object [] centerRoles = ((Set)cRoles.keySet()).toArray() ;
            for ( int ci = 0 ; ci < centerRoles.length ; ci++ ) {
                if ( ci > 0 ) { cR += " , " ; }
                cR += (String)centerRoles[ci] ;
            }
            paramMap.put( centerID , cR ) ;
            paramMap.put( "application" , uso.applicationContext ) ;
            uso.dbMapper.DBAction( dataset , method , paramMap ) ;
            printWriter.println( "<style type=\"text/css\"> " ) ;
            printWriter.println( "<!-- " ) ;
            printWriter.println( " body { font-family:Arial,sans-serif; color:black;font-size:11pt ; } " ) ;
            printWriter.println( "  a:link { text-decoration:none; color:black; } " ) ;
            printWriter.println( "  a:visited { text-decoration:none; color:black ; } " ) ;
            printWriter.println( "  a:hover { text-decoration:none; color:yellow ; } " ) ;
            printWriter.println( "  a:active { text-decoration:none; color:black ; } " ) ;
            printWriter.println( "  td { color:#000000 ; font-size:11pt ; } " ) ;
            printWriter.println( "  tr.header { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
            printWriter.println( "  td.input { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;
            printWriter.println( "  td.data { background-color:#EEEEEE;color:#000000 ; font-size:11pt ; } " ) ;
            printWriter.println( "--> </style> " ) ;
            printWriter.println( "  <script language=\"JavaScript\"> " ) ;
            String autologoutinterval = (String)uso.eidpWebAppCache.sessionData_get( "auto-logout-interval" );
            if(autologoutinterval != null && !autologoutinterval.equals("")){
                printWriter.println( "  function autoLogout(){" ) ;
                printWriter.println( "      var url=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;logout;show&autologout=true\";" ) ;
                printWriter.println( "      top.location.href=url;" ) ;
                printWriter.println( "  }" ) ;
                // Auto-logout nach der in der application.xml definierten Anzahl von Minuten
                printWriter.println( "  window.setTimeout(\"autoLogout()\", " + autologoutinterval + "*60000);" ) ;
            }
            printWriter.println( "      linkClicked = 0 ; " ) ;
            printWriter.println( "      function markRow(rowid){ " ) ;
//            printWriter.println( "          if (document.all){ " ) ;
//            printWriter.println( "              document.all.rowid.style.setAttribute('background-color','#FFFFFF','false'); " ) ;
//            printWriter.println( "              document.all.rowid.style.setAttribute('font-weight','bold','false'); " ) ;
//            printWriter.println( "          }else{ " ) ;
            printWriter.println( "              document.getElementById(rowid).style.backgroundColor='#FFFFFF'; " ) ;
            printWriter.println( "              document.getElementById(rowid).style.fontWeight='bold'; " ) ;
//            printWriter.println( "          } " ) ;
            printWriter.println( "      } " ) ;
            printWriter.println( "      function demarkRow(rowid){ " ) ;
//            printWriter.println( "          if (document.all){ " ) ;
//            printWriter.println( "              document.all.rowid.style.setAttribute('background-color','#EEEEEE','false'); " ) ;
//            printWriter.println( "              document.all.rowid.style.setAttribute('font-weight','normal','false'); " ) ;
//            printWriter.println( "          }else{ " ) ;
            printWriter.println( "              document.getElementById(rowid).style.backgroundColor='#EEEEEE'; " ) ;
            printWriter.println( "              document.getElementById(rowid).style.fontWeight='normal'; " ) ;
//            printWriter.println( "          } " ) ;
            printWriter.println( "      } " ) ;
            printWriter.println( "      function getPatient(patid){ " ) ;
            String strAskForVisit = (String)uso.eidpWebAppCache.sessionData_get( "AskForVisit" ) ;
            if(strAskForVisit != null && strAskForVisit.equals("yes")){
                if(isGerman)
                    printWriter.println( "          Check = confirm('Wollen Sie gleich eine neue Visite f�r den Patienten anlegen?'); " ) ;
                else
                    printWriter.println( "          Check = confirm('Do you want to create a new visit for the patient?'); " ) ;
                printWriter.println( "          if(Check==false) " ) ;
                printWriter.println( "              parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;Find;store&selectValue=' + patid; " ) ;
                printWriter.println( "          else " ) ;
                printWriter.println( "              parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;Find;store&selectValue=' + patid + '&newVisit=1'; " ) ;
            }else{
                printWriter.println( "          parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;Find;store&selectValue=' + patid; " ) ;
            }       
            printWriter.println( "      } " ) ;

            //<editor-fold defaultstate="collapsed" desc="SortBy Methods // David">
            if(sortBy) {
                printWriter.println("   function sortByHeaderID(id) {");
                printWriter.println("       parent.Data.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;Find;show&sortByField=' + id;");
                printWriter.println("   }");
            }
            //</editor-fold>

            printWriter.println( "  </script>" ) ;
            printWriter.println( "</head><body>" ) ;
            printWriter.println( "<table border=\"0\" width=\"100%\"><tr><td align=\"left\" valign=\"top\" class=\"white\" width=\"*\"> <nobr> " ) ;
            printWriter.println( "<img src=\"/EIDPWebApp/images/" + uso.applicationContext + ".jpg\" onerror=\"this.src='/EIDPWebApp/images/eidp_logo.jpg'\" border=\"0\" width=\"40\" height=\"20\"><font size=\"+1\">&nbsp;&nbsp;<b>Find</b></font>" ) ;
//            printWriter.println( "</td></tr></table>" ) ;
            printWriter.println( "<hr> " ) ;
            printWriter.println( "<div align=\"center\"> " ) ;
            printWriter.println( "<table border=\"1\" cellspacing=\"0\">" ) ;
            // Header of the find-table
            printWriter.println( "  <tr class=\"header\"> " ) ;

            for ( int i = 0 ; i < fieldNodes.size() ; i++ ) {
                Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , (NodeList)fieldNodes.get( i )) ;
                boolean blockField = false ;
                for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                    if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                        blockField = true ;
                    }
                }
                if ( blockField == false ) {
                    String fieldLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label" , (NodeList)fieldNodes.get( i )) ).get( 0 ) ;
                    //<editor-fold defaultstate="collapsed" desc="SortBy Methods // David">
                    try {
                        sortBy = uso.xmlDataAccess.getElementsByName( "sort-method" , (NodeList)fieldNodes.get(i) ).isEmpty();
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        sortBy = false;
                    }
                    //</editor-fold>
                    if (sortBy) {
                        printWriter.println( "      <td align=\"center\" valign=\"center\">" + fieldLabel + "</td> ");
                    } else {
                        String classmark = new String();
                        String img = new String();
                        if (sortID == i) {
                            classmark = "sort"; 
                            img = "<img id=\"button\" src=\"/EIDPWebApp/images/sort_triangle.jpg\" height=\"\" width=\"\" alt=\"\" />";
                        } else {
                            classmark = "label";
                            img = "";
                        }
                        printWriter.println( "      <td id=\"" + i + "\" class=\"" + classmark + "\" align=\"center\" valign=\"center\" onClick=\"javascript:sortByHeaderID(" + i + ");\">" + fieldLabel + img + "</td> " ) ;
                    }
                }
            }
            printWriter.println( "  </tr> " ) ;
            // table rows
            for ( int i = 0 ; i < uso.dbMapper.size() ; i++ ) {
                String strID = (String)((HashMap)uso.dbMapper.getRow( i )).get( publicSelectField );
                printWriter.println( "  <tr style=\"cursor:pointer;\" onClick=\"javascript:if ( linkClicked == 0 ) { linkClicked = 1 ; getPatient(" + strID + ") ; } \" bgcolor=\"#EEEEEE\" id=\"" + strID + "\" onMouseover=\"javascript:markRow(" + strID + ")\" onMouseout=\"javascript:demarkRow(" + strID + ")\"> " ) ;
                if ( publicSelectField.equals( "" ) ) {
                    for ( int ii = 0 ; ii < fieldNodes.size() ; ii++ ) {
                        Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , (NodeList)fieldNodes.get( ii )) ;
                        boolean blockField = false ;
                        for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                            if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                                blockField = true ;
                            }
                        }
                        if ( blockField == false ) {
                            String fieldID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)fieldNodes.get( ii ) ) ).get( 0 ) ;
                            Vector encodeNumberVector = (Vector)uso.xmlDataAccess.getElementsByName( "encode" , (NodeList)fieldNodes.get( ii ) ) ;
                            String showNumber = (String)((HashMap)uso.dbMapper.getRow( i )).get( fieldID ) ;
                            if ( encodeNumberVector.size() > 0 ) {
                                String encodeNumber = (String)encodeNumberVector.get( 0 ) ;
                                if ( encodeNumber.equals( "true" ) ) {
                                    showNumber = this.encodeNumber( Integer.parseInt( showNumber ) ) ;
                                }
                            }
                            printWriter.println( "      <td align=\"center\" valign=\"center\">&nbsp;" + showNumber + "&nbsp;</td>" ) ;
                        }
                    }
                } else {
                    for ( int ii = 0 ; ii < fieldNodes.size() ; ii++ ) {
                        Vector blockRoles = (Vector)uso.xmlDataAccess.getElementsByName( "block-role" , (NodeList)fieldNodes.get( ii )) ;
                        boolean blockField = false ;
                        for ( int bi = 0 ; bi < blockRoles.size() ; bi++ ) {
                            if ( uso.eidpWebAppCache.userRoles_contains( (String)blockRoles.get( bi ) ) ) {
                                blockField = true ;
                            }
                        }
                        if ( blockField == false ) {
                            String fieldID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)fieldNodes.get( ii ) ) ).get( 0 ) ;
                            Vector encodeNumberVector = (Vector)uso.xmlDataAccess.getElementsByName( "encode" , (NodeList)fieldNodes.get( ii ) ) ;
                            String showNumber = (String)((HashMap)uso.dbMapper.getRow( i )).get( fieldID ) ;
                            if ( encodeNumberVector.size() > 0 ) {
                                String encodeNumber = (String)encodeNumberVector.get( 0 ) ;
                                if ( encodeNumber.equals( "true" ) ) {
                                    showNumber = this.encodeNumber( Integer.parseInt( showNumber ) ) ;
                                }
                            }
                            printWriter.println( "      <td align=\"center\" valign=\"center\">&nbsp;" + showNumber + "&nbsp;</td>" ) ;
                        }
                    }
                }
                printWriter.println( "  </tr> " ) ;
            }
            printWriter.println( "</table> " ) ;
            printWriter.println( "</div> " ) ;

        } else if ( ((String)uso.eidpWebAppCache.sessionData_get( "moduleAction" )).equals( "store" ) ) {
            String publicSelectField = "" ;
            try {
                publicSelectField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-select-field" , findNode ) ).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                publicSelectField = "" ;
            }
            // selectValue from request
            String selectValue = (String)request.getParameter( "selectValue" ) ;
            String finalModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "load-module" , findNode ) ).get( 0 ) ;
            if ( publicSelectField.equals( "" ) ) {
                // set session:
                String sessionRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "session-ref" , findNode ) ).get( 0 ) ;
                uso.eidpWebAppCache.sessionData_set( sessionRef , selectValue ) ;
            } else {
                // publicSelectField needs to process and load
                // get all public-data:
                String publicSessionRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-session-ref" , findNode ) ).get( 0 ) ;
                String sessionRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "session-ref" , findNode ) ).get( 0 ) ;
                String selectField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "select-field" , findNode ) ).get( 0 ) ;
                String publicDataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-dataset" , findNode ) ).get( 0 ) ;
                String publicMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-method" , findNode ) ).get( 0 ) ;
                Vector publicPanelData = (Vector)uso.xmlDataAccess.getElementsByName( "public-session-panel-data" , findNode ) ;
                Vector publicPanelField = (Vector)uso.xmlDataAccess.getElementsByName( "panel-select-field" , findNode ) ;
                HashMap paramMap = new HashMap() ;
                paramMap.put( publicSelectField , selectValue ) ;
                uso.dbMapper.DBAction( publicDataset , publicMethod , paramMap ) ;
                String hiddenPrimaryKey = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( selectField ) ;
                uso.eidpWebAppCache.sessionData_set( publicSessionRef , selectValue ) ;
                uso.eidpWebAppCache.sessionData_set( sessionRef , hiddenPrimaryKey ) ;
                // CENTER_PERMISSIONS
                String patientCenter = (String)((HashMap)uso.dbMapper.getRow(0)).get( "center" );
                System.out.println("----C PAT_CENTER: "+patientCenter);
                uso.eidpWebAppCache.sessionData_set( "PkCenter" , patientCenter );
                for(int ppd = 0 ; ppd < publicPanelData.size() ; ppd++ ){
                    String strPublicPanelData = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-session-panel-data" , findNode ) ).get( ppd ) ;
                    String strPublicPanelField = (String)((Vector)uso.xmlDataAccess.getElementsByName( "panel-select-field" , findNode ) ).get( ppd ) ;
                    String publicPanelFieldData = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( strPublicPanelField ) ;
                    uso.eidpWebAppCache.sessionData_set( strPublicPanelData , publicPanelFieldData ) ;
                }
            }
            // Now set the secondary key:
            String secRefDataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,dataset" , findNode ) ).get( 0 ) ;
            String secRefMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,method" , findNode ) ).get( 0 ) ;
            String secRefSessionListName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-list-name" , findNode ) ).get( 0 ) ;
            String secRefSessionRefName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-ref-name" , findNode ) ).get( 0 ) ;
            String secRefID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,ref-id" , findNode ) ).get( 0 ) ;
            String secRefLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-label" , findNode ) ).get( 0 ) ;
            String secRefSessionRefNameShow = "" ;
            String secRefIDShow = "" ;
            boolean secRefShow = false ;
            try {
                secRefSessionRefNameShow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-ref-name-show" , findNode ) ).get( 0 ) ;
                secRefShow = true ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                secRefShow = false ;
            }
            if ( secRefShow == true ) {
                secRefIDShow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,ref-id-show" , findNode ) ).get( 0 ) ;
            }
            HashMap paramMap = new HashMap() ;
            NodeList paramNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "secondary-ref" , findNode ) ).get( 0 ) ;
            paramMap = this.getParams( paramNode , uso ) ;
            // get data:
            uso.dbMapper.DBAction( secRefDataset , secRefMethod , paramMap ) ;
            Vector sessionListEntry = new Vector() ;
            String sessionRefEntry = "" ;
            String sessionRefEntryShow = "" ;
            if ( uso.dbMapper.size() > 0 ) {
                sessionListEntry = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;
                sessionRefEntry = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( secRefID ) ;
                if ( secRefShow == true ) {
                    sessionRefEntryShow = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( secRefIDShow ) ;
                }
            }
            uso.eidpWebAppCache.sessionData_set( secRefSessionRefName , sessionRefEntry ) ;
            uso.eidpWebAppCache.sessionData_set( secRefSessionListName , sessionListEntry ) ;
            if ( secRefShow == true ) {
                uso.eidpWebAppCache.sessionData_set( secRefSessionRefNameShow , sessionRefEntryShow ) ;
            }
            uso.eidpWebAppCache.sessionData_set( "SecondaryKeyLabel" , secRefLabel ) ;
            uso.eidpWebAppCache.sessionData_set( "SecondaryKeySessionRef" , secRefSessionRefName ) ;
            // redirect to finalModule:
            String url = "";
            String finalFinalModule = (String)request.getParameter( "finalFinalModule" ) ;
            if( finalFinalModule != null ){
                url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + finalFinalModule + ";show" ;
            }else{
                url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + finalModule + ";show" ;
            }
            // redirect to visit-module if application-variable is set // Stephan
            // newVisitValue from request
            String newVisit = (String)request.getParameter( "newVisit" ) ;
            if(newVisit != null && newVisit.equals("1")){
                url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=XMLDispatcher;NewSecondaryKeyFromList;show" ;
            }
            response.sendRedirect( url ) ;
        }
    }
    
    /**
     *
     * @param request
     * @param response
     * @param printWriter
     * @param primaryKeyNode
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processNewPrimaryKeyFunction( HttpServletRequest request , HttpServletResponse response , PrintWriter printWriter , NodeList primaryKeyNode , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        String dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , primaryKeyNode ) ).get( 0 ) ;
        String setMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "set-method" , primaryKeyNode ) ).get( 0 ) ;
        String getMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "get-method" , primaryKeyNode ) ).get( 0 ) ;
        String primaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-key" , primaryKeyNode ) ).get( 0 ) ;
        String sessionPrimaryRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "session-ref" , primaryKeyNode ) ).get( 0 ) ;
        String secureGenerationString = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secure-generation" , primaryKeyNode ) ).get( 0 ) ;
        if ( ! secureGenerationString.equals( "true") ) {
            HashMap paramMap = new HashMap() ;
            // 1. doc_id; 2. center; 3. doc_timestamp; 4. create_timestamp
            String doc_id = uso.userID ;
            String center = (String)uso.eidpWebAppCache.sessionData_get( "userCenter" ) ;
            Date timestamp = new Date() ;
            String timestampString = String.valueOf( timestamp.getTime() ) ;
            paramMap.put( "doc_id" , doc_id ) ;
            paramMap.put( "doc_timestamp" , timestampString ) ;
            paramMap.put( "center" , center ) ;
            paramMap.put( "create_timestamp" , timestampString ) ;
            paramMap.put( "application" , uso.applicationContext ) ;
            paramMap.put( "id" , "0" ) ;
            uso.dbMapper.DBAction( dataset , setMethod , paramMap ) ;
            // get New Patient ID:
            paramMap.clear() ;
            paramMap.put( "doc_id" , doc_id ) ;
            paramMap.put( "create_timestamp" , timestampString ) ;
            paramMap.put( "application" , uso.applicationContext ) ;
            uso.dbMapper.DBAction( dataset , getMethod , paramMap ) ;
            String patientID = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( primaryKey ) ;
            // set PrimaryKey in Session
            uso.eidpWebAppCache.sessionData_set( sessionPrimaryRef , patientID ) ;
            // CENTER_PREMISSIONS
            String patientCenter = (String)((HashMap)uso.dbMapper.getRow(0)).get( "center" );
            System.out.println("----C PAT_CENTER: "+patientCenter);
            uso.eidpWebAppCache.sessionData_set( "PkCenter" , patientCenter );
        } else {
            String publicPrimaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-primary-key" , primaryKeyNode ) ).get( 0 ) ;
            String publicSessionPrimaryRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-session-ref" , primaryKeyNode ) ).get( 0 ) ;
            HashMap paramMap = new HashMap() ;
            String doc_id = uso.userID ;
            String center = (String)uso.eidpWebAppCache.sessionData_get( "userCenter" ) ;
            Date timestamp = new Date() ;
            String timestampString = String.valueOf( timestamp.getTime() ) ;
            String secureString = doc_id + "-" + center + "-" + timestampString ;
            try {
                secureString = this.encryptString( secureString ) ;
            } catch ( java.lang.Exception e ) {
                throw new javax.servlet.ServletException( "Could not generate secure patient ID: " + e ) ;
            }
            paramMap.put( primaryKey , secureString ) ;
            paramMap.put( "center" , center ) ;
            paramMap.put( "application" , uso.applicationContext ) ;
            paramMap.put( "id" , "0" ) ;
            uso.dbMapper.DBAction( dataset , setMethod , paramMap ) ;
            // get new public id:
            paramMap.clear() ;
            paramMap.put( primaryKey , secureString ) ;
            uso.dbMapper.DBAction( dataset , getMethod , paramMap ) ;
            String publicPatientID = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( publicPrimaryKey ) ;
            // set Session:
            uso.eidpWebAppCache.sessionData_set( sessionPrimaryRef , secureString ) ;
            uso.eidpWebAppCache.sessionData_set( publicSessionPrimaryRef , publicPatientID ) ;
        }
        // Now set the secondary key:
        String secRefDataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,dataset" , primaryKeyNode ) ).get( 0 ) ;
        String secRefMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,method" , primaryKeyNode ) ).get( 0 ) ;
        String secRefSessionListName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-list-name" , primaryKeyNode ) ).get( 0 ) ;
        String secRefSessionRefName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-ref-name" , primaryKeyNode ) ).get( 0 ) ;
        String secRefID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,ref-id" , primaryKeyNode ) ).get( 0 ) ;
        String secRefLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-label" , primaryKeyNode ) ).get( 0 ) ;
        String secRefSessionRefNameShow = "" ;
        String secRefIDShow = "" ;
        boolean secRefShow = false ;
        try {
            secRefSessionRefNameShow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-ref-name-show" , primaryKeyNode ) ).get( 0 ) ;
            secRefShow = true ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            secRefShow = false ;
        }
        if ( secRefShow == true ) {
            secRefIDShow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,ref-id-show" , primaryKeyNode ) ).get( 0 ) ;
        }
        HashMap paramMap = new HashMap() ;
        NodeList paramNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "secondary-ref" , primaryKeyNode ) ).get( 0 ) ;
        paramMap = this.getParams( paramNode , uso ) ;
        // get data:
        uso.dbMapper.DBAction( secRefDataset , secRefMethod , paramMap ) ;
        Vector sessionListEntry = new Vector() ;
        String sessionRefEntry = "" ;
        String sessionRefEntryShow = "" ;
        if ( uso.dbMapper.size() > 0 ) {
            sessionListEntry = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;
            sessionRefEntry = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( secRefID ) ;
            if ( secRefShow == true ) {
                sessionRefEntryShow = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( secRefIDShow ) ;
            }
        }
        uso.eidpWebAppCache.sessionData_set( secRefSessionRefName , sessionRefEntry ) ;
        uso.eidpWebAppCache.sessionData_set( secRefSessionListName , sessionListEntry ) ;
        if ( secRefShow == true ) {
            uso.eidpWebAppCache.sessionData_set( secRefSessionRefNameShow , sessionRefEntryShow ) ;
        }
        uso.eidpWebAppCache.sessionData_set( "SecondaryKeyLabel" , secRefLabel ) ;
        uso.eidpWebAppCache.sessionData_set( "SecondaryKeySessionRef" , secRefSessionRefName ) ;
        
        String finalModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "load-module" , primaryKeyNode ) ).get( 0 ) ;
        // redirect to finalModule:
        String url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + finalModule + ";show" ;
        response.sendRedirect( url ) ;
    }
    
    /**
     *
     * @param request
     * @param response
     * @param primaryKeyNode
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processLoadPrimaryKeyFunction( HttpServletRequest request , HttpServletResponse response , NodeList primaryKeyNode , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        String dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , primaryKeyNode ) ).get( 0 ) ;
        String method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , primaryKeyNode ) ).get( 0 ) ;
        String primaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "primary-key" , primaryKeyNode ) ).get( 0 ) ;
        String sessionPrimaryRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "session-ref" , primaryKeyNode ) ).get( 0 ) ;
        String publicPrimaryKey = "" ;
        String publicSessionPrimaryRef = "" ;
        try {
            publicPrimaryKey = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-primary-key" , primaryKeyNode ) ).get( 0 ) ;
            publicSessionPrimaryRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "public-session-ref" , primaryKeyNode ) ).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
            publicPrimaryKey = "" ;
        }
        HashMap paramMap = new HashMap() ;
        String cR = "" ;
        HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
        Object [] centerRoles = ((Set)cRoles.keySet()).toArray() ;
        for ( int ci = 0 ; ci < centerRoles.length ; ci++ ) {
            if ( ci > 0 ) { cR += " , " ; }
            cR += (String)centerRoles[ci] ;
        }
        // get New Patient ID:
        String patientID = "" ;
        String publicPatientID = "" ;
        if ( publicPrimaryKey.equals( "" ) ) {
            patientID = (String)request.getParameter( primaryKey ) ;
            paramMap.put( primaryKey , patientID ) ;
        } else {
            publicPatientID = (String)request.getParameter( publicPrimaryKey ) ;
            paramMap.put( publicPrimaryKey , publicPatientID ) ;
        }
        paramMap.put( "center" , cR ) ;
        paramMap.put( "application" , uso.applicationContext ) ;
        uso.dbMapper.DBAction( dataset , method , paramMap ) ;
        if ( uso.dbMapper.size() == 0 ) {
            // redirect back to select Select Primary Key
            String url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SelectPrimaryKey;show" ;
            response.sendRedirect( url ) ;
        } else {
            // set PrimaryKey in Session
            if ( publicPrimaryKey.equals( "" ) ) {
                uso.eidpWebAppCache.sessionData_set( sessionPrimaryRef , patientID ) ;
            } else {
                patientID = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( primaryKey ) ;
                // CENTER_PREMISSIONS
                String patientCenter = (String)((HashMap)uso.dbMapper.getRow(0)).get( "center" );
                System.out.println("----C PAT_CENTER: "+patientCenter);
                uso.eidpWebAppCache.sessionData_set( sessionPrimaryRef , patientID ) ;
                uso.eidpWebAppCache.sessionData_set( "PkCenter" , patientCenter );
                uso.eidpWebAppCache.sessionData_set( publicSessionPrimaryRef , publicPatientID ) ;
            }
            // Now set the secondary key:
            String secRefDataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,dataset" , primaryKeyNode ) ).get( 0 ) ;
            String secRefMethod = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,method" , primaryKeyNode ) ).get( 0 ) ;
            String secRefSessionListName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-list-name" , primaryKeyNode ) ).get( 0 ) ;
            String secRefSessionRefName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-ref-name" , primaryKeyNode ) ).get( 0 ) ;
            String secRefID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,ref-id" , primaryKeyNode ) ).get( 0 ) ;
            String secRefLabel = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-label" , primaryKeyNode ) ).get( 0 ) ;
            String secRefSessionRefNameShow = "" ;
            String secRefIDShow = "" ;
            boolean secRefShow = false ;
            try {
                secRefSessionRefNameShow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,session-ref-name-show" , primaryKeyNode ) ).get( 0 ) ;
                secRefShow = true ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                secRefShow = false ;
            }
            if ( secRefShow == true ) {
                secRefIDShow = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-ref,ref-id-show" , primaryKeyNode ) ).get( 0 ) ;
            }
            paramMap.clear() ;
            NodeList paramNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "secondary-ref" , primaryKeyNode ) ).get( 0 ) ;
            paramMap = this.getParams( paramNode , uso ) ;
            // get data:
            uso.dbMapper.DBAction( secRefDataset , secRefMethod , paramMap ) ;
            Vector sessionListEntry = new Vector() ;
            String sessionRefEntry = "" ;
            String sessionRefEntryShow = "" ;
            if ( uso.dbMapper.size() > 0 ) {
                sessionListEntry = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;
                sessionRefEntry = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( secRefID ) ;
                if ( secRefShow == true ) {
                    sessionRefEntryShow = (String)((HashMap)uso.dbMapper.getRow( 0 )).get( secRefIDShow ) ;
                }
            }
            uso.eidpWebAppCache.sessionData_set( secRefSessionRefName , sessionRefEntry ) ;
            uso.eidpWebAppCache.sessionData_set( secRefSessionListName , sessionListEntry ) ;
            if ( secRefShow == true ) {
                uso.eidpWebAppCache.sessionData_set( secRefSessionRefNameShow , sessionRefEntryShow ) ;
            }
            uso.eidpWebAppCache.sessionData_set( "SecondaryKeyLabel" , secRefLabel ) ;
            uso.eidpWebAppCache.sessionData_set( "SecondaryKeySessionRef" , secRefSessionRefName ) ;
            String finalModule = (String)((Vector)uso.xmlDataAccess.getElementsByName( "load-module" , primaryKeyNode ) ).get( 0 ) ;
            // redirect to finalModule:
            String url = "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=" + finalModule + ";show" ;
            response.sendRedirect( url ) ;
        }
    }
    
    /**
     *
     * @param response
     * @param printWriter
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void processHelpPage( HttpServletResponse response , PrintWriter printWriter ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        printWriter.println( "<body><h1>HelpPage</h1></body> " ) ;
    }
    
    /**
     * Show a tooltip.
     * @param request
     * @param response
     * @param uso
     * @throws ServletException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    protected void createTOOLTip( HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws javax.servlet.ServletException , java.sql.SQLException, org.xml.sax.SAXException , java.io.IOException {
        PrintWriter printWriter = this.initModuleHTML( response ) ;
        this.initStyleHTML( printWriter ) ;
        printWriter.println( "</head>" ) ;
        printWriter.println( "<body style=\"background-color:#FF8000\"> " ) ;
        printWriter.println( "<b>TOOLTip:</b><br> " ) ;
        printWriter.println( "<font size=\"-1\"> " ) ;
        printWriter.println( (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ) ;
        printWriter.println( "</font>" ) ;
        printWriter.println( "</body>" ) ;
        this.closeHTML( printWriter ) ;
    }
    
    /**
     * Error Page module.
     * @param request
     * @param response
     * @throws IOException
     * @throws SAXException
     */
    protected void ErrorPageModule( HttpServletRequest request , HttpServletResponse response ) throws java.io.IOException , org.xml.sax.SAXException {
        PrintWriter printWriter = this.initModuleHTML( response ) ;
        this.initStyleHTML( printWriter ) ;
        printWriter.println( "</head>" ) ;
        printWriter.println( "<body > " ) ;
        printWriter.println( request.getParameter( "errorMsg" ) ) ;
        printWriter.println( "</body>" ) ;
        printWriter.println( "<script language=\"JavaScript\">parent.SidePanel.location.href='/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show';</script> " ) ;
        this.closeHTML( printWriter ) ;
    }
    
    private HashMap getParams( NodeList dataNode , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        HashMap paramMap = new HashMap() ;
        Vector globalParamsName = (Vector)uso.xmlDataAccess.getElementsByName( "param,id" , dataNode ) ;
        Vector globalParamsRef = (Vector)uso.xmlDataAccess.getElementsByName( "param,ref" , dataNode ) ;
        int i = 0 ;
        for ( i = 0 ; i < globalParamsName.size() ; i++ ) {
            paramMap.put( (String)globalParamsName.get(i) , uso.eidpWebAppCache.sessionData_get( (String)globalParamsRef.get(i) ) ) ;
        }
        return paramMap ;
    }
    
    private String encryptString( String encryptString ) throws java.lang.Exception {
        encryptString = new String( encrypt( encryptString ) ) ;
        encryptString = javax.xml.bind.DatatypeConverter.printBase64Binary(encryptString.getBytes());
        return encryptString ;
    }
    
    private static byte[] encrypt(String inputString) throws java.lang.Exception {
        java.security.MessageDigest md =null;
        md = java.security.MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(inputString.getBytes( "ISO-8859-1" ));
        return md.digest();
    }
    
    private void setUserScopeObjectBeans( UserScopeObject uso ) throws java.rmi.RemoteException {
        uso.dbMapper = (DBMapping) uso.session.getAttribute( "dbMapperHandle" ) ;
        uso.eidpWebAppCache = (EIDPWebAppCache) uso.session.getAttribute( "eidpWebAppCacheHandle" ) ;
        uso.applicationContext = (String)uso.eidpWebAppCache.sessionData_get( "applicationContext" ) ;
        uso.userLogin = (String)uso.eidpWebAppCache.sessionData_get( "userLogin" ) ;
        uso.userID = (String)uso.eidpWebAppCache.sessionData_get( "userID" ) ;
        uso.sharedMethods = new SharedMethods() ;
    }
    
    private String encodeNumber( int inputNumber ) {
        double resultNumber = inputNumber * 1001 / 29 + 5 - 23 * 12 / 2 ;
        resultNumber = Math.log( resultNumber ) ;
        String resultNumberString = String.valueOf( resultNumber ) ;
        int decIndex = resultNumberString.indexOf( "." ) ;
        resultNumberString = resultNumberString.substring( 0 , 8+decIndex ) ;
        resultNumberString = resultNumberString.replaceFirst( "\\." , "" ) ;
        return resultNumberString ;
    }
    
    private String getCssTheme( UserScopeObject uso ) {
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/webmenu.xml" ;
        String theme = "";
        try {
            uso.xmlWebMenu = new XMLDataAccess( xmlfile ) ;
            Vector sponsorsVector = uso.xmlWebMenu.getNodeListsByName( "menu" ) ;
            theme = (String)((Vector)uso.xmlWebMenu.getElementsByName("css-theme", (NodeList)sponsorsVector.get(0) )).get(0) ;
        } catch (Exception e) {}
        return theme;
    }
}
