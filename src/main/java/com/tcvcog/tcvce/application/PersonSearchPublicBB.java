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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author Dominic Pimpinella
 */
public class PersonSearchPublicBB extends BackingBeanUtils implements Serializable {

    private SearchParamsPerson params;
    private List<Person> personSearchResults;
    private Person selectedPerson;
    
    @PostConstruct
    public void initBean(){
        PersonCoordinator pc = getPersonCoordinator();
//        setParams(pc.);
    }
    
    public void queryPersons(SearchParamsPerson params) throws IntegrationException{
       PersonCoordinator pc = getPersonCoordinator();
       boolean anonymizeResults = true;
       
       // Finish me!
//       setPersonSearchResults(pc.queryPersons(params, anonymizeResults));
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
     * @return the personSearchResults
     */
    public List<Person> getPersonSearchResults() {
        return personSearchResults;
    }

    /**
     * @param personSearchResults the personSearchResults to set
     */
    public void setPersonSearchResults(List<Person> personSearchResults) {
        this.personSearchResults = personSearchResults;
    }

    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }
}
