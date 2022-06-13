/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

/**
 * Declares the two realms in which a charge can be applied, 
 * to an OccPeriod (i.e. a Permit File) where the instance is known as a Fee
 * and to a ce case where the instance is known as a Fine
 * @author sylvia
 */
public enum ChargeOrderDomainEnum {
    FEE, 
    FINE;
}
