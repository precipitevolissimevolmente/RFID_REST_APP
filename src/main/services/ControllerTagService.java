package main.services;

import com.google.common.collect.ImmutableMap;
import main.payword.MessagePacket.ControllerMessage;
import main.util.JsonUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static main.services.Utils.blocksForHashChain;
import static main.services.Utils.sha1;

/**
 * Created by G on 11.06.2015.
 */
public class ControllerTagService {
    public static final String STATUS = "status";
    private ObjectInputStream input;
    private ObjectOutputStream output;
    RFIDService rfidService;

    private String[] hashChain1;
    private int used1 = 0;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 5050;

    public ControllerTagService() {
        rfidService = new RFIDService();
    }

    public Map<String, String> checkCardCertificate() {
        Map<String, String> response = new HashMap<>();
        String cardUID = rfidService.readUID();

        JsonUtil loadCardKeys = new JsonUtil();
        Map<String, List<Integer>> cardKeys = loadCardKeys.getCardKeys();
        if (cardKeys.get(cardUID) == null) {
            return ImmutableMap.of(STATUS, "Invalid card");
        }
        //Load key
        response.putAll(rfidService.loadKey(cardKeys.get(cardUID)));
        String cardCertificate = getCardCertificate();
        ControllerMessage msg = new ControllerMessage("check").setCertificate(cardCertificate).setCardNumber(cardUID);


        try {
            initCommunication();
        } catch (IOException e) {
            e.printStackTrace();
            return ImmutableMap.of(Utils.ERROR, "server Offline");
        }

        System.out.println("in and out local initialized");
        sendMessageToPublicTransportServer(msg);
        ControllerMessage fromServer = readFromBServer();
        sendMessageToPublicTransportServer(ControllerMessage.messageQuit());
        //Close connection to B
        try {
            input.close();
            output.close();
            System.out.println("\nConnection to bank is closed");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        response.put("status", fromServer.isValid() ? "Certificate is valid" : "Invalid certificate");
        return response;
    }

    private void initCommunication() throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        output = new ObjectOutputStream(socket.getOutputStream());
//        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        System.out.println("in and out initialized");
    }

    /**
     * For us PublicTransportServer will serve as a Bank (B)
     *
     * @param messagePacket
     */
    private void sendMessageToPublicTransportServer(ControllerMessage messagePacket) {
        try {
            this.output.writeObject(messagePacket);
            this.output.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Send MPB");
    }

    public ControllerMessage readFromBServer() {
        System.out.println("read from PublicTransportServer (Bank)");
        ControllerMessage message = null;
        try {
            message = (ControllerMessage) this.input.readObject();
            Logger.getLogger(ControllerTagService.class.getName()).log(Level.INFO, null, "PublicTransportServer->Received Data");
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return message;
    }

    private String getCardCertificate() {
        rfidService.authenticate(Utils.certificateBlock);
        return rfidService.readBlockASCII(Utils.certificateBlock);
    }
}
