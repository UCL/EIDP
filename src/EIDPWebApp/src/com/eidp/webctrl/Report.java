/*
 * Report.java
 *
 * Created on 8. Juli 2004, 10:53
 */

package com.eidp.webctrl;

import com.eidp.Generator.RTFGenerator ;
import java.io.PrintWriter;
import com.eidp.xml.XMLDataAccess;
import com.eidp.UserScopeObject.UserScopeObject ;
import com.eidp.core.DB.DBMapping;
import com.eidp.webctrl.modules.Report.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.* ;
import org.apache.soap.util.xml.DOM2Writer ;
import java.io.File ;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.StringWriter ;
import java.util.HashMap ;
import java.util.Vector ;
import java.util.Properties;
import javax.naming.Context ;
import javax.naming.InitialContext ;
import javax.transaction.UserTransaction ;

import java.util.Date ;

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

public class Report {
    
    String reportDestinationFolder = "";
    String reportFilenameTable = "";
    boolean export = false;
    String exportfilename = "";
    UserTransaction ut = null ;
    
    /** Creates a new instance of Report */
    public Report( String reportName , String reportType , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws java.io.IOException , javax.servlet.ServletException , java.sql.SQLException , org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException , java.io.IOException {
        
        try {
            Context cntx = new InitialContext() ;
            this.ut = (UserTransaction)cntx.lookup( "java:comp/UserTransaction" ) ;
            this.ut.begin() ;
        } catch ( javax.naming.NamingException e ) {
            throw new javax.servlet.ServletException( "Could not establish UserTransaction: " + e ) ;
        } catch ( javax.transaction.NotSupportedException e ) {
            throw new javax.servlet.ServletException( "Transaction not supported: " + e ) ;
        } catch ( javax.transaction.SystemException e ) {
            throw new javax.servlet.ServletException( "Transaction SystemException: " + e ) ;
        }
        
        uso.userCenter = (String)uso.eidpWebAppCache.sessionData_get( "userCenter" ) ;
        // uso.dbMapper = (DBMappingRemote)((Handle)uso.session.getAttribute( "dbMapperHandle" )).getEJBObject() ;
        
        DBMapping dbMapper = null ;
        try {
            Properties prop = new Properties() ;
//            prop.setProperty( "org.omg.CORBA.ORBInitialHost" , "localhost" ) ;
//            prop.setProperty( "org.omg.CORBA.ORBInitialPort" , "33365" ) ;
            Context jndiContext = new InitialContext( prop ) ;
            dbMapper = (DBMapping) jndiContext.lookup("DBMapping");
            dbMapper.setApplicationContext(uso.applicationContext);
        } catch ( javax.naming.NamingException e ) {
            throw new javax.servlet.ServletException( "" + e ) ;
        }
        
        String xmlfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/reports.xml" ;
        uso.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
        Vector reportKostenStelle = new Vector() ;
        reportKostenStelle = uso.xmlDataAccess.getElementsByName( "report,report-kostenstelle" ) ;
        Vector reportDestinationFolderName = new Vector() ;
        reportDestinationFolderName = uso.xmlDataAccess.getElementsByName( "report,report-destination-folder" ) ;
        if( reportDestinationFolderName.size() > 0 && reportKostenStelle.size() > 0 ){
            this.reportDestinationFolder = (String)(reportDestinationFolderName).get( 0 ) ;
            Vector reportFilenameTableSource = new Vector() ;
            reportFilenameTableSource = uso.xmlDataAccess.getElementsByName( "report,report-filename-table-source" ) ;
            this.reportFilenameTable = (String)(reportFilenameTableSource).get( 0 ) ;
            String strReportKostenStelle = (String)(reportKostenStelle).get( 0 ) ;
            String strFormAction = (String)uso.eidpWebAppCache.sessionData_get( "moduleAction" ) ;
            if( strFormAction != null && strFormAction.trim().equals("export") )
                this.export = true;
            
            if( this.export ){
                HashMap paramMap = new HashMap() ;
                
                // Hier wird geschaut, ob bestimmte Tags im report angegeben sind und diese werden dann
                // in die Variablen geschrieben. Ansonsten werden Standardwerte genommen.
                
                String reportFilenameMethod = "getReportFileNameDataForVisitID" ;
                String reportFilenamePrimaryKey = "" ;
                String reportFilenameSecondaryKey = "VisitID" ;
                String reportFilenameSecondaryKeyField = "visit_id" ;
                String reportFilenameTertiaryKeyField = "asspatloc" ;
                
                Vector reportFilenameMethodVector = uso.xmlDataAccess.getElementsByName( "report,report-filename-method" ) ;
                Vector reportFilenamePrimaryKeyVector = uso.xmlDataAccess.getElementsByName( "report,report-filename-primary-key" ) ;
                Vector reportFilenameSecondaryKeyVector = uso.xmlDataAccess.getElementsByName( "report,report-filename-secondary-key" ) ;
                Vector reportFilenameSecondaryKeyFieldVector = uso.xmlDataAccess.getElementsByName( "report,report-filename-secondary-key-field" ) ;
                Vector reportFilenameTertiaryKeyFieldVector = uso.xmlDataAccess.getElementsByName( "report,report-tertiary-key-field" ) ;
                
                if ( reportFilenameMethodVector.size() > 0 ) {
                    reportFilenameMethod = (String)reportFilenameMethodVector.get( 0 ) ;
                }
                if ( reportFilenamePrimaryKeyVector.size() > 0 ) {
                    reportFilenamePrimaryKey = (String)reportFilenamePrimaryKeyVector.get( 0 ) ;
                }
                if ( reportFilenameSecondaryKeyVector.size() > 0 ) {
                    reportFilenameSecondaryKey = (String)reportFilenameSecondaryKeyVector.get( 0 ) ;
                }
                if ( reportFilenameSecondaryKeyFieldVector.size() > 0 ) {
                    reportFilenameSecondaryKeyField = (String)reportFilenameSecondaryKeyFieldVector.get( 0 ) ;
                }
                if ( reportFilenameTertiaryKeyFieldVector.size() > 0 ) {
                    reportFilenameTertiaryKeyField = (String)reportFilenameTertiaryKeyFieldVector.get( 0 ) ;
                }
                
                if ( ! reportFilenamePrimaryKey.equals("")) {
                    paramMap.put( "patient_id" , (String)uso.eidpWebAppCache.sessionData_get( reportFilenamePrimaryKey ) ) ;
                }
                paramMap.put( reportFilenameSecondaryKeyField , (String)uso.eidpWebAppCache.sessionData_get( reportFilenameSecondaryKey ) ) ;
                paramMap.put( reportFilenameTertiaryKeyField , strReportKostenStelle ) ;
                
                dbMapper.DBAction( this.reportFilenameTable , reportFilenameMethod , paramMap ) ;
                
                Vector allRowsOfVisit = dbMapper.getRowRange( 0 , dbMapper.size() ) ;
                String REPORT = "";
                if( allRowsOfVisit.size() > 0 ){
                    String ADMSOURCE = (String)((HashMap)allRowsOfVisit.get( 0 ) ).get( "admsource" );
                    if( ADMSOURCE.trim().length() > 0 ){
                        String VISITNUM = (String)((HashMap)allRowsOfVisit.get( 0 ) ).get( "visitnum" );
                        REPORT = (String)((HashMap)allRowsOfVisit.get( 0 ) ).get( "report" );
                        if( !REPORT.trim().equals("1") ){
                            this.exportfilename = this.getFileName( ADMSOURCE , VISITNUM );
                            paramMap.put( "report" , "1" ) ;
                            dbMapper.DBAction( this.reportFilenameTable , "setReportForVisitID" , paramMap ) ;
                            try {
                                this.ut.commit() ;
                            } catch ( javax.transaction.RollbackException e ) {
                                throw new javax.servlet.ServletException( "Transaction commit failed due to RollbackException" + e  ) ;
                            } catch ( javax.transaction.HeuristicMixedException e ) {
                                throw new javax.servlet.ServletException( "Transaction commit failed due to HeuristicMixed" + e  ) ;
                            } catch ( javax.transaction.HeuristicRollbackException e ) {
                                throw new javax.servlet.ServletException( "Transaction commit failed due to HeuristicRollback" + e  ) ;
                            } catch ( javax.transaction.SystemException e ) {
                                throw new javax.servlet.ServletException( "Transaction commit failed due to SystemException" + e  ) ;
                            }
                            this.processRequest( reportName , reportType , false , request , response , uso ) ;
                            response.sendRedirect( "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=AddOn;controller;Report.MESSAGE_ReportExport;show" ) ;
                        }else{
                            response.sendRedirect( "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=AddOn;controller;Report.ERROR_ReportExists;show" ) ;
                        }
                    }else{
                        response.sendRedirect( "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=AddOn;controller;Report.ERROR_ReportNotDefined;show" ) ;
                    }
                }else{
                    response.sendRedirect( "/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=AddOn;controller;Report.ERROR_ReportNotDefined;show" ) ;
                }
            }else{
                this.processRequest( reportName , reportType , true , request , response , uso ) ;
            }
        }else{
            this.processRequest( reportName , reportType , true , request , response , uso ) ;
        }
    }
    
    private void processRequest( String report , String reportType , boolean isDraft , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws java.io.IOException , java.sql.SQLException , javax.servlet.ServletException , java.rmi.RemoteException{
        // get report by reportName
        try {
            Vector reportVector = new Vector() ;
            reportVector = uso.xmlDataAccess.getNodeListsByName( "report" ) ;
            Vector reportName = new Vector() ;
            reportName = uso.xmlDataAccess.getElementsByName( "report,report-name" ) ;
            NodeList reportNode = null ;
            if ( reportVector.size() != reportName.size() ) {
                throw new org.xml.sax.SAXException( "Reports and report names do not match ( reports: " + reportVector.size() + "; names: " + reportName.size() + ")." ) ;
            }
            for ( int ri = 0 ; ri < reportVector.size() ; ri++ ) {
                if ( ((String)reportName.get( ri )).equals( report ) ) {
                    reportNode = (NodeList)reportVector.get( ri ) ;
                    break ;
                }
            }
            // process the report, get the data and build the report structure
            this.generateReportStructure( reportNode , reportType , isDraft , report , request , response , uso ) ;
        } catch ( org.xml.sax.SAXException saxe ) {
            throw new javax.servlet.ServletException( "Report throws SAXException (E20010): " + saxe ) ;
        }
    }
    
    private void generateReportStructure( NodeList reportNode , String reportType , boolean isDraft , String report , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws java.io.IOException , java.sql.SQLException , org.xml.sax.SAXException , javax.servlet.ServletException , java.rmi.RemoteException{
        String rtfDocument = "" ;
        // Preloads
        // get Preload-Nodes
        Vector preloadNode = (Vector)uso.xmlDataAccess.getNodeListsByName( "preload" , reportNode ) ;
        NodeList preloadNodeList = null ;
        for ( int pi = 0 ; pi < preloadNode.size() ; pi++ ) {
            String preloadName = "" ;
            try {
                preloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "name" , (NodeList)preloadNode.get( pi ) ) ).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                throw new javax.servlet.ServletException( "No preload name given in report: " + aiobe ) ;
            }
            try {
                uso.sharedMethods.PreLoad( (NodeList)preloadNode.get( pi ) , preloadName , uso , request ) ;
            } catch ( java.sql.SQLException sqle ) {
                throw new javax.servlet.ServletException( "Report throws SQLExeption (E200020): " + sqle ) ;
            } catch ( java.io.IOException ioe ) {
                throw new javax.servlet.ServletException( "Report throws IOException (E200021): " + ioe ) ;
            }
            ((Element)reportNode).removeChild( (Node)preloadNode.get( pi ) ) ;
        }
        // read and build reportElement Sections
        // 1. header
        Vector reportHeaderVector = new Vector() ;
        reportHeaderVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "header", reportNode ) ;
        if( reportHeaderVector.size() > 0 ){
            reportNode = generateHeader( reportNode , uso ) ;
        }
        // 2. footer
        reportNode = generateSections( reportNode , uso , true ) ;
        // 3. sections
        reportNode = generateSections( reportNode , uso , false ) ;
        // 4. generate RTF
        //        System.out.println( reportNode.toString() ) ;
        try {
            RTFGenerator rtfGenerator = new RTFGenerator( (Element)reportNode , isDraft ) ;
            rtfDocument = (String)rtfGenerator.getGeneratedDocument() ;
        } catch ( javax.xml.parsers.ParserConfigurationException pce ) {
            throw new javax.servlet.ServletException( "Report throws ParserConfigurationException: " + pce ) ;
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "Report throws IOException: " + ioe ) ;
        }
        
