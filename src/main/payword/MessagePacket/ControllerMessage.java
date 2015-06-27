/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.payword.MessagePacket;

import java.io.Serializable;

/**
 * @author G
 */
public class ControllerMessage implements Serializable {
    private String _message = "";
    private String certificate;
    private String cardNumber;
    private boolean valid;

    public static ControllerMessage messageQuit() {
        return new ControllerMessage("QUIT");
    }

    public ControllerMessage(String _message) {
        this._message = _message;
    }

    public String getMessage() {
        return _message;
    }

    public ControllerMessage setMessage(String _message) {
        this._message = _message;
        return this;
    }

    public String getCertificate() {
        return certificate;
    }

    public ControllerMessage setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public ControllerMessage setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
