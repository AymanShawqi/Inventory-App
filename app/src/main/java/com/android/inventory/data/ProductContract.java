package com.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    //An empty private constructor makes sure that the class is not going to be initialised
    private ProductContract() {
    }

    public static final class ProductEntry implements BaseColumns {

        public final static String TABLE_NAME = "products";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_Product_NAME = "name";
        public final static String COLUMN_Product_QUANTITY = "quantity";
        public final static String COLUMN_Product_PRICE = "price";
        public final static String COLUMN_Product_SUPPLIER_PHONE = "supplier_email";
        public final static String COLUMN_Product_IMAGE = "product_image";
        public final static String COLUMN_Product_SUPPLIER_NAME = "supplier_name";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_BASE_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    }
}
