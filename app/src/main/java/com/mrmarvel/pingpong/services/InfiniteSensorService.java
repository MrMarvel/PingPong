package com.mrmarvel.pingpong.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.kircherelectronics.fsensor.observer.SensorSubject;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.sensor.acceleration.LinearAccelerationSensor;
import com.kircherelectronics.fsensor.sensor.gyroscope.ComplementaryGyroscopeSensor;

import java.util.Objects;

public class InfiniteSensorService extends Service {

    private final SensorData data = new SensorData();
    private String ip;
    private int port;

    private FSensor sensorAcceleration;
    private FSensor sensorGyro;

    private volatile ClientUDPService clientUDPService = null;
    private volatile boolean isSensorGyroRefreshed = false;
    private volatile boolean isSensorAccelRefreshed = false;




    ServiceConnection clientUDPServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("MINE Inf.SensorService", ClientUDPService.class.getSimpleName()+" is connected");
            clientUDPService = ((ClientUDPService.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("MINE Inf.SensorService", ClientUDPService.class.getSimpleName()+" is disconnected");
        }
    };

    private final SensorSubject.SensorObserver sensorAccelerationObserver = values -> {
        data.gyro_azimuth = values[0];
        data.gyro_pitch = values[1];
        data.gyro_roll = values[2];
        data.gyro_freq = values[3 ];
        isSensorAccelRefreshed = true;
        checkReadyToSend();
    };

    private final SensorSubject.SensorObserver sensorGyroObserver = values -> {
        data.accel_x = values[0];
        data.accel_y = values[1];
        data.accel_z = values[2];
        data.accel_freq = values[3];
        isSensorGyroRefreshed = true;
        checkReadyToSend();
    };


    @Override
    public void onCreate() {
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");

        Intent i = new Intent(this, ClientUDPService.class);
        i.putExtra("ip", ip).putExtra("port", port);
        bindService(i, clientUDPServiceConnection, BIND_AUTO_CREATE);

        LinearAccelerationSensor linSensor = new LinearAccelerationSensor(this);
        linSensor.setSensorDelay(SensorManager.SENSOR_DELAY_GAME);
        sensorAcceleration = linSensor;
        sensorAcceleration.register(sensorAccelerationObserver);
        sensorAcceleration.start();
        ComplementaryGyroscopeSensor complSensor = new ComplementaryGyroscopeSensor(this);
        complSensor.setSensorDelay(SensorManager.SENSOR_DELAY_GAME);
        sensorGyro = complSensor;
        sensorGyro.register(sensorGyroObserver);
        sensorGyro.start();
    }

    @Override
    public void onDestroy() {
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");
        sensorAcceleration.unregister(sensorAccelerationObserver);
        sensorAcceleration.stop();
        sensorGyro.unregister(sensorGyroObserver);
        sensorGyro.stop();

        unbindService(clientUDPServiceConnection);
    }

    private void checkReadyToSend() {
        if (!isSensorGyroRefreshed) return;
        if (!isSensorAccelRefreshed) return;

        sendData();
    }

    private void sendData() {
        //Intent i = new Intent(this, ClientUDPService.class);
        //i.setAction(ClientUDPService.ACTION_SEND);
        //i.putExtra("data", (Serializable) data);
        if (clientUDPService == null) return;
        clientUDPService.sendData(ip, port, data);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // Заглушка
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");
        ip = intent.getStringExtra("ip");
        if (ip == null) ip = "voidproject.play.ai";
        port = intent.getIntExtra("port", 25565);
        return new Binder();
    }


    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("MINE "+this.getClass().getSimpleName(), Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName()+"");
        return super.onUnbind(intent);
    }
}