/*	
 * Percentile.java	
 *	
 * Created on February 24, 2006, 2:11 PM	
 *	
 * To change this template, choose Tools | Options and locate the template under	
 * the Source Creation and Management node. Right-click the template and choose	
 * Open. You can then make changes to the template in the Source Editor.	
 */	
	
package com.eidp.webctrl.modules.ESID;	
	
import com.eidp.core.DB.DBMappingHomeRemote;	
import com.eidp.core.DB.DBMappingRemote;	
import com.eidp.UserScopeObject.UserScopeObject;	
import com.eidp.webctrl.WebAppCache.EIDPWebAppCacheRemote ;	
import com.eidp.webctrl.modules.EIDPGenerator;	
import com.eidp.webctrl.modules.Hilf;	
	
import java.io.PrintWriter;	
	
import javax.servlet.RequestDispatcher;	
import javax.servlet.http.HttpServlet;	
import javax.servlet.http.HttpSession;	
import javax.servlet.http.HttpServletRequest;	
import javax.servlet.http.HttpServletResponse;	
import javax.ejb.Handle;	
	
import java.util.HashMap;	
import java.util.Vector;	
import java.util.StringTokenizer;	
import java.util.Date;	
import java.text.SimpleDateFormat;	
import java.text.DateFormat;	
	
import java.rmi.RemoteException;	
import java.sql.SQLException;	
import org.xml.sax.SAXException;	
import java.io.IOException;	
import java.sql.*;	
	
/**	
 *	
 * @author  rusch	
 */	
public class Percentile {	
    	
