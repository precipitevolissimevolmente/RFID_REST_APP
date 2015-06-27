package main.services;

import com.google.common.collect.ImmutableList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by G on 08.06.2015.
 */
public class Utils {

    public static final String SUCCESS = "90";
    public static final String ERROR = "error";
    public static List<Integer> blocksForHashChain = ImmutableList.of(
            1, 2,
            4, 5, 6,
            8, 9, 10,
            12, 13, 14,
            16, 17, 18,
            20, 21, 22,
            24, 25, 26,
            28, 29, 30,
            32, 33, 34,
            36, 37, 38,
            40, 41, 42,
            44, 45, 46,
            48, 49, 50,
            52, 53, 54,
            56, 57, 58
    );
    public static Integer certificateBlock = 62;
    public static Integer lastBusData = 61;
    public static Integer usedHashBlock = 60;

    public static String parseResponse(String tagResponse) {
        if (!tagResponse.contains("90")) {
            return "invalid";
        }
        int lastIndexOf = tagResponse.lastIndexOf(SUCCESS);
        return tagResponse.substring(0, lastIndexOf);
    }

    public static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char aChar : chars) {
            hex.append(Integer.toHexString((int) aChar));
        }
        return hex.toString();
    }

    public static String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String sha1(String input) {
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
}
