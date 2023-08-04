package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {
    private GoogleMap Mapeo;
    private PolylineOptions Lineas;
    private ArrayList<MarkerOptions> Marcadores;
    private Double TotDist = 0.00;
    private TextView DatoDist;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DatoDist = findViewById(R.id.txtDist);

        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Mapeo = googleMap;
        Mapeo.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        Mapeo.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate camUpd1 = CameraUpdateFactory.newLatLngZoom(new LatLng(40.6898, -74.0448), 18);
        Mapeo.moveCamera(camUpd1);
        Marcadores = new ArrayList<>();
        Mapeo.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        LatLng punto = new LatLng(latLng.latitude, latLng.longitude);
        MarkerOptions marcador = new MarkerOptions();
        marcador.position(latLng);
        marcador.title("Punto");

        Mapeo.addMarker(marcador);
        Marcadores.add(marcador);

        if (Marcadores.size() == 6) {
            Lineas = new PolylineOptions();

            for (int i = 0; i < Marcadores.size(); i++) {
                Lineas.add(Marcadores.get(i).getPosition());
                LatLng origen = Marcadores.get(i).getPosition();
                LatLng destino = Marcadores.get((i + 1) % Marcadores.size()).getPosition();
                TotDist = 0.00;
                ConectWBS(origen, destino);
            }
            Lineas.add(Marcadores.get(0).getPosition());
            Lineas.width(8);
            Lineas.color(Color.GREEN);
            Mapeo.addPolyline(Lineas);
            Marcadores.clear();
        }
    }

    private void ConectWBS(LatLng Origin, LatLng Destin) {
        String origen = "origins=" + Origin.latitude + "," + Origin.longitude;
        String destinos = "destinations=" + Destin.latitude + "%2C" + Destin.longitude;
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=meters&"
                + origen + "&"
                + destinos + "&key=AIzaSyD0ONVovLBMhzWI2nU0XEkJguQO-y_cJrI";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArrayFila = response.getJSONArray("rows");
                            JSONObject jObjectElementos = jArrayFila.getJSONObject(0);
                            JSONArray jArrayElemento = jObjectElementos.getJSONArray("elements");

                            for (int i = 0; i < jArrayElemento.length(); i++) {
                                JSONObject jObjectDistancia = jArrayElemento.getJSONObject(i);
                                JSONObject Distancia = jObjectDistancia.getJSONObject("distance");
                                TotDist += Double.parseDouble(Distancia.getString("value"));
                            }
                            DatoDist.setText("La distancia es: " + TotDist.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);
    }
}
