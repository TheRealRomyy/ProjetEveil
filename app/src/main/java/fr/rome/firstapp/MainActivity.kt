package fr.rome.firstapp

import android.content.Context
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import fr.rome.firstapp.listener.StepListener
import fr.rome.firstapp.utils.StepDetector
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener, StepListener {
    // Create variable for podometer
    private var simpleStepDetector: StepDetector? = null
    private var sensorManager: SensorManager? = null
    private val TEXT_NUM_STEPS = "Number of Steps: "
    private var numSteps: Int = 0

    // Create variable for scheduler
    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay = 2500

    // Podometer events

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector!!.updateAccelerometer(event.timestamp, event.values[0], event.values[1], event.values[2])
        }
    }

    override fun step(timeNs: Long) {
        numSteps++
        val res: Resources = resources
        val date = Calendar.getInstance()
        val coEco = 0.12 * numSteps.toDouble()
        val decimalRound = BigDecimal(coEco).setScale(2, RoundingMode.HALF_EVEN)
        val invisible_text = findViewById<TextView>(R.id.invisible_text)

        // Marquer proprement la date
        val dateString = res.getString(R.string.date, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));

        // Formater le string
        val strFormated = res.getString(R.string.co2_text, dateString, decimalRound.toString());

        invisible_text.text = strFormated
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get an instance of the SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        simpleStepDetector = StepDetector()
        simpleStepDetector!!.registerListener(this)

        // Get le boutton go
        val go_boutton = findViewById<Button>(R.id.go_button)
        // Mettre un listenner dessus
        go_boutton.setOnClickListener {
            // Get et set toutes les variables
            val invisible_text = findViewById<TextView>(R.id.invisible_text)
            var isAlreadyEnabled = false
            val res: Resources = resources
            var textToSay = "⌛ Compteur de pas activé"
            val date = Calendar.getInstance()
            val co2eco = 0

            // Marquer proprement la date
            val dateString = res.getString(R.string.date, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));

            // Formater le string
            val strFormated = res.getString(R.string.co2_text, dateString, co2eco);
            invisible_text.text = strFormated

            // Vérifier si le podométre est déjà activé ou pas
            if(invisible_text.isVisible) isAlreadyEnabled = true
            if(isAlreadyEnabled) {
                textToSay = "✅ Compteur de pas désactivé"
                sensorManager!!.unregisterListener(this)
            } else {
                numSteps = 0
                sensorManager!!.registerListener(this, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)
            }

            // Envoyer l'information d'activation / désactivation
            Toast.makeText(this@MainActivity, textToSay, Toast.LENGTH_SHORT).show()
            invisible_text.isVisible = !isAlreadyEnabled
        }
    }

    fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (Math.random() * (end - start + 1)).toInt() + start
    }

    override fun onResume() {
        // Get l'élément text_tips
        val res: Resources = resources
        val list = res.getStringArray(R.array.eco_ips);
        val tipText: TextView = findViewById(R.id.text_tips) as TextView

        // Toutes les 2,5s
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            val number: Int = rand(0, list.size-1)
            tipText.setText(list[number])
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }
}