/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 * 
 * @author sylvia
 */
public class PublicInfoBundleCEActionRequest extends PublicInfoBundle implements Serializable {
    
    //************************************************
    //*******Action request case public data********
    //************************************************
    
    // these get moved over directly
    private int requestID;
    private CEActionRequestStatus requestStatus;
    
    // Strings built from CEActinoRequest objects
    private String actionRequestorFLname;
    private String issueTypeString;
    private String caseLinkStatus;
    
    private String formattedSubmittedTimeStamp;
    private String requestDescription;
    
    private String publicExternalNotes;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>");
        sb.append("Code Enforcement Action Request");
        sb.append("</h3>");
        sb.append("<p>");
        sb.append("<span class=\"bold\">");
        sb.append("Action Request Status: ");
        sb.append("</span>");
        sb.append(requestStatus);
        sb.append("<br/>");
        sb.append("<span class=\"bold\">");
        sb.append("Requestor name: ");
        sb.append("</span>");
        sb.append(actionRequestorFLname);
        sb.append("<br/>");
        sb.append("</p>");
        
               
        return sb.toString();
//                
//                
//                "PublicInfoBundleCEActionRequest{" 
//                + "requestID=" + requestID 
//                + ", requestStatus=" + requestStatus 
//                + ", actionRequestorFLname=" + actionRequestorFLname 
//                + ", issueTypeString=" + issueTypeString 
//                + ", caseLinkStatus=" + caseLinkStatus 
//                + ", formattedSubmittedTimeStamp=" + formattedSubmittedTimeStamp 
//                + ", requestDescription=" + requestDescription 
//                + ", publicExternalNotes=" + publicExternalNotes + '}';
    }
    
    

    /**
     * @return the requestID
     */
    public int getRequestID() {
        return requestID;
    }


    /**
     * @return the requestStatus
     */
    public CEActionRequestStatus getRequestStatus() {
        return requestStatus;
    }

    /**
     * @return the actionRequestorFLname
     */
    public String getActionRequestorFLname() {
        return actionRequestorFLname;
    }

    /**
     * @return the issueTypeString
     */
    public String getIssueTypeString() {
        return issueTypeString;
    }

    /**
     * @return the caseLinkStatus
     */
    public String getCaseLinkStatus() {
        return caseLinkStatus;
    }

    /**
     * @return the formattedSubmittedTimeStamp
     */
    public String getFormattedSubmittedTimeStamp() {
        return formattedSubmittedTimeStamp;
    }

    /**
     * @return the requestDescription
     */
    public String getRequestDescription() {
        return requestDescription;
    }

    /**
     * @return the publicExternalNotes
     */
    public String getPublicExternalNotes() {
        return publicExternalNotes;
    }

    /**
     * @param requestID the requestID to set
     */
    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    /**
     * @param requestStatus the requestStatus to set
     */
    public void setRequestStatus(CEActionRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    /**
     * @param actionRequestorFLname the actionRequestorFLname to set
     */
    public void setActionRequestorFLname(String actionRequestorFLname) {
        this.actionRequestorFLname = actionRequestorFLname;
    }

    /**
     * @param issueTypeString the issueTypeString to set
     */
    public void setIssueTypeString(String issueTypeString) {
        this.issueTypeString = issueTypeString;
    }

    /**
     * @param caseLinkStatus the caseLinkStatus to set
     */
    public void setCaseLinkStatus(String caseLinkStatus) {
        this.caseLinkStatus = caseLinkStatus;
    }

    /**
     * @param formattedSubmittedTimeStamp the formattedSubmittedTimeStamp to set
     */
    public void setFormattedSubmittedTimeStamp(String formattedSubmittedTimeStamp) {
        this.formattedSubmittedTimeStamp = formattedSubmittedTimeStamp;
    }

    /**
     * @param requestDescription the requestDescription to set
     */
    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    /**
     * @param publicExternalNotes the publicExternalNotes to set
     */
    public void setPublicExternalNotes(String publicExternalNotes) {
        this.publicExternalNotes = publicExternalNotes;
    }
}
