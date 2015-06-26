package main.services;

import com.google.common.collect.ImmutableMap;
import main.payword.MessagePacket.Info;
import main.payword.MessagePacket.Message;
import main.util.LoadFromJSON;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

/**
 * Created by G on 11.06.2015.
 */
public class ChargeTagService {
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
     *
     * @return current amount on the card
     */
    public Map<String, String> getCardAmount() {
        String cardUID = rfidService.readUID();
        LoadFromJSON jsonLoader = new LoadFromJSON();
        Map<String, List<Integer>> cardKeys = jsonLoader.getCardKeys();
        String cardKey = cardKeys.get(cardUID).stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));
        List<String> hashChain = getPayWords();
        Integer usedPayWords = getUsedPayWords();
        int currentAmount = hashChain.size() - usedPayWords - 1;
        return ImmutableMap.of("amount", String.valueOf(currentAmount));
    }

    public Map<String, String> charge(Integer amount) {
        if (amount > 42) {
            return ImmutableMap.of(Utils.ERROR, "maximum amount allowed is 42");
        }
        Map<String, String> response = new HashMap<>();
        Message certificate;
        String cardUID = rfidService.readUID();

        LoadFromJSON loadCardKeys = new LoadFromJSON();
        Map<String, List<Integer>> cardKeys = loadCardKeys.getCardKeys();

        //check current amount
        List<String> hashChain = getPayWords();
        Integer usedPayWords = getUsedPayWords();
        int currentAmount = hashChain.size() - usedPayWords - 1;
        if (currentAmount + amount > 42) {
            int maxAmountToLoad = 42 - (hashChain.size() - usedPayWords);
            return ImmutableMap.of("currentAmount", String.valueOf(currentAmount), Utils.ERROR, "you can add only " + maxAmountToLoad);
        }
        response.put("totalAmount", String.valueOf(currentAmount+amount));

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
            return ImmutableMap.of(Utils.ERROR, "serverError");
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
        response.putAll(rfidService.loadKey(cardKeys.get(cardUID)));
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
        response.put("hashChain", "Loaded");
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


    static String sha1(String input) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] shortArray = new byte[16];
            byte[] result = mDigest.digest(input.getBytes()); //is 20bytes
            System.arraycopy(result, 0, shortArray, 0, shortArray.length);
            return new String(shortArray);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        return null;
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
            Logger.getLogger(ChargeTagService.class.getName()).log(Level.INFO, null, "PublicTransportServer->Primit Data");
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return message;
    }

    private Integer getUsedPayWords() {
        rfidService.authenticate(Utils.usedHashBlock);
        return Integer.valueOf(rfidService.readBlockASCII(Utils.usedHashBlock));
    }
}