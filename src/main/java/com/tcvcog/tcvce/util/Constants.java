/*
 * Copyright (C) 2017 cedba
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
package com.tcvcog.tcvce.util;

import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import java.io.Serializable;

/**
 *
 * @author cedba
 */
public class Constants implements Serializable {

    /**
     * Creates a new instance of Constants
     */
    public Constants() {
    }
    
    // Coordinator Keys
    public final static String PROPERTY_COORDINATOR_SCOPE = "sessionScope.";
    public final static String PROPERTY_COORDINATOR_KEY = "propertyCoordinator";
    
    public final static String USER_COORDINATOR_SCOPE = "applicationScope.";
    public final static String USER_COORDINATOR_KEY = "userCoordinator";
    
    // blasted unit numbers often have letters in them!
    public final static String DEFAULT_UNIT_NUMBER = "-1";
    public final static String TEMP_UNIT_NUM = "-9";
    
    // NOV keys
    public final static String NOV_VIOLATIONS_INJECTION_POINT = "***VIOLATIONS***";
    
    
    
    // Object management configuration parameters
    public final static int MAX_BOB_HISTORY_SIZE = 30;
    
    public final static int MAX_CONNECTIONS = 1000;
    
    // these are the names of the properties files/bundles

    public final static String MESSAGE_TEXT= "messagetext";
    public final static String LOGGING_CATEGORIES = "loggingcategories";
    public final static String EVENT_CATEGORY_BUNDLE = "eventCategories";
    public final static String DB_FIXED_VALUE_BUNDLE = "dbFixedValueLookup";
    public final static String DB_CONNECTION_PARAMS = "dbConnectionParams";
    public final static String VIOLATIONS_BUNDLE = "violations";
    
    public final static String STYLE_CLASS_INACTIVE_CASE_PHASE = "stage-inactive";
    
//    SESSION REQUEST KEYS
    public final static String PARAM_JSESS = "JSESSIONID";
    public final static String PARAM_USERAGENT = "User-Agent";
    public final static String PARAM_DATERAW = "Date";
     
    public final static String FMT_SEARCH_HEAD_QUERYOG =       "----------- QUERY LOG -------------";
    public final static String FMT_SEARCH_HEAD_FILTERLOG =       "----------- filter log -------------";
    public final static String FMT_NOTE_START =             "----------NOTE-----------";
    public final static String FMT_NOTE_END =               "-------------------------";
    public final static String FMT_NOTE_SEP_INTERNAL =      "-------------------------";
    public final static String FMT_HTML_BREAK = "<br>";
    public final static String FMT_SPACE_LITERAL = " ";
    public final static String FMT_SPLAT = "*";
    
    public final static String FMT_NOTEBYLINE = "Created by: ";
    public final static String FMT_USER = "Username: ";
    public final static String FMT_ID = "ID:";
    public final static String FMT_AT = " at ";
    public final static String FMT_CONTENT = "Content: ";
    
    public final static String FMT_DTYPE_SYMB_USERNAME = "+";
    
    public final static String FMT_DTYPE_OBJECTID_INLINEOPEN = "(";
    public final static String FMT_DTYPE_OBJECTID_INLINECLOSED = ")";
    
    public final static String FMT_DTYPE_KEY_TIMESTAMP = "ts";
    
    public final static String FMT_DTYPE_KEYVALDESCSEP = ":";
    public final static String FMT_DTYPE_KEY_SEP_DESC = "-";
    
    public final static String FMT_DTYPE_KEY_TIMESTAMP_CREATE = "cr";
    public final static String FMT_DTYPE_KEY_TIMESTAMP_LOCK = "lock";
    public final static String FMT_DTYPE_KEY_TIMESTAMP_DEACTIVATE = "deac";
    
    public final static String FMT_SIGNATURELEAD = "Signature: ";
    public final static String FMT_FIELDKVSEP_WSPACE = ": ";
    
    
    
    
    
    
}
