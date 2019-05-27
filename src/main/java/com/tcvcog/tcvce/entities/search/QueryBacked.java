/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

/**
 *
 * @author sylvia
 */
public interface QueryBacked {
    
    public abstract Query getBOBQuery();
    public abstract void setBOBQuery(Query q);

    
}
