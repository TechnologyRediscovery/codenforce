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
public enum LinkedObjectRoleSchemaEnum {
    
    OCCAPPLICATIONHUMAN ("occpermitapplicationhuman","OccApplicationHuman", "???"),  // for Jurplel to update
    CECASEHUMAN ("humancecase", "CECaseHuman", "linkid"), 
    OCCPERIODHUMAN ("humanoccperiod","OccPeriodHuman", ""), 
    PARCELHUMAN ("humanparcel","ParcelHuman", ""), 
    PARCELUNITHUMAN ("humanparcelunit","ParcelUnitHuman", ""), 
    CITATIONHUMAN ("citationhuman","CitationHuman", ""), 
    CITATIONCODEVIOLATION ("citationcodeviolation","CitationCodeViolation", ""),
    EVENTHUMAN ("eventhuman","EventHuman", ""), 
    MAILINGADDRESSHUMAN ("humanmailingaddress","MailingaddressHuman", ""), 
    PARCELMAILINGADDRESS  ("parcelmailingaddress","ParcelMailingaddress", ""),
    MUNIHUMAN ("humanmuni","MuniHuman","");
    
    private final String LINKED_TABLE_NAME;
    private final String LINK_ROLE_SCHEMA_TYPE_STRING;
    private final String LINKED_TABLE_PK;
    
    private LinkedObjectRoleSchemaEnum(String ltn, String ts, String pk){
        LINKED_TABLE_NAME = ltn;
        LINK_ROLE_SCHEMA_TYPE_STRING = ts;
        LINKED_TABLE_PK = pk;
    }
    
    public String getLinkedTableName(){
        return LINKED_TABLE_NAME;
    }
    
    public String getRoleSChemaTypeString(){
        return LINK_ROLE_SCHEMA_TYPE_STRING;
    }
    
    public String getLinkedTablePKField(){
        return LINKED_TABLE_PK;
    }
    
}
