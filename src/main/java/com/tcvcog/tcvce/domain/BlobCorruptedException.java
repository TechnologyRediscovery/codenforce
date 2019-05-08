/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.domain;

/**
 *
 * @author noah
 */
public class BlobCorruptedException extends BlobException{
    
    public BlobCorruptedException(){
        super();
        
    }
    
    public BlobCorruptedException(String message){
        super(message);
    }
    
    public BlobCorruptedException(Exception e){
        super(e);
    }
    
    public BlobCorruptedException(String message, Exception e){
        super(message, e);
        
    }
}
