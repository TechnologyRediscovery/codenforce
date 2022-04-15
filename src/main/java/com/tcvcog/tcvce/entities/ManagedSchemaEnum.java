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
    Icon    (
                "Icon",
                "icon"
            );
    private final String TARGET_OBJECT_FRIENDLY_NAME;
    private final String LINKING_TABLE_NAME;
    private final String LINK_ROLE_SCHEMA_TYPE_STRING;
    private final String LINKING_TABLE_PK_FIELD;
    private final String TARGET_TABLE_FK_FIELD;
    private final String LINKED_OBJECT_FK_FIELD;
    private final String LINKING_TABLE_SEQ_ID;
    
    private ManagedSchemaEnum(String tofn,String ltn){
        TARGET_OBJECT_FRIENDLY_NAME = tofn;
        LINKING_TABLE_NAME = ltn;
        LINK_ROLE_SCHEMA_TYPE_STRING;
        LINKING_TABLE_PK_FIELD;
        TARGET_TABLE_FK_FIELD;
        LINKED_OBJECT_FK_FIELD;
        LINKING_TABLE_SEQ_ID;
    }

    /**
     * @return the TARGET_OBJECT_FRIENDLY_NAME
     */
    public String getTARGET_OBJECT_FRIENDLY_NAME() {
        return TARGET_OBJECT_FRIENDLY_NAME;
    }
}
