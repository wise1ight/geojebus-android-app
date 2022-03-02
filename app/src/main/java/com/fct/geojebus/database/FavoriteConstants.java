package com.fct.geojebus.database;

import android.provider.BaseColumns;

public final class FavoriteConstants implements BaseColumns {

    private FavoriteConstants() {

    }

    public static final class FavoriteData implements BaseColumns {

        public static final String DB_NAME = "Favorite.db";
        public static final int DB_VERSION = 2;
        public static final String TABLE_NAME = "Favorite";
        public static final String COL_NAME = "item_name";
        public static final String COL_TYPE = "item_type";
        public static final String COL_VALUE = "item_value";
        public static final String ORDER = "Item_order";
        private FavoriteData() {

        }
    }

}