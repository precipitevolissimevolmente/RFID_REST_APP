package main.rest;

import com.google.common.collect.ImmutableMap;
import main.services.ChargeTagService;
import main.services.PayService;
import main.services.RFIDService;
import main.services.Utils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by G on 07.06.2015.
 */
@Path("/card")
public class RFIDController {

    @GET
    @Path("creditCard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreditCardData() throws Exception {
        RFIDService rfIdServices = new RFIDService();
        return Response.ok(rfIdServices.getCreditCardData())
                .header("Access-Control-Allow-Origin", "http://localhost")
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUID() throws Exception {
        RFIDService rfIdServices = new RFIDService();
        return Response.ok(ImmutableMap.of("serialNumber", rfIdServices.readUID()))
                .header("Access-Control-Allow-Origin", "http://localhost")
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response getATR() throws Exception {
        RFIDService rfIdServices = new RFIDService();
        return Response.ok(rfIdServices.getDetails())
                .header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("loadKey")
    public Response loadKey(List<Integer> key) throws Exception {
        if (key.size() != 6) {
            throw new InvalidParameterException();
        }
        RFIDService rfIdServices = new RFIDService();
        return Response.ok(rfIdServices.loadKey(key))
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("loadKeyAndAuth/{blockNumber}")
    public Response loadKeyAndAuthenticateBlock(@PathParam("blockNumber") Integer blockNumber, List<Integer> key) throws Exception {
        if (key.size() != 6) {
            throw new InvalidParameterException();
        }
        RFIDService rfIdServices = new RFIDService();
        Map<String, String> response = new HashMap<>();
        response.putAll(rfIdServices.loadKey(key));
        response.putAll(rfIdServices.authenticate(blockNumber));
        return Response.ok(response).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("write/{blockNumber}")
    public Response writeDataBlock(@PathParam("blockNumber") Integer blockNumber, String data) throws Exception {
        RFIDService rfIdServices = new RFIDService();
        Map<String, String> response = new HashMap<>();
        response.putAll(rfIdServices.writeData(blockNumber, data));
        return Response.ok(response).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("authenticate/{blockNumber}")
    public Response authenticate(@PathParam("blockNumber") Integer blockNumber) throws Exception {
        RFIDService rfIdServices = new RFIDService();
        return Response.ok(rfIdServices.authenticate(blockNumber)).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readBlockAscii/{blockNumber}")
    public Response readBlock(@PathParam("blockNumber") Integer blockNumber) throws Exception {
        RFIDService rfIdServices = new RFIDService();
        return Response.ok(ImmutableMap.of("dataBlock", rfIdServices.readBlockASCII(blockNumber)))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readBlockHex/{blockNumber}")
    public Response authenticateAndReadBlockHex(@PathParam("blockNumber") Integer blockNumber) throws Exception {
        RFIDService rfIdServices = new RFIDService();
        return Response.ok(ImmutableMap.of("dataBlock", rfIdServices.readBlockHex(blockNumber)))
                .header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readAllBlocksAsciiMIFARE1K")
    public Response getReadAllBlocks() throws Exception {
        RFIDService rfIdServices = new RFIDService();
        Map<String, String> blocks = new HashMap<>();
        for (Integer blockNumber : Utils.blocksForHashChain) {
            rfIdServices.authenticate(blockNumber);
            blocks.put(blockNumber.toString(), rfIdServices.readBlockASCII(blockNumber));
        }
        rfIdServices.authenticate(60);
        blocks.put("60", rfIdServices.readBlockASCII(60));
        blocks.put("61", rfIdServices.readBlockASCII(61));
        blocks.put("62", rfIdServices.readBlockASCII(62));
        return Response.ok(blocks).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readAllBlocksHexMIFARE1K")
    public Response getReadAllBlocksHex() throws Exception {
        RFIDService rfIdServices = new RFIDService();
        Map<String, String> blocks = new HashMap<>();
        for (Integer blockNumber : Utils.blocksForHashChain) {
            rfIdServices.authenticate(blockNumber);
            blocks.put(blockNumber.toString(), rfIdServices.readBlockHex(blockNumber));
        }
        rfIdServices.authenticate(60);
        blocks.put("60", rfIdServices.readBlockHex(60));
        blocks.put("61", rfIdServices.readBlockHex(61));
        blocks.put("62", rfIdServices.readBlockHex(62));
        return Response.ok(blocks).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readAllBlocksAsciiMIFFARE-Ultralight")
    public Response readAllUltraLightBlocks() throws Exception {
        RFIDService rfIdServices = new RFIDService();
        Map<String, String> blocks = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            blocks.put(String.valueOf(i), rfIdServices.readBlockStringUTF8(i));
        }

        return Response.ok(blocks).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("amount")
    public Response getAmount() throws Exception {
        ChargeTagService chargeTagService = new ChargeTagService();
        return Response.ok(chargeTagService.getCardAmount()).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("charge/{amount}")
    public Response charge(@PathParam("amount") Integer amount) throws Exception {
        ChargeTagService chargeTagService = new ChargeTagService();
        return Response.ok(chargeTagService.charge(amount)).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("pay/{numberOfTickets}")
    public Response pay(@PathParam("numberOfTickets") Integer numberOfTickets) throws Exception {
        PayService chargeTagService = new PayService("I29AC");
        return Response.ok(chargeTagService.payTax(numberOfTickets)).header("Access-Control-Allow-Origin", "*").build();
    }

    @OPTIONS
    public Response getOptions() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "http://localhost")
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD")
                .header("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept")
                .build();
    }


}
