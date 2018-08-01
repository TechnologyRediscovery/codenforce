/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.integration;

import java.io.Serializable;
import java.sql.Connection;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
import java.sql.SQLException;
import com.tcvcog.tcvce.util.Constants;


/**
 *
 * @author cedba
 */

public class PostgresConnectionFactory implements Serializable{

    private Connection con = null;
    Jdbc3PoolingDataSource source;
//    private static final String DBUSERNAME = "cogdba";
//    private static final String DBPASS = "c0d3";
    
    
    /**
     * Creates a new instance of DBConnection
     */
    public PostgresConnectionFactory() {
        System.out.println("DBConnection.constructor - Creating Pooling Datasource");
        try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException ex) {
                System.out.println(ex.toString());
            }
        
        source = new Jdbc3PoolingDataSource();
        source.setServerName(Constants.SERVER_NAME);
        source.setDatabaseName(Constants.DB_NAME);
        source.setUser(Constants.DB_USERNAME);
        source.setPassword(Constants.DB_PASS);
        source.setMaxConnections(Constants.MAX_CONNECTIONS);
       
        
    } // close method
   

    /**
     * @return the con
     */
    public Connection getCon() {
        
        //System.out.println("PostGresConnectionFactor.getCon");
        
//        source.setDataSourceName("cogpgnew");
       
        try {
            con = source.getConnection();
        } catch (SQLException ex) {
            ex.toString();
        } finally {
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) { /* ignored */}
//             }
        
        } // close finally
        return con;
    }

    /**
     * @param con the con to set
     */
  
    public void setCon(Connection con){
        this.con = con;
    }
    
}
