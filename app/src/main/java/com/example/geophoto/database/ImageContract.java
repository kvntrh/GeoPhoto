package com.example.geophoto.database;

import android.provider.BaseColumns;

public final class ImageContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ImageContract() {
    }

    /* Inner class that defines the table contents */
    public static class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "images";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_IMAGE_DATA = "image_base64";
    }
}