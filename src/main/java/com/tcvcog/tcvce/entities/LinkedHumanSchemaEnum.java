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
public enum LinkedHumanSchemaEnum {
    
    OCCAPPLICATIONHUMAN ("occpermitapplicationhuman","OccApplicationHuman", "???", ""),  // for Jurplel to update
    CECASEHUMAN ("humancecase", "CECaseHuman", "linkid", "cecase_caseid"), 
    OCCPERIODHUMAN ("humanoccperiod","OccPeriodHuman", "linkid", "occperiod_periodid"), 
    PARCELHUMAN ("humanparcel","ParcelHuman", "linkid", "parcel_parcelkey"), 
    PARCELUNITHUMAN ("humanparcelunit","ParcelUnitHuman", "linkid", "parcelunit_unitid"), 
    CITATIONHUMAN ("citationhuman","CitationHuman", "linkid", "citation_citationid"), 
    EVENTHUMAN ("eventhuman","EventHuman", "linkid", "event_eventid"), 
    MUNIHUMAN ("humanmuni","MuniHuman","linkid", "muni_municode");
    
    private final String LINKED_TABLE_NAME;
    private final String LINK_ROLE_SCHEMA_TYPE_STRING;
    private final String LINKING_TABLE_PK_FIELD;
    private final String TARGET_TABLE_FK_FIELD;

    private LinkedHumanSchemaEnum(String ltn, String ts, String ltpk, String ttfk){
        LINKED_TABLE_NAME = ltn;
        LINK_ROLE_SCHEMA_TYPE_STRING = ts;
        LINKING_TABLE_PK_FIELD = ltpk;
        TARGET_TABLE_FK_FIELD = ttfk;
    }
    
    public String getLinkedTableName(){
        return LINKED_TABLE_NAME;
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
    
}
