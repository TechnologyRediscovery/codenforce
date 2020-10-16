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
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionStatusEnum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class PublicInfoBB extends BackingBeanUtils implements Serializable {

    private List<PublicInfoBundle> publicInfoBundleList;
    private int submittedPACC;
    private boolean refreshingBundles; //To surpress actions that submitPACC does when getting bundles the first time. When true only do refresh things
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

            sortBundles();

        }
    }

    private void sortBundles() {

        //Lists are only initialized if we need them
        for (PublicInfoBundle bundle : publicInfoBundleList) {
            if (bundle instanceof PublicInfoBundleCEActionRequest) {
                if (bundledRequests == null) {
                    bundledRequests = new ArrayList<>();
                }
                bundledRequests.add((PublicInfoBundleCEActionRequest) bundle);
                continue;
            }

            if (bundle instanceof PublicInfoBundleCECase) {
                if (bundledCases == null) {
                    bundledCases = new ArrayList<>();
                }
                bundledCases.add((PublicInfoBundleCECase) bundle);
                continue;
            }

            if (bundle instanceof PublicInfoBundleOccInspection) {
                if (bundledInspections == null) {
                    bundledInspections = new ArrayList<>();
                }
                bundledInspections.add((PublicInfoBundleOccInspection) bundle);
                continue;
            }

            if (bundle instanceof PublicInfoBundleOccPermitApplication) {
                if (bundledApplications == null) {
                    bundledApplications = new ArrayList<>();
                }
                bundledApplications.add((PublicInfoBundleOccPermitApplication) bundle);
            }
        }

    }

    public String submitPACC() {
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        try {
            publicInfoBundleList = pic.getPublicInfoBundles(submittedPACC);
            if (!publicInfoBundleList.isEmpty()) {

                getSessionBean().setInfoBundleList(publicInfoBundleList);

                if (!refreshingBundles) {

                    //We're not refreshing the bundles
                    //we need to tell them how many we retrieved
                    //Then we need to redirect them
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Retrieved " + String.valueOf(publicInfoBundleList.size()) + " bundles!", ""));

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
        //Or we're refreshing bundles and don't need to redirect
        return "";

    }

    /**
     * Probably not necessary anymore, since we now have PaccView pages
     * @deprecated 
     * @param pib
     * @return 
     */
    public String viewPACCRecordDetails(PublicInfoBundle pib) {
        if (pib instanceof PublicInfoBundleCECase) {
            PublicInfoBundleCECase pibCase = (PublicInfoBundleCECase) pib;
            getSessionBean().setPibCECase(pibCase);
            return "publicInfoCECase";

        }
        return "";
    }
    
    
    /**
     * Looks at an inspection and returns a string describing what stage the 
     * inspection is currently in.
     * @param inspection
     * @return 
     */
    public String inspectionStatus(PublicInfoBundleOccInspection inspection){
        
        if(!inspection.isPaccEnabled()){
            //not PACC enabled, the status is not available
            
            return "Status not available";
        
        } else if(!inspection.getBundledInspection().isReadyForPassedCertification()){
            //Not passed, let's find out why
            
            for(OccInspectedSpace space : inspection.getBundledInspection().getInspectedSpaceList()){
                if(space.getStatus().getStatusEnum() == OccInspectionStatusEnum.NOTINSPECTED){
                    return "Inspection in progress";
                }
            }
            //All spaces must have failed inspection.
            return "Inspection failed";
            
        } else {
            //Passed!
            return "Inspection passed!";
        }
        
    }

    public void attachMessage(PublicInfoBundle selectedBundle) {
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        try {
            pic.attachMessageToBundle(selectedBundle, publicMessage);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Public case note added", ""));

        } catch (IntegrationException 
                | AuthorizationException 
                | BObStatusException 
                | EventException 
                | ViolationException 
                | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to attach messages at this time, sorry!",
                            "This is a system error and has been logged for debugging."));

        }

        publicMessage = null;

        submittedPACC = selectedBundle.getPacc();

        //Clear the lists
        if (bundledApplications != null) {
            bundledApplications.clear();
        }

        if (bundledInspections != null) {
            bundledInspections.clear();
        }

        if (bundledCases != null) {
            bundledCases.clear();
        }

        if (bundledRequests != null) {
            bundledRequests.clear();
        }

        //Refresh the bundles
        refreshingBundles = true;

        submitPACC();

        sortBundles();

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
