package com.mrmarvel.pingpong.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.mrmarvel.pingpong.SensorData;
import com.mrmarvel.pingpong.databinding.ActivityInGameBinding;
import com.mrmarvel.pingpong.services.ClientUDPService;
import com.mrmarvel.pingpong.services.InfiniteSensorService;

import java.util.Objects;

public class InGameActivity extends AppCompatActivity {
    private ActivityInGameBinding bind;
    private String ip;
    private int port;
    private ClientUDPService clientUDPService;


    ServiceConnection sensorService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("MINE InGameActivity", InfiniteSensorService.class.getSimpleName()+" is connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("MINE InGameActivity", InfiniteSensorService.class.getSimpleName()+" is disconnected");
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityInGameBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");
        Intent i = getIntent();
        ip = i.getStringExtra("ip");
        if (ip == null) ip = "voidproject.play.ai";
        port = i.getIntExtra("port", 25565);
        bind.calibrateButton.setOnClickListener(view -> {
            SensorData data = new SensorData();
            data.calibrate = 1;
            clientUDPService.sendData(data);
        });

        i = new Intent(InGameActivity.this, InfiniteSensorService.class);
        i.putExtra("ip", ip).putExtra("port", port);
        bindService(i, sensorService, BIND_AUTO_CREATE | BIND_IMPORTANT);
        i = new Intent(this, ClientUDPService.class);
        bindService(i, clientUDPServiceConnection, BIND_ADJUST_WITH_ACTIVITY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MINE "+this.getClass().getSimpleName(), this.getClass().getSimpleName()+" destroyed");
        unbindService(sensorService);
    }

}