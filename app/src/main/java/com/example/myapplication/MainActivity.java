package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import WebService.Asynchtask;
import WebService.WebService;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        Asynchtask {

    ArrayList<LatLng> marcadores=new ArrayList(6);
    GoogleMap Mapi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Mapi=googleMap;

        Mapi.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        Mapi.getUiSettings().setZoomControlsEnabled(true);

       CameraUpdate camUpd1 =
                CameraUpdateFactory
                        .newLatLngZoom(new LatLng(40.68911920777306, -74.04458623399685), 18);
        Mapi.moveCamera(camUpd1);

        LatLng madrid = new LatLng(40.68911920777306, -74.04458623399685);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(19)
                .bearing(3) //noreste arriba
                .tilt(20) //punto de vista de la cámara 70 grados
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);
        Mapi.animateCamera(camUpd3);
        Mapi.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        LatLng punto = new LatLng(latLng.latitude,
                latLng.longitude);
        MarkerOptions marcador=new MarkerOptions();

        marcadores.add(punto);
        Log.i("Marcador",marcadores.toString());

        //dar los datos

        marcador.position(latLng);
        marcador.title("Punto");
        Mapi.addMarker(marcador);
        //creo el array

        PolylineOptions lineas=new PolylineOptions();
        if (marcadores.size()==6){
            for (int i=0; i<marcadores.size();i++){
                lineas.add(marcadores.get(i));
            }
            lineas.add(marcadores.get(0));
        }
        lineas.width(8);
        lineas.color(Color.RED);
        Mapi.addPolyline(lineas);

        //MODIFICA ESTo para hacer esta vaina en la casa
        Map<String, String> datos = new HashMap<String, String>();

        datos.put("destinations","40.689474437854315,-74.044947065413");
        datos.put("origins","40.68916326633521,-74.0447847917676");
        datos.put("units","meters");
        WebService ws= new
                WebService("https://maps.googleapis.com/maps/api/distancematrix/json",
                datos, this, MainActivity.this);
        ws.execute("POST");
    }

    @Override
    public void processFinish(String result) throws JSONException {
        Log.i("RESULT",result);
    }
}