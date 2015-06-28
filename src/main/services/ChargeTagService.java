package main.services;

import com.google.common.collect.ImmutableMap;
import main.payword.MessagePacket.Info;
import main.payword.MessagePacket.Message;
import main.util.JsonUtil;

import javax.ejb.Singleton;
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

import static main.payword.MessagePacket.Message.messageQuit;
import static main.services.Utils.blocksForHashChain;
import static main.services.Utils.sha1;

/**
 * Created by G on 11.06.2015.
 */
@Singleton
public class ChargeTagService {
    public static final String STATUS = "status";
    private ObjectInputStream input;
    private ObjectOutputStream output;
    RFIDService rfidService;

    private String[] hashChain1;
    private int used1 = 0;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 5050;

    public ChargeTagService() {
        rfidService = new RFIDService();
    }

    /**
     * @return current amount on the card
     */
    public Map<String, String> getCardAmount() {
        String cardUID = rfidService.readUID();
        JsonUtil jsonLoader = new JsonUtil();
        Map<String, List<Integer>> cardKeys = jsonLoader.getCardKeys();
        if (cardKeys.get(cardUID) == null) {
            return ImmutableMap.of(STATUS, "Invalid card");
        }
        Map<String, String> loadKeyResult = rfidService.loadKey(cardKeys.get(cardUID));
        if (Objects.equals(loadKeyResult.get(RFIDService.LOAD_KEY), RFIDService.ERROR_S)) {
            return ImmutableMap.of(STATUS, "Invalid key");
        }

        Integer usedPayWords = getUsedPayWords();
        if (usedPayWords == -1) {
            return ImmutableMap.of("status", "New Card");
        }
        List<String> hashChain = getPayWords();
        int currentAmount = hashChain.size() - usedPayWords - 1;
        return ImmutableMap.of("amount", String.valueOf(currentAmount) + " RON", "tickets", String.valueOf(currentAmount / 2));
    }

    public Map<String, String> charge(Integer amount) {
        if (amount > 42) {
            return ImmutableMap.of(Utils.ERROR, "Maximum amount allowed is 42");
        }
        if (amount < 1) {
            return ImmutableMap.of(Utils.ERROR, "Invalid amount");
        }
        Map<String, String> response = new HashMap<>();
        Message certificate;
        String cardUID = rfidService.readUID();

        JsonUtil loadCardKeys = new JsonUtil();
        Map<String, List<Integer>> cardKeys = loadCardKeys.getCardKeys();
        if (cardKeys.get(cardUID) == null) {
            return ImmutableMap.of(STATUS, "Invalid card");
        }
        //Load key
        response.putAll(rfidService.loadKey(cardKeys.get(cardUID)));
        //check current amount
        Integer usedPayWords = getUsedPayWords();
        List<String> hashChain = getPayWords();
        int currentAmount = hashChain.size() - usedPayWords - 1;
        if (usedPayWords == -1) {
            currentAmount = 0;
            response.put("status", "New card");
        }

        if (currentAmount + amount > 42) {
            int maxAmountToLoad = 42 - currentAmount;
            if (maxAmountToLoad == 0) {
                return ImmutableMap.of("status", "Card is full");
            }
            return ImmutableMap.of("currentAmount", String.valueOf(currentAmount), Utils.ERROR, "you can add only " + maxAmountToLoad);
        }
        response.put("totalAmount", "Total amount: " + String.valueOf(currentAmount + amount));

        Message msg = new Message();
        msg.setMessage("getCertificateAndCharge");
        msg.setCardNumber(cardUID);
        String cardKey = cardKeys.get(cardUID).stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));
        msg.setPublicKey(cardKey);
        Info info = new Info();
        info.setAddress("00000");//Mock Address
        info.setSum(amount);
        info.setCurrentAmount(currentAmount);
        msg.setInfo(info);

        try {
            initCommunication();
        } catch (IOException e) {
            e.printStackTrace();
            return ImmutableMap.of(Utils.ERROR, "server Offline");
        }

        System.out.println("in and out local initialized");
        sendMessageToPublicTransportServer(msg);
        certificate = readFromBServer();
        System.out.println(certificate.getInfo().getSum());
        sendMessageToPublicTransportServer(messageQuit());
        //Close connection to B
        try {
            input.close();
            output.close();
            System.out.println("\nConnection to bank is closed");
            //Generate a random number and compute hashChain
            hashChain1 = computeHashChain(certificate.getInfo().getSum(), 1);
            Logger.getLogger(ChargeTagService.class.getName()).log(Level.INFO, null, "HashChains computed SHA1");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //write data to TAG
        //Save on card block 62 certificate
        response.putAll(rfidService.authenticateAndWriteData(62, sha1(certificate.toString())));
        //Save on card block 60 zero for number for used hashChain
        response.putAll(rfidService.authenticateAndWriteData(60, "0"));
        //Clean Data Blocks
        for (Integer blockNumber : blocksForHashChain) {
            rfidService.authenticateAndWriteData(blockNumber, "0");
        }
        //Load hash chain into blocks
        for (int i = 0; i < hashChain1.length; i++) {
            rfidService.authenticateAndWriteData(blocksForHashChain.get(blocksForHashChain.size() - 1 - i), hashChain1[i]);
        }
        response.put("hashChain", "Hash chain loaded");
        return response;
    }

    private List<String> getPayWords() {
        Map<Integer, String> hashChainBlocks = rfidService.authenticateAndRead(Utils.blocksForHashChain);
        return hashChainBlocks.values().stream().filter(v -> !Objects.equals(v, "0"))
                .collect(Collectors.toList());
    }

    private void initCommunication() throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        output = new ObjectOutputStream(socket.getOutputStream());
//        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        System.out.println("in and out initialized");
    }

    private String[] computeHashChain(int sum, int i) {
        int length = sum / i + 1;
        String[] hc = new String[length];
        BigInteger randomNumber = geneRandomNumber();
        hc[length - 1] = sha1(randomNumber.toString());
        for (int j = length - 2; j >= 0; j--) {
            hc[j] = sha1(hc[j + 1]);
        }
        return hc;
    }

    public BigInteger geneRandomNumber() {
        SecureRandom secureRandom = new SecureRandom();
        BigInteger r = new BigInteger(1024, secureRandom);
        return r;
    }

    /**
     * For us PublicTransportServer will serve as a Bank (B)
     *
     * @param messagePacket
     */
    private void sendMessageToPublicTransportServer(Message messagePacket) {
        try {
            this.output.writeObject(messagePacket);
            this.output.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Send MPB");
    }

    public Message readFromBServer() {
        System.out.println("read from PublicTransportServer (Bank)");
        Message message = new Message();
        try {
            message = (Message) this.input.readObject();
            Logger.getLogger(ChargeTagService.class.getName()).log(Level.INFO, null, "PublicTransportServer->Received Data");
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return message;
    }

    private Integer getUsedPayWords() {
        rfidService.authenticate(Utils.usedHashBlock);
        try {
            return Integer.valueOf(rfidService.readBlockASCII(Utils.usedHashBlock));
        } catch (NumberFormatException ignore) {
            return -1;
        }
    }
}
