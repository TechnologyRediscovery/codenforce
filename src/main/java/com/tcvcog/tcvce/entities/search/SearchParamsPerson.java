/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.RoleType;

/**
 * Stores search parameters and switches turning each one on and off 
 for queries involving Person objects
 * 
 * @author Sylvia Garland
 */
public  class       SearchParamsPerson 
        extends     SearchParams {
    
   
    
    // filter PERS-1
    private boolean name_ctl;
    private String name_val;

    // filter PERS-2
    private boolean under18_ctl;
    private boolean under18_val;
    
    // filter PERS-3
    private boolean businessEntity_ctl;
    private boolean businessEntity_val;
    
    // filter PERS-4
    private boolean multiHuman_ctl;
    private boolean multiHuman_val;
    
    // filter PERS-5
    private boolean jobTitle_ctl;
    private String jobTitle_val;
    
    // filter PERS-6
    protected RoleType phoneNumber_rtMin;
    private boolean phoneNumber_ctl;
    private String phoneNumber_val;

    // filter PERS-7
    protected RoleType email_rtMin;
    private boolean email_ctl;
    private String email_val;

    // filter PERS-8
    private boolean source_ctl;
    private BOBSource source_val;
        
   public SearchParamsPerson(){
       
   }

    /**
     * @return the name_val
     */
    public String getName_last_val() {
        return getName_val();
    }

    /**
     * @return the name_ctl
     */
    public boolean isName_last_ctl() {
        return isName_ctl();
    }

  
    /**
     * @return the email_val
     */
    public String getEmail_val() {
        return email_val;
    }

    /**
     * @return the email_ctl
     */
    public boolean isEmail_ctl() {
        return email_ctl;
    }

   
   

    /**
     * @param name_last_val the name_val to set
     */
    public void setName_val(String name_last_val) {
        this.name_val = name_last_val;
    }

    /**
     * @param name_last_ctl the name_ctl to set
     */
    public void setName_ctl(boolean name_last_ctl) {
        this.name_ctl = name_last_ctl;
    }

  
    /**
     * @param email_val the email_val to set
     */
    public void setEmail_val(String email_val) {
        this.email_val = email_val;
    }

    /**
     * @param email_ctl the email_ctl to set
     */
    public void setEmail_ctl(boolean email_ctl) {
        this.email_ctl = email_ctl;
    }

   
    /**
     * @return the phoneNumber_val
     */
    public String getPhoneNumber_val() {
        return phoneNumber_val;
    }

    /**
     * @param phoneNumber_val the phoneNumber_val to set
     */
    public void setPhoneNumber_val(String phoneNumber_val) {
        this.phoneNumber_val = phoneNumber_val;
    }

    /**
     * @return the phoneNumber_ctl
     */
    public boolean isPhoneNumber_ctl() {
        return phoneNumber_ctl;
    }

    /**
     * @param phoneNumber_ctl the phoneNumber_ctl to set
     */
    public void setPhoneNumber_ctl(boolean phoneNumber_ctl) {
        this.phoneNumber_ctl = phoneNumber_ctl;
    }

  

  
    /**
     * @return the phoneNumber_rtMin
     */
    public RoleType getPhoneNumber_rtMin() {
        return phoneNumber_rtMin;
    }

    /**
     * @param phoneNumber_rtMin the phoneNumber_rtMin to set
     */
    public void setPhoneNumber_rtMin(RoleType phoneNumber_rtMin) {
        this.phoneNumber_rtMin = phoneNumber_rtMin;
    }

    /**
     * @return the email_rtMin
     */
    public RoleType getEmail_rtMin() {
        return email_rtMin;
    }

    /**
     * @param email_rtMin the email_rtMin to set
     */
    public void setEmail_rtMin(RoleType email_rtMin) {
        this.email_rtMin = email_rtMin;
    }

  

  
    /**
     * @return the source_ctl
     */
    public boolean isSource_ctl() {
        return source_ctl;
    }

    /**
     * @param source_ctl the source_ctl to set
     */
    public void setSource_ctl(boolean source_ctl) {
        this.source_ctl = source_ctl;
    }

    /**
     * @return the source_val
     */
    public BOBSource getSource_val() {
        return source_val;
    }

    /**
     * @param source_val the source_val to set
     */
    public void setSource_val(BOBSource source_val) {
        this.source_val = source_val;
    }

   
    /**
     * @return the name_ctl
     */
    public boolean isName_ctl() {
        return name_ctl;
    }

    /**
     * @return the name_val
     */
    public String getName_val() {
        return name_val;
    }

    /**
     * @return the under18_ctl
     */
    public boolean isUnder18_ctl() {
        return under18_ctl;
    }

    /**
     * @return the under18_val
     */
    public boolean isUnder18_val() {
        return under18_val;
    }

    /**
     * @return the businessEntity_ctl
     */
    public boolean isBusinessEntity_ctl() {
        return businessEntity_ctl;
    }

    /**
     * @return the businessEntity_val
     */
    public boolean isBusinessEntity_val() {
        return businessEntity_val;
    }

    /**
     * @return the multiHuman_ctl
     */
    public boolean isMultiHuman_ctl() {
        return multiHuman_ctl;
    }

    /**
     * @return the multiHuman_val
     */
    public boolean isMultiHuman_val() {
        return multiHuman_val;
    }

    /**
     * @return the jobTitle_ctl
     */
    public boolean isJobTitle_ctl() {
        return jobTitle_ctl;
    }

    /**
     * @return the jobTitle_val
     */
    public String getJobTitle_val() {
        return jobTitle_val;
    }

    /**
     * @param under18_ctl the under18_ctl to set
     */
    public void setUnder18_ctl(boolean under18_ctl) {
        this.under18_ctl = under18_ctl;
    }

    /**
     * @param under18_val the under18_val to set
     */
    public void setUnder18_val(boolean under18_val) {
        this.under18_val = under18_val;
    }

    /**
     * @param businessEntity_ctl the businessEntity_ctl to set
     */
    public void setBusinessEntity_ctl(boolean businessEntity_ctl) {
        this.businessEntity_ctl = businessEntity_ctl;
    }

    /**
     * @param businessEntity_val the businessEntity_val to set
     */
    public void setBusinessEntity_val(boolean businessEntity_val) {
        this.businessEntity_val = businessEntity_val;
    }

    /**
     * @param multiHuman_ctl the multiHuman_ctl to set
     */
    public void setMultiHuman_ctl(boolean multiHuman_ctl) {
        this.multiHuman_ctl = multiHuman_ctl;
    }

    /**
     * @param multiHuman_val the multiHuman_val to set
     */
    public void setMultiHuman_val(boolean multiHuman_val) {
        this.multiHuman_val = multiHuman_val;
    }

    /**
     * @param jobTitle_ctl the jobTitle_ctl to set
     */
    public void setJobTitle_ctl(boolean jobTitle_ctl) {
        this.jobTitle_ctl = jobTitle_ctl;
    }

    /**
     * @param jobTitle_val the jobTitle_val to set
     */
    public void setJobTitle_val(String jobTitle_val) {
        this.jobTitle_val = jobTitle_val;
    }
    
}
