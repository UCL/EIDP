/*
 * DBMapping.java
 *
 * Created on April 28, 2003, 10:46 AM
 */

package com.eidp.core.DB;

import com.eidp.xml.XMLDataAccess;
// import com.eidp.logger.Logger;
import java.util.logging.*;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import java.util.HashMap;
import java.util.Vector;
import java.util.Set ;
import java.util.Iterator ;

import java.lang.reflect.* ;

import org.w3c.dom.NodeList;

import javax.servlet.http.HttpSessionListener ;
import javax.servlet.http.HttpSessionEvent ;
import sun.misc.BASE64Encoder;

import java.util.Date ;

// import org.apache.log4j.Logger;
// import org.apache.log4j.BasicConfigurator;

/**
 * DBMapping works as a network interface that performs database actions (insert /
 * update / select / remove) by reading in the file
 * <CODE>com.eidp.&lt;applicationname&gt;resources.db.xml</CODE>
 * file. It maps XML data structures and commands to a database
 * system.
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

public class DBMapping implements SessionBean , HttpSessionListener {
    
    /**
     * Session Context Method.
     */
    public SessionContext context;
    private HashMap dataSourceClasses = new HashMap();
    private HashMap dataSourceObjects = new HashMap() ;
    private String dataSourceIDCache = "default" ;
    
    private XMLDataAccess xmlDataAccess;
    
    private String applicationContext = "" ;
    
    private boolean SSO_AUTH = false ;
    
    private HashMap SSO_AUTH_DATA = new HashMap() ;
    
    private boolean AUTH = false ;
    
    private Vector AUTH_EXCEPT = new Vector() ;
    
    private Vector AUTH_PROPAGATE = new Vector() ;
    
    private Logger logger = null ;
    private FileHandler fh = null ;
    private int LOGIN_TIMEOUT = 120000 ;
    
    /**
     * Create the Enterprise Java Bean.
     * @param applicationname applicationname specifies the name of the application mounting this class
     * @throws SQLException
     * @throws IOException
     * @throws CreateException
     */
    public void ejbCreate( String applicationname ) throws java.sql.SQLException, java.io.IOException , javax.ejb.CreateException {
        this.applicationContext = applicationname ;
        this.applicationInit() ;
    }
    
    private void applicationInit() throws java.sql.SQLException, java.io.IOException , javax.ejb.CreateException {
        this.logger = Logger.getLogger("com.eidp.core.DB.DBMapping."+this.applicationContext+"."+this.context.hashCode());
        // this.fh = new FileHandler("/com/eidp/logs/core/Core."+this.applicationContext+"."+this.context.hashCode()+".log");
        // this.logger.addHandler(fh);
        this.logger.setLevel(Level.INFO);
        this.logger.info("DBMapping: Instantiating Core-System on: "+this.applicationContext+" .");
        this.logger.info("DBMapping: >>> DBMapping.applicationInit() called.");
        this.logger.info("DBMapping: >>> Possible Log-Levels: INFO (default), FINE, FINEST, ALL, OFF, SEVERE");
        
        this.AUTH_EXCEPT.add("USERS.getUserDataForLogin") ;
        this.AUTH_EXCEPT.add("ROLES.getRolesForLogin") ;
        this.AUTH_EXCEPT.add("USERS.setLoginError") ;
        this.AUTH_EXCEPT.add("CENTER_ROLES.getCentersForUser") ;
        String xmlfile = "/com/eidp/" + this.applicationContext + "/resources/db.xml" ;
        try {
            this.xmlDataAccess = new XMLDataAccess( xmlfile ) ;
            Vector logLevelVector = this.xmlDataAccess.getElementsByName( "log-level" ) ;
            if ( logLevelVector.size() > 0 ) {
                String logLevel = (String)logLevelVector.get(0) ;
                logger.info(">>> DBMapping: Log-level will be set to: "+logLevel);
                if ( logLevel.equals("All")) this.logger.setLevel(Level.ALL);
                else if ( logLevel.equals("FINE")) this.logger.setLevel(Level.FINE);
                else if ( logLevel.equals("FINEST")) this.logger.setLevel(Level.FINEST);
                else if ( logLevel.equals("SEVERE")) this.logger.setLevel(Level.SEVERE);
                else if ( logLevel.equals("OFF")) this.logger.setLevel(Level.OFF);
                else this.logger.setLevel(Level.INFO) ;
            }
            Vector authElVector = this.xmlDataAccess.getElementsByName( "authentication" ) ;
            this.logger.fine("DBMapping: Getting Authentication Information.");
            if ( authElVector.size() > 0 ) {
                String authentication = (String)authElVector.get( 0 ) ;
                if ( authentication.equals( "true" ) ) this.AUTH = true ;
                else this.AUTH = false ;
                this.logger.fine("DBMapping: Authentication = "+this.AUTH);
            }
            Vector dataNodeVector = this.xmlDataAccess.getNodeListsByName( "database" ) ;
            this.logger.info("DBMapping: Getting datasource information.");
            Iterator dni = dataNodeVector.iterator() ;
            while( dni.hasNext() ) {
                NodeList dataNode = (NodeList)dni.next() ;
                // get name
                String dnName = "default" ;
                Vector dnNameVector = xmlDataAccess.getElementsByName( "id" , dataNode ) ;
                if ( dnNameVector.size() > 0 ) {
                    dnName = (String)dnNameVector.get( 0 ) ;
                }
                this.logger.fine("DBMapping: Datasource name = "+dnName);
                String dnClass = "DataBaseMapping" ;
                Vector dnClassVector = xmlDataAccess.getElementsByName( "class" , dataNode ) ;
                if ( dnClassVector.size() > 0 ) {
                    dnClass = (String)dnClassVector.get( 0 ) ;
                }
                this.logger.fine("DBMapping: Datasource class = "+dnClass);
                // instantiate class
                this.logger.fine("DBMapping: Instantiating <"+dnName+">.");
                String dnClassInst = "com.eidp.core.DB.modules." + dnClass ;
                Class dnClassObject = Class.forName( dnClassInst ) ;
                Class[] paramClasses = { String.class , NodeList.class , XMLDataAccess.class , Logger.class } ;
                Constructor constr = dnClassObject.getConstructor( paramClasses ) ;
                Object[] paramObjects = { this.applicationContext , dataNode , this.xmlDataAccess , this.logger } ;
                Object object = constr.newInstance( paramObjects ) ;
                this.dataSourceClasses.put( dnName , dnClassObject ) ;
                this.dataSourceObjects.put( dnName , object ) ;
                // AUTH_PROPAGATE
                Vector dnClassAuthPropagate = xmlDataAccess.getElementsByName( "auth-propagate" , dataNode ) ;
                this.logger.fine("DBMapping: Retrieving Authentication Propagation information.");
                if ( dnClassAuthPropagate.size() > 0 ) {
                    String authPropagate = (String)dnClassAuthPropagate.get(0) ;
                    if ( authPropagate.equals("true")) {
                        this.AUTH_PROPAGATE.add(dnName) ;
                        this.logger.fine("DBMapping: Authentication propagation set for <"+dnName+">.");
                    }
                } else this.logger.fine("DBMapping: No authentication propagation set.");
            }
            this.logger.info("DBMapping: Datasource information retrieval finished.");
        } catch ( org.xml.sax.SAXException e ) {
            this.logger.severe("DBMapping: SAXException thrown in DBMapping.applicationInit: "+e);
            throw new javax.ejb.CreateException( "DataNodeVector could not be created: " + e ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException e ) {
            this.logger.severe("DBMapping: CreateException thrown in DBMapping.applicationInit: "+e);
            throw new javax.ejb.CreateException( "DataNodeVector could not be created: " + e ) ;
        } catch ( java.lang.ClassNotFoundException e ) {
            this.logger.severe("DBMapping: ClassNotFoundException thrown in DBMapping.applicationInit: "+e);
            throw new javax.ejb.CreateException( "Class not found at application init: " + e ) ;
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException thrown in DBMapping.applicationInit: "+e);
            throw new javax.ejb.CreateException( "No Such Method Exception at application init: " + e ) ;
        } catch ( java.lang.InstantiationException e ) {
            this.logger.severe("DBMapping: InstantiationException thrown in DBMapping.applicationInit: "+e);
            throw new javax.ejb.CreateException( "Instantiation Exception at application init: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException thrown in DBMapping.applicationInit: "+e);
            throw new javax.ejb.CreateException( "Illegal Access Exception at application init: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException thrown in DBMapping.applicationInit: "+e.getTargetException());
            throw new javax.ejb.CreateException( "Invocation Target Exception at application init: " + e.getTargetException() ) ;
        }
        
    }
    
    /**
     * DBAction reads in the necessary information to process the required SQL
     * querying. DBAction reads in the file
     * <CODE>com.eidp.&lt;applicationname&gt;resources.db.xml</CODE> and
     * performs under the specifications given in this file the necessary actions.
     * "dataset" and "method" specify the table and the method described in the db.xml
     * file. The paramMap contains the data that is needed by dataset-method to perform
     * the required action. paramMap is a HashMap that contains the data by field-ids.
     * @param dataset DATASET to be called.
     * @param method Method to be accessed.
     * @param paramMap HashMap of parameters ( parameter name -> value).
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     */
    public void DBAction(String dataset, String method, HashMap paramMap) throws Exception , org.xml.sax.SAXException, java.io.IOException, java.sql.SQLException {
        this.logger.info("DBMapping: >>> DBMapping.DBAction called.");
        // ======
        // DBAction gets the dataSetNode and the methodNode for the specified dataSet and method
        // and hands it over to the private method ProcessDBAction
        // ======
        // ---> Vars with DBAction-method-scope
        NodeList dataSetNode = null ;
        String dataSourceID = "default" ;
        NodeList methodNode = null ;
        // <---
        // 1. get Array for dataSet:
        this.logger.fine("DBMapping: Retrieving db,dataset.");
        Vector dataSetVector = new Vector();
        try {
            dataSetVector = this.xmlDataAccess.getNodeListsByName( "db,dataset" );
        } catch ( org.xml.sax.SAXException saxe ) {
            this.logger.severe("DBMapping: SAXException thrown when retrieving db,dataset: "+saxe);
            throw new javax.ejb.EJBException( "SAXException thrown when retrieving db,dataset: " + saxe ) ;
        }
        this.logger.fine("DBMapping: Processing dataset entries.");
        // 2. process any dataSet-entry:
        for ( int ds_i = 0 ; ds_i < dataSetVector.size() ; ds_i++ ) {
            // 1. choose the name of the dataSet and check if this is equal String dataSet
            // ---> there exists only one single name for each dataSet!
            Vector nameDataSet = new Vector();
            try {
                nameDataSet = this.xmlDataAccess.getElementsByName( "name" , (NodeList)dataSetVector.get(ds_i) ) ;
            } catch ( org.xml.sax.SAXException saxe ) {
                this.logger.severe("DBMapping: Could not retrieve db,dataset,name."+saxe);
                throw new javax.ejb.EJBException( "DBMapping - DBAction cannot find Elements from NodeList: " + saxe ) ;
            }
            if ( ((String)nameDataSet.get(0)).equals( dataset ) ) {
                this.logger.fine("DBMapping: Dataset found");
                dataSetNode = (NodeList)dataSetVector.get(ds_i) ;
                Vector dataSourceIDVector = new Vector() ;
                try {
                    dataSourceIDVector = this.xmlDataAccess.getElementsByName( "database-id" , (NodeList)dataSetVector.get( ds_i ) ) ;
                } catch ( org.xml.sax.SAXException saxe ) {
                    this.logger.severe("DBMapping: Could not retrieve db,dataset,database-id: "+saxe);
                    throw new javax.ejb.EJBException( "Could not retrieve db,dataset,database-id: " + saxe ) ;
                }
                if ( dataSourceIDVector.size() > 0 ) {
                    dataSourceID = (String)dataSourceIDVector.get( 0 ) ;
                }
                break ; // breaks processing the for loop !
            }
            
        }
        this.logger.fine("DBMapping: Datasource-ID = "+dataSourceID);
        // Now we have the dataSet in dataSetNodeList ;
        // Now look for the method in the dataSet:
        this.logger.fine("DBMapping: Retrieving method signatures (db,dataset,method): ");
        Vector methodVector = this.xmlDataAccess.getNodeListsByName( "method", dataSetNode )  ;
        for ( int m_i = 0 ; m_i < methodVector.size() ; m_i++ ) {
            // see for with dataSetVector for descritpion
            // use again nameRequestArray ( again "name" !!! )
            Vector nameDataSet = this.xmlDataAccess.getElementsByName( "name" , (NodeList)methodVector.get(m_i) ) ;
            if ( nameDataSet.size() == 0 ) {
                this.logger.severe("DBMapping: No method name given.");
                throw new NoSuchMethodException("No method given in db.xml");
            }
            if ( ((String)nameDataSet.get(0)).equals( method ) ) {
                methodNode = (NodeList)methodVector.get(m_i) ;
                break ; // breaks processing for loop !
            }
        }
        String methodSignature = dataset + "." + method ;
        this.logger.fine("DBMapping: Checking method signature: "+methodSignature+" against AUTH_EXCEPT");
        if ( this.AUTH && ! this.AUTH_EXCEPT.contains(methodSignature) ) {
            Vector roleElements = new Vector() ;
            roleElements = this.xmlDataAccess.getElementsByName( "role-name" , methodNode ) ;
            if ( roleElements.size() == 0 ) {
                this.logger.fine("DBMapping: No roles defined in AUTH environment for <"+methodSignature+">");
                throw new java.security.AccessControlException( "DBMapper: AccessControlException. No roles defined for Method: " +dataset+"."+ method+"." ) ;
            }
            Iterator ri = roleElements.iterator() ;
            boolean authOK = false ;
            this.logger.fine("DBMapping: Checking role-authentication against: "+((Vector)this.SSO_AUTH_DATA.get("userRoles")).toString());
            while ( ri.hasNext() ) {
                String roleName = (String)ri.next() ;
                // !!! System.out.println( "CHECKING <"+roleName+"> against SSO_AUTH_DATA.") ;
                this.logger.fine("DBMapping: Checking <"+roleName+"> against SSO_AUTH_DATA.");
                if ( ((Vector)this.SSO_AUTH_DATA.get( "userRoles" )).contains( roleName ) ) {
                    this.logger.fine("DBMapping: Giving <"+roleName+"> access to method: "+methodSignature);
                    authOK = true ;
                    break ;
                }
            }
            if ( authOK == false ) {
                this.logger.severe("DBMapping: AccessControllException thrown. User not allowed to call method: "+methodSignature);
                throw new java.security.AccessControlException( "AccessControllException thrown. User not allowed to call method: "+methodSignature ) ;
            }
        }
        try {
            this.logger.info("DBMapping: Calling datasource-id: "+dataSourceID);
            Class  arguments[] = new Class[] { NodeList.class , NodeList.class , HashMap.class , Logger.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( dataSourceID )).getMethod( "ProcessDBAction" , arguments ) ;
            Object[] paramObjects = { dataSetNode , methodNode , paramMap , this.logger } ;
            Object result = callMethod.invoke( (Object)this.dataSourceObjects.get( dataSourceID ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException in DBAction: "+e);
            throw new Exception( "EIDP Core System ProcessDBAction: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException in DBAction: "+e);
            throw new Exception( "EIDP Core System ProcessDBAction: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException in DBAction: "+e.getTargetException());
            throw new Exception( "EIDP Core System ProcessDBAction: " + e.getTargetException() ) ;
        }
        this.dataSourceIDCache = dataSourceID ;
    }
    
    /**
     * Get row of a ResultSet.
     * @param rowNumber RowNumber to be retrieved from the ResultSet.
     * @return HashMap of the requested row in the ResultSet.
     */
    public HashMap getRow( int rowNumber ) throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.getRow( int ) called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { Integer.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getRow" , arguments ) ;
            Object[] paramObjects = { new Integer( rowNumber ) } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException in getRow: "+e);
            throw new Exception( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException in getRow: "+e);
            throw new Exception( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException in getRow: "+e.getTargetException());
            throw new Exception( "EIDP Core System getRow: " + e.getTargetException() ) ;
        }
        return (HashMap)result ;
    }
    
    /**
     * Get a range of Rows out of the ResultSet.
     * @param rowNumber Start with retrieval from rowNumber.
     * @param endRow Stop with retrieval.
     * @return Vector of Hashes.
     */
    public Vector getRowRange( int rowNumber , int endRow ) throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.getRowRange( int , int ) called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { Integer.class , Integer.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getRowRange" , arguments ) ;
            Object[] paramObjects = { new Integer( rowNumber ) , new Integer( endRow ) } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException in getRow: "+e);
            throw new Exception( "EIDP Core System getRowRange: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException in getRow: "+e);
            throw new Exception( "EIDP Core System getRowRange: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException in getRow: "+e.getTargetException());
            throw new Exception( "EIDP Core System getRow: " + e.getTargetException() ) ;
        }
        return (Vector)result ;
    }
    
    /**
     * Retrieve the size of the cached ResultSet.
     * @return Returns an int for the size of the ResultSet.
     */
    public int size() throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.size() called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "size" , arguments ) ;
            Object[] paramObjects = {} ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException in getRow: "+e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException in getRow: "+e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException in getRow: "+e.getTargetException());
            throw new Exception( "EIDP Core System size: " + e.getTargetException() ) ;
        }
        return ((Integer)result).intValue() ;
    }
    
    public HashMap Authenticate( String TW_PRINCIPAL , String TW_CREDENTIALS ) throws Exception {
        this.logger.info("DBMapping: >>> DBMapping.Authenticate().");
        this.logger.fine("DBMapping: CALL Parameters: PRINCIPAL = " + TW_PRINCIPAL + "; CREDENTIALS=---HIDDEN---");
        HashMap returnAuthData = new HashMap() ;
        HashMap paramMap = new HashMap() ;
        paramMap.put( "login" , TW_PRINCIPAL ) ;
        this.logger.fine("DBMapping: Calling USERS.getUserDataForLogin.");
        this.DBAction( "USERS" , "getUserDataForLogin" , paramMap ) ;
        if ( this.size() > 0 ) {
            this.logger.fine("DBMapping: Data retrieved.");
            HashMap dbResult = new HashMap() ;
            String userID = (String)((HashMap)this.getRow( 0 )).get( "id" ) ;
            String databasePassword = (String)((HashMap)this.getRow( 0 )).get( "password" ) ;
            String pwdCreateTimestamp = (String)((HashMap)this.getRow( 0 )).get( "create_timestamp" ) ;
            String pwdModifyTimestamp = (String)((HashMap)this.getRow( 0 )).get( "modify_timestamp" ) ;
            String pwdErrorTimestamp = (String)((HashMap)this.getRow( 0 )).get( "login_err_timestamp" ) ;
            String pwdErrorNumber = (String)((HashMap)this.getRow( 0 )).get( "login_err_number" ) ;
            Date dateTimestamp = new Date() ;
            Date errorDateTimeStamp = new Date() ;
            int errorNumber = 0;
            if(!pwdErrorNumber.equals(""))
                errorNumber = Integer.parseInt(pwdErrorNumber);
            if(!pwdErrorTimestamp.equals("")){
                errorDateTimeStamp =  new Date(Long.parseLong(pwdErrorTimestamp) + this.LOGIN_TIMEOUT) ;
            }
            
            String userInputPassword = TW_CREDENTIALS ;
            if ( ! databasePassword.equals( "START_PASSWORD" ) ) {
                userInputPassword = new String( encrypt( TW_CREDENTIALS ) ) ;
                BASE64Encoder encoder = new BASE64Encoder() ;
                userInputPassword = new String( encoder.encode( userInputPassword.getBytes() ).toString() ) ;
            }
            this.logger.fine("DBMapping: Checking passwords.");
            if(errorNumber < 4 || errorDateTimeStamp.before(dateTimestamp)){
                if(errorNumber >= 4 && errorDateTimeStamp.before(dateTimestamp)){
                    paramMap.put( "login_err_number" , "0" ) ;
                    paramMap.put( "login_err_timestamp" , String.valueOf(dateTimestamp.getTime()) ) ;
                    this.DBAction( "USERS" , "setLoginError" , paramMap ) ;
                    errorNumber = 0;
                }
                if ( databasePassword.equals( userInputPassword ) || ( databasePassword.equals( "START_PASSWORD" ) && userInputPassword.equals( databasePassword ) ) ) {
                    this.logger.fine("DBMapping: --- Password authentication fulfilled successfully for"+TW_PRINCIPAL+" ---");
                    this.logger.fine("DBMapping: Now checking for authorization details.");
                    // set Session!
                    // 1. set login / user ID
                    returnAuthData.put( "userLogin" , TW_PRINCIPAL ) ;
                    returnAuthData.put( "userID" , userID ) ;
                    // ===> Check if password has expired <===
                    long pwdCreateTimestampLong = Long.parseLong( pwdCreateTimestamp ) ;
                    long pwdModifyTimestampLong = Long.parseLong( pwdModifyTimestamp ) ;
                    String threeMonthsString = "7776000000" ;
                    long threeMonths = Long.parseLong( threeMonthsString ) ;
                    String passwordExpired = "false" ;
                    if ( pwdCreateTimestampLong == pwdModifyTimestampLong ) {
                        passwordExpired = "true" ;
                    } else {
                        pwdModifyTimestampLong += threeMonths ;
                        Date timestamp = new Date() ;
                        String timeStampString = String.valueOf( timestamp.getTime() ) ;
                        long todayTimestamp = Long.parseLong( timeStampString ) ;
                        if ( pwdModifyTimestampLong < todayTimestamp ) {
                            passwordExpired = "true" ;
                        }
                    }
                    this.logger.fine("DBMapping: Password expired = "+passwordExpired);
                    returnAuthData.put( "passwordExpired" , passwordExpired ) ;
                    // 2. get UserRoles
                    this.logger.fine("DBMapping: Calling ROLES.getRolesForLogin");
                    this.DBAction( "ROLES" , "getRolesForLogin" , paramMap ) ;
                    if ( this.size() == 0 ) {
                        this.logger.fine("DBMapping: Role Authentication not successfull for: " + returnAuthData.get( "userID" ) + " ---");
                        this.SSO_AUTH = false ;
                        returnAuthData.clear() ;
                        returnAuthData.put( "loginRoleError" , "true" ) ;
                        this.SSO_AUTH_DATA = returnAuthData ;
                        return returnAuthData ;
                    }
                    Vector userRoles = new Vector() ;
                    for ( int i = 0 ; i < this.size() ; i++ ) {
                        String role = "" ;
                        role = (String)((HashMap)this.getRow( i )).get( "role" ) ;
                        userRoles.add( role ) ;
                    }
                    this.logger.fine("DBMapping: Roleset for user: "+TW_PRINCIPAL+": "+userRoles.toString());
                    returnAuthData.put( "userRoles" , userRoles ) ;
                    // 3. get Center Data
                    this.logger.fine("DBMapping: Calling CENTER_ROLES.getCentersForUser");
                    this.DBAction( "CENTER_ROLES" , "getCentersForUser" , paramMap ) ;
                    if ( this.size() == 0 ) {
                        this.logger.fine("DBMapping: Center authentication not successfull for: "+returnAuthData.get( "userID" )+" ---");
                        this.SSO_AUTH = false ;
                        returnAuthData.clear() ;
                        returnAuthData.put( "loginCenterRoleError" , "true" ) ;
                        this.SSO_AUTH_DATA = returnAuthData ;
                        return returnAuthData ;
                    }
                    HashMap userCenters = new HashMap() ;
                    for ( int i = 0 ; i < this.size() ; i++ ) {
                        String center = "" ;
                        String status = "" ;
                        String permission = "" ;
                        center = (String)((HashMap)this.getRow( i )).get( "center" ) ;
                        status = (String)((HashMap)this.getRow( i )).get( "status" ) ;
                        permission = (String)((HashMap)this.getRow( i )).get( "permission" ) ;
                        userCenters.put( center , permission ) ;
                        if ( status.equals( "m" ) ) {
                            returnAuthData.put( "userCenter" , center ) ;
                        }
                    }
                    this.logger.fine("DBMapping: Centerset for user "+TW_PRINCIPAL+": "+userCenters.toString());
                    returnAuthData.put( "userCenters" , userCenters ) ;
                    this.SSO_AUTH = true ;
                    this.SSO_AUTH_DATA = returnAuthData ;
                    Iterator api = this.AUTH_PROPAGATE.iterator() ;
                    if (this.SSO_AUTH) this.logger.info("DBMapping: Getting into Authentication-Propagation");
                    while ( api.hasNext() ) {
                        String apName = (String)api.next() ;
                        this.logger.fine("DBMapping: Authentication-Propagation for: "+apName);
                        try {
                            Class  arguments[] = new Class[] { String.class , String.class } ;
                            Method callMethod = ((Class)this.dataSourceClasses.get( apName )).getMethod( "Authenticate" , arguments ) ;
                            Object[] paramObjects = { TW_PRINCIPAL , TW_CREDENTIALS } ;
                            Object result = callMethod.invoke( (Object)this.dataSourceObjects.get( apName ) , paramObjects );
                            if ( ( (HashMap)result).isEmpty() ) {
                                this.logger.severe("DBMapping: Could not retrieve remote authentication for: "+returnAuthData.get( "userID" ));
                                this.SSO_AUTH = false ;
                                returnAuthData.clear() ;
                                this.SSO_AUTH_DATA = returnAuthData ;
                                return returnAuthData ;
                            }
                        } catch ( java.lang.NoSuchMethodException e ) {
                            this.logger.severe("DBMapping: NoSuchMethodException in DBMapping.Authenticate(): "+e);
                            throw new Exception( "EIDP Core System ProcessDBAction: " + e ) ;
                        } catch ( java.lang.IllegalAccessException e ) {
                            this.logger.severe("DBMapping: IllegalAccessException in DBMapping.Authenticate(): "+e);
                            throw new Exception( "EIDP Core System ProcessDBAction: " + e ) ;
                        } catch ( java.lang.reflect.InvocationTargetException e ) {
                            this.logger.severe("DBMapping: InvocationTargetException in DBMapping.Authenticate(): "+e.getTargetException());
                            throw new Exception( "EIDP Core System ProcessDBAction: " + e.getTargetException() ) ;
                        }   
                    }
                    paramMap.put( "login_err_number" , "0" ) ;
                    paramMap.put( "login_err_timestamp" , String.valueOf(dateTimestamp.getTime()) ) ;
                    this.DBAction( "USERS" , "setLoginError" , paramMap ) ;
                    return returnAuthData ;
                } else {
                    errorNumber++;
                    paramMap.put( "login_err_number" , String.valueOf(errorNumber) ) ;
                    paramMap.put( "login_err_timestamp" , String.valueOf(dateTimestamp.getTime()) ) ;
                    this.logger.fine("Wrong password. Parameters" + paramMap.toString());
                    this.DBAction( "USERS" , "setLoginError" , paramMap ) ;
                    this.SSO_AUTH = false ;
                    returnAuthData.clear() ;
                    returnAuthData.put( "loginPassError" , "true" ) ;
                    this.SSO_AUTH_DATA = returnAuthData ;
                    return returnAuthData ;
                }
            } else {
                this.SSO_AUTH = false ;
                returnAuthData.clear() ;
                returnAuthData.put( "loginTooOften" , "true" ) ;
                System.out.println("DBMapping.Authenticate(): user " + TW_PRINCIPAL + " login disabled for 20 minutes");
                this.SSO_AUTH_DATA = returnAuthData ;
                return returnAuthData ;
            }
        } else {
            this.SSO_AUTH = false ;
            returnAuthData.clear() ;
            returnAuthData.put( "loginPassError" , "true" ) ;
            this.SSO_AUTH_DATA = returnAuthData ;
            return returnAuthData ;
        }
    }
    
    public boolean isAuthenticated() throws Exception {
        return this.SSO_AUTH ;
    }
    
    public Object getException() throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.getException called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getException" , arguments ) ;
            Object[] paramObjects = { } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException in DBMapping.getException(): "+e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException in DBMapping.getException(): "+e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException in DBMapping.getException(): "+e.getTargetException());
            throw new Exception( "EIDP Core System size: " + e.getTargetException() ) ;
        }
        return result ;
    }
    
    public void resetException() throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.resetException called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "resetException" , arguments ) ;
            Object[] paramObjects = { } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException in DBMapping.resetException(): "+e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException in DBMapping.resetException(): "+e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException in DBMapping.resetException(): "+e.getTargetException());
            throw new Exception( "EIDP Core System size: " + e.getTargetException() ) ;
        }
    }
    
    public void setException( Object o ) throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.setException called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { Object.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getRow" , arguments ) ;
            Object[] paramObjects = { o } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.severe("DBMapping: NoSuchMethodException in DBMapping.setException(): "+e);
            throw new Exception( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.severe("DBMapping: IllegalAccessException in DBMapping.setException(): "+e);
            throw new Exception( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.severe("DBMapping: InvocationTargetException in DBMapping.setException(): "+e.getTargetException());
            throw new Exception( "EIDP Core System getRow: " + e.getTargetException() ) ;
        }
    }
    
    /**
     * Standard EJB-Method.
     */
    public void ejbCreate() {
    }
    
    /**
     * Standard EJB-Method.
     */
    public void ejbActivate(){
//        try {
//            this.applicationInit() ;
//        } catch ( java.io.IOException ioee ) {
//            throw new javax.ejb.EJBException( "" + ioee ) ;
//        } catch ( java.sql.SQLException sqle ) {
//            throw new javax.ejb.EJBException( "" + sqle ) ;
//        } catch ( javax.ejb.CreateException ce ) {
//            throw new javax.ejb.EJBException( "" + ce ) ;
//        }
    }
    
    /**
     * Standard EJB-Method.
     */
    public void ejbPassivate(){
    }
    
    /**
     * Standard EJB-Method.
     */
    public void ejbRemove() {
        this.logger.info("DBMapping: Removing EJB");
        this.xmlDataAccess = null ;
        Set dsKey = this.dataSourceClasses.keySet() ;
        Iterator dsI = dsKey.iterator() ;
        while( dsI.hasNext() ) {
            String dsID = (String)dsI.next() ;
            try {
                Class  arguments[] = new Class[] { Logger.class } ;
                Method callMethod = ((Class)this.dataSourceClasses.get( dsID )).getMethod( "closeConnection" , arguments ) ;
                Object[] paramObjects = { this.logger } ;
                Object result = callMethod.invoke( (Object)this.dataSourceObjects.get( dsID ) , paramObjects );
            } catch ( java.lang.NoSuchMethodException e ) {
                this.logger.severe("DBMapping: NoSuchMethodException in DBMapping.ejbRemove: "+e);
                throw new javax.ejb.EJBException( "EIDP Core System Passivization: " + e ) ;
            } catch ( java.lang.IllegalAccessException e ) {
                this.logger.severe("DBMapping: IllegalAccessException in DBMapping.ejbRemove: "+e);
                throw new javax.ejb.EJBException( "EIDP Core System Passivization: " + e ) ;
            } catch ( java.lang.reflect.InvocationTargetException e ) {
                this.logger.severe("DBMapping: InvocationTargetException in DBMapping.ejbRemove: "+e.getTargetException());
                throw new javax.ejb.EJBException( "EIDP Core System Passivization: " + e.getTargetException() ) ;
            }
        }
        this.dataSourceClasses.clear() ;
        this.logger.removeHandler(fh);
    }
    
    /**
     * Standard EJB-Method.
     * @param sessionContext EJB SessionContext.
     * @throws EJBException
     * @throws RemoteException
     */
    public void setSessionContext(SessionContext sessionContext) throws javax.ejb.EJBException, java.rmi.RemoteException {
        this.context = sessionContext;
    }
    
    /**
     * Standard EJB-Method.
     */
    public void unsetSessionContext(){
        this.context = null;
    }
    
    /**
     * Special method for calls from WebApp.
     * @param httpSessionEvent Needs the httpSessionEvent.
     */
    public void sessionCreated( HttpSessionEvent httpSessionEvent ) {
    }
    
    /**
     * Special method for calls from WebApp.
     * @param httpSessionEvent Needs httpSessionEevent.
     */
    public void sessionDestroyed( HttpSessionEvent httpSessionEvent ) {
        this.ejbRemove() ;
    }
    
    private static byte[] encrypt(String inputString) throws java.security.DigestException , java.security.InvalidAlgorithmParameterException , java.security.NoSuchAlgorithmException , java.io.IOException, javax.servlet.ServletException {
        java.security.MessageDigest md = null;
        md = java.security.MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(inputString.getBytes( "ISO-8859-1" ));
        return  md.digest();
    }
    
    // block Serialization
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException("Class not serializable.");
    }
    
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException("Class not serializable.");
    }
    
}
