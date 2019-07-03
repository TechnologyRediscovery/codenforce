/*
 * Copyright (C) 2018 Technology Rediscovery, LLC.
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
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class OccPeriodType {

    
    private int typeID;
    private int muniCode;
    private String title;
    private String authorizedUses;
    private String description;
    private boolean userAssignable;
    private boolean permittable;
    private boolean startDateRequired;
    private boolean endDateRequired;
    private boolean completedInspectionRequired;
    private boolean rentalCompatible;
    private boolean commercial;
    private boolean active;
    private boolean allowThirdPartyInspection;
    private List<PersonType> requiredPersonTypes;
    private List<PersonType> optionalPersonTypes;
    private int feeID;
    private boolean requirePersonTypeEntryCheck;
    
    public OccPeriodType() {
    
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getMuniCode() {
        return muniCode;
    }

    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorizedUses() {
        return authorizedUses;
    }

    public void setAuthorizedUses(String authorizedUses) {
        this.authorizedUses = authorizedUses;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUserAssignable() {
        return userAssignable;
    }

    public void setUserAssignable(boolean userAssignable) {
        this.userAssignable = userAssignable;
    }

    public boolean isPermittable() {
        return permittable;
    }

    public void setPermittable(boolean permittable) {
        this.permittable = permittable;
    }

    public boolean isStartDateRequired() {
        return startDateRequired;
    }

    public void setStartDateRequired(boolean startDateRequired) {
        this.startDateRequired = startDateRequired;
    }

    public boolean isEndDateRequired() {
        return endDateRequired;
    }

    public void setEndDateRequired(boolean endDateRequired) {
        this.endDateRequired = endDateRequired;
    }

    public boolean isCompletedInspectionRequired() {
        return completedInspectionRequired;
    }

    public void setCompletedInspectionRequired(boolean completedInspectionRequired) {
        this.completedInspectionRequired = completedInspectionRequired;
    }

    public boolean isRentalCompatible() {
        return rentalCompatible;
    }

    public void setRentalCompatible(boolean rentalCompatible) {
        this.rentalCompatible = rentalCompatible;
    }

    public boolean isCommercial() {
        return commercial;
    }

    public void setCommercial(boolean commercial) {
        this.commercial = commercial;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAllowThirdPartyInspection() {
        return allowThirdPartyInspection;
    }

    public void setAllowThirdPartyInspection(boolean allowThirdPartyInspection) {
        this.allowThirdPartyInspection = allowThirdPartyInspection;
    }

    public List<PersonType> getRequiredPersonTypes() {
        return requiredPersonTypes;
    }

    public void setRequiredPersonTypes(List<PersonType> requiredPersonTypes) {
        this.requiredPersonTypes = requiredPersonTypes;
    }

    public List<PersonType> getOptionalPersonTypes() {
        return optionalPersonTypes;
    }

    public void setOptionalPersonTypes(List<PersonType> optionalPersonTypes) {
        this.optionalPersonTypes = optionalPersonTypes;
    }

    public int getFeeID() {
        return feeID;
    }

    public void setFeeID(int feeID) {
        this.feeID = feeID;
    }

    public boolean isRequirePersonTypeEntryCheck() {
        return requirePersonTypeEntryCheck;
    }

    public void setRequirePersonTypeEntryCheck(boolean requirePersonTypeEntryCheck) {
        this.requirePersonTypeEntryCheck = requirePersonTypeEntryCheck;
    }
   
    
    
}
