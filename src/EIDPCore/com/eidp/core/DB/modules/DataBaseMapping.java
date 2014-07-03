/*
 * DataBaseMapping.java
 *
 * Created on 5. April 2005, 10:33
 */

package com.eidp.core.DB.modules;

import com.eidp.xml.XMLDataAccess ;

import javax.naming.Context;
import javax.naming.InitialContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import javax.sql.DataSource;
import java.sql.Statement;

import javax.transaction.UserTransaction ;

import java.util.HashMap;
import java.util.Vector;
import java.util.Set ;
import java.util.Iterator ;

import org.w3c.dom.NodeList;

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
public class DataBaseMapping extends DataSourceMapping implements DataSourceAPI , java.io.Serializable {
    
    // block Serialization am Ende der Klasse!!!
    private Connection connection = null; // serializable
    
    private DataSource dataSource = null ; // serializable
    private transient UserTransaction ut = null ; // Serializisation problem!!!
    
    private String dbType = "Jdbc" ; // serializable
    
    private transient Object exception = null ; // Serializisation problem!!!
    
    /** Creates a new instance of DataBaseMapping */
    public DataBaseMapping( String appContext , NodeList dataSourceNode , XMLDataAccess xmlda , Logger logger ) throws java.io.IOException , org.xml.sax.SAXException , java.sql.SQLException {
        logger.info("DataBaseMapping called");
        this.applicationContext = appContext ;
        this.xmlDataAccess = xmlda ;
        Vector dbTypeVector = (Vector)this.xmlDataAccess.getElementsByName( "type" , dataSourceNode ) ;
        if ( dbTypeVector.size() > 0 ) {
            this.dbType = (String)dbTypeVector.get( 0 ) ;
        }
        logger.fine("DataBaseMapping: dbType="+this.dbType);
        if ( this.dbType.equals( "Jdbc" ) ) {
            logger.fine("DataBaseMapping: Trying to get JDBC connection.");
            this.database.put( "driver" , (Vector)this.xmlDataAccess.getElementsByName( "driver" , dataSourceNode ) );
            this.database.put( "host" , (Vector)this.xmlDataAccess.getElementsByName( "host" , dataSourceNode ) );
            this.database.put( "instance-name" , (Vector)this.xmlDataAccess.getElementsByName( "instance-name" , dataSourceNode ) );
            this.database.put( "user" , (Vector)this.xmlDataAccess.getElementsByName( "user" , dataSourceNode ) );
            this.database.put( "password" , (Vector)this.xmlDataAccess.getElementsByName( "password" , dataSourceNode ) );
            logger.fine("DataBaseMapping: Parameters retrieved for connection: "+this.database.toString());
            this.getConnection(logger);
        } else if ( this.dbType.equals( "Pool" ) ) {
            logger.fine("DataBaseMapping: Trying to get POOL connection.");
            this.database.put( "jndi-datasource" , (String)((Vector)this.xmlDataAccess.getElementsByName( "jndi-datasource" , dataSourceNode )).get( 0 ) ) ;
            this.database.put( "jndi-transaction" , (String)((Vector)this.xmlDataAccess.getElementsByName( "jndi-transaction" , dataSourceNode )).get( 0 ) ) ;
            logger.fine("DataBaseMapping: Parameters retrieved for connection: "+this.database.toString());
            this.getConnection(logger) ;
        }
    }
    
    public void ProcessDBAction(NodeList dataSetNode, NodeList methodNode, HashMap paramMap , Logger logger) throws Exception, org.xml.sax.SAXException, java.io.IOException {
        logger.info("DataBaseMapping: ProcessDBAction called.");
        Vector returnVector = new Vector();
        Set paramKeys = paramMap.keySet() ;
        Iterator pi = paramKeys.iterator() ;
        while ( pi.hasNext() ) {
            String key = (String)pi.next() ;
            String value = (String)paramMap.get( key ) ;
            if ( value != null ) {
                value = value.replace( '\'' , '-' ) ;
                paramMap.put( key , value ) ;
            }
        }
        String tableName = (String)((Vector)this.xmlDataAccess.getElementsByName( "table,name" , dataSetNode )).get(0) ;
        // 2. get type of method and direct it to the matching method
        String methodType = (String)((Vector)this.xmlDataAccess.getElementsByName( "type" , methodNode )).get(0) ;
        String dataSetName = (String)((Vector)this.xmlDataAccess.getElementsByName( "name" , dataSetNode )).get(0) ;
        String methodName = (String)((Vector)this.xmlDataAccess.getElementsByName( "name" , methodNode )).get(0) ;
        logger.fine( "DataBaseMapping: Dataset: " + dataSetName + "; Method: " + methodName + " Method-Type: "+methodType+"; ParamMap: " + paramMap.toString() ) ;
        // process the specific methods for type:
        if ( methodType.equals( "get" ) ) this.get( tableName , dataSetNode , methodNode , paramMap , logger );
        else if ( methodType.equals( "set" ) ) this.set( tableName , dataSetNode , methodNode , paramMap , logger );
        else if ( methodType.equals( "remove" ) ) this.remove( tableName , dataSetNode , methodNode , paramMap , logger ) ;
        // return returnVector ;
    }
    
