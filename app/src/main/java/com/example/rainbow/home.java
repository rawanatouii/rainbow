import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.rainbow.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private EditText editCity;
    private Button btnGetWeather;
    private TextView txtWeatherInfo;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        editCity = findViewById(R.id.editTextCity);
        btnGetWeather = findViewById(R.id.buttonGetWeather);
        txtWeatherInfo = findViewById(R.id.textViewWeather);

        // Initialiser le gestionnaire de localisation
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if the permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed with your location-related code
            setupWeatherButton();
            getLocationAndUpdateWeather();
        }
    }

    private void setupWeatherButton() {
        btnGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });
    }

    private void fetchData() {
        String cityName = editCity.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.prevision-meteo.ch/services/json/" + cityName;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject currentCondition = response.getJSONObject("current_condition");
                            String temperature = currentCondition.getString("tmp");
                            String conditions = currentCondition.getString("condition");
                            txtWeatherInfo.setText("Temperature: " + temperature + "°C\nConditions: " + conditions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            txtWeatherInfo.setText("Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtWeatherInfo.setText("Error fetching data. " + error.getMessage());
                    }
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void getLocationAndUpdateWeather() {
        // Commencer à écouter les mises à jour de localisation
        // Minimum time interval between location updates, in milliseconds
        long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute
        // Minimum distance to change Updates in meters
        float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            // Obtenez la dernière localisation connue
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // Convertir les coordonnées en nom de ville (utilisez votre propre logique)
                String cityName = getCityNameFromLocation(location);
                editCity.setText(cityName);
            }
        }
    }

    private String getCityNameFromLocation(Location location) {
        // Convertir les coordonnées en nom de ville (utilisez Geocoder)
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your location-related code
                setupWeatherButton();
                getLocationAndUpdateWeather();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Mettez à jour l'interface utilisateur avec la ville obtenue de la localisation
        String cityName = getCityNameFromLocation(location);
        editCity.setText(cityName);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Si le fournisseur de localisation est désactivé, proposez à l'utilisateur de l'activer
        //...
    }
}
