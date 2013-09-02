/*	
 * MutationBrowser.java	
 *	
 * Created on August 13, 2005, 4:34 PM	
 */	
	
package com.eidp.webctrl.modules.ESID;	
import com.eidp.webctrl.Keygenerator;	
	
import java.io.PrintWriter;	
	
import com.eidp.UserScopeObject.UserScopeObject;	
import com.eidp.xml.XMLDataAccess;	
import com.eidp.webctrl.modules.EIDPGenerator;	
	
import javax.servlet.http.HttpServletRequest;	
import javax.servlet.http.HttpServletResponse;	
	
import java.util.Vector;	
import java.util.HashMap;	
import java.util.Date;	
import java.util.regex.Pattern;	
import java.util.regex.Matcher;	
import java.util.Arrays;	
import java.util.List;	
import java.util.Iterator;	
	
import java.rmi.RemoteException;	
import java.sql.SQLException;	
import org.xml.sax.SAXException;	
import java.io.IOException;	
import javax.xml.parsers.ParserConfigurationException;	
	
import org.w3c.dom.*;	
import org.xml.sax.*;	
// This import requires JAXP release from https://jaxp.dev.java.net/	
import javax.xml.xpath.*;	
	
	
/**	
 *	
 * @author  david guzman	
 * @author  rusch	
 * @author  veit	
 */	
public class MutationBrowser {	
    	
