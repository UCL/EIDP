/*
 * EIDPException.java
 *
 * Created on 31. Januar 2005, 13:57
 */

package com.eidp.webctrl;

import java.io.*;
import java.net.*;

import javax.servlet.ServletConfig ;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException ;

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


public class EIDPException extends HttpServlet {
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String statusCode = (String)request.getAttribute( "javax.servlet.error.status_code" ) ;
        Throwable exception = (Throwable)request.getAttribute( "javax.servlet.error.exception_type" ) ;
        String message = (String)request.getAttribute( "javax.servlet.error.message" ) ;
        response.setContentType("text/html");
        PrintWriter printWriter = response.getWriter();
        
        printWriter.println("<html>");
        printWriter.println("<head>");
        printWriter.println("<title>EIDP Web-Controller Exception</title>");
        printWriter.println("</head>");
        printWriter.println("<body bgcolor=\"#000000\">");
        
        printWriter.println( "<div align=\"center\" valign=\"center\">" ) ;
        printWriter.println( "<table border=\"0\" width=\"100%\">" ) ;
        
        printWriter.println( "<tr>" ) ;
        printWriter.print( "<td width=\"25\">" ) ;
        printWriter.print( "<img src=\"images/eidp.jpg\" border=\"0\">" ) ;
        printWriter.println( "</td>" ) ;
        
        printWriter.print( "<td> " ) ;
        printWriter.print( "EIDP Web-Controller Exeption" ) ;
        printWriter.println( "</td>" ) ;
        
        printWriter.println( "</tr>" ) ;
        
        printWriter.println( "<tr>" ) ;
        printWriter.print( "<td colspan=\"2\">" ) ;
        printWriter.print( "<font size=\"3\" color=\"#AA0000\">You have been logged in to the system for more than 30 min," ) ;
        printWriter.print( "<br>without performing actions." ) ;
        printWriter.print( "<br>Due to security reasons you have been logged out automatically." ) ;
        printWriter.print( "<br><br>Please click \"Logout\"." ) ;
        printWriter.print( "<br><br>You can log in into the system afterwards." ) ;
        printWriter.println( "</font></td>" ) ;
        printWriter.println( "</tr>" ) ;
        
        printWriter.println( "<tr>" ) ;
        printWriter.print( "<td colspan=\"2\">" ) ;
        printWriter.print( "<font size=\"3\" color=\"#AA0000\">Sie waren l&auml;nger als eine halbe Stunde angemeldet," ) ;
        printWriter.print( "<br>ohne Aktionen auf dieser Seite ausgef�hrt zu haben." ) ;
        printWriter.print( "<br><br>Aus Sicherheitsgr&uuml;nden wurden Sie automatisch wieder abgemeldet." ) ;
        printWriter.print( "<br><br>Bitte klicken Sie auf \"Logout\" oben links." ) ;
        printWriter.print( "<br>Danach k�nnen Sie sich wieder anmelden." ) ;
        printWriter.println( "</font></td>" ) ;
        printWriter.println( "</tr>" ) ;
        
        printWriter.println( "<tr>" ) ;
        
        this.generateExceptionMessage( printWriter , statusCode , exception , message ) ;
        
        printWriter.println( "</tr>" ) ;
        
        printWriter.println( "</table>" ) ;
        printWriter.println( "</div>" ) ;
        printWriter.println("</body>");
        printWriter.println("</html>");
        
        printWriter.close();
    }
    
    protected void generateExceptionMessage( PrintWriter printWriter , String statusCode , Throwable exception , String message ) throws ServletException , IOException {
        printWriter.print( "<td>" ) ;
        printWriter.print( "StatusCode: ") ;
        printWriter.println( "</td>" ) ;
        printWriter.print( "<td>" ) ;
        printWriter.print( statusCode ) ;
        printWriter.println( "</td>" ) ;
        
        printWriter.println( "</tr><tr>" ) ;
        printWriter.print( "<td>" ) ;
        printWriter.print( "Message:" ) ;
        printWriter.println( "</td>" ) ;
        printWriter.print( "<td>" ) ;
        printWriter.print( message ) ;
        printWriter.println( "</td>" ) ;
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "TwException Servlet.";
    }
    
}
