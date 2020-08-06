/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PublicInfoCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PublicInfoBundlePerson;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Dominic Pimpinella
 */
public class PersonSearchPublicBB extends BackingBeanUtils implements Serializable {

    private SearchParamsPerson params;
    private List<Person> personSearchResults; //deprecated
    private Person selectedPerson; //deprecated

    private List<PublicInfoBundlePerson> bundledPersonSearchResults; //rename to "personSearchResults" when update is complete
    private PublicInfoBundlePerson bundledSelectedPerson; //rename to "selectedPerson" when update is complete

    @PostConstruct
    public void initBean() {
        PersonCoordinator pc = getPersonCoordinator();
//        setParams(pc.);
    }

    /**
     * @param params
     * @throws IntegrationException
     */
    public void queryPublicInfoBundlePersons(SearchParamsPerson params) throws IntegrationException {
        UserCoordinator uc = getUserCoordinator();
        SearchCoordinator sc = getSearchCoordinator();

        QueryPerson qp = null;

        try {

            qp = sc.initQuery(QueryPersonEnum.PERSON_NAME, uc.auth_getPublicUserAuthorized().getMyCredential());

            if (qp != null && !qp.getParamsList().isEmpty()) {
                qp.addParams(params);

                sc.runQuery(qp);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Your search completed with " + bundledPersonSearchResults.size() + " results", ""));
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Something when wrong with the person search! Sorry!", ""));
            }

        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong with the person search! Sorry!", ""));
        }

        if (qp != null && !qp.getBOBResultList().isEmpty()) {

            PublicInfoCoordinator pic = getPublicInfoCoordinator();
            List<Person> skeletonHorde = qp.getBOBResultList();
            bundledPersonSearchResults = new ArrayList<>();
            
            for(Person skeleton : skeletonHorde){
                bundledPersonSearchResults.add(pic.extractPublicInfo(skeleton));
            }
            
        }

    }

    /**
     * @return the params
     */
    public SearchParamsPerson getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(SearchParamsPerson params) {
        this.params = params;
    }

    /**
     * @deprecated @return the personSearchResults
     */
    public List<Person> getPersonSearchResults() {
        return personSearchResults;
    }

    /**
     * @deprecated @param personSearchResults the personSearchResults to set
     */
    public void setPersonSearchResults(List<Person> personSearchResults) {
        this.personSearchResults = personSearchResults;
    }

    /**
     * @deprecated @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @deprecated @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    public List<PublicInfoBundlePerson> getBundledPersonSearchResults() {
        return bundledPersonSearchResults;
    }

    public void setBundledPersonSearchResults(List<PublicInfoBundlePerson> bundledPersonSearchResults) {
        this.bundledPersonSearchResults = bundledPersonSearchResults;
    }

    public PublicInfoBundlePerson getBundledSelectedPerson() {
        return bundledSelectedPerson;
    }

    public void setBundledSelectedPerson(PublicInfoBundlePerson bundledSelectedPerson) {
        this.bundledSelectedPerson = bundledSelectedPerson;
    }
    
}
