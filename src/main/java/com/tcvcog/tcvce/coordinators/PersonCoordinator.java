/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SearchCoordinator;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import java.io.Serializable;

/**
 *
 * @author Eric C. Darsow
 */
public class PersonCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of PersonCoordinator
     */
    public PersonCoordinator() {
    }

    /**
     * Hard-coded default values for person searches
     * @return 
     */
    public SearchParamsPersons getDefaultSearchParamsPersons() {
        SearchParamsPersons spp = new SearchParamsPersons();
        // on the parent class SearchParams
        spp.setMuni(getSessionBean().getActiveMuni());
        spp.setFilterByStartEndDate(false);
        spp.setLimitResultCountTo100(true);
        
        // on the subclass SearchParamsPersons
        spp.setFilterByFirstName(false);
        spp.setFilterByLastName(true);
        spp.setOnlySearchCompositeLastNames(false);
        
        spp.setFilterByPersonTypes(false);
        spp.setFilterByEmail(false);
        spp.setFilterByAddressStreet(false);
        
        spp.setFilterByActiveSwitch(false);
        spp.setFilterByVerifiedSwitch(false);
        spp.setFilterByPropertySwitch(false);
        
        return spp;
    }
    
}
