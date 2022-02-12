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
package testing;

import com.tcvcog.tcvce.entities.Property;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sylvia
 */
public class AddressParse {
    
    public static void main(String[] args) {
//        Property prop = new Property();
//        prop.setAddress("1237 MORRELLE AVE");
//        parseAddress(prop);
//        prop.setAddress("889A 4th st BEACHwood.");
//        parseAddress(prop);
        
    }
    
    private static Property parseAddress(Property prop){
        Pattern pat = Pattern.compile("(?<num>\\d+[a-zA-Z]*)\\W+(?<street>\\w.*)");
        Pattern patStreet = Pattern.compile("\\s([a-zA-Z0-9][a-zA-Z_\\s.]*)");
        Matcher matPat = pat.matcher(prop.getAddressString());
        Matcher matStreet = patStreet.matcher(prop.getAddressString());
        
        while (matPat.find()){
            System.out.println(matPat.group());
            System.out.println("NUM:" + matPat.group("num"));
            System.out.println("STREET:" + matPat.group("street"));
        }
        
        while (matStreet.find()){
            System.out.println(matStreet.group());
            System.out.println(matStreet.groupCount());
            System.out.println(matStreet.group(1));
        }
        
        
        
        return prop;
    }
    
    
}
