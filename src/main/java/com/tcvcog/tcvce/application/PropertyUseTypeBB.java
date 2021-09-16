/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;

/**
* backing bean for property use type page
* @author celia
*/
public class PropertyUseTypeBB extends BackingBeanUtils implements Serializable {
	
	private List<PropertyUseType> putList;
	private PropertyUseType putSkeleton;
	
	/**
     * Creates a new 
     * instance of PropertyUseTypeBB	
     */
    public PropertyUseTypeBB() {
        
        
    }
    
    //calls initPutList
    @PostConstruct
    public void initBean() {
    	
    	initPutList();   
    
    }
    
    //initializes putList. grabs PropertyIntegrator and PropertyIntegrator grabs all puts from the database
    public void initPutList() {
    	PropertyIntegrator pi = getPropertyIntegrator();
    	
    	try {
    		setPutList(pi.getPropertyUseTypeList());
    		
    	} catch (IntegrationException ex) {
    		System.out.println(ex);
    	}
    	
    }
    
    public void insertPut() {
    	PropertyIntegrator pi = getPropertyIntegrator();
    	try {
			pi.insertPropertyUseType(putSkeleton);
		} catch (IntegrationException ex) {
			System.out.println(ex);
		}
    }
    
    
    //initializes put skeleton
    public void initPutSkeleton() {
    	setPutSkeleton(new PropertyUseType());
    }

	public List<PropertyUseType> getPutList() {
		return putList;
	}

	public void setPutList(List<PropertyUseType> putList) {
		this.putList = putList;
	}

	public PropertyUseType getPutSkeleton() {
		return putSkeleton;
	}

	public void setPutSkeleton(PropertyUseType putSkeleton) {
		this.putSkeleton = putSkeleton;
	}
    
    
    
    
	
}