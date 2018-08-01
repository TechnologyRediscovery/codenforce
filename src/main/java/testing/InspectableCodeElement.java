/*
 * Copyright (C) 2018 Adam Gutonski
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
package testing;

//--> not needed yet for codeElement import com.tcvcog.tcvce.entities.CodeElement;
import java.time.LocalDateTime;

/**
 * Created before the inspectability components got integrated into the code guide
 * 
 * @deprecated 
 * @author Adam Gutonski
 */
public class InspectableCodeElement {
    //this will need to be of type CodeElement, but I need to understand how to retrievt
    //data from DB and display it on the screen, can't be worried yet about aligning the
    //foreign keys between codeElement table and inspectableCodeElement table
    private int codeElementID;
    
    //private String inspectionGuidelines;
    //private String noncomplianceNotes;
    //private LocalDateTime iceDate;
    private String inspectionTips;
    private boolean inspectionPriority;
    
    private int inspectableCodeElementId;

    /**
     * @return the codeElement
     */
    public int getCodeElementID() {
        return codeElementID;
    }

    /**
     * @param codeElement the codeElement to set
     */
    public void setCodeElementID(int codeElement) {
        this.codeElementID = codeElement;
    }

    /**
     * @return the inspectionGuidelines
     */
    public String getInspectionTips() {
        return inspectionTips;
    }

    /**
     * @param inspectionTips the inspectionGuidelines to set
     */
    public void setInspectionTips(String inspectionTips) {
        this.inspectionTips = inspectionTips;
    }

    /**
     * These non-comp. notes are from first iteration of ICE entity
     
    public String getNoncomplianceNotes() {
        return noncomplianceNotes;
    }

    /**
     * @param noncomplianceNotes the noncomplianceNotes to set
     
    public void setNoncomplianceNotes(String noncomplianceNotes) {
        this.noncomplianceNotes = noncomplianceNotes;
    }
    * */

    /**
     * @return the highImportance
     */
    public boolean getInspectionPriority() {
        return inspectionPriority;
    }

    /**
     * @param inspectionPriority the highImportance to set
     */
    public void setInspectionPriority(boolean inspectionPriority) {
        this.inspectionPriority = inspectionPriority;
    }

    /**
     * @return the inspectableCodeElement_id
     */
    public int getInspectableCodeElementId() {
        return inspectableCodeElementId;
    }

    /**
     * @param inspectableCodeElement_id the inspectableCodeElement_id to set
     */
    public void setInspectableCodeElementId(int inspectableCodeElement_id) {
        this.inspectableCodeElementId = inspectableCodeElement_id;
    }

    /**
     * These date methods are from first iteration of ICE entity
     
    public LocalDateTime getIceDate() {
        return iceDate;
    }

    /**
     * @param iceDate the iceDate to set
     
    public void setIceDate(LocalDateTime iceDate) {
        this.iceDate = iceDate;
    }
    */
}
