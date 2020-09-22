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

import com.tcvcog.tcvce.domain.NavigationException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
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

    /**
     * Pushes the page the user is currently on to the stack. Use before
     * redirecting to another page to save the current page so the user can
     * return to the saved page.
     */
    public void pushCurrentPage() {

        FacesContext context = FacesContext.getCurrentInstance();

        viewIDStack.push(context.getViewRoot().getViewId());

    }

    /**
     * Returns the last page to be pushed on the stack.
     * Put this in the return line of a BB method that redirects the user.
     * @return the url or ID of the last page.
     * @throws NavigationException when stack @throws NoSuchElementException
     */
    public String popLastPage() throws NavigationException{

        try {

            return viewIDStack.pop();

        } catch (NoSuchElementException ex) {
            //We ran out of pages! This should not happen,
            //but sometimes does if you hit refresh at a bad time.
            throw new NavigationException("NavigationStack ran out of pages while popping.");
        }
    }
    
    /**
     * Pushes an arbitrary string onto stack
     * Client must test for ViewID correctness
     * @param viewID 
     */
    public void pushPage(String viewID){
        viewIDStack.push(viewID);
    }
    
   

    /**
     * Takes a look at the last page without removing it from the stack
     *
     * @return the url or ID of the last page. Returns null if there are no more
     * pages
     */
    public String peekLastPage() {

        return viewIDStack.peek();

    }

}
