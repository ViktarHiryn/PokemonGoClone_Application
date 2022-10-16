package com.example.pokemongoclone

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.pokemongoclone.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Creating a constant to hold the permission code
    private val USER_LOCATION_REQUEST_CODE = 1000

    // Creating a variable to store the users location
    private var playerLocation : Location? = null
    //Creating a variable to store the users old locations
    private var oldLocationOfPlayer : Location? = null

    // Creating a variable for a location Manager and location Listener
    private var locationManager : LocationManager? = null
    private var locationListener : PlayerLocationListener? = null

    // Creating an array list for the pokemon characters
    private var pokemonCharacters : ArrayList<PokemonCharacter> = ArrayList()

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initializing the location Manager and location Listener
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = PlayerLocationListener()
        // Requesting location permission
        requestLocationPermission()

        // Creating pokemon characters on the map
        initializePokemonCharacters()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

        //Montana Coordinates
        // 46.877687 109.369015

        val pLocation = LatLng(playerLocation!!.latitude, playerLocation!!.longitude)
        mMap.addMarker(MarkerOptions().position(pLocation).title("Hi I am the Player")
            .snippet("Let's Go")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pokeball)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pLocation))
    }

    // Asking permission
    private fun requestLocationPermission () {
        if(Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                ,USER_LOCATION_REQUEST_CODE)
                return
            }
        }
        accessUserLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == USER_LOCATION_REQUEST_CODE) {
            // If permission granted request location updates
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessUserLocation()
            }
        }
    }

    // Creating an inner class for the location Listener
    inner class PlayerLocationListener : LocationListener {
        constructor() {
            playerLocation = Location("MyProvider")
            playerLocation?.latitude = 0.0
            playerLocation?.longitude = 0.0
        }

        // Overriding the functions on Location Changed
        override fun onLocationChanged(updatedLocation: Location) {
            playerLocation = updatedLocation
        }
        /*
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            super.onStatusChanged(provider, status, extras)
        }
        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
        }
        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
        }*/
    }

    // Creating a function/method for initializing the pokemon instances
    private fun initializePokemonCharacters () {
        pokemonCharacters.add(
            PokemonCharacter("Hello this is Charzard",
        "I am powerful", R.drawable.charzard, 1.651729,
            31.996134)
        )
        pokemonCharacters.add(PokemonCharacter("Hello this is Dino",
            "I am powerful", R.drawable.dino, 27.404523,
            29.647654))
        pokemonCharacters.add(PokemonCharacter("Hello this is Pika",
            "I am powerful", R.drawable.pika, 10.492703,
            10.709112))
        pokemonCharacters.add(PokemonCharacter("Hello this is Penguin",
            "I am powerful", R.drawable.penguin, 28.220750,
            1.898764))
    }

    // Creating a function to get the users location
    private fun accessUserLocation () {
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
            1, 2f, locationListener!!)

        var newThread = NewThread ()
        newThread.start()
    }

    // Creating a class to have multiple thread run concurently
    inner class NewThread : Thread {
        constructor() : super() {
            oldLocationOfPlayer = Location("MyProvider")
            oldLocationOfPlayer?.latitude = 0.0
            oldLocationOfPlayer?.longitude = 0.0
        }
        override fun run() {
            super.run()
            while (true) {
                // Making sure the thread will not exsecute if the user did not move
                if (oldLocationOfPlayer?.distanceTo(playerLocation) == 0F) {
                    continue
                }

                // Updating the old locations of the player
                oldLocationOfPlayer = playerLocation

                // Making sure the thread will be exsecuted if the user moves
                try {
                    runOnUiThread {
                        // Clearing the Map
                        mMap.clear()

                        val pLocation =
                            LatLng(playerLocation!!.latitude, playerLocation!!.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(pLocation).title("Hi I am the Player")
                                .snippet("Let's Go")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pokeball))
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pLocation))

                        // Showing the pokemon characters
                        for (pokemonCharacterIndex in 0.until(pokemonCharacters.size)) {
                            var pc = pokemonCharacters[pokemonCharacterIndex]
                            if (pc.isDefeated == false) {
                                var pcLocation = LatLng(pc.location!!.latitude, pc.location!!.longitude)
                                mMap.addMarker(MarkerOptions()
                                    .position(pcLocation)
                                    .title(pc.titleOfPokemon)
                                    .snippet(pc.message)
                                    .icon(BitmapDescriptorFactory.fromResource(pc.iconOfPokemon!!)))

                                // Checking if the pokemon was caught or no
                                if (playerLocation!!.distanceTo(pc.location) < 1) {
                                    Toast.makeText(this@MapsActivity,
                                        "${pc.titleOfPokemon} is eliminated",
                                        Toast.LENGTH_SHORT).show()
                                    pc.isDefeated = true
                                    pokemonCharacters[pokemonCharacterIndex] = pc
                                }
                            }
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }
}