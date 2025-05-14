package com.example.geophoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileNotFoundException;

public class GalleryActivity extends AppCompatActivity {

    private ImageView imgPhoto;
    private Button btnFindPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initActivity();
        createOnClickPhotoButton();
    }

    private void initActivity() {
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        btnFindPhoto = (Button) findViewById(R.id.btnFindPhoto);
    }

    private void createOnClickPhotoButton() {
        btnFindPhoto.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ouvrir la galerie
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
    }

    /**
     * au retour de la sélection de la photo dans la galerie
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            imgPhoto.setImageURI(selectedImage);
            // BitmapFactory pour obtenir un bitmap et l'afficher dans l'ImageView
            Bitmap bitmap = null;
            try {
                assert selectedImage != null;
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            imgPhoto.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
        }
    }
}