    /** Creates a new instance of MutationBrowser */	
    public MutationBrowser(PrintWriter printWriter, HttpServletRequest request, HttpServletResponse response, UserScopeObject uso) throws RemoteException, SQLException, SAXException, IOException {	
    	
        //Module header (logo + title)	
        EIDPGenerator twGen = new EIDPGenerator("Mutation Browser", printWriter, uso);	
        printWriter.println("</table>");	
        printWriter.println("<hr>");	
        	
        //Gene names extraction	
        String xmlInfile = "/com/eidp/" + uso.applicationContext + "/resources/webctrl/mutation-detection.xml";	
        Vector geneNames = new Vector();	
        //Load XML File	
        try {	
            XMLDataAccess xmlaccess = new XMLDataAccess(xmlInfile);	
            geneNames = xmlaccess.getElementsByName("name");	
        } catch (ParserConfigurationException e) {	
            printWriter.println(e);	
        }	
        	
        printWriter.println("<div style=\"display:block;width:55em;padding-left:2em;text-align:justify\">");	
        printWriter.println("   <p>Press the \"Mutation submission\" button to submit a new mutation event. Note that you will be redirected to the UTA-IDbases website in order to validate your data. Once the mutation is successfully validated and submitted please press the \"Mutation Browser\" webmenu option to display it.</p>");	
        printWriter.println("   <p>Please check previously that your browser allows pop-up windows from this website.</p>");	
        	
        printWriter.println("   <FORM name=\"mutation\" action=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=AddOn;controller;ESID.MutationBrowser;store&formName=mutation\" method=\"POST\">");	
        //names of genes menu	
        if ( geneNames.size() == 1 ) {	
            String gene = (String) geneNames.get(0);	
            String geneval = new String();	
            try {	
                String automark = AutosomalValidator(gene,xmlInfile);	
                geneval = gene + "_" + automark;	
            } catch (XPathExpressionException e) {	
                printWriter.println(e);	
            }	
            printWriter.println("       <INPUT type=\"hidden\" name=\"genenames\" value=\"" + geneval + "\">");	
            printWriter.print("       Gene name: <b>" + gene + "</b>&nbsp;");	
        } else if ( geneNames.size() > 1 ) {	
            printWriter.println("<p>Please select the gene:</p>");	
            printWriter.println("       <SELECT name=\"genenames\">");	
            for ( int i = 0; i < geneNames.size(); i++) {	
                String gene = (String) geneNames.get(i);	
                try {	
                    String automark = AutosomalValidator(gene,xmlInfile);	
                    String geneval = gene + "_" + automark;	
                    printWriter.println("           <OPTION value=\"" + geneval + "\">" + gene + "</OPTION>");	
                } catch (XPathExpressionException e) {	
                    printWriter.println(e);	
                }	
            }	
            printWriter.println("       </SELECT>");	
        }        	
        printWriter.print("       <INPUT type=\"submit\" value=\"Mutation submission\">&nbsp;");	
        printWriter.println("<a href=\"javascript:TOOLTip('Please press the button to submit new mutation')\"><img src=\"/EIDPWebApp/images/tooltip.jpg\" border=\"0\" TITLE=\"TOOLTip\"></a>");	
        printWriter.println("   </FORM>");	
        printWriter.println("</div>");	
    	
        printWriter.println("<hr>");	
        printWriter.println("<br>");	
	
        printWriter.println("<div align=\"left\">");	
        printWriter.println("<script language=\"JavaScript\">");	
        printWriter.println("<!--");	
        printWriter.println("var newwin");	
        printWriter.println("function MUTwin(url) {");	
        printWriter.println("   name = 'mutationZOOMER';");	
        printWriter.println("   params = 'height=150,width=300,scrollbars=yes,toolbar=no,location=no,status=no,menubar=no';");	
        printWriter.println("   newwin = window.open(url,name,params);");	
        printWriter.println("   newWin.focus();");	
        printWriter.println("}// -->");	
        printWriter.println("</script>");	
        	
        //extract the gene corresponding to the last documentation	
        String pathand = (String) uso.eidpWebAppCache.sessionData_get("PatientID");	
        String validgene = new String();	
        String marker = new String();	
        HashMap initMap = new HashMap();	
        HashMap paramMap = new HashMap();	
        Vector allRowsOfMutation = new Vector();	
        boolean autobool = false;	
        initMap.put("patient_id", pathand);	
        	
        uso.dbMapper.DBAction("MUTATION", "getGeneForPatientID", initMap);	
        if (uso.dbMapper.size() > 0) {	
            HashMap genhash = uso.dbMapper.getRow(0);	
            validgene = (String) genhash.get("gene");	
            paramMap.put("patient_id", pathand);	
            try {	
                autobool = AutosomalValidator(validgene, xmlInfile).equals("1");	
            } catch (XPathExpressionException i) {	
                printWriter.println("XML Processing failed. Reason: " + i);	
                printWriter.println("Please contact the system administrator");	
            }	
            // To get the PublicPatientID...	
            //String publicpatid = (String) uso.eidpWebAppCache.sessionData_get("PublicPatientID");	
            marker = validgene + "-"; //+ publicpatid + "-";	
            	
            //Now retrieve the genetic data	
            paramMap.put("gene", validgene);	
            uso.dbMapper.DBAction("MUTATION", "getMutationForPatientIDAndGene", paramMap);	
            allRowsOfMutation = uso.dbMapper.getRowRange(0, uso.dbMapper.size());	
        }	
        	
        //Now for every row...	
        if (allRowsOfMutation.size() > 0) {	
            String[] seqTypes = { "DNA", "CDNA", "PROT" };	
            paramMap.put("type", seqTypes[0]);	
            uso.dbMapper.DBAction("MUTATION", "getMutationForPatientIDAndGeneAndType", paramMap);	
            Vector allRowsOfDNAMutation = uso.dbMapper.getRowRange(0, uso.dbMapper.size());	
            if (allRowsOfDNAMutation.size() > 0) {	
                String dnamarker = marker + seqTypes[0]; 	
                Vector origseq = new Vector();	
                try {	
                    origseq = XMLSequenceExtractor(xmlInfile, seqTypes[0], validgene);	
                } catch (XPathExpressionException f) {	
                    printWriter.println(f);	
                }	
                DNAMutationPrinter(printWriter, allRowsOfDNAMutation, origseq, dnamarker, autobool);	
            }	
            paramMap.put("type", seqTypes[1]);	
            uso.dbMapper.DBAction("MUTATION", "getMutationForPatientIDAndGeneAndType", paramMap);	
            Vector allRowsOfCDNAMutation = uso.dbMapper.getRowRange(0, uso.dbMapper.size());	
            if (allRowsOfCDNAMutation.size() > 0) {	
                String cdnamarker = marker + seqTypes[1]; //should be a replace in case of existing DNA mutations	
                Vector origseq = new Vector();	
                try {	
                    origseq = XMLSequenceExtractor(xmlInfile, seqTypes[1], validgene);	
                } catch (XPathExpressionException f) {	
                    printWriter.println(f);	
                }	
                CDNAProtMutationPrinter(printWriter, allRowsOfCDNAMutation, seqTypes[1], origseq, cdnamarker, autobool);	
            }	
//            paramMap.put("type", seqTypes[2]); //should be a replace in case of existing DNA mutations	
//            uso.dbMapper.DBAction("MUTATION", "getMutationForPatientIDAndGeneAndType", paramMap);	
//            Vector allRowsOfProtMutation = uso.dbMapper.getRowRange(0, uso.dbMapper.size());	
//            if (allRowsOfProtMutation.size() > 0) {	
//                marker += seqTypes[2];	
//                Vector origseq = new Vector();	
//                try {	
//                    origseq = XMLSequenceExtractor(xmlInfile, seqTypes[2], validgene);	
//                } catch (XPathExpressionException f) {	
//                    printWriter.println(f);	
//                }	
//                CDNAProtMutationPrinter(printWriter, allRowsOfProtMutation, seqTypes[2], origseq, marker);	
//            }	
        } else {	
            printWriter.println("No recorded mutations for this patient");	
        }	
        	
        String formAction = (String) uso.eidpWebAppCache.sessionData_get("moduleAction");	
        if (formAction.equals("store")) {	
            HashMap storeMap = new HashMap();	
            Date timestamp = new Date();	
            String timestr = String.valueOf(timestamp.getTime());	
            String patcour = (String) uso.eidpWebAppCache.sessionData_get("PatientID");	
            String usrcour = (String) uso.eidpWebAppCache.sessionData_get("userID");        	
            String genename = (String) request.getParameter("genenames");	
            String[] generec = genename.split("_");	
            //webservid    	
            String webservid[] = GenerateWebServID( uso, Integer.parseInt(generec[1]) );	
            	
            storeMap.put("patient_id", patcour);	
            storeMap.put("doc_id", usrcour);	
            storeMap.put("doc_timestamp", timestr);	
            storeMap.put("webservid", webservid[0]);	
            uso.dbMapper.DBAction("MUTATION_REFERENCE", "setWebServIDForID", storeMap);	
            	
            // repeat procedure when WebServID_2 is not null	
            if (webservid[1] != null) {	
                storeMap.put("webservid", webservid[1]);	
                uso.dbMapper.DBAction("MUTATION_REFERENCE", "setWebServIDForID", storeMap);	
            }	
            	
            // locate the CGI address using XPath from JAXP-API	
            String expression = "/gene-sequence/gene[name=\"" + generec[0] + "\"]/link";	
            InputSource inputSource = new InputSource(xmlInfile);	
            String cgiaddress = new String();	
            try {	
                XPathFactory xpathfactory = XPathFactory.newInstance();	
                XPath xpathlocator = xpathfactory.newXPath();	
                XPathExpression xpathexpression = xpathlocator.compile(expression);	
                cgiaddress = (String) xpathexpression.evaluate(inputSource, XPathConstants.STRING);	
            } catch ( XPathExpressionException e ) {	
                System.out.println(e);	
            }
            // open a popup, forwarding the user to Finland	
            printWriter.println( "<script language=\"JavaScript\">" ) ;	
            printWriter.println( "<!-- " ) ;	
            printWriter.println( "  var newWindow = window.open( \"/EIDPWebApp/MutationForward.html\") ; " ) ;	
            printWriter.println( "  newWindow.document.open(); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"<html><head><title>Sending Data</title>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"</head><body>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"<FORM NAME='FINLAND' action='" + cgiaddress + "' method='POST'>\"); " ) ;	
            // check if the second webservid exists and send it to Finland	
            if (webservid[1] == null) {	
                printWriter.println( "  newWindow.document.writeln(\"<INPUT TYPE='hidden' name='USIDnetWebServID_1' value='" + webservid[0] + "'>\"); ") ;	
            } else {	
                printWriter.println( "  newWindow.document.writeln(\"<INPUT TYPE='hidden' name='USIDnetWebServID_1' value='" + webservid[0] + "'>\"); ") ;	
                printWriter.println( "  newWindow.document.writeln(\"<INPUT TYPE='hidden' name='USIDnetWebServID_2' value='" + webservid[1] + "'>\"); ");	
            }	
            printWriter.println( "  newWindow.document.writeln(\"</FORM>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"<script language='JavaScript'>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"document.FINLAND.submit();\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"</script>\"); " ) ;	
            printWriter.println( "  newWindow.document.writeln(\"</body></html>\"); " ) ;	
            printWriter.println( "  newWindow.document.close(); " ) ;	
            printWriter.println( "  newWindow.focus() ; " ) ; 	
            printWriter.println( "//--> </script> " ) ;	
        }	
        printWriter.println("</div>");	
        printWriter.println("<hr>");	
        twGen.CloseModule(printWriter, uso);	
    }	
    	
