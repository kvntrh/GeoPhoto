package com.example.geophoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private String photoPath = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imgAffichePhoto;
    private Button btnPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnPhoto = findViewById(R.id.btn_photo);
        imgAffichePhoto = findViewById(R.id.imgAffichePhoto);

        findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            // Ouvrir la galerie
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btn_photo).setOnClickListener(v -> {
            // Ouvrir la page de prise de photo
            Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btn_map).setOnClickListener(v -> {
            // Afficher la carte
        });

    }
}