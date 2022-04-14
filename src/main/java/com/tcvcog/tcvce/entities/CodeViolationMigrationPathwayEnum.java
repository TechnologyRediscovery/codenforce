/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Specifies pathways for migrating code violations 
 * from a case to another case, or from an inspection to 
 * a case
 * @author Ellen Bascomb of Apartment 31Y
 */
public enum CodeViolationMigrationPathwayEnum {
    
    CASE_TO_NEW_CASE(       DomainEnum.CODE_ENFORCEMENT, 
                            "Migrate violation(s) to a new case",
                            "This violation was migrated from a code enforcement case ID [CASEID] at [NOW].",
                            true),
    
    CASE_TO_EXISTING_CASE(  DomainEnum.CODE_ENFORCEMENT, 
                            "Migrate violation(s) to an existing case",
                            "This violation was migrated  from a code enforcement case ID [CASEID] at [NOW].",
                            false),
    
    OCC_INSPECTION_TO_NEW_CASE( DomainEnum.OCCUPANCY, 
                                "Migrate violated ordiances from this inspection to a new case",
                                "This violation was migrated from field inspection ID [FINID] on occupancy period ID [PERIODID] at [NOW].",
                                true),    
    
    OCC_INSPECTION_TO_EXISTING_CASE(DomainEnum.OCCUPANCY, 
                                "Migrate violated ordinances from this inspection to an existing case",
                                "This violation was migrated from field inspection ID [FINID] on occupancy period ID [PERIODID] at [NOW].",
                                false),
    
    CASE_INSPECTION_TO_HOST_CASE(DomainEnum.CODE_ENFORCEMENT, 
                                "Transfer violated ordinances from this inspection to the host case",
                                "This violation was transferred from this case's field inspection ID [FINID] at [NOW].",
                                false),
    
    CASE_INSPECTION_TO_NEW_CASE(DomainEnum.CODE_ENFORCEMENT, 
                                "Migrate violated ordinances from this inspection to a new case",
                                "This violation was migrated from field inspection ID [FINID] on case ID [CASEID] at [NOW].",
                                false),
    
    CASE_INSPECTION_TO_EXISTING_CASE(DomainEnum.CODE_ENFORCEMENT, 
                                "Migrate violated ordinances from this inspection to an existing case",
                                "This violation was migrated from field inspection ID [FINID] on case ID [CASEID] at [NOW].",
                                true);
    
    private final DomainEnum domain;
    private final String description;
    private final String violationNoteInjectableString;
    private final boolean useExistingCase;
    
    private CodeViolationMigrationPathwayEnum(DomainEnum de, String d, String vn, boolean excse){
        domain = de;
        description = d;
        violationNoteInjectableString = vn;
        useExistingCase = excse;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the domain
     */
    public DomainEnum getDomain() {
        return domain;
    }

    /**
     * @return the violationNoteInjectableString
     */
    public String getViolationNoteInjectableString() {
        return violationNoteInjectableString;
    }

    /**
     * @return the useExistingCase
     */
    public boolean isUseExistingCase() {
        return useExistingCase;
    }
}
