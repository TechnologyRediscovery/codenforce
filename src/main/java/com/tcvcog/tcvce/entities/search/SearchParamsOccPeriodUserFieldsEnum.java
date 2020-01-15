/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities.search;

/**
 *
 * @author sylvia
 */
public enum SearchParamsOccPeriodUserFieldsEnum 
        implements IFace_userFieldHolder{
    
    CREATED_USER                ("Period creator", "occperiod.createdby_userid"),
    MANAGER_USER                ("Period manager", "occperiod.manager_userid"),
    TYPE_CERTIFYING_USER        ("Period certifier", "occperiod.typecertifiedby_userid"),
    START_DATE_CERTIFYING_USER  ("Period start date certifier", "occperiod.startdatecertifiedby_userid"),
    END_DATE_CERTIFYING_USER    ("Period end date certifier", "occperiod.enddatecertifiedby_userid"),
    AUTHORIZIING_USER           ("Period authorizer", "occperiod.authorizedby_userid"),
    INSPECTOR_USER              ("Inspector", "occperiod.inspector_userid"),
    PASSEDINSPETION_AUTH_USER   ("Inspection pass certifier", "occperiod.passedinspection_userid"),
    PERMIT_ISSUEDBY_USER        ("Permit issuer", "occperiod.issuedby_userid");
    
    private final String title;
    private final String dbField;
    
    private SearchParamsOccPeriodUserFieldsEnum(String t, String db){
        title = t;
        dbField = db;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String extractUserFieldString() {
        return dbField;
    }
    
}
