package com.example.geophoto;

import javax.print.attribute.standard.Media;

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

    private static final int RETOUR_PRENDRE_PHOTO = 1;

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
        btnPrendrePhoto = findViewById(R.id.btnPrendrePhoto);
        imgAffichagePhoto = findViewById(R.id.imgAffichagePhoto);
        btnEnreg = findViewById(R.id.btnEnreg);

        createOnClicBtnPrendrePhoto();
        createOnClicBtnEnreg();
    }

    private void createOnClicBtnEnreg() {
        btnEnreg.setOnClickListener(new Button.setOnClickListener() {
            @Override
            public void onClick(View v) {
                // enregistrer l'image dans la galerie
                MediaStore.Images.Media.insertImage(getContentResolver(), image, "Titre", "Description");
            }
        });
    }

    private void createOnClicBtnPrendrePhoto() {
        btnPrendrePhoto.setOnClickListener(new Button.setOnClickListener() {
            @Override
            public void onClick(View v) {
                prendreUnePhoto();
            }
        });
    }

    private void prendreUnePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Créer un fichier pour stocker l'image
            String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File photoFile = File.createTempFile(
                        "photo" + time, ".jpg", photoDir);
                // Enregistrer le chemin de l'image
                photoPath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.geophoto.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, RETOUR_PRENDRE_PHOTO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RETOUR_PRENDRE_PHOTO && resultCode == RESULT_OK) {
            // Afficher l'image dans l'ImageView
            image = BitmapFactory.decodeFile(photoPath);
            imgAffichagePhoto.setImageBitmap(image);
        }
    }
}