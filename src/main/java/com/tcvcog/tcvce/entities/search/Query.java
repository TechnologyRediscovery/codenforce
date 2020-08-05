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
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
    protected UserAuthorized requestingUser;
    
    private StringBuilder resultsMessage;
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
        this.resultsMessage = new StringBuilder();
        initLog();
        
    }
    
    public Query(){
        this.resultsMessage = new StringBuilder();
        initLog();
        //blank
    }
    
    private void initLog(){
        resultsMessage.append(Constants.FMT_SEARCH_HEAD_QUERYOG);
        resultsMessage.append(Constants.FMT_HTML_BREAK);
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
    public abstract <P extends SearchParams> List<P> getParamsList();
    
    /**
     * Convenience method for retrieving the parameter at the head of the list
     * @param <P>
     * @return 
     */
    public abstract <P extends SearchParams> P getPrimaryParams();
    
    /**
     * Used to include a given SaerchParams subclass in the Query
     * @param params adds the given SearchParams subclass to the subclass's
     * internal parameter list
     */
    public abstract void addParams(SearchParams params);
    
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
    
    public boolean isQueryExecuted(){
        return executionTimestamp != null;
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
    public String getQueryLog() {
        resultsMessage.append(Constants.FMT_NOTE_SEP_INTERNAL);
        resultsMessage.append(Constants.FMT_HTML_BREAK);
        resultsMessage.append("EXECUTION TIMESTAMP: ");
        resultsMessage.append(getExecutionTimestampPretty());
        return resultsMessage.toString();
    }

    /**
     * @param msg
     */
    public void appendToQueryLog(String msg) {
        if(msg != null){
            resultsMessage.append(Constants.FMT_NOTE_SEP_INTERNAL);
            resultsMessage.append(Constants.FMT_HTML_BREAK);
            resultsMessage.append(msg);
            resultsMessage.append(Constants.FMT_HTML_BREAK);
        }
    }
    
    public void appendToQueryLog(SearchParams sp){
        if(sp != null){
            resultsMessage.append(Constants.FMT_SEARCH_HEAD_FILTERLOG);
            resultsMessage.append(Constants.FMT_HTML_BREAK);
            resultsMessage.append("FILTER NAME: ");
            resultsMessage.append(sp.getFilterName());
            resultsMessage.append(Constants.FMT_HTML_BREAK);
            resultsMessage.append("EXECUTION LOG: ");
            resultsMessage.append(Constants.FMT_HTML_BREAK);
            resultsMessage.append(sp.getParamLog());
            resultsMessage.append(Constants.FMT_HTML_BREAK);
            resultsMessage.append("RAW SQL: ");
            resultsMessage.append(sp.extractRawSQL());
        }
        
    }
    
    public void clearQueryLog(){
        resultsMessage = new StringBuilder();
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

    /**
     * @return the requestingUser
     */
    public UserAuthorized getRequestingUser() {
        return requestingUser;
    }

    /**
     * @param requestingUser the requestingUser to set
     */
    public void setRequestingUser(UserAuthorized requestingUser) {
        this.requestingUser = requestingUser;
    }

    
   
   
    
    
}
