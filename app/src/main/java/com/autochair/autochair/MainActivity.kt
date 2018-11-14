package com.autochair.autochair

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.view.View
import android.widget.*
import android.nfc.NfcAdapter
import android.content.Intent
import android.nfc.Tag


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mTemperature: Sensor? = null

    private val defaultTemperature = 70
    private val maxTempToUse = 95
    private val minTempToUse = 40
    private var databaseTemp = defaultTemperature
    private var tempToUse = defaultTemperature

    private var ambientTemp = defaultTemperature.toDouble()


    private val defaultTagID = "No Tag"
    private var currentTagID = defaultTagID

    private var displayDebugInfo = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        findViewById<ImageButton>(R.id.temp_lower).setOnClickListener{lowerTemperature()}
        findViewById<ImageButton>(R.id.temp_raise).setOnClickListener{raiseTemperature()}

        val tagSpinner = findViewById<Spinner>(R.id.tag_spinner)
        val adapter = ArrayAdapter.createFromResource(this, R.array.tags, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tagSpinner.adapter = adapter

        tagSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sendFakeTagIntent(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        downloadChosenTemperature()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            if (intent.getStringExtra(NfcAdapter.EXTRA_ID) != null) {
                scanTag(intent.getStringExtra(NfcAdapter.EXTRA_ID))
            } else {
                val tagIdByteArray = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG).id
                var tagId = ""
                for (b in tagIdByteArray) {
                    tagId += String.format("%02X", b)
                }
                scanTag(tagId)
            }
        }
    }

    private fun updateScreen() {
        val tempToUseField = findViewById<TextView>(R.id.temp_to_use)
        tempToUseField.text = "%d°F".format(tempToUse)
        when {
            tempToUse <= 55 -> tempToUseField.setTextColor(Color.BLUE)
            tempToUse <= 70 -> tempToUseField.setTextColor(Color.GREEN)
            tempToUse <= 85 -> tempToUseField.setTextColor(Color.YELLOW)
            else -> tempToUseField.setTextColor(Color.RED)
        }
        findViewById<TextView>(R.id.ambient_temp).text = getString(R.string.ambient_temp).format(ambientTemp)
        findViewById<TextView>(R.id.database_temp).text = getString(R.string.database_temp).format(databaseTemp)
        findViewById<TextView>(R.id.tag_name).text = getString(R.string.set_tag).format(currentTagID)
    }

    private fun lowerTemperature() {
        if (tempToUse > minTempToUse) {
            tempToUse--
            updateScreen()
            uploadChosenTemperature()
        }
    }

    private fun raiseTemperature() {
        if (tempToUse < maxTempToUse) {
            tempToUse++
            updateScreen()
            uploadChosenTemperature()
        }
    }

    private fun chooseTemperature() {
        var adjustedTemperature = databaseTemp
        if (mTemperature != null) {
            adjustedTemperature += ((defaultTemperature - ambientTemp)/5).toInt()
        }
        tempToUse = minOf(maxTempToUse, maxOf(minTempToUse, adjustedTemperature))
        updateScreen()
    }

    private fun scanTag(tagId: String) {
        currentTagID = tagId
        downloadChosenTemperature()
    }

    private fun sendFakeTagIntent(tag: Int) {
        if (tag != 0) {
            val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED)
            intent.type = "text/plain"
            intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, "Fake Tag")
            intent.putExtra(NfcAdapter.EXTRA_ID, tag.toString())
            startActivity(intent)
        } else {
            scanTag(defaultTagID)
        }
    }

    private fun uploadChosenTemperature() {
        if (currentTagID != defaultTagID) {
            val newData: Map<String, Any> = mapOf("chosen_temperature" to tempToUse)
            FirebaseFirestore.getInstance().collection("tags").document(currentTagID).set(newData)
            databaseTemp = tempToUse
            updateScreen()
        }
    }

    private fun downloadChosenTemperature() {
        if (currentTagID != defaultTagID) {
            val docRef = FirebaseFirestore.getInstance().collection("tags").document(currentTagID)
            docRef.get().addOnCompleteListener{
                if (it.isSuccessful) {
                    val document = it.result
                    databaseTemp = if (document != null && document.exists()) {
                        (document.data!!["chosen_temperature"] as Long).toInt()
                    } else {
                        defaultTemperature
                    }
                    chooseTemperature()
                } else {
                    Log.e("MainActivity", "get failed with ", it.exception)
                }
            }
        } else {
            databaseTemp = defaultTemperature
            chooseTemperature()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        ambientTemp = (event.values[0] * 1.8) + 32
        if (displayDebugInfo) {
            updateScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mTemperature != null) {
            mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

}
