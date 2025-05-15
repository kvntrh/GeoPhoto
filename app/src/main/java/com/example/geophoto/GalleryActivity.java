package com.example.geophoto;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geophoto.database.ImageContract;
import com.example.geophoto.database.ImageDbHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "GalleryActivity";

    private ImageView imgPhoto;
    private Button btnFindPhoto;
    private EditText inputTexteTitre;
    private Button btnEnregistrerPhotoDansSqlite;
    private Button btnLoadPhotosFromSqlite;
    private TextView textViewLoadedData;

    private Bitmap selectedBitmap = null;
    private ImageDbHelper dbHelper;

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

        dbHelper = new ImageDbHelper(this);

        initActivity();
        createOnClickPhotoButton();
        createOnClickEnregistrerPhotoButton();
        createOnClickLoadPhotosButton();
    }

    private void initActivity() {
        imgPhoto = findViewById(R.id.imgPhoto);
        btnFindPhoto = findViewById(R.id.btnFindPhoto);
        inputTexteTitre = findViewById(R.id.inputTexteTitre);
        btnEnregistrerPhotoDansSqlite = findViewById(R.id.btnEnregistrerPhotoDansSqlite);
        btnLoadPhotosFromSqlite = findViewById(R.id.btnLoadPhotosFromSqlite);
        textViewLoadedData = findViewById(R.id.textViewLoadedData);
    }

    private void createOnClickPhotoButton() {
        btnFindPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });
    }

    private void createOnClickEnregistrerPhotoButton() {
        btnEnregistrerPhotoDansSqlite.setOnClickListener(v -> {
            String titre = inputTexteTitre.getText().toString().trim();

            if (titre.isEmpty()) {
                Toast.makeText(GalleryActivity.this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedBitmap == null) {
                Toast.makeText(GalleryActivity.this, "No image selected to save", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ImageContract.ImageEntry.COLUMN_NAME_TITLE, titre);
            values.put(ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA, imageBase64);

            long newRowId = db.insert(ImageContract.ImageEntry.TABLE_NAME, null, values);

            if (newRowId != -1) {
                Toast.makeText(GalleryActivity.this, "Photo saved successfully with ID: " + newRowId, Toast.LENGTH_LONG)
                        .show();
                Log.d(TAG, "Photo saved with ID: " + newRowId + ", Title: " + titre);
                inputTexteTitre.setText("");
                imgPhoto.setImageResource(android.R.color.transparent);
                selectedBitmap = null;
                loadPhotosFromDb(); // Fonctionnalité pour charger les photos après l'enregistrement
            } else {
                Toast.makeText(GalleryActivity.this, "Error saving photo", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error saving photo to SQLite. Title: " + titre);
            }
        });
    }

    private void createOnClickLoadPhotosButton() {
        btnLoadPhotosFromSqlite.setOnClickListener(v -> loadPhotosFromDb());
    }

    private void loadPhotosFromDb() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        StringBuilder dataBuilder = new StringBuilder();

        try {
            String[] projection = {
                    ImageContract.ImageEntry._ID,
                    ImageContract.ImageEntry.COLUMN_NAME_TITLE,
                    ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA
            };

            String sortOrder = ImageContract.ImageEntry.COLUMN_NAME_TITLE + " ASC";

            cursor = db.query(
                    ImageContract.ImageEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder);

            if (cursor == null || cursor.getCount() == 0) {
                dataBuilder.append("Aucune photo trouvée dans la base de données.\n");
            } else {
                dataBuilder.append("Photos depuis la base de données:\n\n");
                while (cursor.moveToNext()) {
                    long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(ImageContract.ImageEntry._ID));
                    String title = cursor
                            .getString(cursor.getColumnIndexOrThrow(ImageContract.ImageEntry.COLUMN_NAME_TITLE));
                    String imageDataBase64 = cursor
                            .getString(cursor.getColumnIndexOrThrow(ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA));

                    dataBuilder.append("ID: ").append(itemId).append("\n");
                    dataBuilder.append("Titre: ").append(title).append("\n");
                    dataBuilder.append("Données Image (30 premiers caractères): ")
                            .append(imageDataBase64.length() > 30 ? imageDataBase64.substring(0, 30) + "..."
                                    : imageDataBase64)
                            .append("\n\n");

                    Log.i(TAG, "Loaded - ID: " + itemId + ", Title: " + title + ", Image Data Length: "
                            + (imageDataBase64 != null ? imageDataBase64.length() : 0));
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error querying database, column might not exist: " + e.getMessage());
            Toast.makeText(this, "Erreur de lecture des données: " + e.getMessage(), Toast.LENGTH_LONG).show();
            dataBuilder.append("Erreur de lecture des données.");
        } catch (Exception e) {
            Log.e(TAG, "Error loading photos from DB: " + e.getMessage());
            Toast.makeText(this, "Erreur lors du chargement des photos.", Toast.LENGTH_SHORT).show();
            dataBuilder.append("Erreur lors du chargement des photos.");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        textViewLoadedData.setText(dataBuilder.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try (InputStream imageStream = getContentResolver().openInputStream(selectedImageUri)) {
                    selectedBitmap = BitmapFactory.decodeStream(imageStream);
                    imgPhoto.setImageBitmap(selectedBitmap);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found: " + e.getMessage());
                    Toast.makeText(this, "Selected image not found", Toast.LENGTH_SHORT).show();
                    selectedBitmap = null;
                    imgPhoto.setImageResource(android.R.color.transparent);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    selectedBitmap = null;
                    imgPhoto.setImageResource(android.R.color.transparent);
                }
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
                selectedBitmap = null;
                imgPhoto.setImageResource(android.R.color.transparent);
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}