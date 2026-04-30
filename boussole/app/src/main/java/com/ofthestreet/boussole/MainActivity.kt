package com.ofthestreet.boussole

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private lateinit var compassView: CompassView
    private lateinit var azimuthText: TextView
    private lateinit var directionText: TextView
    private lateinit var attributionText: TextView
    private lateinit var rootLayout: LinearLayout
    private lateinit var modeSwitch: SwitchCompat

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        compassView = findViewById(R.id.compassView)
        azimuthText = findViewById(R.id.azimuthText)
        directionText = findViewById(R.id.directionText)
        attributionText = findViewById(R.id.attributionText)
        modeSwitch = findViewById(R.id.modeSwitch)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            applyTheme(lightMode = isChecked)
        }
    }

    private fun applyTheme(lightMode: Boolean) {
        if (lightMode) {
            rootLayout.setBackgroundColor(Color.parseColor("#F0F0F5"))
            azimuthText.setTextColor(Color.parseColor("#1A1A2E"))
            directionText.setTextColor(Color.parseColor("#CC2222"))
            attributionText.setTextColor(Color.parseColor("#777788"))
            modeSwitch.setTextColor(Color.parseColor("#1A1A2E"))
        } else {
            rootLayout.setBackgroundColor(Color.parseColor("#1A1A2E"))
            azimuthText.setTextColor(Color.WHITE)
            directionText.setTextColor(Color.parseColor("#FF4444"))
            attributionText.setTextColor(Color.parseColor("#9090A0"))
            modeSwitch.setTextColor(Color.WHITE)
        }
        compassView.setDarkMode(!lightMode)
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
            azimuthText.text = "${normalizedAzimuth.toInt()}°"
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
