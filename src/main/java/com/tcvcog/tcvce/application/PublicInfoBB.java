/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PublicInfoCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccInspection;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccPermitApplication;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class PublicInfoBB extends BackingBeanUtils implements Serializable {

    private List<PublicInfoBundle> publicInfoBundleList;
    private PublicInfoBundle selectedBundle;
    private int submittedPACC;
    private String publicMessage;

    //cePaccView.xhtml fields
    private List<PublicInfoBundleCECase> bundledCases;
    private List<PublicInfoBundleCEActionRequest> bundledRequests;

    //occView.xhtml fields
    private List<PublicInfoBundleOccInspection> bundledInspections;
    private List<PublicInfoBundleOccPermitApplication> bundledApplications;

    /**
     * Creates a new instance of CEPublicAccessBB
     */
    public PublicInfoBB() {
    }

    @PostConstruct
    public void initBean() {

        publicInfoBundleList = getSessionBean().getInfoBundleList();
        
        if (publicInfoBundleList != null) {
            
            //Lists are only initialized if we need them
            
            for (PublicInfoBundle bundle : publicInfoBundleList) {
                if (bundle instanceof PublicInfoBundleCEActionRequest) {
                    if(bundledRequests == null){
                        bundledRequests = new ArrayList<>();
                    }
                    bundledRequests.add((PublicInfoBundleCEActionRequest) bundle);
                    continue;
                }

                if (bundle instanceof PublicInfoBundleCECase) {
                    if(bundledCases == null){
                        bundledCases = new ArrayList<>();
                    }
                    bundledCases.add((PublicInfoBundleCECase) bundle);
                    continue;
                }

                if (bundle instanceof PublicInfoBundleOccInspection) {
                    if(bundledInspections == null){
                        bundledInspections = new ArrayList<>();
                    }
                    bundledInspections.add((PublicInfoBundleOccInspection) bundle);
                    continue;
                }

                if (bundle instanceof PublicInfoBundleOccPermitApplication) {
                    if(bundledApplications == null){
                        bundledApplications = new ArrayList<>();
                    }
                    bundledApplications.add((PublicInfoBundleOccPermitApplication) bundle);
                    continue;
                }
            }
        }
    }

    public String submitPACC() {
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        try {
            publicInfoBundleList = pic.getPublicInfoBundles(submittedPACC);
            if (!publicInfoBundleList.isEmpty()) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Retrieved " + String.valueOf(publicInfoBundleList.size()) + " bundles!", ""));

                getSessionBean().setInfoBundleList(publicInfoBundleList);
                
                //Now look through the bundles and see which interface we need to send the user to
                for (PublicInfoBundle bundle : publicInfoBundleList) {

                    if (bundle.getTypeName().equalsIgnoreCase("CECASE")
                            || bundle.getTypeName().equalsIgnoreCase("CEAR")) {
                        //Code enforcement it is!
                        return "cePaccView";
                    }

                    if (bundle.getTypeName().equalsIgnoreCase("OccPermitApplication")
                            || bundle.getTypeName().equalsIgnoreCase("OccInspection")) {
                        //Occupancy it is!
                        return "occPaccView";
                    }
                }
            } else {

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "No info bundles found for this control code", ""));
                publicInfoBundleList = null;
            }
        } catch (IntegrationException ex) {
            System.out.println("PublicInfoBB.submitPacc() | ERROR: " + ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to search for info bundles, sorry!", "This is a system error."));
        } catch (SearchException
                | EventException
                | AuthorizationException
                | ViolationException ex) {
            System.out.println("PublicInfoBB.submitPacc() | ERROR: " + ex);
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }

        //something went wrong, try again
        return "";

    }

    public String viewPACCRecordDetails(PublicInfoBundle pib) {
        if (pib instanceof PublicInfoBundleCECase) {
            PublicInfoBundleCECase pibCase = (PublicInfoBundleCECase) pib;
            getSessionBean().setPibCECase(pibCase);
            return "publicInfoCECase";

        }
        return "";
    }

    public void attachMessage(ActionEvent ev) {
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        try {
            pic.attachMessageToBundle(selectedBundle, publicMessage);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Public case note added", ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to attach messages at this time, sorry!",
                            "This is a system error and has been logged for debugging."));

        }
        publicMessage = null;

    }

    /**
     * @return the publicInfoBundleList
     */
    public List<PublicInfoBundle> getPublicInfoBundleList() {
        return publicInfoBundleList;
    }

    /**
     * @return the submittedPACC
     */
    public int getSubmittedPACC() {
        return submittedPACC;
    }

    /**
     * @param publicInfoBundleList the publicInfoBundleList to set
     */
    public void setPublicInfoBundleList(List<PublicInfoBundle> publicInfoBundleList) {
        this.publicInfoBundleList = publicInfoBundleList;
    }

    /**
     * @param submittedPACC the submittedPACC to set
     */
    public void setSubmittedPACC(int submittedPACC) {
        this.submittedPACC = submittedPACC;
    }

    /**
     * @return the selectedBundle
     */
    public PublicInfoBundle getSelectedBundle() {
        return selectedBundle;
    }

    /**
     * @param sb
     */
    public void setSelectedBundle(PublicInfoBundle sb) {
        System.out.println("PublicInfoBB.setSelectedBundle | Bundle type: " + sb.getTypeName());
        this.selectedBundle = sb;
    }

    /**
     * @return the publicMessage
     */
    public String getPublicMessage() {
        return publicMessage;
    }

    /**
     * @param publicMessage the publicMessage to set
     */
    public void setPublicMessage(String publicMessage) {
        this.publicMessage = publicMessage;
    }

    public String goToIntensityManage() {

        return "intensityManage";
    }

    public List<PublicInfoBundleCECase> getBundledCases() {
        return bundledCases;
    }

    public void setBundledCases(List<PublicInfoBundleCECase> bundledCases) {
        this.bundledCases = bundledCases;
    }

    public List<PublicInfoBundleCEActionRequest> getBundledRequests() {
        return bundledRequests;
    }

    public void setBundledRequests(List<PublicInfoBundleCEActionRequest> bundledRequests) {
        this.bundledRequests = bundledRequests;
    }

    public List<PublicInfoBundleOccInspection> getBundledInspections() {
        return bundledInspections;
    }

    public void setBundledInspections(List<PublicInfoBundleOccInspection> bundledInspections) {
        this.bundledInspections = bundledInspections;
    }

    public List<PublicInfoBundleOccPermitApplication> getBundledApplications() {
        return bundledApplications;
    }

    public void setBundledApplications(List<PublicInfoBundleOccPermitApplication> bundledApplications) {
        this.bundledApplications = bundledApplications;
    }

}