    private void getConnection( Logger logger ) throws java.sql.SQLException , java.io.IOException {
        if ( this.dbType.equals( "Jdbc" ) ) {
            Vector driver = (Vector)this.database.get("driver") ;
            Vector host = (Vector)( this.database.get( "host" ) ) ;
            Vector instance = (Vector)( this.database.get( "instance-name" ) ) ;
            String url = (String)host.get(0) + "/" + (String)instance.get(0) ;
            Vector user = (Vector)( this.database.get( "user" ) ) ;
            Vector password = (Vector)( this.database.get( "password" ) ) ;
            try {
                Class.forName( (String)driver.get(0) );
                this.connection = DriverManager.getConnection( (String)url , (String)user.get(0) , (String)password.get(0) );
                logger.fine("JDBC Connection created");
            } catch ( java.lang.ClassNotFoundException ce ) {
                logger.severe("ClassNotFoundException in DatabaseMapping.getConnection: "+ce);
                throw new javax.ejb.EJBException(ce);
            } catch ( java.lang.ClassCastException cce ) {
                logger.severe("ClassCastException in DatabaseMapping.getConnection: "+cce);
                throw new javax.ejb.EJBException( "ClassCastException thrown from DBMapping.getConnection: " + cce ) ;
            }
            
        } else if ( this.dbType.equals( "Pool" ) ) {
            try {
                Context cntx = new InitialContext() ;
                this.dataSource = (DataSource)cntx.lookup( (String)this.database.get( "jndi-datasource" ) );
                this.ut = (UserTransaction)cntx.lookup( (String)this.database.get( "jndi-transaction" ) ) ;
            } catch ( javax.naming.NamingException e ) {
                logger.severe("NamingException in DatabaseMapping.getConnection: "+e);
                throw new java.sql.SQLException( "Could not instantiate Connection Pool: " + e ) ;
            }
        }
    }
    
    public void closeConnection( Logger logger ) throws Exception , java.sql.SQLException {
        logger.info( "DataBaseMapping: trying to close database connection" ) ;
        if ( this.dbType.equals( "Jdbc" ) ) {
            this.connection.close() ;
        }
        this.connection = null ;
        logger.info("DataBaseMapping: database connection closed.");
    }
    
    public HashMap getRow( Integer rowNumber ) {
        return (HashMap)this.resultVector.get( rowNumber.intValue() ) ;
    }
    
    public Vector getRowRange( Integer rowNumber, Integer endRow ) {
        if ( endRow.intValue() > this.resultVector.size() ) {
            throw new javax.ejb.EJBException( "DBMapping - get: endRow exceeds size of ResultVector" ) ;
        }
        Vector returnVector = new Vector() ;
        for ( int i = rowNumber.intValue() ; i < endRow.intValue() ; i++ ) {
            returnVector.addElement( (HashMap)this.resultVector.get(i) ) ;
        }
        return returnVector;
    }
    
    public Integer size() {
        return new Integer( this.resultVector.size() ) ;
    }
    
    public void setException( Object o ) {
        this.exception = o ;
    }
    
    public Object getException() {
        Object e = this.exception ;
        return e ;
    }
    
    public void resetException() {
        this.exception = null ;
    }
    
