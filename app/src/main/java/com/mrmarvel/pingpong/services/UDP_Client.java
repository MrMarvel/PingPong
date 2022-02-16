package com.mrmarvel.pingpong.services;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;

public class UDP_Client {
    private final ClientUDPService service;

    private volatile @Getter
    boolean isConnectionEstablished = false;
    private DatagramSocket workingSocket = null;
    public SensorData sd = new SensorData();
    AsyncNetworkSend sender = new AsyncNetworkSend();
    private String ip = null;
    private int port = 0;

    public UDP_Client(ClientUDPService service) {
        this.service = service;
    }


    private boolean checkChangeAddress(@NonNull String ip, int port) {
        if (this.ip == null) return true;
        if (this.port != port) return true;
        try {
            if (!this.ip.equals(InetAddress.getByName(ip).getHostAddress())) return true;
        } catch (UnknownHostException e) {
            return true;
        }
        return false;
    }
    public void sendData(@NonNull String ip, int port, @NonNull SensorData data) {
        if (checkChangeAddress(ip, port)) {
            close();
            establishConnection(ip, port);
        }
        sendData(data);
    }

    public void sendData(@NonNull SensorData data) {
        if (!isConnectionEstablished) return;
        if (workingSocket == null) return;
        sender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workingSocket);
        //networkSend(workingSocket, data);
    }

    Map<String, Object> establishConnection(String ip, int port) {
        AsyncNetworkEstablish async_client = new AsyncNetworkEstablish(this, ip, port);
        async_client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return null;
        /*DatagramSocket socket = null;
        Map<String, Object> result = Maps.newHashMap(ImmutableMap.of("result", 0));

        InetAddress address = null;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            result.put("result", -1);
            return result;
        }
        InetAddress finalAddress = address;
        Gson gson = new GsonBuilder().create();
        String msg = gson.toJson(sd);
        msg = StringUtils.rightPad(msg, 4000, " ");
        try {
            socket = new DatagramSocket();
            socket.connect(finalAddress, port);
            if (!socket.isConnected()) return ImmutableMap.of("result", -1);;
            DatagramPacket dp;
            dp = new DatagramPacket(msg.getBytes(), msg.getBytes().length, finalAddress, port);
            socket.setBroadcast(true);
            socket.send(dp);
            result.put("socket", socket);
        } catch (SocketException e) {
            result.put("result", -1);
            if (socket != null) socket.close();
            socket = null;
        } catch (IOException e) {
            result.put("result", -2);
            socket.close();
            socket = null;
        }
        return result;*/
    }

    public void sendAndReturn(@NonNull String ip, int port) {
        Map<String, Object> resultMap = establishConnection(ip, port);
        Integer result = null;
        {
            Object resultObj = resultMap.get("result");
            if (resultObj instanceof Integer) {
                result = (Integer) resultObj;
            }
        }
        if (!(resultMap.get("socket") instanceof DatagramSocket))
            throw new IllegalArgumentException("2nd argument must be " + DatagramSocket.class.getSimpleName() + "!");
        DatagramSocket socket = (DatagramSocket) resultMap.get("socket");
        if (result == null) throw new IllegalArgumentException("Where is \"result\"? in map");
        Intent i = new Intent(ClientUDPService.CONNECTED);
        String res = null;
        if (result < 0) res = "Нет соединения";
        else res = "Есть соединение";
        String reason = result.toString();
        returnResult(res, reason, socket);
    }

    private void networkSend(@NonNull DatagramSocket workingSocket, @NonNull SensorData sensorData) {
        Gson gson = new GsonBuilder().create();
        String msg = gson.toJson(sensorData);
        msg = StringUtils.rightPad(msg, 4000, " ");
        DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length);
        try {
            workingSocket.send(packet);
        } catch (IOException e) {
            Log.e("MINE ClientUDPService", e.toString());
        }
    }

    public void returnResult(String result, String reason, DatagramSocket socket) {
        if (socket != null) {
            workingSocket = socket;
            isConnectionEstablished = true;
            ip = workingSocket.getInetAddress().getHostAddress();
            port = workingSocket.getPort();
        }
        Intent i = new Intent(ClientUDPService.CONNECTED);
        i.putExtra("result", result);
        i.putExtra("reason", reason);
        service.sendBroadcast(i);
    }

    public void close() {
        if (workingSocket != null) {
            workingSocket.close();
            workingSocket = null;
        }

        if (isConnectionEstablished) {
            isConnectionEstablished = false;
        }
    }
}
