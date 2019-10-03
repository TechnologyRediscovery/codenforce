/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.application.interfaces;

import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.util.List;

/**
 * Delimits a way to query, insert, extract, and manipulate Proposal objects
 * by specifically--at the time of this class creation--CECase and Occperiod objects.
 * 
 * @author Ellen Baskem
 */
public interface IFace_ProposalDriven {
    
    /**
     *
     * @param propList
     */
    public void setProposalList(List<Proposal> propList);
    
    public List<Proposal> assembleProposalList(ViewOptionsProposalsEnum vope); 
    
}
