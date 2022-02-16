package com.mrmarvel.pingpong;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import lombok.Data;

@Data
public class SensorData implements Serializable {
    public @Expose
    float gyro_azimuth = 0;
    public @Expose
    float gyro_pitch = 0;
    public @Expose
    float gyro_roll = 0;
    public @Expose
    float gyro_freq = 0;

    public @Expose
    float accel_x = 0;
    public @Expose
    float accel_y = 0;
    public @Expose
    float accel_z = 0;
    public @Expose
    float accel_freq = 0;

    public @Expose
    long timestamp = 0;

    public @Expose
    int calibrate = 0;

    public void divGyro (float n) {
        gyro_azimuth /= n;
        gyro_pitch /= n;
        gyro_roll /= n;
        gyro_freq /= n;
    }
    public void divAccel (float n) {
        accel_x /= n;
        accel_y /= n;
        accel_z /= n;
        accel_freq /= n;
    }
}
