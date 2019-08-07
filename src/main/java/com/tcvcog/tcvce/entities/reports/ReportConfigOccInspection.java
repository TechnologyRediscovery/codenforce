/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class ReportConfigOccInspection 
        extends Report 
        implements Serializable{
    
    private OccPeriod reportPeriod;
    
    private boolean includeOccPeriodInfoHeader;
    
    private boolean includeElements_notInspected;
    private boolean includeElements_pass;
    private boolean includeElements_fail;
    
    private boolean includePhotos_pass;
    private boolean includePhotos_fail;
    
    private boolean separateElementsBySpace;
    private boolean includeFullOrdText;
    private boolean includeElementNotes;
    
    private boolean includeElementLastInspectedInfo;
    private boolean includeElementComplianceInfo;
   
    private boolean includeNextStepText;
    private boolean includeSignature;
}
