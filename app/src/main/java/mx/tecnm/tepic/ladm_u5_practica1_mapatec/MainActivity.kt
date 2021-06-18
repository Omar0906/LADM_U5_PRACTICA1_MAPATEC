package mx.tecnm.tepic.ladm_u5_practica1_mapatec

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    var db = FirebaseFirestore.getInstance()
    var lugares = ArrayList<Data>()
    var listaid = ArrayList<String>()
    lateinit var locacion: LocationManager
    lateinit var mapa: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        db.collection("lugarestecnologico").addSnapshotListener { value, error ->
            if (error != null) {
                posicionactual.setText("${error.message}")
                return@addSnapshotListener
            }
            lugares.clear()
            listaid.clear()
            for (document in value!!) {
                var data: Data
                var nombre = document.getString("nombre")!!
                var pos1: GeoPoint = document.getGeoPoint("posicion1")!!
                var pos2: GeoPoint = document.getGeoPoint("posicion2")!!
                var internos = ArrayList<String>()
                try {
                    internos = document.get("internos")!! as ArrayList<String>
                } catch (evt: Exception) {

                }
                if (!internos.isEmpty()) {
                    data = Data(nombre, pos1, pos2, internos)
                } else {
                    data = Data(nombre, pos1, pos2)
                }
                lugares.add(data)
                listaid.add(document.id)
            }
            listalugares.adapter =
                ArrayAdapter<Data>(this, android.R.layout.simple_list_item_1, lugares)
            listalugares.setOnItemClickListener { parent, view, position, id ->
                var data = lugares[position]
                var punto = data.puntoMedio()
                mapa.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            punto.latitude,
                            punto.longitude
                        )
                    )
                )
                addMarcador(data.nombre, punto)
            }
        }
        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1f, oyente)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    private fun addMarcador(texto: String, punto: GeoPoint) {
        mapa.clear()
        mapa.addMarker(
            MarkerOptions().position(LatLng(punto.latitude, punto.longitude)).title(texto)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        )
    }

    fun mostrarUbicacion(punto: GeoPoint, especial: Boolean) {
        posicionactual.setText("Ubicación actual: ${punto.latitude},${punto.longitude}")
    }

    override fun onMapReady(p0: GoogleMap) {
        mapa = p0
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        mapa.isMyLocationEnabled = true
        mapa.uiSettings.isZoomControlsEnabled = true
        mapa.uiSettings.isCompassEnabled = true
        mapa.mapType = GoogleMap.MAP_TYPE_HYBRID
        fusedLocationClient.lastLocation.addOnSuccessListener {
            var geoPosicion = GeoPoint(it.latitude, it.longitude)
            mapa.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        geoPosicion.latitude,
                        geoPosicion.longitude
                    ), 20f
                )
            )
        }
        mapa.setOnMapClickListener {
            var punto = GeoPoint(it.latitude, it.longitude)
            for (item in lugares) {
                if (item.estoyEn(punto)) {
                    addMarcador(item.nombre, punto)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}

class Oyente(puntero: MainActivity) : LocationListener {
    var p = puntero
    override fun onLocationChanged(location: Location) {
        p.mostrarUbicacion(GeoPoint(location.latitude, location.longitude), false)
        var geoPosicionGPS = GeoPoint(location.latitude, location.longitude)
        for (item in p.lugares) {
            if (item.estoyEn(geoPosicionGPS)) {
                p.lugaractual.setText("\nEstas en: ${item.nombre}")
                if(!item.interno.isEmpty()){
                    var cadena = ""
                    for (nombre in item.interno) {
                        cadena += nombre + "\n"
                    }
                    p.lugaractual.append("\nAquí hay: \n" + cadena)
                }
                return
            }
        }
        p.lugaractual.setText("")
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}