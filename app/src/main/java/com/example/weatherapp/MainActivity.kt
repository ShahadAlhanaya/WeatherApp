package com.example.weatherapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val activationKey = "0d027b3b0aaf99e6a36d20a4c4a429b8"

    lateinit var cityTextView: TextView
    lateinit var lastUpdatedTextView: TextView
    lateinit var weatherTitleTextView: TextView
    lateinit var degreeTextView: TextView
    lateinit var lowTextView: TextView
    lateinit var highTextView: TextView
    lateinit var sunriseTextView: TextView
    lateinit var sunsetTextView: TextView
    lateinit var humidityTextView: TextView
    lateinit var pressureTextView: TextView
    lateinit var windTextView: TextView
    lateinit var refreshLayout: LinearLayout

    var city: String = ""
    var country: String = ""
    var lastUpdate: Long = 0
    var weatherTitle: String = ""
    var degrees: Long = 0
    var low: Long = 0
    var high: Long = 0
    var sunrise: Long = 0
    var sunset: Long = 0
    var humidity: String = ""
    var pressure: String = ""
    var wind: String = ""


    var currentZipCode = "94040"
    var celcius = true
    var validZipCode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hide the action bar
        supportActionBar?.hide()

        //initialize views
        cityTextView = findViewById(R.id.tv_city)
        lastUpdatedTextView = findViewById(R.id.tv_date)
        weatherTitleTextView = findViewById(R.id.tv_weatherTitle)
        degreeTextView = findViewById(R.id.tv_weatherDegree)
        lowTextView = findViewById(R.id.tv_low)
        highTextView = findViewById(R.id.tv_high)
        sunriseTextView = findViewById(R.id.tv_sunriseTime)
        sunsetTextView = findViewById(R.id.tv_sunsetTime)
        humidityTextView = findViewById(R.id.tv_humidityStrength)
        pressureTextView = findViewById(R.id.tv_pressureStrength)
        windTextView = findViewById(R.id.tv_windStrength)
        refreshLayout = findViewById(R.id.ll_refresh)

        //refresh click listener
        refreshLayout.setOnClickListener {
            requestData(currentZipCode)
        }

        //choose city click listener
        cityTextView.setOnClickListener {
            showBottomSheetDialog(this)
        }

        //change unit
        degreeTextView.setOnClickListener {
            celcius = !celcius
            updateDegrees(celcius)
        }

        // requestData(currentZipCode)

    }

    private fun showBottomSheetDialog(mainActivity: MainActivity) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.DialogStyle)
        bottomSheetDialog.setContentView(R.layout.choose_city_bottom_sheet)
        val submitButton = bottomSheetDialog.findViewById<Button>(R.id.btn_submitZipCode)
        val zipCodeEditText = bottomSheetDialog.findViewById<EditText>(R.id.edt_zipCode)

        submitButton!!.setOnClickListener {
            val zipCode = zipCodeEditText!!.text.toString()
            if (zipCode.length > 16 || zipCode.length < 3) {
                Toast.makeText(this, "please enter a valid zip code", Toast.LENGTH_SHORT).show()
            } else {
                val zipcode = zipCodeEditText.text.toString()
                requestData(zipcode)
                if(!validZipCode){
                    Toast.makeText(this,"please enter a valid zip code", Toast.LENGTH_SHORT).show()
                }else{
                    currentZipCode = zipcode
                    bottomSheetDialog.dismiss()
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun requestData(zipCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.openweathermap.org/data/2.5/weather?q=$zipCode&appid=$activationKey")
                    .build()
                val response =
                    withContext(Dispatchers.Default) {
                        okHttpClient.newCall(request).execute()
                    }
                if (response != null) {
                    if (response.code == 200) {
                        validZipCode = true
                        val jsonObject = JSONObject(response.body!!.string())
                        city = jsonObject.getString("name")
                        country = jsonObject.getJSONObject("sys").getString("country")
                        weatherTitle = jsonObject.getJSONArray("weather").getJSONObject(0)
                            .getString("description")
                        degrees = jsonObject.getJSONObject("main").getLong("temp")
                        low = jsonObject.getJSONObject("main").getLong("temp_min")
                        high = jsonObject.getJSONObject("main").getLong("temp_max")
                        pressure = jsonObject.getJSONObject("main").getString("pressure")
                        humidity = jsonObject.getJSONObject("main").getString("humidity")
                        wind = jsonObject.getJSONObject("wind").getString("speed")
                        sunset = jsonObject.getJSONObject("sys").getLong("sunset")
                        sunrise = jsonObject.getJSONObject("sys").getLong("sunrise")
                        lastUpdate = jsonObject.getLong("dt")
                        updateUI()

                    }
                    if (response.code == 404) {
                        validZipCode = false
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private suspend fun updateUI() {
        withContext(Main) {
            updateDegrees(celcius)

            cityTextView.text = "$city, $country"
            lastUpdatedTextView.text =
                "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(lastUpdate * 1000)
                )
            weatherTitleTextView.text = weatherTitle
            sunriseTextView.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                Date(sunrise * 1000)
            )
            sunsetTextView.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                Date(sunset * 1000)
            )
            windTextView.text = wind
            humidityTextView.text = humidity
            pressureTextView.text = pressure
        }

    }

    private fun updateDegrees(isCelsius: Boolean) {
        if (isCelsius) {
            var lowC = low - 273
            var highC = high - 273
            var degreesC = degrees - 273
            lowTextView.text = "Low: ${lowC}°C"
            highTextView.text = "High: ${highC}°C"
            degreeTextView.text = "${degreesC}°C"
        } else {
            var lowF = 1.8 * (low - 273) + 32
            var highF = 1.8 * (high - 273) + 32
            var degreesF = 1.8 * (degrees - 273) + 32
            lowTextView.text = "Low: ${lowF.toInt()}°F"
            highTextView.text = "High: ${highF.toInt()}°F"
            degreeTextView.text = "${degreesF.toInt()}°F"
        }
    }
}

