/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.util.Iterator;
import java.util.List;

/**
 * Superlcass of entity objects: a Hodgepodge set of methods for use by 
 * entities, such as returning a nicely
 * formatted String version of any given LocalDateTime 
 * @author sylvia Baskem
 */
public class EntityUtils {
    
    /**
     * Pretty prints a List of Integers
     * Used by CodeViolations to list their citations and notices
     * @param intList
     * @return 
     */
    public static String fomatIDListAsString(List<Integer> intList){
        
        String listString;
        StringBuilder sb = new StringBuilder();
        Iterator<Integer> it;
        
        if(!intList.isEmpty()){
            sb.append("ID #s: ");
            it = intList.iterator();
            while(it.hasNext()){
                Integer i = it.next();
                sb.append(String.valueOf(i));
                if(it.hasNext()){
                    sb.append(", ");
                }
            }
             listString = sb.toString();
        } else listString = "";
        
        return listString;
    }
}
