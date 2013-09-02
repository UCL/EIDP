/*
 * SharedMethods.java
 *
 * Created on 8. Juli 2004, 11:08
 */

package com.eidp.webctrl;

import java.util.Vector ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.Iterator ;
import java.io.PrintWriter ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.w3c.dom.NodeList ;

import com.eidp.UserScopeObject.UserScopeObject ;
import java.util.Date;

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

public class SharedMethods {
    
    /** Creates a new instance of SharedMethods */
    public SharedMethods() {
    }
    // f�r ReportWrapper
    protected void PreLoad( NodeList preLoad , String formName , UserScopeObject uso ) throws java.rmi.RemoteException, java.sql.SQLException , org.xml.sax.SAXException , java.io.IOException {
        uso.preLoadFlag = true ;
        String dataset = "" ;
        try {
            dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , preLoad )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException ae ) {
            uso.preLoadFlag = false ;
        }
        if ( uso.preLoadFlag == true && ! dataset.equals( "session" ) ) {
            String method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , preLoad )).get( 0 ) ;
            HashMap paramMap = new HashMap() ;
            // references
            Vector referenceVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "reference" , preLoad ) ;
            for ( int i = 0 ; i < referenceVector.size() ; i++ ) {
                String referenceType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                if ( referenceType.equals( "session" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    if ( ref.equals( "userID" ) ) {
                        paramMap.put( id , (String)uso.userID ) ;
                    } else if ( ref.equals( "userLogin" ) ) {
                        paramMap.put( id , (String)uso.userLogin ) ;
                    } else if (  ref.equals( "userCenter" ) ) {
                        paramMap.put( id , (String)uso.userCenter ) ;
                    } else if ( ref.equals( "centerRoles" ) ) {
                        String cR = "" ;
                        HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
                        Object [] centerRoles = ((Set)cRoles.keySet()).toArray() ;
                        for ( int ci = 0 ; ci < centerRoles.length ; ci++ ) {
                            if ( ci > 0 ) { cR += " , " ; }
                            cR += (String)centerRoles[ci] ;
                        }
                        paramMap.put( id , cR ) ;
                    } else {
                        paramMap.put( id , (String)uso.eidpWebAppCache.sessionData_get( ref ) ) ;
                    }
                } else if ( referenceType.equals( "centerRoles" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String centers = "" ;
                    HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
                    Set centerKeys = (Set)cRoles.keySet() ;
                    Iterator cit = centerKeys.iterator() ;
                    int ci = 0 ;
                    while( cit.hasNext() ) {
                        if ( ci != 0 ) { centers += "," ; }
                        centers += (String)cit.next() ;
                        ci++ ;
                    }
                    paramMap.put( id , centers ) ;
                } else if ( referenceType.equals( "value" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String value = (String)((Vector)uso.xmlDataAccess.getElementsByName( "value" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    paramMap.put( id , value ) ;
                } else if ( referenceType.equals( "secondary-key-list" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String listEntryNrString = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-entry-nr" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    int listEntryNr = Integer.parseInt( listEntryNrString ) ;
                    String secondaryList = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-list-id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String secondaryListRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-entry-ref" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    Vector secondaryListVector = (Vector)uso.eidpWebAppCache.sessionData_get( secondaryList ) ;
                    Vector relativeSecRefVector = (Vector)uso.xmlDataAccess.getElementsByName( "relative-to-secondary-key,session-ref" , (NodeList)referenceVector.get( i )) ;
                    Vector relativeListIDVector = (Vector)uso.xmlDataAccess.getElementsByName( "relative-to-secondary-key,list-entry-id" , (NodeList)referenceVector.get( i )) ;
                    String relativeSecRef = "" ;
                    String relativeListID = "" ;
                    if ( relativeSecRefVector.size() > 0 ) {
                        relativeSecRef = (String)relativeSecRefVector.get( 0 ) ;
                        relativeListID = (String)relativeListIDVector.get( 0 ) ;
                    }
                    int rel = 0 ;
                    if ( ! relativeSecRef.equals( "" ) ) {
                        String relativeRefValue = (String)uso.eidpWebAppCache.sessionData_get( relativeSecRef ) ;
                        for ( int sli = 0 ; sli < secondaryListVector.size() ; sli++ ) {
                            HashMap entryMap = new HashMap() ;
                            entryMap = (HashMap)secondaryListVector.get( sli ) ;
                            String entryMapValue = (String)entryMap.get( relativeListID ) ;
                            if ( entryMapValue.equals( relativeRefValue ) ) {
                                rel = sli ;
                                break ;
                            }
                        }
                    }
                    listEntryNr = rel + listEntryNr ;
                    String value = "" ;
                    HashMap valueMap = new HashMap() ;
                    if ( listEntryNr < secondaryListVector.size() ) {
                        valueMap = (HashMap)secondaryListVector.get( listEntryNr ) ;
                        value = (String)valueMap.get( secondaryListRef ) ;
                    } else {
                        value = "";
                    }
                    paramMap.put( id , value ) ;
                }
            }
            uso.dbMapper.DBAction( dataset , method , paramMap ) ;
            // try for offset and limit
            int offset = 0;
            int limit = uso.dbMapper.size();
            String offsetStr = "";
            String limitStr = "";
            try {
                offsetStr = (String)((Vector)uso.xmlDataAccess.getElementsByName( "offset" , preLoad )).get( 0 ) ;
                limitStr = (String)((Vector)uso.xmlDataAccess.getElementsByName( "limit" , preLoad )).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException ae ) {}
            if (offsetStr.length() > 0) offset = Integer.parseInt(offsetStr);
            if (limitStr.length() > 0) limit = Integer.parseInt(limitStr);
            if (offset > limit) {
                uso.preLoadData.put(formName, null);
            } else {
                uso.preLoadData.put( formName , (Vector)uso.dbMapper.getRowRange( offset , limit ) ) ;
            }
 //           uso.preLoadData.put( formName , (Vector)uso.dbMapper.getRowRange( 0 , uso.dbMapper.size() ) ) ;
        }
        // Session settings:
        boolean sessionSet = false ;
        Vector setVector = new Vector() ;
        try {
            setVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "set" , preLoad ) ;
            sessionSet = true ;
        } catch ( org.xml.sax.SAXException saxe ) {
            sessionSet = false ;
        }
        if ( sessionSet == true ) {
            for ( int i = 0 ; i < setVector.size() ; i++ ) {
                String setType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref-type" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String value = new String() ;
                if ( refType.equals( "resultSet" ) ) {
                    HashMap row = new HashMap() ;
                    if ( uso.dbMapper.size() > 0 ) {
                        row = uso.dbMapper.getRow( 0 ) ;
                        value = (String)row.get( refValue ) ;
                    } else {
                        value = "" ;
                    }
                } else if ( refType.equals( "local" ) ) {
                    value = (String)uso.localRefs.get( refValue ) ;
                } else if ( refType.equals( "session" ) ) {
                    if ( refValue.equals( "userID" ) ) {
                        value =  uso.userID ;
                    } else if ( refValue.equals( "userCenter" ) ) {
                        value =  uso.userCenter ;
                    } else if ( refValue.equals( "userLogin" ) ) {
                        value = uso.userLogin ;
                    } else {
                        value = (String)uso.eidpWebAppCache.sessionData_get( refValue ) ;
                    }
                }
                if ( setType.equals( "session" ) ) {
                    if ( refID.equals( "userID" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userID" , value ) ;
                    } else if ( refID.equals( "userCenter" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userCenter" , value ) ;
                    } else if ( refID.equals( "userLogin" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userLogin" , value ) ;
                    } else {
                        sessionSet = true ;
                        uso.eidpWebAppCache.sessionData_set( refID , value ) ;
                    }
                } else if ( setType.equals( "local" ) ) {
                    uso.localRefs.put( refID , value ) ;
                }
            }
        }
    }
    
    protected void PreLoad( NodeList preLoad , String formName , UserScopeObject uso , HttpServletRequest request ) throws java.rmi.RemoteException, java.sql.SQLException , org.xml.sax.SAXException , java.io.IOException {
        uso.preLoadFlag = true ;
        String dataset = "" ;
        try {
            dataset = (String)((Vector)uso.xmlDataAccess.getElementsByName( "dataset" , preLoad )).get( 0 ) ;
        } catch ( java.lang.ArrayIndexOutOfBoundsException ae ) {
            uso.preLoadFlag = false ;
        }
        if ( uso.preLoadFlag == true && ! dataset.equals( "session" ) ) {
            String method = (String)((Vector)uso.xmlDataAccess.getElementsByName( "method" , preLoad )).get( 0 ) ;
            HashMap paramMap = new HashMap() ;
            // references
            Vector referenceVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "reference" , preLoad ) ;
            for ( int i = 0 ; i < referenceVector.size() ; i++ ) {
                String referenceType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                if ( referenceType.equals( "session" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    if ( ref.equals( "userID" ) ) {
                        paramMap.put( id , (String)uso.userID ) ;
                    } else if ( ref.equals( "userLogin" ) ) {
                        paramMap.put( id , (String)uso.userLogin ) ;
                    } else if (  ref.equals( "userCenter" ) ) {
                        paramMap.put( id , (String)uso.userCenter ) ;
                    } else if ( ref.equals( "centerRoles" ) ) {
                        String cR = "" ;
                        HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
                        Object [] centerRoles = ((Set)cRoles.keySet()).toArray() ;
                        for ( int ci = 0 ; ci < centerRoles.length ; ci++ ) {
                            if ( ci > 0 ) { cR += " , " ; }
                            cR += (String)centerRoles[ci] ;
                        }
                        paramMap.put( id , cR ) ;
                    } else {
                        paramMap.put( id , (String)uso.eidpWebAppCache.sessionData_get( ref ) ) ;
                    }
                } else if ( referenceType.equals( "centerRoles" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String centers = "" ;
                    HashMap cRoles = uso.eidpWebAppCache.centerRoles_getAll() ;
                    Set centerKeys = (Set)cRoles.keySet() ;
                    Iterator cit = centerKeys.iterator() ;
                    int ci = 0 ;
                    while( cit.hasNext() ) {
                        if ( ci != 0 ) { centers += "," ; }
                        centers += (String)cit.next() ;
                        ci++ ;
                    }
                    paramMap.put( id , centers ) ;
                } else if ( referenceType.equals( "value" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String value = (String)((Vector)uso.xmlDataAccess.getElementsByName( "value" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    paramMap.put( id , value ) ;
                } else if ( referenceType.equals( "request" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    paramMap.put( id , request.getParameter( ref ) ) ;
                } else if ( referenceType.equals( "secondary-key-list" ) ) {
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String listEntryNrString = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-entry-nr" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    int listEntryNr = Integer.parseInt( listEntryNrString ) ;
                    String secondaryList = (String)((Vector)uso.xmlDataAccess.getElementsByName( "secondary-list-id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String secondaryListRef = (String)((Vector)uso.xmlDataAccess.getElementsByName( "list-entry-ref" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    Vector secondaryListVector = (Vector)uso.eidpWebAppCache.sessionData_get( secondaryList ) ;
                    Vector relativeSecRefVector = (Vector)uso.xmlDataAccess.getElementsByName( "relative-to-secondary-key,session-ref" , (NodeList)referenceVector.get( i )) ;
                    Vector relativeListIDVector = (Vector)uso.xmlDataAccess.getElementsByName( "relative-to-secondary-key,list-entry-id" , (NodeList)referenceVector.get( i )) ;
                    String relativeSecRef = "" ;
                    String relativeListID = "" ;
                    if ( relativeSecRefVector.size() > 0 ) {
                        relativeSecRef = (String)relativeSecRefVector.get( 0 ) ;
                        relativeListID = (String)relativeListIDVector.get( 0 ) ;
                    }
                    int rel = 0 ;
                    if ( ! relativeSecRef.equals( "" ) ) {
                        String relativeRefValue = (String)uso.eidpWebAppCache.sessionData_get( relativeSecRef ) ;
                        for ( int sli = 0 ; sli < secondaryListVector.size() ; sli++ ) {
                            HashMap entryMap = new HashMap() ;
                            entryMap = (HashMap)secondaryListVector.get( sli ) ;
                            String entryMapValue = (String)entryMap.get( relativeListID ) ;
                            if ( entryMapValue.equals( relativeRefValue ) ) {
                                rel = sli ;
                                break ;
                            }
                        }
                    }
                    listEntryNr = rel + listEntryNr ;
                    String value = "" ;
                    HashMap valueMap = new HashMap() ;
                    if ( listEntryNr < secondaryListVector.size() ) {
                        valueMap = (HashMap)secondaryListVector.get( listEntryNr ) ;
                        value = (String)valueMap.get( secondaryListRef ) ;
                    } else {
                        value = "";
                    }
                    paramMap.put( id , value ) ;
                } else if (referenceType.equals("local")) { //David
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    String value = (String) uso.localRefs.get(ref);
                    paramMap.put( id , value ) ;
                } else if (referenceType.equals("timestamp")) { //David
                    String id = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)referenceVector.get( i ) )).get( 0 ) ;
                    Date timestamp = new Date() ;
                    String timeStampString = String.valueOf( timestamp.getTime() ) ;
                    paramMap.put( id , timeStampString ) ;
                }
            }
            uso.dbMapper.DBAction( dataset , method , paramMap ) ;
            // try for offset and limit
            int offset = 0;
            int limit = uso.dbMapper.size();
            String offsetStr = "";
            String limitStr = "";
            try {
                offsetStr = (String)((Vector)uso.xmlDataAccess.getElementsByName( "offset" , preLoad )).get( 0 ) ;
                limitStr = (String)((Vector)uso.xmlDataAccess.getElementsByName( "limit" , preLoad )).get( 0 ) ;
            } catch ( java.lang.ArrayIndexOutOfBoundsException ae ) {}
            if (offsetStr.length() > 0) offset = Integer.parseInt(offsetStr);
            if (limitStr.length() > 0) limit = Integer.parseInt(limitStr);
            if (offset > limit) {
                uso.preLoadData.put(formName, null);
            } else {
                uso.preLoadData.put( formName , (Vector)uso.dbMapper.getRowRange( offset , limit ) ) ;
            }
        }
        // Session settings:
        boolean sessionSet = false ;
        Vector setVector = new Vector() ;
        try {
            setVector = (Vector)uso.xmlDataAccess.getNodeListsByName( "set" , preLoad ) ;
            sessionSet = true ;
        } catch ( org.xml.sax.SAXException saxe ) {
            sessionSet = false ;
        }
        if ( sessionSet == true ) {
            for ( int i = 0 ; i < setVector.size() ; i++ ) {
                String setType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "type" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refType = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref-type" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "ref" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String refID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "id" , (NodeList)setVector.get( i ) ) ).get( 0 ) ;
                String value = new String() ;
                if ( refType.equals( "resultSet" ) ) {
                    HashMap row = new HashMap() ;
                    if ( uso.dbMapper.size() > 0 ) {
                        row = uso.dbMapper.getRow( 0 ) ;
                        value = (String)row.get( refValue ) ;
                    } else {
                        value = "" ;
                    }
                } else if ( refType.equals( "local" ) ) {
                    value = (String)uso.localRefs.get( refValue ) ;
                } else if ( refType.equals( "session" ) ) {
                    if ( refValue.equals( "userID" ) ) {
                        value =  uso.userID ;
                    } else if ( refValue.equals( "userCenter" ) ) {
                        value =  uso.userCenter ;
                    } else if ( refValue.equals( "userLogin" ) ) {
                        value = uso.userLogin ;
                    } else {
                        value = (String)uso.eidpWebAppCache.sessionData_get( refValue ) ;
                    }
                } else if ( refType.equals( "request" ) ) {
                    value = request.getParameter( refValue );
                }
                if ( setType.equals( "session" ) ) {
                    if ( refID.equals( "userID" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userID" , value ) ;
                    } else if ( refID.equals( "userCenter" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userCenter" , value ) ;
                    } else if ( refID.equals( "userLogin" ) ) {
                        uso.eidpWebAppCache.sessionData_set( "userLogin" , value ) ;
                    } else {
                        sessionSet = true ;
                        uso.eidpWebAppCache.sessionData_set( refID , value ) ;
                    }
                } else if ( setType.equals( "local" ) ) {
                    uso.localRefs.put( refID , value ) ;
                }
            }
        }
    }
    
    protected String getReferenceValue( NodeList fieldNode , String formName , int row , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        String fieldValue = "" ;
        String debugInfo = "getReferenceValue throws Exception due to: " ;
        debugInfo += "formName = " + formName + "; " ;
        try {
            if ( uso.preLoadFlag == true ) {
                String ref = "" ;
                String refType = "" ;
                try {
                    ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "session-ref" , fieldNode ) ).get( 0 ) ;
                    refType = "session" ;
                } catch ( java.lang.ArrayIndexOutOfBoundsException ae ) {
                    try {
                        ref = (String)((Vector)uso.xmlDataAccess.getElementsByName( "db-ref" , fieldNode ) ).get( 0 ) ;
                        refType = "db" ;
                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
                        refType = "NULL" ;
                    }
                }
                debugInfo += "refType = " + refType + "; ref = " + ref + "; " ;
                // get type
                if ( refType.equals( "session" ) ) {
                    if ( ref.equals( "userID" ) ) {
                        fieldValue = (String)uso.userID ;
                    } else if ( ref.equals( "userLogin" ) ) {
                        fieldValue = (String)uso.userLogin ;
                    } else if ( ref.equals( "userCenter" ) ) {
                        fieldValue = (String)uso.userCenter ;
                    } else {
                        fieldValue = (String)uso.eidpWebAppCache.sessionData_get( ref ) ;
                    }
                } else if ( refType.equals( "db" ) ) {
                    String preloadID = "" ;
                    String preloadValue = "" ;
                    String preloadFillValue = "" ;
                    boolean preloadFill = true ;
                    Vector plData = (Vector)uso.preLoadData.get( formName ) ;
                    // PreLoadData?
                    try {
                        preloadID = (String)((Vector)uso.xmlDataAccess.getElementsByName( "preload-ref,id" , fieldNode )).get( 0 ) ;
                        preloadValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "preload-ref,value" , fieldNode )).get( 0 ) ;
                    } catch ( java.lang.ArrayIndexOutOfBoundsException aiob ) {
                        if ( uso.multiFlag == false ) {
                            row = 0 ;
                        }
                    }
                    debugInfo += "preloadID = " + preloadID + "; prloadValue = " + preloadValue + "; " ;
                    if ( ! preloadID.equals( "" ) ) {
                        try {
                            preloadFillValue = (String)((Vector)uso.xmlDataAccess.getElementsByName( "preload-ref,if-not-exist-value" , fieldNode )).get( 0 ) ;
                        } catch ( java.lang.ArrayIndexOutOfBoundsException aib ) {
                            preloadFill = false ;
                        }
                        // get row:
                        for ( int pli = 0 ; pli < plData.size() ; pli++ ) {
                            
                            if ( ((String)((HashMap)plData.get( pli )).get( preloadID )).equals( preloadValue ) ) {
                                row = pli ;
                                break ;
                            } else {
                                row = -1 ;
                            }
                        }
                        if ( preloadFill == true ) {
                            fieldValue = preloadFillValue ;
                        }
                    }
                    if ( plData.size() > 0 && row != -1 ) {
                        fieldValue = (String)((HashMap)plData.get( row )).get( ref ) ;
                    } else { fieldValue = "" ; }
                    if ( fieldValue.equals( "" ) && preloadFill == true ) {
                        fieldValue = preloadFillValue ;
                    }
                } else {
                    fieldValue = "" ;
                }
            }
            debugInfo += "fieldValue = " + fieldValue + "." ;
        } catch ( java.lang.NullPointerException e ) {
            System.out.println( debugInfo ) ;
            throw new org.xml.sax.SAXException( "getReferenceValue: NullPointerException: " + debugInfo ) ;
        }
        return fieldValue ;
    }
    
    protected HashMap getParams( NodeList dataNode , UserScopeObject uso ) throws org.xml.sax.SAXException , java.io.IOException {
        HashMap paramMap = new HashMap() ;
        Vector globalParamsName = (Vector)uso.xmlDataAccess.getElementsByName( "param,id" , dataNode ) ;
        Vector globalParamsRef = (Vector)uso.xmlDataAccess.getElementsByName( "param,ref" , dataNode ) ;
        for ( int i = 0 ; i < globalParamsName.size() ; i++ ) {
            paramMap.put( (String)globalParamsName.get(i) , uso.eidpWebAppCache.sessionData_get( (String)globalParamsRef.get(i) ) ) ;
        }
        return paramMap ;
    }
    
    protected PrintWriter initHTML( HttpServletRequest request , HttpServletResponse response ) throws java.io.IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter = response.getWriter() ;
        printWriter.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"");
        printWriter.println("   http://www.w3.org/TR/html4/loose.dtd\">");
        printWriter.println( "<html>");
        printWriter.println( "<head>");
        printWriter.println( "  <meta http-equiv=\"pragma\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"cache-control\" content=\"no-cache\"> " ) ;
        printWriter.println( "  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >");
        printWriter.println( "  <title>EIDP Web XMLDispatcher</title> " ) ;
        printWriter.println( "  <style type=\"text/css\"> " ) ;
        printWriter.println( "  <!-- " ) ;
        printWriter.println( "      body { font-family:Arial,sans-serif; color:black; font-size:10pt ; } " ) ;
        printWriter.println( "      a:link { text-decoration:none; color:black; } " ) ;
        printWriter.println( "      a:visited { text-decoration:none; color:black ; } " ) ;
        printWriter.println( "      a:hover { text-decoration:none; color:yellow ; } " ) ;
        printWriter.println( "      a:active { text-decoration:none; color:black ; } " ) ;
        // printWriter.println( "  table { border-style:solid;border-color:#333333;border-width:2px;border-spacing:1px ; } " ) ;
        printWriter.println( "      td { background-color:#DDDDDD;color:#000000 ; font-size:10pt ; } " ) ;
        printWriter.println( "      td.label { background-color:#DDDDDD;color:#000000 ; font-size:10pt ; } " ) ;
        printWriter.println( "      td.inputNEW { background-color:#EEEEEE;color:#000000 ; font-size:10pt ; } " ) ;
        printWriter.println( "      td.input { background-color:#CCCCCC;color:#000000 ; font-size:10pt ; } " ) ;
        printWriter.println( "      td.inputContrast { background-color:#999999;color:#000000 ; font-size:10pt ; } " ) ;
        printWriter.println( "      td.white { background-color:#FFFFFF;color:#000000 ; font-size:10pt ; } " ) ;
        printWriter.println( "  --> </style> " ) ;
        // Calendar // David
        printWriter.println("   <link rel=\"stylesheet\" type=\"text/css\" href=\"/EIDPWebApp/stylesheets/calendar.css\">");
        printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/CalendarPopup.js\"></script>");
        printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/date.js\"></script>");
        printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/AnchorPosition.js\"></script>");
        printWriter.println("   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/EIDPWebApp/javascript/PopupWindow.js\"></script>");
        //
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        printWriter.println( "      var userChanges = 0 ; " ) ;
        printWriter.println( "      var exceptionMap = new Array() ; " ) ;
        printWriter.println( "      var commaSeparation = \", \" ; " ) ;
        printWriter.println( "      var commaSeparationTrim = \",\" ; " ) ;        
        printWriter.println( "  </script> " ) ;
        
        return printWriter ;
    }
    
    protected void closeHTML( PrintWriter printWriter , UserScopeObject uso ) throws java.rmi.RemoteException {
        String actualModule = (String)uso.eidpWebAppCache.sessionData_get( "module" ) ;
        actualModule += ";" + (String)uso.eidpWebAppCache.sessionData_get( "xmlFile" ) ;
        actualModule += ";" + (String)uso.eidpWebAppCache.sessionData_get( "moduleParameter" ) ;
        uso.eidpWebAppCache.sidePanelEntry_set( actualModule ) ;
        printWriter.println( "  <script language=\"JavaScript\" type=\"text/javascript\"> " ) ;
        printWriter.println( "      if ( this.name == 'Data' ) { " ) ;
        printWriter.println( "          parent.SidePanel.location.href=\"/EIDPWebApp/servlet/com.eidp.webctrl.Controller?module=Function;SidePanel;show\"; " ) ;
        printWriter.println( "      } " ) ;
        printWriter.println( "  </script> " ) ;
        printWriter.println( "  </body> " ) ;
        printWriter.println( "</html>" ) ;
        printWriter.close() ;
    }
}
