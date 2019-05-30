/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class CECaseQuery extends BOBQuery{
    
    private List<SearchParamsCECases> caseSearchParamsList; 
    
    public CECaseQuery(String queryTitle, Municipality muni) {
        super(queryTitle, muni);
    }

    /**
     * @return the caseSearchParamsList
     */
    public List<SearchParamsCECases> getCaseSearchParamsList() {
        return caseSearchParamsList;
    }

    /**
     * @param caseSearchParamsList the caseSearchParamsList to set
     */
    public void setCaseSearchParamsList(List<SearchParamsCECases> caseSearchParamsList) {
        this.caseSearchParamsList = caseSearchParamsList;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.caseSearchParamsList);
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
        final CECaseQuery other = (CECaseQuery) obj;
        if (!Objects.equals(this.caseSearchParamsList, other.caseSearchParamsList)) {
            return false;
        }
        return true;
    }
    
    
    
}
