/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Marks an object that is given a Priority of some kind
 * 
 * @author sylvia
 */
public interface IFace_prioritized {
   
    /**
     * Extracts the priority enum. See the log for where it came from.
     * @return 
     */
    public PriorityEnum getPriority();
    
    /**
     * Setter for priority
     * @param penum 
     */
    public void setPriority(PriorityEnum penum);
    

    /**
     * Accepts incremental log strings that implementor will append to its
     * log, implemented likely with StringBuilder.append() calls and optionally
     * an HTML escape when param is true
     * 
     * @param msg
     * @param includeNewline 
     */
    public void logPriorityAssignmentMessage(String msg, boolean includeNewline);
    
    
    /**
     * Extracts the current log as a String with HTML ESACPES!!!!!
     * @return 
     */
    public String getPriorityLog();
    
    /**
     * Inject icon here
     * @param icn 
     */
    public void setPriorityIcon(Icon icn);
    
    
    /**
     * getter for the Priority's Icon
     * @return 
     */
    public Icon getPriorityIcon();
    
}
