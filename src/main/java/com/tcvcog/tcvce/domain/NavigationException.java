/*
 * Copyright (C) 2020 Turtle Creek Valley
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
package com.tcvcog.tcvce.domain;

/**
 * An exception used when procedural or dynamic navigation goes wrong
 * @author Nathan Dietz
 */
public class NavigationException extends BaseException {

    /**
     * Creates a new instance of <code>NavigationException</code> without detail
     * message.
     */
    public NavigationException() {
        super();
    }

    /**
     * Constructs an instance of <code>NavigationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NavigationException(String msg) {
        super(msg);
    }
    
    public NavigationException(Exception e){
        super(e);
    }
    
    public NavigationException(String message, Exception e){
        super(message, e);
    }
}