    private String AutosomalValidator(String genename, String xmlInfile) throws XPathExpressionException {	
        String autosomal = new String();	
        InputSource inputSource = new InputSource(xmlInfile);	
        String expression = "/gene-sequence/gene[name=\"" + genename + "\"]/autosomal";	
        try {	
            XPathFactory factory = XPathFactory.newInstance();	
            XPath xpatheval = factory.newXPath();	
            XPathExpression xpathexpression = xpatheval.compile(expression);	
            autosomal = (String) xpathexpression.evaluate(inputSource, XPathConstants.STRING);	
        } catch ( XPathExpressionException e) {	
            throw new XPathExpressionException(e);	
        }	
        return autosomal;	
    }	
    	
    private String getCGILinkAddress(String genename, String xmlInfile) {	
        String link = new String();	
        // locate the CGI address using XPath from JAXP-API	
        String expression = "/gene-sequence/gene[name=\"" + genename + "\"]/link";	
        InputSource inputSource = new InputSource(xmlInfile);	
        String cgiaddress = new String();	
        try {	
            XPathFactory xpathfactory = XPathFactory.newInstance();	
            XPath xpathlocator = xpathfactory.newXPath();	
            XPathExpression xpathexpression = xpathlocator.compile(expression);	
            cgiaddress = (String) xpathexpression.evaluate(inputSource, XPathConstants.STRING);	
        } catch ( XPathExpressionException e ) {	
            System.out.println(e);	
        }	
        link = cgiaddress;	
        return cgiaddress;	
    }	
    	
