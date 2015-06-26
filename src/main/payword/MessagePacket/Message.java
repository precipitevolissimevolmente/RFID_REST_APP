/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.payword.MessagePacket;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Date;

/**
 * @author G
 */
public class Message implements Serializable {

    private String _message = "";
    private String cardNumber;
    private String publicKey;
    private String idBank;
    private String IPU;
    private Date expDate;
    private String encryptedData;
    private Info info = new Info();
    private PublicKey publicKeyRSA;

    public static Message messageQuit() {
        Message msgQ = new Message();
        msgQ.setMessage("QUIT");
        return msgQ;
    }

    public PublicKey getPublicKeyRSA() {
        return publicKeyRSA;
    }

    public void setPublicKeyRSA(PublicKey publicKeyRSA) {
        this.publicKeyRSA = publicKeyRSA;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public Date getExpDate() {
        return expDate;
    }

    public void setExpDate(Date expDate) {
        this.expDate = expDate;
    }

    public String getIPU() {
        return IPU;
    }

    public void setIPU(String IPU) {
        this.IPU = IPU;
    }

    public String getIdBank() {
        return idBank;
    }

    public void setIdBank(String idBank) {
        this.idBank = idBank;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String _message) {
        this._message = _message;
    }

    @Override
    public String toString() {
        return "Message{" + "B=" + idBank +
                ", U=" + cardNumber +
                ", IPU=" + IPU +
                ", K=" + publicKey +
                ", Exp=" + expDate +
                ", EncryptedData=" + encryptedData +
                ", publicKeyRSA=" + publicKeyRSA +
                ", Info=" + info.toString() + '}';
    }
}