    /** Creates a new instance of Percentile */	
    public Percentile( PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws RemoteException, SQLException, SAXException, IOException {	
        	
        Hilf hilfe = new Hilf() ;	
        	
        // Stylesheet	
        printWriter.println( "<style type=\"text/css\">" ) ;	
        printWriter.println( "<!-- " ) ;	
        printWriter.println( "  td { background-color:#DDDDDD;color:#000000 ; font-size:11pt ; } " ) ;	
        printWriter.println( "  td.label { background-color:#FFFFFF;color:#000000 ; font-size:11pt ; vertical-align:bottom ; text-align:center ; } " ) ;	
        printWriter.println( "  td.data { background-color:#FFFFFF ;color:#777777 ; font-size:10pt ; vertical-align:bottom ; text-align:center ; } " ) ;	
        printWriter.println( "  a.a_svg { color:#FF0000 ; font-size:12pt ; } " ) ;	
        printWriter.println( "--> </style> " ) ;	
        	
        printWriter.println( "<script language=\"JavaScript\"> " ) ;	
        printWriter.println( "function openSVG(){" ) ;	
        printWriter.println( "    url=\"http://www.adobe.com/svg/viewer/install/main.html\";" ) ;	
        printWriter.println( "    windowOpenFeatures = \"height=400,width=600,scrollbars=yes\" ; " ) ;	
        printWriter.println( "    windowName= \"SVG\" ; " ) ;	
        printWriter.println( "    var nwindow = window.open( url,windowName,windowOpenFeatures ) ; " ) ;	
        printWriter.println( "    nwindow.focus() ; " ) ;	
        printWriter.println( "}" ) ;	
        printWriter.println( "function openSVGHelp(){" ) ;	
        printWriter.println( "    url=\"/EIDPWebApp/svghelp.html\";" ) ;	
        printWriter.println( "    windowOpenFeatures = \"height=400,width=600,scrollbars=yes\" ; " ) ;	
        printWriter.println( "    windowName= \"SVG\" ; " ) ;	
        printWriter.println( "    var nwindow = window.open( url,windowName,windowOpenFeatures ) ; " ) ;	
        printWriter.println( "    nwindow.focus() ; " ) ;	
        printWriter.println( "}" ) ;	
        printWriter.println( "function isSVGPluginInstalled() {" ) ;	
        printWriter.println( "  return (navigator.mimeTypes[\"image/svg\"]&&navigator.mimeTypes[\"image/svg\"].enabledPlugin!=null)||(navigator.mimeTypes[\"image/svg-xml\"]&&navigator.mimeTypes[\"image/svg-xml\"].enabledPlugin!=null);" ) ;	
        printWriter.println( "};" ) ;	
        printWriter.println( "</script> " ) ;	
        	
        // ------------------------- get the data ------------------------------------------	
        HashMap paramMap = new HashMap() ;	
        paramMap.put( "patient_id" , (String)uso.eidpWebAppCache.sessionData_get("PatientID") ) ;	
        // 1. Height and Weight and Head in Vector	
        uso.dbMapper.DBAction( "CLINICALINVESTIGATION" , "getPercentileDataForPatientID" , paramMap ) ;	
        Vector allRowsOfPercentile = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;	
        boolean showHeight = true;	
        boolean showWeight = true;	
        boolean showHead = true;	
        boolean showAny = true;	
        String strHeight = "";	
        String strWeight = "";	
        String strHead = "";	
        String strWeightDates = "";	
        String strHeightDates = "";	
        String strHeadDates = "";	
        String strLastVisitDate = "";	
        String strFirstVisitDate = "";	
        for(int i=0 ; i < allRowsOfPercentile.size() ; i++){	
            HashMap hmData = (HashMap)allRowsOfPercentile.get(i);	
            String test = this.getEmptyStringForNull((String)hmData.get("height"));	
            String strDate = (String)hmData.get("date");	
            if(!test.equals("")){	
                strHeight += test + ";";	
                strHeightDates += strDate + ";";	
            }	
            test = this.getEmptyStringForNull((String)hmData.get("weight"));	
            if(!test.equals("")){	
                strWeight += test + ";";	
                strWeightDates += strDate + ";";	
            } 	
            test = this.getEmptyStringForNull((String)hmData.get("head_circumf"));	
            if(!test.equals("")){	
                strHead += test + ";";	
                strHeadDates += strDate + ";";	
            }	
            if(i==0){	
                strFirstVisitDate = strDate;	
            }	
            strLastVisitDate = strDate;	
        }	
        if(strHeight.equals("") || strHeight.replaceAll(";","").equals("")){	
            showHeight = false;	
        }	
        if(strWeight.equals("") || strWeight.replaceAll(";","").equals("")){	
            showWeight = false;	
        }	
        if(strHead.equals("") || strHead.replaceAll(";","").equals("")){	
            showHead = false;	
        }	
        // 2. Birthdate and sex in Vector	
        uso.dbMapper.DBAction( "PATIENTS" , "getPatientForPatientID" , paramMap ) ;	
        allRowsOfPercentile = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;	
        HashMap hmData = (HashMap)allRowsOfPercentile.get(0);	
        String strDobM = this.getEmptyStringForNull((String)hmData.get("dob_m"));	
        String strDobY = this.getEmptyStringForNull((String)hmData.get("dob_y"));	
        String strSex = this.getEmptyStringForNull((String)hmData.get("sex"));	
        if(strDobM.equals("") || strDobY.equals("") || !(strSex.equals("m") || strSex.equals("f"))){	
            showAny = false;	
        }	
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:ss:mm.SSS") ;	
        Date dateMaxDate = new Date();	
        Date dateFirstVisitDate = new Date();	
        Date dateLastVisitDate = new Date();	
        Date dateBirthdate = new Date();	
        try{	
            dateBirthdate = dateFormat.parse( strDobY + "-" + strDobM + "-01 00:00:00.0" ) ;	
            dateFirstVisitDate = dateFormat.parse( strFirstVisitDate + " 00:00:00.0" ) ;	
            dateLastVisitDate = dateFormat.parse( strLastVisitDate + " 00:00:00.0" ) ;	
        }catch(java.text.ParseException e){	
            showAny = false;	
        }	
	
        // Header	
        EIDPGenerator twGen = new EIDPGenerator("Percentiles", printWriter, uso ) ;	
         	
        printWriter.println( "<hr>" ) ;	
        	
        printWriter.println( "<br>" ) ;	
        printWriter.println( "<br>" ) ;	
        printWriter.println( "<br>" ) ;	
        printWriter.println( "<br>" ) ;	
        printWriter.println( "<br>" ) ;	
        	
        if(showAny){	
            if(showWeight){	
                // get the maximum-date where percentile data is reachable	
                try{	
                    int addMonths = 192;	
                    int month = Integer.parseInt(strDobM);	
                    month = (month + addMonths) % 12;	
                    int year = Integer.parseInt(strDobY);	
                    year = year + ((month + addMonths) / 12);	
                    dateMaxDate = dateFormat.parse( year + "-" + month + "-01 00:00:00.0" ) ;	
                    if(dateFirstVisitDate.compareTo(dateMaxDate) > 0){	
                        showWeight = false;	
                    }	
                }catch(java.text.ParseException e){	
                }	
 	
                if(showWeight){	
                    this.printImage(printWriter, "Weight", strWeight, strDobM, strDobY, strWeightDates, strSex);	
                }else{	
                    printWriter.println( "<center>Weight: The first time of the measurement for the patient is after the the range of the percentiles.</center><br>" ) ;	
                }	
            }else{	
                printWriter.println( "<center>Weight: There is no weight-data for the patient in the database</center><br>" ) ;	
            }	
            if(showHeight){	
                // get the maximum-date where percentile data is reachable	
                try{	
                    int addMonths = 216;	
                    int month = Integer.parseInt(strDobM);	
                    month = (month + addMonths) % 12;	
                    int year = Integer.parseInt(strDobY);	
                    year = year + ((month + addMonths) / 12);	
                    dateMaxDate = dateFormat.parse( year + "-" + month + "-01 00:00:00.0" ) ;	
                    if(dateFirstVisitDate.compareTo(dateMaxDate) > 0){	
                        showHeight = false;	
                    }	
                }catch(java.text.ParseException e){	
                }	
 	
                if(showHeight){	
                    this.printImage(printWriter, "Height", strHeight, strDobM, strDobY, strHeightDates, strSex);	
                }else{	
                    printWriter.println( "<center>Height: The first time of the measurement for the patient is after the the range of the percentiles.</center><br>" ) ;	
                }	
            }else{	
                printWriter.println( "<center>Height: There is no height-data for the patient in the database.</center><br>" ) ;	
            }	
            if(showHead){	
                // get the maximum-date where percentile data is reachable	
                try{	
                    int addMonths = 72;	
                    int month = Integer.parseInt(strDobM);	
                    month = (month + addMonths) % 12;	
                    int year = Integer.parseInt(strDobY);	
                    year = year + ((month + addMonths) / 12);	
                    dateMaxDate = dateFormat.parse( year + "-" + month + "-01 00:00:00.0" ) ;	
                    if(dateFirstVisitDate.compareTo(dateMaxDate) > 0){	
                        showHead = false;	
                    }	
                }catch(java.text.ParseException e){	
                }	
 	
                if(showHead){	
                    this.printImage(printWriter, "Head", strHead, strDobM, strDobY, strHeadDates, strSex);	
                }else{	
                    printWriter.println( "<center>Head: The first time of the measurement for the patient is after the the range of the percentiles.</center><br>" ) ;	
                }	
            }else{	
                printWriter.println( "<center>Head: There is no head-circumference-data for the patient in the database</center><br>" ) ;	
            }	
            	
            	
        }else{	
            // Birthmonth or birthyear or gender is empty	
            printWriter.println( "<br>");	
            printWriter.println( "<center>No data: Please enter first data for birthdate and gender and weight,heigt or head-extent.</center>");	
        }	
        	
        twGen.CloseModule( printWriter , uso ) ;	
    }	
    	
    public String getEmptyStringForNull(String val){	
        if(val==null || val.trim().equals("") || val.trim().equals("null") ){	
            return "";	
        }else{	
            return val ;	
        }	
    }	
    	
    public void printImage(PrintWriter printWriter, String category, String data, String dobM, String dobY, String dates, String gender){	
        printWriter.println( "<table align=\"center\" border=\"0\">" ) ;	
        printWriter.println( "  <tr>" ) ;	
        printWriter.println( "      <td>" ) ;	
        printWriter.println( "<script language=\"JavaScript\"> " ) ;
        printWriter.println( "          document.writeln(\"<IMG SRC='/EIDPWebApp/servlet/com.eidp.webctrl.modules.ESID.Percentile_IMG?category=" + category + "&data=" + data + "&dobM=" + dobM + "&dobY=" + dobY + "&dates=" + dates + "&sex=" + gender + "&svg=0'>\"); ") ;	
        // Moeglichkeit der SVG-Anzeige wegen Performance-Schwierigkeiten bei Browsern ausdokumentiert
//        printWriter.println( "if(navigator.appName=='Microsoft Internet Explorer'){ " ) ;	
//        printWriter.println( "      if(isSVGPluginInstalled()){ " ) ;	
//        printWriter.println( "          document.writeln(\"<embed src='http://localhost/EIDPWebApp/servlet/com.eidp.webctrl.modules.ESID.Percentile_IMG?category=" + category + "&data=" + data + "&dobM=" + dobM + "&dobY=" + dobY + "&dates=" + dates + "&sex=" + gender + "&svg=1' width='1000' height='400'></embed>\"); ");	
//        printWriter.println( "      }else{ " ) ;	
//        // alternativ jpg anzeigen	
//        printWriter.println( "          document.writeln(\"<IMG SRC='/EIDPWebApp/servlet/com.eidp.webctrl.modules.ESID.Percentile_IMG?category=" + category + "&data=" + data + "&dobM=" + dobM + "&dobY=" + dobY + "&dates=" + dates + "&sex=" + gender + "&svg=0'>\"); ") ;	
//        printWriter.println( "          document.writeln(\"<br>Your browser is not able to show SVG-Graphics!<br>\"); ") ;	
//        printWriter.println( "          document.writeln(\"You need a Plug-In, that you can get <b><u><a class='a_svg' href='javascript:openSVG();'>HERE</a></u></b>.<br>\"); ") ;	
//        printWriter.println( "          document.writeln(\"After installation you will be able to zoom into the graphics (right mouseclick on the chart -> Zoom in).<br>\"); ") ;	
//        printWriter.println( "          document.writeln(\"If you have problems with/after the installation of the SVG-Viewer, you can find help <b><u><a class='a_svg' href='javascript:openSVGHelp();'>HERE</a></u></b>.<br>\"); ") ;	
//        printWriter.println( "      }" ) ;	
//        printWriter.println( "  }else{ " ) ;	
//        printWriter.println( "          document.writeln(\"<object data='/EIDPWebApp/servlet/com.eidp.webctrl.modules.ESID.Percentile_IMG?category=" + category + "&data=" + data + "&dobM=" + dobM + "&dobY=" + dobY + "&dates=" + dates + "&sex=" + gender + "&svg=1' type='image/svg+xml' width='1000' height='400'>\"); ") ;	
//        printWriter.println( "          document.writeln(\"    <param name='src' value='" + category + ".svg'>\"); ") ;	
//        // alternativ jpg anzeigen	
//        printWriter.println( "          document.writeln(\"<IMG SRC='/EIDPWebApp/servlet/com.eidp.webctrl.modules.ESID.Percentile_IMG?category=" + category + "&data=" + data + "&dobM=" + dobM + "&dobY=" + dobY + "&dates=" + dates + "&sex=" + gender + "&svg=0'>\"); ") ;	
//        printWriter.println( "          document.writeln(\"<br>Your browser is not able to show SVG-Graphics!<br>\"); ") ;	
//        printWriter.println( "          document.writeln(\"You need a Plug-In, that you can get <b><u><a class='a_svg' href='javascript:openSVG();'>HERE</a></u></b>.<br>\"); ") ;	
//        printWriter.println( "          document.writeln(\"After installation you will be able to zoom into the graphics (right mouseclick on the chart -> Zoom in).<br>\"); ") ;	
//        printWriter.println( "          document.writeln(\"If you have problems with/after the installation of the SVG-Viewer, you can find help <b><u><a class='a_svg' href='javascript:openSVGHelp();'>HERE</a></u></b>.<br>\"); ") ;	
//        printWriter.println( "          document.writeln(\"</object>\"); ") ;	
//        printWriter.println( "  } ") ;	
        printWriter.println( "</script> " ) ;	
        printWriter.println( "      </td>" ) ;	
        printWriter.println( "  </tr>" ) ;	
//        printWriter.println( "  <tr>" ) ;	
//        printWriter.println( "      <td align=\"right\">" ) ;	
//        printWriter.println( "          supported by milupa&reg;" ) ;	
//        printWriter.println( "      </td>" ) ;	
//        printWriter.println( "  </tr>" ) ;	
        printWriter.println( "</table>" ) ;	
        printWriter.println( "<br>" ) ;	
        printWriter.println( "<br>" ) ;	
    }	
}	
	
