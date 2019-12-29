/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.IFace_CredentialSigned;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    
    /**
     * Security mechanism for controlling queried data: Coordinators
     * must check incoming requests to runQuery to ensure that the requestor's
     * rank meets the minimum for the Query. Since a number of locations use
     * Query objects to get data, we need a uniform location from which 
     * to control who can get what queried information.
     * 
     */
    private RoleType userRankAccessMinimum;
    private Credential credential;
    
    private String resultsMessage;
    private LocalDateTime executionTimestamp;
    
    
    
    /**
     * Creates an instance of Query by baking in the User's credential, which
     * thus ties the Query to a particular municipality. Because a Credential is 
     * required on object formation, there's no setCredential(Credential cr) method
     * for general security reasons.
     * 
     * @param c 
     */
    public Query(Credential c) {
        this.credential = c;
        
    }
    
    public Query(){
        //blank
    }
    
    /**
     * Implementing classes must return a list of BObs
     * @return 
     */
    public abstract List<E> getBOBResultList();
    
    /**
     * Implementing classes must allow a BOb list to be set
     * TODO: Fix inheritance snafoo here! 
     * 
     * @param l 
     */
    public abstract void addBObListToResults(List<E> l);
    
    /**
     * Implementing classes must allow retrieval of the SearchParam subclasses
     * @param <P>
     * @return 
     */
    public abstract <P extends SearchParams> List<P> getParmsList();
    
    /**
     * Used to include a given SaerchParams subclass in the Query
     * @param params adds the given SearchParams subclass to the subclass's
     * internal parameter list
     * @return the size of the List after insertion
     */
    public abstract int addParams(SearchParams params);
    
    /**
     * Accesses the size of the implementer's parameter list which is used to
     * determine the proper search type
     * @return the size of the parameter list
     */
    public abstract int getParamsListSize();
    
    /**
     * Implementing classes will usually grab the title from the QueryXXXEnum
     * @return 
     */
    public abstract String getQueryTitle();
    
    /**
     * Implementing classes usually just call clear() on their Collections object
     */
    public abstract void clearResultList();
    
    
    
    /**
     * @return the credentialSignature
     */
    @Override
    public String getCredentialSignature() {
        if(credential != null){
            return credential.getSignature();
        }
        return null;
    }

   
    public Credential getCredential(){
        return credential;
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
            return EntityUtils.getPrettyDate(executionTimestamp);
        }
        return null;
    }

   
   
    
    
}
