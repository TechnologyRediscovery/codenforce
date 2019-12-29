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
public enum SearchParamsOccPeriodDateFields {
    
    CREATED_TS("Creation timestamp", "createdts"), // in occperiod
    TYPE_CERTIFIED_TS, // in occperiod
    PERIOD_START_DATE, // in occperiod
    PERIOD_END_DATE, // in occperiod
    START_DATE_CERTIFIED_TS, // in occperiod
    END_DATE_CERTIFIED_TS, // in occperiod
    AUTHORIZATION_TS, // in occperiod
    INSPECTION_EFFECTIVEDATE, // in occinspection
    PASSEDINSPECTION_TS, // in occinspection
    THIRDPARTY_INSPECTOR_APPROVAL_TS, // in occinspection
    PERMIT_ISSUANCE_DATE // in occpermit
    ;
    
    private final String title;
    private final String dbField;
    
    private SearchParamsOccPeriodDateFields(String t, String db){
        title = t;
        dbField = db;
    }
    
}