    private void get(String tableName, NodeList dataSetNode, NodeList methodNode, HashMap paramMap , Logger logger ) throws Exception , org.xml.sax.SAXException , java.io.IOException {
        logger.info( "DataBaseMapping: Processing GET" ) ;
        Vector resultSet = new Vector() ;
        // - selectIDs
        Vector selectIDs = (Vector)this.xmlDataAccess.getElementsByName( "query,field" , methodNode ) ;
        Vector fieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector fieldNames = (Vector)this.xmlDataAccess.getElementsByName( "table,field,name" , dataSetNode ) ;
        Vector fieldTypes = (Vector)this.xmlDataAccess.getElementsByName( "table,field,type" , dataSetNode ) ;
        HashMap fieldMap = this.getFields( fieldIDs , fieldNames ) ;
        logger.fine("DataBaseMapping: FieldMap: "+fieldMap.toString());
        HashMap typeMap = this.getFields( fieldIDs , fieldTypes ) ;
        logger.fine("DataBaseMapping: TypeMap: "+typeMap.toString());
        HashMap selectFields = new HashMap();
        for ( int s_i = 0 ; s_i < selectIDs.size() ; s_i++ ) {
            selectFields.put( (String)selectIDs.get( s_i ) , (String)fieldMap.get( (String)selectIDs.get(s_i) ) ) ;
        }
        logger.fine("DataBaseMapping: SelectFields: "+selectFields.toString());
        // - forFields
        Vector forVector = (Vector)this.xmlDataAccess.getNodeListsByName( "for" , (NodeList)methodNode ) ;
        Vector forFieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "for,field" , methodNode ) ;
        Vector forOperators = (Vector)this.xmlDataAccess.getElementsByName( "for,operator" , methodNode ) ;
        Vector forTypes = (Vector)this.xmlDataAccess.getElementsByName( "for,type" , methodNode ) ;
        logger.fine("DataBaseMapping: FOR fields: "+forFieldIDs.toString());
        if ( forFieldIDs.size() != forOperators.size() ) {
            logger.severe("DataBaseMapping: Number of field-ids does not correlate to the number of operators given.");
            throw new org.xml.sax.SAXException( "DBMapping getFor throws XML Exception: fields do not match database columns" ) ;
        }
        Vector forFields = new Vector() ;
        for ( int f_i = 0 ; f_i < forFieldIDs.size() ; f_i++ ) {
            int intCloseBracketsNumber = 0 ;
            int intOpenBracketsNumber = 0 ;
            Vector openBrackets = (Vector)this.xmlDataAccess.getElementsByName( "open-brackets" , (NodeList)forVector.get( f_i ) ) ;
            if( openBrackets.size() > 0 ){
                String strOpenBracketsNumber = (String)((Vector)this.xmlDataAccess.getElementsByName( "open-brackets" , (NodeList)forVector.get( f_i ) ) ).get( 0 ) ;
                try{
                    intOpenBracketsNumber = Integer.parseInt( strOpenBracketsNumber.trim() ) ;
                }catch(NumberFormatException e){}
            }
            Vector closeBrackets = (Vector)this.xmlDataAccess.getElementsByName( "close-brackets" , (NodeList)forVector.get( f_i ) ) ;
            if( closeBrackets.size() > 0 ){
                String strCloseBracketsNumber = (String)((Vector)this.xmlDataAccess.getElementsByName( "close-brackets" , (NodeList)forVector.get( f_i ) ) ).get( 0 ) ;
                try{
                    intCloseBracketsNumber = Integer.parseInt( strCloseBracketsNumber.trim() ) ;
                }catch(NumberFormatException e){}
            }
            String forKey = (String)fieldMap.get( (String)forFieldIDs.get( f_i ) ) ;
            logger.fine("DataBaseMapping: Processing FOR-KEY: "+forKey);
            String forOperator = (String)forOperators.get( f_i ) ;
            String forType = (String)forTypes.get(f_i) ;
            String forFieldType = (String)typeMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forValue = new String();
            if ( forOperator.equals("in") ) {
                forOperator = " in " ;
                if ( forFieldType.equals("String") || forFieldType.equals("Date") || forFieldType.equals("Time") ) {
                    forValue = "('" + (String)paramMap.get( (String)forFieldIDs.get(f_i) ) + "')" ;
                    forValue = forValue.replaceAll( "," , "','" ) ;
                } else {
                    forValue = "(" + (String)paramMap.get( (String)forFieldIDs.get(f_i) ) + ")" ;
                }
            } else if ( forOperator.equals("gt") || forOperator.equals("get") || forOperator.equals("lt") || forOperator.equals("let") || forOperator.equals("equal") || forOperator.equals("notequal") || forOperator.equals("like") || forOperator.equals("isnull") || forOperator.equals("isnotnull") || forOperator.equals("raw") ) {
                String origForValue = (String)paramMap.get( (String)forFieldIDs.get(f_i) ) ;
                boolean isLike = false ;
                if ( forOperator.equals("gt") ) { forOperator = " > " ;} else if (forOperator.equals("get") ) { forOperator = " >= " ; } else if ( forOperator.equals("lt") ) { forOperator = " < " ; } else if ( forOperator.equals("let") ) { forOperator = " <= " ; } else if ( forOperator.equals("equal") ) { forOperator = " = " ; } else if ( forOperator.equals("notequal") ) { forOperator = " != " ; } else if ( forOperator.equals("like") ) {
                    forOperator = " like " ;
                    forValue = origForValue.replace( '*' , '%' ) ;
                    Vector searchType = (Vector)this.xmlDataAccess.getElementsByName( "for,easysearch" , methodNode ) ;
                    Vector searchCaseSen = (Vector)this.xmlDataAccess.getElementsByName( "for,case" , methodNode ) ;
                    if( searchType.size() > 0 ){
                        String strEasySearch = (String)searchType.get( 0 );
                        if(strEasySearch.equals("true")){
                            forValue = "%" + forValue + "%" ;
                        }
                        if( searchCaseSen.size() > 0 ){
                            String strCaseSensitivity = (String)searchCaseSen.get( 0 ) ;;
                            if(strCaseSensitivity.equals("upper")){
                                forValue = forValue.toUpperCase() ;
                            }else if(strCaseSensitivity.equals("lower")){
                                forValue = forValue.toLowerCase() ;
                            }
                        }
                    }else{
                        
                    }
                    isLike = true ;
                }else if ( forOperator.equals("isnull") ) {
                    forOperator = " is null" ;
                }else if ( forOperator.equals("isnotnull") ) {
                    forOperator = " is null" ;
                    forKey = "not " + forKey;
                }else if ( forOperator.equals("raw") ) {
                    String rawOperator = (String)((Vector)this.xmlDataAccess.getElementsByName( "for,raw" , (NodeList)forVector.get( f_i ) ) ).get( 0 ) ;
                    forOperator = " " + rawOperator + " ";
                    Vector searchCaseSen = (Vector)this.xmlDataAccess.getElementsByName( "for,case" , methodNode ) ;
                    if( searchCaseSen.size() > 0 ){
                            String strCaseSensitivity = (String)searchCaseSen.get( 0 ) ;;
                            if(strCaseSensitivity.equals("upper")){
                                origForValue = origForValue.toUpperCase() ;
                            }else if(strCaseSensitivity.equals("lower")){
                                origForValue = origForValue.toLowerCase() ;
                            }
                        }
                }
                if ( isLike == false ) {
                    forValue = origForValue ;
                }
                if ( forFieldType.equals("Date") || forFieldType.equals("String") || forFieldType.equals("Time") ) {
                    forValue = "'" + forValue + "'" ;
                }
            }
            
            if ( forOperator.equals(" is null") ){
                for(int j = 0 ; j < intOpenBracketsNumber ; j++ )
                    forKey = "(" + forKey ;
                for(int j = 0 ; j < intCloseBracketsNumber ; j++ )
                    forOperator = forOperator + ")" ;
                if ( f_i > 0 ) { forKey = " " + forType + " " + forKey ; }
                forFields.addElement( forKey + forOperator ) ;
            }else{
                for(int j = 0 ; j < intOpenBracketsNumber ; j++ )
                    forKey = "(" + forKey ;
                for(int j = 0 ; j < intCloseBracketsNumber ; j++ )
                    forValue = forValue + ")" ;
                if ( f_i > 0 ) { forKey = " " + forType + " " + forKey ; }
                forFields.addElement( forKey + forOperator + forValue ) ;
            }
            logger.fine("DataBaseMapping: ForKey: "+forKey+" ForValue: "+forValue+" ForType: "+forType+" fieldType: "+forFieldType);
        }
        // Order construction
        logger.fine("DataBaseMapping: Processing order instructions");
        Vector orderFieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "order,field" , methodNode ) ;
        Vector orderSortings = (Vector)this.xmlDataAccess.getElementsByName( "order,sorting" , methodNode ) ;
        if ( orderFieldIDs.size() != orderSortings.size() ) { throw new org.xml.sax.SAXException( "DBMapping getFor throws XML Exception: fields do not match database columns" ) ; }
        Vector orderFields = new Vector() ;
        for ( int o_i = 0 ; o_i < orderFieldIDs.size() ; o_i++ ) {
            orderFields.addElement( fieldMap.get( (String)orderFieldIDs.get(o_i) ) + " " + orderSortings.get(o_i) ) ;
        }
        logger.fine("DataBaseMapping: order-instructions: "+orderFields.toString());
        String statement = new String();
        // 1. Combine selectFields
        String selectColumns = (String)(selectFields.values()).toString() ;
        selectColumns = selectColumns.substring( 1 , selectColumns.length() - 1 ) ;
        statement += selectColumns ;
        // put in tablename
        statement += " FROM " + tableName ;
        // 3. Combine forFields if exists
        if ( forFields.size() > 0 ) statement += " WHERE" ;
        for ( int fi = 0 ; fi < forFields.size() ; fi++ ) {
            statement += " " + (String)forFields.get( fi ) ;
        }
        // 4. Combine orderFields if exists
        for ( int oi = 0 ; oi < orderFields.size() ; oi++ ) {
            if ( oi == 0 ) {
                statement += " ORDER BY " ;
                statement += (String)orderFields.get( oi ) ;
            } else {
                statement += " , " + orderFields.get( oi ) ;
            }
        }
        logger.info("DataBaseMapping: Select-Statement: SELECT "+statement);
        this.ProcessSelectStatement( statement , selectIDs , fieldMap , typeMap , logger ) ;
    }
    
    private void set( String tableName , NodeList dataSetNode , NodeList methodNode , HashMap paramMap , Logger logger ) throws Exception , org.xml.sax.SAXException , java.io.IOException {
        logger.fine("DataBaseMapping: SET");
        // ===> updateStmnt and insertStmnt are different.
        // updateStmnt uses a for clause. If this forClause does not turn into an update procedure,
        // the insertStmnt is executed. Howoever, the insert stmnt does also insert the values
        // in the forClause. Therefore the forClause is also put into the value-clause!!!
        Vector resultSet = new Vector() ;
        // === RquestArrays & other Vars
        String insertStmntKeys = "" ;
        String insertStmntValues = "" ;
        String updateStatement = new String() ;
        String insertStatement = new String() ;
        updateStatement = " " + tableName + " SET " ;
        insertStatement = " " + tableName + " " ;
        String primaryKey = (String)((Vector)this.xmlDataAccess.getElementsByName( "table,primary-key" , dataSetNode )).get(0) ;
        String primaryKeyGeneration = (String)((Vector)this.xmlDataAccess.getElementsByName( "table,primary-key-generation" , dataSetNode )).get(0) ;
        logger.fine("DataBaseMapping: Primary-Key: "+primaryKey);
        logger.fine("DataBaseMapping: Primary-Key-Generation: "+primaryKeyGeneration);
        // === Vectors & HashMaps
        Vector queryIDs = (Vector)this.xmlDataAccess.getElementsByName( "values,field" , methodNode ) ;
        Vector fieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector fieldNames = (Vector)this.xmlDataAccess.getElementsByName( "table,field,name" , dataSetNode ) ;
        Vector fieldTypes = (Vector)this.xmlDataAccess.getElementsByName( "table,field,type" , dataSetNode ) ;
        Vector fieldSizes = (Vector)this.xmlDataAccess.getElementsByName( "table,field,size" , dataSetNode ) ;
        HashMap fieldMap = this.getFields( fieldIDs , fieldNames ) ;
        HashMap typeMap = this.getFields( fieldIDs , fieldTypes ) ;
        HashMap sizeMap = this.getFields( fieldIDs , fieldSizes ) ;
        logger.fine("DataBaseMapping: FieldMap: "+fieldMap.toString());
        logger.fine("DataBaseMapping: TypeMap: "+typeMap.toString());
        HashMap queryFields = new HashMap();
        HashMap queryValues = new HashMap();
        // ==========================
        // === Business Logic
        // Query Fields (value clauses)
        for ( int q_i = 0 ; q_i < queryIDs.size() ; q_i++ ) {
            logger.fine("DataBaseMapping: Query-Field: "+(String)queryIDs.get( q_i ));
            queryFields.put( (String)queryIDs.get( q_i ) , (String)fieldMap.get( (String)queryIDs.get(q_i) ) ) ;
        }
        // paramMap checkValues:
        for ( int p_i = 0 ; p_i < queryIDs.size() ; p_i++ ) {
            String queryKey = (String)queryIDs.get( p_i ) ;
            String queryFieldSize = (String)sizeMap.get( (String)queryIDs.get( p_i ) ) ;
            String queryFieldType = (String)typeMap.get( (String)queryIDs.get( p_i ) ) ;
            String queryValue = (String)paramMap.get( (String)queryIDs.get( p_i ) ) ;
            try {
                if ( queryValue.length() > Integer.parseInt( queryFieldSize ) ) {
                    logger.severe("DataBaseMapping: Number of Query-Field-Values does not correspond to the given number of Query-Field-Sizes.");
                    throw new javax.ejb.EJBException( "Value-Size exception thrown in DBMapping: The Value of " + queryKey + "is too big." ) ;
                }
            } catch ( java.lang.NullPointerException npe ) {
                logger.fine( "DataBaseMapping: Cannot get queryValue for " + queryKey + ", seems to be NULL ( if ( queryValue.length() > Integer.parseInt( queryFieldSize ) ) ).") ;
                if ( queryFieldType.equals( "Date" ) ) queryValue = "9999-12-31";
                else if ( queryFieldType.equals( "Time" ) ) queryValue = "00:00:00";
                else if ( queryFieldType.equals( "String" ) ) queryValue = "" ;
                else if ( queryFieldType.equals( "Float" ) ) queryValue = "null";
                else if ( queryFieldType.equals( "Integer" ) ) queryValue = "null";
                else if ( queryFieldType.equals( "Double" ) ) queryValue = "null" ;
                else if ( queryFieldType.equals( "Long" ) ) queryValue = "null" ;
            }
            if ( queryFieldType.equals( "String" ) && queryValue.equals( "null" ) ) queryValue = "" ;
            else if ( queryFieldType.equals( "Date" ) && ( queryValue.equals( "null" ) || queryValue.equals( "" ) ) ) queryValue = "9999-12-31" ;
            else if ( queryFieldType.equals( "Time" ) && ( queryValue.equals( "null" ) || queryValue.equals( "" ) ) ) queryValue = "00:00:00" ;
            else if ( queryFieldType.equals( "Float" ) && ( queryValue.equals( "null" ) || queryValue.equals( "" ) ) ) queryValue = "null" ;
            else if ( queryFieldType.equals( "Integer" ) && ( queryValue.equals( "null" ) || queryValue.equals( "" ) ) ) queryValue = "null" ;
            else if ( queryFieldType.equals( "Double" ) && ( queryValue.equals( "null" ) || queryValue.equals( "" ) ) ) queryValue = "null" ;
            else if ( queryFieldType.equals( "Long" ) && ( queryValue.equals( "null" ) || queryValue.equals( "" ) ) ) queryValue = "null" ;
            // Checks String, Date or Time and puts '' around them. ;
            if ( queryFieldType.equals("String") || queryFieldType.equals("Date") || queryFieldType.equals("Time") ) {
                queryValue = "'" + queryValue + "'" ;
            }
            queryValues.put( queryKey , queryValue ) ;
            logger.fine("DataBaseMapping: <"+queryKey+"="+queryValue+">");
        }
        // forFields
        Vector forFieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "for,field" , methodNode ) ;
        Vector forOperators = (Vector)this.xmlDataAccess.getElementsByName( "for,operator" , methodNode ) ;
        Vector forTypes = (Vector)this.xmlDataAccess.getElementsByName( "for,type" , methodNode ) ;
        logger.fine("DataBaseMapping: For-Field-IDs: "+forFieldIDs.toString());
        logger.fine("DataBaseMapping: For-Field-Operators: "+forOperators.toString());
        logger.fine("DataBaseMapping: For-Field-Types: "+forTypes.toString());
        if ( forFieldIDs.size() != forOperators.size() ) { throw new org.xml.sax.SAXException( "DBMapping getFor throws XML Exception: fields do not match database columns" ) ; }
        Vector forFields = new Vector() ;
        for ( int f_i = 0 ; f_i < forFieldIDs.size() ; f_i++ ) {
            String forKey = (String)fieldMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forOperator = (String)forOperators.get( f_i ) ;
            String forType = (String)forTypes.get(f_i) ;
            String forFieldType = (String)typeMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forValue = new String();
            if ( forOperator.equals("in") ) {
                forOperator = " in " ;
                if ( forFieldType.equals("String") || forFieldType.equals("Date") || forFieldType.equals("Time") ) {
                    forValue = "('" + (String)paramMap.get( (String)forFieldIDs.get(f_i) ) + "')" ;
                    forValue = forValue.replaceAll( "," , "','" ) ;
                } else {
                    forValue = "(" + (String)paramMap.get( (String)forFieldIDs.get(f_i) ) + ")" ;
                }
            } else if ( forOperator.equals("gt") || forOperator.equals("get") || forOperator.equals("lt") || forOperator.equals("let") || forOperator.equals("equal") || forOperator.equals("like") ) {
                if ( forOperator.equals("gt") ) { forOperator = " > " ;} else if (forOperator.equals("get") ) { forOperator = " => " ; } else if ( forOperator.equals("lt") ) { forOperator = " < " ; } else if ( forOperator.equals("let") ) { forOperator = " =< " ; } else if ( forOperator.equals("equal") ) { forOperator = " = " ; } else if ( forOperator.equals("like") ) { forOperator = " like " ; }
                forValue = (String)paramMap.get( (String)forFieldIDs.get(f_i) ) ;
                if ( forFieldType.equals("Date") || forFieldType.equals("String") || forFieldType.equals("Time") ) {
                    forValue = "'" + forValue + "'" ;
                }
                if ( forFieldType.equals( "String" ) && forValue.equals( "'null'" ) ) { forValue = "''" ; } else if ( forFieldType.equals( "Date" ) && ( forValue.equals( "'null'" ) || forValue.equals( "''" ) ) ) { forValue = "'9999-12-31'" ; } else if ( forFieldType.equals( "Time" ) && ( forValue.equals( "'null'" ) || forValue.equals( "''" ) ) ) { forValue = "'00:00:00'" ; } else if ( forFieldType.equals( "Float" ) && ( forValue.equals( "null" ) || forValue.equals( "" ) ) ) { forValue = "0.0" ; } else if ( forFieldType.equals( "Integer" ) && ( forValue.equals( "null" ) || forValue.equals( "" ) ) ) { forValue = "0" ; } else if ( forFieldType.equals( "Double" ) && ( forValue.equals( "null" ) || forValue.equals( "" ) ) ) { forValue = "0" ; } else if ( forFieldType.equals( "Long" ) && ( forValue.equals( "null" ) || forValue.equals( "" ) ) ) { forValue = "0" ; }
                // else forValue = null ; (automatically!!!)
            }
            if ( f_i > 0 ) forFields.addElement( forType + " " + forKey + " " + forOperator + " " + forValue ) ;
            else forFields.addElement( forKey + " "  + forOperator + " " + forValue ) ;
            // the insert also needs the for values!
            if ( ! ((String)forFieldIDs.get( f_i )).equals( primaryKey ) ) {
                insertStmntKeys += " " + forKey + " , " ;
                insertStmntValues += " " + forValue + " , " ;
            }
            logger.fine("DataBaseMapping: ForKey: "+forKey+" ForOperator: "+forOperator+" ForValue: "+forValue);
        }
        // 1. Combine queryFields
        for ( int b_s = 0 ; b_s < queryIDs.size() ; b_s++ ) {
            String stmntKey = (String)queryFields.get( (String)queryIDs.get( b_s ) ) ;
            String stmntValue = (String)queryValues.get( (String)queryIDs.get( b_s ) ) ;
            updateStatement += stmntKey + " = " + stmntValue ;
            if ( queryIDs.size() > 0 && b_s < queryIDs.size() - 1 ) {
                updateStatement += " , " ;
            }
            if ( ((String)queryIDs.get( b_s )).equals( primaryKey ) ) {
                continue ;
            } else {
                insertStmntKeys += queryFields.get( (String)queryIDs.get( b_s ) ) ;
                insertStmntValues += queryValues.get( (String)queryIDs.get( b_s ) ) ;
                if ( queryIDs.size() > 0 && b_s < queryIDs.size() - 1 ) {
                    insertStmntKeys += " , " ;
                    insertStmntValues += " , " ;
                }
            }
        }
        // 2. do the rest with inserStatement and updateStatement.
        insertStmntKeys += " , " + (String)fieldMap.get( primaryKey ) ;
        
        // Workaround for supporting PostgreSQL
        insertStmntValues += " , " + this.generatePrimaryKeyValue( primaryKeyGeneration , tableName, primaryKey ) ;
        // 3. Combine forFields if exists
        String forColumns = "" ;
        for ( int fi = 0 ; fi < forFields.size() ; fi++ ) {
            forColumns += (String)forFields.get( fi ) + " " ;
        }
        // statement = statement to be executed
        if ( ! forColumns.equals( "" ) ) {
            updateStatement += " WHERE " + forColumns ;
        }
        insertStatement += "( " + insertStmntKeys + " ) values ( " + insertStmntValues + " ) " ;
        logger.fine("DataBaseMapping: Update-Statement: UPDATE "+updateStatement);
        logger.fine("DataBaseMapping: Insert-Statement: INSERT "+insertStatement);
        this.ProcessUpdateInsertStatement( updateStatement , insertStatement , logger ) ;
    }
    
    private void remove( String tableName , NodeList dataSetNode , NodeList methodNode , HashMap paramMap , Logger logger ) throws org.xml.sax.SAXException , java.io.IOException {
        logger.info("DataBaseMapping: REMOVE");
        String removeStatement = "" ;
        Vector fieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "table,field,id" , dataSetNode ) ;
        Vector fieldNames = (Vector)this.xmlDataAccess.getElementsByName( "table,field,name" , dataSetNode ) ;
        Vector fieldTypes = (Vector)this.xmlDataAccess.getElementsByName( "table,field,type" , dataSetNode ) ;
        HashMap fieldMap = this.getFields( fieldIDs , fieldNames ) ;
        HashMap typeMap = this.getFields( fieldIDs , fieldTypes ) ;
        logger.fine("DataBaseMapping: FieldMap: "+fieldMap.toString());
        logger.fine("DataBaseMapping: TypeMap: "+typeMap.toString());
        // forFields
        Vector forFieldIDs = (Vector)this.xmlDataAccess.getElementsByName( "for,field" , methodNode ) ;
        Vector forOperators = (Vector)this.xmlDataAccess.getElementsByName( "for,operator" , methodNode ) ;
        Vector forTypes = (Vector)this.xmlDataAccess.getElementsByName( "for,type" , methodNode ) ;
        logger.fine("DataBaseMapping: ForFieldIDs: "+forFieldIDs.toString());
        logger.fine("DataBaseMapping: ForFieldOperators: "+forOperators.toString());
        logger.fine("DataBaseMapping: ForFieldTypes: "+forTypes.toString());
        if ( forFieldIDs.size() != forOperators.size() ) { throw new org.xml.sax.SAXException( "DBMapping getFor throws XML Exception: fields do not match database columns" ) ; }
        Vector forFields = new Vector() ;
        for ( int f_i = 0 ; f_i < forFieldIDs.size() ; f_i++ ) {
            String forKey = (String)fieldMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forOperator = (String)forOperators.get( f_i ) ;
            String forType = (String)forTypes.get(f_i) ;
            String forFieldType = (String)typeMap.get( (String)forFieldIDs.get( f_i ) ) ;
            String forValue = new String();
            if ( forOperator.equals("in") ) {
                forOperator = " in " ;
                if ( forFieldType.equals("String") || forFieldType.equals("Date") ) {
                    forValue = "('" + (String)paramMap.get( (String)forFieldIDs.get(f_i) ) + "')" ;
                    forValue = forValue.replaceAll( "," , "','" ) ;
                } else {
                    forValue = "(" + (String)paramMap.get( (String)forFieldIDs.get(f_i) ) + ")" ;
                }
            } else if ( forOperator.equals("gt") || forOperator.equals("get") || forOperator.equals("lt") || forOperator.equals("let") || forOperator.equals("equal") ) {
                if ( forOperator.equals("gt") ) { forOperator = ">" ;} else if (forOperator.equals("get") ) { forOperator = "=>" ; } else if ( forOperator.equals("lt") ) { forOperator = "<" ; } else if ( forOperator.equals("let") ) { forOperator = "=<" ; } else if ( forOperator.equals("equal") ) { forOperator = "=" ; }
                forValue = (String)paramMap.get( (String)forFieldIDs.get(f_i) ) ;
                if ( forFieldType.equals("Date") || forFieldType.equals("String") ) {
                    forValue = "'" + forValue + "'" ;
                }
            }
            if ( f_i > 0 ) { forKey = forType + " " + forKey ; }
            forFields.addElement( " " + forKey + " " + forOperator + " " + forValue ) ;
            logger.fine("DataBaseMapping: forKey: "+forKey+" forOperator: "+forOperator+" forType: "+forType);
        }
        removeStatement += " FROM " + tableName ;
        // 3. Combine forFields if exists
        if ( forFields.size() > 0 ) {
            String forColumns = (String)forFields.toString() ;
            forColumns = forColumns.substring( 1 , forColumns.length() - 1 ) ;
            removeStatement += " WHERE " + forColumns.replace(",  ", " ");
        }
        logger.fine("DataBaseMapping: removeStatement = DELETE " + removeStatement ) ;
        this.ProcessRemoveStatement( removeStatement , logger ) ;
    }
    
    // =====================
    // Process concrete DB Actions
    
    private void ProcessSelectStatement( String statement , Vector selectIDs , HashMap fieldMap , HashMap typeMap , Logger logger ) throws java.io.IOException {
        // statement = statement to be executed
        // selectFields = fields that shall be collected (
        logger.fine("DataBaseMapping: ProcessSelectStatement called" ) ;
        try {
            statement = "SELECT " + statement ;
            if ( this.dbType.equals( "Pool" ) ) {
                this.connection = this.dataSource.getConnection() ;
            }
            Statement stmnt = null ;
            stmnt = this.connection.createStatement() ;
            this.resultVector.clear() ;
            ResultSet rs = stmnt.executeQuery( statement ) ;
            while ( rs.next() ) {
                HashMap resultMap = new HashMap() ;
                for ( int i = 0 ; i < selectIDs.size() ; i++ ) {
                    if ( ((String)typeMap.get( (String)selectIDs.get(i) )).equals("String") ) {
                        String value = rs.getString( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ;
                        try {
                            if ( value.trim().equalsIgnoreCase( "null" ) ) { value = "" ; }
                        } catch ( java.lang.NullPointerException npe ) { value = "" ; }
                        resultMap.put( (String)selectIDs.get(i) , value ) ;
                    } else if ( ((String)typeMap.get( (String)selectIDs.get(i) )).equals("Integer")  ) {
                        String strInt = "" ;
                        int tmpInt = rs.getInt( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ;
                        if ( rs.wasNull() ) { strInt = "" ; } else { strInt = String.valueOf( tmpInt) ; }
                        resultMap.put( (String)selectIDs.get(i) , strInt ) ;
                    } else if ( ((String)typeMap.get( (String)selectIDs.get(i) )).equals("Double")  ) {
                        String strDouble = "" ;
                        double tmpDouble = rs.getDouble( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ;
                        if ( rs.wasNull() ) { strDouble = "" ; } else { strDouble = String.valueOf( tmpDouble ) ; }
                        resultMap.put( (String)selectIDs.get(i) , strDouble ) ;
                    } else if ( ((String)typeMap.get( (String)selectIDs.get(i) )).equals("Long")  ) {
                        String strLong = "" ;
                        long tmpLong = rs.getLong( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ;
                        if ( rs.wasNull() ) { strLong = "" ; } else { strLong = String.valueOf( tmpLong ) ; }
                        resultMap.put( (String)selectIDs.get(i) , strLong ) ;
                    } else if ( ((String)typeMap.get( (String)selectIDs.get(i) )).equals("Float")  ) {
                        String strFloat = "" ;
                        float tmpFloat = rs.getFloat( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ;
                        if ( rs.wasNull() ) { strFloat = "" ; } else { strFloat = String.valueOf( tmpFloat ) ; }
                        resultMap.put( (String)selectIDs.get(i) , strFloat ) ;
                    } else if ( ((String)typeMap.get( (String)selectIDs.get(i) )).equals("Date") ) {
                        String strDate = rs.getString( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ;
                        try {
                            if ( strDate.trim().equalsIgnoreCase( "null" ) ) { strDate = "" ; }
                        } catch ( java.lang.NullPointerException npe ) { strDate = "" ; }
                        resultMap.put( (String)selectIDs.get(i) , strDate ) ;
                    } else if ( ((String)typeMap.get( (String)selectIDs.get(i) )).equals("Time") ) {
                        String strTime = rs.getString( (String)fieldMap.get( (String)selectIDs.get(i) ) ) ;
                        try {
                            if ( strTime.trim().equalsIgnoreCase( "null" ) ) { strTime = "00:00:00" ; }
                        } catch ( java.lang.NullPointerException npe ) { strTime = "00:00:00" ; }
                        resultMap.put( (String)selectIDs.get(i) , strTime ) ;
                    }
                }
                this.resultVector.addElement( resultMap ) ;
            }
            if ( this.dbType.equals( "Pool" ) ) {
                this.connection.close() ;
            }
            logger.fine("DataBaseMapping:    ------------------------   " ) ;
            logger.fine("DataBaseMapping: Size of resultVector = " + this.resultVector.size() ) ;
            logger.fine("DataBaseMapping: ResultVector: " + this.resultVector.toString() ) ;
            logger.fine("DataBaseMapping:    ------------------------   " ) ;
        } catch ( java.sql.SQLException e ) {
            logger.severe("DataBaseMapping: SQLException in DataBaseMapping.ProcessSelectStatment:"+e ) ;
            this.setException( e ) ;
        }
    }
    
    private void ProcessUpdateInsertStatement( String updateStatement , String insertStatement , Logger logger  ) throws java.io.IOException {
        logger.fine("DataBaseMapping: ProcessUpdateInsertStatement called" ) ;
        try {
            this.resultVector.clear() ;
            updateStatement = "UPDATE " + updateStatement ;
            insertStatement = "INSERT INTO " + insertStatement ;
            if ( this.dbType.equals( "Pool" ) ) {
                this.connection = this.dataSource.getConnection() ;
            }
            int rs;
            Statement stmnt = this.connection.createStatement() ;
            rs = stmnt.executeUpdate( updateStatement ) ;
            if ( rs == 0 ) {
                rs = stmnt.executeUpdate( insertStatement ) ;
            }
            if ( this.dbType.equals( "Pool" ) ) {
                this.connection.close() ;
            }
        } catch ( java.sql.SQLException e ) {
            logger.severe("DataBaseMapping: SQLException in DataBaseMapping.ProcessUpdateInsertStatement: "+e ) ;
            this.setException( e ) ;
        }
    }
    
    private void ProcessRemoveStatement( String removeStatement , Logger logger ) throws java.io.IOException {
        logger.fine("DataBaseMapping: ProcessRemoveStatement called" ) ;
        try {
            this.resultVector.clear() ;
            removeStatement = "DELETE " + removeStatement ;
            if ( this.dbType.equals( "Pool" ) ) {
                this.connection = this.dataSource.getConnection() ;
            }
            int rs ;
            Statement stmnt = this.connection.createStatement() ;
            rs = stmnt.executeUpdate( removeStatement ) ;
            if ( this.dbType.equals( "Pool" ) ) {
                this.connection.close() ;
            }
        } catch ( java.sql.SQLException e ) {
            logger.severe("DataBaseMapping: SQLException in DataBaseMapping.ProcessRemoveStatement: "+e ) ;
            this.setException( e ) ;
        }
    }
    
    protected String generatePrimaryKeyValue( String primaryKeyGeneration, String tableName, String primaryKey ) {
        if ( primaryKeyGeneration.equals( "GUID-TO-FILL" ) ) {
            return "'GUID-TO-FILL'" ;
        } else if ( primaryKeyGeneration.equals( "AUTO-STRING" ) ) {
            return "'AUTO-STRING'" ;
        } else if ( primaryKeyGeneration.equals( "SERIAL" ) ) {
            return "0" ;
        } else if ( primaryKeyGeneration.equals( "SERIAL-SEQ") ) { // Support for PostgreSQL through sequences
            return "nextval('" + tableName.toLowerCase() + "_" + primaryKey + "_seq')";
        }
        return "" ;
    }
    
    // block Serialization
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException("Class not serializable.");
    }
    
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException("Class not serializable.");
    }
    
}
