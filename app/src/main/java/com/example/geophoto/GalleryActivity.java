package com.example.geophoto;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // Added import for EditText
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geophoto.database.ImageContract; // Import for database contract
import com.example.geophoto.database.ImageDbHelper; // Import for database helper

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "GalleryActivity"; // TAG for logging

    private ImageView imgPhoto;
    private Button btnFindPhoto;
    private EditText inputTexteTitre; // Declaration for EditText
    private Button btnEnregistrerPhotoDansSqlite;

    private Bitmap selectedBitmap = null; // To store the selected image
    private ImageDbHelper dbHelper; // Database helper

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

        dbHelper = new ImageDbHelper(this); // Initialize dbHelper

        initActivity();
        createOnClickPhotoButton();
        createOnClickEnregistrerPhotoButton();
    }

    private void initActivity() {
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        btnFindPhoto = (Button) findViewById(R.id.btnFindPhoto);
        inputTexteTitre = (EditText) findViewById(R.id.inputTexteTitre); // Initialization
        btnEnregistrerPhotoDansSqlite = (Button) findViewById(R.id.btnEnregistrerPhotoDansSqlite);
    }

    private void createOnClickPhotoButton() {
        btnFindPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ouvrir la galerie
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
    }

    /**
     * Enregistrer la photo dans SQLite en Base64
     */
    private void createOnClickEnregistrerPhotoButton() {
        btnEnregistrerPhotoDansSqlite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupérer le titre
                String titre = inputTexteTitre.getText().toString().trim();

                if (titre.isEmpty()) {
                    Toast.makeText(GalleryActivity.this, "Please enter a title", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedBitmap == null) {
                    Toast.makeText(GalleryActivity.this, "No image selected to save", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convertir le bitmap en base64
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // Using PNG for better quality, can be
                                                                                 // JPEG
                byte[] byteArray = stream.toByteArray();
                String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                // Enregistrer dans SQLite
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(ImageContract.ImageEntry.COLUMN_NAME_TITLE, titre);
                values.put(ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA, imageBase64);

                long newRowId = db.insert(ImageContract.ImageEntry.TABLE_NAME, null, values);

                if (newRowId != -1) {
                    Toast.makeText(GalleryActivity.this, "Photo saved successfully with ID: " + newRowId,
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Photo saved with ID: " + newRowId + ", Title: " + titre);
                    // Optionally clear fields
                    inputTexteTitre.setText("");
                    imgPhoto.setImageResource(android.R.color.transparent); // Clear image view or set placeholder
                    selectedBitmap = null;
                } else {
                    Toast.makeText(GalleryActivity.this, "Error saving photo", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving photo to SQLite. Title: " + titre);
                }
                // db.close(); // dbHelper will be closed in onDestroy
            }
        });
    }

    /**
     * au retour de la sélection de la photo dans la galerie
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    selectedBitmap = BitmapFactory.decodeStream(imageStream);
                    imgPhoto.setImageBitmap(selectedBitmap);
                    if (imageStream != null) {
                        imageStream.close();
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found: " + e.getMessage());
                    Toast.makeText(this, "Selected image not found", Toast.LENGTH_SHORT).show();
                    selectedBitmap = null; // Reset bitmap
                    imgPhoto.setImageResource(android.R.color.transparent); // Clear image view
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    selectedBitmap = null; // Reset bitmap
                    imgPhoto.setImageResource(android.R.color.transparent); // Clear image view
                }
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
                selectedBitmap = null;
                imgPhoto.setImageResource(android.R.color.transparent);
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            // It's good practice to also clear if the user cancels
            // selectedBitmap = null;
            // imgPhoto.setImageResource(android.R.color.transparent); // Or keep previous
            // if desired
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close(); // Close the database helper
        super.onDestroy();
    }
}