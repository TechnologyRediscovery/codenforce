/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class PrintStyle  implements Serializable{
    
    private int styleID;
    private String description;
    private int header_height;
    private int header_img_id;
    
    private int nov_page_margin_top;
    private int nov_addressee_margin_left;
    private int nov_addressee_margin_top;
    private int nov_text_margin_top;

    /**
     * @return the nov_addressee_margin_top
     */
    public int getNov_addressee_margin_top() {
        return nov_addressee_margin_top;
    }

    /**
     * @param nov_addressee_margin_top the nov_addressee_margin_top to set
     */
    public void setNov_addressee_margin_top(int nov_addressee_margin_top) {
        this.nov_addressee_margin_top = nov_addressee_margin_top;
    }

    /**
     * @return the styleID
     */
    public int getStyleID() {
        return styleID;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the header_height
     */
    public int getHeader_height() {
        return header_height;
    }

    /**
     * @return the header_img_id
     */
    public int getHeader_img_id() {
        return header_img_id;
    }

    /**
     * @return the nov_page_margin_top
     */
    public int getNov_page_margin_top() {
        return nov_page_margin_top;
    }

    /**
     * @return the nov_addressee_margin_left
     */
    public int getNov_addressee_margin_left() {
        return nov_addressee_margin_left;
    }

    /**
     * @param styleID the styleID to set
     */
    public void setStyleID(int styleID) {
        this.styleID = styleID;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param header_height the header_height to set
     */
    public void setHeader_height(int header_height) {
        this.header_height = header_height;
    }

    /**
     * @param header_img_id the header_img_id to set
     */
    public void setHeader_img_id(int header_img_id) {
        this.header_img_id = header_img_id;
    }

    /**
     * @param nov_page_margin_top the nov_page_margin_top to set
     */
    public void setNov_page_margin_top(int nov_page_margin_top) {
        this.nov_page_margin_top = nov_page_margin_top;
    }

    /**
     * @param nov_addressee_margin_left the nov_addressee_margin_left to set
     */
    public void setNov_addressee_margin_left(int nov_addressee_margin_left) {
        this.nov_addressee_margin_left = nov_addressee_margin_left;
    }

    /**
     * @return the nov_text_margin_top
     */
    public int getNov_text_margin_top() {
        return nov_text_margin_top;
    }

    /**
     * @param nov_text_margin_top the nov_text_margin_top to set
     */
    public void setNov_text_margin_top(int nov_text_margin_top) {
        this.nov_text_margin_top = nov_text_margin_top;
    }
    
    
}
