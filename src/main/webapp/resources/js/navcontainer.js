/* 
 * Copyright (C) 2021 Technology Rediscovery LLC
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

function onClickNavbarTogglerButton() {
var x = document.getElementById("navbar_id");
        if (x.className === "navbar") {
x.className += " responsive";
} else {
x.className = "navbar";
}
}
var dropdown = document.getElementsByClassName("sidebar_firstmenu_item");
        for (var i = 0; i < dropdown.length; i++) {
var n = dropdown[i];
        n.addEventListener("click", function() {
        this.classList.toggle("sidebar_firstmenu_font_active");
                var dropdownContent = this.nextElementSibling;
                if (dropdownContent.style.display === "block") {
        dropdownContent.style.display = "none";
        } else {
        dropdownContent.style.display = "block";
                openSideBar();
        }
        });
}

function openSideBar() {
document.getElementById("toolSidebar").style.width = "250px";
        document.getElementById("maincontent").style.marginLeft = "255px";
        document.getElementById("toolSidebarCollapseId").style.display = "none";
        document.getElementById("toolSidebarExpansionId").style.display = "flex";
        var fm = document.getElementsByClassName("sidebar_firstmenu_font");
        for (var i = 0; i < fm.length; i++) {
fm[i].style.display = "inline";
}
var sm = document.getElementsByClassName("sidebar_secondmenu_font");
        for (var i = 0; i < sm.length; i++) {
sm[i].style.display = "inline";
}
}

function closeSideBar() {
document.getElementById("toolSidebar").style.width = "35px";
        document.getElementById("maincontent").style.marginLeft = "40px";
        document.getElementById("toolSidebarCollapseId").style.display = "flex";
        document.getElementById("toolSidebarExpansionId").style.display = "none";
        var fm = document.getElementsByClassName("sidebar_firstmenu_font");
        for (var i = 0; i < fm.length; i++) {
fm[i].style.display = "none";
}
var sm = document.getElementsByClassName("sidebar_secondmenu_font");
        for (var i = 0; i < sm.length; i++) {
sm[i].style.display = "none";
}
}

function initSideBar(){
var smi = document.getElementsByClassName("sidebar_secondmenu");
        for (var i = 0; i < smi.length; i++) {
smi[i].style.display = "none";
}
}

document.getElementById("toolSidebarExpansionButtonIcon").addEventListener("click", openSideBar);
        document.getElementById("toolSidebarCollapseButtonIcon").addEventListener("click", closeSideBar);
        document.getElementById("toolSidebarExpansionButtonIcon").addEventListener("click", initSideBar);
        document.getElementById("toolSidebarCollapseButtonIcon").addEventListener("click", initSideBar);
