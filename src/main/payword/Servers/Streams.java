/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.payword.Servers;

import java.io.Serializable;

/**
 *
 * @author G
 */
public class Streams implements Serializable{
    private String name;
    private int price;

    public Streams(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    
    
}
