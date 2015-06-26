package main.services;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by G on 10.06.2015.
 */
public class Experiments {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static void main(String[] args) {

//        String s = "D0 70 42 01 20 00 00 96 00 00 0F 5F 20 1A 43 48 41 4E 47 20 53 41 55 20 53 48 45 4F 4E 47 20 20 20 20 20 20 20 20 20 20 9F 1F 18 32 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 39 36 30 30 30 30 30 30";
        String s = "32 50 41 59 2e 53 59 53 2e 44 44 46 30 31 00";

        bytesToHex("2PAY.SYS.DDF01".getBytes());

        //MasterCard
        String hexValue = asciiToHex("Visa Electron");
//        String hex = bytesToHex("2PAY.SYS.DDF01".getBytes());
        String hex = "00A404000E";
//        String hex = s.replace(" ","");
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        System.out.println(output);

//        Map<String, String> cardD = new HashMap<>();
//
//        try (BufferedReader br = new BufferedReader(new FileReader("E:\\JavaProjects\\RFID_Rest\\RFID_REST_APP\\src\\main\\util\\cardDetails.json"))) {
//            String line;
//            int i = 0;
//            String key = "";
//            String value = "";
//            while ((line = br.readLine()) != null) {
//                if (i == 0) {
//                    key = line;
//                }
//                if(i != 0) {
//                    value = value + " " + line;
//                }
//                i++;
//                if (Objects.equals(line, "")) {
//                    cardD.put(key,value);
//                    key="";
//                    value="";
//                    i = 0;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(cardD);
//        Gson gson = new Gson();
//        String json = gson.toJson(cardD);
//
//        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
//                new FileOutputStream("E:\\JavaProjects\\RFID_Rest\\RFID_REST_APP\\src\\main\\util\\cardsInfo.json"), "utf-8"))) {
//            writer.write(json);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char aChar : chars) {
            hex.append(Integer.toHexString((int) aChar));
        }
        return hex.toString();
    }

    private static String asciiToByteArray(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char aChar : chars) {
            hex.append(Integer.toHexString((int) aChar));
        }
        return hex.toString();
    }

    private static String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
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
}
