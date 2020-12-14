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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSource;
import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class OccSpaceElement extends CodeElement {
    protected int spaceElementID;
    
    public OccSpaceElement(CodeElement ele){
        this.elementID = ele.getElementID();

        this.guideEntry = ele.getGuideEntry();
        this.guideEntryID = ele.getGuideEntryID();
        this.source = ele.getSource();

        this.ordchapterNo = ele.getOrdchapterNo();

        this.ordchapterTitle = ele.getOrdchapterTitle();
        this.ordSecNum = ele.getOrdSecNum();
        this.ordSecTitle = ele.getOrdSecTitle();

        this.ordSubSecNum = ele.getOrdSecNum();
        this.ordSubSecTitle = ele.getOrdSubSecTitle();
        this.ordTechnicalText = ele.getOrdTechnicalText();

        this.ordHumanFriendlyText = ele.getOrdHumanFriendlyText();
        this.isActive = ele.isIsActive();

        this.resourceURL = ele.getResourceURL();

        this.headerString = ele.getHeaderString();
        
    }
    
    public OccSpaceElement(CodeElement ele, int spcEleID){
        this.elementID = ele.getElementID();

        this.guideEntry = ele.getGuideEntry();
        this.guideEntryID = ele.getGuideEntryID();
        this.source = ele.getSource();

        this.ordchapterNo = ele.getOrdchapterNo();

        this.ordchapterTitle = ele.getOrdchapterTitle();
        this.ordSecNum = ele.getOrdSecNum();
        this.ordSecTitle = ele.getOrdSecTitle();

        this.ordSubSecNum = ele.getOrdSecNum();
        this.ordSubSecTitle = ele.getOrdSubSecTitle();
        this.ordTechnicalText = ele.getOrdTechnicalText();

        this.ordHumanFriendlyText = ele.getOrdHumanFriendlyText();
        this.isActive = ele.isIsActive();

        this.resourceURL = ele.getResourceURL();

        this.headerString = ele.getHeaderString();
        
        
        this.spaceElementID=spcEleID;
        
    }
    

    /**
     * @return the spaceElementID
     */
    public int getSpaceElementID() {
        return spaceElementID;
    }

    /**
     * @param spaceElementID the spaceElementID to set
     */
    public void setSpaceElementID(int spaceElementID) {
        this.spaceElementID = spaceElementID;
    }

   
}
