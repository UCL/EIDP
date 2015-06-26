/*
 * DBMapping.java
 *
 * Created on April 28, 2003, 10:46 AM
 */

package com.eidp.core.DB;

import com.eidp.xml.XMLDataAccess;
import java.io.IOException;
import java.util.logging.*;
import javax.ejb.SessionContext;
import java.util.HashMap;
import java.util.Vector;
import java.util.Set ;
import java.util.Iterator ;
import java.lang.reflect.* ;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import org.w3c.dom.NodeList;
import javax.servlet.http.HttpSessionListener ;
import javax.servlet.http.HttpSessionEvent ;
import java.util.Date ;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.servlet.ServletException;
import org.xml.sax.SAXException;

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

@Stateful
@Remote(DBMappingRemote.class)
public class DBMapping implements HttpSessionListener, DBMappingRemote {
    
    /**
     * Session Context Method.
     */
    private final HashMap dataSourceClasses = new HashMap();
    private final HashMap dataSourceObjects = new HashMap() ;
    private String dataSourceIDCache = "default" ;
    private XMLDataAccess xmlDataAccess;   
    private String applicationContext = "" ;  
    private boolean SSO_AUTH = false ;   
    private HashMap SSO_AUTH_DATA = new HashMap() ;  
    private boolean AUTH = false ;
    private final Vector AUTH_EXCEPT = new Vector() ; 
    private final Vector AUTH_PROPAGATE = new Vector() ; 
    private Logger logger = null ;
    private final FileHandler fh = null ;
    private final int LOGIN_TIMEOUT = 120000 ;
    
    @Resource
    private SessionContext context;
    
