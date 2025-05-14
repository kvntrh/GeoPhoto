package com.example.geophoto.database;

import android.provider.BaseColumns;

public final class ImageContract {
    // no one should ever create an instance
    private ImageContract() {
    }

    /* defines the table contents */
    public static class ImageEntry implements BaseColumns {
        // BaseColumns interface provides _ID and _COUNT
        public static final String TABLE_NAME = "images";
        public static final String _ID = "_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_IMAGE_DATA = "image_base64";
    }
}