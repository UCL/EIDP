/*
 * Connect.java
 *
 * Created on March 9, 2004, 9:37 AM
 */

/**
 *
 * @author  rusch
 */

package com.eidp.webctrl.modules;

import java.io.*;
import java.sql.*;

public class Connect {
    
    private Connection connection = null;
    
    private String driver = "com.sap.dbtech.jdbc.DriverSapDB";
    
    /** Creates a new instance of Connect */
    public Connect() {
        
    }
    
    /** Returns the Connection-Object, default for MaxDB */
    public Connection getConn(String user, String password, String host, String dbname){
        String url = "jdbc:sapdb://" + host + "/" + dbname;
        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch(ClassNotFoundException e){
            System.out.println(e);
        } catch(SQLException e){
            System.out.println(e);
        }
        return connection;
    }
    
    /** Returns the Connection-Object for other vendors */
    public Connection getConn(String user, String password, String host, String dbname, String vendor, String aDriver) {
        String url = "jdbc:" + vendor + "://" + host + "/" + dbname;
        try{
            Class.forName(aDriver);
            connection = DriverManager.getConnection(url, user, password);
        } catch(ClassNotFoundException e){
            System.out.println(e);
        } catch(SQLException e){
            System.out.println(e);
        }
        return connection;
    }
    
}