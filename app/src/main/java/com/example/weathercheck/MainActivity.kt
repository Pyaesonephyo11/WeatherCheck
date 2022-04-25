package com.example.weathercheck

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.net.HttpURLConnection

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQ_CODE=102

    }

    private val progressBar by lazy {
        findViewById<ProgressBar>(R.id.progress_bar)
    }
    private val tvTemperature by lazy {
        findViewById<TextView>(R.id.tvTemperature)
    }
    private val img_weather by lazy {
        findViewById<ImageView>(R.id.img_weather)
    }
    private val et_city_name by lazy {
        findViewById<EditText>(R.id.et_city_name)
    }

    private val btn_search by lazy {
        findViewById<Button>(R.id.btn_search)
    }

    private val tv_error by lazy {
        findViewById<TextView>(R.id.tv_error)
    }

    private val btn_reset by lazy {
        findViewById<Button>(R.id.btn_reset)
    }
    private val retrofit by lazy {
        RetrofitInstanceFactory.instance()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


       btn_search.setOnClickListener {
           val cityName=et_city_name.text.toString()
           executeNetworkCall(cityName)
       }

        btn_reset.setOnClickListener {
            getLocation()
        }
        getLocation()
//        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
//            !=PackageManager.PERMISSION_GRANTED){
//
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQ_CODE
//            )
//
//        }else{
//            getLocation()
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//
//        if (requestCode== REQ_CODE){
//            if (grantResults.isEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                getLocation()
//            }else if(grantResults.isEmpty()  && grantResults[0]==PackageManager.PERMISSION_DENIED){
//                Log.d("msg","Permission Denied")
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
 }

    private fun executeNetworkCall(cityName: String) {
        showLoaging()
        val openWeatherApi=retrofit.create(OpenWeatherMapApi::class.java)
        openWeatherApi.getByCityName(
            cityName=cityName,
            unit = "metric"

        ).enqueue(object :retrofit2.Callback<OpenWeatherMapResponse>{
            override fun onResponse(
                call: retrofit2.Call<OpenWeatherMapResponse>,
                response: retrofit2.Response<OpenWeatherMapResponse>
            ) {
                if (response.isSuccessful){
                    response.body()?.let { openWeatherMapResponse ->
                        Log.d("msg",openWeatherMapResponse.toString())

                        val iconUrl=openWeatherMapResponse.weatherList.getOrNull(0)?.icon ?: ""
                        val fullUrl="https://openweathermap.org/img/wn/$iconUrl@2x.png"

                        showData(
                            temperature = openWeatherMapResponse.main.temp,
                            cityName = openWeatherMapResponse.name,
                            icon =fullUrl
                        )
                    }
                }else{
                   showError()
                }

            }

            override fun onFailure(call: retrofit2.Call<OpenWeatherMapResponse>, t: Throwable) {
                showError()
            }

        })
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object:PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    Log.d("msg", "Permission Granted")
                    val locationManager =
                        this@MainActivity.getSystemService(LOCATION_SERVICE) as LocationManager
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    Log.d("msg", location?.latitude.toString())

                    executerWeathercall(
                        latitude =location?.latitude.toString(),
                        longitute = location?.longitude.toString()
                    )
                }
                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                   showError()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showError()
                }

            })
            .check()
    }

    private fun showError(){
        progressBar.visibility=View.GONE
        tvTemperature.visibility=View.GONE
        et_city_name.visibility=View.GONE
        img_weather.visibility=View.GONE
        btn_search.visibility=View.GONE

        tv_error.visibility=View.VISIBLE
        btn_reset.visibility=View.VISIBLE
    }

    private fun showLoaging() {
        progressBar.visibility=View.VISIBLE
        tvTemperature.visibility=View.GONE
        et_city_name.visibility=View.GONE
        img_weather.visibility=View.GONE
        btn_search.visibility=View.GONE

        tv_error.visibility=View.GONE
        btn_reset.visibility=View.GONE

    }

    private fun showData(
        temperature:String,
        cityName:String,
        icon:String
    ){
        progressBar.visibility=View.GONE
        tvTemperature.text="$temperature°C"
        et_city_name.setText(cityName)
        Glide.with(this).load(icon).into(img_weather)
        tvTemperature.visibility=View.VISIBLE
        et_city_name.visibility=View.VISIBLE
        img_weather.visibility=View.VISIBLE
        btn_search.visibility=View.VISIBLE

    }

    private fun executerWeathercall(latitude:String,longitute:String){
//      val httpUrl=HttpUrl.Builder()
//          .scheme("https")
//          .host("api.openweathermap.org")
//          .addPathSegment("data")
//          .addEncodedPathSegment("2.5")
//          .addPathSegment("weather")
//          .addQueryParameter("lat",latitude)
//          .addQueryParameter("lon",longitute)
//          .addQueryParameter("appid", API_KEY)
//          .build()
//
//        val client=OkHttpClient()
//        val request=Request.Builder()
//            .url(httpUrl)
//            .build()

        val openWeatherApi=retrofit.create(OpenWeatherMapApi::class.java)
        openWeatherApi.getCoordinate(
            latitude=latitude,
            longitude =longitute,
            unit = "metric"
        ).enqueue(object :retrofit2.Callback<OpenWeatherMapResponse>{
            override fun onResponse(
                call: retrofit2.Call<OpenWeatherMapResponse>,
                response: retrofit2.Response<OpenWeatherMapResponse>
            ) {
                if (response.isSuccessful){
                    response.body()?.let { openWeatherMapResponse ->
                        Log.d("msg",openWeatherMapResponse.toString())

                        val iconUrl=openWeatherMapResponse.weatherList.getOrNull(0)?.icon ?: ""
                        val fullUrl="https://openweathermap.org/img/wn/$iconUrl@2x.png"

                        showData(
                            temperature = openWeatherMapResponse.main.temp,
                            cityName = openWeatherMapResponse.name,
                            icon =fullUrl
                        )
                    }
                }else{
                    showError()
                }

            }

            override fun onFailure(call: retrofit2.Call<OpenWeatherMapResponse>, t: Throwable) {
               showError()
            }

        })

//        client.newCall(request).enqueue(object :Callback{
//            override fun onFailure(call: Call, e: IOException) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//               if (response.isSuccessful){
//                   response.body?.let { responseBody ->
//                       val jsonString=responseBody.string()
//
//                       //moshi
//                       val moshi=Moshi.Builder().build()
//                       val adapter=moshi.adapter(OpenWeatherMapResponse::class.java)
//                       val response=adapter.fromJson(jsonString)
//                       Log.d("msg", response.toString())
//                   }
//
//               }else{
//                   Log.d("msg","")
//               }
//            }
//
//        })
    }

}