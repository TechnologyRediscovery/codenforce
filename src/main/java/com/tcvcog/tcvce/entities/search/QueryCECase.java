/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOB;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class QueryCECase 
        extends Query{
    
    private List<SearchParamsCECases> searchParams; 
    private List<CECase> caseList;
    
    public QueryCECase(String queryTitle, Municipality muni) {
        super(queryTitle, muni);
    }

    /**
     * @return the searchParams
     */
    public List<SearchParamsCECases> getSearchParams() {
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(List<SearchParamsCECases> searchParams) {
        this.searchParams = searchParams;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.searchParams);
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
        final QueryCECase other = (QueryCECase) obj;
        if (!Objects.equals(this.searchParams, other.searchParams)) {
            return false;
        }
        return true;
    }

   
    

    /**
     *
     * @return
     */
    @Override
    public List<SearchParamsCECases> getParamsList() {
        return searchParams;
    }

    /**
     * @return the caseList
     */
    public List<CECase> getCaseList() {
        return caseList;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECase> caseList) {
        this.caseList = caseList;
    }

    @Override
    public void setParamsList(List l) {
        searchParams = l;
    }
    
    
    
}
