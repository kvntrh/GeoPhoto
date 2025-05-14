package com.example.geophoto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Import your contract and helper classes if they are in the same package
// If not, use the full package name:
// import com.example.sqliteimagedemo.ImageContract;
// import com.example.sqliteimagedemo.ImageDbHelper;

public class InternalDatabaseActivity extends AppCompatActivity {

    private static final String TAG = "InternalDBActivity"; // Updated TAG for clarity

    private EditText editTextTitle;
    private Button buttonSave;
    private Button buttonLoad;
    private TextView textViewData;

    private ImageDbHelper dbHelper;

    // This is your pre-existing Base64 string for an image.
    // For a real app, this would likely come from an image picker and encoder.
    // Replace this with a shorter valid Base64 string for testing if needed, or a
    // real one.
    private String stringBase64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA" +
            "AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO" +
            "9TXL0Y4OHwAAAABJRU5ErkJggg=="; // Example: 5x5 red dot

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure to use the new layout file name here
        setContentView(R.layout.activity_internal_database);

        editTextTitle = findViewById(R.id.editTextTitle);
        buttonSave = findViewById(R.id.buttonSave);
        buttonLoad = findViewById(R.id.buttonLoad);
        textViewData = findViewById(R.id.textViewData);

        dbHelper = new ImageDbHelper(this);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageEntry();
            }
        });

        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImageEntries();
            }
        });
    }

    private void saveImageEntry() {
        String title = editTextTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ImageContract.ImageEntry.COLUMN_NAME_TITLE, title);
        values.put(ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA, stringBase64Image);

        long newRowId = db.insert(ImageContract.ImageEntry.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Image entry saved with ID: " + newRowId, Toast.LENGTH_SHORT).show();
            editTextTitle.setText("");
            Log.d(TAG, "Inserted new row with ID: " + newRowId);
        } else {
            Toast.makeText(this, "Error saving image entry", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error inserting new row");
        }
    }

    private void loadImageEntries() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                ImageContract.ImageEntry.COLUMN_NAME_TITLE,
                ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA
        };

        String sortOrder = ImageContract.ImageEntry.COLUMN_NAME_TITLE + " ASC";

        Cursor cursor = db.query(
                ImageContract.ImageEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);

        StringBuilder dataBuilder = new StringBuilder();
        if (cursor.getCount() == 0) {
            dataBuilder.append("No image entries found.\n");
        } else {
            dataBuilder.append("Image Entries:\n\n");
        }

        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(ImageContract.ImageEntry.COLUMN_NAME_TITLE));
            String imageData = cursor
                    .getString(cursor.getColumnIndexOrThrow(ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA));

            dataBuilder.append("ID: ").append(itemId).append("\n");
            dataBuilder.append("Title: ").append(title).append("\n");
            dataBuilder.append("Image Data (first 30 chars): ").append(
                    imageData.length() > 30 ? imageData.substring(0, 30) + "..." : imageData).append("\n\n");

            Log.d(TAG,
                    "Read item: ID=" + itemId + ", Title='" + title + "', ImageData(len)='" + imageData.length() + "'");
        }
        cursor.close();
        textViewData.setText(dataBuilder.toString());
    }

    public int updateImageEntry(long rowId, String newTitle, String newImageData) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ImageContract.ImageEntry.COLUMN_NAME_TITLE, newTitle);
        values.put(ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA, newImageData);

        String selection = ImageContract.ImageEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(rowId) };

        int count = db.update(
                ImageContract.ImageEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.d(TAG, "Updated " + count + " rows.");
        return count;
    }

    public int deleteImageEntry(long rowId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = ImageContract.ImageEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(rowId) };
        int deletedRows = db.delete(ImageContract.ImageEntry.TABLE_NAME, selection, selectionArgs);
        Log.d(TAG, "Deleted " + deletedRows + " rows.");
        return deletedRows;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}