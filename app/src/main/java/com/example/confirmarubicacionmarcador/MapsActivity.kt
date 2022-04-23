package com.example.confirmarubicacionmarcador

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.confirmarubicacionmarcador.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,  GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap
    lateinit var marcador: Marker
    private lateinit var binding: ActivityMapsBinding
    lateinit var context: Context
    private var locationManager: LocationManager? = null
    var latitud: Double = 0.0
    var longitud: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context= this
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getLocalizacion()

        btnConfirmar.setOnClickListener {
            setLocation(latitud, longitud)
        }
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
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
       // mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        val locationManager = this@MapsActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val miUbicacion = LatLng(location.getLatitude(), location.getLongitude())
                latitud = location.latitude
                longitud = location.longitude
                locationManager.removeUpdates(this)

               marcador = googleMap.addMarker(MarkerOptions().position(miUbicacion).draggable(true).title("Mi Ubicacion").snippet("holaaa").icon(BitmapDescriptorFactory.fromResource(
                   R.drawable.miubicacion4
               )))!!

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 20F))

            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                when (status) {
                    LocationProvider.AVAILABLE -> Log.d("debug", "LocationProvider.AVAILABLE")
                    LocationProvider.OUT_OF_SERVICE -> Log.d("debug", "LocationProvider.OUT_OF_SERVICE")
                    LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE")
                }
            }
            override fun onProviderEnabled(provider: String) {
             //   Toast.makeText(context, "GPS activado", Toast.LENGTH_SHORT).show()

            }
            override fun onProviderDisabled(provider: String) {
             //  Toast.makeText(context, "GPS Desactivado", Toast.LENGTH_SHORT).show()
            }
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)

        //los marcadores
       // Utils_k.Marcador(mMap, applicationContext, telefono)
      //  mMap!!.setOnMapLongClickListener(this)
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMarkerDragListener(this)
    }
    private fun getLocalizacion() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        //En caso de que el Gps este desactivado, entrara en el if y nos mandara a la configuracion de nuestro Gps para activarlo
        //En caso de que el Gps este desactivado, entrara en el if y nos mandara a la configuracion de nuestro Gps para activarlo

        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)

            return
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
       if (p0!!.equals(marcador!!)){
           System.out.println("entro")
             latitud = p0.position.latitude
             longitud = p0.position.longitude
          //  Toast.makeText(context, "latitud ${latitud.toString()} longitud $longitud", Toast.LENGTH_LONG).show()
       }
       return false
    }


    override fun onMarkerDragEnd(p0: Marker) {
        if (p0.equals(marcador)) {
           // Toast.makeText(context, "finish", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMarkerDragStart(p0: Marker) {
        if (p0.equals(marcador)) {
            //Toast.makeText(context, "start", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onMarkerDrag(p0: Marker) {
        if (p0!!.equals(marcador!!)){
            val nuevoTitulo  = String.format(Locale.getDefault(),getString(R.string.marker_detail),
                marcador.position.latitude,
                marcador.position.longitude)

            setTitle(nuevoTitulo)
             latitud = p0.position.latitude
             longitud = p0.position.longitude
           // setLocation(latitud, longitud)
        }
    }


    fun setLocation(latitud: Double, longitud: Double) {
        if (latitud !== 0.0 && longitud !== 0.0) {
            try {
                val geocoder: Geocoder
                val direccion: List<Address>
                geocoder = Geocoder(this, Locale.getDefault())

                direccion = geocoder.getFromLocation(latitud, longitud, 1) // 1 representa la cantidad de resultados a obtener
                val address = direccion[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                val ciudad = direccion[0].locality // ciudad
                val estado = direccion[0].adminArea //estado
                val pais = direccion[0].countryName // pais
                val codigoPostal = direccion[0].postalCode //codigo Postal
                val calle = direccion[0].thoroughfare // la calle
                val colonia =  direccion[0].subLocality// colonia
                val numExterior = direccion[0].subThoroughfare

               /* txtDireccion.setText(address)
                txtCiudad.setText(ciudad)
                txtEstado.setText(estado)
                txtPais.setText(pais)
                txtCalle.setText(calle)
                txtColonia.setText(colonia)
                txtNoExterior.setText(numExterior)
                txtCodigoPostal.setText(codigoPostal)*/
                texViewDireecion.setText("ciudad $ciudad \n Estado  $estado \n pais $pais \n codigo Postal $codigoPostal \n calle $calle \n colonia $colonia")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }



    }

}