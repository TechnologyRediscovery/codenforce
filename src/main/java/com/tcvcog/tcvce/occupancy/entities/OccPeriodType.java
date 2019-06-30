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
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PersonType;
import java.util.List;


/**
 *
 * @author Adam Gutonski and Sylvia
 */
public class OccPeriodType {
    private int typeid;
    private Municipality muni;
    private String title;
    private String authorizeduses;
    private String description;
    private boolean userassignable;
    private boolean permittable;
    private boolean startdaterequired;
    private boolean enddaterequired;
    private boolean completedinspectionrequired;
    private boolean rentalcompatible;
    private boolean active;
    private boolean allowthirdpartyinspection;
    private List<PersonType> optionalpersontypeList;
    private List<PersonType> requiredPersontypeList;
    private boolean commercial;
    private OccInspecFee fee;
    private boolean requirepersontypeentrycheck;

    /**
     * @return the typeid
     */
    public int getTypeid() {
        return typeid;
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
     * @return the startdaterequired
     */
    public boolean isStartdaterequired() {
        return startdaterequired;
    }

    /**
     * @return the enddaterequired
     */
    public boolean isEnddaterequired() {
        return enddaterequired;
    }

    /**
     * @return the completedinspectionrequired
     */
    public boolean isCompletedinspectionrequired() {
        return completedinspectionrequired;
    }

    /**
     * @return the rentalcompatible
     */
    public boolean isRentalcompatible() {
        return rentalcompatible;
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
     * @return the optionalpersontypeList
     */
    public List<PersonType> getOptionalpersontypeList() {
        return optionalpersontypeList;
    }

    /**
     * @return the requiredPersontypeList
     */
    public List<PersonType> getRequiredPersontypeList() {
        return requiredPersontypeList;
    }

    /**
     * @return the commercial
     */
    public boolean isCommercial() {
        return commercial;
    }

    /**
     * @return the fee
     */
    public OccInspecFee getFee() {
        return fee;
    }

    /**
     * @return the requirepersontypeentrycheck
     */
    public boolean isRequirepersontypeentrycheck() {
        return requirepersontypeentrycheck;
    }

    /**
     * @param typeid the typeid to set
     */
    public void setTypeid(int typeid) {
        this.typeid = typeid;
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
     * @param startdaterequired the startdaterequired to set
     */
    public void setStartdaterequired(boolean startdaterequired) {
        this.startdaterequired = startdaterequired;
    }

    /**
     * @param enddaterequired the enddaterequired to set
     */
    public void setEnddaterequired(boolean enddaterequired) {
        this.enddaterequired = enddaterequired;
    }

    /**
     * @param completedinspectionrequired the completedinspectionrequired to set
     */
    public void setCompletedinspectionrequired(boolean completedinspectionrequired) {
        this.completedinspectionrequired = completedinspectionrequired;
    }

    /**
     * @param rentalcompatible the rentalcompatible to set
     */
    public void setRentalcompatible(boolean rentalcompatible) {
        this.rentalcompatible = rentalcompatible;
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
     * @param optionalpersontypeList the optionalpersontypeList to set
     */
    public void setOptionalpersontypeList(List<PersonType> optionalpersontypeList) {
        this.optionalpersontypeList = optionalpersontypeList;
    }

    /**
     * @param requiredPersontypeList the requiredPersontypeList to set
     */
    public void setRequiredPersontypeList(List<PersonType> requiredPersontypeList) {
        this.requiredPersontypeList = requiredPersontypeList;
    }

    /**
     * @param commercial the commercial to set
     */
    public void setCommercial(boolean commercial) {
        this.commercial = commercial;
    }

    /**
     * @param fee the fee to set
     */
    public void setFee(OccInspecFee fee) {
        this.fee = fee;
    }

    /**
     * @param requirepersontypeentrycheck the requirepersontypeentrycheck to set
     */
    public void setRequirepersontypeentrycheck(boolean requirepersontypeentrycheck) {
        this.requirepersontypeentrycheck = requirepersontypeentrycheck;
    }
    
}