    private String[] GenerateWebServID(UserScopeObject userhand, int autosomal) {	
        String patientPart = new String();	
        try {	
            patientPart = (String) userhand.eidpWebAppCache.sessionData_get("PatientID");	
        } catch (RemoteException e) {	
            System.out.println(e);	
        }	
        Date timestamp = new Date();	
        String timestr = String.valueOf(timestamp.getTime());	
        Keygenerator keygen = new Keygenerator();	
        String[] reception = new String[2];	
        reception[0] = patientPart + timestr + "0";	
        String[] md5hash = new String[2];	
        md5hash[0] = keygen.getWebKey(reception[0]);	
        // when non autosomal, return only one key	
        if (autosomal == 0) {	
            md5hash[1] = null;           	
        // when autosomal, return the complete md5hash array	
        } else if (autosomal == 1) {	
            reception[1] = patientPart + timestr + "1";	
            md5hash[1] = keygen.getWebKey(reception[1]);	
        }	
        return md5hash;	
    }	
    	
    private Vector XMLSequenceExtractor(String xmlInfile, String moltype, String genename) throws XPathExpressionException {	
        Vector seqVector = new Vector();	
        try {	
            XPath xpath = XPathFactory.newInstance().newXPath();	
            String expression = "/gene-sequence/gene[name=\"" + genename + "\"]";	
            InputSource inputSource = new InputSource(xmlInfile);	
            Node nodes = (Node) xpath.evaluate(expression, inputSource, XPathConstants.NODE);	
            xpath.reset();	
            if ( moltype.equals("DNA") ) {	
                String dnastring = "sequence[@type=\"DNA\"]";	
                NodeList dnanode = (NodeList) xpath.evaluate(dnastring, nodes, XPathConstants.NODESET);	
                xpath.reset();	
                //let's rock with the combined loop	
                for (int i = 0; i < dnanode.getLength(); i++) {	
                    String[] dnavalue = new String[2];	
                    Node receiver = dnanode.item(i);	
                    NamedNodeMap attr = receiver.getAttributes();	
                    String extract = attr.getNamedItem("extract").getNodeValue();	
                    String dnaquery = "sequence[position()=" + String.valueOf(i + 1) + " and @type=\"DNA\" and @extract=\"" + extract + "\"]";	
                    String dnaseq = (String) xpath.evaluate(dnaquery, nodes, XPathConstants.STRING);	
                    dnavalue[0] = extract;	
                    if ( extract.equals("intron") ) {	
                        dnavalue[1] = dnaseq.toLowerCase();	
                    } else if ( extract.equals("exon") ) {	
                        dnavalue[1] = dnaseq;	
                    }	
                    seqVector.add(dnavalue);	
                } 	
            } else if ( moltype.equals("CDNA") || moltype.equals("PROT") ) {	
                String value[] = new String[2];	
                String evalstring = "sequence[@type=\"" + moltype + "\"]";	
                value[0] = moltype.toLowerCase();	
                value[1] = (String) xpath.evaluate(evalstring, nodes, XPathConstants.STRING);	
                seqVector.add(value);	
            }	
        } catch (XPathExpressionException f) {	
            //System.out.println(f);	
            throw new XPathExpressionException(f.getMessage());	
        } 	
        return seqVector;	
    }	
    	
