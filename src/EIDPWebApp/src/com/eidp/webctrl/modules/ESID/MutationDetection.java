/*	
 * MutationDetection.java	
 *	
 * Created on September 20, 2004, 2:07 PM	
 */	
	
package com.eidp.webctrl.modules.ESID;	
	
import com.eidp.core.DB.DBMappingHomeRemote;	
import com.eidp.core.DB.DBMappingRemote;	
import com.eidp.webctrl.WebAppCache.EIDPWebAppCacheRemote ;	
import com.eidp.webctrl.Keygenerator;	
import com.eidp.webctrl.modules.EIDPGenerator;	
import com.eidp.webctrl.modules.Hilf;	
	
import java.io.PrintWriter;	
	
import com.eidp.UserScopeObject.UserScopeObject ;	
import com.eidp.xml.XMLDataAccess ;	
	
import javax.servlet.http.HttpServletRequest;	
import javax.servlet.http.HttpServletResponse;	
import javax.servlet.RequestDispatcher;	
	
import java.util.Vector ;	
import java.util.Iterator ;	
import java.util.HashMap ;	
import java.util.Date;	
	
import java.rmi.RemoteException;	
import java.sql.SQLException;	
import org.xml.sax.SAXException;	
import java.io.IOException;	
import org.w3c.dom.* ;	
	
import com.eidp.logger.Logger ;	
	
/**	
 *	
 * @author  veit	
 */	
public class MutationDetection {	
    	
    int alleles = 1 ;	
    String strLink = "";	
    String strWebServID = "";	
    // Background-Styles => bold	
    //    final String finstrDeletionStyle = "<font face=\"courier\" style=\"color:black;background-color:#FF6347;font-weight:bold\">" ;	
    //    final String finstrExonStyle = "<font face=\"courier\" style=\"color:darkred;background-color:#87CEEB;font-weight:bold\">" ;	
    //    final String finstrMutationStyle = "<font face=\"courier\" style=\"color:red;background-color:yellow;font-weight:bold\">" ;	
    //    final String finstrMutationLinkStyle = "<font face=\"courier\" style=\"color:black;background-color:#66CDAA;font-weight:bold\">" ;	
    //    final String finstrNotIdStyle = "<font face=\"courier\" style=\"color:black;background-color:#FFD700;font-weight:bold\">" ;	
    	
