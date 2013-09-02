/*
 * RTFGenerator.java
 *
 * Created on 1. Juli 2004, 09:17
 */

package com.eidp.Generator;

import com.eidp.xml.XMLDataAccess ;

import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.InputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.OutputStreamWriter ;
import java.io.ByteArrayInputStream ;
import java.text.ParseException;
import java.util.Vector ;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The EIDP RTF Generator generates RTF documents from an
 * xml document.
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

public class RTFGenerator implements GeneratorAPI {
    
    private XMLDataAccess xmlDataAccess ;
    
    private String generatedDocument = "" ;
    private String xmlDataString = "" ;
    private String strWaterMark = "" ;
    
    private NodeList reportNode ;
    
    private Document xmlDocument ;
    
    private Element initElement ;
    
    private boolean initText = true ;
    
    private String locale = "en_GB";
    
    /**
     * Creates a new instance of RTFGenerator with the xml document
     * specified as Element.
     * @param initElement the xml document as Element to be processed.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public RTFGenerator(Element initElement , boolean isDraft) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        this.xmlDataAccess = new XMLDataAccess( initElement ) ;
        if( isDraft ){
            this.strWaterMark = this.getDiagWaterMark() ;
        }
        // !!! Check for removed preloads done. Works.
        this.initElement = initElement ;
        this.generateDocument( (NodeList)initElement ) ;
    }
    
    /**
     * Creates a new instance of RTFGenerator with the xml document
     * specified as filename. The second argument is the report-name
     * to be processed in this document.
     * @param xmlDataString filename of the xml-file to be read in.
     * @param report name of the report to be generated from the xml
     * document.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public RTFGenerator(String xmlDataString , String report , boolean isDraft) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        this.xmlDataString = xmlDataString ;
        if( isDraft ){
            this.strWaterMark = this.getDiagWaterMark() ;
        }
        InputStream xmlInputStream = this.convert( xmlDataString ) ;
        this.xmlDataAccess = new XMLDataAccess( xmlInputStream ) ;
        this.generateDocument( report ) ;
    }
    
    private void generateDocument( NodeList reportNode ) throws org.xml.sax.SAXException {
        // Locale configuration: David
        Vector localeConf = new Vector();
        localeConf = (Vector) this.xmlDataAccess.getElementsByName("locale", 
                reportNode);
        if (!localeConf.isEmpty()) {
            this.locale = (String) localeConf.get(0);
        }
        
        Vector reportHeaderVector = new Vector() ;
        reportHeaderVector = this.xmlDataAccess.getNodeListsByName( "header", reportNode ) ;
        if( reportHeaderVector.size() > 0 ){
            Vector appVector = new Vector() ;
            appVector = this.xmlDataAccess.getNodeListsByName( "header,application,name", reportNode ) ;
            NodeList appname = (NodeList)appVector.get( 0 ) ;
            String strAppName = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , appname )).get( 0 ) ;
            if(strAppName.equals("rheuma")){
                this.writeRheumaHeader( reportNode ) ;
            }else if(strAppName.equals("transplant")){
                this.writeTransplantHeader( reportNode ) ;
            }else if(strAppName.equals("hiv-data-online")){
                this.writeHIVHeader( reportNode ) ;
            }else if(strAppName.equals("esid")){
                this.writeEsidHeader( reportNode ) ;
            }
        }else{
            this.writeRTFHeader() ;
            this.writeRTFFooter( reportNode ) ;
            this.writeBridge() ;
        }
        this.generateSections( reportNode ) ;
        this.closeRTF() ;
    }
    
    private void generateDocument( String report ) throws org.xml.sax.SAXException {
        // 1. get the correct report by name:
        Vector reportVector = new Vector() ;
        reportVector = this.xmlDataAccess.getNodeListsByName( "report" ) ;
        Vector reportName = new Vector() ;
        reportName = this.xmlDataAccess.getElementsByName( "report,report-name" ) ;
        NodeList reportNode = null ;
        if ( reportVector.size() != reportName.size() ) {
            throw new org.xml.sax.SAXException( "Reports and report names do not match." ) ;
        }
        for ( int ri = 0 ; ri < reportVector.size() ; ri++ ) {
            if ( ((String)reportName.get( ri )).equals( report ) ) {
                reportNode = (NodeList)reportVector.get( ri ) ;
                break ;
            }
        }
        Vector reportHeaderVector = new Vector() ;
        reportHeaderVector = this.xmlDataAccess.getNodeListsByName( "header", reportNode ) ;
        if( reportHeaderVector.size() > 0 ){
            Vector appVector = new Vector() ;
            appVector = this.xmlDataAccess.getNodeListsByName( "header,application,name", reportNode ) ;
            NodeList appname = (NodeList)appVector.get( 0 ) ;
            String strAppName = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , appname )).get( 0 ) ;
            if(strAppName.equals("rheuma")){
                this.writeRheumaHeader( reportNode ) ;
            }else if(strAppName.equals("transplant")){
                this.writeTransplantHeader( reportNode ) ;
            }else if(strAppName.equals("hiv-data-online")){
                this.writeHIVHeader( reportNode ) ;
            }else if(strAppName.equals("esid")){
                this.writeEsidHeader( reportNode ) ;
            }
        }else{
            this.writeRTFHeader() ;
            this.writeRTFFooter( reportNode ) ;
            this.writeBridge() ;
        }
        this.generateSections( reportNode ) ;
        this.closeRTF() ;
    }
    
    private void writeRTFHeader() {
        
        String header = "{\\rtf1\\ansi\\deff1\\adeflang1025\n";
        header += "{\\fonttbl{\\f0\\froman\\fprq2\\fcharset0 Arial{\\*\\falt Verdana};}{\\f1\\froman\\fprq2\\fcharset0 Arial;}{\\f2\\froman\\fprq2\\fcharset0 Arial;}{\\f3\\fnil\\fprq0\\fcharset2 StarSymbol;}{\\f4\\fnil\\fprq2\\fcharset0 HG Mincho Light J;}}\n" ;
        header += "{\\colortbl;\\red0\\green0\\blue0;\\red128\\green128\\blue128;}\n";
        header += "{\\stylesheet{\\s1\\rtlch\\lang255\\snext1 Standard;}\n";
        header += "{\\s2\\cf1\\rtlch\\lang255\\ltrch\\dbch\\af4\\loch\\lang255\\sbasedon1\\snext2 Default;}\n";
        header += "{\\s3\\sa283\\brdrb\\brdrdb\\brdrw15\\brdrcf2\\brsp0{\\*\\brdrb\\brdlncol2\\brdlnin1\\brdlnout1\\brdlndist20}\\brsp0\\cf1\\rtlch\\afs12\\lang255\\ltrch\\dbch\\af4\\afs12\\loch\\fs12\\lang255\\sbasedon2\\snext4 Horizontal Line;}\n";
        header += "{\\s4\\sa120\\cf1\\rtlch\\lang255\\ltrch\\dbch\\af4\\loch\\lang255\\sbasedon2\\snext4 Text body;}\n";
        header += "{\\s5\\sa120\\cf1\\rtlch\\lang255\\ltrch\\dbch\\af4\\loch\\lang255\\sbasedon4\\snext5 Table Contents;}\n";
        header += "{\\s6\\sa120\\cf1\\qc\\rtlch\\lang255\\ltrch\\dbch\\af4\\loch\\lang255\\i\\b\\sbasedon5\\snext6 Table Heading;}\n";
        header += "{\\s7\\sa120\\rtlch\\lang255\\sbasedon1\\snext7 Textk\\'f6rper;}\n";
        header += "{\\s8\\sa120\\rtlch\\lang255\\sbasedon7\\snext8 Tabellen Inhalt;}\n";
        header += "{\\s9\\sa120\\qc\\rtlch\\lang255\\ltrch\\loch\\i\\b\\sbasedon8\\snext9 Tabellen \\'dcberschrift;}\n";
        header += "{\\*\\cs11\\cf0\\rtlch\\af1\\lang255\\ltrch\\dbch\\af4\\loch\\lang255 Numbering Symbols;}\n";
        header += "{\\*\\cs12\\cf0\\rtlch\\af3\\afs18\\lang255\\ltrch\\dbch\\af3\\afs18\\loch\\f3\\fs18\\lang255 Bullet Symbols;}\n";
        header += "}\n";
        header += "{\\info{\\comment EIDP Report Generator}{\\vern6410}}\\deftab1250\n";
        this.generatedDocument += header ;
    }
    
    private void writeHIVHeader( NodeList reportNode ) throws org.xml.sax.SAXException {
        
        // get date of today
        ReportContentFunctions rcf = new ReportContentFunctions();
        String strActualDate = rcf.getActualDate();
        
        // get Footer-text
        Vector footerSections = new Vector() ;
        footerSections = this.xmlDataAccess.getNodeListsByName( "footer" , reportNode ) ;
        String strFooterText = "";
        for ( int rs = 0 ; rs < footerSections.size() ; rs++ ) {
            NodeList footer = (NodeList)footerSections.get( rs ) ;
            String footerSectionType = (String)((Vector)this.xmlDataAccess.getElementsByName( "footer-type" , footer )).get( 0 ) ;
            if ( footerSectionType.equals( "text" ) ) {
                strFooterText += this.getTextSection( footer , true , false , "" , false ) ;
            }
        }
        
        // get arzt-anrede
//        Vector reportHeaderAnrede = new Vector() ;
//        reportHeaderAnrede = this.xmlDataAccess.getNodeListsByName( "header,adress-physician,anrede", reportNode ) ;
//        NodeList anrede = (NodeList)reportHeaderAnrede.get( 0 ) ;
//        String strReportHeaderArztAnrede = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , anrede )).get( 0 ) ;
        String strReportHeaderArztAnrede = "";
        // get the name of the physician
        
        // get letter destination: physician or patient.
        Vector reportDestinationAdress = new Vector() ;
        reportDestinationAdress = this.xmlDataAccess.getNodeListsByName( "header,letter,destination,contents", reportNode ) ;
        NodeList destinationNode = (NodeList)reportDestinationAdress.get( 0 ) ;
        String strReportDestinationAdress = (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , destinationNode )).get( 0 ) ;
        
        String strReportDestinationTag = "adress-physician";
        if( strReportDestinationAdress.trim().equals("nach Hause") ){
            strReportDestinationTag = "adress-patient";
        }
        
        Vector reportHeaderArztName = new Vector() ;
        reportHeaderArztName = this.xmlDataAccess.getNodeListsByName( "header," + strReportDestinationTag + ",name,contents", reportNode ) ;
        String strReportHeaderArztName = "";
        for(int i = 0 ; i < reportHeaderArztName.size() ; i++ ){
            NodeList arztname = (NodeList)reportHeaderArztName.get( i ) ;
            strReportHeaderArztName += (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , arztname )).get( 0 ) ;
        }
        strReportHeaderArztName = strReportHeaderArztName.trim();
        
        // get the street of the physician/patient
        Vector reportHeaderArztStrasse = new Vector() ;
        reportHeaderArztStrasse = this.xmlDataAccess.getNodeListsByName( "header," + strReportDestinationTag + ",street", reportNode ) ;
        NodeList arztstrasse = (NodeList)reportHeaderArztStrasse.get( 0 ) ;
        String strReportHeaderArztStrasse = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , arztstrasse )).get( 0 ) ;
        
        // get the city and postcode of the physician/patient
        Vector reportHeaderArztOrt = new Vector() ;
        reportHeaderArztOrt = this.xmlDataAccess.getNodeListsByName( "header," + strReportDestinationTag + ",city,contents", reportNode ) ;
        String strReportHeaderArztOrt = "";
        for(int i = 0 ; i < reportHeaderArztOrt.size() ; i++ ){
            NodeList arztort = (NodeList)reportHeaderArztOrt.get( i ) ;
            strReportHeaderArztOrt += (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , arztort )).get( 0 ) ;
        }
        
        String header = "{\\rtf1\\ansi\\ansicpg1252\\uc1\\deff1\\stshfdbch0\\stshfloch0\\stshfhich0\\stshfbi0\\deflang1031\\deflangfe1031{\\fonttbl{\\f0\\froman\\fcharset0\\fprq2{\\*\\panose 02020603050405020304}Arial{\\*\\falt Arial};}\n" ;
        header += "{\\f1\\fswiss\\fcharset0\\fprq2{\\*\\panose 020b0604020202020204}Arial{\\*\\falt Verdana};}{\\f36\\fswiss\\fcharset0\\fprq2{\\*\\panose 00000000000000000000}Helvetica-Light;}{\\f37\\froman\\fcharset238\\fprq2 Arial CE{\\*\\falt Arial};}\n" ;
        header += "{\\f38\\froman\\fcharset204\\fprq2 Arial Cyr{\\*\\falt Arial};}{\\f40\\froman\\fcharset161\\fprq2 Arial Greek{\\*\\falt Arial};}{\\f41\\froman\\fcharset162\\fprq2 Arial Tur{\\*\\falt Arial};}\n" ;
        header += "{\\f42\\froman\\fcharset177\\fprq2 Arial (Hebrew){\\*\\falt Arial};}{\\f43\\froman\\fcharset178\\fprq2 Arial (Arabic){\\*\\falt Arial};}{\\f44\\froman\\fcharset186\\fprq2 Arial Baltic{\\*\\falt Arial};}\n" ;
        header += "{\\f45\\froman\\fcharset163\\fprq2 Arial (Vietnamese){\\*\\falt Arial};}{\\f47\\fswiss\\fcharset238\\fprq2 Arial CE{\\*\\falt Verdana};}{\\f48\\fswiss\\fcharset204\\fprq2 Arial Cyr{\\*\\falt Verdana};}\n" ;
        header += "{\\f50\\fswiss\\fcharset161\\fprq2 Arial Greek{\\*\\falt Verdana};}{\\f51\\fswiss\\fcharset162\\fprq2 Arial Tur{\\*\\falt Verdana};}{\\f52\\fswiss\\fcharset177\\fprq2 Arial (Hebrew){\\*\\falt Verdana};}{\\f53\\fswiss\\fcharset178\\fprq2 Arial (Arabic){\\*\\falt Verdana};}\n" ;
        header += "{\\f54\\fswiss\\fcharset186\\fprq2 Arial Baltic{\\*\\falt Verdana};}{\\f55\\fswiss\\fcharset163\\fprq2 Arial (Vietnamese){\\*\\falt Verdana};}}{\\colortbl;\\red0\\green0\\blue0;\\red0\\green0\\blue255;\\red0\\green255\\blue255;\\red0\\green255\\blue0;\\red255\\green0\\blue255;\n" ;
        header += "\\red255\\green0\\blue0;\\red255\\green255\\blue0;\\red255\\green255\\blue255;\\red0\\green0\\blue128;\\red0\\green128\\blue128;\\red0\\green128\\blue0;\\red128\\green0\\blue128;\\red128\\green0\\blue0;\\red128\\green128\\blue0;\\red128\\green128\\blue128;\\red192\\green192\\blue192;}\n" ;
        header += "{\\stylesheet{\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\snext0 \\styrsid7158313 Normal;}{\\*\\cs10 \\additive \\ssemihidden Default Paragraph Font;}{\\*\n" ;
        header += "\\ts11\\tsrowd\\trftsWidthB3\\trpaddl108\\trpaddr108\\trpaddfl3\\trpaddft3\\trpaddfb3\\trpaddfr3\\tscellwidthfts0\\tsvertalt\\tsbrdrt\\tsbrdrl\\tsbrdrb\\tsbrdrr\\tsbrdrdgl\\tsbrdrdgr\\tsbrdrh\\tsbrdrv \n" ;
        header += "\\ql \\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\fs20\\lang1024\\langfe1024\\cgrid\\langnp1024\\langfenp1024 \\snext11 \\ssemihidden Normal Table;}{\\s15\\ql \\li0\\ri0\\sl240\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f36\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext15 \\ssemihidden \\styrsid7158313 annotation text;}{\\s16\\ql \\li0\\ri0\\sl140\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext16 \\ssemihidden \\styrsid7158313 Absenderadresse;}{\\s17\\ql \\li0\\ri0\\sl180\\slmult0\n" ;
        header += "\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext17 \\styrsid7158313 Klinik;}{\n" ;
        header += "\\s18\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \\b\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext18 \\styrsid7158313 Abteilung;}{\\s19\\ql \\li0\\ri0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs24\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext19 \\styrsid7158313 Bereich;}{\\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\n" ;
        header += "\\tx567\\pvpg\\phpg\\posx6520\\posy3514\\absh-2277\\absw2380\\abslock1\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\shading10000\\cfpat8 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext20 \\styrsid7158313 Einrichtungsleitung;}{\\s21\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext21 \\ssemihidden \\styrsid7158313 Closing;}{\\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx9156\\posy3514\\absh-2624\\absw2624\\abslock1\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext22 \\styrsid7158313 Absenderangaben;}{\\s23\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext23 \\styrsid947662 header;}{\\s24\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext24 \\styrsid947662 footer;}{\\*\\cs25 \\additive \\sbasedon10 \\styrsid947662 page number;}}{\\*\\rsidtbl \\rsid680207\\rsid947662\\rsid1050224\\rsid2361660\\rsid2495643\\rsid2641361\n" ;
        header += "\\rsid2646936\\rsid3309508\\rsid3344382\\rsid3622531\\rsid4010742\\rsid4205030\\rsid4478637\\rsid4526417\\rsid4612648\\rsid5572086\\rsid6254617\\rsid6750650\\rsid6831816\\rsid6848431\\rsid6900398\\rsid7158313\\rsid7289481\\rsid7879388\\rsid8664068\\rsid9779576\\rsid10380151\n" ;
        header += "\\rsid10497578\\rsid10580602\\rsid10910343\\rsid11237530\\rsid12654584\\rsid13434924\\rsid13968944\\rsid15027781\\rsid15673768\\rsid15729916\\rsid15803792}{\\*\\generator Microsoft Word 10.0.2627;}{\\info{\\title Medizinische Klinik}{\\author Stephan Rusch}\n" ;
        header += "{\\operator Stephan Rusch}{\\creatim\\yr2005\\mo8\\dy11\\hr15\\min35}{\\revtim\\yr2005\\mo8\\dy11\\hr15\\min35}{\\version2}{\\edmins0}{\\nofpages1}{\\nofwords75}{\\nofchars479}{\\*\\company CwebRD Universit\\'e4tsklinik Freiburg}{\\nofcharsws553}{\\vern16437}}\n" ;
        header += "\\paperw11906\\paperh16838\\margl1417\\margr1417\\margt1417\\margb1134 \\deftab708\\widowctrl\\ftnbj\\aenddoc\\hyphhotz425\\noxlattoyen\\expshrtn\\noultrlspc\\dntblnsbdb\\nospaceforul\\hyphcaps0\\formshade\\horzdoc\\dgmargin\\dghspace180\\dgvspace180\\dghorigin1417\n" ;
        header += "\\dgvorigin1417\\dghshow1\\dgvshow1\\jexpand\\viewkind1\\viewscale117\\viewzk2\\pgbrdrhead\\pgbrdrfoot\\splytwnine\\ftnlytwnine\\htmautsp\\nolnhtadjtbl\\useltbaln\\alntblind\\lytcalctblwd\\lyttblrtgr\\lnbrkrule\\nobrkwrptbl\\snaptogridincell\\allowfieldendsel\n" ;
        header += "\\wrppunct\\asianbrkrule\\rsidroot7158313 \\donotshowmarkup1\\fet0\\sectd \\linex0\\headery708\\footery708\\colsx708\\endnhere\\sectlinegrid360\\sectdefaultcl\\sftnbj " + strWaterMark + "{\\footer \\pard\\plain \\s24\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\n" ;
        header += "\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\fs16\\insrsid947662\\charrsid947662 " + strFooterText + "\\tab }{\\field{\\*\\fldinst {\n" ;
        header += "\\cs25\\fs16\\insrsid947662\\charrsid947662  PAGE }}{\\fldrslt {\\cs25\\fs16\\lang1024\\langfe1024\\noproof\\insrsid10910343 1}}}{\\fs16\\insrsid947662\\charrsid947662 \n" ;
        header += "\\par }}{\\*\\pnseclvl1\\pnucrm\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl2\\pnucltr\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl3\\pndec\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl4\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxta )}}\n" ;
        header += "{\\*\\pnseclvl5\\pndec\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl6\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl7\\pnlcrm\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl8\n" ;
        header += "\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl9\\pnlcrm\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}\\pard\\plain \\s17\\ql \\li0\\ri0\\sl180\\slmult0\n" ;
        header += "\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\n" ;
        header += "\\lang1024\\langfe1024\\noproof\\insrsid10910343 {\\shp{\\*\\shpinst\\shpleft1247\\shptop2884\\shpright5841\\shpbottom3217\\shpfhdr0\\shpbxpage\\shpbxignore\\shpbypage\\shpbyignore\\shpwr3\\shpwrk0\\shpfblwtxt0\\shpz0\\shplockanchor\\shplid1026\n" ;
        header += "{\\sp{\\sn shapeType}{\\sv 202}}{\\sp{\\sn fFlipH}{\\sv 0}}{\\sp{\\sn fFlipV}{\\sv 0}}{\\sp{\\sn lTxid}{\\sv 65536}}{\\sp{\\sn dxTextLeft}{\\sv 0}}{\\sp{\\sn dyTextTop}{\\sv 0}}{\\sp{\\sn dxTextRight}{\\sv 0}}{\\sp{\\sn dyTextBottom}{\\sv 0}}{\\sp{\\sn hspNext}{\\sv 1026}}\n" ;
        header += "{\\sp{\\sn fLine}{\\sv 0}}{\\sp{\\sn posrelh}{\\sv 1}}{\\sp{\\sn posrelv}{\\sv 1}}{\\sp{\\sn fLayoutInCell}{\\sv 1}}{\\sp{\\sn fAllowOverlap}{\\sv 1}}{\\sp{\\sn fLayoutInCell}{\\sv 1}}{\\shptxt \\pard\\plain \\s16\\ql \\li0\\ri0\\sl140\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 UNIVERSIT\\'c4TSKLINIKUM FREIBURG\n" ;
        header += "\\par }{\\b\\insrsid7158313 Rheumatologie und Klinische Immunologie, }{\\insrsid7158313 Hugstetterstr. 55, D-79106 Freiburg\n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313\\charrsid13434924 \n" ;
        header += "\\par }}}{\\shprslt{\\*\\do\\dobxpage\\dobypage\\dodhgt8192\\dptxbx\\dptxlrtb{\\dptxbxtext\\pard\\plain \\s16\\ql \\li0\\ri0\\sl140\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\n" ;
        header += "\\insrsid7158313 UNIVERSIT\\'c4TSKLINIKUM FREIBURG\n" ;
        header += "\\par }{\\b\\insrsid7158313 Rheumatologie und Klinische Immunologie, }{\\insrsid7158313 Hugstetterstr. 55, D-79106 Freiburg\n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313\\charrsid13434924 \n" ;
        header += "\\par }}\\dpx1247\\dpy2884\\dpxsize4594\\dpysize333\\dpfillfgcr255\\dpfillfgcg255\\dpfillfgcb255\\dpfillbgcr255\\dpfillbgcg255\\dpfillbgcb255\\dpfillpat1\\dplinehollow}}}}{\\insrsid7158313 Medizinische }{\\insrsid7158313\\charrsid6900398 Klinik\n" ;
        header += "\\par }\\pard\\plain \\s15\\ql \\li0\\ri0\\sl-180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f36\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\f1\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s18\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 Rheumatologie und Klinische Immunologie}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl-160\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\b\\fs16\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s19\\ql \\li0\\ri0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-1135\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid6848431 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs24\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\b0\\fs20\\insrsid7289481\\charrsid6848431 Medizinische Klinik\n" ;
        header += "\\par }{\\fs16\\insrsid7289481 \n" ;
        header += "\\par }{\\b0\\fs20\\insrsid6848431\\charrsid6848431 Abteilung }{\\b0\\fs20\\insrsid7289481\\charrsid6848431 Rheumatologie und Klinische Immunologie\n" ;
        header += "\\par }{\\fs16\\insrsid7289481\\charrsid7289481 \n" ;
        header += "\\par }{\\insrsid4612648 HIV}{\\insrsid7158313 -Ambulanz}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-1855\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid15027781 \\shading10000\\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 \\'c4rztlicher}{\\insrsid7158313\\charrsid6900398  Direktor:\n" ;
        header += "\\par Prof. Dr. }{\\insrsid7158313 med. H. H. Peter}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid7158313 Hugstetterstr. 55}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par D-}{\\insrsid7158313 79106}{\\insrsid7158313\\charrsid6900398  Freiburg\n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 Tel\\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 \\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 0761/270-344}{\n" ;
        header += "\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 8}{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 \n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 Sekrerariat\\tab 0761/270-3449\n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 Fax\\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 \\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 0761/270-3446\n" ;
        header += "\\par }\\pard\\plain \\s22\\ql \\fi-567\\li567\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-1855\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin567\\itap0\\pararsid15027781 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 E-Mail    peterhh@medizin.ukl.\n" ;
        header += "\\par \\tab  uni-freiburg.de\n" ;
        header += "\\par }\\pard\\plain \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-1855\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid15027781 \\shading10000\\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1040\\langfe1031\\langnp1040\\insrsid6831816\\charrsid6848431 \n" ;
        header += "\\par }\\pard \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx6521\\posy3521\\absh-1855\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid15027781 \\shading10000\\cfpat8 {\n" ;
        header += "\\insrsid9779576\\charrsid4612648 \n" ;
        header += "\\par }\\pard\\plain \\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx9134\\posy3521\\absh-2395\\absw2625\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid15027781 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid6848431 \n" ;
        header += "\\par }{\\insrsid4612648 \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par }\\pard\\plain \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx9134\\posy3521\\absh-2395\\absw2625\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid15027781 \\shading10000\\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid4612648\\charrsid680207 \n" ;
        header += "\\par }{\\lang2057\\langfe1031\\langnp2057\\insrsid4612648\\charrsid4612648 Labor\\tab \\tab 0761/270-3528\n" ;
        header += "\\par Station\\tab \\tab 0761/270-3735\n" ;
        header += "\\par }{\\insrsid7879388\\charrsid7879388 F}{\\insrsid4478637 ax}{\\insrsid7879388\\charrsid7879388 \\tab }{\\insrsid680207\\charrsid7879388 \\tab 0761/270-3}{\\insrsid4478637 44}{\\insrsid680207\\charrsid7879388 6}{\\insrsid4612648\\charrsid7879388 \n" ;
        header += "\\par }\\pard\\plain \\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx9134\\posy3521\\absh-2395\\absw2625\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid15027781 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid4612648\\charrsid7879388 Ambulanz}{\\insrsid7879388 arzt}{\\insrsid4612648\\charrsid7879388 \\tab 0761/270-}{\\insrsid680207\\charrsid7879388 7307}{\\insrsid4612648\\charrsid7879388 \n" ;
        header += "\\par }{\\insrsid680207\\charrsid7879388 Notf\\'e4lle}{\\insrsid4612648\\charrsid7879388 \\tab \\tab 0761/270-3}{\\insrsid680207\\charrsid7879388 401}{\\insrsid4612648\\charrsid7879388 \n" ;
        header += "\\par }{\\insrsid680207 \\tab }{\\insrsid4612648 \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid7158313\\charrsid6750650 Freiburg, " + strActualDate + "\n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\pvpg\\phpg\\posx1247\\posy3515\\absh-1440\\absw4309\\abslock1\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid10380151 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid5572086\\charrsid6750650 " + strReportHeaderArztAnrede + "}{\\insrsid10580602\\charrsid6750650 \n" ;
        header += "\\par }{\\insrsid7158313 " + strReportHeaderArztName + "\n" ;
        header += "\\par " + strReportHeaderArztStrasse + "\n" ;
        header += "\\par \n" ;
        header += "\\par }{\\b\\insrsid7158313\\charrsid4205030 " + strReportHeaderArztOrt + "}{\\insrsid7158313 \n" ;
        header += "\\par }\\pard \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 {\\insrsid1050224 \n" ;
        header += "\\par }{\\insrsid8664068 \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid15803792 \n" ;
        header += "\\par }{\\insrsid6254617 \n" ;
        header += "\\par \n" ;
        header += "\\par }\\pard \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid4010742 {\\insrsid4526417 \n" ;
        header += "\\par }{\\insrsid15027781 \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par }\n" ;
        
        this.generatedDocument += header ;
    }
    private void writeEsidHeader( NodeList reportNode ) throws org.xml.sax.SAXException {
    }
    private void writeTransplantHeader( NodeList reportNode ) throws org.xml.sax.SAXException {
        // get date of today
        ReportContentFunctions rcf = new ReportContentFunctions();
        String strActualDate = rcf.getActualDate();
        
        // get Footer-text
        Vector footerSections = new Vector() ;
        footerSections = this.xmlDataAccess.getNodeListsByName( "footer" , reportNode ) ;
        String strFooterText = "";
        for ( int rs = 0 ; rs < footerSections.size() ; rs++ ) {
            NodeList footer = (NodeList)footerSections.get( rs ) ;
            String footerSectionType = (String)((Vector)this.xmlDataAccess.getElementsByName( "footer-type" , footer )).get( 0 ) ;
            if ( footerSectionType.equals( "text" ) ) {
                strFooterText += this.getTextSection( footer , true , false , "" , false ) ;
            }
        }
        
        // get name of user
//        Vector reportHeaderDokumentarName = new Vector() ;
//        reportHeaderDokumentarName = this.xmlDataAccess.getNodeListsByName( "header,documentator,name", reportNode ) ;
//        NodeList dokumentarname = (NodeList)reportHeaderDokumentarName.get( 0 ) ;
//        String strReportHeaderDokumentarName = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , dokumentarname )).get( 0 ) ;
        
        // get user-email
//        Vector reportHeaderDokumentarEmail = new Vector() ;
//        reportHeaderDokumentarEmail = this.xmlDataAccess.getNodeListsByName( "header,documentator,email", reportNode ) ;
//        NodeList email = (NodeList)reportHeaderDokumentarEmail.get( 0 ) ;
//        String strReportHeaderDokumentarEmail = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , email )).get( 0 ) ;
        
        // get info, if we need home-physivian or nephro
        Vector destVector = new Vector() ;
        destVector = this.xmlDataAccess.getNodeListsByName( "header,letter,destination", reportNode ) ;
        NodeList dest = (NodeList)destVector.get( 0 ) ;
        String strDestination = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , dest )).get( 0 ) ;
        String strAddrContext = "adress-physician";
        if( strDestination.equals("2") ){
            strAddrContext = "adress-home-physician";
        }
        
        
        // get arzt-anrede
        Vector reportHeaderAnrede = new Vector() ;
        reportHeaderAnrede = this.xmlDataAccess.getNodeListsByName( "header," + strAddrContext + ",anrede", reportNode ) ;
        NodeList anrede = (NodeList)reportHeaderAnrede.get( 0 ) ;
        String strReportHeaderArztAnrede = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , anrede )).get( 0 ) ;
        
        // get the name of the physician
        Vector reportHeaderArztName = new Vector() ;
        reportHeaderArztName = this.xmlDataAccess.getNodeListsByName( "header," + strAddrContext + ",name,contents", reportNode ) ;
        String strReportHeaderArztName = "";
        for(int i = 0 ; i < reportHeaderArztName.size() ; i++ ){
            NodeList arztname = (NodeList)reportHeaderArztName.get( i ) ;
            strReportHeaderArztName += (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , arztname )).get( 0 ) ;
        }
        strReportHeaderArztName = strReportHeaderArztName.trim();
        
        // get the street (and Number) of the physician
        Vector reportHeaderArztStrasse = new Vector() ;
        reportHeaderArztStrasse = this.xmlDataAccess.getNodeListsByName( "header," + strAddrContext + ",street,contents", reportNode ) ;
        String strReportHeaderArztStrasse = "";
        for(int i = 0 ; i < reportHeaderArztStrasse.size() ; i++ ){
            NodeList arztstrasse = (NodeList)reportHeaderArztStrasse.get( i ) ;
            strReportHeaderArztStrasse += (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , arztstrasse )).get( 0 ) ;
        }
        
        // get the city and postcode of the physician
        Vector reportHeaderArztOrt = new Vector() ;
        reportHeaderArztOrt = this.xmlDataAccess.getNodeListsByName( "header," + strAddrContext + ",city,contents", reportNode ) ;
        String strReportHeaderArztOrt = "";
        for(int i = 0 ; i < reportHeaderArztOrt.size() ; i++ ){
            NodeList arztort = (NodeList)reportHeaderArztOrt.get( i ) ;
            strReportHeaderArztOrt += (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , arztort )).get( 0 ) ;
        }
        
        String header =  "{\\rtf1\\ansi\\ansicpg1252\\uc1\\deff1\\stshfdbch0\\stshfloch0\\stshfhich0\\stshfbi0\\deflang1031\\deflangfe1031{\\fonttbl{\\f0\\froman\\fcharset0\\fprq2{\\*\\panose 02020603050405020304}Arial;}\n" ;
        header += "{\\f1\\fswiss\\fcharset0\\fprq2{\\*\\panose 020b0604020202020204}Arial{\\*\\falt Verdana};}{\\f36\\fswiss\\fcharset0\\fprq2{\\*\\panose 00000000000000000000}Helvetica-Light;}{\\f37\\froman\\fcharset238\\fprq2 Arial CE;}\n" ;
        header += "{\\f38\\froman\\fcharset204\\fprq2 Arial Cyr;}{\\f40\\froman\\fcharset161\\fprq2 Arial Greek;}{\\f41\\froman\\fcharset162\\fprq2 Arial Tur;}{\\f42\\froman\\fcharset177\\fprq2 Arial (Hebrew);}\n" ;
        header += "{\\f43\\froman\\fcharset178\\fprq2 Arial (Arabic);}{\\f44\\froman\\fcharset186\\fprq2 Arial Baltic;}{\\f45\\froman\\fcharset163\\fprq2 Arial (Vietnamese);}{\\f47\\fswiss\\fcharset238\\fprq2 Arial CE{\\*\\falt Verdana};}\n" ;
        header += "{\\f48\\fswiss\\fcharset204\\fprq2 Arial Cyr{\\*\\falt Verdana};}{\\f50\\fswiss\\fcharset161\\fprq2 Arial Greek{\\*\\falt Verdana};}{\\f51\\fswiss\\fcharset162\\fprq2 Arial Tur{\\*\\falt Verdana};}{\\f52\\fswiss\\fcharset177\\fprq2 Arial (Hebrew){\\*\\falt Verdana};}\n" ;
        header += "{\\f53\\fswiss\\fcharset178\\fprq2 Arial (Arabic){\\*\\falt Verdana};}{\\f54\\fswiss\\fcharset186\\fprq2 Arial Baltic{\\*\\falt Verdana};}{\\f55\\fswiss\\fcharset163\\fprq2 Arial (Vietnamese){\\*\\falt Verdana};}}{\\colortbl;\\red0\\green0\\blue0;\\red0\\green0\\blue255;\n" ;
        header += "\\red0\\green255\\blue255;\\red0\\green255\\blue0;\\red255\\green0\\blue255;\\red255\\green0\\blue0;\\red255\\green255\\blue0;\\red255\\green255\\blue255;\\red0\\green0\\blue128;\\red0\\green128\\blue128;\\red0\\green128\\blue0;\\red128\\green0\\blue128;\\red128\\green0\\blue0;\n" ;
        header += "\\red128\\green128\\blue0;\\red128\\green128\\blue128;\\red192\\green192\\blue192;}{\\stylesheet{\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\snext0 \\styrsid7158313 Normal;}{\\*\\cs10 \\additive \\ssemihidden Default Paragraph Font;}{\\*\n" ;
        header += "\\ts11\\tsrowd\\trftsWidthB3\\trpaddl108\\trpaddr108\\trpaddfl3\\trpaddft3\\trpaddfb3\\trpaddfr3\\trcbpat1\\trcfpat1\\tscellwidthfts0\\tsvertalt\\tsbrdrt\\tsbrdrl\\tsbrdrb\\tsbrdrr\\tsbrdrdgl\\tsbrdrdgr\\tsbrdrh\\tsbrdrv \n" ;
        header += "\\ql \\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\fs20\\lang1024\\langfe1024\\cgrid\\langnp1024\\langfenp1024 \\snext11 \\ssemihidden Normal Table;}{\\s15\\ql \\li0\\ri0\\sl240\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f36\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext15 \\ssemihidden \\styrsid7158313 annotation text;}{\\s16\\ql \\li0\\ri0\\sl140\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext16 \\ssemihidden \\styrsid7158313 Absenderadresse;}{\\s17\\ql \\li0\\ri0\\sl180\\slmult0\n" ;
        header += "\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext17 \\styrsid7158313 Klinik;}{\n" ;
        header += "\\s18\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \\b\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext18 \\styrsid7158313 Abteilung;}{\\s19\\ql \\li0\\ri0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs24\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext19 \\styrsid7158313 Bereich;}{\\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\n" ;
        header += "\\tx567\\pvpg\\phpg\\posx6520\\posy3514\\absh-2277\\absw2380\\abslock1\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\shading10000\\cfpat8 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext20 \\styrsid7158313 Einrichtungsleitung;}{\\s21\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext21 \\ssemihidden \\styrsid7158313 Closing;}{\\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx9156\\posy3514\\absh-2624\\absw2624\\abslock1\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext22 \\styrsid7158313 Absenderangaben;}{\\s23\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext23 \\styrsid947662 header;}{\\s24\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext24 \\styrsid947662 footer;}{\\*\\cs25 \\additive \\sbasedon10 \\styrsid947662 page number;}}{\\*\\rsidtbl \\rsid599385\\rsid947662\\rsid1050224\\rsid2361660\\rsid2495643\\rsid2641361\n" ;
        header += "\\rsid2646936\\rsid3309508\\rsid3344382\\rsid3622531\\rsid4010742\\rsid4205030\\rsid4526417\\rsid4736569\\rsid6113438\\rsid6254617\\rsid6831816\\rsid6900398\\rsid7158313\\rsid7289481\\rsid8664068\\rsid9779576\\rsid10380151\\rsid10497578\\rsid11237530\\rsid12654584\n" ;
        header += "\\rsid13434924\\rsid13968944\\rsid14752189\\rsid15673768\\rsid15729916\\rsid15803792}{\\*\\generator Microsoft Word 10.0.2627;}{\\info{\\title Medizinische Klinik}{\\author Stephan Rusch}{\\operator Stephan Rusch}{\\creatim\\yr2005\\mo6\\dy7\\hr15\\min37}\n" ;
        header += "{\\revtim\\yr2005\\mo6\\dy20\\hr12\\min2}{\\version5}{\\edmins0}{\\nofpages1}{\\nofwords47}{\\nofchars301}{\\*\\company CwebRD Universit\\'e4tsklinik Freiburg}{\\nofcharsws347}{\\vern16437}}\\paperw11906\\paperh16838\\margl1417\\margr1417\\margt1417\\margb1134 \n" ;
        header += "\\deftab708\\widowctrl\\ftnbj\\aenddoc\\hyphhotz425\\noxlattoyen\\expshrtn\\noultrlspc\\dntblnsbdb\\nospaceforul\\hyphcaps0\\formshade\\horzdoc\\dgmargin\\dghspace180\\dgvspace180\\dghorigin1417\\dgvorigin1417\\dghshow1\\dgvshow1\n" ;
        header += "\\jexpand\\viewkind1\\viewscale117\\viewzk2\\pgbrdrhead\\pgbrdrfoot\\splytwnine\\ftnlytwnine\\htmautsp\\nolnhtadjtbl\\useltbaln\\alntblind\\lytcalctblwd\\lyttblrtgr\\lnbrkrule\\nobrkwrptbl\\snaptogridincell\\allowfieldendsel\\wrppunct\\asianbrkrule\\rsidroot7158313 \n" ;
        header += "\\donotshowmarkup1\\fet0\\sectd \\linex0\\headery708\\footery708\\colsx708\\endnhere\\sectlinegrid360\\sectdefaultcl\\sftnbj " + strWaterMark + "{\\footer \\pard\\plain \\s24\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\fs16\\insrsid947662\\charrsid947662 " + strFooterText + "\\tab }{\\field{\\*\\fldinst {\\cs25\\fs16\\insrsid947662\\charrsid947662  PAGE }}{\\fldrslt {\n" ;
        header += "\\cs25\\fs16\\lang1024\\langfe1024\\noproof\\insrsid4736569 1}}}{\\fs16\\insrsid947662\\charrsid947662 \n" ;
        header += "\\par }}{\\*\\pnseclvl1\\pnucrm\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl2\\pnucltr\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl3\\pndec\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl4\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxta )}}\n" ;
        header += "{\\*\\pnseclvl5\\pndec\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl6\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl7\\pnlcrm\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl8\n" ;
        header += "\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl9\\pnlcrm\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}\\pard\\plain \\s17\\ql \\li0\\ri0\\sl180\\slmult0\n" ;
        header += "\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\n" ;
        header += "\\lang1024\\langfe1024\\noproof\\insrsid4736569 {\\shp{\\*\\shpinst\\shpleft1247\\shptop2884\\shpright5841\\shpbottom3217\\shpfhdr0\\shpbxpage\\shpbxignore\\shpbypage\\shpbyignore\\shpwr3\\shpwrk0\\shpfblwtxt0\\shpz0\\shplockanchor\\shplid1026\n" ;
        header += "{\\sp{\\sn shapeType}{\\sv 202}}{\\sp{\\sn fFlipH}{\\sv 0}}{\\sp{\\sn fFlipV}{\\sv 0}}{\\sp{\\sn lTxid}{\\sv 65536}}{\\sp{\\sn dxTextLeft}{\\sv 0}}{\\sp{\\sn dyTextTop}{\\sv 0}}{\\sp{\\sn dxTextRight}{\\sv 0}}{\\sp{\\sn dyTextBottom}{\\sv 0}}{\\sp{\\sn hspNext}{\\sv 1026}}\n" ;
        header += "{\\sp{\\sn fLine}{\\sv 0}}{\\sp{\\sn posrelh}{\\sv 1}}{\\sp{\\sn posrelv}{\\sv 1}}{\\sp{\\sn fLayoutInCell}{\\sv 1}}{\\sp{\\sn fAllowOverlap}{\\sv 1}}{\\sp{\\sn fLayoutInCell}{\\sv 1}}{\\shptxt \\pard\\plain \\s16\\ql \\li0\\ri0\\sl140\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 UNIVERSIT\\'c4TSKLINIKUM FREIBURG\n" ;
        header += "\\par }{\\b\\insrsid14752189 Abt. Innere Medizin IV}{\\b\\insrsid7158313 , }{\\insrsid7158313 Hugstetterstr. 55, D-79106 Freiburg\n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313\\charrsid13434924 \n" ;
        header += "\\par }}}{\\shprslt{\\*\\do\\dobxpage\\dobypage\\dodhgt8192\\dptxbx\\dptxlrtb{\\dptxbxtext\\pard\\plain \\s16\\ql \\li0\\ri0\\sl140\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\n" ;
        header += "\\insrsid7158313 UNIVERSIT\\'c4TSKLINIKUM FREIBURG\n" ;
        header += "\\par }{\\b\\insrsid14752189 Abt. Innere Medizin IV}{\\b\\insrsid7158313 , }{\\insrsid7158313 Hugstetterstr. 55, D-79106 Freiburg\n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313\\charrsid13434924 \n" ;
        header += "\\par }}\\dpx1247\\dpy2884\\dpxsize4594\\dpysize333\\dpfillfgcr255\\dpfillfgcg255\\dpfillfgcb255\\dpfillbgcr255\\dpfillbgcg255\\dpfillbgcb255\\dpfillpat1\\dplinehollow}}}}{\\insrsid7158313 Medizinische }{\\insrsid7158313\\charrsid6900398 Klinik\n" ;
        header += "\\par }\\pard\\plain \\s15\\ql \\li0\\ri0\\sl-180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f36\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\f1\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s18\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 Rheumatologie und Klinische Immunologie}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl-160\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\b\\fs16\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s19\\ql \\li0\\ri0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-1135\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid4736569 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs24\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\b0\\fs20\\insrsid7289481\\charrsid4736569 Medizinische Klinik\n" ;
        header += "\\par }{\\fs20\\insrsid7289481\\charrsid4736569 \n" ;
        header += "\\par }{\\fs20\\insrsid14752189\\charrsid4736569 Nephrologie und Allgemeinmedizin\n" ;
        header += "\\par }{\\fs16\\insrsid7289481\\charrsid7289481 \n" ;
        header += "\\par }{\\insrsid14752189 Transplanta}{\\insrsid7158313 mbulanz}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-2575\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid9779576 \\shading10000\\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 \\'c4rztlicher}{\\insrsid7158313\\charrsid6900398  Direktor:\n" ;
        header += "\\par Prof. Dr. }{\\insrsid7158313 med. }{\\insrsid14752189 G. Walz}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid7158313 Hugstetterstr. 55}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par D-}{\\insrsid7158313 79106}{\\insrsid7158313\\charrsid6900398  Freiburg}{\\insrsid7158313 \n" ;
        header += "\\par }{\\insrsid14752189\\charrsid6900398 \n" ;
        header += "\\par }{\\insrsid7158313\\charrsid4736569 Tel}{\\insrsid4736569\\charrsid4736569 efon}{\\insrsid7158313\\charrsid4736569 \\tab }{\\insrsid9779576\\charrsid4736569 \\tab }{\\insrsid7158313\\charrsid4736569 0761/270-}{\\insrsid14752189\\charrsid4736569 3371}{\n" ;
        header += "\\insrsid7158313\\charrsid4736569 \n" ;
        header += "\\par Fax\\tab }{\\insrsid9779576\\charrsid4736569 \\tab }{\\insrsid7158313\\charrsid4736569 0761/270-}{\\insrsid14752189\\charrsid4736569 7321}{\\insrsid7158313\\charrsid4736569 \n" ;
        header += "\\par }{\\insrsid14752189\\charrsid4736569 \n" ;
        header += "\\par }\\pard\\plain \\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx6521\\posy3521\\absh-2575\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid14752189 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid14752189\\charrsid6900398 Freiburg, }{\\insrsid14752189 " + strActualDate + "}{\\insrsid14752189\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-2575\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid9779576 \\shading10000\\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid14752189\\charrsid4736569 \n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\pvpg\\phpg\\posx1247\\posy3515\\absh-1440\\absw4309\\abslock1\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid10380151 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid6113438 " + strReportHeaderArztAnrede + "}{\\insrsid7158313 \n" ;
        header += "\\par " + strReportHeaderArztName + "\n" ;
        header += "\\par " + strReportHeaderArztStrasse + "\n" ;
        header += "\\par \n" ;
        header += "\\par }{\\b\\insrsid7158313 " + strReportHeaderArztOrt + "}{\\insrsid7158313 \n" ;
        
//        header += "\\par }{\\b\\insrsid599385 7}{\\b\\insrsid7158313\\charrsid4205030 9100}{\\insrsid7158313  }{\\b\\insrsid7158313\\charrsid4205030 Freiburg}{\\insrsid7158313 \n" ;
        header += "\\par }\\pard \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 {\\insrsid1050224 \n" ;
        header += "\\par }{\\insrsid8664068 \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid15803792 \n" ;
        header += "\\par }{\\insrsid6254617 \n" ;
        header += "\\par \n" ;
        header += "\\par }\\pard \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid4010742 {\\insrsid4526417 \n" ;
//        header += "\\par }{\\insrsid4010742 \n" ;
//        header += "\\par \n" ;
//        header += "\\par }{\\insrsid4010742\\charrsid4010742 \n" ;
        header += "\\par }\n" ;
        
        this.generatedDocument += header ;
    }
    
    private void writeRheumaHeader( NodeList reportNode ) throws org.xml.sax.SAXException {
        
        // get date of today
        ReportContentFunctions rcf = new ReportContentFunctions();
        String strActualDate = rcf.getActualDate();
        
        // get Footer-text
        Vector footerSections = new Vector() ;
        footerSections = this.xmlDataAccess.getNodeListsByName( "footer" , reportNode ) ;
        String strFooterText = "";
        for ( int rs = 0 ; rs < footerSections.size() ; rs++ ) {
            NodeList footer = (NodeList)footerSections.get( rs ) ;
            String footerSectionType = (String)((Vector)this.xmlDataAccess.getElementsByName( "footer-type" , footer )).get( 0 ) ;
            if ( footerSectionType.equals( "text" ) ) {
                strFooterText += this.getTextSection( footer , true , false , "" , false ) ;
            }
        }
        // get loginname of user
        Vector reportHeaderDokumentarLogin = new Vector() ;
        reportHeaderDokumentarLogin = this.xmlDataAccess.getNodeListsByName( "header,documentator,login", reportNode ) ;
        NodeList dokumentarlogin = (NodeList)reportHeaderDokumentarLogin.get( 0 ) ;
        String strLogin = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , dokumentarlogin )).get( 0 ) ;
        
        // get name of user
        Vector reportHeaderDokumentarName = new Vector() ;
        reportHeaderDokumentarName = this.xmlDataAccess.getNodeListsByName( "header,documentator,name", reportNode ) ;
        NodeList dokumentarname = (NodeList)reportHeaderDokumentarName.get( 0 ) ;
        String strReportHeaderDokumentarName = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , dokumentarname )).get( 0 ) ;
        
        // get user-email
        Vector reportHeaderDokumentarEmail = new Vector() ;
        reportHeaderDokumentarEmail = this.xmlDataAccess.getNodeListsByName( "header,documentator,email", reportNode ) ;
        NodeList email = (NodeList)reportHeaderDokumentarEmail.get( 0 ) ;
        String strReportHeaderDokumentarEmail = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , email )).get( 0 ) ;
        
        // get arzt-anrede
        Vector reportHeaderAnrede = new Vector() ;
        reportHeaderAnrede = this.xmlDataAccess.getNodeListsByName( "header,adress-physician,anrede", reportNode ) ;
        NodeList anrede = (NodeList)reportHeaderAnrede.get( 0 ) ;
        String strReportHeaderArztAnrede = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , anrede )).get( 0 ) ;
        
        // get the name of the physician
        Vector reportHeaderArztName = new Vector() ;
        reportHeaderArztName = this.xmlDataAccess.getNodeListsByName( "header,adress-physician,name,contents", reportNode ) ;
        String strReportHeaderArztName = "";
        for(int i = 0 ; i < reportHeaderArztName.size() ; i++ ){
            NodeList arztname = (NodeList)reportHeaderArztName.get( i ) ;
            strReportHeaderArztName += (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , arztname )).get( 0 ) ;
        }
        strReportHeaderArztName = strReportHeaderArztName.trim();
        
        // get the street of the physician
        Vector reportHeaderArztStrasse = new Vector() ;
        reportHeaderArztStrasse = this.xmlDataAccess.getNodeListsByName( "header,adress-physician,street", reportNode ) ;
        NodeList arztstrasse = (NodeList)reportHeaderArztStrasse.get( 0 ) ;
        String strReportHeaderArztStrasse = (String)((Vector)this.xmlDataAccess.getElementsByName( "contents,text" , arztstrasse )).get( 0 ) ;
        
        // get the city and postcode of the physician
        Vector reportHeaderArztOrt = new Vector() ;
        reportHeaderArztOrt = this.xmlDataAccess.getNodeListsByName( "header,adress-physician,city,contents", reportNode ) ;
        String strReportHeaderArztOrt = "";
        for(int i = 0 ; i < reportHeaderArztOrt.size() ; i++ ){
            NodeList arztort = (NodeList)reportHeaderArztOrt.get( i ) ;
            strReportHeaderArztOrt += (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , arztort )).get( 0 ) ;
        }
        
        String header = "{\\rtf1\\ansi\\ansicpg1252\\uc1\\deff1\\stshfdbch0\\stshfloch0\\stshfhich0\\stshfbi0\\deflang1031\\deflangfe1031{\\fonttbl{\\f0\\froman\\fcharset0\\fprq2{\\*\\panose 02020603050405020304}Arial;}\n" ;
        header += "{\\f1\\fswiss\\fcharset0\\fprq2{\\*\\panose 020b0604020202020204}Arial{\\*\\falt Verdana};}{\\f36\\fswiss\\fcharset0\\fprq2{\\*\\panose 00000000000000000000}Helvetica-Light;}{\\f37\\froman\\fcharset238\\fprq2 Arial CE;}\n" ;
        header += "{\\f38\\froman\\fcharset204\\fprq2 Arial Cyr;}{\\f40\\froman\\fcharset161\\fprq2 Arial Greek;}{\\f41\\froman\\fcharset162\\fprq2 Arial Tur;}{\\f42\\froman\\fcharset177\\fprq2 Arial (Hebrew);}\n" ;
        header += "{\\f43\\froman\\fcharset178\\fprq2 Arial (Arabic);}{\\f44\\froman\\fcharset186\\fprq2 Arial Baltic;}{\\f45\\froman\\fcharset163\\fprq2 Arial (Vietnamese);}{\\f47\\fswiss\\fcharset238\\fprq2 Arial CE{\\*\\falt Verdana};}\n" ;
        header += "{\\f48\\fswiss\\fcharset204\\fprq2 Arial Cyr{\\*\\falt Verdana};}{\\f50\\fswiss\\fcharset161\\fprq2 Arial Greek{\\*\\falt Verdana};}{\\f51\\fswiss\\fcharset162\\fprq2 Arial Tur{\\*\\falt Verdana};}{\\f52\\fswiss\\fcharset177\\fprq2 Arial (Hebrew){\\*\\falt Verdana};}\n" ;
        header += "{\\f53\\fswiss\\fcharset178\\fprq2 Arial (Arabic){\\*\\falt Verdana};}{\\f54\\fswiss\\fcharset186\\fprq2 Arial Baltic{\\*\\falt Verdana};}{\\f55\\fswiss\\fcharset163\\fprq2 Arial (Vietnamese){\\*\\falt Verdana};}}{\\colortbl;\\red0\\green0\\blue0;\\red0\\green0\\blue255;\n" ;
        header += "\\red0\\green255\\blue255;\\red0\\green255\\blue0;\\red255\\green0\\blue255;\\red255\\green0\\blue0;\\red255\\green255\\blue0;\\red255\\green255\\blue255;\\red0\\green0\\blue128;\\red0\\green128\\blue128;\\red0\\green128\\blue0;\\red128\\green0\\blue128;\\red128\\green0\\blue0;\n" ;
        header += "\\red128\\green128\\blue0;\\red128\\green128\\blue128;\\red192\\green192\\blue192;}{\\stylesheet{\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\snext0 \\styrsid7158313 Normal;}{\\*\\cs10 \\additive \\ssemihidden Default Paragraph Font;}{\\*\n" ;
        header += "\\ts11\\tsrowd\\trftsWidthB3\\trpaddl108\\trpaddr108\\trpaddfl3\\trpaddft3\\trpaddfb3\\trpaddfr3\\trcbpat1\\trcfpat1\\tscellwidthfts0\\tsvertalt\\tsbrdrt\\tsbrdrl\\tsbrdrb\\tsbrdrr\\tsbrdrdgl\\tsbrdrdgr\\tsbrdrh\\tsbrdrv \n" ;
        header += "\\ql \\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\fs20\\lang1024\\langfe1024\\cgrid\\langnp1024\\langfenp1024 \\snext11 \\ssemihidden Normal Table;}{\\s15\\ql \\li0\\ri0\\sl240\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f36\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext15 \\ssemihidden \\styrsid7158313 annotation text;}{\\s16\\ql \\li0\\ri0\\sl140\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext16 \\ssemihidden \\styrsid7158313 Absenderadresse;}{\\s17\\ql \\li0\\ri0\\sl180\\slmult0\n" ;
        header += "\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext17 \\styrsid7158313 Klinik;}{\n" ;
        header += "\\s18\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \\b\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext18 \\styrsid7158313 Abteilung;}{\\s19\\ql \\li0\\ri0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs24\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext19 \\styrsid7158313 Bereich;}{\\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\n" ;
        header += "\\tx567\\pvpg\\phpg\\posx6520\\posy3514\\absh-2277\\absw2380\\abslock1\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\shading10000\\cfpat8 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext20 \\styrsid7158313 Einrichtungsleitung;}{\\s21\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \n" ;
        header += "\\sbasedon0 \\snext21 \\ssemihidden \\styrsid7158313 Closing;}{\\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx9156\\posy3514\\absh-2624\\absw2624\\abslock1\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext22 \\styrsid7158313 Absenderangaben;}{\\s23\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext23 \\styrsid947662 header;}{\\s24\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 \\sbasedon0 \\snext24 \\styrsid947662 footer;}{\\*\\cs25 \\additive \\sbasedon10 \\styrsid947662 page number;}}{\\*\\rsidtbl \\rsid947662\\rsid1050224\\rsid2361660\\rsid2495643\\rsid2641361\\rsid2646936\n" ;
        header += "\\rsid3309508\\rsid3344382\\rsid3622531\\rsid4010742\\rsid4205030\\rsid4526417\\rsid5572086\\rsid6254617\\rsid6831816\\rsid6848431\\rsid6900398\\rsid7158313\\rsid7289481\\rsid8664068\\rsid9779576\\rsid10380151\\rsid10497578\\rsid10580602\\rsid11237530\\rsid12654584\n" ;
        header += "\\rsid13434924\\rsid13968944\\rsid15673768\\rsid15729916\\rsid15803792}{\\*\\generator Microsoft Word 10.0.2627;}{\\info{\\title Medizinische Klinik}{\\author Stephan Rusch}{\\operator Stephan Rusch}{\\creatim\\yr2005\\mo6\\dy2\\hr11\\min32}\n" ;
        header += "{\\revtim\\yr2005\\mo6\\dy14\\hr16\\min11}{\\version28}{\\edmins0}{\\nofpages1}{\\nofwords78}{\\nofchars497}{\\*\\company CwebRD Universit\\'e4tsklinik Freiburg}{\\nofcharsws574}{\\vern16437}}\\paperw11906\\paperh16838\\margl1417\\margr1417\\margt1417\\margb1134 \n" ;
        header += "\\deftab708\\widowctrl\\ftnbj\\aenddoc\\hyphhotz425\\noxlattoyen\\expshrtn\\noultrlspc\\dntblnsbdb\\nospaceforul\\hyphcaps0\\formshade\\horzdoc\\dgmargin\\dghspace180\\dgvspace180\\dghorigin1417\\dgvorigin1417\\dghshow1\\dgvshow1\n" ;
        header += "\\jexpand\\viewkind1\\viewscale88\\viewzk2\\pgbrdrhead\\pgbrdrfoot\\splytwnine\\ftnlytwnine\\htmautsp\\nolnhtadjtbl\\useltbaln\\alntblind\\lytcalctblwd\\lyttblrtgr\\lnbrkrule\\nobrkwrptbl\\snaptogridincell\\allowfieldendsel\\wrppunct\\asianbrkrule\\rsidroot7158313 \n" ;
        header += "\\donotshowmarkup1\\fet0\\sectd \\linex0\\headery708\\footery708\\colsx708\\endnhere\\sectlinegrid360\\sectdefaultcl\\sftnbj " + strWaterMark + "{\\footer \\pard\\plain \\s24\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\fs16\\insrsid947662\\charrsid947662 " + strFooterText + "\\tab }{\\field{\\*\\fldinst {\\cs25\\fs16\\insrsid947662\\charrsid947662  PAGE }}{\\fldrslt {\n" ;
        header += "\\cs25\\fs16\\lang1024\\langfe1024\\noproof\\insrsid5572086 1}}}{\\fs16\\insrsid947662\\charrsid947662 \n" ;
        header += "\\par }}{\\*\\pnseclvl1\\pnucrm\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl2\\pnucltr\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl3\\pndec\\pnstart1\\pnindent720\\pnhang {\\pntxta .}}{\\*\\pnseclvl4\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxta )}}\n" ;
        header += "{\\*\\pnseclvl5\\pndec\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl6\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl7\\pnlcrm\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl8\n" ;
        header += "\\pnlcltr\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}{\\*\\pnseclvl9\\pnlcrm\\pnstart1\\pnindent720\\pnhang {\\pntxtb (}{\\pntxta )}}\\pard\\plain \\s17\\ql \\li0\\ri0\\sl180\\slmult0\n" ;
        header += "\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\n" ;
        header += "\\lang1024\\langfe1024\\noproof\\insrsid5572086 {\\shp{\\*\\shpinst\\shpleft1247\\shptop2884\\shpright5841\\shpbottom3217\\shpfhdr0\\shpbxpage\\shpbxignore\\shpbypage\\shpbyignore\\shpwr3\\shpwrk0\\shpfblwtxt0\\shpz0\\shplockanchor\\shplid1026\n" ;
        header += "{\\sp{\\sn shapeType}{\\sv 202}}{\\sp{\\sn fFlipH}{\\sv 0}}{\\sp{\\sn fFlipV}{\\sv 0}}{\\sp{\\sn lTxid}{\\sv 65536}}{\\sp{\\sn dxTextLeft}{\\sv 0}}{\\sp{\\sn dyTextTop}{\\sv 0}}{\\sp{\\sn dxTextRight}{\\sv 0}}{\\sp{\\sn dyTextBottom}{\\sv 0}}{\\sp{\\sn hspNext}{\\sv 1026}}\n" ;
        header += "{\\sp{\\sn fLine}{\\sv 0}}{\\sp{\\sn posrelh}{\\sv 1}}{\\sp{\\sn posrelv}{\\sv 1}}{\\sp{\\sn fLayoutInCell}{\\sv 1}}{\\sp{\\sn fAllowOverlap}{\\sv 1}}{\\sp{\\sn fLayoutInCell}{\\sv 1}}{\\shptxt \\pard\\plain \\s16\\ql \\li0\\ri0\\sl140\\slmult0\n" ;
        header += "\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 UNIVERSIT\\'c4TSKLINIKUM FREIBURG\n" ;
        header += "\\par }{\\b\\insrsid7158313 Rheumatologie und Klinische Immunologie, }{\\insrsid7158313 Hugstetterstr. 55, D-79106 Freiburg\n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313\\charrsid13434924 \n" ;
        header += "\\par }}}{\\shprslt{\\*\\do\\dobxpage\\dobypage\\dodhgt8192\\dptxbx\\dptxlrtb{\\dptxbxtext\\pard\\plain \\s16\\ql \\li0\\ri0\\sl140\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs12\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\n" ;
        header += "\\insrsid7158313 UNIVERSIT\\'c4TSKLINIKUM FREIBURG\n" ;
        header += "\\par }{\\b\\insrsid7158313 Rheumatologie und Klinische Immunologie, }{\\insrsid7158313 Hugstetterstr. 55, D-79106 Freiburg\n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313\\charrsid13434924 \n" ;
        header += "\\par }}\\dpx1247\\dpy2884\\dpxsize4594\\dpysize333\\dpfillfgcr255\\dpfillfgcg255\\dpfillfgcb255\\dpfillbgcr255\\dpfillbgcg255\\dpfillbgcb255\\dpfillpat1\\dplinehollow}}}}{\\insrsid7158313 Medizinische }{\\insrsid7158313\\charrsid6900398 Klinik\n" ;
        header += "\\par }\\pard\\plain \\s15\\ql \\li0\\ri0\\sl-180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f36\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\f1\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s18\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 Rheumatologie und Klinische Immunologie}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl-160\\slmult0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-975\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\b\\fs16\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s19\\ql \\li0\\ri0\\widctlpar\\pvpg\\phpg\\posx6521\\posy2268\\absh-1135\\absw5222\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid6848431 \\cfpat8 \n" ;
        header += "\\b\\f1\\fs24\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\b0\\fs20\\insrsid7289481\\charrsid6848431 Medizinische Klinik\n" ;
        header += "\\par }{\\fs16\\insrsid7289481 \n" ;
        header += "\\par }{\\b0\\fs20\\insrsid6848431\\charrsid6848431 Abteilung }{\\b0\\fs20\\insrsid7289481\\charrsid6848431 Rheumatologie und Klinische Immunologie\n" ;
        header += "\\par }{\\fs16\\insrsid7289481\\charrsid7289481 \n" ;
        header += "\\par }{\\insrsid7158313 Rheuma-Ambulanz}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-2575\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid9779576 \\shading10000\\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid7158313 \\'c4rztlicher}{\\insrsid7158313\\charrsid6900398  Direktor:\n" ;
        header += "\\par Prof. Dr. }{\\insrsid7158313 med. H. H. Peter}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid7158313 Hugstetterstr. 55}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par D-}{\\insrsid7158313 79106}{\\insrsid7158313\\charrsid6900398  Freiburg\n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 Tel\\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 \\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 0761/270-344}{\n" ;
        header += "\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 8}{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 \n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 Sekrerariat\\tab 0761/270-3449\n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 Fax\\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 \\tab }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 0761/270-3446\n" ;
        header += "\\par }\\pard\\plain \\s22\\ql \\fi-567\\li567\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-2575\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin567\\itap0\\pararsid9779576 \\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 E-Mail    peterhh@medizin.ukl.\n" ;
        header += "\\par \\tab  uni-freiburg.de\n" ;
        header += "\\par }\\pard\\plain \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\tx1080\\pvpg\\phpg\\posx6521\\posy3521\\absh-2575\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid9779576 \\shading10000\\cfpat8 \n" ;
        header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1040\\langfe1031\\langnp1040\\insrsid6831816\\charrsid6848431 \n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 Labor\\tab \\tab 0761/270-3528\n" ;
        header += "\\par Station\\tab \\tab 0761/270-37}{\\lang1040\\langfe1031\\langnp1040\\insrsid6831816\\charrsid6848431 35}{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 \n" ;
        header += "\\par Station (FAX)\\tab 0761/270-37}{\\lang1040\\langfe1031\\langnp1040\\insrsid6831816\\charrsid6848431 29}{\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 \n" ;
        header += "\\par }\\pard \\s20\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx6521\\posy3521\\absh-2575\\absw2461\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid9779576 \\shading10000\\cfpat8 {\n" ;
        header += "\\lang1040\\langfe1031\\langnp1040\\insrsid9779576\\charrsid6848431 \n" ;
        header += "\\par }\\pard\\plain \\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx9134\\posy3521\\absh-2625\\absw2625\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\cfpat8 \n" ;
        if( strLogin.equals("peter") || strLogin.equals("vaith") || strLogin.equals("seidel") ){
            header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 \n" ;
            header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 \n" ;
            header += "\\par \n" ;
            header += "\\par \n" ;
            header += "\\par \n" ;
        }else{
            header += "\\f1\\fs16\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 Zuständiger Arzt:\n" ;
            header += "\\par \\b " + strReportHeaderDokumentarName + "}{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 \n" ;
            // Arzt-Tel (Frau Ruf)
            header += "\\par Tel\\tab 0761/270-3544\n" ;
            // Arzt-FAX
            header += "\\par E-Mail\\tab " + strReportHeaderDokumentarEmail + "\n" ;
        }
        header += "\\par }\\pard \\s22\\ql \\fi-567\\li567\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx9134\\posy3521\\absh-2625\\absw2625\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin567\\itap0\\pararsid7158313 \\cfpat8 {\n" ;
        header += "\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 \n" ;
        header += "\\par }\\pard \\s22\\ql \\li0\\ri0\\sl180\\slmult0\\widctlpar\\tx567\\pvpg\\phpg\\posx9134\\posy3521\\absh-2625\\absw2625\\abslock1\\dxfrtext181\\dfrmtxtx181\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid7158313 \\cfpat8 {\n" ;
        header += "\\lang1040\\langfe1031\\langnp1040\\insrsid7158313\\charrsid6848431 \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid7158313 \n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid6848431 \n" ;
        header += "\\par }{\\lang1040\\langfe1031\\langnp1040\\insrsid6848431\\charrsid6848431 \n" ;
        header += "\\par }{\\insrsid6848431 \n" ;
        header += "\\par }{\\insrsid6848431 \n" ;
        header += "\\par }{\\insrsid7158313\\charrsid6900398 Freiburg, }{\\insrsid7158313 " + strActualDate + "}{\\insrsid7158313\\charrsid6900398 \n" ;
        header += "\\par }\\pard\\plain \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\pvpg\\phpg\\posx1247\\posy3515\\absh-1440\\absw4309\\abslock1\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid10380151 \\shading10000\\cfpat8\\cbpat8 \n" ;
        header += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\insrsid5572086 " + strReportHeaderArztAnrede + "}{\\insrsid10580602 \n" ;
        header += "\\par }{\\insrsid7158313 " + strReportHeaderArztName + "\n" ;
        header += "\\par " + strReportHeaderArztStrasse + "\n" ;
        header += "\\par \n" ;
        header += "\\par }{\\b\\insrsid7158313\\charrsid4205030 " + strReportHeaderArztOrt + "}{\\insrsid7158313 \n" ;
        header += "\\par }\\pard \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 {\\insrsid1050224 \n" ;
        header += "\\par }{\\insrsid8664068 \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid15803792 \n" ;
        header += "\\par }{\\insrsid6254617 \n" ;
        header += "\\par \n" ;
        header += "\\par }\\pard \\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\\pararsid4010742 {\\insrsid4526417 \n" ;
        header += "\\par }{\\insrsid4010742 \n" ;
        header += "\\par \n" ;
        header += "\\par }{\\insrsid4010742\\charrsid4010742 \n" ;
        header += "\\par }\n" ;
        
        this.generatedDocument += header ;
    }
    
    private void writeRTFFooter( NodeList reportNode ) throws org.xml.sax.SAXException {
        this.generatedDocument += "{\\*\\pgdsctbl\n";
        this.generatedDocument += "{\\pgdsc0\\pgdscuse195\\pgwsxn11906\\pghsxn16838\\marglsxn1417\\margrsxn1417\\margtsxn1417\\margbsxn1134\\footery0{\\*\\footeryt0\\footerxl0\\footerxr0\\footeryh0}{\\footer \\pard\\plain \\s2\\cf1{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\rtlch\\af1\\afs24\\lang255\\ltrch\\dbch\\af4\\afs24\\langfe255\\loch\\f1\\fs24\\lang255\\cf1\\tqc\\tx4152\\ltrch\\dbch\\af4\\loch\\fs20\\lang255 {\\ltrch\\loch\\f1 ";
        Vector footerSections = new Vector() ;
        footerSections = this.xmlDataAccess.getNodeListsByName( "footer" , reportNode ) ;
        for ( int rs = 0 ; rs < footerSections.size() ; rs++ ) {
            NodeList footer = (NodeList)footerSections.get( rs ) ;
            String footerSectionType = (String)((Vector)this.xmlDataAccess.getElementsByName( "footer-type" , footer )).get( 0 ) ;
            if ( footerSectionType.equals( "text" ) ) {
                this.createTextSection( footer , true , false , "" , false ) ;
            }
        }
        this.generatedDocument += "\\tab \\tab \\tab \\tab {\\field{\\*\\fldinst \\\\page}{\\fldrslt 1}}}\n";
        this.generatedDocument += "\\par }\n";
        this.generatedDocument += "\\pgdscnxt0 Default;}}\n";
        this.generatedDocument += "\\paperh16838\\paperw11906\\margl1417\\margr1417\\margt1417\\margb1134\\sectd\\sbknone\\pgwsxn11906\\pghsxn16838\\marglsxn1417\\margrsxn1417\\margtsxn1417\\margbsxn1134\\footery1440{\\footer \\pard\\plain \\s2\\cf1{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\rtlch\\af1\\afs24\\lang255\\ltrch\\dbch\\af4\\afs24\\langfe255\\loch\\f1\\fs24\\lang255\\cf1\\tqc\\tx4152\\ltrch\\dbch\\af4\\loch\\fs20\\lang255 {\\ltrch\\loch\\f1  \n";
        for ( int rs = 0 ; rs < footerSections.size() ; rs++ ) {
            NodeList footer = (NodeList)footerSections.get( rs ) ;
            String footerSectionType = (String)((Vector)this.xmlDataAccess.getElementsByName( "footer-type" , footer )).get( 0 ) ;
            if ( footerSectionType.equals( "text" ) ) {
                this.createTextSection( footer , true , false , "" , false ) ;
            }
        }
        this.generatedDocument += "\\tab {\\field{\\*\\fldinst \\\\page}{\\fldrslt 1}}}\n";
        this.generatedDocument += "\\par }\n";
    }
    
    private void writeBridge() throws org.xml.sax.SAXException {
        this.generatedDocument += "\\ftnbj\\ftnstart1\\ftnrstcont\\ftnnar\\aenddoc\\aftnrstcont\\aftnstart1\\aftnnrlc\n" ;
    }
    
    private void generateSections( NodeList reportNode ) throws org.xml.sax.SAXException {
        Vector reportSections = new Vector() ;
        reportSections = this.xmlDataAccess.getNodeListsByName( "section" , reportNode ) ;
        for ( int rs = 0 ; rs < reportSections.size() ; rs++ ) {
            NodeList section = (NodeList)reportSections.get( rs ) ;
            String reportSectionType = (String)((Vector)this.xmlDataAccess.getElementsByName( "section-type" , section )).get( 0 ) ;
            String reportSectionFormat = "" ;
            try{
                reportSectionFormat = (String)((Vector)this.xmlDataAccess.getElementsByName( "section-format" , section )).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                
            }
            if ( reportSectionType.equals( "text" ) ) {
                this.createTextSection( section , false , false , reportSectionFormat , false ) ;
            } else if ( reportSectionType.equals( "table" ) ) {
                this.createTable( section , true , reportSectionFormat ) ;
            } else if ( reportSectionType.equals( "newpage" ) ) {
                this.createNewPage() ;
            }
        }
    }
    
    private void createNewPage() {
        String textSection = "\\par \\page\\pard\\plain \\s1\\cf0{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\rtlch\\af1\\afs24\\lang255\\ltrch\\dbch\\af1\\afs24\\langfe255\\loch\\f1\\fs24\\lang255\\ltrch\\loch\\fs20\\lang1033 \n" ;
        this.generatedDocument += textSection ;
    }
    
    private void createTextSection( NodeList section , boolean footer , boolean table , String reportSectionFormat , boolean innerCellFontSize ) throws org.xml.sax.SAXException {
        String textSection = "" ;
        String block = "";
        // fuer Blocksatz
        if(reportSectionFormat.equals("justified"))
            block = "\\qj ";
        
        if ( footer == false ) {
            if ( this.initText == true ) {
                textSection = "\\pard\\plain \\s4\\rtlch\\lang255\\ltrch\\loch\\lang1033 {\\ltrch\\loch\\f1 " ;
                this.initText = false ;
            } else if ( table == false ) {
                textSection = "\\par " + block + "{\\ltrch\\loch\\f0 " ;
            } else {
                textSection =  block + "{\\ltrch\\loch\\f0 " ;
            }
        }
        Vector textContents = new Vector() ;
        textContents = this.xmlDataAccess.getNodeListsByName( "contents" , section ) ;
        
        Vector columnFunction = new Vector() ;
        columnFunction = (Vector)this.xmlDataAccess.getElementsByName( "column-function" , section ) ;
        String strColumnText = "";
        boolean boolLastElement = false;
        for ( int ci = 0 ; ci < textContents.size() ; ci++ ) {
            String text = "" ;
            String textattributes = "" ;
            
            String strTempContent = (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , (NodeList)textContents.get( ci ) ) ).get( 0 ) ;
            
            String strContent = strTempContent ;
            int textOpenerForAttributes = 0 ;
            String strFontSize = "\\fs20 " ;
            
            if ( !innerCellFontSize ) {
                Vector fontSize = new Vector() ;
                fontSize = (Vector)this.xmlDataAccess.getElementsByName( "font-size" , (NodeList)textContents.get( ci ) ) ;
                
                if ( fontSize.size() > 0 ) {
                    strFontSize = "\\fs" + ((String)fontSize.get( 0 )) + " " ;
                }
                
            }else{
                strFontSize = "" ;
            }
            
            textSection += strFontSize ;
            
            Vector textAttributes = new Vector() ;
            textAttributes = (Vector)this.xmlDataAccess.getElementsByName( "contents-attribute" , (NodeList)textContents.get( ci ) ) ;
            for ( int ai = 0 ; ai < textAttributes.size() ; ai++ ) {
                if ( ((String)textAttributes.get( ai )).equals( "bold" ) ) {
                    textattributes += "\\b" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)textAttributes.get( ai )).equals( "italic" ) ) {
                    textattributes += "\\i" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)textAttributes.get( ai )).equals( "underline" ) ) {
                    textattributes += "\\ul" ;
                    textOpenerForAttributes++ ;
                }
            }
            
            Vector labelAttributes = new Vector() ;
            labelAttributes = (Vector)this.xmlDataAccess.getElementsByName( "label-attribute" , (NodeList)textContents.get( ci ) ) ;
            for ( int ai = 0 ; ai < labelAttributes.size() ; ai++ ) {
                if ( ((String)labelAttributes.get( ai )).equals( "bold" ) ) {
                    textattributes += "\\b" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)labelAttributes.get( ai )).equals( "italic" ) ) {
                    textattributes += "\\i" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)labelAttributes.get( ai )).equals( "underline" ) ) {
                    textattributes += "\\ul" ;
                    textOpenerForAttributes++ ;
                }
            }
            
            Vector textFunctions = new Vector() ;
            textFunctions = (Vector)this.xmlDataAccess.getElementsByName( "function" , (NodeList)textContents.get( ci ) ) ;
            ReportContentFunctions rcf = new ReportContentFunctions();
            for ( int ai = 0 ; ai < textFunctions.size() ; ai++ ) {
                if ( ((String)textFunctions.get( ai )).equals( "convertDate" ) ) {
                    strContent = rcf.convertDate( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "convertValue" ) ) {
                    strContent = rcf.convertValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "makeNewLineAfterEachItem" ) ) {
                    strContent = rcf.makeNewLineAfterEachItem( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "convertNewLine" ) ) {
                    strContent = rcf.convertNewLine( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "convertCheckValue" ) ) {
                    strContent = rcf.convertCheckValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getActualDate" ) ) {
                    strContent = rcf.getActualDate() ;
                }else if ( ((String)textFunctions.get( ai )).equals( "cutSecondsFromTimestring" ) ) {
                    strContent = rcf.cutSecondsFromTimestring( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getInfectionStatusValue" ) ) {
                    strContent = rcf.getInfectionStatusValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAutoImmunDiseaseValue" ) ) {
                    strContent = rcf.getAutoImmunDiseaseValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAutoImmunDiseaseTitreValue" ) ) {
                    strContent = rcf.getAutoImmunDiseaseTitreValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAutoImmunDiseaseNormalTitreValue" ) ) {
                    strContent = rcf.getAutoImmunDiseaseNormalTitreValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getNeoplasmsStatusValue" ) ) {
                    strContent = rcf.getNeoplasmsStatusValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getPatientHistoryKindOfTreatmentValue" ) ) {
                    strContent = rcf.getPatientHistoryKindOfTreatmentValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getPatientHistoryStatusValue" ) ) {
                    strContent = rcf.getPatientHistoryStatusValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getQoLDaysMissedValue" ) ) {
                    strContent = rcf.getQoLDaysMissedValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getYesNoUnknownValue" ) ) {
                    strContent = rcf.getYesNoUnknownValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getKarnowskyValue" ) ) {
                    strContent = rcf.getKarnowskyValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getUltrasoundLnodesValue" ) ) {
                    strContent = rcf.getUltrasoundLnodesValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getPreventiveIntervalValue" ) ) {
                    strContent = rcf.getPreventiveIntervalValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatDosierung" ) ) {
                    strContent = rcf.formatDosierung( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatDate" ) ) {
                    strContent = rcf.formatDate( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatDosierungRheuma" ) ) {
                    strContent = rcf.formatDosierungRheuma( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatNZBasistherapieRheuma" ) ) {
                    strContent = rcf.formatNZBasistherapieRheuma( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getEmptyStringIfValueEquals_x" ) ) {
                    strContent = rcf.getEmptyStringIfValueEquals_x( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getValueWhithoutFront_x" ) ) {
                    strContent = rcf.getValueWhithoutFront_x( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getEmptyStringIfValueEquals_pointpoint" ) ) {
                    strContent = rcf.getEmptyStringIfValueEquals_pointpoint( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getUntersuchungsbefundBezeichnung" ) ) {
                    strContent = rcf.getUntersuchungsbefundBezeichnung( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatAuswaertigeBefunde" ) ) {
                    strContent = rcf.formatAuswaertigeBefunde( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatMonatTag" ) ) {
                    strContent = rcf.formatMonatTag( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getReportFileName" ) ) {
                    strContent = rcf.getReportFileName( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAddNTXValue" ) ) {
                    strContent = rcf.getAddNTXValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatTransplantMirror" ) ) {
                    strContent = rcf.formatTransplantMirror( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getDefiniteDiagnosis" ) ) {
                    strContent = rcf.getDefiniteDiagnosis( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatTransplantDiagnosenText" ) ) {
                    strContent = rcf.formatTransplantDiagnosenText( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "parseBoneDensity" ) ) {
                    strContent = rcf.parseBoneDensity( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatESIDTherapieEmpfehlung" ) ) {
                    strContent = rcf.formatESIDTherapieEmpfehlung( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getDosisIntervall" ) ) {
                    strContent = rcf.getDosisIntervall( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getFloatFormat" ) ) {
                    strContent = rcf.getFloatFormat( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getJaNeinValue" ) ) {
                    strContent = rcf.getJaNeinValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getFloatFormatForDosis" ) ) {
                    strContent = rcf.getFloatFormatForDosis( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getDosisIntervallForRheuma" ) ) {
                    strContent = rcf.getDosisIntervallForRheuma( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "parseHTML" ) ) {
                    strContent = rcf.parseHTML( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "toUppercase" ) ) {
                    strContent = rcf.toUppercase( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "toLowercase" ) ) {
                    strContent = rcf.toLowercase( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatHospitalNumber" ) ) {
                    strContent = rcf.formatHospitalNumber( strTempContent ) ;
                }
            }
            
            // Replace functions defined through XML. David
            Vector repFunct = new Vector() ;
            repFunct = (Vector) this.xmlDataAccess.getElementsByName("replace-function", 
                    (NodeList)textContents.get( ci ));
            if (!repFunct.isEmpty()) {
                String rep = (String) repFunct.get(0);
                Vector repDef = (Vector) this.xmlDataAccess.getNodeListsByName("replace-def");
                ReportContentReplace rcr = new ReportContentReplace(rep, repDef);
                if (strTempContent.length() < 1) {
                    strTempContent = "";
                }
                strContent = rcr.processReplace(strTempContent);
            }      
            
            Vector convFunct = new Vector();
            convFunct = (Vector) this.xmlDataAccess.getNodeListsByName("convert-function", 
                    (NodeList)textContents.get( ci ));
            if (!convFunct.isEmpty()) {
                NodeList funcNode = (NodeList) convFunct.get(0);
                String type = (String) 
                        ((Vector)this.xmlDataAccess.getElementsByName("type", 
                        funcNode)).get(0);
                String format = (String) 
                        ((Vector)this.xmlDataAccess.getElementsByName("format", 
                        funcNode)).get(0);
                for (ReportDateFormat r : ReportDateFormat.values()) {
                    if (r.toString().equals(this.locale) && type.equals("date")) {
                        try {
                            if (format.equals("default")) {
                                strContent = r.formatDate(strTempContent);
                            } else if (format.equals("short")) {
                                strContent = r.formatShortDate(strTempContent);
                            } else if (format.equals("medium")) {
                                strContent = r.formatMediumDate(strTempContent);
                            } else if (format.equals("long")) {
                                strContent = r.formatLongDate(strTempContent);
                            } else {
                                strContent = r.formatDate(strTempContent);
                            }
                        } catch (ParseException ex) {
                            Logger log = Logger.getLogger(RTFGenerator.class.getName());
                            log.log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                }
            }
            
            // Schmutz herausfiltern
            strContent = this.replaceAll( strContent , "?" , "" ) ;
            
            if ( textOpenerForAttributes > 0 ) {
                text = "{" + textattributes + " " + strContent + "}" ;
            } else {
                text = strContent ;
            }
            
            if( columnFunction.size() == 0 ){
                textSection +=  text ;
                
                if ( ci == textContents.size() -1 && footer == false ) {
                    textSection += "}\n" ;
                }
            }else if( columnFunction.size() > 0 ){
                
                strColumnText += strContent ;
                
                if ( ci == textContents.size() -1 && footer == false ) {
                    boolLastElement = true;
                }
            }
        }
        
        if( columnFunction.size() > 0 ){
            
            ReportContentFunctions rcf = new ReportContentFunctions();
            for ( int ai = 0 ; ai < columnFunction.size() ; ai++ ) {
                if ( ((String)columnFunction.get( ai )).equals( "filterRange" ) ) {
                    strColumnText = rcf.filterRange( strColumnText ) ;
                }else if ( ((String)columnFunction.get( ai )).equals( "filterEsidRange" ) ) {
                    strColumnText = rcf.filterEsidRange( strColumnText ) ;
                }
            }
            
            textSection += strColumnText ;
            
            if ( boolLastElement ) {
                textSection += "}\n" ;
            }
        }
        
        this.generatedDocument += textSection ;
    }
    
    private String getTextSection( NodeList section , boolean footer , boolean table , String reportSectionFormat , boolean innerCellFontSize ) throws org.xml.sax.SAXException {
        String textSection = "" ;
        String block = " ";
        // fuer Blocksatz
        if(reportSectionFormat.equals("justified"))
            block = "\\qj ";
        
        if ( footer == false ) {
            if ( this.initText == true ) {
                textSection = "\\pard\\plain \\s4\\rtlch\\lang255\\ltrch\\loch\\lang1033 {\\ltrch\\loch\\f1 " ;
                this.initText = false ;
            } else if ( table == false ) {
                textSection = "\\par " + block + "{\\ltrch\\loch\\f0 " ;
            } else {
                textSection =  block + "{\\ltrch\\loch\\f0 " ;
            }
        }
        Vector textContents = new Vector() ;
        textContents = this.xmlDataAccess.getNodeListsByName( "contents" , section ) ;
        
        Vector columnFunction = new Vector() ;
        columnFunction = (Vector)this.xmlDataAccess.getElementsByName( "column-function" , section ) ;
        String strColumnText = "";
        boolean boolLastElement = false;
        for ( int ci = 0 ; ci < textContents.size() ; ci++ ) {
            String text = "" ;
            String textattributes = "" ;
            
            String strTempContent = (String)((Vector)this.xmlDataAccess.getElementsByName( "text" , (NodeList)textContents.get( ci ) ) ).get( 0 ) ;
            
            String strContent = strTempContent ;
            int textOpenerForAttributes = 0 ;
            String strFontSize = "\\fs20 " ;
            
            if ( !innerCellFontSize ) {
                Vector fontSize = new Vector() ;
                fontSize = (Vector)this.xmlDataAccess.getElementsByName( "font-size" , (NodeList)textContents.get( ci ) ) ;
                
                if ( fontSize.size() > 0 ) {
                    strFontSize = "\\fs" + ((String)fontSize.get( 0 )) + " " ;
                }
                
            }else{
                strFontSize = "" ;
            }
            
            textSection += strFontSize ;
            
            Vector textAttributes = new Vector() ;
            textAttributes = (Vector)this.xmlDataAccess.getElementsByName( "contents-attribute" , (NodeList)textContents.get( ci ) ) ;
            for ( int ai = 0 ; ai < textAttributes.size() ; ai++ ) {
                if ( ((String)textAttributes.get( ai )).equals( "bold" ) ) {
                    textattributes += "\\b" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)textAttributes.get( ai )).equals( "italic" ) ) {
                    textattributes += "\\i" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)textAttributes.get( ai )).equals( "underline" ) ) {
                    textattributes += "\\ul" ;
                    textOpenerForAttributes++ ;
                }
            }
            
            Vector labelAttributes = new Vector() ;
            labelAttributes = (Vector)this.xmlDataAccess.getElementsByName( "label-attribute" , (NodeList)textContents.get( ci ) ) ;
            for ( int ai = 0 ; ai < labelAttributes.size() ; ai++ ) {
                if ( ((String)labelAttributes.get( ai )).equals( "bold" ) ) {
                    textattributes += "\\b" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)labelAttributes.get( ai )).equals( "italic" ) ) {
                    textattributes += "\\i" ;
                    textOpenerForAttributes++ ;
                } else if ( ((String)labelAttributes.get( ai )).equals( "underline" ) ) {
                    textattributes += "\\ul" ;
                    textOpenerForAttributes++ ;
                }
            }
            
            Vector textFunctions = new Vector() ;
            textFunctions = (Vector)this.xmlDataAccess.getElementsByName( "function" , (NodeList)textContents.get( ci ) ) ;
            ReportContentFunctions rcf = new ReportContentFunctions();
            for ( int ai = 0 ; ai < textFunctions.size() ; ai++ ) {
                if ( ((String)textFunctions.get( ai )).equals( "convertDate" ) ) {
                    strContent = rcf.convertDate( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "convertValue" ) ) {
                    strContent = rcf.convertValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "makeNewLineAfterEachItem" ) ) {
                    strContent = rcf.makeNewLineAfterEachItem( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "convertNewLine" ) ) {
                    strContent = rcf.convertNewLine( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "convertCheckValue" ) ) {
                    strContent = rcf.convertCheckValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getActualDate" ) ) {
                    strContent = rcf.getActualDate() ;
                }else if ( ((String)textFunctions.get( ai )).equals( "cutSecondsFromTimestring" ) ) {
                    strContent = rcf.cutSecondsFromTimestring( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getInfectionStatusValue" ) ) {
                    strContent = rcf.getInfectionStatusValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAutoImmunDiseaseValue" ) ) {
                    strContent = rcf.getAutoImmunDiseaseValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAutoImmunDiseaseTitreValue" ) ) {
                    strContent = rcf.getAutoImmunDiseaseTitreValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAutoImmunDiseaseNormalTitreValue" ) ) {
                    strContent = rcf.getAutoImmunDiseaseNormalTitreValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getNeoplasmsStatusValue" ) ) {
                    strContent = rcf.getNeoplasmsStatusValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getPatientHistoryKindOfTreatmentValue" ) ) {
                    strContent = rcf.getPatientHistoryKindOfTreatmentValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getPatientHistoryStatusValue" ) ) {
                    strContent = rcf.getPatientHistoryStatusValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getQoLDaysMissedValue" ) ) {
                    strContent = rcf.getQoLDaysMissedValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getYesNoUnknownValue" ) ) {
                    strContent = rcf.getYesNoUnknownValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getKarnowskyValue" ) ) {
                    strContent = rcf.getKarnowskyValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getUltrasoundLnodesValue" ) ) {
                    strContent = rcf.getUltrasoundLnodesValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getPreventiveIntervalValue" ) ) {
                    strContent = rcf.getPreventiveIntervalValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatDosierung" ) ) {
                    strContent = rcf.formatDosierung( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatDate" ) ) {
                    strContent = rcf.formatDate( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatDosierungRheuma" ) ) {
                    strContent = rcf.formatDosierungRheuma( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatNZBasistherapieRheuma" ) ) {
                    strContent = rcf.formatNZBasistherapieRheuma( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getEmptyStringIfValueEquals_x" ) ) {
                    strContent = rcf.getEmptyStringIfValueEquals_x( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getValueWhithoutFront_x" ) ) {
                    strContent = rcf.getValueWhithoutFront_x( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getEmptyStringIfValueEquals_pointpoint" ) ) {
                    strContent = rcf.getEmptyStringIfValueEquals_pointpoint( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getUntersuchungsbefundBezeichnung" ) ) {
                    strContent = rcf.getUntersuchungsbefundBezeichnung( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatAuswaertigeBefunde" ) ) {
                    strContent = rcf.formatAuswaertigeBefunde( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatMonatTag" ) ) {
                    strContent = rcf.formatMonatTag( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getReportFileName" ) ) {
                    strContent = rcf.getReportFileName( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getAddNTXValue" ) ) {
                    strContent = rcf.getAddNTXValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatTransplantDosierung" ) ) {
                    strContent = rcf.formatTransplantMirror( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getDefiniteDiagnosis" ) ) {
                    strContent = rcf.getDefiniteDiagnosis( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatTransplantDiagnosenText" ) ) {
                    strContent = rcf.formatTransplantDiagnosenText( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "parseBoneDensity" ) ) {
                    strContent = rcf.parseBoneDensity( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "formatESIDTherapieEmpfehlung" ) ) {
                    strContent = rcf.formatESIDTherapieEmpfehlung( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getDosisIntervall" ) ) {
                    strContent = rcf.getDosisIntervall( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getFloatFormat" ) ) {
                    strContent = rcf.getFloatFormat( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getJaNeinValue" ) ) {
                    strContent = rcf.getJaNeinValue( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getFloatFormatForDosis" ) ) {
                    strContent = rcf.getFloatFormatForDosis( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "getDosisIntervallForRheuma" ) ) {
                    strContent = rcf.getDosisIntervallForRheuma( strTempContent ) ;
                }else if ( ((String)textFunctions.get( ai )).equals( "parseHTML" ) ) {
                    strContent = rcf.parseHTML( strTempContent ) ;
                }
            }
            
            // Schmutz herausfiltern
            strContent = this.replaceAll( strContent , "?" , "" ) ;
            
            if ( textOpenerForAttributes > 0 ) {
                text = "{" + textattributes + " " + strContent + "}" ;
            } else {
                text = strContent ;
            }
            
            if( columnFunction.size() == 0 ){
                textSection +=  text ;
                
                if ( ci == textContents.size() -1 && footer == false ) {
                    textSection += "}\n" ;
                }
            }else if( columnFunction.size() > 0 ){
                
                strColumnText += strContent ;
                
                if ( ci == textContents.size() -1 && footer == false ) {
                    boolLastElement = true;
                }
            }
        }
        
        if( columnFunction.size() > 0 ){
            
            ReportContentFunctions rcf = new ReportContentFunctions();
            for ( int ai = 0 ; ai < columnFunction.size() ; ai++ ) {
                if ( ((String)columnFunction.get( ai )).equals( "filterRange" ) ) {
                    strColumnText = rcf.filterRange( strColumnText ) ;
                }else if ( ((String)columnFunction.get( ai )).equals( "filterEsidRange" ) ) {
                    strColumnText = rcf.filterEsidRange( strColumnText ) ;
                }
            }
            
            textSection += strColumnText ;
            
            if ( boolLastElement ) {
                textSection += "}\n" ;
            }
        }
        
        return textSection ;
    }
    
    private void createTable( NodeList section , boolean init , String reportSectionFormat ) throws org.xml.sax.SAXException {
        int rowCache = 0 ;
        String strBorder = "\\brdrw0" ;
        Vector rowWidth = new Vector() ;
        rowWidth = (Vector)this.xmlDataAccess.getElementsByName( "column-def,width" , section ) ;
        Vector tableBorder = new Vector() ;
        tableBorder = (Vector)this.xmlDataAccess.getElementsByName( "table-border" , section ) ;
        Vector tableRowBehaviour = new Vector() ;
        tableRowBehaviour = (Vector)this.xmlDataAccess.getElementsByName( "table-row-behaviour" , section ) ;
        if ( init == true ) {
            String strParagraphFormat = getParagraphFormat( reportSectionFormat , true ) ;
            String strRowBehaviour = "";
            if( tableRowBehaviour.size() > 0 ){
                if ( ((String)tableRowBehaviour.get( 0 )).trim().equals( "static" ) ) {
                    strRowBehaviour = "\\trrh-220" ;
                }
            }
            this.generatedDocument += strParagraphFormat + "\\par \\trowd\\trql\\trleft-19" + strRowBehaviour;
        }
        if ( ((String)tableBorder.get( 0 )).equals( "no" ) ) {
            strBorder = "\\brdrw0" ;
        } else if ( ((String)tableBorder.get( 0 )).equals( "yes" ) ) {
            strBorder = "\\brdrw5" ;
        } else if ( ((String)tableBorder.get( 0 )).equals( "bold" ) ) {
            strBorder = "\\brdrw30" ;
        }
        for ( int ri = 0 ; ri < rowWidth.size() ; ri++ ) {
            String cellWidthString = (String)rowWidth.get( ri ) ;
            String strTableBorderClose = "" ;
            float cellWidthFloat = Float.parseFloat( cellWidthString ) / 100  ;
            int cellWidth = ( (int)(9072 * cellWidthFloat) ) + rowCache ;
            rowCache = cellWidth ;
            if ( ri == rowWidth.size() - 1 )
                strTableBorderClose = "\\clbrdrr\\brdrs" + strBorder + "\\brdrcf1\\brsp0" ;
            if ( strBorder.equals( "\\brdrw0" ) )
                this.generatedDocument += "\\cellx" + cellWidth ;
            else
                this.generatedDocument += "\\clbrdrt\\brdrs" + strBorder + "\\brdrcf1\\brsp0\\clbrdrl\\brdrs" + strBorder + "\\brdrcf1\\brsp0\\clbrdrb\\brdrs" + strBorder + "\\brdrcf1\\brsp0" + strTableBorderClose + "\\cellx" + cellWidth ;
        }
        this.generatedDocument += "\n";
        this.writeColumns( section ) ;
        this.closeTable( reportSectionFormat ) ;
    }
    
    
    
    private void writeColumns( NodeList section ) throws org.xml.sax.SAXException {
        Vector row = new Vector() ;
        this.initText = false ;
        row = (Vector)this.xmlDataAccess.getNodeListsByName( "row" , section ) ;
        
        for ( int ri = 0 ; ri < row.size() ; ri++ ) {
            
            Vector column = (Vector)this.xmlDataAccess.getNodeListsByName( "column" , (NodeList)row.get( ri ) ) ;
            
            for ( int ci = 0 ; ci < column.size() ; ci++ ) {
                
                Vector columnAlign = new Vector() ;
                columnAlign = (Vector)this.xmlDataAccess.getElementsByName( "column-align" , (NodeList)column.get( ci ) ) ;
                String strAlign = " " ;
                if ( columnAlign.size() > 0 ) {
                    if ( ((String)columnAlign.get( 0 )).equals( "right" ) ) {
                        strAlign = "\\qr " ;
                    } else if ( ((String)columnAlign.get( 0 )).equals( "center" ) ) {
                        strAlign = "\\qc " ;
                    } else if ( ((String)columnAlign.get( 0 )).equals( "left" ) ) {
                        strAlign = " " ;
                    }
                }
                
                //                Vector columnValign = new Vector() ;
                //                columnValign = (Vector)this.xmlDataAccess.getElementsByName( "column-valign" , (NodeList)column.get( ci ) ) ;
                //                String strValign = "" ;
                //                if ( columnValign.size() > 0 ) {
                //                    if ( ((String)columnValign.get( 0 )).equals( "bottom" ) ) {
                //                        strValign = "\\clvertalb" ;
                //                    }
                //                }
                
                Vector labelAlign = new Vector() ;
                labelAlign = (Vector)this.xmlDataAccess.getElementsByName( "label-align" , (NodeList)column.get( ci ) ) ;
                if ( labelAlign.size() > 0 ) {
                    if ( ((String)labelAlign.get( 0 )).equals( "right" ) ) {
                        strAlign = "\\qr " ;
                    } else if ( ((String)labelAlign.get( 0 )).equals( "center" ) ) {
                        strAlign = "\\qc " ;
                    } else if ( ((String)labelAlign.get( 0 )).equals( "left" ) ) {
                        strAlign = " " ;
                    }
                }
                
                Vector fontSize = new Vector() ;
                fontSize = (Vector)this.xmlDataAccess.getElementsByName( "font-size" , (NodeList)column.get( ci ) ) ;
                String strCellFontSize = "\\fs18" ;
                if ( fontSize.size() > 0 ) {
                    strCellFontSize = "\\fs" + ((String)fontSize.get( 0 )) ;
                }
                
                Vector labelfontSize = new Vector() ;
                labelfontSize = (Vector)this.xmlDataAccess.getElementsByName( "label-font-size" , (NodeList)column.get( ci ) ) ;
                if ( labelfontSize.size() > 0 ) {
                    strCellFontSize = "\\fs" + ((String)labelfontSize.get( 0 )) ;
                }
                // replaced \sa120 for \sa0
                if ( ri == 0 && ci == 0 ) {
                    this.generatedDocument += "\\pard\\intbl\\pard\\plain \\intbl\\s5\\sa0\\cf1\\rtlch\\lang255\\ltrch\\dbch\\af4\\loch\\lang255\\ltrch\\loch" + strCellFontSize + strAlign ;
                } else {
                    if ( ci == 0 ) {
                        this.generatedDocument += "\\cell\\row\\pard\\plain \\intbl\\s5\\sa0\\cf1\\rtlch\\lang255\\ltrch\\dbch\\af4\\loch\\lang255\\ltrch\\loch" + strCellFontSize + strAlign ;
                    } else {
                        this.generatedDocument += "\\cell\\pard\\plain \\intbl\\s5\\sa0\\cf1\\rtlch\\lang255\\ltrch\\dbch\\af4\\loch\\lang255\\ltrch\\loch" + strCellFontSize + strAlign ;
                    }
                }
                
                this.createTextSection( (NodeList)column.get( ci ) , false , true , "" , true ) ;
            }
        }
    }
    
    private void closeTable( String reportSectionFormat ) {
        String strParagraphFormat = getParagraphFormat( reportSectionFormat , false ) ;
        if ( strParagraphFormat.equals( "" ) )
            this.generatedDocument += "\\cell\\row\\pard \\pard\\plain \\s1\\rtlch\\lang255\\ltrch\\loch\\lang1033\n";
        else
            this.generatedDocument += "\\cell\\row\\pard \\pard\\plain " + strParagraphFormat ;
    }
    
    private String getParagraphFormat( String reportSectionFormat , boolean start  ) {
        String strRet = "" ;
        if ( reportSectionFormat.equals( "greydoubleline" ) ) {
            if ( start ) {
                strRet = "\\par \\pard\\plain \\s3\\sa283\\brdrb\\brdrdb\\brdrw15\\brdrcf2\\brsp0{\\*\\brdrb\\brdlncol2\\brdlnin1\\brdlnout1\\brdlndist20}\\brsp0{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\rtlch\\afs12\\lang255\\ltrch\\dbch\\af4\\afs12\\langfe255\\loch\\f1\\fs12\\lang255 \n" ;
            } else {
                strRet = "\\s3\\sa283\\brdrb\\brdrdb\\brdrw15\\brdrcf2\\brsp0{\\*\\brdrb\\brdlncol2\\brdlnin1\\brdlnout1\\brdlndist20}\\brsp0{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\rtlch\\afs12\\lang255\\ltrch\\dbch\\af4\\afs12\\langfe255\\loch\\f1\\fs12\\lang255 \n" ;
                strRet += "\\par \\pard\\plain \\s4\\sa120{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\rtlch\\afs24\\lang255\\ltrch\\dbch\\af4\\afs24\\langfe255\\loch\\f1\\fs24\\lang255 \n" ;
            }
        }
        return strRet ;
    }
    
    private void closeRTF() {
        this.generatedDocument += "\\par }" ;
    }
    
    
    /**
     * retrieves the generated document as Object.
     * @return the generated document as Object.
     */
    public Object getGeneratedDocument() {
        return (Object)this.generatedDocument ;
    }
    
