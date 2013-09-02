package com.eidp.core.DB.modules;

import com.eidp.core.DB.DBMappingHomeRemote;
import com.eidp.core.DB.DBMappingRemote;
import com.eidp.xml.XMLDataAccess ;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.logging.*;

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

public class DataSourceDispatcher extends DataSourceMapping implements DataSourceAPI , java.io.Serializable {
    
    private DBMappingRemote dbMapper = null ;
    private XMLDataAccess xmlDataAccess = null ;
    
    private Object Exception = null ;
    
    
    public DataSourceDispatcher( String appContext , NodeList dataSourceNode , XMLDataAccess xmlda , Logger logger ) throws java.io.IOException , org.xml.sax.SAXException , java.sql.SQLException {
        logger.info("DataSourceDispatcher called");
        this.xmlDataAccess = xmlda ;
        String ejbReference = (String) ( (Vector)xmlda.getElementsByName( "ejb-ref" , dataSourceNode ) ).get( 0 ) ;
        String contextToCall = (String) ( (Vector)xmlda.getElementsByName( "context" , dataSourceNode )).get(0);
        logger.info("DataSourceDispatcher: Instantiating with parameters: ejb-ref="+ejbReference+" contextToCall: "+contextToCall);
        try {
            logger.info("DataSourceDispatcher: looking up remote DBMapping ("+ejbReference+"): "+contextToCall);            
            Properties prop = new Properties();
            Context jndiContext = new InitialContext() ;
            Object ref = jndiContext.lookup(ejbReference);
            logger.fine("DataSourceDispatcher: DBMapping object reference obtained");
            DBMappingHomeRemote home = (DBMappingHomeRemote) PortableRemoteObject.narrow(ref, DBMappingHomeRemote.class);
            logger.fine("DataSourceDispatcher: create dbMapper");
            this.dbMapper = home.create( appContext ) ;
            logger.fine("DataSourceDispatcher: dbMapper created");
        } catch ( javax.ejb.CreateException ce ) {
            logger.severe("DataSourceDispatcher: CreateException in DataSourceDispatcher: "+ce);
            throw new IOException( "CreateException by DataSourceDispatcher: " + "" + "" + ce ) ;
        } catch ( javax.naming.NamingException ne ) {
            logger.severe("DataSourceDispatcher: NamingException in DataSourceDispatcher: "+ne);
            throw new IOException( "Naming Exception by DataSourceDispatcher: " + ne ) ;
        } catch ( java.sql.SQLException sqle ) {
            logger.severe("DataSourceDispatcher: SQLException in DataSourceDispatcher: "+sqle);
            throw new IOException( "SQL Exception by DataSourceDispatcher: " + sqle ) ;
        } catch ( java.rmi.RemoteException re ) {
            logger.severe("DataSourceDispatcher: RemoteException in DataSourceDispatcher: "+re);
            throw new IOException( "RemoteException by DataSourceDispatcher: " + re ) ;
        } catch ( java.io.IOException ioe ) {
            logger.severe("DataSourceDispatcher: IOException in DataSourceDispatcher: "+ioe);
            throw new IOException( "IOException by DataSourceDispatcher: " + ioe ) ;
        } catch (java.lang.ClassCastException e){
            logger.severe("DataSourceDispatcher: ClassCastException in DataSourceDispatcher: "+e);
            throw new IOException("ClassCastExceptionb by DataSourceDispatcher: "+e);
        }
    }
    
    public void ProcessDBAction(NodeList dataSetNode, NodeList methodNode, HashMap paramMap , Logger logger) throws Exception, SAXException, IOException {
        String dataset = (String) ( (Vector)this.xmlDataAccess.getElementsByName( "name" , dataSetNode )).get( 0 ) ;
        String method = (String) ( (Vector)this.xmlDataAccess.getElementsByName( "name" , methodNode )).get(0) ;
        logger.fine("DataSourceDispatcher: Dispatching dataset: "+dataset+" method: "+method);
        this.dbMapper.DBAction( dataset , method , paramMap ) ;
    }
    
    public Vector getRowRange(Integer rowNumber, Integer endRow) {
        Vector returnVector = new Vector() ;
        try {
            returnVector = this.dbMapper.getRowRange( rowNumber.intValue() , endRow.intValue() ) ;
        } catch ( Exception e ) {
            this.setException( e ) ;
        }
        return returnVector ;
    }
    
    public HashMap getRow(Integer rowNumber) {
        HashMap returnMap = new HashMap() ;
        try {
            returnMap = this.dbMapper.getRow( rowNumber.intValue() ) ;
        } catch ( Exception e ) {
            this.setException(e);
        }
        return returnMap;
    }
    
    public Integer size() {
        int size = 0;
        try {
            size = this.dbMapper.size() ;
        } catch ( Exception e ) {
            this.setException(e);
        }
        return Integer.valueOf(String.valueOf(size));
    }
    
    public void closeConnection( Logger logger ) throws Exception {
        this.dbMapper.remove() ;
    }
    
    public HashMap Authenticate( String TW_PRINCIPAL , String TW_CREDENTIALS ) throws Exception {
        HashMap returnMap = new HashMap() ;
        try {
            returnMap = this.dbMapper.Authenticate( TW_PRINCIPAL , TW_CREDENTIALS ) ;
        } catch ( java.rmi.RemoteException e ) {
            this.setException(e);
        }
        return returnMap ;
    }
    
    public Object getException() {
        return this.Exception ;
    }
    
    public void setException(Object o) {
        this.Exception = o ;
    }
    
    public void resetException() {
        this.Exception = null ;
    }
    
    //	 block Serialization
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException("Class not serializable.");
    }
    
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException("Class not serializable.");
    }
    
}
