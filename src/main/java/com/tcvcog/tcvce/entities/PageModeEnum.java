/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities;

/**
 * Marks an Event as existing in either the CodeEnforcement
 * or Occupancy domain; used for routing in Integration and
 * tallying of event stuff
 * 
 * @author Ellen Bascomb
 */
public enum PageModeEnum {
    
    LOOKUP        (   "LOOKUP", true, 3             ),
    INSERT        (   "INSERT", false, 4            ),
    UPDATE        (   "UPDATE", false, 4            ),
    REMOVE        (   "REMOVE", false, 5            );
                                                                
    private final String title;                                 
    private final boolean defaultMode;
    private final int minUserRankToEnable;
                                                                
    private PageModeEnum(String t, boolean def, int rnk){
        title = t;
        defaultMode = def;
        minUserRankToEnable = rnk;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the dbField
     */
    public boolean getDefaultMode() {
        return defaultMode;
    }
    
    public int getMinUserRankToEnable(){
        return minUserRankToEnable;
    }


    
}
