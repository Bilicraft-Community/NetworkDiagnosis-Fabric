package com.bilicraft.networkdiagnosis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
@AllArgsConstructor
@Data
@Builder
public class ResponseContainer {
    private String type;
    private String netCard;
    @Singular
    private List<HostResponse> responses;

    private String clientConnectedAddress;
    private long clientConnectedPing;

    @AllArgsConstructor
    @Data
    @Builder
    static class HostResponse{
        private String host;
        private String traceroute;
        private String ping;
        private boolean reachable;
        private String dnsLookup;
    }
}
