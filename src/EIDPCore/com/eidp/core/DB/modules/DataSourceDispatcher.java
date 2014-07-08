package com.eidp.core.DB.modules;

import com.eidp.core.DB.DBMappingRemote;
import com.eidp.xml.XMLDataAccess ;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
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
    
    
    public DataSourceDispatcher( String appContext , NodeList dataSourceNode , XMLDataAccess xmlda , Logger logger ) throws java.io.IOException , org.xml.sax.SAXException {
        logger.info("DataSourceDispatcher called");
        this.xmlDataAccess = xmlda ;
        String ejbReference = (String) ( (Vector)xmlda.getElementsByName( "ejb-ref" , dataSourceNode ) ).get( 0 ) ;
        String contextToCall = (String) ( (Vector)xmlda.getElementsByName( "context" , dataSourceNode )).get(0);
        logger.log(Level.INFO, "DataSourceDispatcher: Instantiating with parameters: ejb-ref={0} contextToCall: {1}", new Object[]{ejbReference, contextToCall});
        try {
            logger.log(Level.INFO, "DataSourceDispatcher: looking up remote DBMapping ({0}): {1}", new Object[]{ejbReference, contextToCall});            
            Properties prop = new Properties();
            Context jndiContext = new InitialContext() ;
            logger.fine("DataSourceDispatcher: DBMapping object reference obtained");
            logger.fine("DataSourceDispatcher: create dbMapper");
            this.dbMapper = (DBMappingRemote) jndiContext.lookup(ejbReference);
            logger.fine("DataSourceDispatcher: dbMapper created");
            this.dbMapper.setApplicationContext(appContext);            
        } catch ( javax.naming.NamingException ne ) {
            logger.log(Level.SEVERE, "DataSourceDispatcher: NamingException in DataSourceDispatcher: {0}", ne);
            throw new IOException( "Naming Exception by DataSourceDispatcher: " + ne ) ;
        } catch ( java.rmi.RemoteException re ) {
            logger.log(Level.SEVERE, "DataSourceDispatcher: RemoteException in DataSourceDispatcher: {0}", re);
            throw new IOException( "RemoteException by DataSourceDispatcher: " + re ) ;
        } catch ( java.io.IOException ioe ) {
            logger.log(Level.SEVERE, "DataSourceDispatcher: IOException in DataSourceDispatcher: {0}", ioe);
            throw new IOException( "IOException by DataSourceDispatcher: " + ioe ) ;
        } catch (java.lang.ClassCastException e){
            logger.log(Level.SEVERE, "DataSourceDispatcher: ClassCastException in DataSourceDispatcher: {0}", e);
            throw new IOException("ClassCastExceptionb by DataSourceDispatcher: "+e);
        }
    }
    
    @Override
    public void ProcessDBAction(NodeList dataSetNode, NodeList methodNode, HashMap paramMap , Logger logger) throws Exception, SAXException, IOException {
        String dataset = (String) ( (Vector)this.xmlDataAccess.getElementsByName( "name" , dataSetNode )).get( 0 ) ;
        String method = (String) ( (Vector)this.xmlDataAccess.getElementsByName( "name" , methodNode )).get(0) ;
        logger.log(Level.FINE, "DataSourceDispatcher: Dispatching dataset: {0} method: {1}", new Object[]{dataset, method});
        this.dbMapper.DBAction( dataset , method , paramMap ) ;
    }
    
    @Override
    public Vector getRowRange(Integer rowNumber, Integer endRow) {
        Vector returnVector = new Vector() ;
        try {
            returnVector = this.dbMapper.getRowRange( rowNumber.intValue() , endRow.intValue() ) ;
        } catch ( Exception e ) {
            this.setException( e ) ;
        }
        return returnVector ;
    }
    
    @Override
    public HashMap getRow(Integer rowNumber) {
        HashMap returnMap = new HashMap() ;
        try {
            returnMap = this.dbMapper.getRow( rowNumber.intValue() ) ;
        } catch ( Exception e ) {
            this.setException(e);
        }
        return returnMap;
    }
    
    @Override
    public Integer size() {
        int size = 0;
        try {
            size = this.dbMapper.size() ;
        } catch ( Exception e ) {
            this.setException(e);
        }
        return Integer.valueOf(String.valueOf(size));
    }
    
    @Override
    public void closeConnection( Logger logger ) throws Exception {
        this.dbMapper.remove() ;
    }
    
    public HashMap Authenticate( String TW_PRINCIPAL , String TW_CREDENTIALS ) {
        HashMap returnMap = new HashMap() ;
        returnMap = this.dbMapper.Authenticate( TW_PRINCIPAL , TW_CREDENTIALS ) ;
        return returnMap ;
    }
    
    @Override
    public Object getException() {
        return this.Exception ;
    }
    
    @Override
    public void setException(Object o) {
        this.Exception = o ;
    }
    
    @Override
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
