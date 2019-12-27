/*
 * Copyright (C) 2019 cogconnect2
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

import java.util.List;

/**
 *
 * @author xiaohong
 */
public class NavigationItem {

    private String value;
    private String icon;
    private String searchpageurl;
    /**
     * This contains all the sub-navigation items that can be accessed from the
     * current NavigationItem
     */
    private List<NavigationSubItem> subNavitem;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<NavigationSubItem> getSubNavitem() {
        return subNavitem;
    }

    public void setSubNavitem(List<NavigationSubItem> subNavitem) {
        this.subNavitem = subNavitem;
    }

    public String getSearchpageurl() {
        return searchpageurl;
    }

    public void setSearchpageurl(String searchpageurl) {
        this.searchpageurl = searchpageurl;
    }

}
