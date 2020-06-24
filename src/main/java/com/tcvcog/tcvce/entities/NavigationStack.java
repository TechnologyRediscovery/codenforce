/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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
package com.tcvcog.tcvce.entities;

import java.util.LinkedList;
import javax.faces.context.FacesContext;

/**
 *
 * @author Nathan Dietz
 */
public class NavigationStack {

    private LinkedList<String> viewIDStack;
    
    public NavigationStack() {
        
        viewIDStack = new LinkedList<>();

    }
    
    public void pushCurrentPage(){
        
        FacesContext context = FacesContext.getCurrentInstance();
        
        viewIDStack.push(context.getViewRoot().getViewId());
        
    }
    
    public String popLastPage(){
        
        return viewIDStack.pop();
        
    }
    
    public String peekLastPage(){
        
        return viewIDStack.peek();
        
    }
    
}