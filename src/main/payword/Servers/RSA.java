/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.payword.Servers;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G
 */
public class RSA implements Serializable {

    private PublicKey publicKeyRSA;
    private Key privateKey;
    private Cipher cipher;
    private byte[] cipherData = null;
    private byte[] decryptedData;

    public PublicKey getPublicKeyRSA() {
        return publicKeyRSA;
    }

    public void setPublicKeyRSA(PublicKey publicKeyRSA) {
        this.publicKeyRSA = publicKeyRSA;
    }

    public RSA() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512);
            KeyPair kp = kpg.genKeyPair();
            Key publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
            KeyFactory fact = KeyFactory.getInstance("RSA");

            RSAPublicKeySpec pub = (RSAPublicKeySpec) fact.getKeySpec(publicKey,
                    RSAPublicKeySpec.class);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(pub.getModulus(), pub
                    .getPublicExponent());
            KeyFactory factory = KeyFactory.getInstance("RSA");

            publicKeyRSA = factory.generatePublic(spec);
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchPaddingException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
            Logger.getLogger(RSA.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String encriptData(String data) throws Exception {
        byte[] dataToEncrypt = data.getBytes("UTF-16LE");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKeyRSA);
            cipherData = cipher.doFinal(dataToEncrypt);
        } catch (Exception e1) {
            System.out.println(e1);
            System.out.println("Encrypt error--");
        }

        String s = Base64.encode(cipherData);
        return s;
    }

    public String decriptData(String data) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        try {
            decryptedData = cipher.doFinal(Base64.decode(data));
        } catch (Exception e1) {
            System.out.println(e1);
            System.out.println("Decrypt error");
        }

        return new String(decryptedData, "UTF-16LE");
    }

    public String encriptLongData(String dataToSign) {
        String encriptedData = "";

        while (dataToSign.length() > 0) {
            if (dataToSign.length() > 15) {
                String mini = dataToSign.substring(0, 15);
                try {
                    encriptedData += encriptData(mini);
                } catch (Exception ex) {
                    Logger.getLogger(RSA.class.getName()).log(Level.SEVERE, null, ex);
                }
                dataToSign = dataToSign.substring(15, dataToSign.length());
            } else {
                String mini = dataToSign.substring(0, dataToSign.length());
                try {
                    encriptedData += encriptData(mini);
                } catch (Exception ex) {
                    Logger.getLogger(RSA.class.getName()).log(Level.SEVERE, null, ex);
                }
                dataToSign = "";
            }
        }
        return encriptedData;
    }

    public String decryptLongData(String encriptedData) {
        String decriptedData = "";
        while (encriptedData.length() > 0) {
            String mini1 = encriptedData.substring(0, 88);
            try {
                decriptedData += decriptData(mini1);
            } catch (Exception ex) {
                Logger.getLogger(RSA.class.getName()).log(Level.SEVERE, null, ex);
            }
            encriptedData = encriptedData.substring(88, encriptedData.length());
        }
        return decriptedData;
    }
}
