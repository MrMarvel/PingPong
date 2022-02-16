package com.mrmarvel.pingpong.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
import lombok.NonNull;

// P.S. П**ДЕЦ ЛАПША ВКУСНАЯ
public class ClientUDPService extends IntentService {
    public static final String CHANNEL = "CLIENT_UDP_SERVICE";
    public static final String CONNECTED = "com.mrmarvel.pingpong.action.connected";
    public static final String ACTION_SEND = "com.mrmarvel.pingpong.client.send";
    public static final String INFO = "INFO";
    public final MyBinder binder = new MyBinder();
    private UDP_Client client = new UDP_Client(this);

    public ClientUDPService() {
        super("ClientUDPService");
    }

    public enum ACTION {
        CONNECT
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");
        return binder;
    }

    public class MyBinder extends Binder {
        ClientUDPService getService() {
            return ClientUDPService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Заглушка
        return super.onUnbind(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");
        if (intent == null) return;

        String ip = intent.getStringExtra("ip");
        if (ip == null) ip = "voidproject.play.ai";
        int port = intent.getIntExtra("port", 25565);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_SEND)) {
                if (Math.random() < 1) throw new RuntimeException("STOP USING INTENT TO FAST REPEATING  WORK!");
                SensorData data = null;
                try {
                    data = (SensorData) intent.getSerializableExtra("data");
                } catch (Exception ignored) {}
                if (data != null) {
                    sendData(ip, port, data);
                }
            }
        }
        ACTION action;
        try {
            action = ACTION.valueOf(Objects.requireNonNull(intent.getAction()).toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }
        switch (action) {
            case CONNECT:
                if (!client.isConnectionEstablished()) {
                    //client.establishConnection(ip, port);
                    establishConnection(ip, port);
                }
                break;
        }
        return;
    }

    public void sendData(@NonNull String ip, int port, @NonNull SensorData data) {
        client.sendData(ip, port, data);
    }

    public void establishConnection(@NonNull String ip, int port) {
        client.establishConnection(ip, port);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");
        String ip = null;
        if (intent == null) return START_STICKY;
        ip = intent.getStringExtra("ip");
        if (ip == null) ip = "voidproject.play.ai";
        int port = intent.getIntExtra("port", 25565);
        ACTION action;
        try {
            action = ACTION.valueOf(Objects.requireNonNull(intent.getAction()).toUpperCase());
        } catch (IllegalArgumentException e) {
            return START_STICKY;
        }
        switch (action) {
            case CONNECT:
                if (!client.isConnectionEstablished()) {
                    //client.establishConnection(ip, port);
                    establishConnection(ip, port);
                }
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");
        client.close();
        Log.d("MINE", "Intent service destroyed");
        super.onDestroy();
    }
}

@Data class SensorData implements Serializable {
    public @Expose float gyro_azimuth;
    public @Expose float gyro_pitch;
    public @Expose float gyro_roll;
    public @Expose float gyro_freq;

    public @Expose float accel_x;
    public @Expose float accel_y;
    public @Expose float accel_z;
    public @Expose float accel_freq;

}