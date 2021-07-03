package com.bilicraft.networkdiagnosis;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class CommandContainer {
    private String type;
    private boolean traceroute;
    private boolean ping;
    private boolean checkReachable;
    private boolean dnsLookup;
    private boolean netCard;
    private List<String> hosts;
}