    // Background-Styles => not bold	
    final String finstrDeletionStyle = "<font face=\"courier\" style=\"color:black;background-color:#FF6347\">" ;	
    final String finstrExonStyle = "<font face=\"courier\" style=\"color:darkred;background-color:#87CEEB\">" ;	
    final String finstrMutationStyle = "<font face=\"courier\" style=\"color:red;background-color:yellow\">" ;	
    final String finstrMutationLinkStyle = "<font face=\"courier\" style=\"color:black;background-color:#66CDAA\">" ;	
    final String finstrNotIdStyle = "<font face=\"courier\" style=\"color:black;background-color:#FFD700\">" ;	
    	
    	
    /** Creates a new instance of MutationDetection */	
    public MutationDetection( PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws RemoteException, SQLException, SAXException, IOException {	
        	
        String xmlFile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/gene-sequence.xml" ;	
        XMLDataAccess xmlDataAccess = null ;	
        Hilf hilfe = new Hilf() ;	
        String [] arrstrLocation = { "DNA", "cDNA", "PROTEIN" } ;	
        Vector geneNodes = new Vector() ;	
        Vector geneStrings = new Vector() ;	
        String strFormAction = (String)uso.eidpWebAppCache.sessionData_get( "moduleAction" ) ;	
       	
        // Seitenkopf	
        EIDPGenerator twGen = new EIDPGenerator("Mutation Detection", printWriter, uso ) ;	
        HashMap paramMap = new HashMap() ;	
     	
        // XML-File laden	
        try{	
            xmlDataAccess = new XMLDataAccess( xmlFile ) ;	
        }catch(javax.xml.parsers.ParserConfigurationException e){	
        }	
        	
        // XML-Daten rausziehen	
        this.alleles = Integer.parseInt( (String)((Vector)xmlDataAccess.getElementsByName( "alleles" )).get( 0 ) ) ;	
        this.strLink = (String)((Vector)xmlDataAccess.getElementsByName( "link" )).get( 0 ) ;	
        	
        geneNodes = xmlDataAccess.getNodeListsByName( "sequence" ) ;	
        geneStrings = xmlDataAccess.getElementsByName( "sequence" ) ;	
        	
        Keygenerator keygen = new Keygenerator() ;	
        this.strWebServID = keygen.getWebKey( (String)uso.eidpWebAppCache.sessionData_get("userID") ) ;	
        	
        printWriter.println( "</table>" ) ;	
        printWriter.println( "<hr>" ) ;	
        //        printWriter.println( "<br>" ) ;	
        	
        // Stylesheet	
        printWriter.println( "<style type=\"text/css\">" ) ;	
        printWriter.println( "<!-- " ) ;	
        printWriter.println( "  td.AddOn { background-color:#FFFFFF ; color:#000000 ; font-size:11pt ; } " ) ;	
        printWriter.println( "  td.Counter { background-color:#EEEEEE ; color:#000000 ; font-size:11pt ; } " ) ;	
        printWriter.println( "--> </style> " ) ;	
        	
        // JavaScript	
//        printWriter.println( "<script language=\"JavaScript\">" ) ;	
//        printWriter.println( "<!-- " ) ;	
//        printWriter.println( "  function followLink(){ " ) ;	
//        printWriter.println( "      document.MUTATION.submit() ; " ) ;	
//        printWriter.println( "      var newWindow = window.open( \"" + this.strLink + "?FreiburgWebServID=" + strWebServID + "\" ) ; " ) ;	
//        printWriter.println( "      newWindow.focus() ; " ) ;	
//        printWriter.println( "  } " ) ;	
//        printWriter.println( "--> </script> " ) ;	
        	
        // Tabelle fuer SubmitButton	
        printWriter.println( "<div align=\"left\">" ) ;	
        printWriter.println( "<FORM NAME=\"MUTATION\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=AddOn;controller;ESID.MutationDetection;store&formName=MUTATION\" method=\"POST\">" ) ;	
        printWriter.println( "<table border=\"0\" cellspacing=\"0\">" ) ;	
        printWriter.println( "  <INPUT TYPE=\"hidden\" name=\"WebServID\" value=\"" + strWebServID + "\">" ) ;	
        printWriter.println( "  <tr>" ) ;	
        printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
        printWriter.println( "          <INPUT TYPE=\"submit\" VALUE=\"Mutation submission\">&nbsp;<a href=\"javascript:TOOLTip('Please press the button to submit new mutation')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" TITLE=\"Please press the button to submit your new mutation\"></a>" ) ;	
        printWriter.println( "      </td>" ) ;	
        printWriter.println( "  </tr>" ) ;	
        printWriter.println( "</table>" ) ;	
        printWriter.println( "</FORM>" ) ;	
        printWriter.println( "</div>" ) ;	
        	
        printWriter.println( "<hr>" ) ;	
        printWriter.println( "<br>" ) ;	
        	
        // Mutationsdaten fuer den Patienten in Vector schreiben	
        paramMap.put( "patient_id" , (String)uso.eidpWebAppCache.sessionData_get("PatientID") ) ;	
        uso.dbMapper.DBAction( "MUTATION" , "getMutationForPatientID" , paramMap ) ;	
        Vector allRowsOfMutation = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;	
        	
        // gibt es ueberhaupt Mutationsdaten fuer den Patienten ?	
        if(allRowsOfMutation.size()>0){	
           	
            String strHomo = (String)((HashMap)allRowsOfMutation.get(0)).get( "homo" );	
            	
            // KopfTabelle fuer Mutation incl. Legende	
            printWriter.println( "<div align=\"left\">" ) ;	
            printWriter.println( "<table border=\"0\" cellspacing=\"0\">" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\" colspan=2>" ) ;	
            printWriter.println( "          <h2>Mutation</h2>" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "       <TD nowrap class=\"AddOn\" align=\"left\" valign=\"center\"><nobr><input type=\"radio\" name=\"radioHomo\" value=\"1\" " + hilfe.getRadio(strHomo,"1") + " disabled>Heterozygous</nobr> </td> " ) ;	
            printWriter.println( "       <TD nowrap class=\"AddOn\" align=\"left\" valign=\"center\"><nobr><input type=\"radio\" name=\"radioHomo\" value=\"0\" " + hilfe.getRadio(strHomo,"0") + " disabled>Homozygous</nobr> </td> " ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
            printWriter.println( finstrExonStyle + "Exon-Style</font>" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
            printWriter.println( finstrMutationStyle + "Insertion-Style</font>" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
            printWriter.println( finstrMutationLinkStyle + "Complex- and Point-Style</font>&nbsp;<a href=\"javascript:TOOLTip('Click the link to watch the reference-sequence')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" TITLE=\"Click the link to watch the reference-sequence\"></a>" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
            printWriter.println( finstrDeletionStyle + "Deletion-Style</font>&nbsp;<a href=\"javascript:TOOLTip('Click the link to watch the deleted reference-sequence')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" TITLE=\"Click the link to watch the deleted reference-sequence\"></a>" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "  <tr>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
            printWriter.println( finstrNotIdStyle + "Not identified-Style</font>&nbsp;<a href=\"javascript:TOOLTip('The start-position is marked like this')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" TITLE=\"The start-position is marked like this\"></a>" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\">" ) ;	
            printWriter.println( "          &nbsp;" ) ;	
            printWriter.println( "      </td>" ) ;	
            printWriter.println( "  </tr>" ) ;	
            printWriter.println( "</table>" ) ;	
            printWriter.println( "</div>" ) ;	
            	
            for( int j = 0; j < arrstrLocation.length ; j++ ){	
                	
                // Mutationsdaten fuer Patienten und Typ aus der Datenbank holen	
                paramMap.put( "type" , arrstrLocation[j].toUpperCase() ) ;	
                uso.dbMapper.DBAction( "MUTATION" , "getMutationForPatientIDandType" , paramMap ) ;	
                Vector allRowsOfDNAMutation = uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ;	
                int dbRowSize = allRowsOfDNAMutation.size();	
                	
                // Wenn Daten fuer beide Allele vorhanden sind,	
                // Tabelle um die beiden Tabellen bauen	
                if( dbRowSize > 1 ){	
                    printWriter.println( "<div align=\"left\">" ) ;	
                    printWriter.println( "<table border=\"0\" cellspacing=\"0\">" ) ;	
                    printWriter.println( "  <tr>" ) ;	
                }	
                	
                // Fuer jedes Allel Tabelle erstellen	
                for( int i = 0; i < allRowsOfDNAMutation.size() ; i++ ){	
                    int intStart = Integer.parseInt( (String)((HashMap)allRowsOfDNAMutation.get(i)).get( "start_nr" ) );	
                    String strRefSeq = (String)((HashMap)allRowsOfDNAMutation.get(i)).get( "reference_sequence" );	
                    String strMutation = (String)((HashMap)allRowsOfDNAMutation.get(i)).get( "mutation_sequence" );	
                    String strComment = (String)((HashMap)allRowsOfDNAMutation.get(i)).get( "comment" );	
                    int intAllel = Integer.parseInt( (String)((HashMap)allRowsOfDNAMutation.get(i)).get( "allele" ) );	
                    String strType = getMutationType( strRefSeq.length(), strMutation.length() );	
                    	
                    if( dbRowSize > 1 ){	
                        printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"top\"" ) ;	
                    }	
                    	
                    // Tabelle fuer das entsprechende Allel erstellen	
                    createMutationTableForType( printWriter, xmlDataAccess, strType, strComment, arrstrLocation[j], intStart, strRefSeq, strMutation, geneNodes, geneStrings, uso, intAllel );	
                    	
                    if( dbRowSize > 1 ){	
                        printWriter.println( "      </td>" ) ;	
                    }	
                }	
                	
                if( dbRowSize > 1 ){	
                    printWriter.println( "  </tr>" ) ;	
                    printWriter.println( "  </table>" ) ;	
                    printWriter.println( "  </div>" ) ;	
                }	
                	
                printWriter.println( "  <p>" ) ;	
                printWriter.println( "  &nbsp;" ) ;	
                printWriter.println( "  </p>" ) ;	
            }	
        }	
        	
        if ( strFormAction.equals( "store" ) ) {	
            paramMap.clear() ;	
            Date timestamp = new Date() ;	
            String timeStampString = String.valueOf( timestamp.getTime() ) ;	
            paramMap.put( "patient_id" , (String)uso.eidpWebAppCache.sessionData_get("PatientID") ) ;	
            paramMap.put( "doc_id" , (String)uso.eidpWebAppCache.sessionData_get("userID") ) ;	
            paramMap.put( "doc_timestamp" , timeStampString ) ;	
            paramMap.put( "webservid" , (String)request.getParameter("WebServID") ) ;	
            uso.dbMapper.DBAction( "MUTATION_REFERENCE" , "setWebServIDForID" , paramMap ) ;	
            	
            printWriter.println( "<script language=\"JavaScript\">" ) ;	
            printWriter.println( "<!-- " ) ;	
            printWriter.println( "  newWindow = window.open( \"/EIDPWebApp/MutationForward.html\") ; " ) ;	
            printWriter.println( "  newWindow.document.open(); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"<html><head><title>Sending Data</title>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"</head><body>\"); " ) ;	
            	
            printWriter.println( "  newWindow.document.writeln(\"<FORM NAME='FINLAND' action='" + this.strLink + "' method='POST'>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"<INPUT TYPE='hidden' name='FreiburgWebServID' value='" + (String)request.getParameter("WebServID") + "'>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"</FORM>\"); " ) ;	
            	
            printWriter.println( "  newWindow.document.writeln(\"<script language='JavaScript'>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"document.FINLAND.submit();\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"</script>\"); " ) ;	
            	
            printWriter.println( "  newWindow.document.writeln(\"</body></html>\"); " ) ;	
            printWriter.println( "  newWindow.document.close(); " ) ;	
            	
            printWriter.println( "  newWindow.focus() ; " ) ; 	
            printWriter.println( "//--> </script> " ) ;	
      	
        }	
        	
        twGen.CloseModule( printWriter , uso ) ;	
    }	
    	
    public void createMutationTableForType( PrintWriter printWriter, XMLDataAccess xmlDataAccess, String strType, String strComment, String strLocation, int intStart, String strRefSeq, String strMutation, Vector geneNodes, Vector geneStrings, UserScopeObject uso, int intAllelNr ) throws SAXException, IOException {	
        	
        printWriter.println( "<div align=\"left\" valign=\"top\">" ) ;	
        printWriter.println( "<table border=\"1\" cellspacing=\"0\">" ) ;	
        	
        // Kopf	
        printWriter.println( "  <tr>" ) ;	
        printWriter.println( "      <td class=\"AddOn\" align=\"center\" valign=\"center\" colspan=7>" ) ;	
        printWriter.println( "          <b>Allel " + intAllelNr + " (" + strLocation + ")</b>" ) ;	
        printWriter.println( "      </td>" ) ;	
        printWriter.println( "  </tr>" ) ;	
        	
        printWriter.println( "  <tr>" ) ;	
        printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\" colspan=7>" ) ;	
        printWriter.println( "          Type of Mutation:&nbsp;<b>" + strType + "</b>" ) ;	
        printWriter.println( "      </td>" ) ;	
        printWriter.println( "  </tr>" ) ;	
        	
        printWriter.println( "  <tr>" ) ;	
        printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\" colspan=7>" ) ;	
        printWriter.println(            "Comment:&nbsp;<b>" + strComment + "</b>" ) ;	
        printWriter.println( "      </td>" ) ;	
        printWriter.println( "  </tr>" ) ;	
        	
        printWriter.println( "  <tr>" ) ;	
        printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\" colspan=7>" ) ;	
        printWriter.println(            "Startpoint of Mutation:&nbsp;<b>" + intStart + "</b>" ) ;	
        printWriter.println( "      </td>" ) ;	
        printWriter.println( "  </tr>" ) ;	
        	
        int intMutationLen = strMutation.length() ;	
        int intReferenceLen = strRefSeq.length() ;	
        	
        // gesamte DNA-Sequenz in einen String einlesen	
        String strDNA = "";	
        for( int i = 0 ; i < geneStrings.size() ; i++ ){	
            if( ((String)((Vector)xmlDataAccess.getAttributesByName( "type" , (NodeList)geneNodes.get( i ) ) ).get( 0 )).equals( strLocation.toUpperCase() ) ){	
                strDNA += ((String)geneStrings.get(i)).trim().replaceAll("\\s","");	
            }	
        }	
        	
        // neue DNA zusammensetzen	
        strDNA = replaceDNASequence(strDNA, intStart, strRefSeq, strMutation);	
        	
        // FontStyle eingrenzen	
        int intGeneStringLen = 0;	
        int len = 0;	
        int exonEnd = 0;	
        int anzExons = 0;	
        HashMap styleMap = new HashMap() ;	
        int x = 1 ;	
        	
        for( int i = 0 ; i < geneStrings.size() ; i++ ){	
            	
            boolean boolIN = false;	
            	
            if( ((String)((Vector)xmlDataAccess.getAttributesByName( "extract" , (NodeList)geneNodes.get( i ) ) ).get( 0 )).equals( "exon" ) ){	
                	
                intGeneStringLen = ((String)geneStrings.get(i)).trim().replaceAll("\\s","").length();	
                exonEnd = len + intGeneStringLen;	
                	
                if( intStart >= len && intStart <= exonEnd ){	
                    boolIN = true ;	
                }	
                	
                if( strType.equals("Insertion") ){	
                    	
                    if( intStart < len && boolIN == false ){	
                        	
                        styleMap.put( "exon_start_" + x, String.valueOf( len + intMutationLen) );	
                        styleMap.put( "exon_end_" + x, String.valueOf( exonEnd + intMutationLen ));	
                        	
                    }else{	
                        	
                        styleMap.put( "exon_start_" + x, String.valueOf( len ) );	
                        styleMap.put( "exon_end_" + x, String.valueOf( exonEnd ) );	
                        	
                    }	
                    	
                    	
                }else if( strType.equals("Complex") || strType.equals("Point") ){	
                    	
                    if( intStart < len && boolIN == false ){	
                        	
                        styleMap.put( "exon_start_" + x, String.valueOf( len ) );	
                        styleMap.put( "exon_end_" + x, String.valueOf( exonEnd + intMutationLen - intReferenceLen));	
                        	
                    }else{	
                        	
                        styleMap.put( "exon_start_" + x, String.valueOf( len + intMutationLen - intReferenceLen) );	
                        styleMap.put( "exon_end_" + x, String.valueOf( exonEnd + intMutationLen - intReferenceLen));	
                        	
                    }	
                    	
                }else if( strType.equals("Deletion") ){	
                    	
                    if( intStart < len && boolIN == false ){	
                        	
                        styleMap.put( "exon_start_" + x, String.valueOf( len - intReferenceLen ) );	
                        styleMap.put( "exon_end_" + x, String.valueOf( exonEnd - intReferenceLen ));	
                        	
                    }else{	
                        	
                        styleMap.put( "exon_start_" + x, String.valueOf( len ) );	
                        styleMap.put( "exon_end_" + x, String.valueOf( exonEnd - intReferenceLen));	
                        	
                    }	
                    	
                }else{	
                    	
                    styleMap.put( "exon_start_" + x, String.valueOf( len ) );	
                    styleMap.put( "exon_end_" + x, String.valueOf( exonEnd ) );	
                    	
                }	
                	
                x++ ;	
            }	
            	
            anzExons = x;	
            	
            len += ((String)geneStrings.get(i)).trim().replaceAll("\\s","").length();	
        }	
        	
        int id = 0;	
        int y = 1;	
        String str = "" ;	
        String strFontStyle = "" ;	
        printWriter.println( "  <tr>" ) ;	
        int i = 1;	
        String strMutationStyle = "" ;	
        String strMutationTooltip = "" ;	
        	
        // DNA-Sequenz in 10er-Stuecken in Tabelle schreiben	
        for( ; i <= strDNA.length() ; i++ ){	
            	
            String strFontBreakStyle = "" ;	
            	
            if( strType.equals( "Deletion" ) ) {	
                	
                strMutationStyle = finstrDeletionStyle ;	
                strMutationTooltip = "<a href=\"javascript:TOOLTip('deleted reference-sequence: " + strRefSeq +  "')\" title=\"deleted reference-sequence: " + strRefSeq +  "\">" ;	
                intMutationLen = 1 ;	
                	
            }else if( strType.equals( "Not identified" ) ) {	
                	
                strMutationStyle = finstrNotIdStyle ;	
                strMutationTooltip = "" ;	
                intMutationLen = 1 ;	
                	
            }else if( strType.equals( "Complex" ) || strType.equals( "Point" ) ) {	
                	
                strMutationStyle = finstrMutationLinkStyle ;	
                strMutationTooltip = "<a href=\"javascript:TOOLTip('Reference-sequence: " + strRefSeq +  ", Mutation-sequence: " + strMutation +  "')\" title=\"Reference-sequence: " + strRefSeq +  ", Mutation-sequence: " + strMutation +  "\">" ;	
                	
            }else{	
                	
                strMutationStyle = finstrMutationStyle ;	
                strMutationTooltip = "" ;	
                	
            }	
            	
            // Styleveraenderungen innerhalb der Zellen	
            if( i == Integer.parseInt((String)styleMap.get( "exon_start_" + y )) ){	
                	
                if( intReferenceLen > 0 )	
                    strFontBreakStyle = "</a></font>" + finstrExonStyle ;	
                else	
                    strFontBreakStyle = "</font>" + finstrExonStyle ;	
                	
            }else if( i == Integer.parseInt((String)styleMap.get( "exon_end_" + y )) ){	
                	
                if( intReferenceLen > 0 )	
                    strFontBreakStyle = "</a></font><font face=\"courier\">" ;	
                else	
                    strFontBreakStyle = "</font><font face=\"courier\">" ;	
                	
            }	
            	
            if( i == intStart - 1 + intMutationLen && i <= Integer.parseInt((String)styleMap.get( "exon_end_" + y )) && i >= Integer.parseInt((String)styleMap.get( "exon_start_" + y )) ){	
                	
                if( intReferenceLen > 0 )	
                    strFontBreakStyle = "</a></font>" + finstrExonStyle ;	
                else	
                    strFontBreakStyle = "</font>" + finstrExonStyle ;	
                	
            }	
            	
            if( i == intStart - 1 ){	
                	
                if( intReferenceLen > 0 )	
                    strFontBreakStyle = "</a></font>" + strMutationStyle + strMutationTooltip ;	
                else	
                    strFontBreakStyle = "</font>" + strMutationStyle ;	
                	
            }else if( i == intStart - 1 + intMutationLen ){	
                	
                String strWechselStyle = "</font><font face=\"courier\">" ;	
                	
                if( i == intStart - 1 + intMutationLen && i <= Integer.parseInt((String)styleMap.get( "exon_end_" + y )) && i >= Integer.parseInt((String)styleMap.get( "exon_start_" + y )) )	
                    strWechselStyle = finstrExonStyle ;	
                	
                if( intReferenceLen > 0 )	
                    strFontBreakStyle = "</a>" + strWechselStyle;	
                else	
                    strFontBreakStyle = strWechselStyle ;	
                	
            }	
            	
            str += strDNA.substring(i-1,i) + strFontBreakStyle ;	
            	
            if( i <= ( strDNA.length() / 10) * 10 ){	
                if(i % 10 == 0){	
                    	
                    printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
                    String strCellContent = str ;	
                    	
                    // FontStyle fuer jede Zelle festlegen	
                    strFontStyle = "<font face=\"courier\">" ;	
                    	
                    if( (i >= Integer.parseInt((String)styleMap.get( "exon_start_" + y )) && i <= Integer.parseInt((String)styleMap.get( "exon_end_" + y ))) || (i - Integer.parseInt((String)styleMap.get( "exon_end_" + y ))) / 10 == 0 ){	
                        if(id>0)	
                            strFontStyle = finstrExonStyle ;	
                        id++;	
                    }	
                    	
                    if( ( (i - 10) >= (intStart - 1) && (i - 10) < (intStart - 1 + intMutationLen) ) || (intStart == 1 && i <= 10) ){	
                        	
                        if( intReferenceLen > 0 )	
                            strFontStyle = strMutationStyle + strMutationTooltip ;	
                        else	
                            strFontStyle = strMutationStyle ;	
                        	
                    }	
                    	
                    if( i >= Integer.parseInt((String)styleMap.get( "exon_end_" + y )) ){	
                        if( y < anzExons - 1 ){	
                            y++;	
                        }	
                        id = 0;	
                    }	
                    	
                    // link wird geschlossen	
                    if( strCellContent.indexOf( "<a href" ) != -1 && strCellContent.indexOf( "</a>" ) == -1 )	
                        printWriter.println( strFontStyle + strCellContent + "</a></font>" ) ;	
                    else	
                        printWriter.println( strFontStyle + strCellContent + "</font>" ) ;	
                    	
                    //                    printWriter.println( strFontStyle + strCellContent + "</font>" ) ;	
                    printWriter.println( "      </td>" ) ;	
                    	
                    str = "" ;	
                }	
                	
                if(i % 60 == 0){	
                    printWriter.println( "      <td class=\"Counter\" align=\"right\" valign=\"center\">" ) ;	
                    printWriter.println( "          " + i ) ;	
                    printWriter.println( "      </td>" ) ;	
                    printWriter.println( "  </tr><tr>" ) ;	
                }	
                	
                	
            }	
        }	
        	
        // falls DNA nicht buendig abschliesst:	
        if( strDNA.length() % 60 != 0 ){	
            	
            // FontStyle fuer Zelle festlegen	
            strFontStyle = "<font face=\"courier\">" ;	
            	
            if( (i >= Integer.parseInt((String)styleMap.get( "exon_start_" + y )) && i <= Integer.parseInt((String)styleMap.get( "exon_end_" + y ))) || (i - Integer.parseInt((String)styleMap.get( "exon_end_" + y ))) / 10 == 0 ){	
                if(id>0)	
                    strFontStyle = finstrExonStyle ;	
            }	
            	
            if( ( (i - 10 + 2) >= (intStart - 1) && (i - 10) < (intStart - 1 + intMutationLen) ) || (intStart == 1 && i <= 10) ){	
                	
                if( intReferenceLen > 0 )	
                    strFontStyle = strMutationStyle + strMutationTooltip ;	
                else	
                    strFontStyle = strMutationStyle ;	
                	
            }	
            	
            	
            printWriter.println( "      <td class=\"AddOn\" align=\"left\" valign=\"center\">" ) ;	
            printWriter.println( "          " + strFontStyle + str + "</font>" ) ;	
            printWriter.println( "      </td>" ) ;	
            	
            // benoetigte Leerzellen berechnen	
            //             7 braucht man insgesamt,             2 werden manuell geschrieben	
            int intCells = 7 - (((strDNA.length() % 60) / 10) + 2) ;	
            	
            for(int zaehl=0; zaehl < intCells; zaehl++){	
                printWriter.println( "      <td class=\"AddOn\" align=\"right\" valign=\"center\">" ) ;	
                printWriter.println( "          &nbsp;" ) ;	
                printWriter.println( "      </td>" ) ;	
            }	
            	
            printWriter.println( "      <td class=\"Counter\" align=\"right\" valign=\"center\">" ) ;	
            printWriter.println( "          " + strDNA.length() ) ;	
            printWriter.println( "      </td>" ) ;	
        }	
        	
        	
        printWriter.println( "  </tr>" ) ;	
        printWriter.println( "</table>" ) ;	
        printWriter.println( "</div>" ) ;	
        	
    }	
    	
    	
    public String replaceDNASequence( String dna , int start , String origSequence , String replaceSequence ) {	
        String erg = "" ;	
        int end = start + origSequence.length() ;	
        try {	
            if ( start <= end ) {	
                String dnaBegin = dna.substring( 0 , start-1 ) ;	
                String dnaEnd = dna.substring(end-1 , dna.length() ) ;	
                erg = dnaBegin + replaceSequence + dnaEnd ;	
            }	
        } catch ( java.lang.StringIndexOutOfBoundsException siobe ) {	
            erg = siobe.toString() ;	
        }	
        return erg ;	
    }	
    	
    public String getMutationType( int refLen, int mutationLen ) {	
        	
        String erg = "" ;	
        	
        if( refLen >= 1 && mutationLen == 0 )	
            erg = "Deletion" ; 	
        if( refLen == 0 && mutationLen >= 1 )	
            erg = "Insertion" ;	
        if( (refLen >= 2 && mutationLen >= 1) || (refLen >= 1 && mutationLen >= 2) )	
            erg = "Complex" ;	
        if( refLen == 0 && mutationLen == 0 )	
            erg = "Not identified" ;	
        if( refLen == 1 && mutationLen == 1 )	
            erg = "Point" ;	
        	
        return erg ;	
    }	
    	
}	
