/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 *  Represents objects which are linked to blobs
 * Contains fields for information about the name of the
 * parent and its linking tables for code reuse
 * 
 * @author Ellen Bascomb
 */
public enum BlobLinkEnum {
    
     MUNICIPALITY   (   "Municipality",
                            "municipality",
                            "muniphotodoc",
                            "muni_municode",
                            "photodoc_photodocid"),
    
    PROPERTY   (   "Property",
                            "parcel",
                            "parcelphotodoc",
                            "parcel_parcelkey",
                            "photodoc_photodocid"),
     
    OCC_PERIOD          (   "Occupancy Period",
                            "occperiod",
                            "occperiodphotodoc",
                            "occperiod_periodid",
                            "photodoc_photodocid"),
    
    FIELD_INSPECTION    (   "Field Inspection",
                            "occinspection",
                            "occinspectionphotodoc",
                            "inspection_inspectionid",
                            "photodoc_photodocid"),
    
    INSPECTED_ELEMENT   (   "Inspected Ordinance",
                            "occinspectedspaceelement",
                            "occinspectedspaceelementphotodoc",
                            "inspectedspaceelement_elementid",
                            "photodoc_photodocid"),
    
    CEACTION_REQUEST   (   "Code enforcement action request",
                            "ceactionrequest",
                            "ceactionrequestphotodoc",
                            "ceactionrequest_requestid",
                            "photodoc_photodocid"),
    
    CE_CASE   (         "Code Enforcement Case",
                            "cecase",
                            "cecasephotodoc",
                            "cecase_caseid",
                            "photodoc_photodocid"),
    
    CODE_VIOLATION  (    "Code Violation",
                            "codeviolation",
                            "codeviolationphotodoc",
                            "codeviolation_violationid",
                            "photodoc_photodocid"),
    
    CITATION   (            "Citation",
                            "citation",
                            "citationphotodoc",
                            "citation_citationid",
                            "photodoc_photodocid"),
    
    USER        (           "User",
                            "login",
                            "loginphotodocs",
                            "user_userid",
                            "photodoc_id");
    
    private final String objectParentTitle;
    private final String objectParentTableName;
    private final String blobLinkTableName;
    private final String blobLinkTableParentIDFieldName;
    private final String blobLinkTablePhotodocIDFieldName;
    
    private BlobLinkEnum(String opt, String optn, String bltn, String bltpidfn, String bltpdidfn){
        objectParentTitle = opt;
        objectParentTableName = optn;
        blobLinkTableName = bltn;
        blobLinkTableParentIDFieldName = bltpidfn;
        blobLinkTablePhotodocIDFieldName = bltpdidfn;
    }

    /**
     * @return the objectParentTitle
     */
    public String getObjectParentTitle() {
        return objectParentTitle;
    }

    /**
     * @return the objectParentTableName
     */
    public String getObjectParentTableName() {
        return objectParentTableName;
    }

    /**
     * @return the blobLinkTableName
     */
    public String getBlobLinkTableName() {
        return blobLinkTableName;
    }

    /**
     * @return the blobLinkTableParentIDFieldName
     */
    public String getBlobLinkTableParentIDFieldName() {
        return blobLinkTableParentIDFieldName;
    }

    /**
     * @return the blobLinkTablePhotodocIDFieldName
     */
    public String getBlobLinkTablePhotodocIDFieldName() {
        return blobLinkTablePhotodocIDFieldName;
    }
    
}
