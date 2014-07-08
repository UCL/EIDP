/*
 * Hilf.java
 *
 * Created on June 16, 2004, 2:52 PM
 */

package com.eidp.webctrl.modules;

import java.util.StringTokenizer;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author  rusch
 */
public class Hilf {
    
    /** Creates a new instance of Hilf */
    public Hilf() {
    }
    
    public String getDateForTimestamp( long sec ){
        Date date = new Date(sec);
        SimpleDateFormat fmt = new SimpleDateFormat() ;
        fmt.applyPattern( "dd.MM.yyyy" ) ;
        Calendar cal = new GregorianCalendar() ;
        return fmt.format(date) ;
    }
    
    public String getActualDate(){
        SimpleDateFormat fmt = new SimpleDateFormat() ;
        fmt.applyPattern( "dd.MM.yyyy" ) ;
        Calendar cal = new GregorianCalendar() ;
        return fmt.format(cal.getTime()) ;
    }
    
    public int getActualYear(){
        SimpleDateFormat fmt = new SimpleDateFormat() ;
        fmt.applyPattern( "yyyy" ) ;
        Calendar cal = new GregorianCalendar() ;
        return Integer.parseInt(fmt.format(cal.getTime())) ;
    }
    
    public String replaceEmptyValue( String s){
        if( s.equals("") || s == null )
            return "0" ;
        else
            return s ;
    }
    
    public String getEmptyStringForNull( String s){
        if( s == null )
            return "" ;
        else
            return s.trim() ;
    }
    
    public String getCheck( String s){
        if(s.equals("1") || s.equals("ja"))
            return "checked";
        else
            return "";
    }
    
