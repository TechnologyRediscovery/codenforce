/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.RoleType;
import java.time.LocalDateTime;

/**
 * Containerization of data related SQL constraints, for use in lists
 * inside SearchParams objects, which are injected into QueryXXX objects
 * 
 * @author Ellen Bascomb
 * 
 */
public class SearchParamsDateRule {
    protected RoleType date_rtMin;
    
    protected IFace_dateFieldHolder date_field;
    protected LocalDateTime date_start_val;
    protected LocalDateTime date_end_val;
    
    protected boolean date_null_ctl;
    protected boolean date_null_val;

    protected boolean date_relativeDates_ctl;
    protected int date_relativeDates_start_val;
    protected int date_realtiveDates_end_val;

    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(date_field != null){
            sb.append("Field: " + date_field.extractDateFieldString());
        }
        sb.append("START: " + date_start_val);
        sb.append("END: " + date_end_val);
        sb.append("Null ctl: " + date_null_ctl);
        sb.append("Null val: " + date_null_val);
        sb.append("Rel Dates" + date_relativeDates_ctl);
        return sb.toString();
        
    }
    
    /**
     * @return the date_rtMin
     */
    public RoleType getDate_rtMin() {
        return date_rtMin;
    }

   

    /**
     * @return the date_field
     */
    public IFace_dateFieldHolder getDate_field() {
        return date_field;
    }

    /**
     * @return the date_start_val
     */
    public LocalDateTime getDate_start_val() {
        return date_start_val;
    }

    /**
     * @return the date_end_val
     */
    public LocalDateTime getDate_end_val() {
        return date_end_val;
    }

    /**
     * @return the date_null_ctl
     */
    public boolean isDate_null_ctl() {
        return date_null_ctl;
    }

    /**
     * @return the date_null_val
     */
    public boolean isDate_null_val() {
        return date_null_val;
    }


    /**
     * @return the date_relativeDates_ctl
     */
    public boolean isDate_relativeDates_ctl() {
        return date_relativeDates_ctl;
    }

    /**
     * @return the date_relativeDates_start_val
     */
    public int getDate_relativeDates_start_val() {
        return date_relativeDates_start_val;
    }

    /**
     * @return the date_realtiveDates_end_val
     */
    public int getDate_realtiveDates_end_val() {
        return date_realtiveDates_end_val;
    }

    /**
     * @param date_rtMin the date_rtMin to set
     */
    public void setDate_rtMin(RoleType date_rtMin) {
        this.date_rtMin = date_rtMin;
    }

   

    /**
     * @param date_field the date_field to set
     */
    public void setDate_field(IFace_dateFieldHolder date_field) {
        this.date_field = date_field;
    }

    /**
     * @param date_start_val the date_start_val to set
     */
    public void setDate_start_val(LocalDateTime date_start_val) {
        this.date_start_val = date_start_val;
    }

    /**
     * @param date_end_val the date_end_val to set
     */
    public void setDate_end_val(LocalDateTime date_end_val) {
        this.date_end_val = date_end_val;
    }

    /**
     * @param date_null_ctl the date_null_ctl to set
     */
    public void setDate_null_ctl(boolean date_null_ctl) {
        this.date_null_ctl = date_null_ctl;
    }

    /**
     * @param date_null_val the date_null_val to set
     */
    public void setDate_null_val(boolean date_null_val) {
        this.date_null_val = date_null_val;
    }


    /**
     * @param date_relativeDates_ctl the date_relativeDates_ctl to set
     */
    public void setDate_relativeDates_ctl(boolean date_relativeDates_ctl) {
        this.date_relativeDates_ctl = date_relativeDates_ctl;
    }

    /**
     * @param date_relativeDates_start_val the date_relativeDates_start_val to set
     */
    public void setDate_relativeDates_start_val(int date_relativeDates_start_val) {
        this.date_relativeDates_start_val = date_relativeDates_start_val;
    }

    /**
     * @param date_realtiveDates_end_val the date_realtiveDates_end_val to set
     */
    public void setDate_realtiveDates_end_val(int date_realtiveDates_end_val) {
        this.date_realtiveDates_end_val = date_realtiveDates_end_val;
    }
}
