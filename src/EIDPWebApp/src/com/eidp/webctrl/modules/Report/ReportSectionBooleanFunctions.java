/*
 * SectionBooleanFunctions.java
 *
 * Created on October 15, 2004, 8:55 AM
 */

package com.eidp.webctrl.modules.Report;

import java.util.StringTokenizer;
/**
 *
 * @author  rusch
 */
public class ReportSectionBooleanFunctions {
    
    /** Creates a new instance of SectionBooleanFunctions */
    public ReportSectionBooleanFunctions() {
    }
    
    // erstmals bei Transplant implementiert
    public boolean getBooleanValueForSexOfPhysician( String refValue, String parameter ){
        boolean erg = false ;
        
        if( refValue != null ){
            refValue = refValue.trim() ;
        }else{
            refValue = "" ;
        }
        
        if( refValue.equals( "" ) )
            refValue = "andere" ;
        
        if( refValue.indexOf( parameter ) != -1 )
            erg = true ;
        
        if( refValue.indexOf( "Frau" ) == -1 && refValue.indexOf( "Herr" ) == -1 && parameter.equals("andere") )
            erg = true ;
        
        return erg;
    }
    
    public boolean getBooleanValueForSexOfPatient( String refValue, String parameter ){
        StringTokenizer strto = new StringTokenizer( parameter , ";" ) ;
        boolean erg = false ;
        
        if( refValue != null ){
            refValue = refValue.trim() ;
        }else{
            refValue = "" ;
        }
        
        if( refValue.equals( "" ) )
            refValue = "M" ;
        
        while( strto.hasMoreTokens() ){
            if( strto.nextToken().equals( refValue ) ){
                erg = true ;
                break ;
            }
        }
        
        return erg;
    }
    
    public boolean getBooleanValueForPhysicianLetterToPatientAndHomePhysician( String refValue ){
        
        if( refValue != null )
            refValue = refValue.trim() ;
        
        if( refValue.equals("zum Hausarzt und nach Hause") )
            return true ;
        else
            return false;
    }
    
    public boolean getBooleanValueForPhysicianLetterToHomePhysician( String refValue ){
        if( refValue != null )
            refValue = refValue.trim() ;
        else
            refValue = "" ;
        
        if( refValue.equals("") || refValue.equals("zum Hausarzt") || refValue.equals("x") || refValue.equals("zum Hausarzt und nach Hause") )
            return true ;
        else
            return false;
    }
    
    public boolean getBooleanValueForPhysicianLetterToPatient( String refValue ){
        if( refValue != null )
            refValue = refValue.trim() ;
        
        if( refValue.equals("nach Hause") || refValue.equals( "yes" )  )
            return true ;
        else
            return false;
    }
    
    public boolean getBooleanValueForVisitDate( String refValue ){
        if( refValue != null )
            refValue = refValue.trim() ;
        
        if( refValue.equals("") )
            return false ;
        else
            return true;
    }
    
    public boolean getFalseBooleanValueForVisitDate( String refValue ){
        if( refValue != null )
            refValue = refValue.trim() ;
        
        if( refValue.equals("") )
            return true ;
        else
            return false;
    }
    
    public boolean getBooleanValueForDatasetIsNotEmpty( String refValue ){
        if( refValue != null )
            refValue = refValue.trim() ;
        else
            refValue = "" ;
        
        if( refValue.equals("") || refValue.equals("0") || refValue.equals("0.0") || refValue.equals("00:00:00"))
            return false ;
        else
            return true;
    }
    
    public boolean getBooleanValueForDatasetIsEmpty( String refValue ){
        if( refValue != null )
            refValue = refValue.trim() ;
        else
            refValue = "" ;
        
        if( refValue.equals("") || refValue.equals("0") || refValue.equals("0.0") || refValue.equals("00:00:00"))
            return true ;
        else
            return false;
    }
    
    // erstmals bei Hepatitis implementiert
    public boolean getBooleanValueForRefValueIsEqualToParameter( String refValue, String parameter ){
        boolean erg = false ;
        
        if( refValue != null ){
            refValue = refValue.trim() ;
        }else{
            refValue = "" ;
        }
        
        if( refValue.equals( parameter ) )
            erg = true ;
        
        return erg;
    }
}
