package com.bilicraft.networkdiagnosis;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Environment(EnvType.CLIENT)
public class NetworkDiagnosis implements ModInitializer {
    private final DiagnosisExecutor diagnosisExecutor = new DiagnosisExecutor();
    private final Identifier identifier = new Identifier("networkdiagnosis", "command");
    private final Gson gson = new Gson();
    private final ClientPlayNetworking.PlayChannelHandler playChannelHandler = (client, handler, buf, responseSender) -> {
        try {
            byte[] buff = new byte[buf.readableBytes()];
            buf.readBytes(buff);
            String data = new String(buff, StandardCharsets.UTF_8);
            CommandContainer commandContainer = gson.fromJson(data, CommandContainer.class);

            Thread thread = new Thread("NetworkDiagnosis Executor") {
                @Override
                public void run() {
                    ResponseContainer responseContainer = executeCommand(client, commandContainer);
                    String response = gson.toJson(responseContainer);
                    ByteBuf buf = Unpooled.buffer(response.getBytes().length);
                    buf.writeBytes(response.getBytes(StandardCharsets.UTF_8));
                    Packet<?> responsePacket = responseSender.createPacket(identifier, new PacketByteBuf(buf));
                    responseSender.sendPacket(responsePacket);
                }
            };
            thread.start();
            ServerInfo connected = client.getCurrentServerEntry();
            if (connected != null) {
                System.out.println("You're on server " + connected.name + ", address: " + connected.address);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        ClientPlayConnectionEvents.INIT.register((handler, sender) -> ClientPlayNetworking.registerReceiver(identifier, playChannelHandler));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientPlayNetworking.unregisterReceiver(identifier));
    }

    private ResponseContainer executeCommand(MinecraftClient client, CommandContainer commandContainer) {
        ResponseContainer.ResponseContainerBuilder builder = ResponseContainer.builder();
        if (!commandContainer.getType().equals("test")) {
            return new ResponseContainer("error", null, null, null, -1);
        }
        if (commandContainer.isNetCard()) {
            builder.netCard(diagnosisExecutor.netCard());
        }
        for (String host : commandContainer.getHosts()) {
            ResponseContainer.HostResponse.HostResponseBuilder hostResponseBuilder = ResponseContainer.HostResponse.builder();
            hostResponseBuilder.host(host);
            System.out.println("Executing checks for host: " + host);
            if (commandContainer.isCheckReachable()) {
                System.out.println("Executing checks for host: " + host + ": Reachable");
                hostResponseBuilder.reachable(reachable(host));
            }
            if (commandContainer.isDnsLookup()) {
                System.out.println("Executing checks for host: " + host + ": DnsLookup");
                hostResponseBuilder.dnsLookup(diagnosisExecutor.dnsLookup(host));
            }
            if (commandContainer.isPing()) {
                System.out.println("Executing checks for host: " + host + ": ICMP Ping");
                hostResponseBuilder.ping(diagnosisExecutor.ping(host));
            }
            if (commandContainer.isTraceroute()) {
                System.out.println("Executing checks for host: " + host + ": Traceroute");
                hostResponseBuilder.traceroute(diagnosisExecutor.traceroute(host));
            }
            System.out.println("Executing checks for host: " + host + ": Complete");
            builder.response(hostResponseBuilder.build());
        }
        if (client.getCurrentServerEntry() != null) {
            builder.clientConnectedAddress(client.getCurrentServerEntry().address);
            builder.clientConnectedPing(client.getCurrentServerEntry().ping);
        }
        builder.type("test");
        return builder.build();
    }

    private boolean reachable(String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            return inetAddress.isReachable(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
