/*
 * Copyright (C) 2020 marosco
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
package com.tcvcog.tcvce.application.validators;

import java.time.LocalDateTime;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 *
 * @author marosco
 */
@FacesValidator("dateValidation")
public class DateValidator implements Validator{

    @Override
    public void validate(FacesContext context,
            UIComponent component,
            Object value) throws ValidatorException {
        LocalDateTime startDate = (LocalDateTime) value;
        UIInput uiInputEndDate = (UIInput) component.getAttributes().get("endDate");
        LocalDateTime endDate = (LocalDateTime) uiInputEndDate.getSubmittedValue();
        
        if (!startDate.isAfter(endDate) && !startDate.equals(endDate)){
            return;
        }
        else{
            uiInputEndDate.setValid(false);
            throw new ValidatorException(new FacesMessage("End date must be later than start date."));
        }
    }
    
}