    /**
     * retrieves the root element of the xml document.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @return the root element of the xml document to be
     * processed.
     */
    public Element getInitElement() throws javax.xml.parsers.ParserConfigurationException , org.xml.sax.SAXException , java.io.IOException {
        return this.initElement ;
    }
    
    /**
     * retrieves the water-mark to be processed.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @return returns the water-mark for the draft.
     */
    public String getHoriWaterMark() {
        String retString = "{\\header \\pard\\plain \\s23\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0\n" ;
        retString += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1024\\langfe1024\\noproof\\insrsid5053302 \n" ;
        retString += "{\\shp{\\*\\shpinst\\shpleft-2858\\shptop-22840\\shpright6214\\shpbottom-20824\\shpfhdr0\\shpbxcolumn\\shpbxignore\\shpbypara\\shpbyignore\\shpwr3\\shpwrk0\\shpfblwtxt0\\shpz2\\shplid2051{\\sp{\\sn shapeType}{\\sv 136}}{\\sp{\\sn fFlipH}{\\sv 0}}{\\sp{\\sn fFlipV}{\\sv 0}}\n" ;
        retString += "{\\sp{\\sn gtextUNICODE}{\\sv VORSCHAU}}{\\sp{\\sn gtextSize}{\\sv 65536}}{\\sp{\\sn gtextFont}{\\sv Arial}}{\\sp{\\sn gtextFReverseRows}{\\sv 0}}{\\sp{\\sn fGtext}{\\sv 1}}{\\sp{\\sn gtextFNormalize}{\\sv 0}}{\\sp{\\sn fillColor}{\\sv 12632256}}\n" ;
        retString += "{\\sp{\\sn fFilled}{\\sv 1}}{\\sp{\\sn fLine}{\\sv 0}}{\\sp{\\sn wzName}{\\sv PowerPlusWaterMarkObject3}}{\\sp{\\sn pWrapPolygonVertices}{\\sv 8;13;(3749,1290);(107,1451);(-36,1612);(1000,17087);(3713,17087);(20529,17087)\n" ;
        retString += ";(20565,17087);(20815,16764);(21243,14507);(21314,11606);(21314,1451);(12496,1290);(3749,1290)}}{\\sp{\\sn posh}{\\sv 2}}{\\sp{\\sn posrelh}{\\sv 0}}{\\sp{\\sn posv}{\\sv 2}}{\\sp{\\sn posrelv}{\\sv 0}}{\\sp{\\sn fBehindDocument}{\\sv 1}}\n" ;
        retString += "{\\sp{\\sn fLayoutInCell}{\\sv 1}}}{\\shprslt\\par\\pard\\ql \\li0\\ri0\\widctlpar\\phmrg\\posxc\\posyc\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 {\\pict\\picscalex100\\picscaley100\\piccropl0\\piccropr0\\piccropt0\\piccropb0\n" ;
        retString += "\\picw15766\\pich2642\\picwgoal8938\\pichgoal1498\\wmetafile8\\bliptag-99986223\\blipupi-47{\\*\\blipuid fa0a54d1176503cdfeacd801af5a5099}\n" ;
        retString += "0100090000030f0b000006006002000000000400000003010800050000000b0200000000050000000c0278028b0e040000002e0118001c000000fb0210000700\n" ;
        retString += "00000000bc02000000000102022253797374656d00028b0e0000ec82110072edc63020e616000c0200008b0e0000040000002d010000030000001e0007000000\n" ;
        retString += "fc020000c0c0c0000000040000002d010100040000000601010008000000fa02050000000000ffffff00040000002d0102004400000024032000b8006402a200\n" ;
        retString += "19028a00ce015c0038012d00a2001600560000000b0044000b00630078008200e500a1005301c000c001c700da01ce00f301d4000b02d9002202df000a02e500\n" ;
        retString += "f101ec00d801f300c001130153013301e5005401780074010b00b4010b009d0156008601a200570138012801ce0110011902f9006402b800640208000000fa02\n" ;
        retString += "00000000000000000000040000002d010300040000000601010007000000fc020000ffffff000000040000002d010400040000002d0101000400000006010100\n" ;
        retString += "040000002d01020012020000380502008b007b00da013f01da012d01db011b01dc010a01de01f900e001e800e301d800e601c900ea01ba00ee01ac00f3019e00\n" ;
        retString += "f8019000fe018400040277000b026b00120260001902550022024b002a024100330238003c023000450228004e02210058021b00620215006d02110078020c00\n" ;
        retString += "830209008e0206009a020300a6020200b2020100be020000c6020100cf020100d7020200de020300e6020400ee020600f6020800fd020a0005030d000c031000\n" ;
        retString += "130313001a03170021031b0028031f002f03230035032800420333004e033e005a034b00640358006e036600770375007f03860083038e00870397008d03a900\n" ;
        retString += "9303bc009703cf009b03e3009e03f700a0030d01a2032201a2033901a2034f01a00365019e037a019b038f019703a3019203b7018c03ca018503dc018103e501\n" ;
        retString += "7d03ee017903f6017503fe01700306026c030d026703140262031b025c0322025703280251032f024b03340245033a023f033f0238034402320349022b034e02\n" ;
        retString += "240352021d03560217035a0210035d020803600201036302fa026502f3026802eb026902e4026b02dc026c02d5026d02cd026e02c6026e02be026f02b6026e02\n" ;
        retString += "ad026e02a5026d029d026c0295026b028d026902860267027e026502770262026f025f0268025b02610258025a02540253024f024c024b024502460239023b02\n" ;
        retString += "2c022f0221022202170215020d0206020402f701fc01e701f501d601ef01c401e901b201e4019f01e1018d01de017a01db016701da015301da013f01da013f01\n" ;
        retString += "1b0241011b024e011c025b011c0268011e0274011f02800121028c01240297012702a2012a02ac012d02b6013102c0013502ca013a02d3013f02db014402e401\n" ;
        retString += "4902ec014f02f3015502fb015b0201026202070268020d026f021202760217027d021b0285021f028c022202940224029c022702a4022802ac022a02b5022a02\n" ;
        retString += "be022a02c6022a02cf022a02d7022802e0022702e8022402f0022202f7021f02ff021b02060317020d03120214030d021b030702210301022703fa012d03f301\n" ;
        retString += "3303eb013903e3013e03db014303d2014703c8014b03bf014f03b4015203aa0155039f01580393015b0388015d037b015e036f015f0362016003540161034601\n" ;
        retString += "6103380161032601600315015e0304015c03f3005903e4005603d4005203c6004e03b8004803aa0042039e003c039200350387002e037d00260374001d036b00\n" ;
        retString += "140363000f035f000a035c0000035600f6025000ec024c00e1024900d6024700ca024500bf024500b6024500ae024600a60247009e02480097024a008f024d00\n" ;
        retString += "87025000800253007902570072025b006b026000640265005e026b0058027100510278004b027f004502860040028f003b0297003602a1003202ab002e02b500\n" ;
        retString += "2a02c1002702cc002402d9002202e6002002f3001e0202011d0211011c0220011b0230011b0241011b024101040000002d010300040000000601010004000000\n" ;
        retString += "2d010400040000002d0101000400000006010100040000002d010200420100003805020071002d00f5036402f5030b00c8040b00d8040b00e6040c00f4040d00\n" ;
        retString += "01050f000c051100170514002105170029051b00310520003905250040052b00470532004e0539005405410059054b005e05540063055f0067056a006a057500\n" ;
        retString += "6d0580006f058b00710597007205a3007205af007205b7007205bf007105c6007005ce006f05d5006e05dc006c05e3006a05ea006805f1006505f70060050401\n" ;
        retString += "5905100151051b014d0520014905260144052a013f052f013a053301340537012e053b0128053f01210542011a054501130548010c054b0104054d01fc044f01\n" ;
        retString += "f4045101ec045301f2045701f7045a01fd045e0102056201060566010b0569010e056d0112057101190579012005820126058b012d05950133059f013a05aa01\n" ;
        retString += "4005b5014605c1015b05ea017005130285053b02990564024a0564023a0545022a0526021a0507020b05e7010405da01fe04ce01f704c201f204b701ec04ae01\n" ;
        retString += "e704a401e2049c01dd049401d9048d01d4048701d0048101cc047c01c8047701c4047301c1046f01bd046c01b6046701af046201a8045f01a0045c019d045b01\n" ;
        retString += "9a045b0196045a0192045a018d045a0188045901830459017d0459013404590134046402f5036402f503640234041401bc041401c6041401d0041401da041301\n" ;
        retString += "e2041201ea041001f2040e01f9040c01ff040901050506010b0503011005ff001505fa001905f6001d05f1002105eb002405e5002705df002a05d8002c05d200\n" ;
        retString += "2e05cb002f05c4003005bd003105b6003105af003105a50030059b002e0591002b05880028058000230578001e05700019056900120562000b055d0002055800\n" ;
        retString += "f9045400ef045100e4044f00d8044e00cb044e0034044e003404140134041401040000002d0103000400000006010100040000002d010400040000002d010100\n" ;
        retString += "0400000006010100040000002d0102006002000024032e01c005a301fb059d01fd05a801fe05b2010006bc010306c5010506ce010806d7010c06df010f06e601\n" ;
        retString += "1306ee011806f4011d06fb01230601022906060230060b02370610023f0615024706190250061d0259062002620623026b062502750626027e06270288062702\n" ;
        retString += "910627029a062602a2062502aa062402b2062202ba062002c1061d02c8061a02cf061602d5061202db060e02e0060a02e5060502ea060002ee06fa01f206f401\n" ;
        retString += "f506ee01f706e801fa06e201fc06dc01fd06d501fe06cf01ff06c801ff06c101ff06ba01fe06b301fd06ad01fc06a701fa06a101f8069b01f5069501f2068f01\n" ;
        retString += "ee068a01ea068501e6068101e1067c01db067801d5067401ce067001c7066c01c4066b01c1066a01be066801ba066701b6066501b2066401ad066201a8066001\n" ;
        retString += "a3065e019d065c0197065a019006580189065601820654017a06510172064f016a064c0162064a015b064701540645014d0643014606400140063e013a063c01\n" ;
        retString += "340639012f0637012a06350125063201210630011d062e0119062c01160629010e06230106061d01ff051701f8051001f2050901ec050101e705f900e305f100\n" ;
        retString += "df05e900dc05e000d905d700d605ce00d405c400d305bb00d205b100d205a700d2059b00d3059000d5058500d7057b00da057000de056600e2055b00e7055100\n" ;
        retString += "ec054800f2053f00f805360000062f00070627001006210019061b00220615002c06100037060c00410609004c06060057060400620602006e0601007a060100\n" ;
        retString += "8706010094060200a0060400ac060600b8060900c3060d00ce061100d8061600e2061c00eb062200f4062900fc063100030739000a07420010074c0016075600\n" ;
        retString += "1b07600020076b00240777002707820029078e002b079b002d07a7002e07b400f106ba00ef06ac00ed069f00ea069300e6068800e2067e00dc067400d7066c00\n" ;
        retString += "d0066400c9065d00c0065700b7065200ad064e00a2064b00970649008a0647007d0647006f0647006206490056064b004b064d00410651003d06530038065600\n" ;
        retString += "3406580030065b002d065e0029066100230668001e066f001906770016067e001306870011068f000f0698000f06a1000f06a9001006b1001206b8001406bf00\n" ;
        retString += "1606c5001906cb001d06d1002206d6002406d9002706dc002a06de002e06e1003306e4003706e6003d06e9004206ec004906ee004f06f1005606f4005e06f600\n" ;
        retString += "6606f9006f06fc007706fe00810601018a060401930607019c060901a4060c01ac060e01b4061101bb061301c2061601c8061801ce061b01d4061d01d9061f01\n" ;
        retString += "de062101e3062301e7062601ea062801f4062e01fe06350107073c010f07430116074b011d07530123075c01280765012d076e01310778013407820137078d01\n" ;
        retString += "390798013b07a3013c07af013c07bb013c07c6013a07d2013907de013607e9013307f4013007ff012b070a022607150220071f021a072902130732020b073b02\n" ;
        retString += "03074202fb064a02f1065102e8065702dd065c02d2066102c7066502bc066902b0066b02a4066d0298066e028b066f027b066e026c066d025e066b0250066902\n" ;
        retString += "42066502360661022a065c021f065702140650020a06490201064102f8053902ef053002e8052602e1051b02da050f02d4050302cf05f601cb05ea01c705dc01\n" ;
        retString += "c405cf01c205c101c105b201c005a301040000002d0103000400000006010100040000002d010400040000002d0101000400000006010100040000002d010200\n" ;
        retString += "1002000024030601e708920106099c012609a6012309b2012109bd011d09c9011a09d4011609de011309e8010e09f2010a09fc010609050201090e02fc081602\n" ;
        retString += "f6081e02f1082602eb082d02e5083402df083b02d8084102d1084702cb084d02c4085202bc085602b5085b02ad085e02a50862029d086502950867028d086a02\n" ;
        retString += "84086b027c086d0273086e026a086e0260086f0257086e024e086e0244086d023c086c0233086b022a086902220867021a086502130862020b085f0204085c02\n" ;
        retString += "fd075802f6075502ef075002e9074c02e3074702dd074202d7073c02d2073702cc073102c7072b02c2072402bd071d02b8071602b4070f02af070802ab070002\n" ;
        retString += "a707f801a307ef01a007e7019d07de019907d4019307c1018e07ae018a079b01860787018407720182075e01800749018007330180071c01820706018407f000\n" ;
        retString += "8707db008b07c7009007b4009607a1009d078f00a0078700a4077e00a8077600ac076e00b1076700b5076000ba075900bf075200c4074b00ca074500cf073f00\n" ;
        retString += "d5073900db073400e1072f00e7072a00ee072500f4072000fb071c0002081800080815000f08120016080f001d080c0025080a002c080800330806003b080400\n" ;
        retString += "420803004a0802005208010059080100610801006a080100730801007b080200840804008c080500940807009c080900a3080c00ab080f00b2081300b9081600\n" ;
        retString += "c0081a00c7081f00cd082400d4082900da082f00e0083500e5083b00eb084200f0084800f5084f00fa085700ff085e000309660007096e000b0977000f098000\n" ;
        retString += "130989001609920019099c001b09a6001e09b000ff08b900e008c200de08ba00db08b300d908ab00d608a400d4089d00d1089600ce089000cb088a00c8088400\n" ;
        retString += "c5087e00c2087900be087400bb086f00b7086b00b3086700b0086300ac085f00a7085c009f085600960850008c084c0082084900770847006c08450060084500\n" ;
        retString += "59084500520845004c084600460847003f08480039084a0033084b002d084d0028084f00220852001d0855001808580012085b000e085e000908620004086600\n" ;
        retString += "fb076f00f3077800eb078200e5078d00de079800d907a500d407b200d007bf00cd07cd00ca07db00c707ea00c507f800c3070701c2071501c1072401c1073301\n" ;
        retString += "c1074601c2075801c4076a01c6077b01c8078b01cb079b01cf07aa01d307b901d807c601dd07d301e307df01ea07eb01f107f501f907fe01010807020a080e02\n" ;
        retString += "130815021d081b0227082002310824023b0827024608290250082a025b082b0262082a0268082a026f082902750828027b08270281082502870823028c082102\n" ;
        retString += "92081e0297081c029d081802a2081502a7081102ac080d02b0080902b5080402b908ff01be08fa01c208f401c608ee01ca08e801cd08e201d008db01d408d401\n" ;
        retString += "d708cd01da08c501dc08bd01df08b501e108ad01e308a401e5089b01e7089201040000002d0103000400000006010100040000002d010400040000002d010100\n" ;
        retString += "0400000006010100040000002d0102001e00000024030d007609640276090b00b5090b00b5090201ad0a0201ad0a0b00ec0a0b00ec0a6402ad0a6402ad0a4901\n" ;
        retString += "b5094901b509640276096402040000002d0103000400000006010100040000002d010400040000002d0101000400000006010100040000002d0102006a000000\n" ;
        retString += "380502001a001800210b6402380b19024f0bce017d0b3801ab0ba200c20b5600d80b0b001d0c0b00350c56004d0ca2007e0c3801af0cce01c80c1902e00c6402\n" ;
        retString += "980c64028a0c37027c0c09026e0cdc01600cae01990bae018c0bdc017e0b0902710b3702640b6402210b6402210b6402ab0b6d014d0c6d01400c4401340c1a01\n" ;
        retString += "270cf1001b0cc700150cb400100ca3000b0c9200070c8200030c7300ff0b6400fc0b5700f90b4a00f70b5900f40b6700ee0b8400e70ba000df0bbd00d20be900\n" ;
        retString += "c50b1501b80b4101ab0b6d01ab0b6d01040000002d0103000400000006010100040000002d010400040000002d0101000400000006010100040000002d010200\n" ;
        retString += "2201000024038f004c0e0b008b0e0b008b0e67018b0e72018b0e7d018a0e87018a0e9101890e9b01880ea501880eaf01870eb801850ec001840ec901830ed101\n" ;
        retString += "810ed901800ee1017e0ee8017c0ef0017b0ef601760e0302710e10026b0e1c02640e27025c0e3102530e3b024a0e4402400e4d023b0e5102350e55022f0e5902\n" ;
        retString += "290e5c02230e5f021c0e6202160e64020f0e6602080e6802000e6a02f90d6b02f10d6d02e90d6d02e10d6e02d90d6f02d00d6f02c00d6e02b00d6d02a10d6b02\n" ;
        retString += "930d6702860d6302790d5e026d0d5802630d5202580d4a024f0d4102460d38023e0d2e02370d2302300d17022a0d0a02260dfd01230df601210def011f0de701\n" ;
        retString += "1e0ddf011c0dd7011b0dce01190dc501180dbc01170db201160da801150d9e01150d9401140d8901140d7e01130d7201130d6701130d0b00530d0b00530d6601\n" ;
        retString += "530d7001530d7901530d8201530d8b01540d9301540d9b01550da301550daa01560db101570db801580dbe01590dc4015a0dca015b0dcf015d0dd4015e0dd901\n" ;
        retString += "610de201650deb01690df3016e0dfa01730d0102790d08027f0d0d02860d13028d0d1802950d1c029d0d1f02a50d2202ae0d2402b70d2602c10d2602cb0d2702\n" ;
        retString += "d30d2702db0d2602e30d2502eb0d2402f20d2302f90d2102000e1f02060e1d020d0e1a02120e1702180e14021d0e1102220e0d02260e08022a0e04022e0eff01\n" ;
        retString += "320efa01350ef401380eed013b0ee6013e0edf01400ed701420ece01440ec501460ebb01470eb101490ea6014a0e9a014b0e8e014b0e81014c0e74014c0e6601\n" ;
        retString += "4c0e0b00040000002d0103000400000006010100040000002d010400040000002701ffff04000000020101001c000000fb029cff000000000000900100000000\n" ;
        retString += "0440001254696d6573204e657720526f6d616e0000000000000000000000000000000000040000002d010500050000000902000000020d000000320a910016000100040016003700a90eae0220d12d00040000002d010000030000000000}\\par}}}{\\insrsid5053302 \n" ;
        retString += "\\par }}\n" ;
        
        
        return retString;
        
    }
    
