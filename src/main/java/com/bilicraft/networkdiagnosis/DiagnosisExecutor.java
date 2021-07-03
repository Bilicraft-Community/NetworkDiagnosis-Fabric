package com.bilicraft.networkdiagnosis;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author https://stackoverflow.com/questions/2627706/how-to-icmps-and-traceroutes-in-java
 */

public class DiagnosisExecutor {
    private final String os = System.getProperty("os.name").toLowerCase();

    public String traceroute(String host) {
        String output;
        try {
            Process process;
            if (os.contains("windows")) process = Runtime.getRuntime().exec("tracert " + host);
            else process = Runtime.getRuntime().exec("traceroute " + host);
            StringBuilder builder = new StringBuilder();
            // read the output from the command
            output = convertStreamToString(process.getInputStream());
            builder.append(output).append("\n");
            // read any errors from the attempted command
            String errors = convertStreamToString(process.getErrorStream());
            if (!errors.equals("")) builder.append(errors);
            return builder.toString();
        } catch (IOException e) {
            return e.getMessage();
        }

    }

    public String ping(String host) {
        String output;
        try {
            Process process;
            if (os.contains("windows")) process = Runtime.getRuntime().exec("ping  " + host);
            else process = Runtime.getRuntime().exec("ping " + host+" -c 10");
            StringBuilder builder = new StringBuilder();
            output = convertStreamToString(process.getInputStream());
            builder.append(output).append("\n");
            // read any errors from the attempted command
            String errors = convertStreamToString(process.getErrorStream());
            if (!errors.equals("")) builder.append(errors);
            return builder.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public String dnsLookup(String host) {
        String output;
        try {
            Process process = Runtime.getRuntime().exec("nslookup " + host);
            StringBuilder builder = new StringBuilder();
            output = convertStreamToString(process.getInputStream());
            builder.append(output).append("\n");
            // read any errors from the attempted command
            String errors = convertStreamToString(process.getErrorStream());
            if (!errors.equals("")) builder.append(errors);
            return builder.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public String netCard() {
        String output;
        try {
            Process process;
            if (os.contains("windows")) process = Runtime.getRuntime().exec("ipconfig /all");
            else process = Runtime.getRuntime().exec("ifconfig");
            StringBuilder builder = new StringBuilder();
            output = convertStreamToString(process.getInputStream());
            builder.append(output).append("\n");
            // read any errors from the attempted command
            String errors = convertStreamToString(process.getErrorStream());
            if (!errors.equals("")) builder.append(errors);
            return builder.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private String convertStreamToString(InputStream inputStream) {
        try {
            //noinspection UnstableApiUsage
            return new String(CharStreams.toString(new InputStreamReader(
                    inputStream)).getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_8).replaceAll("\r\n", "\n");
        } catch (IOException exception) {
            return exception.getMessage();
        }
    }
}
