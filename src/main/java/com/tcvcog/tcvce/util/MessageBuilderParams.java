/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.util;

import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.User;

/**
 * Convenience class to avoid a 5-input-parameter method for formatting
 * messages
 * @author ellen baskum
 */
public class MessageBuilderParams {
    private String existingContent;
    private String header;
    private String explanation;
    private String newMessageContent;
    private User user;
    private Credential cred;
    private boolean includeCredentialSig;

    public MessageBuilderParams(String existingContent, 
                                String header, 
                                String explanation, 
                                String newMessageContent, 
                                User user,
                                Credential cr) {
        this.existingContent = existingContent;
        this.header = header;
        this.explanation = explanation;
        this.newMessageContent = newMessageContent;
        this.user = user;
        this.cred = cr;
    }
    
    public MessageBuilderParams(){
        
    }

    /**
     * @return the existingContent
     */
    public String getExistingContent() {
        return existingContent;
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @return the explanation
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * @return the newMessageContent
     */
    public String getNewMessageContent() {
        return newMessageContent;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param existingContent the existingContent to set
     */
    public void setExistingContent(String existingContent) {
        this.existingContent = existingContent;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @param explanation the explanation to set
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * @param newMessageContent the newMessageContent to set
     */
    public void setNewMessageContent(String newMessageContent) {
        this.newMessageContent = newMessageContent;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the cred
     */
    public Credential getCred() {
        return cred;
    }

    /**
     * @param cred the cred to set
     */
    public void setCred(Credential cred) {
        this.cred = cred;
    }

    /**
     * @return the includeCredentialSig
     */
    public boolean isIncludeCredentialSig() {
        return includeCredentialSig;
    }

    /**
     * @param includeCredentialSig the includeCredentialSig to set
     */
    public void setIncludeCredentialSig(boolean includeCredentialSig) {
        this.includeCredentialSig = includeCredentialSig;
    }
    
}
