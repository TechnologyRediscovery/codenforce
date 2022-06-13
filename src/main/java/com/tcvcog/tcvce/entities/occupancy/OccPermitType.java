/*
 * Copyright (C) 2018 Adam Gutonski
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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PersonType;
import java.util.List;
import java.util.Objects;


/**
 *
 * @author Adam Gutonski and Sylvia
 */
public class OccPermitType implements Cloneable {
    private int typeID;
    private Municipality muni;
    private String title;
    private String authorizeduses;
    private String description;
    private boolean userassignable;
    private List<Fee> permittedFees;
    
    
    private boolean permittable;
    private boolean inspectable;
    private boolean requireInspectionPass;
    private boolean requireManager;
    private boolean requireTenant;
    private boolean requireZeroBalance;
    
    private int baseRuleSetID;
    
    private boolean requireLeaseLink;
    private boolean active;
    private boolean allowthirdpartyinspection;
    
    private boolean commercial;
    
    private int defaultValidityPeriodDays;
    
    private String permitTitle;
    private String permitTitleSub;
    private boolean expires;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    /**
     * @return the typeid
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the authorizeduses
     */
    public String getAuthorizeduses() {
        return authorizeduses;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the userassignable
     */
    public boolean isUserassignable() {
        return userassignable;
    }

    /**
     * @return the permittable
     */
    public boolean isPermittable() {
        return permittable;
    }

    /**
     * @return the requireInspectionPass
     */
    public boolean isPassedInspectionRequired() {
        return isRequireInspectionPass();
    }

    /**
     * @return the requireLeaseLink
     */
    public boolean isRequireLeaseLink() {
        return requireLeaseLink;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the allowthirdpartyinspection
     */
    public boolean isAllowthirdpartyinspection() {
        return allowthirdpartyinspection;
    }

    /**
     * @return the commercial
     */
    public boolean isCommercial() {
        return commercial;
    }


    /**
     * @param typeid the typeid to set
     */
    public void setTypeID(int typeid) {
        this.typeID = typeid;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param authorizeduses the authorizeduses to set
     */
    public void setAuthorizeduses(String authorizeduses) {
        this.authorizeduses = authorizeduses;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param userassignable the userassignable to set
     */
    public void setUserassignable(boolean userassignable) {
        this.userassignable = userassignable;
    }

    /**
     * @param permittable the permittable to set
     */
    public void setPermittable(boolean permittable) {
        this.permittable = permittable;
    }

    /**
     * @param passedInspectionRequired the requireInspectionPass to set
     */
    public void setPassedInspectionRequired(boolean passedInspectionRequired) {
        this.setRequireInspectionPass(passedInspectionRequired);
    }

    /**
     * @param requireLeaseLink the requireLeaseLink to set
     */
    public void setRequireLeaseLink(boolean requireLeaseLink) {
        this.requireLeaseLink = requireLeaseLink;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param allowthirdpartyinspection the allowthirdpartyinspection to set
     */
    public void setAllowthirdpartyinspection(boolean allowthirdpartyinspection) {
        this.allowthirdpartyinspection = allowthirdpartyinspection;
    }

    /**
     * @param commercial the commercial to set
     */
    public void setCommercial(boolean commercial) {
        this.commercial = commercial;
    }


    /**
     * @return the defaultValidityPeriodDays
     */
    public int getDefaultValidityPeriodDays() {
        return defaultValidityPeriodDays;
    }

    /**
     * @param defaultValidityPeriodDays the defaultValidityPeriodDays to set
     */
    public void setDefaultValidityPeriodDays(int defaultValidityPeriodDays) {
        this.defaultValidityPeriodDays = defaultValidityPeriodDays;
    }

    /**
     * @return the inspectable
     */
    public boolean isInspectable() {
        return inspectable;
    }

    /**
     * @param inspectable the inspectable to set
     */
    public void setInspectable(boolean inspectable) {
        this.inspectable = inspectable;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.typeID;
        hash = 23 * hash + Objects.hashCode(this.muni);
        hash = 23 * hash + Objects.hashCode(this.title);
        hash = 23 * hash + Objects.hashCode(this.authorizeduses);
        hash = 23 * hash + Objects.hashCode(this.description);
        hash = 23 * hash + (this.userassignable ? 1 : 0);
        hash = 23 * hash + (this.permittable ? 1 : 0);
        hash = 23 * hash + (this.inspectable ? 1 : 0);
        hash = 23 * hash + (this.isRequireInspectionPass() ? 1 : 0);
        hash = 23 * hash + (this.requireLeaseLink ? 1 : 0);
        hash = 23 * hash + (this.active ? 1 : 0);
        hash = 23 * hash + (this.allowthirdpartyinspection ? 1 : 0);
        hash = 23 * hash + (this.commercial ? 1 : 0);
        hash = 23 * hash + this.defaultValidityPeriodDays;
        hash = 23 * hash + Objects.hashCode(this.permittedFees);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OccPermitType other = (OccPermitType) obj;
        return this.typeID == other.typeID;
    }

    /**
     * @return the permitTitle
     */
    public String getPermitTitle() {
        return permitTitle;
    }

    /**
     * @return the permitTitleSub
     */
    public String getPermitTitleSub() {
        return permitTitleSub;
    }

    /**
     * @param permitTitle the permitTitle to set
     */
    public void setPermitTitle(String permitTitle) {
        this.permitTitle = permitTitle;
    }

    /**
     * @param permitTitleSub the permitTitleSub to set
     */
    public void setPermitTitleSub(String permitTitleSub) {
        this.permitTitleSub = permitTitleSub;
    }

    
    public List<Fee> getPermittedFees() {
        return permittedFees;
    }

    public void setPermittedFees(List<Fee> permittedFees) {
        this.permittedFees = permittedFees;
    }

    /**
     * @return the defaultRuleSetID
     */
    public int getBaseRuleSetID() {
        return baseRuleSetID;
    }

    /**
     * @param baseRuleSetID the defaultRuleSetID to set
     */
    public void setBaseRuleSetID(int baseRuleSetID) {
        this.baseRuleSetID = baseRuleSetID;
    }

    /**
     * @return the expires
     */
    public boolean isExpires() {
        return expires;
    }

    /**
     * @param expires the expires to set
     */
    public void setExpires(boolean expires) {
        this.expires = expires;
    }

    /**
     * @return the requireManager
     */
    public boolean isRequireManager() {
        return requireManager;
    }

    /**
     * @param requireManager the requireManager to set
     */
    public void setRequireManager(boolean requireManager) {
        this.requireManager = requireManager;
    }

    /**
     * @return the requireTenant
     */
    public boolean isRequireTenant() {
        return requireTenant;
    }

    /**
     * @param requireTenant the requireTenant to set
     */
    public void setRequireTenant(boolean requireTenant) {
        this.requireTenant = requireTenant;
    }

    /**
     * @return the requireZeroBalance
     */
    public boolean isRequireZeroBalance() {
        return requireZeroBalance;
    }

    /**
     * @param requireZeroBalance the requireZeroBalance to set
     */
    public void setRequireZeroBalance(boolean requireZeroBalance) {
        this.requireZeroBalance = requireZeroBalance;
    }

    /**
     * @return the requireInspectionPass
     */
    public boolean isRequireInspectionPass() {
        return requireInspectionPass;
    }

    /**
     * @param requireInspectionPass the requireInspectionPass to set
     */
    public void setRequireInspectionPass(boolean requireInspectionPass) {
        this.requireInspectionPass = requireInspectionPass;
    }
    
}
