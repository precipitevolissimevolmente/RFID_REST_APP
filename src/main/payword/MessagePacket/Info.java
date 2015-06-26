/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.payword.MessagePacket;

import java.io.Serializable;

/**
 *
 * @author G
 */
public class Info implements Serializable{
    private int sum;
    private int currentAmount;
    private String address;

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }

    @Override
    public String toString() {
        return "sum=" + sum + ", address=" + address;
    }

}
