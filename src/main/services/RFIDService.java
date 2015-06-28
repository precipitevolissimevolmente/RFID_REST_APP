package main.services;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import main.util.JsonUtil;

import javax.ejb.Singleton;
import javax.smartcardio.*;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static main.services.Utils.asciiToHex;
import static main.services.Utils.parseResponse;

/**
 * Created by G on 07.06.2015.
 */
@Singleton
public class RFIDService {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static final int error = -1;
    public static final int MEMORY_BLOCK_SIZE = 16;
    public static final String LOAD_KEY = "LoadKey";
    public static final String ERROR_S = "error";
    private CardTerminal terminal;
    Map<String, String> atrInfo;

    public RFIDService() {
        try {
            TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
            List<CardTerminal> terminals = factory.terminals().list();
            if (terminals.isEmpty()) {
                throw new Exception("No card terminals available");
            }
            terminal = terminals.get(0);
            JsonUtil loadFromJSON = new JsonUtil();
            atrInfo = loadFromJSON.getATRInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getCreditCardData() {
        Map<String, String> info = new HashMap<>();
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            /*
            * CA is the <Get Data> command
            * */
            CommandAPDU command = new CommandAPDU(toByteArray(of(0xFF, 0xCA, 0x00, 0x00, 0x04)));
            ResponseAPDU response = channel.transmit(command);
            byte[] byteArray = response.getBytes();
            info.put("UID", parseResponse(bytesToHex(byteArray)));
            /*
            *   Get PPSE - Proximit Payment System Environment
            *   Command:  00 a4 04 00 0e    32 50 41 59 2e 53 59 53 2e 44 44 46 30 31 00
            *   00 a4 04 00 0e : select command defined in ISO7816
            *   32 50 41 59 2e 53 59 53 2e 44 44 46 30 31 00 : 2PAY.SYS.DDF01
            * */
            command = new CommandAPDU(Bytes.concat(toByteArray(of(0x00, 0xA4, 0x04, 0x00, 0x0E)), "2PAY.SYS.DDF01".getBytes()));
            response = channel.transmit(command);
            info.put("PPSE", parseResponse(bytesToHex(response.getBytes())));

            /*
            *   Command to get card Type for MasterCard
            * */
            command = new CommandAPDU(toByteArray(of(0x00, 0xA4, 0x04, 0x00, 0x07,
                    0xA0, 0x00, 0x00, 0x00, 0x04,
                    0x10, 0x10, 0x00
            )));
            String hexResponse = bytesToHex(channel.transmit(command).getBytes());
            //6A83: Record not found
            //6A82: File not found
            if (!hexResponse.toUpperCase().equals("6A83") && !hexResponse.toUpperCase().equals("6A82")) {
                info.put("cardType", parseCardTypeInfo(hexResponse));
            }

            /*
            *   Command to get card Type for Visa
            * */
            command = new CommandAPDU(toByteArray(of(0x00, 0xA4, 0x04, 0x00, 0x07,
                    0xA0, 0x00, 0x00, 0x00, 0x03,
                    0x10, 0x10, 0x00
            )));
            hexResponse = bytesToHex(channel.transmit(command).getBytes());
            if (!hexResponse.toUpperCase().equals("6A83") && !hexResponse.toUpperCase().equals("6A82")) {
                info.put("cardType", parseCardTypeInfo(hexResponse));
            }

            /*
            *   Command to get card number for EMV (EuroPay MasterCard Visa) card : 00 B2 01 0C 00
            * */
            command = new CommandAPDU(toByteArray(of(0x00, 0xB2, 0x01, 0x0C, 0x00)));
            response = channel.transmit(command);
            info.put("cardNumber", parseCardNumber(bytesToHex(response.getBytes())));

            //Cardholder for VISA
            command = new CommandAPDU(toByteArray(of(0x00, 0xB2, 0x02, 0x0C, 0x00)));
            response = channel.transmit(command);
            info.put("cardholder", parseCardHolder(bytesToHex(response.getBytes())));

//            //Cardholder for ---
//            command = new CommandAPDU(toByteArray(of(0x00, 0xB2, 0x02, 0x0C, 0x00)));
//            responseB = channel.transmit(command);
//            info.put("ss", Utils.hexToASCII(bytesToHex(responseB.getBytes())));

            //Transaction history
            response = getTransactionHistory(channel, 0x01);
            info.put("t1", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x02);
            info.put("t2", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x03);
            info.put("t3", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x04);
            info.put("t4", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x05);
            info.put("t5", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x06);
            info.put("t6", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x07);
            info.put("t7", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x08);
            info.put("t8", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x09);
            info.put("t9", parseTransaction(bytesToHex(response.getBytes())));

            response = getTransactionHistory(channel, 0x0A);
            info.put("t10", parseTransaction(bytesToHex(response.getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    private ResponseAPDU getTransactionHistory(CardChannel channel, int nr) throws CardException {
        CommandAPDU command;
        ResponseAPDU response;
        command = new CommandAPDU(toByteArray(of(0x00, 0xB2, nr, 0x64, 0x00)));
        response = channel.transmit(command);
        if (bytesToHex(response.getBytes()).equals("6A82")) {
            command = new CommandAPDU(toByteArray(of(0x00, 0xB2, nr, 0x5C, 0x00)));
            response = channel.transmit(command);
        }
        return response;
    }

    private String parseTransaction(String responseHex) {
        String amount = responseHex.substring(2, 12);
        String date = responseHex.substring(18, 24);

        return amount + " " + date;
    }

    private String parseCardHolder(String responseHex) {
        if (!responseHex.contains("5F20")) {
            return ""; //not found
        }
        int startIndex = responseHex.toUpperCase().indexOf("5F20");
        int endIndex = responseHex.toUpperCase().indexOf("9F1F");
        return Utils.hexToASCII(responseHex.substring(startIndex, endIndex));
    }

    private String parseCardTypeInfo(String cardTypeInfo) {
        if (cardTypeInfo.toUpperCase().contains(asciiToHex("Visa Electron").toUpperCase())) {
            return "Visa Electron";
        }
        if (cardTypeInfo.toUpperCase().contains(asciiToHex("MasterCard").toUpperCase())) {
            return "MasterCard";
        }
        return Utils.hexToASCII(cardTypeInfo);
    }

    private String parseCardNumber(String hexData) {
        int indexOf = hexData.indexOf("D1");
        return hexData.substring(indexOf - 16, indexOf);
    }

    private byte[] toByteArray(List<Integer> commandInHex) {
        byte[] command = new byte[commandInHex.size()];
        for (int i = 0; i < commandInHex.size(); i++) {
            command[i] = (byte) commandInHex.get(i).intValue();
        }
        return command;
    }

    /**
     * @return CARD UNIQUE IDENTIFIER
     */
    public String readUID() {
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            /*
            * CA is the <Get Data> command
            * */
            CommandAPDU command = new CommandAPDU(new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x04});
            ResponseAPDU response = channel.transmit(command);
            byte[] byteArray = response.getBytes();
            String tagResponse = bytesToHex(byteArray);
            return Utils.parseResponse(tagResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ">>Read Error";
        }
    }

    public Map<String, String> getDetails() {
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            /*
            *
            * This ATR string (as defined in ISO 7816) is telling us how we can communicate with the current tag found on the card reader.
            * */
            byte[] ATRbytes = card.getATR().getBytes();
            String atr = bytesToHexFormatted(ATRbytes);

            Map<String, String> info = new HashMap<>();
            info.put("ATR", atr);
            if (atrInfo.get(atr.trim()) != null) {
                info.put("details", atrInfo.get(atr.trim()));
            }
            return info;
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> loadKey(List<Integer> key) {
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            CommandAPDU command = getKeyCommand(key);
            ResponseAPDU response = channel.transmit(command);

            byte[] byteArray = response.getBytes();
            String tagResponse = bytesToHex(byteArray);

            if (tagResponse.contains(Utils.SUCCESS)) {
                return ImmutableMap.of(LOAD_KEY, "ok");
            } else {
                return ImmutableMap.of(LOAD_KEY, ERROR_S);
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CommandAPDU getKeyCommand(List<Integer> key) {
        byte[] command = new byte[11];
        command[0] = (byte) 0xFF;
        command[1] = (byte) 0x82;
        command[2] = (byte) 0x00;
        command[3] = (byte) 0x00; //key location 0x00
        command[4] = (byte) 0x06;
        for (int i = 5; i < 11; i++) {
            command[i] = key.get(i - 5).byteValue();
        }
        return new CommandAPDU(command);
    }

    public Map<String, String> authenticate(int blockNumber) {
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            CommandAPDU command = getAuthenticationCommand(blockNumber);
            ResponseAPDU response = channel.transmit(command);
            byte[] byteArray = response.getBytes();
            String tagResponse = bytesToHex(byteArray);


            if (tagResponse.contains("90")) {
                return ImmutableMap.of("authentication", "ok");
            } else {
                return ImmutableMap.of("authentication", "error", "errCode", tagResponse);
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param blocksToRead
     * @return Map of blockId vs data converted to sting
     */
    public Map<Integer, String> authenticateAndRead(List<Integer> blocksToRead) {
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            Map<Integer, String> blockNumberVsData = new HashMap<>();
            for (Integer blockNumber : blocksToRead) {
                //Authenticate block
                ResponseAPDU response = channel.transmit(getAuthenticationCommand(blockNumber));
                byte[] byteArray = response.getBytes();
                String tagResponse = bytesToHex(byteArray);
                if (!tagResponse.contains("90")) {
                    return ImmutableMap.of(error, tagResponse);
                }
                //Read block
                blockNumberVsData.put(blockNumber, readBlockASCII(blockNumber));
            }
            return blockNumberVsData;
        } catch (CardException e) {
            e.printStackTrace();
            return ImmutableMap.of(error, "errr");
        }
    }

    private CommandAPDU getAuthenticationCommand(Integer blockNumber) {
        if (blockNumber < 0 || blockNumber > 63) {
            throw new InvalidParameterException("Block Number must be between 0 and 63.");
        }
        /*
        * Authentication with a type A (0x60), key number 0x00 for memory block 0x04
        * FF 86 00 00 05 01 00 01 60 00
        * 01 ->     memory Block
        * FF 86 00 00 05 01 00 04 60 00
        * 60 ->     Key Type: TYPE A
        * 00 ->     Key Number: 00
        * 04 ->     memory Block
        * */
        byte keyTypeA = (byte) 0x60;
        return new CommandAPDU(new byte[]{(byte) 0xFF, (byte) 0x86, (byte) 0x00, (byte) 0x00,
                (byte) 0x05, (byte) 0x01, (byte) 0x00, blockNumber.byteValue(), keyTypeA, (byte) 0x00});
    }

    public String readBlockHex(Integer blockNumber) {
        try {
            byte[] byteArray = readBlock(blockNumber);
            return bytesToHex(byteArray);
        } catch (SecurityException e) {
            return "Authentication failed";
        }
    }

    public String readBlockASCII(Integer blockNumber) {
        byte[] byteArray = readBlock(blockNumber);
        return new String(byteArray).trim();
    }

    public String readBlockStringUTF8(Integer blockNumber) {
        byte[] byteArray = readBlock(blockNumber);
        return new String(byteArray, Charset.forName("UTF-8"));
    }

    private byte[] readBlock(Integer blockNumber) {
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();
            /*
            * Ex read from block 0x04, 16bytes
            *
            * FF B0 00 04 10
            * 04 ->     memory Block
            * */
            CommandAPDU command = new CommandAPDU(new byte[]{(byte) 0xFF, (byte) 0xB0, (byte) 0x00, blockNumber.byteValue(), (byte) 0x10});
            ResponseAPDU response = channel.transmit(command);
            byte[] bytes = response.getBytes();
            if (bytes.length < 16) {
                throw new SecurityException("Authentication failed");
            }
            byte[] utilBytes = new byte[16];
            System.arraycopy(bytes, 0, utilBytes, 0, utilBytes.length);
            return utilBytes;
        } catch (CardException e) {
            e.printStackTrace();
            throw new InstantiationError("Cant reach Card Reader");
        }
    }

    /**
     * @param blockNumber data block number
     * @param data        data to write
     * @return status
     */
    public Map<String, String> authenticateAndWriteData(int blockNumber, String data) {
        validateBlockToWrite(blockNumber);
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            //Authenticate block
            ResponseAPDU response = channel.transmit(getAuthenticationCommand(blockNumber));
            String tagResponse = bytesToHex(response.getBytes());
            if (!tagResponse.contains("90")) {
                return ImmutableMap.of(Utils.ERROR, tagResponse);
            }
            //write data block
            if (write(blockNumber, data)) {
                return ImmutableMap.of("write", "Write command executed");
            }
            return ImmutableMap.of("write", "Write command failed");
        } catch (CardException e) {
            e.printStackTrace();
            return ImmutableMap.of(Utils.ERROR, "errr");
        }
    }

    /**
     * @param blockNumber data block number
     * @param data        data to write
     * @return status
     */
    public Map<String, String> writeData(int blockNumber, String data) {
        validateBlockToWrite(blockNumber);
        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            //write data block
            if (write(blockNumber, data)) {
                return ImmutableMap.of("write", "command executed");
            }
            return ImmutableMap.of("write", "command failed");
        } catch (CardException e) {
            e.printStackTrace();
            return ImmutableMap.of(Utils.ERROR, "write error");
        }
    }

    private boolean write(Integer blockNumber, String data) {
        validateBlockToWrite(blockNumber);
        byte[] bytesToWrite = toByteArray(data);
        if (bytesToWrite.length > MEMORY_BLOCK_SIZE) {
            throw new InvalidParameterException("Memory block has only 16 bytes.");
        }

        //Init byte array with 0x00
        byte[] dataBytes = new byte[MEMORY_BLOCK_SIZE];
        for (int i = 0; i < 16; i++) {
            dataBytes[i] = (byte) 0x00;
        }

        int dataLength = bytesToWrite.length;
        int j = 0;
        for (int i = MEMORY_BLOCK_SIZE - dataLength; i < MEMORY_BLOCK_SIZE; i++) {
            dataBytes[i] = bytesToWrite[j];
            j++;
        }
        /*
        * Write to block number 3E
        * FF D6 00 3E 10 3E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2E
        * 3E ->     memory Block
        * 3E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2E ->    value to write
        * */
        byte[] commandWrite = new byte[21];
        commandWrite[0] = (byte) 0xFF;
        commandWrite[1] = (byte) 0xD6;
        commandWrite[2] = (byte) 0x00;
        commandWrite[3] = blockNumber.byteValue();
        commandWrite[4] = (byte) 0x10;
        for (int i = 5; i < 21; i++) {
            commandWrite[i] = dataBytes[i - 5];
        }

        try {
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();
            ResponseAPDU response = channel.transmit(new CommandAPDU(commandWrite));
            if (bytesToHex(response.getBytes()).contains("90")) {
                return true;
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return false;
    }

    private byte[] toByteArray(String data) {
        return data.getBytes();
    }

    private void validateBlockToWrite(int blockNumber) {
        if (blockNumber == 0 || (blockNumber + 1) % 4 == 0) {
            throw new InvalidParameterException("Restrict to write in trailer or 0x00 block.");
        }
    }


    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHexFormatted(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2 + bytes.length];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

}
