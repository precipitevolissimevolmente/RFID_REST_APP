package main.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by G on 11.06.2015.
 */
public class LoadFromJSON {
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
}
