/*
 * Copyright (C) 2017 cedba
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
package com.tcvcog.tcvce.resultsets;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import java.sql.*;
import java.util.ArrayList;
import javax.faces.component.UISelectItem;
import java.util.HashMap;
import com.tcvcog.tcvce.util.Constants;

/**
 *
 * @author cedba
 */
public class PublicViolationSet extends BackingBeanUtils {
    
      private HashMap<String, Integer> violationMap = new HashMap<>();
      
   
      
      public PublicViolationSet(){
          
      }

    /**
     * @return the violationMap
     */
    public HashMap<String, Integer> getViolationMap() {
        
        
        
        Connection con = getPostgresCon();
        String query = "SELECT issueTypeID, typeName FROM public.actionRqstIssueType;";
        ResultSet rs = null;
 
        try {
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                violationMap.put(rs.getString("typeName"), rs.getInt("issueTypeID"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } // end try/catch
        return violationMap;
    }

    /**
     * @param violationMap the violationMap to set
     */
    public void setViolationMap(HashMap<String, Integer> violationMap) {
        this.violationMap = violationMap;
    }
}
