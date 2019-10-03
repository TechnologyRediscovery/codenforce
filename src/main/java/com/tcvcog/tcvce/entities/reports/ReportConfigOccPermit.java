/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class ReportConfigOccPermit 
        extends Report 
        implements Serializable{
    
    private OccPermit permit;
    private OccPeriod period;
    private PropertyUnitWithProp propUnitWithProp;
    
    private boolean includeAdditionalText;
    private boolean includeListingOfTenantPersons;
    private boolean includeListingOfOwnerPersons;

    /**
     * @return the permit
     */
    public OccPermit getPermit() {
        return permit;
    }

    /**
     * @param permit the permit to set
     */
    public void setPermit(OccPermit permit) {
        this.permit = permit;
    }

    /**
     * @return the includeAdditionalText
     */
    public boolean isIncludeAdditionalText() {
        return includeAdditionalText;
    }

    /**
     * @param includeAdditionalText the includeAdditionalText to set
     */
    public void setIncludeAdditionalText(boolean includeAdditionalText) {
        this.includeAdditionalText = includeAdditionalText;
    }

    /**
     * @return the period
     */
    public OccPeriod getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(OccPeriod period) {
        this.period = period;
    }

    /**
     * @return the propUnitWithProp
     */
    public PropertyUnitWithProp getPropUnitWithProp() {
        return propUnitWithProp;
    }

    /**
     * @param propUnitWithProp the propUnitWithProp to set
     */
    public void setPropUnitWithProp(PropertyUnitWithProp propUnitWithProp) {
        this.propUnitWithProp = propUnitWithProp;
    }

    /**
     * @return the includeListingOfTenantPersons
     */
    public boolean isIncludeListingOfTenantPersons() {
        return includeListingOfTenantPersons;
    }

    /**
     * @return the includeListingOfOwnerPersons
     */
    public boolean isIncludeListingOfOwnerPersons() {
        return includeListingOfOwnerPersons;
    }

    /**
     * @param includeListingOfTenantPersons the includeListingOfTenantPersons to set
     */
    public void setIncludeListingOfTenantPersons(boolean includeListingOfTenantPersons) {
        this.includeListingOfTenantPersons = includeListingOfTenantPersons;
    }

    /**
     * @param includeListingOfOwnerPersons the includeListingOfOwnerPersons to set
     */
    public void setIncludeListingOfOwnerPersons(boolean includeListingOfOwnerPersons) {
        this.includeListingOfOwnerPersons = includeListingOfOwnerPersons;
    }
  
   
    
}
