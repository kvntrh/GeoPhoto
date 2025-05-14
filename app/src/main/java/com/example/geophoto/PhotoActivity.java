package com.example.geophoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
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

public class PhotoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button btnPrendrePhoto;
    private ImageView imgAffichagePhoto;
    private Button btnEnreg;
    private String photoPath = null;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initActivity();
    }

    private void initActivity() {
        btnPrendrePhoto = (Button) findViewById(R.id.btnPrendrePhoto);
        imgAffichagePhoto = (ImageView) findViewById(R.id.imgAffichagePhoto);
        btnEnreg = (Button) findViewById(R.id.btnEnreg);

        createOnClicBtnPrendrePhoto();
        createOnClicBtnEnreg();
    }

    private void createOnClicBtnEnreg() {
        btnEnreg.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // enregistrer l'image dans la galerie
                MediaStore.Images.Media.insertImage(getContentResolver(), image, "Titre", "Description");
            }
        });
    }

    private void createOnClicBtnPrendrePhoto() {
        btnPrendrePhoto.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                prendreUnePhoto();
            }
        });
    }

    private void prendreUnePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Vérifie qu’il existe bien une app pour gérer cet intent
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File photoFile = File.createTempFile("photo" + time, ".jpg", photoDir);
            photoPath = photoFile.getAbsolutePath();
            Uri photoUri = FileProvider.getUriForFile(PhotoActivity.this,
                    PhotoActivity.this.getApplicationContext().getPackageName() + ".provider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Afficher l'image dans l'ImageView
            image = BitmapFactory.decodeFile(photoPath);
            imgAffichagePhoto.setImageBitmap(image);
        }
    }
}