    private void DNAMutationPrinter(PrintWriter pWriter, Vector dnaMutationHandler, Vector dnaOrig, String session, boolean autosomal) {        	
        //Get the sequence data from XML!!!	
        int exnum = 1;	
        int seqpos = 0;	
        int[] mutstart = new int[dnaMutationHandler.size()];	
        String[] mutstring = new String[dnaMutationHandler.size()];	
        String[] refstring = new String[dnaMutationHandler.size()];	
        Integer[] alleles = new Integer[dnaMutationHandler.size()];	
        String mutbefore = new String();	
        String mutafter = new String();	
        String mutlink = new String();	
        String muttrack = new String();	
        String genename = session.split("-")[0];	
        for ( int h = 0; h < dnaMutationHandler.size(); h++) {	
            mutstart[h] = Integer.parseInt( (String)((HashMap)dnaMutationHandler.get(h)).get( "start_nr" ) );	
            mutstring[h] = (String)((HashMap)dnaMutationHandler.get(h)).get( "mutation_sequence" );	
            refstring[h] = (String)((HashMap)dnaMutationHandler.get(h)).get( "reference_sequence" );	
            alleles[h] = Integer.valueOf( (String)((HashMap)dnaMutationHandler.get(h)).get( "allele" ) );	
        }	
        pWriter.println("Reported DNA mutations in <b>" + genename + "</b>:<hr><br>");	
        pWriter.println("<div class=\"seqrep\" style=\"display:block;\">");	
        pWriter.println("   <table class=\"DNA\">");	
        pWriter.println("       <tbody style=\"background-color:white;\"><tr class=\"header\">");	
        pWriter.println("           <th>Exon/Intron</th>");	
        pWriter.println("           <th>Number</th>");	
        pWriter.println("           <th>Start</th>");	
        pWriter.println("           <th>End</th>");	
        if (autosomal == false) {	
            pWriter.println("           <th>Sequence</th>");	
        } else if (autosomal == true) {	
            pWriter.println("           <th>Sequence Allele 1</th>");	
            pWriter.println("           <th>Sequence Allele 2</th>");	
        }	
        pWriter.println("       </tr>");	
        int poscounter = 0;	
        int poscounter_a = 0;	
        int poscounter_b = 0;	
        int[][] allelepos = new int[2][];	
        int start = 1;	
        for ( int i = 0; i < dnaOrig.size(); i++) {	
            String[] receiver = (String[]) dnaOrig.get(i);	
            Pattern cleanpat = Pattern.compile("[\n\t]");	
            Matcher cleanmat = cleanpat.matcher(receiver[1].replaceAll(" ",""));	
            String seqstring1 = cleanmat.replaceAll("");	
            String seqstring2 = seqstring1;	
            pWriter.println("       <tr class=\"" + receiver[0] + "\" style=\"background-color:white;font-family:courier,monospace;\">");	
            pWriter.println("           <td>" + receiver[0] + "</td>");	
            pWriter.print("           <td style=\"text-align: center;\">");	
            if ( receiver[0].compareTo("intron") == 0 ) {	
                pWriter.println("</td>");	
            } else if ( receiver[0].compareTo("exon") == 0 ) {                	
                pWriter.println(exnum + "</td>");	
                exnum++;	
            }	
            int end = start + seqstring1.length() - 1;	
            pWriter.println("           <td>" + start + "</td>");	
            pWriter.println("           <td>" + end + "</td>");	
            if ( receiver[0].compareTo("intron") == 0 ) {	
                pWriter.println("           <td style=\"color:blue\">" + IntronTrimmer(seqstring1) + "</td>");	
            } else if ( receiver[0].compareTo("exon") == 0 ) {	
                if (autosomal == false) {	
                    for ( int k = 0; k < mutstart.length; k++ ) {	
                        if ( mutstart[k] >= start && mutstart[k] <= end ) {	
                            int poslink = mutstart[k];	
                            if (k > 0) {	
                                poslink += poscounter;	
                            }	
                            muttrack = session + "_" + String.valueOf(k) + "-" + String.valueOf(mutstart[k]);	
                            String[] seqlink = LinkWriter(poslink, seqstring1, refstring[k], muttrack, mutstring[k], start);	
                            seqstring1 = seqlink[0];	
                            int seqlength = Integer.parseInt(seqlink[1]);	
                            poscounter += seqlength - 1;	
                        }	
                    }	
                    pWriter.println("           <td style=\"background-color:#bababa\">" + ColumnFormatter(seqstring1) + "</td>");	
                } else if (autosomal == true) {	
                    allelepos = AlleleSorting(alleles);	
                    for ( int k = 0; k < allelepos[0].length; k++) {	
                        if ( mutstart[allelepos[0][k]] >= start && mutstart[allelepos[0][k]] <= end ) {	
                            int poslink_a = mutstart[allelepos[0][k]];	
                            if ( k > 0 ) {	
                                poslink_a += poscounter_a;	
                            }	
                            muttrack = session + "_" + String.valueOf(k) + "-" + String.valueOf(mutstart[allelepos[0][k]]);	
                            String[] seqlink = LinkWriter(poslink_a, seqstring1, refstring[allelepos[0][k]], muttrack, mutstring[allelepos[0][k]], 1);	
                            seqstring1 = seqlink[0];	
                            int seqlength = Integer.parseInt(seqlink[1]);	
                            poscounter_a += seqlength - 1;	
                        }                        	
                    }	
                    pWriter.println("           <td style=\"background-color:#bababa\">" + ColumnFormatter(seqstring1) + "</td>");	
                    for ( int m = 0; m < allelepos[1].length; m++) {	
                        if ( mutstart[allelepos[1][m]] >= start && mutstart[allelepos[1][m]] <= end ) {	
                            int poslink_b = mutstart[allelepos[1][m]];	
                            if ( m > 0 ) {	
                                poslink_b += poscounter_b;	
                            }	
                            muttrack = session + "_" + String.valueOf(m) + "-" + String.valueOf(mutstart[allelepos[1][m]]);	
                            String[] seqlink = LinkWriter(poslink_b, seqstring2, refstring[allelepos[1][m]], muttrack, mutstring[allelepos[1][m]], 1);	
                            seqstring2 = seqlink[0];	
                            int seqlength = Integer.parseInt(seqlink[1]);	
                            poscounter_b += seqlength - 1;	
                        }                  	
                    }	
                    pWriter.println("           <td style=\"background-color:#bababa\">" + ColumnFormatter(seqstring2) + "</td>");	
                }                	
            }	
            pWriter.println("       </tr>");	
            start = end + 1;	
        }	
        pWriter.println("       </tbody>");	
        pWriter.println("   </table>");	
        pWriter.println("</div>");	
        pWriter.println("<hr><br>");	
    }	
    	
