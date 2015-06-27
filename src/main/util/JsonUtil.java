package main.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;

import java.io.*;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by G on 11.06.2015.
 */
public class JsonUtil {

    public static final String PATH_TO_CARD_CERTIFICATES = "E:\\JavaProjects\\RFID_Rest\\RFID_REST_APP\\src\\main\\util\\cardCertificates.json";

    public Map<String, List<Integer>> getCardKeys() {
        Gson gson = new Gson();
        try {
            URL url = getClass().getResource("cardKeys.json");
            File file = new File(url.getPath());
            BufferedReader br = new BufferedReader(
                    new FileReader(file));

            Map<String, List<Double>> rawKeys = gson.fromJson(br, Map.class);

            return rawKeys.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            k -> k.getValue().stream().map(Double::intValue).collect(Collectors.toList())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableMap.of();
    }

    public Map<String, String> getATRInfo() {
        Gson gson = new Gson();
        try {
            URL url = getClass().getResource("cardsInfo.json");
            File file = new File(url.getPath());
            BufferedReader br = new BufferedReader(
                    new FileReader(file));

            return gson.fromJson(br, Map.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableMap.of();
    }

    public void saveCertificate(String cardId, String certificate) {
        Gson gson = new Gson();
        Map<String, String> allCertificates = getAllCertificates();
        allCertificates.put(cardId, certificate);
        File file = new File(PATH_TO_CARD_CERTIFICATES);
        writeToFile(file, gson.toJson(allCertificates));
    }

    public Map<String, String> getAllCertificates() {
        Gson gson = new Gson();
        try {
            File file = new File(PATH_TO_CARD_CERTIFICATES);
            BufferedReader br = new BufferedReader(
                    new FileReader(file));

            Map map = gson.fromJson(br, Map.class);
            if (map == null) {
                return new HashMap<>();
            }
            return map;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public void writeToFile(File file, String data) {
        try {
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            throw new InvalidParameterException("Write exceptions");
        }
    }
}
