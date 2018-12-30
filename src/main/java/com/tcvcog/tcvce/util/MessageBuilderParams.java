/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.util;

import com.tcvcog.tcvce.entities.User;

/**
 * Convenience class to avoid a 5-input-parameter method for formatting
 * messages
 * @author ellen baskum
 */
public class MessageBuilderParams {
    public String existingContent;
    public String header;
    public String explanation;
    public String newMessageContent;
    public User user;
    
}
