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
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class Credential implements Serializable{
    
    private final UserMuniAuthPeriod governingAuthPeriod;
    
    private final String signature;
    
    private final boolean hasDeveloperPermissions;
    private final boolean hasSysAdminPermissions;
    private final boolean hasCOGStaffPermissions;
    private final boolean hasEnfOfficialPermissions;
    private final boolean hasMuniStaffPermissions;
    private final boolean hasMuniReaderPermissions;

    public Credential(  UserMuniAuthPeriod uap,
                                boolean dev,
                                boolean admin,
                                boolean cogstaff,
                                boolean ceo,
                                boolean munistaff,
                                boolean munireader){
        
        governingAuthPeriod = uap;
        
        hasDeveloperPermissions = dev;
        hasSysAdminPermissions = admin;
        hasCOGStaffPermissions = cogstaff;
        hasEnfOfficialPermissions = ceo;
        hasMuniStaffPermissions = munistaff;
        hasMuniReaderPermissions = munireader;
        
        StringBuilder sb = new StringBuilder();
        sb.append("CREDSIG:V0.9|");
        sb.append("MC:");
        sb.append(governingAuthPeriod.getMuni().muniCode);
        sb.append("|");
        sb.append("UID:");
        sb.append(governingAuthPeriod.getUserID());
        sb.append("|");
        sb.append("UMAPID:");
        sb.append(governingAuthPeriod.getUserMuniAuthPeriodID());
        sb.append("|");
        sb.append("UMAPEVALTS:");
        sb.append(governingAuthPeriod.getValidityEvaluatedTS());
        sb.append("|");
        sb.append("UMAPISVALTS:");
        sb.append(governingAuthPeriod.getValidatedTS());
        sb.append("|");
//        sb.append("UMAPLOGID:");
//        sb.append(governingAuthPeriod.getPeriodActivityLogBook().get(0));
//        sb.append("|");
        sb.append("POSTCON:");
        sb.append("|");
        signature = sb.toString();
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(signature);
        sb.append("SIGTS:");
        sb.append(generateSignatureTimeStr());
        sb.append("|");
        
        return sb.toString();
    }

    
    private String generateSignatureTimeStr(){
        return LocalDateTime.now().toString();
    }


    /**
     * @return the hasDeveloperPermissions
     */
    public boolean isHasDeveloperPermissions() {
        return hasDeveloperPermissions;
    }

    /**
     * @return the hasSysAdminPermissions
     */
    public boolean isHasSysAdminPermissions() {
        return hasSysAdminPermissions;
    }

    /**
     * @return the hasCOGStaffPermissions
     */
    public boolean isHasCOGStaffPermissions() {
        return hasCOGStaffPermissions;
    }

    /**
     * @return the hasEnfOfficialPermissions
     */
    public boolean isHasEnfOfficialPermissions() {
        return hasEnfOfficialPermissions;
    }

    /**
     * @return the hasMuniStaffPermissions
     */
    public boolean isHasMuniStaffPermissions() {
        return hasMuniStaffPermissions;
    }

    /**
     * @return the hasMuniReaderPermissions
     */
    public boolean isHasMuniReaderPermissions() {
        return hasMuniReaderPermissions;
    }

    /**
     * @return the governingAuthPeriod
     */
    public UserMuniAuthPeriod getGoverningAuthPeriod() {
        return governingAuthPeriod;
    }

    
}