    public String formatDatum( String tag, String monat, String jahr){
        String strDatum = "";
        tag = formatMonatTag(tag);
        monat = formatMonatTag(monat);
        
        if( tag.trim().equals("") && !monat.trim().equals("") && !jahr.trim().equals("") ){
            strDatum = monat + "." + jahr;
        }else
            if( tag.trim().equals("") && monat.trim().equals("") && !jahr.trim().equals("") ){
            strDatum = jahr;
            }else
                if( !tag.trim().equals("") && !monat.trim().equals("") && !jahr.trim().equals("") ){
            strDatum = tag + "." + monat + "." + jahr;
                }else
                    strDatum = "";
        return strDatum;
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
    
    public String getRadio( String s, String comparestring ) {
        String strReturn = "";
        
        if(s.equals(comparestring))
            strReturn = "checked";
        
        return strReturn;
    }
    
    public String getOptionSelectedValue( String s, String comparestring ) {
        String strReturn = "";
        
        if(s.equals(comparestring))
            strReturn = "selected";
        
        return strReturn;
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
    
    public String getRange( String lower, String upper ) {
        String erg = "";
        
        if( lower.equals("&nbsp;") && upper.equals("&nbsp;") )
            erg = "&nbsp;";
        else if( lower.equals("&nbsp;") )
            erg = upper;
        else if( upper.equals("&nbsp;") )
            erg = lower;
        else if( !lower.equals("&nbsp;") && !upper.equals("&nbsp;") )
            erg = lower + " - " + upper;
        else if( lower.equals(upper) )
            erg = "&nbsp;";
        
        return erg;
    }
    
    //    public String getStringRange( String lower, String upper ) {
    //        String erg = "&nbsp;";
    //
    //        if( (lower.equals("") && upper.equals("")) || (lower.equals("0") && upper.equals("0")) || (lower.equals("&nbsp;") && upper.equals("&nbsp;")))
    //            erg = "&nbsp;";
    //        else if( lower.equals("") )
    //            erg = upper;
    //        else if( upper.equals("") || upper.equals("0") )
    //            erg = lower;
    //        else if( !lower.equals("") && !upper.equals("") && !upper.equals("0") )
    //            erg = lower + " - " + upper;
    //        else if( lower.equals(upper) )
    //            erg = "&nbsp;";
    //
    //        return erg;
    //    }
    
    public String getStringRange( String lower, String upper ) {
        String erg = "&nbsp;";
        lower = this.cutEndString( lower, ".0" ) ;
        upper = this.cutEndString( upper, ".0" ) ;
        
        if( (lower.equals("") && upper.equals("")) || (lower.equals("0") && upper.equals("0")) || (lower.equals("&nbsp;") && upper.equals("&nbsp;")))
            erg = "&nbsp;";
        else if( lower.equals("") )
            erg = upper.replace('.', ',');
        else if( upper.equals("") || upper.equals("0") )
            erg = "&lt; " + lower.replace('.', ',');
        else if( !lower.equals("") && !upper.equals("") && !upper.equals("0") ){
            erg = lower.replace('.', ',') + " - " + upper.replace('.', ',');
        } else if( lower.equals(upper) )
            erg = "&nbsp;";
        
        if( erg.trim().equals("-1") || erg.trim().equals("&lt; -1"))
            erg = "&nbsp;";
        
        return erg;
    }
    
    public String getMin( Vector vec , String key ) {
        String retstr = "";
        float fl = 0;
        int j = 0;
        for( int i = 0 ; i < vec.size() ; i++ ){
            String strLaborID = (String)((HashMap)vec.get(i)).get( "bezeichnung_kurz" ) ;
            if( strLaborID.equals( "kp-cd4" ) ){
                String value = (String) ( (HashMap)vec.get(i) ).get(key);
                String tempValue = this.replaceAll( value , "," , "." );
                try{
                    float test = Float.parseFloat(tempValue);
                    if(test < fl || j == 0){
                        retstr = value;
                        fl = test;
                        j++;
                    }
                }catch(java.lang.NumberFormatException e){
                }
            }
        }
        
        return retstr;
    }
    
    public String convertDate( String datum ) {
        String tag,monat,jahr,retstr;
        retstr = datum.trim();
        
        if( !retstr.equals("") ){
            StringTokenizer st = new StringTokenizer(datum,"-");
            
            jahr = st.nextToken();
            monat = st.nextToken();
            tag = st.nextToken();
            
            retstr = tag + "." + monat + "." + jahr;
        }
        return retstr;
    }
    
    public int getResultSetLength( ResultSet rs ) {
        
        int umlauf = 0 ;
        
        try{
            while(rs.next()){
                umlauf++ ;
            }
        }catch(java.sql.SQLException e){
        }
        
        return umlauf ;
    }
    
    public int getMaxVectorsize( Vector[] v ) {
        
        int max = 0 ;
        int vectorAnzahl = 0 ;
        
        try{
            for( int i = 0; i < v.length ; i++ ){
                if( max < v[i].size() ){
                    max = v[i].size() ;
                }
            }
        }catch(java.lang.NullPointerException e){
        }
        
        return max ;
    }
    
    public int getMaxVectorID( Vector[] v ) {
        
        int max = 0 ;
        int id = 0 ;
        
        try{
            for( int i = 0; i < v.length ; i++ ){
                if( max < v[i].size() ){
                    max = v[i].size() ;
                    id = i ;
                }
            }
        }catch(java.lang.NullPointerException e){
        }
        
        return id ;
    }
    
    public int getVectorNotEmptyLength( Vector[] v ) {
        
        int vectorAnzahl = 0 ;
        
        for( int i = 0; i < v.length ; i++ )
            if( v[i].size() != 0 )
                vectorAnzahl++ ;
        
        return vectorAnzahl ;
    }
    
    public String suchenUndKuerzen( String str, char ch ) {
        String vorKomma = "" ;
        String nachKomma = "" ;
        String erg = "" ;
        int testNachkomma = 0 ;
        if( str != null && !str.equals( "" ) ){
            if( str.indexOf( ch ) >= 0 ){
                vorKomma = str.substring( 0, str.indexOf( ch ) ) ;
                nachKomma = str.substring( str.indexOf( ch ) + 1, str.length()  ) ;
                testNachkomma = Integer.parseInt( nachKomma ) ;
                if ( testNachkomma == 0 )
                    nachKomma = "" ;
                else
                    vorKomma += "," ;
            }
        }
        erg = vorKomma + nachKomma ;
        return erg ;
    }
    
    public String formatFloat( String str , char kommaalt , char kommaneu , int anzahl ) {
        String vorKomma = "" ;
        String nachKomma = "" ;
        int testNachkomma = 0 ;
        if( str != null && !str.trim().equals( "" ) ){
            if( str.indexOf( kommaalt ) >= 0 && anzahl > 0 ){
                vorKomma = str.substring( 0, str.indexOf( kommaalt ) ) ;
                if( anzahl >= str.length() - str.indexOf( kommaalt ) )
                    anzahl = str.length() - str.indexOf( kommaalt ) - 1;
                nachKomma = str.substring( str.indexOf( kommaalt ) + 1, str.indexOf( kommaalt ) + 1 + anzahl  ) ;
                testNachkomma = Integer.parseInt( nachKomma ) ;
                if ( testNachkomma == 0 )
                    nachKomma = "" ;
                else
                    vorKomma += kommaneu ;
                if( anzahl > 0 )
                    str = vorKomma + nachKomma ;
            }
        }
        return str ;
    }
    
    /** Returns "newee" String,
     *  if "str" equals "arg". */
    public String getNewStringForArg( String str, String arg, String newee ) {
        try{
            if( str.equals( arg ) )
                str = newee ;
        }catch(java.lang.NullPointerException e){
            str = newee ;
        }
        return str ;
    }
    
    /** Cuts the String "cut" from the end of the String "str",
     *  if "cut" exists. */
    public String cutEndString(String str, String cut){
        if( str.substring( str.length() - cut.length(), str.length() ).equals( cut ) ){
            str = str.substring( 0, str.length() - cut.length() ) ;
        }
        return str ;
    }
    
    public String getEmptyCell(String str){
        if( str == null  ){
            str = "&nbsp;" ;
        }else if( str.equals("x") || str.equals("") ){
            str = "&nbsp;" ;
        }
        return str ;
    }
    
    public String getFFbHScore(HashMap hmFFbH){
        
        String strErg = "" ;
        
        if( hmFFbH != null ){
            
            int intErg = 0 ;
            
            int sum = getFFbH_p_Sum( hmFFbH ) ;
            
            if( sum == 1200 ){
                strErg = "" ;
            }else{
                
                int anzGueltigeAntworten = 12 - ( sum / 100 ) ;
                sum -= ( sum / 100 ) * 100 ;
                intErg = ( sum * 100 ) / ( 2 * anzGueltigeAntworten ) ;
                
                strErg = String.valueOf( intErg ) ;
                
            }
        }
        
        return strErg ;
        
    }
    
    
    
    public String getHaqScore(HashMap hmFFbH){
        
        String strErg = "" ;
        
        if( hmFFbH != null ){
            
            double ffbhErg = 0 ;
            double haqErg = 0 ;
            NumberFormat formatter = new DecimalFormat( "0.00" );
            
            int sum = getFFbH_p_Sum( hmFFbH ) ;
            
            if( sum == 1200 ){
                strErg = "" ;
            }else{
                
                int anzGueltigeAntworten = 12 - ( sum / 100 ) ;
                sum -= ( sum / 100 ) * 100 ;
                ffbhErg = ( sum * 100 ) / ( 2 * anzGueltigeAntworten ) ;
                
                if( ffbhErg < 4 )
                    haqErg = 3 ;
                else
                    if( ffbhErg == 100 )
                        haqErg = 0.51 ;
                    else{
                    haqErg = 3.31 - ( 0.028 * ffbhErg ) ;
                    }
                
                strErg = formatter.format( haqErg ) ;
                
            }
        }
        
        return strErg ;
        
    }
    
    public int getFFbH_p_Sum(HashMap FFbH){
        int sum = 0 ;
        sum += get100ForNothing( (String) (FFbH.get( "TELEFON" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "BROT" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "BETTAUFSTEHEN" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "SCHREIBEN" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "ZUDREHEN" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "TRAGEN" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "KORPERWASCHEN" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "BODENAUFHEBEN" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "STRUEMPFE" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "WINTERMANTEL" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "LAUFEN" ) ) ) ;
        sum += get100ForNothing( (String) (FFbH.get( "VMBENUTZEN" ) ) ) ;
        
        return sum ;
    }
    
    public int get100ForNothing(String strNullOrEmpty){
        int test = 0 ;
        try{
            test = Integer.parseInt( strNullOrEmpty ) ;
        }catch( java.lang.NumberFormatException e ){
            test = 100 ;
        }
        return test ;
    }
    
    
    public String matchDiagKat(String value){
        String retStr = "" ;
        if( value != null ){
            value = value.trim() ;
            if( value.equals("1") )
                retStr = "haupt" ;
            else if( value.equals("2") )
                retStr = "neben" ;
            else if( value.equals("3") )
                retStr = "aktuell" ;
            else
                retStr = value ;
        }
        return retStr;
    }
    
    public String matchDefiniteDiag(String value){
        String retStr = "" ;
        if( value != null ){
            value = value.trim() ;
            if( value.equals("1") )
                retStr = "sicher" ;
            else if( value.equals("-1") )
                retStr = "" ;
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
    
    public String matchJaNein( String s ){
        String retStr = "" ;
        if( s != null ){
            if( s.equals("1") )
                retStr = "ja";
            else if( s.equals("0") )
                retStr = "nein";
        }
        return retStr;
    }
    
    public String getUpperString(String str){
        String retString = "";
        Character c = new Character('a');
        for(int i=0 ; i<str.length() ; i++){
            retString += c.toUpperCase(str.charAt(i));
        }
        return retString;
    }
    
    public String decodeRTF(String encodedString){
        String decString = "";
        if(encodedString != null && !encodedString.trim().equals("")){
            byte[] decodedByteArray = javax.xml.bind.DatatypeConverter.parseBase64Binary(encodedString);
            decString = new String( decodedByteArray, 0 , decodedByteArray.length) ;
            decString = decString.substring(0, decString.lastIndexOf('}') + 1).replaceAll("\\n", "").replaceAll("\\r", " ");
        }
        return decString;
    }
}
