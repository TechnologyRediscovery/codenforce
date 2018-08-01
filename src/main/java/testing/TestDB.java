/*
 * Copyright (C) 2017 Eric C. Darsow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package testing;

import com.tcvcog.tcvce.domain.ObjectNotFoundException;
import com.tcvcog.tcvce.entities.User;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

/**
 *
 * @author Eric C. Darsow
 */
public class TestDB {
    
    static Connection con = null;
    
    public static void main(String[] args) {
        
    
    
        try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException ex) {
                System.out.println(ex.toString());
            }
        
        Jdbc3PoolingDataSource source = new Jdbc3PoolingDataSource();
        source.setServerName("localhost:5432");
        source.setDatabaseName("cogdb");
        source.setUser("Eric C. Darsow");
        source.setPassword("c0d3");
        source.setMaxConnections(10);
        try {
            System.out.println("Trying connection");
            con = source.getConnection();
        } catch (SQLException ex) {
            ex.toString();
        }
        
        String query = "SELECT * FROM login; ";
        ResultSet rs;
        
        // login is successful if the result set has any rows in it
        // TODO: create value comparison check as a backup to avoid SQL injection risks
        try {
            System.out.println("down in try");
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            rs.next();
            System.out.println(rs.getString("username"));
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        }
        
        
    
}
}