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
    
    OCCAPPLICATIONHUMAN (   
                            "Occupancy Application - Person",
                            "public.occpermitapplicationhuman",
                            "OccApplicationHuman", 
                            "???", 
                            "",
                            "???",
                            LinkedObjectFamilyEnum.HUMAN,
                            false
                        ),  // for Jurplel to update
    
    CECASEHUMAN         (   
                            "Code Enf. Case - Person",
                            "public.humancecase", 
                            "CECaseHuman", 
                            "linkid", 
                            "cecase_caseid",
                            "humancecase_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
        
                        ), 
    
    OCCPERIODHUMAN      (
                            "Occupancy Period - Person",
                            "public.humanoccperiod",
                            "OccPeriodHuman", 
                            "linkid", 
                            "occperiod_periodid",
                            "humanoccperiod_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    PARCELHUMAN         (   
                            "Parcel - Person",
                            "public.humanparcel",
                            "ParcelHuman", 
                            "linkid", 
                            "parcel_parcelkey",
                            "humanparcel_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    PARCELUNITHUMAN     (   
                            "Parcel Unit - Person",
                            "public.humanparcelunit",
                            "ParcelUnitHuman", 
                            "linkid", 
                            "parcelunit_unitid",
                            "parcelunithuman_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ),
    
    CITATIONHUMAN       (
                            "Citation - Person",
                            "public.citationhuman",
                            "CitationHuman", 
                            "linkid", 
                            "citation_citationid",
                            "citationhuman_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    CITATIONDCKETHUMAN       (
                            "Citation Docket - Person",
                            "public.citationdockethuman",
                            "CitationDocketHuman", 
                            "linkid", 
                            "docketno_docketid",
                            "citationdockethuman_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            false
                        ), 
    
    EVENTHUMAN          (
                            "Event - Person",
                            "public.eventhuman",
                            "EventHuman", 
                            "linkid", 
                            "event_eventid",
                            "eventhuman_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ), 
    
    MUNIHUMAN           (
                            "Municipality - Person",
                            "public.humanmuni",
                            "MuniHuman",
                            "linkid", 
                            "muni_municode",
                            "humanmuni_linkid_seq",
                            LinkedObjectFamilyEnum.HUMAN,
                            true
                        ),
    
    HUMANMAILINGADDRESS (   
                            "Mailing Address - Person",
                            "humanmailingaddress",
                            "MailingaddressHuman", 
                            "linkid",
                            "humanmailing_addressid",
                            "humanmailing_linkid_seq",
                            LinkedObjectFamilyEnum.MAILING,
                            true
                        ), 
    PARCELMAILINGADDRESS  (
                            "Parcel - Mailing Address",
                            "parcelmailingaddress",
                            "ParcelMailingaddress", 
                            "", 
                            "",
                            "",
                            LinkedObjectFamilyEnum.MAILING,
                            true
                        ),
    CITATION_CODEVIOLATION  (
                            "Citation - Code Violation",
                            "citationviolation",
                            "", 
                            "citationviolationid", 
                            "codeviolation_violationid",
                            "citationviolation_cvid_seq",
                            LinkedObjectFamilyEnum.MAILING,
                            true
                            
                        );

    
    private final String TARGET_OBJECT_FRIENDLY_NAME;
    private final String LINKING_TABLE_NAME;
    private final String LINK_ROLE_SCHEMA_TYPE_STRING;
    private final String LINKING_TABLE_PK_FIELD;
    private final String TARGET_TABLE_FK_FIELD;
    private final String LINKING_TABLE_SEQ_ID;
    private final LinkedObjectFamilyEnum FAMILY;
    private final boolean ACTIVELINK;

    private LinkedObjectSchemaEnum   (
                                        String friendly,
                                        String ltn, 
                                        String ts, 
                                        String ltpk, 
                                        String ttfk,
                                        String seqid,
                                        LinkedObjectFamilyEnum fam,
                                        boolean active
                                    )    {
        TARGET_OBJECT_FRIENDLY_NAME = friendly;
        LINKING_TABLE_NAME = ltn;
        LINK_ROLE_SCHEMA_TYPE_STRING = ts;
        LINKING_TABLE_PK_FIELD = ltpk;
        TARGET_TABLE_FK_FIELD = ttfk;
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
    
    public String getLinkedTablePKField(){
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
    
}
