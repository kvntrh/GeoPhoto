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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Vérifie qu’il existe bien une app pour gérer cet intent
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File photoFile = File.createTempFile("photo" + time, ".jpg", photoDir);
            photoPath = photoFile.getAbsolutePath();
            Uri photoUri = FileProvider.getUriForFile(MainActivity.this,
                    MainActivity.this.getApplicationContext().getPackageName() + ".provider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestcode, int resultCode, Intent data) {
        super.onActivityResult(requestcode, resultCode, data);
        if (requestcode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap image = BitmapFactory.decodeFile(photoPath);
            imgAffichePhoto.setImageBitmap(image);
        }
    }
}