    /**
     * retrieves the water-mark to be processed.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @return returns the water-mark for the draft.
     */
    public String getDiagWaterMark() {
        String retString = "{\\header \\pard\\plain \\s23\\ql \\li0\\ri0\\sl240\\slmult0\\widctlpar\\tqc\\tx4536\\tqr\\tx9072\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        retString += "\\f1\\fs22\\lang1031\\langfe1031\\cgrid\\langnp1031\\langfenp1031 {\\lang1024\\langfe1024\\noproof\\insrsid5053302 \n" ;
        retString += "{\\shp{\\*\\shpinst\\shpleft-4275\\shptop-30393\\shpright6190\\shpbottom-28068\\shpfhdr0\\shpbxcolumn\\shpbxignore\\shpbypara\\shpbyignore\\shpwr3\\shpwrk0\\shpfblwtxt0\\shpz2\\shplid2051{\\sp{\\sn shapeType}{\\sv 136}}{\\sp{\\sn fFlipH}{\\sv 0}}{\\sp{\\sn fFlipV}{\\sv 0}}\n" ;
        retString += "{\\sp{\\sn rotation}{\\sv 20643840}}{\\sp{\\sn gtextUNICODE}{\\sv VORSCHAU}}{\\sp{\\sn gtextSize}{\\sv 65536}}{\\sp{\\sn gtextFont}{\\sv Arial}}{\\sp{\\sn gtextFReverseRows}{\\sv 0}}{\\sp{\\sn fGtext}{\\sv 1}}{\\sp{\\sn gtextFNormalize}{\\sv 0}}\n" ;
        retString += "{\\sp{\\sn fillColor}{\\sv 12632256}}{\\sp{\\sn fFilled}{\\sv 1}}{\\sp{\\sn fLine}{\\sv 0}}{\\sp{\\sn wzName}{\\sv PowerPlusWaterMarkObject3}}{\\sp{\\sn pWrapPolygonVertices}{\\sv 8;96;(21229,1254);(19619,1672);(19217,697);(19124,1254);(17732,1394);(17391,975)\n" ;
        retString += ";(17237,2230);(17051,4599);(15999,1394);(15720,697);(15628,1254);(13833,1394);(13802,5714);(12873,1812);(12285,139);(12069,975);(11574,1951);(11233,3623);(11017,5714);(10119,1951);(9469,139);(9284,975);(9005,1533);(8758,2090)\n" ;
        retString += ";(8541,4041);(7272,1115);(5787,1394);(5756,4041);(4456,1394);(4270,1115);(3744,975);(3249,2230);(2352,1115);(2104,1394);(2042,1672);(1981,2926);(1826,5295);(557,1672);(248,975);(31,1394);(31,2090);(1021,16583)\n" ;
        retString += ";(1145,17001);(1207,17280);(1454,16862);(1826,11706);(1981,12403);(3683,17280);(4363,17141);(4425,17001);(4858,15886);(5137,13935);(5787,16862);(6158,17001);(6189,12403);(6344,13099);(7860,17141)\n" ;
        retString += ";(7922,17001);(8201,17141);(8201,16583);(7860,13517);(8046,14493);(9284,17559);(9377,17280);(10026,17001);(10429,15747);(10552,14214);(10614,13378);(10893,14632);(12069,17559);(12193,17280);(12750,16862)\n" ;
        retString += ";(13152,15329);(13338,16165);(14142,17419);(14235,17001);(14235,10870);(15628,16862);(15999,17280);(16030,15747);(16308,16862);(16680,17001);(16772,15886);(16927,13517);(17391,12403);(18444,16862);(18877,16862)\n" ;
        retString += ";(18784,15468);(19836,17141);(20486,17280);(20579,17141);(21012,16026);(21229,13935);(21321,11148);(21321,1672);(21229,1254)}}{\\sp{\\sn posh}{\\sv 2}}{\\sp{\\sn posrelh}{\\sv 0}}{\\sp{\\sn posv}{\\sv 2}}{\\sp{\\sn posrelv}{\\sv 0}}\n" ;
        retString += "{\\sp{\\sn fBehindDocument}{\\sv 1}}{\\sp{\\sn fLayoutInCell}{\\sv 1}}}{\\shprslt\\par\\pard\\ql \\li0\\ri0\\widctlpar\\phmrg\\posxc\\posyc\\dxfrtext180\\dfrmtxtx180\\dfrmtxty0\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n" ;
        retString += "{\\pict\\picscalex100\\picscaley100\\piccropl0\\piccropr0\\piccropt0\\piccropb0\\picw14617\\pich14300\\picwgoal8287\\pichgoal8107\\wmetafile8\\bliptag1616515694\\blipupi187{\\*\\blipuid 605a126e330cc4613cc9d4ea1c587aaa}\n" ;
        retString += "0100090000036f0c00000600bc02000000000400000003010800050000000b0200000000050000000c02360d780d040000002e0118001c000000fb0210000700\n" ;
        retString += "00000000bc02000000000102022253797374656d000d780d0000a084110072edc63020e616000c020000780d0000040000002d010000030000001e0007000000\n" ;
        retString += "fc020000c0c0c0000000040000002d010100040000000601010008000000fa02050000000000ffffff00040000002d0102004c000000240324008202310d3202\n" ;
        retString += "060de201dc0c4101870ca100330c5000080c0000de0b3800a60baa00e60b1d01250c8f01650c0202a50c0f02ac0c1c02b40c2902bb0c3602c30c4302ca0c4f02\n" ;
        retString += "d20c5b02d90c6602e10c5702c80c4802af0c3902950c2b027b0cec01080cae01950b6f01210b3001ae0a6501790a8f01ca0ab9011a0b0e02bb0b62025c0c8c02\n" ;
        retString += "ac0cb702fc0c8202310d08000000fa0200000000000000000000040000002d010300040000000601010007000000fc020000ffffff000000040000002d010400\n" ;
        retString += "040000002d0101000400000006010100040000002d0102006a02000038050200aa0088007f02560b7002470b6202380b5502290b4802190b3d020a0b3202fb0a\n" ;
        retString += "2802ec0a1f02dc0a1702cd0a0f02be0a0902af0a0302a00afe01900afa01810af601720af301630af201540af101450af001370af101290af2011b0af4010e0a\n" ;
        retString += "f701010afb01f4090002e7090502db090b02cf091202c3091a02b8092202ad092b02a209350298093c02910943028b094a02850951027f0959027a0960027509\n" ;
        retString += "6802710970026c0978026809810265098902620992025f099b025c09a4025a09ad025809b7025709c0025609ca025509d3025509dd025409e7025509f0025509\n" ;
        retString += "fa025609040358090e03590917035b0921035e092b036009350363093f03670949036b0953036f095d0374096703790971037e097b038309850389098f038f09\n" ;
        retString += "98039609a2039c09ac03a309b503aa09bf03b209c803ba09d203c209db03ca09e403d309ed03dc09f703e609ff03ef090804f8091004020a18040c0a2004150a\n" ;
        retString += "27041f0a2e04290a3504330a3b043d0a4104470a4704510a4c045b0a5204650a56046f0a5b047a0a5f04840a63048e0a6604980a6904a20a6c04ac0a6e04b60a\n" ;
        retString += "7004c00a7204ca0a7304d40a7404dd0a7404e70a7404f10a7404fa0a7304040b72040d0b7004160b6f041f0b6c04290b6a04310b67043a0b6404420b61044b0b\n" ;
        retString += "5e04530b5a045b0b5504620b51046a0b4c04710b4704780b42047f0b3c04860b36048d0b3004930b29049a0b2204a00b1b04a60b1304ac0b0c04b10b0404b60b\n" ;
        retString += "fc03bb0bf403bf0beb03c30be303c70bda03ca0bd103cd0bc803cf0bbf03d10bb603d30bac03d40ba303d50b9903d60b8f03d60b8603d60b7c03d60b7203d50b\n" ;
        retString += "6803d40b5f03d30b5503d10b4b03cf0b4103cc0b3703c90b2d03c60b2303c30b1903bf0b1003ba0bfc02b10be902a70bd6029c0bc302900bb102830ba002750b\n" ;
        retString += "8f02660b7f02560b7f02560bb502220bc0022d0bcb02370bd602410be1024a0bec02520bf7025a0b0303610b0e03680b19036e0b2403730b2f03780b3a037c0b\n" ;
        retString += "4503800b5003830b5b03860b6603880b7103890b7c038a0b86038a0b91038a0b9b03890ba403880bae03860bb703840bc003810bc9037d0bd203790bda03740b\n" ;
        retString += "e2036f0bea03690bf103630bf8035c0bff03550b06044d0b0c04450b11043d0b1604350b1a042c0b1e04230b21041a0b2304110b2504070b2704fd0a2704f30a\n" ;
        retString += "2804e90a2704de0a2604d40a2504c90a2304bd0a2004b20a1c04a70a18049c0a1404900a0f04850a09047a0a02046e0afb03630af403570aeb034c0ae203400a\n" ;
        retString += "d903340ace03290ac4031d0ab803110aa903030a9a03f5098b03e9097c03dd096d03d2095e03c9094e03c0093f03b8093803b5093003b2092903af092103ac09\n" ;
        retString += "1a03aa091303a8090b03a6090403a509fd02a309f502a209ee02a209e702a109e002a109d902a109d202a109cb02a209c402a309bd02a409b702a509b002a709\n" ;
        retString += "aa02a909a402ab099e02ae099802b0099202b3098c02b7098702ba098102be097702c6096d02cf096702d6096002dd095b02e5095602ec095102f4094d02fd09\n" ;
        retString += "4902050a46020e0a4302170a4102200a3f02290a3e02330a3d023d0a3d02470a3d02520a3e025c0a4002670a4202730a45027e0a4902890a4e02950a5302a10a\n" ;
        retString += "5902ad0a6002b90a6802c60a7002d20a7a02df0a8402ec0a8f02f90a9b02070ba702140bb502220bb502220b040000002d010300040000000601010004000000\n" ;
        retString += "2d010400040000002d0101000400000006010100040000002d01020074010000380502008000370025058e0a3b03a308e803f707ee03f007f403ea07fb03e407\n" ;
        retString += "0104df070704da070d04d5071304d0071904cc071e04c8072404c4072904c1072f04be073404bb073904b8073f04b6074404b4074e04b2075904b0076304af07\n" ;
        retString += "6f04ae077a04af078004b0078604b1078b04b2079204b4079804b6079e04b807aa04bd07b604c207c204c807cd04cf07d804d707e304df07ee04e807f804f207\n" ;
        retString += "fe04f8070405ff070a0506080f050c081405130819051a081d0521082105280825053008280537082b053e082e05460830054d083205550834055d0835056408\n" ;
        retString += "36056c083705740837057c083605840835058c083405940833059c083005a4082e05ac082b05b4082705bc082305c4081f05cc081a05d4081505dd081005e508\n" ;
        retString += "1805e3081f05e1082705e0082e05df083505de083b05de084105de084705de085305df086005e1086d05e3087b05e5088805e8089605ec08a405f008b305f508\n" ;
        retString += "e5050509180615094a0626097c0636093c06770915066a09ef055e09c8055109a205450992053f0982053b0974053609670532095a052f094e052c0943052909\n" ;
        retString += "3905260930052409270522091f0521091805200911051f090a051f0904051f09fe042009f9042009f4042109eb042309e2042609da042a09d7042c09d4042e09\n" ;
        retString += "d0043109cc043409c8043709c4043b09c0044009bb0444097f04800959055a0a25058e0a25058e0a47044809b604d908be04d008c604c808cd04c008d304b708\n" ;
        retString += "d804af08dd04a808e004a008e3049908e6049108e8048a08e9048308e9047b08e9047408e8046c08e6046508e4045d08e2045508de044e08db044708d7044008\n" ;
        retString += "d3043908ce043308c8042d08c3042708be042208ba041f08b6041b08b1041708a80411089e040c08940408088a0405088004030876040208700401086b040208\n" ;
        retString += "66040208600403085b04040856040608500408084b040a0845040d08400410083b0414083504180830041c082b0420082504250820042b08a503a60847044809\n" ;
        retString += "47044809040000002d0103000400000006010100040000002d010400040000002d0101000400000006010100040000002d010200bc02000024035c01fe057908\n" ;
        retString += "2906430833064b083d0653084706590851065f085a066408630669086d066c0876066f087f06720888067408920675089b067508a5067408af067308b9067108\n" ;
        retString += "c3066f08cd066b08d7066708e1066308ea065d08f3065708fc065108050749080e07420815073a081b07320821072b08270723082b071b083007130833070b08\n" ;
        retString += "370702083907fa073b07f2073c07e9073d07e1073d07da073d07d2073c07ca073a07c2073707bb073507b4073107ad072e07a6072a07a0072507990720079307\n" ;
        retString += "1b078e07150788070f07830709077f0703077b07fc067707f6067407ef067207e8067007e1066e07d9066e07d2066e07ca066e07c2066f07b9067107b1067307\n" ;
        retString += "a8067607a5067707a20678079e067a079a067c0795067e07900680078b068307850685077f06880779068b0772068f076b069207630696075c069a0753069f07\n" ;
        retString += "4b06a3074206a8073a06ac073206b0072a06b4072206b8071b06bb071406be070d06c1070706c4070106c607fb05c907f505cb07f005cc07eb05ce07e605cf07\n" ;
        retString += "e105d007d505d207ca05d307bf05d407b405d407a905d3079e05d1079405cf078a05cc078005c8077605c4076c05bf076305b9075905b3075005ac074805a507\n" ;
        retString += "3f059d07360593072e05890726057f071f0574071905690714055e070f0552070a0546070805400707053a070505340704052e07030522070205150703050907\n" ;
        retString += "0405fd060705f1060a05e4060e05d8061305cd061905c1062005b6062705ab062f05a1063805960641058c064c0582065705780662056f066e0568067a056106\n" ;
        retString += "86055b06920555069f055106ac054e06b8054c06c5054a06d2054a06d8054a06df054a06e5054b06ec054c06f2054d06f8054f06ff0550060506520612065706\n" ;
        retString += "1f065c062b06620638066906430671064f0679065b06820666068c065006a7063906c2063306bd062d06b8062606b4062006b0061a06ac061406a8060e06a506\n" ;
        retString += "0806a2060106a006fb059e06f5059c06ef059a06ea059906e4059806de059706d8059706d2059706cc059806c7059806c1059a06bb059b06b5059d06af059f06\n" ;
        retString += "aa05a206a405a5069e05a8069905ac069305b0068d05b4068805b9068205be067c05c3067705c9067105cf066c05d5066805da066405e0066005e6065c05eb06\n" ;
        retString += "5905f1065605f7065405fc0652050207500507074f050d074e0512074e0517074e051d074e0527075005310752053b07550545075a054e075f05560765055f07\n" ;
        retString += "6c05670773056d077a0572078105770788057b0790057e07970580079f058207a7058307ab058307b0058307b5058207ba058107c0058007c6057e07cc057c07\n" ;
        retString += "d3057907da057607e2057307ea056f07f2056b07fb056707040662070e065d0718065807210652072b064d07340648073d06440746063f074e063b0756063707\n" ;
        retString += "5d063407640631076b062d0772062b07780628077e06260783062407880622078d0621079a061e07a7061c07b4061a07c1061a07cd061a07da061c07e5061e07\n" ;
        retString += "f1062107fd06250708072a0713072f071e07350729073c07330744073d074d07470757075107600759076b0761077607690781076f078c07750798077a07a507\n" ;
        retString += "7f07b2078107b8078307bf078407c5078607cc078707d9078807e6078807f3078707010885070e0882071b087e07280879073408740741086d074d0866075808\n" ;
        retString += "5d07640854076f084a077908440780083d07860836078c08300792082907970822079c081b07a1081507a5080e07a9080707ad080007b108fa06b408f306b708\n" ;
        retString += "ec06ba08e506bc08de06be08d706c008d006c208c906c308c206c408bb06c508b406c608ad06c608a606c6089f06c6089706c5089006c4088906c3088206c208\n" ;
        retString += "7a06c0087306be086c06bc085d06b7084f06b1084106aa083306a20825069908180690080b068508fe057908040000002d010300040000000601010004000000\n" ;
        retString += "2d010400040000002d0101000400000006010100040000002d0102004e020000240325018208d905a408c705c608b605ce08c105d508cd05dc08d905e208e505\n" ;
        retString += "e808f005ed08fc05f1080706f6081306f9081e06fc082906ff08340601093f0603094a060409540605095f0606096a060509740605097e060409880602099206\n" ;
        retString += "00099c06fd08a506fa08af06f708b806f208c106ee08ca06e908d206e308db06dd08e306d708eb06d008f306c908fb06c1080207b9080a07b1081007a9081707\n" ;
        retString += "a0081d07980822079008270788082c077f083007770834076e08370765083a075d083c0754083e074b084007420841073908410730084207270841071d084107\n" ;
        retString += "140840070b083f0701083e07f8073c07ee073a07e4073707db073407d1073107c7072d07bd072907b3072407a9071f0794071507800709076d07fd065a07ef06\n" ;
        retString += "4707e1063407d2062207c2061007b1060707a706fe069d06f5069406ed068a06e5068006dd067606d6066d06cf066306c8065906c1064f06bb064506b6063b06\n" ;
        retString += "b0063106ab062706a6061d06a20613069e0609069a0600069706f6059406ec059106e2058f06d9058d06cf058c06c5058b06bc058a06b2058a06a9058a06a005\n" ;
        retString += "8a0696058b068d058c0684058e067b058f06720591066905940660059606580599064f059d064705a0063f05a4063805a8063005ad062805b2062105b7061a05\n" ;
        retString += "bc061305c2060c05c8060505ce06ff04d606f804dd06f104e506eb04ed06e504f506e004fd06db040507d6040d07d2041607cf041f07cb042807c9043107c604\n" ;
        retString += "3a07c5044307c3044d07c2045607c2046007c2046a07c2047307c3047d07c4048707c6049107c8049b07cb04a507cd04af07d104b907d404c407d904ce07dd04\n" ;
        retString += "d807e204e207e804ed07ee04f707f404e5071505d3073605cb073105c3072c05bb072805b3072405ab072105a3071d059c071b05950718058d07160586071405\n" ;
        retString += "7f07120578071105720710056b070f0564070f055e070f0558070f05520710054c07110546071205400713053a071505340717052f071a0529071c0524071f05\n" ;
        retString += "1e0723051907260514072a050f072e050a07330505073705ff063d05fa064305f5064905f1064f05ed065505e9065b05e5066105e2066705df066e05dd067405\n" ;
        retString += "db067b05d9068205d7068805d6068f05d5069605d5069d05d506a505d506ac05d506b305d606ba05d706c105d806c905d906d005db06d705dd06de05df06e505\n" ;
        retString += "e206ec05e506f405e806fb05eb060206ef060906f3061106fc061f0605072d060e073b0618074806230755062e07620639076f0645077b0655078a0664079806\n" ;
        retString += "7407a6068307b2069307bd06a207c806b107d106c107da06c807dd06d007e106d707e406df07e706e607ea06ee07ec06f507ee06fc07f0060308f1060a08f206\n" ;
        retString += "1108f3061808f4061f08f4062608f4062d08f3063408f3064108f0064d08ed065908e9066508e4067008de067a08d8068408d0068d08c8069208c2069708bc06\n" ;
        retString += "9c08b706a008b106a408ab06a708a406aa089e06ad089806b0089106b2088a06b4088306b5087c06b6087506b7086e06b7086606b7085f06b6085706b5084f06\n" ;
        retString += "b4084706b3083f06b1083706ae082f06ac082706a9081e06a5081606a1080e069d0805069808fc059308f3058e08eb058808e2058208d905040000002d010300\n" ;
        retString += "0400000006010100040000002d010400040000002d0101000400000006010100040000002d0102001e00000024030d00a3091006b8072604ec07f203b508bc04\n" ;
        retString += "8009f103b6082803ea08f402d40adf04a10a1205b9092b04ef08f504d609dd05a3091006040000002d0103000400000006010100040000002d01040004000000\n" ;
        retString += "2d0101000400000006010100040000002d0102006e000000380502001a001a00ff0ab404d40a6404aa0a1404550a7403000ad302d5098302aa093402e209fc01\n" ;
        retString += "330a2602840a4f02270ba202c90bf4021a0c1d036c0c4703310c8203000c6803d00b4e039f0b35036f0b1b03cc0abe03e70aed03010b1d041b0b4d04360b7d04\n" ;
        retString += "ff0ab404ff0ab404a60a7a032a0bf602fe0ade02d20ac602a60aaf027a0a9702660a8c02540a8202420a7802310a6e02220a65021a0a6102130a5d020c0a5902\n" ;
        retString += "050a5402ff095102f8094d020c0a68021e0a8502300aa202410abf025a0aee02740a1d038d0a4b03a60a7a03a60a7a03040000002d0103000400000006010100\n" ;
        retString += "040000002d010400040000002d0101000400000006010100040000002d010200520100002403a700aa0b3400de0b0100f90c1c01020d25010b0d2e01130d3701\n" ;
        retString += "1b0d4001230d48012a0d5101310d5901380d61013e0d6a01440d71014a0d79014f0d8101540d8901590d90015d0d9701610d9e01650da501680dad016b0db401\n" ;
        retString += "6e0dbb01700dc201720dca01740dd101760dd801770de001780de701790def01790df601790dfe01790d0602780d0d02780d1502770d1d02750d2402730d2c02\n" ;
        retString += "710d34026f0d3b026c0d4302680d4a02640d5202600d59025b0d6002560d6802510d6f024b0d7602450d7d023f0d8402380d8b02310d92022a0d9802230d9e02\n" ;
        retString += "1c0da402150da9020e0dae02070db302000db702f90cbb02f20cbf02eb0cc202e40cc502dc0cc802d50cca02ce0ccc02c70ccd02bf0cce02b80ccf02b10cd002\n" ;
        retString += "a90cd002a20cd0029b0cd002930ccf028c0cce02840ccd027d0ccb02750cc9026e0cc702670cc4025f0cc102580cbe02500cba02490cb602410cb202390cad02\n" ;
        retString += "310ca802290ca202210c9d02180c9602100c9002070c8902fe0b8202f50b7a02ec0b7202e30b6a02da0b6102d00b5802c70b4e02ab0a3301df0aff00fa0b1a02\n" ;
        retString += "020c2202090c2902110c3102180c37021f0c3e02260c44022d0c4a02340c4f023a0c5402400c5902460c5d024c0c6202520c6502570c69025c0c6c02610c6f02\n" ;
        retString += "6b0c7402750c78027f0c7b02890c7d02930c7e029d0c7f02a70c7e02b00c7d02ba0c7b02c40c7802cd0c7502d60c7002df0c6b02e80c6402f10c5d02f90c5502\n" ;
        retString += "000d4e02060d47020c0d4002110d3902160d32021b0d2b021e0d2402220d1d02240d1602270d0e02290d07022a0d00022a0df9012b0df2012a0deb012a0de401\n" ;
        retString += "280ddd01260dd501230dcd01200dc5011c0dbd01170db401120dab010c0da201050d9901fe0c8f01f60c8501ee0c7b01e50c7001db0c6601d00c5a01c50c4f01\n" ;
        retString += "aa0b3400040000002d0103000400000006010100040000002d010400040000002701ffff04000000020101001c000000fb029cff000000000000900100000000\n" ;
        retString += "0440001254696d6573204e657720526f6d616e0000000000000000000000000000000000040000002d010500050000000902000000020d000000320a59006900010004006900ffffe90d340d20072d00040000002d010000030000000000}\\par}}}{\\insrsid5053302 \n" ;
        retString += "\\par }}\n" ;
        
        return retString;
        
    }
    
    /**
     * retrieves the xml document to be processed.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @return returns the xml document to be processed.
     */
    public String getXMLDocument() throws org.xml.sax.SAXException , javax.xml.parsers.ParserConfigurationException {
        try {
            return this.getInitElement().toString() ;
        } catch ( java.io.IOException ioe ) {
            throw new org.xml.sax.SAXException( "IOException thrown in getXMLDocument: " + ioe ) ;
        }
    }
    
    /**
     * Converts a String into an InputStream.
     * @param str to be converted into an InputStream.
     * @throws IOException
     * @return the InputStream from a String.
     */
    public static InputStream convert( String str ) throws java.io.IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OutputStreamWriter ow = new OutputStreamWriter( os, "utf-8" );
        
        ow.write( str , 0 , str.length() ) ;
        ow.flush() ;
        
        return new ByteArrayInputStream(os.toByteArray());
    }
    
    public String replaceAll( String s, String search, String replace ) {
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
