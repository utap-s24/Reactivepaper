package com.example.reactivepaper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build.VERSION_CODES.Q


class GyroSensor(context: Context): SensorEventListener {
    private var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    private var rotationValues: FloatArray = FloatArray(3)

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        this.onResume()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            rotationValues = event.values
        }
    }

    fun getRotationValues(): FloatArray {
        return rotationValues
    }

    fun onResume() {
        rotationSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun onPause() {
        sensorManager.unregisterListener(this)
    }
}