    private void CDNAProtMutationPrinter(PrintWriter pWriter, Vector cdnaprotMutationHandler, String marker, Vector cdnaprotOrig, String session, boolean autosomal) {	
        String genename = session.split("-")[0];	
        if (marker.equals("CDNA")) {	
            pWriter.println("<p>Reported cDNA mutations in <b>" + genename + "</b>:</p>");	
        } else if (marker.equals("PROT")) {	
            pWriter.println("Reported Protein mutations:");	
        }	
        pWriter.println("<div class=\"seqrep\" style=\"width:36em;\">");	
        pWriter.println("   <table class=\"CDNAPROT\">");	
        pWriter.println("       <tbody style=\"font-family:courier,monospace;font-size:14pt;\">");	
        String[] receiver = (String[]) cdnaprotOrig.get(0);	
        Pattern cleanpat = Pattern.compile("[\n\t]");	
        Matcher cleanmat = cleanpat.matcher(receiver[1].replaceAll(" ", ""));	
        String seqstring1 = cleanmat.replaceAll("");	
        String seqstring2 = seqstring1;	
        int[] mutstart = new int[cdnaprotMutationHandler.size()];	
        String[] mutstring = new String[cdnaprotMutationHandler.size()];	
        String[] refstring = new String[cdnaprotMutationHandler.size()];	
        Integer[] alleles = new Integer[cdnaprotMutationHandler.size()];	
        String muttrack = new String();	
        for ( int h = 0; h < cdnaprotMutationHandler.size(); h++) {	
            mutstart[h] = Integer.parseInt( (String)((HashMap)cdnaprotMutationHandler.get(h)).get( "start_nr" ) );	
            mutstring[h] = (String)((HashMap)cdnaprotMutationHandler.get(h)).get( "mutation_sequence" );	
            refstring[h] = (String)((HashMap)cdnaprotMutationHandler.get(h)).get( "reference_sequence" );	
            alleles[h] = Integer.valueOf( (String)((HashMap)cdnaprotMutationHandler.get(h)).get( "allele" ) );	
        }	
        int poscounter = 0;	
        int poscounter_a = 0;	
        int poscounter_b = 0;	
        int[][] allelepos = new int[2][];	
        if (autosomal == false) {	
            for ( int j = 0; j < mutstart.length; j++) {	
                int poslink = mutstart[j];	
                if ( j > 0 ) {	
                    poslink += poscounter;	
                }	
                muttrack = session + "_" + String.valueOf(j) + "-" + String.valueOf(mutstart[j]);	
                String[] seqlink = LinkWriter(poslink, seqstring1, refstring[j], muttrack, mutstring[j] , 1);	
                seqstring1 = seqlink[0];	
                int seqlength = Integer.parseInt(seqlink[1]);	
                poscounter += seqlength - 1;	
            }	
            pWriter.println("           <tr><td class=\"mutseq\">" + ColumnFormatter(seqstring1) + "</td></tr>");	
        } else if (autosomal == true) {	
            pWriter.println("       <tr class=\"header\">");	
            pWriter.println("           <th>Allele 1</th>");	
            pWriter.println("           <th>Allele 2</th>");	
            pWriter.println("       </tr>");	
            pWriter.println("       <tr>");	
            allelepos = AlleleSorting(alleles);	
            for ( int k = 0; k < allelepos[0].length; k++) {	
                int poslink_a = mutstart[allelepos[0][k]];	
                if ( k > 0 ) {	
                    poslink_a += poscounter_a;	
                }	
                muttrack = session + "_" + String.valueOf(k) + "-" + String.valueOf(mutstart[allelepos[0][k]]);	
                String[] seqlink = LinkWriter(poslink_a, seqstring1, refstring[allelepos[0][k]], muttrack, mutstring[allelepos[0][k]], 1);	
                seqstring1 = seqlink[0];	
                int seqlength = Integer.parseInt(seqlink[1]);	
                poscounter_a += seqlength - 1;	
            }	
            pWriter.println("           <td class=\"mutseq\">" + ColumnFormatter(seqstring1) + "</td>");	
            for ( int m = 0; m < allelepos[1].length; m++) {	
                int poslink_b = mutstart[allelepos[1][m]];	
                if ( m > 0 ) {	
                    poslink_b += poscounter_b;	
                }	
                muttrack = session + "_" + String.valueOf(m) + "-" + String.valueOf(mutstart[allelepos[1][m]]);	
                String[] seqlink = LinkWriter(poslink_b, seqstring2, refstring[allelepos[1][m]], muttrack, mutstring[allelepos[1][m]], 1);	
                seqstring2 = seqlink[0];	
                int seqlength = Integer.parseInt(seqlink[1]);	
                poscounter_b += seqlength - 1;	
            }	
            pWriter.println("           <td class=\"mutseq\">" + ColumnFormatter(seqstring2) + "</td>");	
            pWriter.println("       </tr>");	
        }	
        pWriter.println("       </tbody>");	
        pWriter.println("   </table>");	
        pWriter.println("</div>");	
        pWriter.println("<hr><br>");	
    }	
    	
