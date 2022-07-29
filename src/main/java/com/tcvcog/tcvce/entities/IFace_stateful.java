/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Delimits objects who can return a state
 * @author sylvia
 */
public interface IFace_stateful extends IFace_keyIdentified{
    /**
     * Extracts the implementor's state
     * @return 
     */
    public StateEnum getState();
    
    
}