    @Override
    public void setApplicationContext(String applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        this.logger = Logger.getLogger("com.eidp.core.DB.DBMapping."+this.applicationContext+"."+this.context.hashCode());
        // this.fh = new FileHandler("/com/eidp/logs/core/Core."+this.applicationContext+"."+this.context.hashCode()+".log");
        // this.logger.addHandler(fh);
        this.logger.setLevel(Level.INFO);
        this.logger.log(Level.INFO, "DBMapping: Instantiating Core-System on: {0} .", this.applicationContext);
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
                logger.log(Level.INFO, ">>> DBMapping: Log-level will be set to: {0}", logLevel);
                switch (logLevel) {
                    case "All":
                        this.logger.setLevel(Level.ALL);
                        break;
                    case "FINE":
                        this.logger.setLevel(Level.FINE);
                        break;
                    case "FINEST":
                        this.logger.setLevel(Level.FINEST);
                        break;
                    case "SEVERE":
                        this.logger.setLevel(Level.SEVERE);
                        break;
                    case "OFF":
                        this.logger.setLevel(Level.OFF);
                        break;
                    default:
                        this.logger.setLevel(Level.INFO) ;
                        break;
                }
            }
            Vector authElVector = this.xmlDataAccess.getElementsByName( "authentication" ) ;
            this.logger.fine("DBMapping: Getting Authentication Information.");
            if ( authElVector.size() > 0 ) {
                String authentication = (String)authElVector.get( 0 ) ;
                this.AUTH = authentication.equals( "true" );
                this.logger.log(Level.FINE, "DBMapping: Authentication = {0}", this.AUTH);
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
                this.logger.log(Level.FINE, "DBMapping: Datasource name = {0}", dnName);
                String dnClass = "DataBaseMapping" ;
                Vector dnClassVector = xmlDataAccess.getElementsByName( "class" , dataNode ) ;
                if ( dnClassVector.size() > 0 ) {
                    dnClass = (String)dnClassVector.get( 0 ) ;
                }
                this.logger.log(Level.FINE, "DBMapping: Datasource class = {0}", dnClass);
                // instantiate class
                this.logger.log(Level.FINE, "DBMapping: Instantiating <{0}>.", dnName);
                String dnClassInst = "com.eidp.core.DB.modules." + dnClass ;
                Class dnClassObject = Class.forName( dnClassInst ) ;
                Class[] paramClasses = { String.class , NodeList.class , XMLDataAccess.class , Logger.class } ;
                Constructor constr = dnClassObject.getConstructor( paramClasses ) ;
                Object[] paramObjects = { this.applicationContext, dataNode , this.xmlDataAccess , this.logger } ;
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
                        this.logger.log(Level.FINE, "DBMapping: Authentication propagation set for <{0}>.", dnName);
                    }
                } else this.logger.fine("DBMapping: No authentication propagation set.");
            }
            this.logger.info("DBMapping: Datasource information retrieval finished.");
        } catch ( org.xml.sax.SAXException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: SAXException thrown in DBMapping.applicationInit: {0}", e);
            throw new javax.ejb.EJBException( "DataNodeVector could not be created: " + e ) ;
        } catch ( javax.xml.parsers.ParserConfigurationException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: CreateException thrown in DBMapping.applicationInit: {0}", e);
            throw new javax.ejb.EJBException( "DataNodeVector could not be created: " + e ) ;
        } catch ( java.lang.ClassNotFoundException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: ClassNotFoundException thrown in DBMapping.applicationInit: {0}", e);
            throw new javax.ejb.EJBException( "Class not found at application init: " + e ) ;
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException thrown in DBMapping.applicationInit: {0}", e);
            throw new javax.ejb.EJBException( "No Such Method Exception at application init: " + e ) ;
        } catch ( java.lang.InstantiationException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InstantiationException thrown in DBMapping.applicationInit: {0}", e);
            throw new javax.ejb.EJBException( "Instantiation Exception at application init: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException thrown in DBMapping.applicationInit: {0}", e);
            throw new javax.ejb.EJBException( "Illegal Access Exception at application init: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException thrown in DBMapping.applicationInit: {0}", e.getTargetException());
            throw new javax.ejb.EJBException( "Invocation Target Exception at application init: " + e.getTargetException() ) ;
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
    @Override
    public void DBAction(String dataset, String method, HashMap paramMap) throws org.xml.sax.SAXException {
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
            this.logger.log(Level.SEVERE, "DBMapping: SAXException thrown when retrieving db,dataset: {0}", saxe);
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
                this.logger.log(Level.SEVERE, "DBMapping: Could not retrieve db,dataset,name.{0}", saxe);
                throw new javax.ejb.EJBException( "DBMapping - DBAction cannot find Elements from NodeList: " + saxe ) ;
            }
            if ( ((String)nameDataSet.get(0)).equals( dataset ) ) {
                this.logger.fine("DBMapping: Dataset found");
                dataSetNode = (NodeList)dataSetVector.get(ds_i) ;
                Vector dataSourceIDVector = new Vector() ;
                try {
                    dataSourceIDVector = this.xmlDataAccess.getElementsByName( "database-id" , (NodeList)dataSetVector.get( ds_i ) ) ;
                } catch ( org.xml.sax.SAXException saxe ) {
                    this.logger.log(Level.SEVERE, "DBMapping: Could not retrieve db,dataset,database-id: {0}", saxe);
                    throw new javax.ejb.EJBException( "Could not retrieve db,dataset,database-id: " + saxe ) ;
                }
                if ( dataSourceIDVector.size() > 0 ) {
                    dataSourceID = (String)dataSourceIDVector.get( 0 ) ;
                }
                break ; // breaks processing the for loop !
            }
            
        }
        this.logger.log(Level.FINE, "DBMapping: Datasource-ID = {0}", dataSourceID);
        // Now we have the dataSet in dataSetNodeList ;
        // Now look for the method in the dataSet:
        this.logger.fine("DBMapping: Retrieving method signatures (db,dataset,method): ");
        Vector methodVector = this.xmlDataAccess.getNodeListsByName( "method", dataSetNode )  ;
        for ( int m_i = 0 ; m_i < methodVector.size() ; m_i++ ) {
            // see for with dataSetVector for descritpion
            // use again nameRequestArray ( again "name" !!! )
            Vector nameDataSet = this.xmlDataAccess.getElementsByName( "name" , (NodeList)methodVector.get(m_i) ) ;
            if ( nameDataSet.isEmpty() ) {
                this.logger.severe("DBMapping: No method name given.");
                throw new javax.ejb.EJBException("No method given in db.xml");
            }
            if ( ((String)nameDataSet.get(0)).equals( method ) ) {
                methodNode = (NodeList)methodVector.get(m_i) ;
                break ; // breaks processing for loop !
            }
        }
        String methodSignature = dataset + "." + method ;
        this.logger.log(Level.FINE, "DBMapping: Checking method signature: {0} against AUTH_EXCEPT", methodSignature);
        if ( this.AUTH && ! this.AUTH_EXCEPT.contains(methodSignature) ) {
            Vector roleElements ;
            roleElements = this.xmlDataAccess.getElementsByName( "role-name" , methodNode );
            if ( roleElements.isEmpty() ) {
                this.logger.log(Level.FINE, "DBMapping: No roles defined in AUTH environment for <{0}>", methodSignature);
                throw new java.security.AccessControlException( "DBMapper: AccessControlException. No roles defined for Method: " +dataset+"."+ method+"." ) ;
            }
            Iterator ri = roleElements.iterator() ;
            boolean authOK = false ;
            this.logger.log(Level.FINE, "DBMapping: Checking role-authentication against: {0}", ((Vector)this.SSO_AUTH_DATA.get("userRoles")).toString());
            while ( ri.hasNext() ) {
                String roleName = (String)ri.next() ;
                // !!! System.out.println( "CHECKING <"+roleName+"> against SSO_AUTH_DATA.") ;
                this.logger.log(Level.FINE, "DBMapping: Checking <{0}> against SSO_AUTH_DATA.", roleName);
                if ( ((Vector)this.SSO_AUTH_DATA.get( "userRoles" )).contains( roleName ) ) {
                    this.logger.log(Level.FINE, "DBMapping: Giving <{0}> access to method: {1}", new Object[]{roleName, methodSignature});
                    authOK = true ;
                    break ;
                }
            }
            if ( authOK == false ) {
                this.logger.log(Level.SEVERE, "DBMapping: AccessControllException thrown. User not allowed to call method: {0}", methodSignature);
                throw new java.security.AccessControlException( "AccessControllException thrown. User not allowed to call method: "+methodSignature ) ;
            }
        }
        try {
            this.logger.log(Level.INFO, "DBMapping: Calling datasource-id: {0}", dataSourceID);
            Class  arguments[] = new Class[] { NodeList.class , NodeList.class , HashMap.class , Logger.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( dataSourceID )).getMethod( "ProcessDBAction" , arguments ) ;
            Object[] paramObjects = { dataSetNode , methodNode , paramMap , this.logger } ;
            Object result = callMethod.invoke( (Object)this.dataSourceObjects.get( dataSourceID ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in DBAction: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System ProcessDBAction: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in DBAction: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System ProcessDBAction: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in DBAction: {0}", e.getTargetException());
            throw new javax.ejb.EJBException( "EIDP Core System ProcessDBAction: " + e.getTargetException() ) ;
        }
        this.dataSourceIDCache = dataSourceID ;
    }
    
    /**
     * Get row of a ResultSet.
     * @param rowNumber RowNumber to be retrieved from the ResultSet.
     * @return HashMap of the requested row in the ResultSet.
     */
    @Override
    public HashMap getRow( int rowNumber ) {
        this.logger.finest("DBMapping: >>> DBMapping.getRow( int ) called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { Integer.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getRow" , arguments ) ;
            Object[] paramObjects = { new Integer( rowNumber ) } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in getRow: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in getRow: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in getRow: {0}", e.getTargetException());
            throw new javax.ejb.EJBException( "EIDP Core System getRow: " + e.getTargetException() ) ;
        }
        return (HashMap)result ;
    }
    
    /**
     * Get a range of Rows out of the ResultSet.
     * @param rowNumber Start with retrieval from rowNumber.
     * @param endRow Stop with retrieval.
     * @return Vector of Hashes.
     */
    @Override
    public Vector getRowRange( int rowNumber , int endRow ) {
        this.logger.finest("DBMapping: >>> DBMapping.getRowRange( int , int ) called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { Integer.class , Integer.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getRowRange" , arguments ) ;
            Object[] paramObjects = { new Integer( rowNumber ) , new Integer( endRow ) } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in getRow: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System getRowRange: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in getRow: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System getRowRange: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in getRow: {0}", e.getTargetException());
            throw new javax.ejb.EJBException( "EIDP Core System getRow: " + e.getTargetException() ) ;
        }
        return (Vector)result ;
    }
    
    /**
     * Retrieve the size of the cached ResultSet.
     * @return Returns an int for the size of the ResultSet.
     */
    @Override
    public int size() {
        this.logger.finest("DBMapping: >>> DBMapping.size() called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "size" , arguments ) ;
            Object[] paramObjects = {} ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in getRow: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in getRow: {0}", e);
            throw new javax.ejb.EJBException( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in getRow: {0}", e.getTargetException());
            throw new javax.ejb.EJBException( "EIDP Core System size: " + e.getTargetException() ) ;
        }
        return ((Integer)result).intValue() ;
    }
    
    @Override
    public HashMap Authenticate( String TW_PRINCIPAL , String TW_CREDENTIALS ) {
        this.logger.info("DBMapping: >>> DBMapping.Authenticate().");
        this.logger.log(Level.FINE, "DBMapping: CALL Parameters: PRINCIPAL = {0}; CREDENTIALS=---HIDDEN---", TW_PRINCIPAL);
        HashMap returnAuthData = new HashMap() ;
        HashMap paramMap = new HashMap() ;
        paramMap.put( "login" , TW_PRINCIPAL ) ;
        this.logger.fine("DBMapping: Calling USERS.getUserDataForLogin.");
        try {
            this.DBAction( "USERS" , "getUserDataForLogin" , paramMap ) ;
        } catch (SAXException ex) {
            this.logger.log(Level.SEVERE, "DBMapping: Exception in Authenticate.getUserDataForLogin: {0}", ex);
        } 
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
                try {
                    userInputPassword = new String( encrypt( TW_CREDENTIALS ) ) ;
                } catch (DigestException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IOException | ServletException ex) {
                    this.logger.log(Level.SEVERE, "DBMapping: Exception in Authenticate.encrypt: {0}", ex);
                }
                userInputPassword = javax.xml.bind.DatatypeConverter.printBase64Binary(userInputPassword.getBytes());
            }
            this.logger.fine("DBMapping: Checking passwords.");
            if(errorNumber < 4 || errorDateTimeStamp.before(dateTimestamp)){
                if(errorNumber >= 4 && errorDateTimeStamp.before(dateTimestamp)){
                    paramMap.put( "login_err_number" , "0" ) ;
                    paramMap.put( "login_err_timestamp" , String.valueOf(dateTimestamp.getTime()) ) ;
                    try {
                        this.DBAction( "USERS" , "setLoginError" , paramMap ) ;
                    } catch (SAXException ex) {
                        this.logger.log(Level.SEVERE, "DBMapping: Exception in Authenticate.setLoginError: {0}", ex);
                    }
                    errorNumber = 0;
                }
                if ( databasePassword.equals( userInputPassword ) || ( databasePassword.equals( "START_PASSWORD" ) && userInputPassword.equals( databasePassword ) ) ) {
                    this.logger.log(Level.FINE, "DBMapping: --- Password authentication fulfilled successfully for{0} ---", TW_PRINCIPAL);
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
                    this.logger.log(Level.FINE, "DBMapping: Password expired = {0}", passwordExpired);
                    returnAuthData.put( "passwordExpired" , passwordExpired ) ;
                    // 2. get UserRoles
                    this.logger.fine("DBMapping: Calling ROLES.getRolesForLogin");
                    try {
                        this.DBAction( "ROLES" , "getRolesForLogin" , paramMap ) ;
                    } catch (SAXException ex) {
                        this.logger.log(Level.SEVERE, "DBMapping: Exception in Authenticate.getRolesForLogin: {0}", ex);
                    }
                    if ( this.size() == 0 ) {
                        this.logger.log(Level.FINE, "DBMapping: Role Authentication not successfull for: {0} ---", returnAuthData.get( "userID" ));
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
                    this.logger.log(Level.FINE, "DBMapping: Roleset for user: {0}: {1}", new Object[]{TW_PRINCIPAL, userRoles.toString()});
                    returnAuthData.put( "userRoles" , userRoles ) ;
                    // 3. get Center Data
                    this.logger.fine("DBMapping: Calling CENTER_ROLES.getCentersForUser");
                    try {
                        this.DBAction( "CENTER_ROLES" , "getCentersForUser" , paramMap ) ;
                    } catch (SAXException ex) {
                        this.logger.log(Level.SEVERE, "DBMapping: Exception in Authenticate.getCentersForUser: {0}", ex);
                    }
                    if ( this.size() == 0 ) {
                        this.logger.log(Level.FINE, "DBMapping: Center authentication not successfull for: {0} ---", returnAuthData.get( "userID" ));
                        this.SSO_AUTH = false ;
                        returnAuthData.clear() ;
                        returnAuthData.put( "loginCenterRoleError" , "true" ) ;
                        this.SSO_AUTH_DATA = returnAuthData ;
                        return returnAuthData ;
                    }
                    HashMap userCenters = new HashMap() ;
                    for ( int i = 0 ; i < this.size() ; i++ ) {
                        String center;
                        String status;
                        String permission;
                        center = (String)((HashMap)this.getRow( i )).get( "center" ) ;
                        status = (String)((HashMap)this.getRow( i )).get( "status" ) ;
                        permission = (String)((HashMap)this.getRow( i )).get( "permission" ) ;
                        userCenters.put( center , permission ) ;
                        if ( status.equals( "m" ) ) {
                            returnAuthData.put( "userCenter" , center ) ;
                        }
                    }
                    this.logger.log(Level.FINE, "DBMapping: Centerset for user {0}: {1}", new Object[]{TW_PRINCIPAL, userCenters.toString()});
                    returnAuthData.put( "userCenters" , userCenters ) ;
                    this.SSO_AUTH = true ;
                    this.SSO_AUTH_DATA = returnAuthData ;
                    Iterator api = this.AUTH_PROPAGATE.iterator() ;
                    if (this.SSO_AUTH) this.logger.info("DBMapping: Getting into Authentication-Propagation");
                    while ( api.hasNext() ) {
                        String apName = (String)api.next() ;
                        this.logger.log(Level.FINE, "DBMapping: Authentication-Propagation for: {0}", apName);
                        try {
                            Class  arguments[] = new Class[] { String.class , String.class } ;
                            Method callMethod = ((Class)this.dataSourceClasses.get( apName )).getMethod( "Authenticate" , arguments ) ;
                            Object[] paramObjects = { TW_PRINCIPAL , TW_CREDENTIALS } ;
                            Object result = callMethod.invoke( (Object)this.dataSourceObjects.get( apName ) , paramObjects );
                            if ( ( (HashMap)result).isEmpty() ) {
                                this.logger.log(Level.SEVERE, "DBMapping: Could not retrieve remote authentication for: {0}", returnAuthData.get( "userID" ));
                                this.SSO_AUTH = false ;
                                returnAuthData.clear() ;
                                this.SSO_AUTH_DATA = returnAuthData ;
                                return returnAuthData ;
                            }
                        } catch ( java.lang.NoSuchMethodException e ) {
                            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in DBMapping.Authenticate(): {0}", e);
                            throw new javax.ejb.EJBException( "EIDP Core System ProcessDBAction: " + e ) ;
                        } catch ( java.lang.IllegalAccessException e ) {
                            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in DBMapping.Authenticate(): {0}", e);
                            throw new javax.ejb.EJBException( "EIDP Core System ProcessDBAction: " + e ) ;
                        } catch ( java.lang.reflect.InvocationTargetException e ) {
                            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in DBMapping.Authenticate(): {0}", e.getTargetException());
                            throw new javax.ejb.EJBException( "EIDP Core System ProcessDBAction: " + e.getTargetException() ) ;
                        }   
                    }
                    paramMap.put( "login_err_number" , "0" ) ;
                    paramMap.put( "login_err_timestamp" , String.valueOf(dateTimestamp.getTime()) ) ;
                    try {
                        this.DBAction( "USERS" , "setLoginError" , paramMap ) ;
                    } catch (SAXException ex) {
                        this.logger.log(Level.SEVERE, "DBMapping: Exception in Authenticate.setLoginError: {0}", ex);
                    }
                    return returnAuthData ;
                } else {
                    errorNumber++;
                    paramMap.put( "login_err_number" , String.valueOf(errorNumber) ) ;
                    paramMap.put( "login_err_timestamp" , String.valueOf(dateTimestamp.getTime()) ) ;
                    this.logger.log(Level.FINE, "Wrong password. Parameters{0}", paramMap.toString());
                    try {
                        this.DBAction( "USERS" , "setLoginError" , paramMap ) ;
                    } catch (SAXException ex) {
                        this.logger.log(Level.SEVERE, "DBMapping: Exception in Authenticate.setLoginError: {0}", ex);
                    }
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
    
    @Override
    public boolean isAuthenticated() {
        return this.SSO_AUTH ;
    }
    
    @Override
    public Object getException() throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.getException called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getException" , arguments ) ;
            Object[] paramObjects = { } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in DBMapping.getException(): {0}", e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in DBMapping.getException(): {0}", e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in DBMapping.getException(): {0}", e.getTargetException());
            throw new Exception( "EIDP Core System size: " + e.getTargetException() ) ;
        }
        return result ;
    }
    
    @Override
    public void resetException() throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.resetException called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "resetException" , arguments ) ;
            Object[] paramObjects = { } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in DBMapping.resetException(): {0}", e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in DBMapping.resetException(): {0}", e);
            throw new Exception( "EIDP Core System size: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in DBMapping.resetException(): {0}", e.getTargetException());
            throw new Exception( "EIDP Core System size: " + e.getTargetException() ) ;
        }
    }
    
    @Override
    public void setException( Object o ) throws Exception {
        this.logger.finest("DBMapping: >>> DBMapping.setException called.");
        Object result = null ;
        try {
            Class  arguments[] = new Class[] { Object.class } ;
            Method callMethod = ((Class)this.dataSourceClasses.get( this.dataSourceIDCache )).getMethod( "getRow" , arguments ) ;
            Object[] paramObjects = { o } ;
            result = callMethod.invoke( (Object)this.dataSourceObjects.get( this.dataSourceIDCache ) , paramObjects );
        } catch ( java.lang.NoSuchMethodException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in DBMapping.setException(): {0}", e);
            throw new Exception( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.IllegalAccessException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in DBMapping.setException(): {0}", e);
            throw new Exception( "EIDP Core System getRow: " + e ) ;
        } catch ( java.lang.reflect.InvocationTargetException e ) {
            this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in DBMapping.setException(): {0}", e.getTargetException());
            throw new Exception( "EIDP Core System getRow: " + e.getTargetException() ) ;
        }
    }
    
    @Remove
    @Override
    public void remove() {
        this.logger = Logger.getLogger("com.eidp.core.DB.DBMapping."+this.applicationContext+"."+this.context.hashCode());
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
                this.logger.log(Level.SEVERE, "DBMapping: NoSuchMethodException in DBMapping.ejbRemove: {0}", e);
                throw new javax.ejb.EJBException( "EIDP Core System Passivization: " + e ) ;
            } catch ( java.lang.IllegalAccessException e ) {
                this.logger.log(Level.SEVERE, "DBMapping: IllegalAccessException in DBMapping.ejbRemove: {0}", e);
                throw new javax.ejb.EJBException( "EIDP Core System Passivization: " + e ) ;
            } catch ( java.lang.reflect.InvocationTargetException e ) {
                this.logger.log(Level.SEVERE, "DBMapping: InvocationTargetException in DBMapping.ejbRemove: {0}", e.getTargetException());
                throw new javax.ejb.EJBException( "EIDP Core System Passivization: " + e.getTargetException() ) ;
            }
        }
        this.dataSourceClasses.clear() ;
        this.logger.removeHandler(fh);
    }
    
    /**
     * Special method for calls from WebApp.
     * @param httpSessionEvent Needs the httpSessionEvent.
     */
    @Override
    public void sessionCreated( HttpSessionEvent httpSessionEvent ) {
    }
    
    /**
     * Special method for calls from WebApp.
     * @param httpSessionEvent Needs httpSessionEevent.
     */
    @Override
    public void sessionDestroyed( HttpSessionEvent httpSessionEvent ) {
        this.remove() ;
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
