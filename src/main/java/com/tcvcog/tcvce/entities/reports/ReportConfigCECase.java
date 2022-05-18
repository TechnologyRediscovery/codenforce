/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class ReportConfigCECase extends Report{
    
    private CECaseDataHeavy cse;
    private Person caseManagerPerson;
    private PropertyDataHeavy propDH;
    
    private List<EventCnF> eventListForReport;
    private List<NoticeOfViolation> noticeListForReport;
    
    private boolean maximumOutputForUserRank; 
    // general
    private boolean includeCaseName; // add to ui
    
    // events
    private boolean includeOfficerOnlyEvents;
    private boolean includeMunicipalityDiclosedEvents;
    private boolean includeHiddenEvents;
    private boolean includeInactiveEvents;
    private boolean includeEventNotes;
    private boolean includeEventMetadata; // add to ui
    private boolean includeEventPersonLinks;
    
    // NOVs
    private boolean includeAllNotices;
    private boolean includeNoticeFullText;
    
    // CEARS
    private boolean includeActionRequsts;
    private boolean includeActionRequestPhotos;
    private int cearColumnCount;
    private int cearPhotoGridSquares;
    
    // fees and payments
    private boolean includeFeeAndPaymentInfo;
    
    // FIRS
    private boolean includeFieldInspectionReports;
    private boolean includeFieldInspectionReportsWithPhotos;
     
    // violations
    private boolean includeFullOrdinanceText;
    private boolean includeViolationPhotos;
    private int violationPhotoColumnCount;
    private int violationPhotoGridSquares;
    private boolean includeViolationNotes;
    
    // photos and blobs
    private boolean includePhotoFilenames;
    private boolean includePropertyBroadviewPhoto;
    private boolean includeDocDownloadLinks;
    private boolean includeCECasePhotoPool;
    private int ceCasePhotoPoolColumnCount;
    private int ceCasePhotoPoolGridSquares;
    
    // persons
    private boolean includeAssociatedPersons;
    private boolean includeAssociatedPersonsOnParentProperty;
    private boolean includePersonAddrPhoneEmail;
    /**
     * @return the cse
     */
    public CECaseDataHeavy getCse() {
        return cse;
    }

    /**
     * @return the includeHiddenEvents
     */
    public boolean isIncludeHiddenEvents() {
        return includeHiddenEvents;
    }

    /**
     * @return the includeInactiveEvents
     */
    public boolean isIncludeInactiveEvents() {
        return includeInactiveEvents;
    }

    /**
     * @return the includeEventNotes
     */
    public boolean isIncludeEventNotes() {
        return includeEventNotes;
    }

    /**
     * @return the includeNoticeFullText
     */
    public boolean isIncludeNoticeFullText() {
        return includeNoticeFullText;
    }

    /**
     * @return the includeFullOrdinanceText
     */
    public boolean isIncludeFullOrdinanceText() {
        return includeFullOrdinanceText;
    }

    /**
     * @param cse the cse to set
     */
    public void setCse(CECaseDataHeavy cse) {
        this.cse = cse;
    }

    /**
     * @param includeHiddenEvents the includeHiddenEvents to set
     */
    public void setIncludeHiddenEvents(boolean includeHiddenEvents) {
        this.includeHiddenEvents = includeHiddenEvents;
    }

    /**
     * @param includeInactiveEvents the includeInactiveEvents to set
     */
    public void setIncludeInactiveEvents(boolean includeInactiveEvents) {
        this.includeInactiveEvents = includeInactiveEvents;
    }

    /**
     * @param includeEventNotes the includeEventNotes to set
     */
    public void setIncludeEventNotes(boolean includeEventNotes) {
        this.includeEventNotes = includeEventNotes;
    }

    /**
     * @param includeNoticeFullText the includeNoticeFullText to set
     */
    public void setIncludeNoticeFullText(boolean includeNoticeFullText) {
        this.includeNoticeFullText = includeNoticeFullText;
    }

    /**
     * @param includeFullOrdinanceText the includeFullOrdinanceText to set
     */
    public void setIncludeFullOrdinanceText(boolean includeFullOrdinanceText) {
        this.includeFullOrdinanceText = includeFullOrdinanceText;
    }

    /**
     * @return the includeAllNotices
     */
    public boolean isIncludeAllNotices() {
        return includeAllNotices;
    }

    /**
     * @param includeAllNotices the includeAllNotices to set
     */
    public void setIncludeAllNotices(boolean includeAllNotices) {
        this.includeAllNotices = includeAllNotices;
    }

    /**
     * @return the includeMunicipalityDiclosedEvents
     */
    public boolean isIncludeMunicipalityDiclosedEvents() {
        return includeMunicipalityDiclosedEvents;
    }

    /**
     * @param includeMunicipalityDiclosedEvents the includeMunicipalityDiclosedEvents to set
     */
    public void setIncludeMunicipalityDiclosedEvents(boolean includeMunicipalityDiclosedEvents) {
        this.includeMunicipalityDiclosedEvents = includeMunicipalityDiclosedEvents;
    }

    /**
     * @return the includeOfficerOnlyEvents
     */
    public boolean isIncludeOfficerOnlyEvents() {
        return includeOfficerOnlyEvents;
    }

    /**
     * @param includeOfficerOnlyEvents the includeOfficerOnlyEvents to set
     */
    public void setIncludeOfficerOnlyEvents(boolean includeOfficerOnlyEvents) {
        this.includeOfficerOnlyEvents = includeOfficerOnlyEvents;
    }

    /**
     * @return the includeCaseName
     */
    public boolean isIncludeCaseName() {
        return includeCaseName;
    }

    /**
     * @param includeCaseName the includeCaseName to set
     */
    public void setIncludeCaseName(boolean includeCaseName) {
        this.includeCaseName = includeCaseName;
    }

    /**
     * @return the includeEventMetadata
     */
    public boolean isIncludeEventMetadata() {
        return includeEventMetadata;
    }

    /**
     * @param includeEventMetadata the includeEventMetadata to set
     */
    public void setIncludeEventMetadata(boolean includeEventMetadata) {
        this.includeEventMetadata = includeEventMetadata;
    }

    /**
     * @return the includeViolationPhotos
     */
    public boolean isIncludeViolationPhotos() {
        return includeViolationPhotos;
    }

    /**
     * @param includeViolationPhotos the includeViolationPhotos to set
     */
    public void setIncludeViolationPhotos(boolean includeViolationPhotos) {
        this.includeViolationPhotos = includeViolationPhotos;
    }

    /**
     * @return the eventListForReport
     */
    public List<EventCnF> getEventListForReport() {
        return eventListForReport;
    }

    /**
     * @return the noticeListForReport
     */
    public List<NoticeOfViolation> getNoticeListForReport() {
        return noticeListForReport;
    }

    /**
     * @param eventListForReport the eventListForReport to set
     */
    public void setEventListForReport(List<EventCnF> eventListForReport) {
        this.eventListForReport = eventListForReport;
    }

    /**
     * @param noticeListForReport the noticeListForReport to set
     */
    public void setNoticeListForReport(List<NoticeOfViolation> noticeListForReport) {
        this.noticeListForReport = noticeListForReport;
    }

    /**
     * @return the includeCECasePhotoPool
     */
    public boolean isIncludeCECasePhotoPool() {
        return includeCECasePhotoPool;
    }

    /**
     * @param includeCECasePhotoPool the includeCECasePhotoPool to set
     */
    public void setIncludeCECasePhotoPool(boolean includeCECasePhotoPool) {
        this.includeCECasePhotoPool = includeCECasePhotoPool;
    }

    /**
     * @return the includeAssociatedPersons
     */
    public boolean isIncludeAssociatedPersons() {
        return includeAssociatedPersons;
    }

    /**
     * @return the includeAssociatedPersonsOnParentProperty
     */
    public boolean isIncludeAssociatedPersonsOnParentProperty() {
        return includeAssociatedPersonsOnParentProperty;
    }

    /**
     * @return the includePersonAddrPhoneEmail
     */
    public boolean isIncludePersonAddrPhoneEmail() {
        return includePersonAddrPhoneEmail;
    }

    /**
     * @return the includeViolationNotes
     */
    public boolean isIncludeViolationNotes() {
        return includeViolationNotes;
    }

    /**
     * @param includeAssociatedPersons the includeAssociatedPersons to set
     */
    public void setIncludeAssociatedPersons(boolean includeAssociatedPersons) {
        this.includeAssociatedPersons = includeAssociatedPersons;
    }

    /**
     * @param includeAssociatedPersonsOnParentProperty the includeAssociatedPersonsOnParentProperty to set
     */
    public void setIncludeAssociatedPersonsOnParentProperty(boolean includeAssociatedPersonsOnParentProperty) {
        this.includeAssociatedPersonsOnParentProperty = includeAssociatedPersonsOnParentProperty;
    }

    /**
     * @param includePersonAddrPhoneEmail the includePersonAddrPhoneEmail to set
     */
    public void setIncludePersonAddrPhoneEmail(boolean includePersonAddrPhoneEmail) {
        this.includePersonAddrPhoneEmail = includePersonAddrPhoneEmail;
    }

    /**
     * @param includeViolationNotes the includeViolationNotes to set
     */
    public void setIncludeViolationNotes(boolean includeViolationNotes) {
        this.includeViolationNotes = includeViolationNotes;
    }

    /**
     * @return the includeFieldInspectionReports
     */
    public boolean isIncludeFieldInspectionReports() {
        return includeFieldInspectionReports;
    }

    /**
     * @return the includeFieldInspectionReportsWithPhotos
     */
    public boolean isIncludeFieldInspectionReportsWithPhotos() {
        return includeFieldInspectionReportsWithPhotos;
    }

    /**
     * @param includeFieldInspectionReports the includeFieldInspectionReports to set
     */
    public void setIncludeFieldInspectionReports(boolean includeFieldInspectionReports) {
        this.includeFieldInspectionReports = includeFieldInspectionReports;
    }

    /**
     * @param includeFieldInspectionReportsWithPhotos the includeFieldInspectionReportsWithPhotos to set
     */
    public void setIncludeFieldInspectionReportsWithPhotos(boolean includeFieldInspectionReportsWithPhotos) {
        this.includeFieldInspectionReportsWithPhotos = includeFieldInspectionReportsWithPhotos;
    }

    /**
     * @return the includeDocDownloadLinks
     */
    public boolean isIncludeDocDownloadLinks() {
        return includeDocDownloadLinks;
    }

    /**
     * @param includeDocDownloadLinks the includeDocDownloadLinks to set
     */
    public void setIncludeDocDownloadLinks(boolean includeDocDownloadLinks) {
        this.includeDocDownloadLinks = includeDocDownloadLinks;
    }

    /**
     * @return the includeFeeAndPaymentInfo
     */
    public boolean isIncludeFeeAndPaymentInfo() {
        return includeFeeAndPaymentInfo;
    }

    /**
     * @param includeFeeAndPaymentInfo the includeFeeAndPaymentInfo to set
     */
    public void setIncludeFeeAndPaymentInfo(boolean includeFeeAndPaymentInfo) {
        this.includeFeeAndPaymentInfo = includeFeeAndPaymentInfo;
    }

    /**
     * @return the includeActionRequsts
     */
    public boolean isIncludeActionRequsts() {
        return includeActionRequsts;
    }

    /**
     * @param includeActionRequsts the includeActionRequsts to set
     */
    public void setIncludeActionRequsts(boolean includeActionRequsts) {
        this.includeActionRequsts = includeActionRequsts;
    }

    /**
     * @return the propDH
     */
    public PropertyDataHeavy getPropDH() {
        return propDH;
    }

    /**
     * @param propDH the propDH to set
     */
    public void setPropDH(PropertyDataHeavy propDH) {
        this.propDH = propDH;
    }

    /**
     * @return the maximumOutputForUserRank
     */
    public boolean isMaximumOutputForUserRank() {
        return maximumOutputForUserRank;
    }

    /**
     * @param maximumOutputForUserRank the maximumOutputForUserRank to set
     */
    public void setMaximumOutputForUserRank(boolean maximumOutputForUserRank) {
        this.maximumOutputForUserRank = maximumOutputForUserRank;
    }

    /**
     * @return the includePropertyBroadviewPhoto
     */
    public boolean isIncludePropertyBroadviewPhoto() {
        return includePropertyBroadviewPhoto;
    }

    /**
     * @param includePropertyBroadviewPhoto the includePropertyBroadviewPhoto to set
     */
    public void setIncludePropertyBroadviewPhoto(boolean includePropertyBroadviewPhoto) {
        this.includePropertyBroadviewPhoto = includePropertyBroadviewPhoto;
    }

   

    /**
     * @param includePhotoFilenames the includePhotoFilenames to set
     */
    public void setIncludePhotoFilenames(boolean includePhotoFilenames) {
        this.includePhotoFilenames = includePhotoFilenames;
    }

    /**
     * @return the violationPhotoColumnCount
     */
    public int getViolationPhotoColumnCount() {
        return violationPhotoColumnCount;
    }

    /**
     * @return the violationPhotoGridSquares
     */
    public int getViolationPhotoGridSquares() {
        return violationPhotoGridSquares;
    }

    /**
     * @return the includePhotoFilenames
     */
    public boolean isIncludePhotoFilenames() {
        return includePhotoFilenames;
    }

    /**
     * @return the ceCasePhotoPoolColumnCount
     */
    public int getCeCasePhotoPoolColumnCount() {
        return ceCasePhotoPoolColumnCount;
    }

    /**
     * @return the ceCasePhotoPoolGridSquares
     */
    public int getCeCasePhotoPoolGridSquares() {
        return ceCasePhotoPoolGridSquares;
    }

    /**
     * @param violationPhotoColumnCount the violationPhotoColumnCount to set
     */
    public void setViolationPhotoColumnCount(int violationPhotoColumnCount) {
        this.violationPhotoColumnCount = violationPhotoColumnCount;
    }

    /**
     * @param violationPhotoGridSquares the violationPhotoGridSquares to set
     */
    public void setViolationPhotoGridSquares(int violationPhotoGridSquares) {
        this.violationPhotoGridSquares = violationPhotoGridSquares;
    }

    /**
     * @param ceCasePhotoPoolColumnCount the ceCasePhotoPoolColumnCount to set
     */
    public void setCeCasePhotoPoolColumnCount(int ceCasePhotoPoolColumnCount) {
        this.ceCasePhotoPoolColumnCount = ceCasePhotoPoolColumnCount;
    }

    /**
     * @param ceCasePhotoPoolGridSquares the ceCasePhotoPoolGridSquares to set
     */
    public void setCeCasePhotoPoolGridSquares(int ceCasePhotoPoolGridSquares) {
        this.ceCasePhotoPoolGridSquares = ceCasePhotoPoolGridSquares;
    }

    /**
     * @return the includeEventPersonLinks
     */
    public boolean isIncludeEventPersonLinks() {
        return includeEventPersonLinks;
    }

    /**
     * @param includeEventPersonLinks the includeEventPersonLinks to set
     */
    public void setIncludeEventPersonLinks(boolean includeEventPersonLinks) {
        this.includeEventPersonLinks = includeEventPersonLinks;
    }

    /**
     * @return the caseManagerPerson
     */
    public Person getCaseManagerPerson() {
        return caseManagerPerson;
    }

    /**
     * @param caseManagerPerson the caseManagerPerson to set
     */
    public void setCaseManagerPerson(Person caseManagerPerson) {
        this.caseManagerPerson = caseManagerPerson;
    }

    /**
     * @return the includeActionRequestPhotos
     */
    public boolean isIncludeActionRequestPhotos() {
        return includeActionRequestPhotos;
    }

    /**
     * @return the cearColumnCount
     */
    public int getCearColumnCount() {
        return cearColumnCount;
    }

    /**
     * @return the cearPhotoGridSquares
     */
    public int getCearPhotoGridSquares() {
        return cearPhotoGridSquares;
    }

    /**
     * @param includeActionRequestPhotos the includeActionRequestPhotos to set
     */
    public void setIncludeActionRequestPhotos(boolean includeActionRequestPhotos) {
        this.includeActionRequestPhotos = includeActionRequestPhotos;
    }

    /**
     * @param cearColumnCount the cearColumnCount to set
     */
    public void setCearColumnCount(int cearColumnCount) {
        this.cearColumnCount = cearColumnCount;
    }

    /**
     * @param cearPhotoGridSquares the cearPhotoGridSquares to set
     */
    public void setCearPhotoGridSquares(int cearPhotoGridSquares) {
        this.cearPhotoGridSquares = cearPhotoGridSquares;
    }

   
    
}

