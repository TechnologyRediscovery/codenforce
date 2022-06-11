/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * @author Mike-Faux
 */
public enum ManagedSchemaEnum {
    Icon(
            "Icon",
            "icon",
            "Icon",
            "icon_iconid",
            "iconid",
            "",
            "",
            null,
            null
    ),
    PropertyUseType(
            "Property Use Type",
            "put",
            "PropertyUseType",
            "icon_iconid",
            "iconid",
            "",
            "",
            null,
            null
    ),
    BlobType(
            "Blob Type",
            "icon",
            "Icon",
            "icon_iconid",
            "iconid",
            "",
            "",
            null,
            null)/*,
    BobSource(),
    CEActionRequestIssueType(),
    CEActionRequestStatus(),
    CitationStatus(),
    CitationFilingType(),
    ContactPhoneType(),
    ImprovementType(),
    LinkedObjectRole(),
    LogCategory(),
    MoneyPaymentType(),
    OCCPeriodType(),
    OCCInspectionCause(),
    OCCInspectionDetermination(),
    TaxStatus(),
    TextBlockCategory(),
    IntensityClass()/*,
    CourtEntity()*/;

    private final String TARGET_OBJECT_FRIENDLY_NAME;
    private final String TARGET_TABLE_NAME;
    private final String SCHEMA_TYPE_STRING;
    private final String FK_ID_FIELD;
    private final String TARGET_TABLE_ID_FIELD;
    private final String TARGET_TABLE_NAME_FIELD;
    private final String TARGET_TABLE_ICON_ID_FIELD;
    private final String TARGET_TABLE_DEACTIVATED_TS_FIELD;
    private final String[] EXTENDED_VARIABLE_LIST;

    private ManagedSchemaEnum(String tofn, String ttn, String sts, String fif, String ttif, String tttf, String ttiif, String ttdtf, String[] evl) {
        TARGET_OBJECT_FRIENDLY_NAME = tofn;
        TARGET_TABLE_NAME = ttn;
        SCHEMA_TYPE_STRING = sts;
        FK_ID_FIELD = fif;
        TARGET_TABLE_ID_FIELD = ttif;
        TARGET_TABLE_NAME_FIELD = tttf;
        TARGET_TABLE_ICON_ID_FIELD = ttiif;
        TARGET_TABLE_DEACTIVATED_TS_FIELD = ttdtf;
        EXTENDED_VARIABLE_LIST = evl;
    }

    /**
     * @return the TARGET_OBJECT_FRIENDLY_NAME
     */
    public String getTARGET_OBJECT_FRIENDLY_NAME() {
        return TARGET_OBJECT_FRIENDLY_NAME;
    }

    /**
     * @return the TARGET_TABLE_NAME
     */
    public String getTARGET_TABLE_NAME() {
        return TARGET_TABLE_NAME;
    }

    /**
     * @return the SCHEMA_TYPE_STRING
     */
    public String getSCHEMA_TYPE_STRING() {
        return SCHEMA_TYPE_STRING;
    }

    /**
     * @return the TARGET_TABLE_ID_FIELD
     */
    public String getTARGET_TABLE_ID_FIELD() {
        return TARGET_TABLE_ID_FIELD;
    }

    /**
     * @return the TARGET_TABLE_NAME_FIELD
     */
    public String getTARGET_TABLE_NAME_FIELD() {
        return TARGET_TABLE_NAME_FIELD;
    }

    /**
     * @return the TARGET_TABLE_ICON_ID_FIELD
     */
    public String getTARGET_TABLE_ICON_ID_FIELD() {
        return TARGET_TABLE_ICON_ID_FIELD;
    }

    /**
     * @return the TARGET_TABLE_DEACTIVATED_TS_FIELD
     */
    public String getTARGET_TABLE_DEACTIVATED_TS_FIELD() {
        return TARGET_TABLE_DEACTIVATED_TS_FIELD;
    }

    /**
     * @return the EXTENDED_VARIABLE_LIST as a 'ObjectField':'TableField' List
     */
    public String[] getEXTENDED_VARIABLE_LIST() {
        return EXTENDED_VARIABLE_LIST;
    }

    /**
     * @return the FK_ID_FIELD
     */
    public String getFK_ID_FIELD() {
        return FK_ID_FIELD;
    }
}
