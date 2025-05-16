package com.example.geophoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PARTIE 1 : Prendre une photo et l'afficher
 * PARTIE 2 : Transformer une image en chaîne de caractères et vice versa
 * PARTIE 3 : Enregistrer l'image dans une base de données SQLite
 * PARTIE 4 : Afficher l'image de la base de données
 */
public class PhotoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button btnPrendrePhoto;
    private ImageView imgAffichagePhoto;
    private Button btnEnreg;
    private String photoPath = null;
    private Bitmap image;

    /**
     * PARTIE 2
     */
    private Button btnBitmapToString;
    private TextView txtBitmap;
    private Button btnStringToBitmap;
    private ImageView imgAffichageFromString;

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
        btnBitmapToString = (Button) findViewById(R.id.btnBitmapToString);
        txtBitmap = (TextView) findViewById(R.id.StringBase64);
        btnStringToBitmap = (Button) findViewById(R.id.btnStringToBitmap);
        imgAffichageFromString = (ImageView) findViewById(R.id.imgAffichageFromString);

        createOnClicBtnPrendrePhoto();
        createOnClicBtnEnreg();
        createOnClicBtnBitmapToString();
        createOnClicBtnStringToBitmap();
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

    /**
     * PARTIE 2
     * Convertir une image en chaîne de caractères et vice versa
     */
    private void createOnClicBtnBitmapToString() {
        btnBitmapToString.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                    String imageString = bitmapToString(bitmap);
                    txtBitmap.setText(imageString);
                }
            }
        });
    }

    private void createOnClicBtnStringToBitmap() {
        btnStringToBitmap.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgAffichageFromString.setImageBitmap(stringToBitmap(txtBitmap.getText().toString()));
            }
        });
    }

    private void prendreUnePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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

    /**
     * PARTIE 2
     * Convertir une image en chaîne de caractères et vice versa
     */
    private String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap stringToBitmap(String encodedString) {
        byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
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