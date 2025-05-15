package com.example.geophoto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation; // This will hold the current fetched location

    private FusedLocationProviderClient fusedLocationClient;
    private TextView latitudeTextView, longitudeTextView; // Renamed for clarity
    private Button btnSaveLocation, btnDisplayLastLocation;

    private boolean positionInitialeDefinie = false;

    // --- Database Configuration ---
    // WARNING: Hardcoding credentials is a security risk.
    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/MyDB"; // 10.0.2.2 is for Android Emulator to
                                                                            // connect to host's localhost
    private static final String DB_USER = "aaa";
    private static final String DB_PASSWORD = "aaa";
    private static final String TAG_DB = "DatabaseAccess";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);
        btnSaveLocation = findViewById(R.id.btnSaveLocation);
        btnDisplayLastLocation = findViewById(R.id.btnDisplayLastLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation(); // Get initial location

        btnSaveLocation.setOnClickListener(v -> {
            if (currentLocation != null) {
                saveLocationToDB(currentLocation.getLatitude(), currentLocation.getLongitude());
            } else {
                Toast.makeText(MapActivity.this, "Current location not available to save.", Toast.LENGTH_SHORT).show();
                getLastLocation(); // Try to fetch it again
            }
        });

        btnDisplayLastLocation.setOnClickListener(v -> {
            displayLastSavedLocationFromDB();
        });

        // Explicitly load the MySQL driver (optional for newer JDBC drivers, but good
        // practice for older ones)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.e(TAG_DB, "MySQL JDBC Driver not found.", e);
            Toast.makeText(this, "MySQL Driver Error. Check logs.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        if (currentLocation == null) {
            Log.w("MapReady", "currentLocation is null when map is ready. Attempting to fetch again.");
            getLastLocation(); // Try to fetch if it was null
            return;
        }

        LatLng p = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String adresse = "Address unknown";
        try {
            List<Address> addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                adresse = address.getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting address", e);
        }

        map.clear(); // Clear previous markers
        map.addMarker(new MarkerOptions().position(p).title(adresse));

        if (!positionInitialeDefinie) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 17.f)); // Zoomed in a bit more
            positionInitialeDefinie = true;
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLng(p)); // Animate to new location if already initialized
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION }, FINE_PERMISSION_CODE);
            return;
        }

        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location; // Update the class member
                    Log.d("LocationUpdate",
                            "Fetched Location: " + location.getLatitude() + ", " + location.getLongitude());

                    latitudeTextView.setText(String.format(Locale.US, "Lat: %.6f", location.getLatitude()));
                    longitudeTextView.setText(String.format(Locale.US, "Lng: %.6f", location.getLongitude()));

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(MapActivity.this);
                    } else {
                        Log.e("MapFragment", "SupportMapFragment not found");
                    }
                } else {
                    Toast.makeText(MapActivity.this, "Could not get current location. Ensure GPS is on.",
                            Toast.LENGTH_LONG).show();
                    Log.w("LocationUpdate", "FusedLocationProvider returned null location.");
                }
            }
        });
        task.addOnFailureListener(e -> {
            Toast.makeText(MapActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("LocationUpdate", "Error getting location", e);
        });
    }

    private void saveLocationToDB(double lat, double lon) {
        new Thread(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                Log.d(TAG_DB, "Connecting to database to save...");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Log.d(TAG_DB, "Connected!");

                String sql = "INSERT INTO MyTable (latitude, longitude) VALUES (?, ?)";
                statement = connection.prepareStatement(sql);
                statement.setDouble(1, lat);
                statement.setDouble(2, lon);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    Log.d(TAG_DB, "Location saved successfully: Lat=" + lat + ", Lon=" + lon);
                    runOnUiThread(
                            () -> Toast.makeText(MapActivity.this, "Location saved to DB!", Toast.LENGTH_SHORT).show());
                } else {
                    Log.w(TAG_DB, "Location not saved, no rows affected.");
                    runOnUiThread(() -> Toast.makeText(MapActivity.this, "Failed to save location (no rows affected).",
                            Toast.LENGTH_SHORT).show());
                }

            } catch (SQLException e) {
                Log.e(TAG_DB, "SQL Exception while saving: " + e.getMessage(), e);
                runOnUiThread(() -> Toast
                        .makeText(MapActivity.this, "DB Error (Save): " + e.getMessage(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG_DB, "Generic Exception while saving: " + e.getMessage(), e);
                runOnUiThread(() -> Toast
                        .makeText(MapActivity.this, "Error (Save): " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                try {
                    if (statement != null)
                        statement.close();
                    if (connection != null)
                        connection.close();
                    Log.d(TAG_DB, "Connection closed after saving.");
                } catch (SQLException e) {
                    Log.e(TAG_DB, "Error closing resources (Save): ", e);
                }
            }
        }).start();
    }

    private void displayLastSavedLocationFromDB() {
        new Thread(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                Log.d(TAG_DB, "Connecting to database to display last...");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Log.d(TAG_DB, "Connected!");

                // Assuming 'id' or a timestamp column can be used for ordering to get the
                // "last"
                // If you have an auto-increment ID:
                String sql = "SELECT latitude, longitude FROM MyTable ORDER BY id DESC LIMIT 1";
                // If you use a timestamp column e.g., 'saved_at':
                // String sql = "SELECT latitude, longitude FROM MyTable ORDER BY saved_at DESC
                // LIMIT 1";

                statement = connection.prepareStatement(sql);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    double lat = resultSet.getDouble("latitude");
                    double lon = resultSet.getDouble("longitude");
                    Log.d(TAG_DB, "Last saved location: Lat=" + lat + ", Lon=" + lon);
                    runOnUiThread(() -> {
                        latitudeTextView.setText(String.format(Locale.US, "DB Lat: %.6f", lat));
                        longitudeTextView.setText(String.format(Locale.US, "DB Lng: %.6f", lon));
                        Toast.makeText(MapActivity.this, "Last saved: " + lat + ", " + lon, Toast.LENGTH_LONG).show();

                        // Optionally, update map to this location
                        if (map != null) {
                            LatLng dbLocation = new LatLng(lat, lon);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(dbLocation, 17.f));
                            map.addMarker(new MarkerOptions().position(dbLocation).title("Last Saved Location"));
                        }
                    });
                } else {
                    Log.w(TAG_DB, "No locations found in database.");
                    runOnUiThread(() -> Toast
                            .makeText(MapActivity.this, "No locations found in DB.", Toast.LENGTH_SHORT).show());
                }

            } catch (SQLException e) {
                Log.e(TAG_DB, "SQL Exception while displaying: " + e.getMessage(), e);
                runOnUiThread(() -> Toast
                        .makeText(MapActivity.this, "DB Error (Display): " + e.getMessage(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG_DB, "Generic Exception while displaying: " + e.getMessage(), e);
                runOnUiThread(() -> Toast
                        .makeText(MapActivity.this, "Error (Display): " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                try {
                    if (resultSet != null)
                        resultSet.close();
                    if (statement != null)
                        statement.close();
                    if (connection != null)
                        connection.close();
                    Log.d(TAG_DB, "Connection closed after displaying.");
                } catch (SQLException e) {
                    Log.e(TAG_DB, "Error closing resources (Display): ", e);
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        // Corrected signature: removed 'deviceId'
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location Permission Denied. App features will be limited.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}