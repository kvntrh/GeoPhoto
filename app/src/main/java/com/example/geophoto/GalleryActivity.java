package com.example.geophoto;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageView;
import androidx.core.widget.Button;

public class GalleryActivity extends AppCompatActivity {

    private ImageView imgPhoto;
    private Button btnPhoto;

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
    }

    private void initActivity() {
        imgFindPhoto = (ImageView) findViewById(R.id.imgPhoto);
        btnPhoto = (Button) findViewById(R.id.btnPhoto);
    }

    private void createOnClickPhotoButton() {
        btnPhoto.setOnClickListener(new Button.OnClickListener() {
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
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
            imgPhoto.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
        }
    }
}