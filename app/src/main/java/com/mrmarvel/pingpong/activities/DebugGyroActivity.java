package com.mrmarvel.pingpong.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mrmarvel.pingpong.R;
import com.kircherelectronics.fsensor.observer.SensorSubject;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.sensor.gyroscope.ComplementaryGyroscopeSensor;

import java.util.Objects;

//Активность для сравнения и дебага сенсоров
public class DebugGyroActivity extends AppCompatActivity {

    //текстовые поля для вывода информации
    private TextView xyAngle;
    private TextView xzAngle;
    private TextView zyAngle;
    private TextView modeText;
    private Button changeModeBtn;

    private FSensor fSensor;
    private DefaultSensor dSensor;
    enum Mode {
        DEFAULT, ADVANCED;
    }
    private Mode mode = Mode.DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_gyro);
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");

        xyAngle = (TextView) findViewById(R.id.xyValue);  //
        xzAngle = (TextView) findViewById(R.id.xzValue);  // Наши текстовые поля для вывода показаний
        zyAngle = (TextView) findViewById(R.id.zyValue);  //
        changeModeBtn = (Button) findViewById(R.id.changeMode);
        modeText = (TextView) findViewById(R.id.mode);

        changeModeBtn.setOnClickListener(view -> {
            Mode newMode = Mode.DEFAULT;
            if (newMode == mode) newMode = Mode.ADVANCED;
            changeMode(newMode);
        });
    }

    private void changeMode(Mode newMode) {
        if (mode == newMode) return;
        modeText.setText(newMode.toString());
        stopSensor();
        mode = newMode;
        startSensor();
    }

    private SensorSubject.SensorObserver sensorObserver = new SensorSubject.SensorObserver() {
        @Override
        public void onSensorChanged(float[] values) {

            //вывод результата
            xyAngle.setText(String.valueOf(Math.round(Math.toDegrees(values[0]))));
            xzAngle.setText(String.valueOf(Math.round(Math.toDegrees(values[1]))));
            zyAngle.setText(String.valueOf(Math.round(Math.toDegrees(values[2]))));
        }
    };

    private void stopSensor() {
        switch(mode) {
            case DEFAULT:
                dSensor.stop();
                break;
            case ADVANCED:
                fSensor.unregister(sensorObserver);
                fSensor.stop();
                break;
        }
    }

    private void startSensor() {
        switch(mode) {
            case DEFAULT:
                dSensor.start();
                break;
            case ADVANCED:
                fSensor.register(sensorObserver);
                fSensor.start();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fSensor = new ComplementaryGyroscopeSensor(this);
        dSensor = new DefaultSensor(this);
        startSensor();
    }

    @Override
    public void onPause() {
        stopSensor();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("MINE", this.getClass().getSimpleName() + " " +
                Objects.requireNonNull(new Object() {}.getClass().getEnclosingMethod()).getName()+"");
        super.onDestroy();
    }
}

class DefaultSensor implements SensorEventListener {
    private final DebugGyroActivity activity;

    private TextView xyAngle;
    private TextView xzAngle;
    private TextView zyAngle;

    private float xy_angle;
    private float xz_angle;
    private float zy_angle;

    private SensorManager sensorManager; //менеджер сенсоров

    private float[] rotationMatrix; //матрица поворота

    private float[] accelerometer;  //данные с акселерометра
    private float[] geomagnetism;   //данные геомагнитного датчика
    private float[] orientation;    //матрица положения в пространстве

    public DefaultSensor(DebugGyroActivity activity) {
        this.activity = activity;

        sensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE); //получаем объект менеджера датчиков

        rotationMatrix = new float[16];
        accelerometer = new float[3];
        geomagnetism = new float[3];
        orientation = new float[3];

        // поля для вывода показаний
        xyAngle = (TextView) activity.findViewById(R.id.xyValue);  //
        xzAngle = (TextView) activity.findViewById(R.id.xzValue);  // Наши текстовые поля для вывода показаний
        zyAngle = (TextView) activity.findViewById(R.id.zyValue);  //
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        loadSensorData(sensorEvent); // получаем данные с датчика
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, geomagnetism); //получаем матрицу поворота
        SensorManager.getOrientation(rotationMatrix, orientation); //получаем данные ориентации устройства в пространстве

        if ((xyAngle == null) || (xzAngle == null) || (zyAngle == null)) {
            xyAngle = (TextView) activity.findViewById(R.id.xyValue);
            xzAngle = (TextView) activity.findViewById(R.id.xzValue);
            zyAngle = (TextView) activity.findViewById(R.id.zyValue);
        }

        //вывод результата
        xyAngle.setText(String.valueOf(Math.round(Math.toDegrees(orientation[0]))));
        xzAngle.setText(String.valueOf(Math.round(Math.toDegrees(orientation[1]))));
        zyAngle.setText(String.valueOf(Math.round(Math.toDegrees(orientation[2]))));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void start() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI );
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI );
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    private void loadSensorData(SensorEvent event) {
        final int type = event.sensor.getType(); //определяем тип датчика
        if (type == Sensor.TYPE_ACCELEROMETER) { //если акселерометр
            accelerometer = event.values.clone();
        }

        if (type == Sensor.TYPE_MAGNETIC_FIELD) { //если геомагнитный датчик
            geomagnetism = event.values.clone();

        }
    }
}