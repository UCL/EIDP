/*
 * ReportContentFunctions.java
 *
 * Created on October 15, 2004, 10:39 AM
 */

package com.eidp.Generator;

import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.NumberFormat;

/**
 *
 * @author Stephan Rusch
 * @version 3.0
 * @copyright Copyright (C) 2005 Stephan Rusch (schmutz@powl.name)
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

public class ReportContentFunctions {
    
    /** Creates a new instance of ReportContentFunctions */
    public ReportContentFunctions() {
    }
    
    public String getReportFileName( String value ) {
        String filename = "";
        if(value != null && !value.trim().equals("") ){
            String VISITNUM = value.substring( 1 , value.length() );
            String ADMSOURCE = value.substring( 0 , 1 );
            String strVisitNumLong = VISITNUM.substring( 0 , 8);
            String strVisitNumShort_S = VISITNUM.substring(VISITNUM.length() - 2, VISITNUM.length());
            String strVisitNumShort_A = VISITNUM.substring(VISITNUM.length() - 3, VISITNUM.length());
            filename = ADMSOURCE + strVisitNumLong + ".";
            if( ADMSOURCE.trim().equals("S") ){
                filename += strVisitNumShort_S;
            }else if( ADMSOURCE.trim().equals("A") ){
                filename += strVisitNumShort_A;
            }
        }
        return filename;
    }
    
    public String formatMonatTag( String tag ){
        String strReturn = "";
        
        try{
            tag = tag.trim();
            int zahl = Integer.parseInt( tag );
            if(zahl < 10)
                strReturn = "0" + tag;
            else
                strReturn = tag;
        }catch(java.lang.NumberFormatException e){
        }
        
        return strReturn;
    }
    
    public String formatDate( String value ) {
        
        if( value != null  && !value.equals("") ){
            if( value.indexOf( '.' ) != -1 && !value.equals("Datum") ){
                
                String tag="";
                String monat="";
                String jahr="";
                String toki="";
                int i = 1;
                StringTokenizer strto = new StringTokenizer(value,".");
                while( strto.hasMoreTokens() ){
                    
                    toki = strto.nextToken();
                    
                    switch (i) {
                        case 1:
                            tag = formatMonatTag(toki);
                            break;
                        case 2:
                            monat = formatMonatTag(toki);
                            break;
                        case 3:
                            jahr = toki;
                            break;
                        default: break;
                    }
                    i++;
                }
                
                if( tag.trim().equals("") && !monat.trim().equals("") && !jahr.trim().equals("") ){
                    value = monat + "." + jahr;
                }else
                    if( tag.trim().equals("") && monat.trim().equals("") && !jahr.trim().equals("") ){
                    value = jahr;
                    }else
                        if( !tag.trim().equals("") && !monat.trim().equals("") && !jahr.trim().equals("") ){
                    value = tag + "." + monat + "." + jahr;
                        }else
                            value = "";
            }
        }
        return value.trim();
    }
    
    public String convertDate( String value ) {
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim();
            if( value.indexOf( '-' ) != -1 && value.length() == 10 ) {
                
                String tag="";
                String monat="";
                String jahr="";
                
                StringTokenizer st = new StringTokenizer(value,"-");
                jahr = st.nextToken();
                monat = st.nextToken();
                tag = st.nextToken();
                
                retStr = tag + "." + monat + "." + jahr;
            }else{
                retStr = value ;
            }
        }
        return retStr;
    }
    
    public String convertValue( String value ) {
        
        if( value != null && !value.equals("") ) {
            if( value.indexOf('$') != -1 ){
                value = " " ;
            }else
                if( value.indexOf('.') != -1 ){
                value = value.trim();
                value = getFloatFormat( value.trim() ) ;
                }
        }
        
        return value.trim();
    }
    
    public String filterRange( String value ) {
        
        if( value != null && !value.equals("") ) {
            
            value = value.trim();
            
            if( value.equals("0-0") || value.equals("-1-0") ){
                value = " " ;
            }
            
            if( !(value.indexOf('-') == -1) ){
                StringTokenizer strto = new StringTokenizer(value,"-");
                String strLower = strto.nextToken();
                float lower = 0;
                String strUpper = strto.nextToken();
                float upper = 0;
                
                try{
                    lower = Float.parseFloat(strLower.replace(',','.'));
                    if(value.substring(0,1).equals("-")){
                        lower *= -1;
                        strLower = "-" + strLower;
                    }
                }catch(java.lang.NumberFormatException e){
                }
                
                try{
                    upper = Float.parseFloat(strUpper.replace(',','.'));
                    if( value.substring( strLower.length() + 1 , strLower.length() + 2 ).equals( "-" ) ){
                        upper *= -1;
                    }
                }catch(java.lang.NumberFormatException e){
                }
                
                if(lower > upper){
                    value = "< " + strLower;
                }
            }
        }else{
            value = " ";
        }
        
        return value;
    }
    
    public String getValueWhithoutFront_x( String value ) {
        
        if( value != null && !value.equals("") ) {
            if( value.length() > 2 ) {
                if( value.trim().substring( 0 , 2 ).equals( "x " ) ){
                    value = value.substring( 2 , value.length() ) ;
                }
            }else{
                value = getEmptyStringIfValueEquals_x( value ) ;
            }
        }
        return value;
    }
    
    public String returnNewIfValueIsLikeReference( String value, String newValue, String reference ) {
        
        if( value != null && !value.equals("") ) {
            if( value.indexOf( reference ) != -1 )
                value = newValue;
        }
        
        return value;
    }    
    
    public String convertCheckValue( String value ) {
        
        if( value.equals( "1" ) )
            value = "Positiv" ;
        else if( value.equals( "0" ) )
            value = "Negativ" ;
        
        return value;
    }
    
    public String makeNewLineAfterEachItem( String value ) {
        
        if( value != null && !value.equals("") ) {
            value = this.replaceAll( value, "<br>", "\\line " );
            value = this.replaceAll( value, " -- ", "\\line " );
        }
        
        return value;
    }
    
    public String convertNewLine( String value ) {
        
        if( value != null && !value.equals("") ) {
            value = this.replaceAll( value, "\r", "\\line " );
        }
        
        return value;
    }
    
    public String parseHTML( String value ) {
        
        if( value != null && !value.equals("") ) {
            value = this.replaceAll( value, "<br>", "\\line " );
            value = this.replaceAll( value, "<b>", "{\\b " );
            value = this.replaceAll( value, "<u>", "{\\ul " );
            value = this.replaceAll( value, "<i>", "{\\i " );
            value = this.replaceAll( value, "</b>", "}" );
            value = this.replaceAll( value, "</u>", "}" );
            value = this.replaceAll( value, "</i>", "}" );
        }
        
        return value;
    }
    
    public String getActualDate(){
        SimpleDateFormat fmt = new SimpleDateFormat() ;
        fmt.applyPattern( "dd.MM.yyyy" ) ;
        Calendar cal = new GregorianCalendar() ;
        return fmt.format(cal.getTime()) ;
    }
    
    public String cutSecondsFromTimestring( String value ){
        String retStr = "" ;
        if( value != null && value.length() >= 3 )
            retStr = value.substring( 0, value.length() - 3 ) ;
        return retStr;
    }    
    
    public String formatAuswaertigeBefunde( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.trim().equals("Auswärtige Untersuchungsbefunde:") && !retStr.equals("") ){
            retStr = "";
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            int i = 1 ;
            String toki = "";
            
            while( strto.hasMoreTokens() ){
                
                toki = strto.nextToken().trim();
                
                switch (i) {
                    case 1:
                        retStr += "Datum: " + toki + ".";
                        break;
                    case 2:
                        retStr += toki + ".";
                        break;
                    case 3:
                        retStr += toki;
                        break;
                    case 4:
                        retStr += " - Herkunft: " + toki;
                        break;
                    case 5:
                        retStr += " - Untersuchung: " + toki;
                        break;
                    case 6:
                        retStr += "\\line " + convertNewLine(toki);
                        break;
                    default: break;
                }
                i++;
            }
            
        }
        
        return retStr;
    }
    
    ///////////////////////////////////////////////
    // Therapievorschlag HIV
    public String formatDosierung( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.equals("") && !retStr.trim().equals("Therapievorschlag:") && !retStr.trim().equals("Basistherapie") ){
            retStr = "- ";
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            int i = 1 ;
            String toki = "";
            
            while( strto.hasMoreTokens() ){
                
                toki = strto.nextToken();
                
                switch (i) {
                    case 1:
                        retStr += toki;
                        break;
                    case 2:
                        retStr += " " + toki + " [";
                        break;
                    case 3:
                    case 4:
                    case 5:
                        retStr += getFloatFormat( toki ) + "-";
                        break;
                    case 6:
                        retStr += getFloatFormat( toki ) + "]";
                        break;
                    case 7:
                        retStr += ", " + getDosisIntervall( toki );
                        break;
                    default: break;
                }
                i++;
            }
            
        }
        
        return retStr;
    }
    
    public String getDosisIntervallForRheuma( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.equals("") && retStr.trim().indexOf("Intervall/") == -1 ){
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            String strIntervall = getDosisIntervall(strto.nextToken());
            String strBemerkung = "";
            try{
                strBemerkung = strto.nextToken();
            }catch(java.util.NoSuchElementException e){
            }
            retStr = strIntervall + strBemerkung;
        }
        
        return retStr;
    }
    
    // nicht zuordenbare Medikamente RHEUMA
    public String formatNZBasistherapieRheuma( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.equals("") && !retStr.trim().equals("Therapievorschlag:") && !retStr.trim().equals("Basistherapie") ){
            retStr = "";
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            int i = 1 ;
            String toki = "";
            
            while( strto.hasMoreTokens() ){
                
                toki = strto.nextToken();
                
                switch (i) {
                    case 1:
                        retStr += toki + "[";
                        break;
                    case 2:
                    case 3:
                        retStr += getFloatFormat( toki ) + "-";
                        break;
                    case 4:
                        retStr += getFloatFormat( toki ) + "]";
                    default: break;
                }
                i++;
            }
            
        }
        
        return retStr;
    }
    
    // Therapievorschlag RHEUMA
    public String formatDosierungRheuma( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.equals("") && !retStr.trim().equals("Rheumatherapie:") && !retStr.trim().equals("Basistherapie") ){
            retStr = "- ";
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            int i = 1 ;
            String toki = "";
            
            while( strto.hasMoreTokens() ){
                
                toki = strto.nextToken();
                
                switch (i) {
                    case 1:
                        retStr += toki;
                        break;
                    case 2:
                    case 3:
                        retStr += getFloatFormat( toki ) + "mg-";
                        break;
                    case 4:
                        retStr += getFloatFormat( toki ) + "mg";
                        break;
                    case 5:
                        retStr += ", " + getDosisIntervall( toki );
                        break;
                    default: break;
                }
                i++;
            }
            
        }
        
        return retStr;
    }
    
    public String getFloatFormat( String str ) {
        String erg = "" ;
        if( str != null  && !str.equals("") ){
            if(str.indexOf('.') != -1){
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(2);
                nf.setGroupingUsed(false);
                try{
                    erg = ((String)(nf.format(nf.parseObject(str)))).replace('.', ',') ;
                }catch(java.text.ParseException e){
                    erg = str ;
                }
            }else{
                if( str.trim().equals("") )
                    erg = "0" ;
                else
                    erg = str ;
            }
        }else{
            erg = "0" ;
        }
        return erg;
    }
    
    public String getFloatFormatForDosis( String str ) {
        String erg = "" ;
        if( str != null  && !str.equals("") ){
            if(str.indexOf('.') != -1){
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(2);
                nf.setGroupingUsed(false);
                try{
                    erg = ((String)(nf.format(nf.parseObject(str)))).replace('.', ',') ;
                }catch(java.text.ParseException e){
                    erg = str ;
                }
            }else{
                if( str.trim().equals("") )
                    erg = "-" ;
                else
                    erg = str ;
            }
        }else{
            erg = "-" ;
        }
        if( erg.trim().equals("0") )
            erg = "-" ;
        return erg;
    }
    
    public String getEmptyStringIfValueEquals_x( String str ) {
        if( str != null  && !str.equals("") ){
            str = str.trim() ;
            if( str.equals( "x" ) ){
                str = "" ;
            }
        }
        return str ;
    }
    
    public String getEmptyStringIfValueEquals_pointpoint( String str ) {
        if( str != null  && !str.equals("") ){
            str = str.trim() ;
            if( str.equals( ".." ) ){
                str = "" ;
            }
        }
        return str ;
    }
    
    public String getJaNeinValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("1") )
                retStr = "ja" ;
            else if( value.equals("0") )
                retStr = "nein" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getUntersuchungsbefundBezeichnung( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("augenbindehaut") )
                retStr = "Augenbindehaut" ;
            else if( value.equals("rachen") )
                retStr = "Rachen" ;
            else if( value.equals("hauttyp") )
                retStr = "Hauttyp" ;
            else if( value.equals("zunge") )
                retStr = "Zunge" ;
            else if( value.equals("lymphknoten") )
                retStr = "Lymphknoten" ;
            else if( value.equals("herz") )
                retStr = "Herz" ;
            else if( value.equals("lunge") )
                retStr = "Lunge" ;
            else if( value.equals("abdomen") )
                retStr = "Abdomen" ;
            else if( value.equals("leber") )
                retStr = "Leber" ;
            else if( value.equals("milz") )
                retStr = "Milz" ;
            else if( value.equals("nieren") )
                retStr = "Nieren" ;
            else if( value.equals("genital") )
                retStr = "Genitalien" ;
            else if( value.equals("bewegung") )
                retStr = "Bewegungsapparat" ;
            else if( value.equals("asr_re") )
                retStr = "ASR-rechts" ;
            else if( value.equals("asr_li") )
                retStr = "ASR-links" ;
            else if( value.equals("psr_re") )
                retStr = "PSR-rechts" ;
            else if( value.equals("psr_li") )
                retStr = "PSR-links" ;
            else if( value.equals("palla_re") )
                retStr = "Pallaesthesie-rechts" ;
            else if( value.equals("palla_li") )
                retStr = "Pallaesthesie-links" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getDosisIntervall( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("d") )
                retStr = "täglich" ;
            else if( value.equals("w1") )
                retStr = "1 x wöchentl." ;
            else if( value.equals("w2") )
                retStr = "2 x wöchentl." ;
            else if( value.equals("w3") )
                retStr = "3 x wöchentl." ;
            else if( value.equals("2w") )
                retStr = "14 tägig" ;
            else if( value.equals("m1") )
                retStr = "1 x monatl." ;
            else if( value.equals("m2") )
                retStr = "2 x monatl." ;
            else if( value.equals("m3") )
                retStr = "3 x monatl." ;
            else if( value.equals("3w") )
                retStr = "3-wöchentl." ;
            else if( value.equals("6w") )
                retStr = "6-wöchentl." ;
            else if( value.equals("8w") )
                retStr = "8-wöchentl." ;
            else if( value.equals("12w") )
                retStr = "12-wöchentl." ;
            else if( value.equals("y1") )
                retStr = "1 x jährl." ;
            else if( value.equals("bb") )
                retStr = "bei Bedarf" ;
            else if( value.equals("o") )
                retStr = "Anderes (unter Absprache mit dem Patienten)" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    ///////////////////////////////////////////////
    
    public String replaceAll( String s, String search, String replace ) {
        StringBuffer s2 = new StringBuffer();
        int i = 0, j = 0 ;
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
    
    // ESID Value-Translation-Functions ////////////////////////////////////////////////////
    
    public String filterEsidRange( String value ) {
        
        if( value != null && !value.equals("") ) {
            
            value = value.trim();
            
            if( value.equals("0-0") || value.equals("-1-0")  || value.equals("-1--1") || value.equals("-1-") || value.equals("-")){
                value = "-" ;
            }else if( !(value.indexOf('-') == -1) ){
                if(value.charAt(value.length()-1) == '-'){
                    value += "0";
                }
                if(value.charAt(0) == '-'){
                    value = "0" + value;
                }
                StringTokenizer strto = new StringTokenizer(value,"-");
                String strLower = strto.nextToken();
                float lower = 0;
                String strUpper = strto.nextToken();
                float upper = 0;
                
                try{
                    lower = Float.parseFloat(strLower.replace(',','.'));
                    if(value.substring(0,1).equals("-")){
                        lower *= -1;
                        strLower = "-" + strLower;
                    }
                }catch(java.lang.NumberFormatException e){
                }
                
                try{
                    upper = Float.parseFloat(strUpper.replace(',','.'));
                    if( value.substring( strLower.length() + 1 , strLower.length() + 2 ).equals( "-" ) ){
                        upper *= -1;
                    }
                }catch(java.lang.NumberFormatException e){
                }
                
                if(lower > upper){
                    value = "< " + strLower;
                }
            }
        }else{
            value = "-";
        }
        
        return value;
    }
    
    public String formatESIDTherapieEmpfehlung( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.equals("") && !retStr.equals("Therapieempfehlung:") ){
            retStr = "";
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            int i = 1 ;
            String toki = "";
            
            while( strto.hasMoreTokens() ){
                
                toki = strto.nextToken().trim();
                
                switch (i) {
                    case 1:
                        retStr += toki;
                        break;
                    case 2:
                        if( toki.trim().length() > 0 )
                            retStr += " (" + toki + "):";
                        break;
                    case 3:
                        if( toki.trim().length() > 0 )
                            retStr += " " + getFloatFormat(toki) ;
                        break;
                    case 4:
                        if( toki.trim().length() > 0 )
                            retStr += " " + toki ;
                        break;
                    case 5:
                        if( toki.trim().length() > 0 )
                            retStr += " " + getESIDTherapieRoute(toki) ;
                        break;
                    case 6:
                        if( toki.trim().length() > 0 )
                            retStr += ", " + getFloatFormat(toki);
                        break;
                    case 7:
                        if( toki.trim().length() > 0 )
                            retStr += " pro " + getESIDDosisIntervall(toki);
                        break;
                    default: break;
                }
                i++;
            }
            
        }
        
        return retStr;
    }
    
    public String getESIDDosisIntervall( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("d") )
                retStr = "Tag" ;
            else if( value.equals("w") )
                retStr = "Woche" ;
            else if( value.equals("m") )
                retStr = "Monat" ;
            else if( value.equals("y") )
                retStr = "Jahr" ;
        }
        return retStr;
    }
    
    public String getESIDTherapieRoute( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("o") )
                retStr = "oral" ;
            else if( value.equals("sc") )
                retStr = "s.c." ;
            else if( value.equals("iv") )
                retStr = "i.v." ;
            else if( value.equals("im") )
                retStr = "i.m." ;
            else if( value.equals("in") )
                retStr = "inhalativ" ;
            else if( value.equals("t") )
                retStr = "topisch" ;
            else if( value.equals("x") )
                retStr = "unbekannt" ;
        }
        return retStr;
    }
    
    public String getInfectionStatusValue( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("c") )
                retStr = "chronisch" ;
            else if( value.equals("a") )
                retStr = "akut" ;
            else if( value.equals("r") )
                retStr = "behoben" ;
            else if( value.equals("x") )
                retStr = "unbekannt" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getAutoImmunDiseaseValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("un") )
                retStr = "unbekannt" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getAutoImmunDiseaseTitreValue( String value ){
        String retStr = "" ;
        if( value != null && value.length() > 0 ){
            value = value.trim() ;
            if( value.equals("0") )
                retStr = "unbekannt" ;
            else if( value.equals("8") || value.equals("16") || value.equals("32") || value.equals("64") || value.equals("128") || value.equals("256") || value.equals("512") || value.equals("1024") || value.equals("2048") || value.equals("4096") )
                retStr = "1:" + value ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getAutoImmunDiseaseNormalTitreValue( String value ){
        String retStr = "" ;
        if( value != null && value.length() > 0 ){
            value = value.trim() ;
            if( value.equals("0") )
                retStr = "unbekannt" ;
            else if( value.equals("8") || value.equals("16") || value.equals("32") || value.equals("64") || value.equals("128") || value.equals("256") || value.equals("512") || value.equals("1024") || value.equals("2048") || value.equals("4096") )
                retStr = "< 1:" + value ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getNeoplasmsStatusValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("o") )
                retStr = "ongoing" ;
            else if( value.equals("r") )
                retStr = "remission" ;
            else if( value.equals("x") )
                retStr = "unbekannt" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getPatientHistoryKindOfTreatmentValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("1") )
                retStr = "surgery" ;
            else if( value.equals("2") )
                retStr = "oral drugs" ;
            else if( value.equals("3") )
                retStr = "ir drugs" ;
            else if( value.equals("4") )
                retStr = "hospitalization" ;
            else if( value.equals("5") )
                retStr = "none" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getPatientHistoryStatusValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("1") )
                retStr = "anhaltend" ;
            else if( value.equals("2") )
                retStr = "behoben" ;
            else if( value.equals("3") )
                retStr = "unbekannt" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getQoLDaysMissedValue( String value ){
        String retStr = "" ;
        
        if( value != null && !value.equals("") && !value.equals("-1") ){
            value = value.trim() ;
            if( value.indexOf("Zeit") != -1 || value.indexOf("Schule") != -1 ){
                retStr = value;
            }else if( value.equals("0") )
                retStr = "keine" ;
            else if( value.equals("1") )
                retStr = "< 7 Tage" ;
            else if( value.equals("2") )
                retStr = "1-2 Wochen" ;
            else if( value.equals("3") )
                retStr = "2-3 Wochen" ;
            else if( value.equals("94") )
                retStr = "3-4 Wochen" ;
            else if( value.equals("4") )
                retStr = "4-5 Wochen" ;
            else if( value.equals("5") )
                retStr = "5-6 Wochen" ;
            else if( value.equals("6") )
                retStr = "6-7 Wochen" ;
            else if( value.equals("7") )
                retStr = "7-8 Wochen" ;
            else if( value.equals("8") )
                retStr = "8-9 Wochen" ;
            else if( value.equals("9") )
                retStr = "9-10 Wochen" ;
            else if( value.equals("10") )
                retStr = "10-11 Wochen" ;
            else if( value.equals("11") )
                retStr = "11-12 Wochen" ;
            else if( value.equals("12") )
                retStr = "> 12 Wochen" ;
            else if( value.equals("15") )
                retStr = "keine Angabe" ;
            else if( value.equals("16") )
                retStr = "arbeitslos" ;
            else if( value.equals("17") )
                retStr = "in Rente" ;
        }
        return retStr;
    }
    
    public String getYesNoUnknownValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("y") )
                retStr = "ja" ;
            else if( value.equals("n") )
                retStr = "nein" ;
            else if( value.equals("x") )
                retStr = "unbekannt" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getKarnowskyValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("1") || value.equals("2") || value.equals("3") || value.equals("4") || value.equals("5") || value.equals("6") || value.equals("7") || value.equals("8") || value.equals("9") || value.equals("10") )
                retStr = value + "0%" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getUltrasoundLnodesValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("p") )
                retStr = "present" ;
            else if( value.equals("n") )
                retStr = "none" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getPreventiveIntervalValue( String value ){
        String retStr = "" ;
        if( value != null && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("m") )
                retStr = "Monat" ;
            else if( value.equals("w") )
                retStr = "Woche" ;
            else if( value.equals("y") )
                retStr = "Jahr" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    ///////////////////////////////////// TRANSPLANT //////////////////////////////////////////
    
    public String getAddNTXValue(String value){
        String retString = value;
        if( value != null  && !value.equals("") ){
            try{
                int test = Integer.parseInt(value.trim());
                if( test > 0 )
                    retString = "ja" ;
                else
                    retString = "nein" ;
            }catch(java.lang.NumberFormatException e){
                retString = "nein" ;
            }
        }else{
            retString = "nein" ;
        }
        return retString;
    }
    
    public String formatTransplantMirror( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.equals("") && !retStr.equals("Spiegel:") ){
            retStr = "";
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            int i = 1 ;
            String toki = "";
            
            while( strto.hasMoreTokens() ){
                
                toki = strto.nextToken().trim();
                
                switch (i) {
                    case 1:
                        retStr += toki + " ";
                        break;
                    case 2:
                        retStr += getFloatFormat(toki) ;
                        break;
                    case 3:
                        retStr += " (" + formatMonatTag(toki) ;
                        break;
                    case 4:
                        retStr += "." + formatMonatTag(toki) ;
                        break;
                    case 5:
                        retStr += "." + toki + ")";
                        break;
                    default: break;
                }
                i++;
            }
            
        }
        
        return retStr;
    }
    
    public String formatTransplantDiagnosenText( String value ){
        String retStr = value ;
        
        if( retStr != null && !retStr.equals("") && !retStr.equals(" ") ){
            retStr = "";
            StringTokenizer strto = new StringTokenizer( value, "@" ) ;
            int i = 1 ;
            String toki = "";
            
            while( strto.hasMoreTokens() ){
                
                toki = strto.nextToken().trim();
                
                switch (i) {
                    case 1:
                        retStr += "\\b \\bullet  " + toki + " ";
                        break;
                    case 2:
                        retStr += "\\line }{ \\fs18\\cf1\\lang255\\langfe1031\\dbch\\af0\\langnp255\\insrsid5274696\\charrsid5274696   " + toki + " ";
                        break;
                        
                    default: break;
                }
                i++;
            }
            
        }
        
        return retStr;
    }
    
    public String getTransplantDosisIntervall( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("1") )
                retStr = "täglich" ;
            else if( value.equals("2") )
                retStr = "1 x wöchentl." ;
            else if( value.equals("3") )
                retStr = "2 x wöchentl." ;
            else if( value.equals("14") )
                retStr = "3 x wöchentl." ;
            else if( value.equals("4") )
                retStr = "14 tägig" ;
            else if( value.equals("5") )
                retStr = "1 x monatl." ;
            else if( value.equals("6") )
                retStr = "2 x monatl." ;
            else if( value.equals("7") )
                retStr = "3 x monatl." ;
            else if( value.equals("8") )
                retStr = "6-wöchentl." ;
            else if( value.equals("9") )
                retStr = "8-wöchentl." ;
            else if( value.equals("10") )
                retStr = "12-wöchentl." ;
            else if( value.equals("11") )
                retStr = "1 x jährl." ;
            else if( value.equals("13") )
                retStr = "bei Bedarf" ;
            else if( value.equals("12") )
                retStr = "Anderes (unter Absprache mit dem Patienten)" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String getDefiniteDiagnosis( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            if( value.equals("-1") )
                retStr = "" ;
            else if( value.equals("1") )
                retStr = "sicher" ;
            else if( value.equals("2") )
                retStr = "Verdacht auf" ;
            else if( value.equals("3") )
                retStr = "anamnestisch" ;
            else if( value.equals("4") )
                retStr = "unbekannt" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String parseBoneDensity( String value ){
        String retStr = "" ;
        if( value != null  && !value.equals("") ){
            value = value.trim() ;
            StringTokenizer strto = new StringTokenizer(value,":");
            value = strto.nextToken().trim();
            String finding = ":" ;
            try{
                finding += strto.nextToken();
            }catch(java.util.NoSuchElementException e){
                
            }
            if( value.equals("1") )
                retStr = "DEXA" + finding;
            else if( value.equals("2") )
                retStr = "Quant. CT." + finding;
            else if( value.equals("3") )
                retStr = "Sonstige" + finding;
            else if( value.equals("4") )
                retStr = "unbekannt" + finding;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    // RFH Intranet : David
   public String toUppercase(String instring) {
       String retStr = new String();
       retStr = instring.toUpperCase();
       return retStr;
   }
   
   public String toLowercase(String instring) {
       String retStr = new String();
       retStr = instring.toLowerCase();
       return retStr;
   }
   
   public String removeTrailingZero(String instring) {
       String retStr = new String();
       retStr = instring.replaceFirst("\\.0 ", "");
       return retStr;
   }
   
   public String formatHospitalNumber(String hospno) {
      String retStr = new String();
      retStr = hospno.substring(4);
      return retStr;
   }
}
