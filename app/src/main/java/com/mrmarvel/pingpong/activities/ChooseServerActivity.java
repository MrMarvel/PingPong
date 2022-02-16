package com.mrmarvel.pingpong.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrmarvel.pingpong.databinding.ChooseServerMainBinding;
import com.mrmarvel.pingpong.services.ClientUDPService;

import java.util.Objects;

import retrofit2.Response;
import retrofit2.http.POST;

public class ChooseServerActivity extends AppCompatActivity {
    private ChooseServerMainBinding bind;
    private TextView ipValue;
    private TextView portValue;
    private String ip;
    private int port;
    private boolean isPaused = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");
        bind = ChooseServerMainBinding.inflate(getLayoutInflater());
        isPaused = false;


        Button gyrotestBtn = bind.gyrotestBtn;
        TextView ipValue = bind.ipInput;
        TextView portValue = bind.portInput;


        gyrotestBtn.setOnClickListener(view -> {
            Intent toDebugScreen = new Intent(ChooseServerActivity.this, DebugGyroActivity.class);
            startActivity(toDebugScreen);

        });
        Button connectBtn = bind.connectBtn;
        connectBtn.setOnClickListener(view -> {
            /*Snackbar.make(view, "CONNECT ACTION REPLACEMENT", Snackbar.LENGTH_LONG).setAction("?", view1 -> {
            }).show();*/
            //Попытка подключения асинхронно
            Intent i = new Intent(ChooseServerActivity.this, ClientUDPService.class);
            port = Integer.parseInt(String.valueOf(portValue.getHint()));
            ip = String.valueOf(ipValue.getHint());
            try {
                if (portValue.length() > 0) port = Integer.parseInt(String.valueOf(portValue.getText()));
                if (ipValue.length() > 0) ip = String.valueOf(ipValue.getText());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Неправильные входные данные", Toast.LENGTH_LONG).show();
            }
            startService(i.setAction("connect").putExtra("ip", ip).putExtra("port", port));
            //В случае успеха: (вызывется callback)
        });
        registerReceiver(receiverConnection, new IntentFilter(ClientUDPService.CONNECTED));
        setContentView(bind.getRoot());
    }

    @Override
    protected void onDestroy() {
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected BroadcastReceiver receiverConnection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result;
            try {
                result = intent.getStringExtra("result");
                Toast.makeText(ChooseServerActivity.this, result + ":" + intent.getStringExtra("reason"), Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(ChooseServerActivity.this, "Wrong format", Toast.LENGTH_LONG).show();
                return;
            }
            if (!result.equals("Есть соединение")) return;
            //Успех подключения: переходим в игру
            if (!isPaused) { // Экран выбора ещё есть?
                Intent i = new Intent(ChooseServerActivity.this, InGameActivity.class);
                i.putExtra("ip", ip);
                i.putExtra("port", port);
                startActivity(i);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");
    }
}

interface API {
    @POST("v1/sensors")
    Response sendSensors();
}

