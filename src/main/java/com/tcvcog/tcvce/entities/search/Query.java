/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.IFace_CredentialSigned;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * An experimental Generic superclass of the Query family
 * of objects. The writer of this class has never designed with
 * Generics before and needs some high-stakes tinkering
 * 
 * @author sylvia
 * @param <E> the Business Object of which this Query is used to 
 * retrieve sets
 */
public abstract class   Query<E extends BOb> 
        extends         EntityUtils 
        implements      Serializable,
                        IFace_CredentialSigned{
    
    private Municipality muni;
    private UserAuthorized user;
    
    /**
     * Security mechanism for controlling queried data: Coordinators
     * must check incoming requests to runQuery to ensure that the requestor's
     * rank meets the minimum for the Query. Since a number of locations use
     * Query objects to get data, we need a uniform location from which 
     * to control who can get what queried information.
     * 
     */
    private RoleType userRankAccessMinimum;
    
    private String resultsMessage;
    private LocalDateTime executionTimestamp;
    private String executionTimestampPretty;
    
    private boolean executedByIntegrator;
    private String credentialSignature;
            
    
    public abstract List<E> getBOBResultList();
    public abstract void setBOBResultList(List<E> l);
    
    public abstract List getParmsList();
    public abstract String getQueryTitle();
    
    public abstract void clearResultList();
    
    
    public Query(Municipality muni, UserAuthorized u) {
        this.muni = muni;
        this.user = u;
        
    }
    
    public Query(){
        //blank
    }
    
    
    /**
     * @return the credentialSignature
     */
    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }

   

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }


    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the userRankAccessMinimum
     */
    public RoleType getUserRankAccessMinimum() {
        return userRankAccessMinimum;
    }

    /**
     * @param userRankAccessMinimum the userRankAccessMinimum to set
     */
    public void setUserRankAccessMinimum(RoleType userRankAccessMinimum) {
        this.userRankAccessMinimum = userRankAccessMinimum;
    }

  

    /**
     * @return the user
     */
    public UserAuthorized getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(UserAuthorized user) {
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

    

    /**
     * @return the executionTimestamp
     */
    public LocalDateTime getExecutionTimestamp() {
        return executionTimestamp;
    }

    /**
     * @param executionTimestamp the executionTimestamp to set
     */
    public void setExecutionTimestamp(LocalDateTime executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    /**
     * @return the executionTimestampPretty
     */
    public String getExecutionTimestampPretty() {
        if(executionTimestamp != null){
            executionTimestampPretty = EntityUtils.getPrettyDate(executionTimestamp);
        }
        return executionTimestampPretty;
    }

    /**
     * @return the executedByIntegrator
     */
    public boolean isExecutedByIntegrator() {
        return executedByIntegrator;
    }

    /**
     * @param executedByIntegrator the executedByIntegrator to set
     */
    public void setExecutedByIntegrator(boolean executedByIntegrator) {
        this.executedByIntegrator = executedByIntegrator;
    }

   
    
    
}
