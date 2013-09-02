/*
 * Db.java
 *
 * Created on March 26, 2004, 1:09 PM
 */

package com.eidp.webctrl.modules;

import java.io.*;
import java.sql.*;
/**
 *
 * @author  rusch
 */
public class Db {
    
    /** Creates a new instance of Db */
    public Db() {
    }
    
    public ResultSet getAllTableNames(Connection conn){
        String sql = "SELECT table_name from user_tables order by table_name";
        
        ResultSet rs = null;
        try{
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            //System.out.println(sql);
        } catch(SQLException e){
            System.out.println("getAllTableNames: " + e);
        }
        return rs;
    }
    
    public ResultSet getAllViewNames(Connection conn){
        String sql = "select viewname, definition from viewdefs order by viewname";
        
        ResultSet rs = null;
        try{
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            //System.out.println(sql);
        } catch(SQLException e){
            System.out.println("getAllTableNames: " + e);
        }
        return rs;
    }
    
    public String getViewDefinition(Connection conn, String viewName){
        String sql = "select definition from viewdefs where viewname = '" + viewName + "'";
        String retStr = "";
        ResultSet rs = null;
        try{
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            retStr = rs.getString(1);
//            System.out.println(retStr);
        } catch(SQLException e){
            System.out.println("getViewDefinition: " + e);
        }
        return retStr;
    }
    
    public boolean isView(Connection conn, String viewName){
        String sql = "select definition from viewdefs where viewname = '" + viewName + "'";
        String retStr = "";
        ResultSet rs = null;
        boolean boolret = true;
        try{
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            retStr = rs.getString(1);
//            System.out.println(retStr);
        } catch(SQLException e){
            System.out.println("isView: " + e);
        }
        
        if( retStr.equals("") )
            boolret = false;
        
        return boolret;
    }
    
    public void addColumn(Connection conn, String tableName, String columnName, String collumnFormat){
        String sql = "ALTER TABLE " + tableName + " ADD (" + columnName + " " + collumnFormat + ")";
        
        try{
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println(sql);
        } catch(SQLException e){
            System.out.println("addColumn: " + e);
        }
        
    }
    
    public void dropColumn(Connection conn, String tableName, String columnName){
        String sql = "ALTER TABLE " + tableName + " DROP " + columnName;
        
        try{
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println(sql);
        } catch(SQLException e){
            System.out.println("dropColumn: " + e);
        }
        
    }
    
    public void updateColumn(Connection conn, String tableName, String columnName, String columnValue){
        String sql = "UPDATE " + tableName + " SET " + columnName + " = " + columnValue;
        
        try{
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch(SQLException e){
            System.out.println("updateColumn: " + e);
        }
    }
    
    public void updateColumn(Connection conn, String tableName, String columnName, Date columnValue){
        String sql = "UPDATE " + tableName + " SET " + columnName + " = " + columnValue;
        
        try{
            //System.out.println(sql);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch(SQLException e){
            System.out.println("updateColumn: " + e);
        }
    }
    
    public void updateColumn(Connection conn, String tableName, String columnName, Time columnValue){
        String sql = "UPDATE " + tableName + " SET " + columnName + " = " + columnValue;
        
        try{
            //System.out.println(sql);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch(SQLException e){
            System.out.println("updateColumn: " + e);
        }
    }
    
    public void modifyColumn(Connection conn, String tableName, String columnName, String columnFormat){
        String sql = "ALTER TABLE " + tableName + " MODIFY (" + columnName + " " + columnFormat + ")";
        
        try{
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println(sql);
        } catch(SQLException e){
            System.out.println("modifyColumn: " + e);
        }
    }
    
    public void renameColumn(Connection conn, String tableName, String oldColumnName, String newColumnName){
        String sql = "RENAME COLUMN " + tableName + "." + oldColumnName + " TO " + newColumnName;
        
        try{
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println(sql);
        } catch(SQLException e){
            System.out.println("renameColumn: " + e);
        }
    }
    
    public void renameTable(Connection conn, String oldTableName, String newTableName){
        String sql = "RENAME TABLE " + oldTableName + " TO " + newTableName;
        
        try{
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println(sql);
        } catch(SQLException e){
            System.out.println("renameTable: " + e);
        }
    }
    
    public boolean insertTable(Connection conn, String sql){
        
        try{
            //System.out.println(sql);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            
            return true;
        } catch(SQLException e){
            System.out.println("insertTable: " + e);
            return false;
        }
    }
    
    public boolean updateTable(Connection conn, String sql){
        
        try{
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println(sql);
            return true;
        } catch(SQLException e){
            System.out.println("updateTable: " + e);
            return false;
        }
    }
    
    public String getPrimaryKey(Connection conn, String tableName){
        
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select COLUMNNAME from " + tableName + " where mode = 'key'");
            return rs.getString("COLUMNNAME");
        } catch(SQLException e){
            return "";
        }
        
    }
    
}