        if(!this.export){
            this.printDocument( rtfDocument , response ) ;
        }else{
            if( !this.exportfilename.equals("") ){
                this.saveDocument( rtfDocument ) ;
            }else{
                throw new javax.servlet.ServletException( "Report throws ServletException in saveDocument: Report has no name!" ) ;
            }
        }
        
    }
    
    private NodeList generateHeader( NodeList reportNode , UserScopeObject uso ) throws org.xml.sax.SAXException , javax.servlet.ServletException {
        Vector sectionVector = new Vector() ;
        sectionVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "header" , reportNode ) ;
        Element root = null,root1 = null,item = null;
        
        StringWriter xmlWriter = new StringWriter() ;
        
        root = uso.xmlDataAccess.createElement("header");
        
        for(Node child = ((Node)sectionVector.get(0)).getFirstChild(); child != null; child = child.getNextSibling()) {
            String outertagname = "";
            outertagname = child.getNodeName();
            if( !outertagname.trim().equals("#text") ){
                
                root1 = uso.xmlDataAccess.createElement(outertagname);
                
                NodeList childnodes = child.getChildNodes();
                for(int i = 0; i < childnodes.getLength() ; i++) {
                    String innertagname =  "";
                    innertagname = childnodes.item(i).getNodeName();
                    if( !innertagname.trim().equals("#text") ){
                        Vector contentsVector = new Vector() ;
                        contentsVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "header," + outertagname + "," + innertagname , reportNode ) ;
                        ((Element)childnodes).removeChild( (Node)contentsVector.get( 0 ) ) ;
                        root1.appendChild((Node)generateTextContents( (NodeList)contentsVector.get(0) , uso ));
                        
                        root.appendChild(root1);
                    }
                }
            }
        }
        DOM2Writer.serializeAsXML( (Node)root , xmlWriter ) ;
        
        // remove node
        ((Element)reportNode).removeChild( (Node)sectionVector.get( 0 ) ) ;
        ((Element)reportNode).appendChild( root ) ;
        return reportNode ;
    }
    
    private NodeList generateSections( NodeList reportNode , UserScopeObject uso , boolean footer ) throws org.xml.sax.SAXException , javax.servlet.ServletException {
        Vector sectionVector = new Vector() ;
        if ( footer == true ) {
            sectionVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "footer" , reportNode ) ;
        } else {
            sectionVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "section" , reportNode ) ;
        }
        //        try{
        //                  Logger log = new Logger("/com/eidp/rheuma/code.log");
        for ( int si = 0 ; si < sectionVector.size() ; si++ ) {
            
            Vector sectionShow = new Vector() ;
            sectionShow = (Vector)uso.xmlDataAccess.getNodeListsByName( "section-show" , (NodeList)sectionVector.get( si ) ) ;
            Vector BooleanType = new Vector() ;
            BooleanType = (Vector)uso.xmlDataAccess.getElementsByName( "section-show-boolean-type" , (NodeList)sectionVector.get( si ) ) ;
            String strSectionBooleanType = "AND";
            if ( BooleanType.size() > 0 ) {
                strSectionBooleanType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "section-show-boolean-type" , (NodeList)sectionVector.get( si ) ) ).get( 0 ) ;
            }
            boolean boolsectionShow = true;
            
            if ( sectionShow.size() > 0 ) {
                
                for ( int ss = 0 ; ss < sectionShow.size() ; ss++ ) {
                    
                    boolsectionShow = true ;
                    String refValue = "" ;
                    Vector preloadName = new Vector() ;
                    preloadName = (Vector)uso.xmlDataAccess.getElementsByName( "section-preload-name" , (NodeList)sectionShow.get( ss ) ) ;
                    
                    if ( preloadName.size() > 0 ) {
                        
                        String strPreloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "section-preload-name" , (NodeList)sectionShow.get( ss ) ) ).get( 0 ) ;
                        
                        try {
                            refValue = uso.sharedMethods.getReferenceValue( (NodeList)sectionShow.get( ss ) , strPreloadName , 0 , uso ) ;
                            //                            log.logMessage( "-strPreloadName = " + strPreloadName );
                            //                            log.logMessage( "-refValue = " + refValue );
                        } catch ( java.io.IOException ioe ) {
                            throw new javax.servlet.ServletException( "Report throws IOException in generateReportStructure: " + ioe ) ;
                        }
                        
                        Vector sectionFunction = new Vector() ;
                        sectionFunction = (Vector)uso.xmlDataAccess.getElementsByName( "section-function" , (NodeList)sectionShow.get( ss ) ) ;
                        
                        if ( sectionFunction.size() > 0 ) {
                            Vector sectionFunctionParameter = new Vector() ;
                            sectionFunctionParameter = (Vector)uso.xmlDataAccess.getElementsByName( "section-function-parameter" , (NodeList)sectionShow.get( ss ) ) ;
                            String strSectionFunctionParameter = "" ;
                            if ( sectionFunctionParameter.size() > 0 ) {
                                strSectionFunctionParameter = (String)((Vector)uso.xmlDataAccess.getElementsByName( "section-function-parameter" , (NodeList)sectionShow.get( ss ) ) ).get( 0 ) ;
                            }
                            ReportSectionBooleanFunctions rsbf = new ReportSectionBooleanFunctions();
                            String strSectionFunction = (String)((Vector)uso.xmlDataAccess.getElementsByName( "section-function" , (NodeList)sectionShow.get( ss ) ) ).get( 0 ) ;
                            if( strSectionFunction.equals("getBooleanValueForPhysicianLetterToPatient") ){
                                boolsectionShow = rsbf.getBooleanValueForPhysicianLetterToPatient( refValue ) ;
                            }else if( strSectionFunction.equals("getBooleanValueForPhysicianLetterToHomePhysician") ){
                                boolsectionShow = rsbf.getBooleanValueForPhysicianLetterToHomePhysician( refValue ) ;
                            }else if( strSectionFunction.equals("getBooleanValueForPhysicianLetterToPatientAndHomePhysician") ){
                                boolsectionShow = rsbf.getBooleanValueForPhysicianLetterToPatientAndHomePhysician( refValue ) ;
                            }else if( strSectionFunction.equals("getBooleanValueForVisitDate") ){
                                boolsectionShow = rsbf.getBooleanValueForVisitDate( refValue ) ;
                            }else if( strSectionFunction.equals("getFalseBooleanValueForVisitDate") ){
                                boolsectionShow = rsbf.getFalseBooleanValueForVisitDate( refValue ) ;
                            }else if( strSectionFunction.equals("getBooleanValueForDatasetIsNotEmpty") ){
                                boolsectionShow = rsbf.getBooleanValueForDatasetIsNotEmpty( refValue ) ;
                            }else if( strSectionFunction.equals("getBooleanValueForDatasetIsEmpty") ){
                                boolsectionShow = rsbf.getBooleanValueForDatasetIsEmpty( refValue ) ;
                            }else if( strSectionFunction.equals("getBooleanValueForSexOfPatient") ){
                                boolsectionShow = rsbf.getBooleanValueForSexOfPatient( refValue , strSectionFunctionParameter ) ;
                            }else if( strSectionFunction.equals("getBooleanValueForSexOfPhysician") ){
                                boolsectionShow = rsbf.getBooleanValueForSexOfPhysician( refValue , strSectionFunctionParameter ) ;
                            }
                        }
                        
                    }
                    //                    else{
                    //                        if( ( (String)((Vector)uso.xmlDataAccess.getElementsByName( "section-show" , (NodeList)sectionShow.get( ss ) ) ).get( 0 ) ).equals("false") ){
                    //                            boolsectionShow = false ;
                    //                        }
                    //                    }
                    
                    // strSectionBooleanType = AND: falls eine der Bedingungen nicht erf�llt wurde, wird die section ausgeblendet
                    // strSectionBooleanType = OR: falls eine der Bedingungen erf�llt wurde, wird die section eingeblendet
                    if( strSectionBooleanType.equals( "OR" ) ){
                        if( boolsectionShow == true ){
                            break;
                        }
                    }else{
                        if( boolsectionShow == false ){
                            break;
                        }
                    }
                }
            }
            // remove node
            ((Element)reportNode).removeChild( (Node)sectionVector.get( si ) ) ;
            
            // create new section node if "section-show"-tag equals true
            if( boolsectionShow ){
                String sectionType = "" ;
                if ( footer == true ) {
                    sectionType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "footer-type" , (NodeList)sectionVector.get( si ) ) ).get( 0 ) ;
                } else {
                    sectionType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "section-type" , (NodeList)sectionVector.get( si ) ) ).get( 0 ) ;
                }
                uso.multiFlag = false ;
                if ( sectionType.equals( "text" ) ) {
                    sectionVector.set( si , this.generateTextContents( (NodeList)sectionVector.get( si ) , uso ) ) ;
                } else if ( sectionType.equals( "table" ) ) {
                    sectionVector.set( si , this.generateTables( reportNode , (NodeList)sectionVector.get( si ) , uso ) ) ;
                } else if ( sectionType.equals( "list" ) ) {
                    uso.multiFlag = true ;
                    sectionVector.set( si , this.generateList( (NodeList)sectionVector.get( si ) , uso ) ) ;
                } else if ( sectionType.equals( "HIV_Verlauf" ) ) {
                    uso.multiFlag = true ;
                    EIDPReportModuleLoader eidprepmodloader = new EIDPReportModuleLoader() ;
                    try{
                        sectionVector.set( si , eidprepmodloader.LoadClass( "HIV_Verlauf", reportNode , uso ) ) ;
                    }catch(javax.xml.parsers.ParserConfigurationException e){
                    }
                } else if ( sectionType.equals( "RHEUMA_Verlauf" ) ) {
                    uso.multiFlag = true ;
                    EIDPReportModuleLoader eidprepmodloader = new EIDPReportModuleLoader() ;
                    try{
                        sectionVector.set( si , eidprepmodloader.LoadClass( "RHEUMA_Verlauf", reportNode , uso ) ) ;
                    }catch(javax.xml.parsers.ParserConfigurationException e){
                    }
                } else if ( sectionType.equals( "RHEUMA_NZ_Verlauf" ) ) {
                    uso.multiFlag = true ;
                    EIDPReportModuleLoader eidprepmodloader = new EIDPReportModuleLoader() ;
                    try{
                        sectionVector.set( si , eidprepmodloader.LoadClass( "RHEUMA_NZ_Verlauf", reportNode , uso ) ) ;
                    }catch(javax.xml.parsers.ParserConfigurationException e){
                    }
                }
                ((Element)reportNode).appendChild( (Node)sectionVector.get( si ) ) ;
            }
        }
        //                }catch(java.io.IOException e){}
        return reportNode ;
    }
    
    private NodeList generateTables( NodeList reportNode , NodeList sectionNode , UserScopeObject uso ) throws org.xml.sax.SAXException , javax.servlet.ServletException {
        ReportDOMFunctions rdf = new ReportDOMFunctions();
        NodeList returnEmptyNode;
        NodeList tableHeaderSectionNode = null;
        returnEmptyNode = rdf.createEmptySectionNode( uso.xmlDataAccess ) ;
        
        // create TableHeaderSectionNode
        Vector headerVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "table-header" , sectionNode ) ;
        if( headerVector.size() > 0 ){
            String strTableHeader = (String)((Vector)uso.xmlDataAccess.getElementsByName( "table-header" , sectionNode ) ).get( 0 ) ;
            tableHeaderSectionNode = rdf.createTableHeaderSectionNode( strTableHeader , uso.xmlDataAccess ) ;
        }
        
        Vector rowVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "row" , sectionNode ) ;
        Vector rowCountVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "rowcount" , sectionNode ) ;
        int rowCount = 0 ;
        if( rowCountVector.size() > 0 ){
            rowCount = Integer.parseInt( (String)((Vector)uso.xmlDataAccess.getElementsByName( "rowcount" , sectionNode ) ).get( 0 ) );
        }
        
        for ( int ri = 0 ; ri < rowVector.size() ; ri++ ) {
            
            Vector rowShowVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "row-show" , (NodeList)rowVector.get( ri ) ) ;
            Vector referenceVector = new Vector() ;
            if( rowShowVector.size() > 0 )
                referenceVector = uso.xmlDataAccess.getElementsByName( "reference" , (NodeList)rowShowVector.get( 0 ) ) ;
            
            String strCellSumValue = "" ;
            String strCellValue = "" ;
            boolean boolRowShow = false ;
            
            for ( int ai = 0 ; ai < referenceVector.size() ; ai++ ) {
                NodeList referenceNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "reference" , (NodeList)rowShowVector.get( 0 ) ) ).get( ai ) ;
                String preloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "preload-name" , referenceNode ) ).get( 0 ) ;
                try {
                    // Spezialfunktion, um die $-LaborValues herauszufiltern (muss mal ge�ndert werden),
                    // da jetzt in keiner Zelle mehr ein Dollarzeichen stehen darf!
                    // bzw.: der Inhalt der Zelle wird in diesem Fall gel�scht
                    strCellValue = uso.sharedMethods.getReferenceValue( referenceNode , preloadName , ri , uso );
                    if( strCellValue.indexOf( "$" ) != -1 )
                        strCellValue = "" ;
                    strCellSumValue += strCellValue ;
                } catch ( java.io.IOException e ) {
                    throw new javax.servlet.ServletException( "IOException thrown when calling getReferenceValue from generateList: " + e ) ;
                }
            }
            
            ((Element)sectionNode ).removeChild( (Node)rowVector.get( ri ) ) ;
            
            if( strCellSumValue.trim().length() > 0 )
                boolRowShow = true ;
            else
                if( rowCountVector.size() > 0 ){
                rowCount-- ;
                //                    try{
                //                      Logger log = new Logger("/com/eidp/rheuma/code.log");
                //                      log.logMessage( "rowCount = " + rowCount );
                //                     }catch(java.io.IOException e){}
                }
            
            if( boolRowShow || rowShowVector.size() == 0 ){
                Vector columnVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "column" , (NodeList)rowVector.get( ri ) ) ;
                for ( int ci = 0 ; ci < columnVector.size() ; ci++ ) {
                    ((Element)rowVector.get( ri ) ).removeChild( (Node)columnVector.get( ci ) ) ;
                    columnVector.set( ci , this.generateTextContents( (NodeList)columnVector.get( ci ) , uso ) ) ;
                    ((Element)rowVector.get( ri )).appendChild( (Node)columnVector.get( ci ) ) ;
                }
                ((Element)sectionNode ).appendChild( (Node)rowVector.get( ri ) ) ;
            }
        }
        
        if( headerVector.size() > 0 ){
            if( rowCountVector.size() > 0 ){
                if( rowCount == 0 ){
                    return returnEmptyNode ;
                }else{
                    ((Element)reportNode).appendChild((Node)tableHeaderSectionNode) ;
                    return sectionNode ;
                }
            }else{
                ((Element)reportNode).appendChild((Node)tableHeaderSectionNode) ;
                return sectionNode ;
            }
        }else{
            if( rowCountVector.size() > 0 ){
                if( rowCount == 0 ){
                    return returnEmptyNode ;
                }else{
                    return sectionNode ;
                }
            }else{
                return sectionNode ;
            }
        }
        
    }
    
    private NodeList generateTextContents( NodeList sectionNode , UserScopeObject uso ) throws org.xml.sax.SAXException , javax.servlet.ServletException {
        Vector contentsVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "contents" , sectionNode ) ;
        for ( int i = 0 ; i < contentsVector.size() ; i++ ) {
            boolean boolcontentShow = true ;
            String refValue = "" ;
            
            Vector contentShow = new Vector() ;
            contentShow = (Vector)uso.xmlDataAccess.getElementsByName( "content-show" , (NodeList)contentsVector.get( i ) ) ;
            
            if ( contentShow.size() > 0 ) {
                
                Vector preloadName = new Vector() ;
                preloadName = (Vector)uso.xmlDataAccess.getElementsByName( "content-preload-name" , (NodeList)contentsVector.get( i ) ) ;
                
                if ( preloadName.size() > 0 ) {
                    
                    String strPreloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "content-preload-name" , (NodeList)contentsVector.get( i ) ) ).get( 0 ) ;
                    
                    try {
                        refValue = uso.sharedMethods.getReferenceValue( (NodeList)contentsVector.get( i ) , strPreloadName , 0 , uso ) ;
                    } catch ( java.io.IOException ioe ) {
                        throw new javax.servlet.ServletException( "Report throws IOException in generateReportStructure: " + ioe ) ;
                    }
                    
                    Vector contentFunction = new Vector() ;
                    contentFunction = (Vector)uso.xmlDataAccess.getElementsByName( "content-function" , (NodeList)contentsVector.get( i ) ) ;
                    
                    if ( contentFunction.size() > 0 ) {
                        Vector contentFunctionParameter = new Vector() ;
                        contentFunctionParameter = (Vector)uso.xmlDataAccess.getElementsByName( "content-function-parameter" , (NodeList)contentsVector.get( i ) ) ;
                        String strContentFunctionParameter = "" ;
                        if ( contentFunctionParameter.size() > 0 ) {
                            strContentFunctionParameter = (String)((Vector)uso.xmlDataAccess.getElementsByName( "content-function-parameter" , (NodeList)contentsVector.get( i ) ) ).get( 0 ) ;
                        }
                        ReportSectionBooleanFunctions rsbf = new ReportSectionBooleanFunctions();
                        String strContentFunction = (String)((Vector)uso.xmlDataAccess.getElementsByName( "content-function" , (NodeList)contentsVector.get( i ) ) ).get( 0 ) ;
                        if( strContentFunction.equals("getBooleanValueForDatasetIsNotEmpty") ){
                            boolcontentShow = rsbf.getBooleanValueForDatasetIsNotEmpty( refValue ) ;
                        }else if( strContentFunction.equals("getBooleanValueForDatasetIsEmpty") ){
                            boolcontentShow = rsbf.getBooleanValueForDatasetIsEmpty( refValue ) ;
                        }
                    }
                    
                }else{
                    if( ( (String)((Vector)uso.xmlDataAccess.getElementsByName( "content-show" , (NodeList)contentsVector.get( i ) ) ).get( 0 ) ).equals("false") ){
                        boolcontentShow = false ;
                    }
                }
            }
            
            // remove node
            ((Element)sectionNode ).removeChild( (Node)contentsVector.get( i ) ) ;
            
            // create new section node if "content-show"-tag equals true
            if( boolcontentShow ){
                
                Vector referenceVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "reference" , (NodeList)contentsVector.get( i ) ) ;
                for ( int ri = 0 ; ri < referenceVector.size() ; ri++ ) {
                    String preloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "preload-name" , (NodeList)referenceVector.get( ri ) ) ).get( 0 ) ;
                    refValue = "" ;
                    try {
                        refValue = uso.sharedMethods.getReferenceValue( (NodeList)referenceVector.get( ri ) , preloadName , 0 , uso ) ;
                    } catch ( java.io.IOException ioe ) {
                        throw new javax.servlet.ServletException( "Report throws IOException in generateReportStructure: " + ioe ) ;
                    }
                    Element textNode = uso.xmlDataAccess.createElement( "text" ) ;
                    Text textValue = uso.xmlDataAccess.createTextNode( (String)refValue ) ;
                    textNode.appendChild( textValue ) ;
                    ((Element)contentsVector.get( i )).appendChild( textNode ) ;
                }
                ((Element)sectionNode ).appendChild( (Node)contentsVector.get( i ) ) ;
            }
        }
        return sectionNode ;
    }
    
    private void generateLabelTextContents( NodeList labelNode , UserScopeObject uso ) throws org.xml.sax.SAXException , javax.servlet.ServletException {
        
        Vector referenceVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "label-reference" , labelNode ) ;
        
        for ( int ri = 0 ; ri < referenceVector.size() ; ri++ ) {
            String preloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "preload-name" , (NodeList)referenceVector.get( ri ) ) ).get( 0 ) ;
            String refValue = "" ;
            
            try {
                refValue = uso.sharedMethods.getReferenceValue( (NodeList)referenceVector.get( ri ) , preloadName , 0 , uso ) ;
            } catch ( java.io.IOException ioe ) {
                throw new javax.servlet.ServletException( "Report throws IOException in generateReportStructure: " + ioe ) ;
            }
            Element textNode = uso.xmlDataAccess.createElement( "text" ) ;
            Text textValue = uso.xmlDataAccess.createTextNode( (String)refValue ) ;
            textNode.appendChild( textValue ) ;
            ((Element)referenceVector.get( ri )).appendChild( textNode ) ;
            
        }
        
    }
    
    private NodeList generateList( NodeList sectionNode , UserScopeObject uso ) throws javax.servlet.ServletException , org.xml.sax.SAXException {
        // section type to table
        NodeList sectionType = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "section-type" , sectionNode ) ).get( 0 ) ;
        ((Element)sectionNode ).removeChild( (Node)sectionType ) ;
        Element sectionTypeNode = uso.xmlDataAccess.createElement( "section-type" ) ;
        Text textSectionType = uso.xmlDataAccess.createTextNode( "table" ) ;
        sectionTypeNode.appendChild( textSectionType ) ;
        ((Element)sectionNode ).appendChild( sectionTypeNode ) ;
        int listStartElement = 0 ;
        int listSizeMax = 0 ;
        int listSize = 0 ;
        String listSizePreload = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-size-from-preload" , sectionNode ) ).get( 0 ) ;
        Vector listSizeMaxVector = (Vector)uso.xmlDataAccess.getElementsByName( "list-size-max" , sectionNode ) ;
        Vector listSizeMaxReverse = (Vector)uso.xmlDataAccess.getElementsByName( "list-size-max-reverse" , sectionNode ) ;
        String lsmr = "false" ;
        if ( listSizeMaxReverse.size() > 0 ) {
            lsmr = (String) listSizeMaxReverse.get( 0 ) ;
        }
        if ( lsmr.equals( "true" ) ) {
            listSizeMax = Integer.parseInt( (String)listSizeMaxVector.get( 0 ) ) ;
            listSize = ((Vector)uso.preLoadData.get( listSizePreload )).size() ;
            listStartElement = listSize - listSizeMax ;
            if ( listStartElement < 0 ) {
                listStartElement = 0 ;
            }
        } else {
            if ( listSizeMaxVector.size() > 0 ) {
                listSizeMax = Integer.parseInt( (String)listSizeMaxVector.get( 0 ) ) ;
            }
            listSize = ((Vector)uso.preLoadData.get( listSizePreload )).size() ;
            if ( listSize > listSizeMax && listSizeMax != 0 ) {
                listSize = listSizeMax ;
            }
        }
        // ========== LABELS
        NodeList rowNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "row" , sectionNode ) ).get( 0 ) ;
        Vector columnVector = uso.xmlDataAccess.getNodeListsByName( "column" , rowNode ) ;
        for ( int ci = 0 ; ci < columnVector.size() ; ci++ ) {
            // ContentsVector
            NodeList contentsNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "contents" , (NodeList)columnVector.get( ci ) ) ).get( 0 ) ;
            NodeList labelsNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "label" , contentsNode ) ).get( 0 ) ;
            
            try{
                String labelReference = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label-reference" , labelsNode ) ).get( 0 ) ;
                generateLabelTextContents( labelsNode, uso ) ;
            }catch( java.lang.ArrayIndexOutOfBoundsException e ){
                String label = (String)((Vector)uso.xmlDataAccess.getElementsByName( "label" , contentsNode ) ).get( 0 ) ;
                Element labelEl = uso.xmlDataAccess.createElement( "text" ) ;
                Text labelText = uso.xmlDataAccess.createTextNode( label ) ;
                labelEl.appendChild( labelText ) ;
                ((Element)contentsNode).appendChild( labelEl ) ;
            }
        } // == Rest der Liste
        Vector rowElVector = new Vector() ;
        System.out.println( "listStartElement = " + listStartElement + "; listSize = " + listSize );
        // liRef = li auf 0 bezogen (wird nach jedem Schleifendurchlauf hochgesetzt
        // liRef wird benoetigt, da rowElVector neu gebaut wird. rowElVector hat jedoch
        // bei einem listStartElement > 0 keine Eintroege
        // listStartElement = 15; li = 15; Element in rowElVector = 0
        // Dadurch wuerde eine ArrayIndexOutOfBoundsException entstehen.
        int liRef = 0 ;
        for ( int li = listStartElement ; li < listSize ; li++ ) {
            // new row
            rowElVector.add( uso.xmlDataAccess.createElement( "row" ) ) ;
            // columns
            for ( int ci = 0 ; ci < columnVector.size() ; ci++ ) {
                // new column node
                Element columnEl = uso.xmlDataAccess.createElement( "column" ) ;
                // 1. align
                Vector alignVector = uso.xmlDataAccess.getElementsByName( "column-align" , (NodeList)columnVector.get( ci ) ) ;
                for ( int ai = 0 ; ai < alignVector.size() ; ai++ ) {
                    // new align node
                    Element alignEl = uso.xmlDataAccess.createElement( "column-align" ) ;
                    Text alignText = uso.xmlDataAccess.createTextNode( (String)alignVector.get( ai ) ) ;
                    alignEl.appendChild( alignText ) ;
                    // append align node
                    columnEl.appendChild( alignEl ) ;
                }
                // contents nodes
                Vector contentsVector = uso.xmlDataAccess.getNodeListsByName( "contents" , (NodeList)columnVector.get( ci ) ) ;
                for ( int coni = 0 ; coni < contentsVector.size() ; coni++ ) {
                    // new contents node
                    Element contentsEl = uso.xmlDataAccess.createElement( "contents" ) ;
                    // 2. contents attributes
                    Vector attrVector = uso.xmlDataAccess.getElementsByName( "contents-attribute" , (NodeList)contentsVector.get( coni ) ) ;
                    for ( int ai = 0 ; ai < attrVector.size() ; ai++ ) {
                        // new attribute node
                        Element attrEl = uso.xmlDataAccess.createElement( "contents-attribute" ) ;
                        Text attrText = uso.xmlDataAccess.createTextNode( (String)attrVector.get( ai ) ) ;
                        attrEl.appendChild( attrText ) ;
                        // append attribute node
                        contentsEl.appendChild( attrEl ) ;
                    }
                    // 3. font attributes
                    Vector fontVector = uso.xmlDataAccess.getElementsByName( "font-size" , (NodeList)contentsVector.get( coni ) ) ;
                    for ( int ai = 0 ; ai < fontVector.size() ; ai++ ) {
                        // new font-size node
                        Element fontEl = uso.xmlDataAccess.createElement( "font-size" ) ;
                        Text fontText = uso.xmlDataAccess.createTextNode( (String)fontVector.get( ai ) ) ;
                        fontEl.appendChild( fontText ) ;
                        // append attribute node
                        contentsEl.appendChild( fontEl ) ;
                    }
                    // 4. reference OR text
                    String textValue = "" ;
                    Vector textNode = uso.xmlDataAccess.getElementsByName( "list-text" , (NodeList)contentsVector.get( coni ) ) ;
                    Vector delimiterNode = uso.xmlDataAccess.getElementsByName( "delimiter" , (NodeList)contentsVector.get( coni ) ) ;
                    if ( textNode.size() == 0 ) {
                        // reference
                        Vector referenceVector = uso.xmlDataAccess.getElementsByName( "reference" , (NodeList)contentsVector.get( coni ) ) ;
                        // Delimiter
                        String strDelim = "";
                        if( delimiterNode.size() > 0 ){
                            strDelim = (String)delimiterNode.get( 0 ) ;
                        }
                        for ( int ai = 0 ; ai < referenceVector.size() ; ai++ ) {
                            NodeList referenceNode = (NodeList)((Vector)uso.xmlDataAccess.getNodeListsByName( "reference" , (NodeList)contentsVector.get( coni ) ) ).get( ai ) ;
                            String preloadName = (String)((Vector)uso.xmlDataAccess.getElementsByName( "preload-name" , referenceNode ) ).get( 0 ) ;
                            try {
                                textValue += uso.sharedMethods.getReferenceValue( referenceNode , preloadName , li , uso ) + strDelim ;
                            } catch ( java.io.IOException e ) {
                                throw new javax.servlet.ServletException( "IOException thrown when calling getReferenceValue from generateList: " + e ) ;
                            }
                        }
                        
                        textValue = textValue.substring( 0, textValue.length() - strDelim.length() ) ;
                        
                    } else {
                        textValue = (String)textNode.get( 0 ) ;
                    }
                    // 5. functions
                    Vector textFunctions = uso.xmlDataAccess.getElementsByName( "function" , (NodeList)contentsVector.get( coni ) ) ;
                    for ( int ai = 0 ; ai < textFunctions.size() ; ai++ ) {
                        // new function node
                        Element functionEl = uso.xmlDataAccess.createElement( "function" ) ;
                        Text functionText = uso.xmlDataAccess.createTextNode( (String)textFunctions.get( ai ) ) ;
                        functionEl.appendChild( functionText ) ;
                        // append function node
                        contentsEl.appendChild( functionEl ) ;
                    }
                    Element textEl = uso.xmlDataAccess.createElement( "text" ) ;
                    Text textText = uso.xmlDataAccess.createTextNode( textValue ) ;
                    textEl.appendChild( textText ) ;
                    contentsEl.appendChild( textEl ) ;
                    // append contents
                    columnEl.appendChild( contentsEl ) ;
                }
                // append columnEl
                ((Element)rowElVector.get( liRef ) ).appendChild( columnEl ) ;
            }
            // append row
            ((Element)sectionNode).appendChild( (Element)rowElVector.get( liRef ) ) ;
            liRef++ ;
        }
        return sectionNode ;
    }
    
    private void printDocument( String document , HttpServletResponse response ) throws java.io.IOException , org.xml.sax.SAXException , java.sql.SQLException , javax.servlet.ServletException , java.rmi.RemoteException {
        
        try {
            //
            Date date = new Date();
            long time = date.getTime();
            response.setContentType("text/rtf");
            // new line David
            response.setHeader("Content-Disposition","attachment; filename=\"" + time + ".rtf\";");
            PrintWriter rtfWriter = response.getWriter();
            rtfWriter.print( this.replaceAll( document , "�", "" ) ) ;
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "Report throws IOException in printDocument: " + ioe ) ;
        }
        
    }
    
    private void saveDocument( String document ) throws org.xml.sax.SAXException , java.sql.SQLException , javax.servlet.ServletException , java.rmi.RemoteException {
        
        try {
            File reportFile = new File(this.reportDestinationFolder + "/" + this.exportfilename);
            FileWriter fw = new FileWriter(reportFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write( this.replaceAll( document , "�", "" ) );
            bw.close();
        } catch ( java.io.IOException ioe ) {
            throw new javax.servlet.ServletException( "Report throws IOException in saveDocument: " + ioe ) ;
        }
        
    }
    
    private String getFileName( String ADMSOURCE, String VISITNUM ) throws java.io.IOException , org.xml.sax.SAXException , java.sql.SQLException , javax.servlet.ServletException , java.rmi.RemoteException {
        String filename = "";
        String strVisitNumLong = VISITNUM.substring(0, 7);
        String strVisitNumShort_S = VISITNUM.substring(VISITNUM.length() - 2, VISITNUM.length());
        String strVisitNumShort_A = VISITNUM.substring(VISITNUM.length() - 3, VISITNUM.length());
        filename = ADMSOURCE + strVisitNumLong + ".";
        if( ADMSOURCE.trim().equals("S") ){
            filename += strVisitNumShort_S;
        }else if( ADMSOURCE.trim().equals("A") ){
            filename += strVisitNumShort_A;
        }
        
        return filename;
    }
    
    private String replaceAll( String s, String search, String replace ) {
        StringBuffer s2 = new StringBuffer();
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
}
