package com.fct.geojebus.database;

import android.provider.BaseColumns;

public class RecentConstants {

    private RecentConstants() {

    }

    public static final class RecentData implements BaseColumns {

        public static final String DB_NAME = "Recent.db";
        public static final int DB_VERSION = 2;
        public static final String TABLE_NAME = "Recent";
        public static final String COL_NAME = "item_name";
        public static final String COL_TYPE = "item_type";
        public static final String COL_COUNTRY = "item_country";
        public static final String COL_NUM = "item_num";
        private RecentData() {

        }
    }
}
