package com.ofthestreet.boussole

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private lateinit var compassView: CompassView
    private lateinit var azimuthText: TextView
    private lateinit var directionText: TextView

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compassView = findViewById(R.id.compassView)
        azimuthText = findViewById(R.id.azimuthText)
        directionText = findViewById(R.id.directionText)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onResume() {
        super.onResume()
        rotationVectorSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val azimuthRad = orientationAngles[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            val normalizedAzimuth = (azimuthDeg + 360) % 360

            compassView.setAzimuth(normalizedAzimuth)
            azimuthText.text = String.format("%.1f°", normalizedAzimuth)
            directionText.text = getCardinalDirection(normalizedAzimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getCardinalDirection(azimuth: Float): String = when {
        azimuth < 22.5f || azimuth >= 337.5f -> "Nord"
        azimuth < 67.5f -> "Nord-Est"
        azimuth < 112.5f -> "Est"
        azimuth < 157.5f -> "Sud-Est"
        azimuth < 202.5f -> "Sud"
        azimuth < 247.5f -> "Sud-Ouest"
        azimuth < 292.5f -> "Ouest"
        else -> "Nord-Ouest"
    }
}
