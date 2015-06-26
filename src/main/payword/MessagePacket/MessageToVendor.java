/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.payword.MessagePacket;

import main.payword.Servers.Streams;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author G
 */
public class MessageToVendor implements Serializable {

    private String V;
    private Message CU;
    private String c01;
    private int nc01;

    public int getNc01() {
        return nc01;
    }

    public void setNc01(int nc01) {
        this.nc01 = nc01;
    }

    private Date D;
    private String info;
    private String _message;
    private Vector<Streams> products;

    public Vector<Streams> getProducts() {
        return products;
    }

    public void setProducts(Vector<Streams> products) {
        this.products = products;
    }

    
    public String getMessage() {
        return _message;
    }

    public void setMessage(String _message) {
        this._message = _message;
    }
    private String Sig;

    public String getV() {
        return V;
    }

    public void setV(String V) {
        this.V = V;
    }

    public Message getCU() {
        return CU;
    }

    public void setCU(Message CU) {
        this.CU = CU;
    }

    public Date getD() {
        return D;
    }

    public String getC01() {
        return c01;
    }

    public void setC01(String c01) {
        this.c01 = c01;
    }

    public void setD(Date D) {
        this.D = D;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSig() {
        return Sig;
    }

    public void setSig(String Sig) {
        this.Sig = Sig;
    }

    @Override
    public String toString() {
        return "MessageToVendor{" + "V=" + V + ", CU=" + CU + ", c01=" + c01 + ", D=" + D + ", info=" + info + '}';
    }
}
