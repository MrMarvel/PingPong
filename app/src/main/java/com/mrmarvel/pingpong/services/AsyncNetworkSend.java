package com.mrmarvel.pingpong.services;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

//
//
//
@SuppressWarnings("deprecation")
@Deprecated
public class AsyncNetworkSend extends AsyncTask<Object, Void, Void> {

    @Override
    protected Void doInBackground(Object... objects) {
        if (!(objects[0] instanceof DatagramSocket))
            throw new IllegalArgumentException("1st argument must be DatagramSocket!");
        DatagramSocket socket = (DatagramSocket) objects[0];

        if (!(objects[1] instanceof SensorData))
            throw new IllegalArgumentException("2nd argument must be SensorData!");
        SensorData sensorData = (SensorData) objects[1];
        Gson gson = new GsonBuilder().create();
        String msg = gson.toJson(sensorData);
        msg = StringUtils.rightPad(msg, 4000, " ");
        DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length);
        try {
            socket.send(packet);
        } catch (IOException e) {
            Log.e("MINE ClientUDPService", e.toString());
        }

        return null;
    }
}
