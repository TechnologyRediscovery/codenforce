/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccPermitType;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class        SearchParamsOccPermit 
        extends     SearchParams {
    
    public static final String MUNI_DBFIELD = "municipality.municode";
    
    
    private boolean permitTypeList_ctl;
    private List<OccPermitType> permitTypeList_val;
    
    private boolean draft_ctl;
    private boolean draft_val;
    
    
    public SearchParamsOccPermitDateFieldsEnum[] getDateFieldList(){
       SearchParamsOccPermitDateFieldsEnum[] fields = SearchParamsOccPermitDateFieldsEnum.values();
       return fields;
   }
   
   public SearchParamsOccPermitUserFieldsEnum[] getUserFieldList(){
       SearchParamsOccPermitUserFieldsEnum[] fields = SearchParamsOccPermitUserFieldsEnum.values();
       return fields;
   }

    /**
     * @return the permitTypeList_ctl
     */
    public boolean isPermitTypeList_ctl() {
        return permitTypeList_ctl;
    }

    /**
     * @return the permitTypeList_val
     */
    public List<OccPermitType> getPermitTypeList_val() {
        return permitTypeList_val;
    }

    /**
     * @param permitTypeList_ctl the permitTypeList_ctl to set
     */
    public void setPermitTypeList_ctl(boolean permitTypeList_ctl) {
        this.permitTypeList_ctl = permitTypeList_ctl;
    }

    /**
     * @param permitTypeList_val the permitTypeList_val to set
     */
    public void setPermitTypeList_val(List<OccPermitType> permitTypeList_val) {
        this.permitTypeList_val = permitTypeList_val;
    }

    /**
     * @return the draft_ctl
     */
    public boolean isDraft_ctl() {
        return draft_ctl;
    }

    /**
     * @return the draft_val
     */
    public boolean isDraft_val() {
        return draft_val;
    }

    /**
     * @param draft_ctl the draft_ctl to set
     */
    public void setDraft_ctl(boolean draft_ctl) {
        this.draft_ctl = draft_ctl;
    }

    /**
     * @param draft_val the draft_val to set
     */
    public void setDraft_val(boolean draft_val) {
        this.draft_val = draft_val;
    }
    
    
  
   
   
    
}
