/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.integration;

import java.io.Serializable;
import java.sql.Connection;
//import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
import java.sql.SQLException;
import com.tcvcog.tcvce.util.Constants;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author cedba
 */

public class PostgresConnectionFactory implements Serializable{

    @Resource(name="jdbc/cogpg")
    private DataSource ds;
    private Connection con = null;
//    Jdbc3PoolingDataSource source;
//    private static final String DBUSERNAME = "cogdba";
//    private static final String DBPASS = "c0d3";
    
    
    /**
     * Creates a new instance of DBConnection
     */
    public PostgresConnectionFactory() {
        
//        source.setServerName(Constants.SERVER_NAME);
//        source.setDatabaseName(Constants.DB_NAME);
//        source.setUser(Constants.DB_USERNAME);
//        source.setPassword(Constants.DB_PASS);
//        source.setMaxConnections(Constants.MAX_CONNECTIONS);
//        source = new Jdbc3PoolingDataSource();
       
        
    } // close method
   

    /**
     * @return the con
     */
    public Connection getCon() {
        
        try {
            //System.out.println("PostGresConnectionFactor.getCon");
            
//        source.setDataSourceName("cogpgnew");

            con = source.getConnection();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        finally {
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
