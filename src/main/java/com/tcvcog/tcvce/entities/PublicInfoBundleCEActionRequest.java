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
    private boolean linkedToCase;
    
    private String formattedSubmittedTimeStamp;
    private String requestDescription;
    
    private String publicExternalNotes;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>");
        sb.append("Code Enforcement Action Request");
        sb.append("</h2>");
        sb.append("<p>");
        
        sb.append("<span class=\"bold\">");
        sb.append("Public access code: ");
        sb.append("</span>");
        sb.append(getPacc());
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("PACC status message: ");
        sb.append("</span>");
        sb.append(getPaccStatusMessage());
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Municipality: ");
        sb.append("</span>");
        sb.append(getMuni().getMuniName());
        sb.append("<br />");
//        sb.append(getMuni().getAddress_street());
//        sb.append("<br />");
//        sb.append(getMuni().getAddress_city());
//        sb.append(" ");
//        sb.append(getMuni().getAddress_state());
//        sb.append(" ");
//        sb.append(getMuni().getAddress_zip());
//        sb.append("<br />");
//        sb.append(getMuni().getPhone());
//        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Action Request Status: ");
        sb.append("</span>");
        sb.append(requestStatus.statusTitle);
        sb.append("<br />");
        
        
        
        sb.append("<span class=\"bold\">");
        sb.append("Data bundle name: ");
        sb.append("</span>");
        sb.append(getTypeName());
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Date of record: ");
        sb.append("</span>");
        sb.append(getDateOfRecord());
        sb.append("<br />");
        
        if(isAddressAssociated()){
            sb.append("<span class=\"bold\">");
            sb.append("Property Address: ");
            sb.append("</span>");
            sb.append(getPropertyAddress());
            sb.append("<br />");
            
        }
        
        if(getCaseManagerName() != null){
            sb.append("<span class=\"bold\">");
            sb.append("Assigned code enforcement officer: ");
            sb.append("</span>");
            sb.append(getCaseManagerName());
            sb.append("<br />");
            sb.append(getCaseManagerContact());
            sb.append("<br />");
        }
        
        // start action specific sections
        
        sb.append("<span class=\"bold\">");
        sb.append("Request ID number: ");
        sb.append("</span>");
        sb.append(requestID);
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Submission date: ");
        sb.append("</span>");
        sb.append(formattedSubmittedTimeStamp);
        sb.append("<br />");
        
        
        sb.append("<span class=\"bold\">");
        sb.append("Requestor name: ");
        sb.append("</span>");
        sb.append(actionRequestorFLname);
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Issue type: ");
        sb.append("</span>");
        sb.append(issueTypeString);
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Case link status: ");
        sb.append("</span>");
        sb.append(caseLinkStatus);
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Request description: ");
        sb.append("</span>");
        sb.append(requestDescription);
        sb.append("<br />");
        
        sb.append("<span class=\"bold\">");
        sb.append("Public notes: ");
        sb.append("</span>");
        sb.append(publicExternalNotes);
        sb.append("<br />");
        
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

    /**
     * @return the linkedToCase
     */
    public boolean isLinkedToCase() {
        return linkedToCase;
    }

    /**
     * @param linkedToCase the linkedToCase to set
     */
    public void setLinkedToCase(boolean linkedToCase) {
        this.linkedToCase = linkedToCase;
    }
}
