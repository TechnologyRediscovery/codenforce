/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import java.util.List;

/**
 *
 * @author sylvia
 * @param <E>
 */
public interface Searchable<E> {
    
    public abstract List<E> getParamsList();
    public abstract void setParamsList(List<E> l);
    
}
