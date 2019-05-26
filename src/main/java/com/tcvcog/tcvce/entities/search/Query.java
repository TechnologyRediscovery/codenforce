/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public abstract class Query extends EntityUtils implements Serializable, Reportable{
    
    private String queryTitle;
    private Municipality muni;
    private RoleType userRankAccessCeiling;
    private String resultsMessage;
    private User user;

    public Query(String queryTitle, Municipality muni) {
        this.queryTitle = queryTitle;
        this.muni = muni;
        
    }
    
    public Query(Municipality muni) {
        this.muni = muni;
        
    }
    
    public Query(){
        //emtpy
    }
    
    

    /**
     * @return the queryTitle
     */
    public String getQueryTitle() {
        return queryTitle;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    

    /**
     * @param queryTitle the queryTitle to set
     */
    public void setQueryTitle(String queryTitle) {
        this.queryTitle = queryTitle;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the userRankAccessCeiling
     */
    public RoleType getUserRankAccessCeiling() {
        return userRankAccessCeiling;
    }

    /**
     * @param userRankAccessCeiling the userRankAccessCeiling to set
     */
    public void setUserRankAccessCeiling(RoleType userRankAccessCeiling) {
        this.userRankAccessCeiling = userRankAccessCeiling;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.queryTitle);
        hash = 67 * hash + Objects.hashCode(this.muni);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Query other = (Query) obj;
        if (!Objects.equals(this.queryTitle, other.queryTitle)) {
            return false;
        }
        if (!Objects.equals(this.muni, other.muni)) {
            return false;
        }
        return true;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the resultsMessage
     */
    public String getResultsMessage() {
        return resultsMessage;
    }

    /**
     * @param resultsMessage the resultsMessage to set
     */
    public void setResultsMessage(String resultsMessage) {
        this.resultsMessage = resultsMessage;
    }

    
   
    
    
}