    private int[][] AlleleSorting(Integer[] alleles) {	
        int[][] allelepos = new int[2][];	
        int i = 0;	
        int j = 0;	
        int k = 0;	
        List allelelist = Arrays.asList(alleles);	
        Iterator alleleit = allelelist.iterator();	
        while (alleleit.hasNext()) {	
            Integer alleleval = (Integer) alleleit.next();	
            if (alleleval.intValue() == 1) {	
                j++;	
            } else if (alleleval.intValue() == 2) {	
                k++;	
            }	
            i++;	
        }	
        int[] allele1 = new int[j];	
        int[] allele2 = new int[k];	
        int m = 0;	
        int n = 0;	
        for (int l = 0; l < alleles.length; l++) {	
            if (alleles[l].intValue() == 1) {	
                allele1[m] = l;	
                m++;	
            } else if (alleles[l].intValue() == 2) {	
                allele2[n] = l;	
                n++;	
            }	
        }	
        allelepos[0] = allele1;	
        allelepos[1] = allele2;	
        return allelepos;	
    }	
    	
    private String[] LinkWriter(int start, String patseq, String refseq, String track, String mutseq, int init) {	
        String[] link = new String[2];	
        String mutbefore = patseq.substring(0, start - init);	
        String mutafter = patseq.substring(start + refseq.length() - init);	
        String mutlink = "<a class=\"mutlink\" href=\"javascript:MUTwin('/EIDPWebApp/tools/jsp/MutationZoomer.jsp?track=" + track + "&refseq=" + refseq + "&mutseq=" + mutseq + "')\" style=\"background-color:yellow\">" + refseq + "</a>";	
        link[0] = mutbefore + mutlink + mutafter;	
        link[1] = String.valueOf(mutlink.length());	
        return link;	
    }	
    	
