/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.util.List;

/**
 * Marks a class that stores lists of MADLinks, PHones, and emails
 * @author sylvia
 */
public interface IFace_contactable extends IFace_addressListHolder{
    public List<ContactEmail> getEmailList();
    public void setEmailList(List<ContactEmail> eml);
    
    public List<ContactPhone> getPhoneList();
    public void setPhoneList(List<ContactPhone> phl);
    
    public String getMailingAddressListPretty();
    public void setMailingAddressListPretty(String madlp);
    
    public String getPhoneListPretty();
    public void setPhoneListPretty(String madlp);
    
    public String getEmailListPretty();
    public void setEmailListPretty(String madlp);
    
    public int getHumanID();
    
    
}
