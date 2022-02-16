package com.mrmarvel.pingpong.services;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

@SuppressWarnings("deprecation")
public class AsyncNetworkEstablish extends AsyncTask<Void, Void, Map<String, Object>> {

    private final UDP_Client client;
    private final String ip;
    private final int port;
    public SensorData sd = new SensorData();
    private DatagramSocket socket = null;

    public AsyncNetworkEstablish(UDP_Client client, String ip, int port) {
        this.client = client;
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected Map<String, Object> doInBackground(Void... voids) {
        socket = null;
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
            if (!socket.isConnected()) return ImmutableMap.of("result", -1);
            ;
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
        return result;
    }

    @Override
    protected void onPostExecute(Map<String, Object> resultMap) {
        super.onPostExecute(resultMap);
        Integer result = null;
        {
            Object resultObj = resultMap.get("result");
            if (resultObj instanceof Integer) {
                result = (Integer) resultObj;
            }
        }
        DatagramSocket socket = null;
        if ((resultMap.get("socket") instanceof DatagramSocket))
            socket = (DatagramSocket) resultMap.get("socket");
        if (result == null) throw new IllegalArgumentException("Where is \"result\"? in map");
        Intent i = new Intent(ClientUDPService.CONNECTED);
        String res = null;
        if (result < 0) res = "Нет соединения";
        else res = "Есть соединение";
        String reason = result.toString();
        client.returnResult(res, reason, socket);
    }
}
