package com.mrmarvel.pingpong.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.mrmarvel.pingpong.databinding.ActivityInGameBinding;
import com.mrmarvel.pingpong.services.InfiniteSensorService;

import java.util.Objects;

public class InGameActivity extends AppCompatActivity {
    private ActivityInGameBinding bind;
    private String ip;
    private int port;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");
        bind = ActivityInGameBinding.inflate(getLayoutInflater());
        Intent i = getIntent();
        ip = i.getStringExtra("ip");
        if (ip == null) ip = "voidproject.play.ai";
        port = i.getIntExtra("port", 25565);

        setContentView(bind.getRoot());

        i = new Intent(InGameActivity.this, InfiniteSensorService.class);
        i.putExtra("ip", ip).putExtra("port", port);
        bindService(i, sensorService, BIND_AUTO_CREATE | BIND_IMPORTANT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MINE "+this.getClass().getSimpleName(), this.getClass().getSimpleName()+" destroyed");
        unbindService(sensorService);
    }

}