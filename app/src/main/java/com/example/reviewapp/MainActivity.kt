package com.example.reviewapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reviewapp.adapters.RestaurantAdapter
import com.example.reviewapp.models.RestaurantModel
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.koushikdutta.ion.Ion
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val userID = mAuth.currentUser!!.uid
    private val docReference = db.document("users/$userID")
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = "AIzaSyCRm8T4L4_06qdyAZzD69XjNG0q4BFWaMA"
    private val LOCATION_PERMISSION_REQ_CODE = 1
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var recyclerView: RecyclerView
    private var restaurantList: MutableList<RestaurantModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.title = "Home"
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        displayProfileImage()
        getLastLocation() 
    }

    private fun initRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            restaurantAdapter = RestaurantAdapter()
            adapter = restaurantAdapter
        }
    }

    // Adds the data to recycler view
    private fun addData(list: MutableList<RestaurantModel>) {
        restaurantAdapter.submitList(list)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 4000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper())
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations){
                val latitude = location.latitude
                val longitude = location.longitude

                loadRestaurantsNearBy(latitude, longitude)
                getCityName(latitude, longitude)
                stopLocationUpdates()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getLastLocation() {
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE)
                return
            }
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                var location: Location? = task.result
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    Log.d("lat: ", latitude.toString())
                    Log.d("long: ", longitude.toString())

                    loadRestaurantsNearBy(latitude, longitude)
                    getCityName(latitude, longitude)
                } else { 
                    createLocationRequest()
                }
            }
        }
        else {
            val mRootView = findViewById<View>(R.id.snackbar_text)
            val locSnack = Snackbar.make(mRootView, R.string.location_switch, Snackbar.LENGTH_LONG)
            locSnack.show()
        }
    }

    private fun loadRestaurantsNearBy(latitude: Double, longitude: Double) {
        val keyword = "food"
        val proximityRadius = 2000
        val type = "restaurant"

        val googlePlacesUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlacesUrl.append("?keyword=$keyword")
        googlePlacesUrl.append("&location=$latitude%2C$longitude")
        googlePlacesUrl.append("&radius=$proximityRadius")
        googlePlacesUrl.append("&type=$type")
        googlePlacesUrl.append("&key=$apiKey")

        val url = googlePlacesUrl.toString()
        Log.d("json url: ", url)

        Ion.with(this)
            .load(url)
            .asString()
            .setCallback{ex, result ->
                processNameAndImg(result)
            }
    }

    private fun getCityName(lat: Double,long: Double){
        val cityName: String
        val countryName: String
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,long,3)
        Log.d("address", address.toString())

        cityName = address[0].locality
        countryName = address[0].countryName
        val displayLocation = findViewById<TextView>(R.id.display_user_location)
        displayLocation.text = "$cityName, $countryName"
    }

    private fun processNameAndImg(result: String?) {
        val myJson = JSONObject(result)
        val getStatus = myJson.getString("status")
        val resultsArr = myJson.getJSONArray("results")

        if (getStatus.equals("ZERO_RESULTS")) {
            val mRootView = findViewById<View>(R.id.snackbar_text)
            Snackbar.make(mRootView, R.string.no_results, Snackbar.LENGTH_LONG).show()
            Log.d("tag", "could not find results at your location")
        } else { 
            try {
                for (i in 0 until resultsArr.length()) {
                    val obj = resultsArr.getJSONObject(i)

                    if (obj.has("name") && obj.has("photos")) {
                        val getName = obj.getString("name")
                        val photosArray = obj.getJSONArray("photos")
                        val photoRef = photosArray.getJSONObject(0).getString("photo_reference")
                        val imgUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$photoRef&key=$apiKey"

                        val listObj = RestaurantModel(getName,imgUrl)
                        restaurantList.add(listObj)
                        initRecyclerView()
                        addData(restaurantList)
                    }
                }
            } catch(e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun displayProfileImage() {
        val profileMenu = findViewById<CircleImageView>(R.id.display_user)

        docReference.get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("profilePhoto")) {
                    Glide.with(this).load(document.getString("profilePhoto")).into(profileMenu)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_bar, menu)
        val item = menu.findItem(R.id.search_action)
        val searchView = item.actionView as SearchView
        val tempArrayList: MutableList<RestaurantModel> = ArrayList()

        searchView.onActionViewExpanded()
        searchView.queryHint = "search for restaurants"

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                tempArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())

                if (searchText.isNotEmpty()) {
                    restaurantList.forEach {
                        if (it.name.lowercase().contains(searchText)) {
                            tempArrayList.add(it)
                            }
                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                } else {
                    tempArrayList.clear()
                    tempArrayList.addAll(restaurantList)
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
                addData(tempArrayList)
                return false
            }
        })
        return true
    }

    fun openProfileActivity(view: View) {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    fun refreshActivity(view: View) {
        this.recreate()
    }
}