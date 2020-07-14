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
 * @author Ellen Bascomb of apartment 31Y
 */
public enum PageModeEnum {
    
    LOOKUP        (   "LOOKUP", true, 3, false      ),
    INSERT        (   "INSERT", false, 4, false     ),
    UPDATE        (   "UPDATE", false, 4, true      ),
    REMOVE        (   "REMOVE", false, 5, true      );
                                                                
    private final String title;                                 
    private final boolean defaultMode;
    private final int minUserRankToEnable;
    private final boolean objectSelectRequiredToEnable;
                                                                
    private PageModeEnum(String t, boolean def, int rnk, boolean obreq){
        title = t;
        defaultMode = def;
        minUserRankToEnable = rnk;
        objectSelectRequiredToEnable = obreq;
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

    /**
     * @return the objectSelectRequiredToEnable
     */
    public boolean isObjectSelectRequiredToEnable() {
        return objectSelectRequiredToEnable;
    }


    
}
