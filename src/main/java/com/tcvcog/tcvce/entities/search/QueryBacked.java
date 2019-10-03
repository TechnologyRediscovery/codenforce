/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

/**
 * Specifies a getter/setter pair for a BOBQuery subclass which
 * stores all manner of information including the actual List of
 * Business Objects to be displayed.
 * 
 * Implementing classes are Objects that represent a set of business objects,
 * such as ReportCEARList
 * 
 * 
 * @author sylvia
 */
public interface QueryBacked {
    
    public abstract Query getBOBQuery();
    public abstract void setBOBQuery(Query q);

    
}