    private String IntronTrimmer(String intronseq) {	
        String intronfinal = new String();	
        String intronhead = intronseq.substring(0, 25);	
        String introntail = intronseq.substring(intronseq.length() - 25, intronseq.length());	
        intronfinal = intronhead + ".........." + introntail;	
        return intronfinal;	
    }	
	
    private String ColumnFormatter(String inputstr) {	
        String formattedstr = new String();	
        String factory = inputstr;	
        String prestr = new String();	
        String posstr = new String();	
        String groupfac = new String();	
        String receiver = new String();	
        Pattern linkpat = Pattern.compile("<[^>]*>");        	
        String cleanstr = factory.replaceAll("<[^>]*>","");        	
        if (cleanstr.length() > 60) {	
            int width = cleanstr.length() / 60;	
            for (int i = 1; i <= width; i++) {	
                int counter = 60;	
                int lwidth = 0;	
                Matcher matli = linkpat.matcher(factory);	
                while (matli.find() && (matli.start() - lwidth) < 60) {	
                    groupfac = matli.group();	
                    lwidth += groupfac.length();	
                }	
                counter += lwidth;	
                receiver += factory.substring(0, counter) + "<br>";	
                factory = factory.substring(counter);	
                matli.reset();	
                if (i == width) {	
                    receiver += factory;	
                }	
            }	
            formattedstr = receiver;	
        } else {	
            formattedstr = inputstr;	
        }	
        return formattedstr;	
    }	
	
}	
