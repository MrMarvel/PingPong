package com.mrmarvel.pingpong.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kircherelectronics.fsensor.sensor.FSensor;

public class SensorService extends Service {

    private FSensor fSensor;

    public SensorService() {

    }
    // W.I.P.
    // Здесь будет сбор данных с датчиков в ЧЁРНОМ ЭКРАНЕ ТЕЛЕФОНА(ФОНОВАЯ РАБОТА)

    @Override
    public IBinder onBind(Intent intent) {
        // Заглушка
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Заглушка
        return super.onStartCommand(intent, flags, startId);
    }
}