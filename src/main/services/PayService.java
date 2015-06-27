package main.services;

import com.google.common.collect.ImmutableMap;
import main.util.JsonUtil;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by G on 11.06.2015.
 */
public class PayService {
    public static final int ticketPrice = 2;
    private RFIDService rfidService;
    private String busNumber;

    public PayService(String busNumber) {
        if (busNumber.getBytes().length > 5) {
            throw new InvalidParameterException("Bus number size exceeded!");
        }

        this.busNumber = busNumber;
        rfidService = new RFIDService();
    }

    public Map<String, String> payTax(Integer numberOfTickets) {
        if (numberOfTickets < 1) {
            return ImmutableMap.of(Utils.ERROR, "Invalid number of tickets");
        }
        JsonUtil loadCardKeys = new JsonUtil();
        Map<String, List<Integer>> cardKeys = loadCardKeys.getCardKeys();
        String cardUID = rfidService.readUID();

        if (!cardKeys.keySet().contains(cardUID)) {
            return ImmutableMap.of("status", "Card not registered");
        }
        Map<String, String> status = new HashMap<>();
        status.put("status", "Card id is registered");
        status.putAll(rfidService.loadKey(cardKeys.get(cardUID)));
        Map<Integer, String> hashChainBlocks = rfidService.authenticateAndRead(Utils.blocksForHashChain);
//        status.putAll(hashChainBlocks.entrySet().stream()
//                .collect(Collectors.toMap(me -> "Block:" + me.getKey(), Map.Entry::getValue)));
        List<String> hashChain = hashChainBlocks.values().stream().filter(v -> !Objects.equals(v, "0"))
                .collect(Collectors.toList());
        Integer usedHash = getUsedPayWords();
        //Check amount
        if (usedHash + numberOfTickets * ticketPrice > hashChain.size()) {
            if(hashChain.size() - 1 - usedHash == 0) {
                status.put("error", "Card is empty");
                return status;
            }
            status.put("error", "You have only " + String.valueOf(hashChain.size() - 1 - usedHash) + " Ron");
            return status;
        }
        usedHash += numberOfTickets * ticketPrice;
        rfidService.authenticateAndWriteData(Utils.usedHashBlock, String.valueOf(usedHash));
        long epochSecond = Instant.now().getEpochSecond();
        rfidService.authenticateAndWriteData(Utils.lastBusData, String.valueOf(busNumber + "#" + epochSecond));
        status.put("busInfo", busNumber);
        status.put("time", Instant.ofEpochSecond(epochSecond).toString());
        status.put("left", hashChain.size() - usedHash - 1 + " Ron left");
        return status;
    }

    private Integer getUsedPayWords() {
        rfidService.authenticate(Utils.usedHashBlock);
        return Integer.valueOf(rfidService.readBlockASCII(Utils.usedHashBlock));
    }
}
