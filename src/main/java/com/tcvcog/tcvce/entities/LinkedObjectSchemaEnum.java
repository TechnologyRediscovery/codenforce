/*
 * Copyright (C) 2021 Technology Rediscovery LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.entities;

/**
 * Enumerates the schemas for linked object roles and maps these roles
 * to actual DB identifiers
 * 
 * @author sylvia
 */
public enum LinkedObjectSchemaEnum {
    
    OccApplicationHuman (   
                            "Occupancy Application",
                            "public.occpermitapplicationhuman",
                            "OccApplicationHuman", 
                            "???", 
                            "",
                            null,
                            "???",
                            LinkedObjectFamilyEnum.HUMAN,
                            false
                        ),  // for Jurplel to update
    
    CECaseHuman         (   
                            "Code Enf. Case",
                            "public.humancecase", 
                            "CECaseHuman", 
                            "linkid", 
                            "cecase_caseid",
                            null,
                            "humancecase_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
        
                        ), 
    
    OccPeriodHuman      (
                            "Occupancy Period",
                            "public.humanoccperiod",
                            "OccPeriodHuman", 
                            "linkid", 
                            "occperiod_periodid",
                            null,
                            "humanoccperiod_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    ParcelHuman         (   
                            "Parcel",
                            "public.humanparcel",
                            "ParcelHuman", 
                            "linkid", 
                            "parcel_parcelkey",
                            null,
                            "humanparcel_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    ParcelUnitHuman     (   
                            "Parcel Unit",
                            "public.humanparcelunit",
                            "ParcelUnitHuman", 
                            "linkid", 
                            "parcelunit_unitid",
                            null,
                            "parcelunithuman_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ),
    
    CitationHuman       (
                            "Citation",
                            "public.citationhuman",
                            "CitationHuman", 
                            "linkid", 
                            "citation_citationid",
                            null,
                            "citationhuman_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    CitationDocketHuman       (
                            "Citation Docket",
                            "public.citationdockethuman",
                            "CitationDocketHuman", 
                            "linkid", 
                            "docketno_docketid",
                            null,
                            "citationdockethuman_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            false
                        ), 
    
    EventHuman          (
                            "Event",
                            "public.eventhuman",
                            "EventHuman", 
                            "linkid", 
                            "event_eventid",
                            null,
                            "eventhuman_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    MuniHuman           (
                            "Municipality",
                            "public.humanmuni",
                            "MuniHuman",
                            "linkid", 
                            "muni_municode",
                            null,
                            "humanmuni_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ),
    
    MailingaddressHuman (   
                            "Person",
                            "humanmailingaddress",
                            "MailingaddressHuman", 
                            "linkid",
                            "humanmailing_humanid",
                            "humanmailing_addressid",
                            "humanmailing_linkid_seq",
                            LinkedObjectFamilyEnum.MAILING,
                            true
                        ), 
    ParcelMailingaddress  (
                            "Parcel",
                            "parcelmailingaddress",
                            "ParcelMailingaddress", 
                            "linkid", 
                            "parcel_parcelkey",
                            "mailingaddress_addressid",
                            "parcelmailing_linkid_seq",
                            LinkedObjectFamilyEnum.MAILING,
                            true
                        ),
    CITATION_CODEVIOLATION  (
                            "Citation",
                            "citationviolation",
                            "", 
                            "citationviolationid", 
                            "codeviolation_violationid",
                            null,
                            "citationviolation_cvid_seq",
                            LinkedObjectFamilyEnum.MAILING,
                            true
                            
                        );

    
    private final String TARGET_OBJECT_FRIENDLY_NAME;
    private final String LINKING_TABLE_NAME;
    private final String LINK_ROLE_SCHEMA_TYPE_STRING;
    private final String LINKING_TABLE_PK_FIELD;
    private final String TARGET_TABLE_FK_FIELD;
    private final String LINKED_OBJECT_FK_FIELD;
    private final String LINKING_TABLE_SEQ_ID;
    private final LinkedObjectFamilyEnum FAMILY;
    private final boolean ACTIVELINK;

    private LinkedObjectSchemaEnum   (
                                        String friendly,
                                        String ltn, 
                                        String ts, 
                                        String ltpk, 
                                        String ttfk,
                                        String lofk,
                                        String seqid,
                                        LinkedObjectFamilyEnum fam,
                                        boolean active
                                    )    {
        TARGET_OBJECT_FRIENDLY_NAME = friendly;
        LINKING_TABLE_NAME = ltn;
        LINK_ROLE_SCHEMA_TYPE_STRING = ts;
        LINKING_TABLE_PK_FIELD = ltpk;
        TARGET_TABLE_FK_FIELD = ttfk;
        LINKED_OBJECT_FK_FIELD = lofk;
        LINKING_TABLE_SEQ_ID = seqid;
        FAMILY = fam;
        ACTIVELINK = active;
    }
    
    public String getLinkingTableName(){
        return LINKING_TABLE_NAME;
    }
    
    public String getRoleSChemaTypeString(){
        return LINK_ROLE_SCHEMA_TYPE_STRING;
    }
    
    public String getLinkingTablePKField(){
        return LINKING_TABLE_PK_FIELD;
    }
    
    public String getTargetTableFKField(){
        return TARGET_TABLE_FK_FIELD;
    }
    
    public String getLinkingTableSequenceID(){
        return LINKING_TABLE_SEQ_ID;
    
    }
    public LinkedObjectFamilyEnum getLinkedObjectFamilyEnum(){
        return FAMILY;
    }

    /**
     * @return the TARGET_OBJECT_FRIENDLY_NAME
     */
    public String getTARGET_OBJECT_FRIENDLY_NAME() {
        return TARGET_OBJECT_FRIENDLY_NAME;
    }

    /**
     * @return the ACTIVELINK
     */
    public boolean isACTIVELINK() {
        return ACTIVELINK;
    }

    /**
     * @return the LINKED_OBJECT_FK_FIELD
     */
    public String getLINKED_OBJECT_FK_FIELD() {
        return LINKED_OBJECT_FK_FIELD;
    }
    
}
