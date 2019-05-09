/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class CECaseQuery extends BOBQuery{
    
    private List<SearchParamsCECases> caseSearchParamsList; 
    
    public CECaseQuery(String queryTitle, Municipality muni, User u) {
        super(queryTitle, muni, u);
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
    
    
    
}
