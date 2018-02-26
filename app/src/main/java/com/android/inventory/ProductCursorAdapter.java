package com.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.inventory.data.ProductContract.ProductEntry;
import com.android.inventory.utilits.ImageOperations;

import java.text.DecimalFormat;


public class ProductCursorAdapter extends CursorAdapter implements View.OnClickListener {

    private final static String QUANTITY_UNIT = "items";
    private final static String ONE_QUANTITY_UNIT = "item";

    private final MainActivity mActivity;

    public ProductCursorAdapter(MainActivity context, Cursor cursor) {
        super(context, cursor, 0);
        mActivity = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {


        TextView nameTextView = view.findViewById(R.id.name);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        TextView priceTextView = view.findViewById(R.id.price);
        ImageView productImageView = view.findViewById(R.id.main_prod_image);
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_IMAGE);

        int id = cursor.getInt(idColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        int quantity = cursor.getInt(quantityColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);

        byte[] imageByte = cursor.getBlob(imageColumnIndex);
        if (imageByte.length > 0) {
            Bitmap bitmap = ImageOperations.convertImageByteToBitmap(imageByte);
            productImageView.setImageBitmap(bitmap);
        }

        String unit = QUANTITY_UNIT;
        if (quantity == 1)
            unit = ONE_QUANTITY_UNIT;

        nameTextView.setText(name);
        quantityTextView.setText(String.format("%d %s", quantity, unit));
        priceTextView.setText(String.format("%s %s", formatePrice(price), "$"));


        TextView saleBtn = view.findViewById(R.id.sale);
        SalesHolder holder = new SalesHolder();
        holder.id = id;
        holder.quantity = quantity;
        saleBtn.setTag(holder);
        saleBtn.setOnClickListener(this);
    }

    //handle sale button
    @Override
    public void onClick(View view) {
        SalesHolder holder = (SalesHolder) view.getTag();
        if (holder.quantity > 0) {
            mActivity.updateQuantity(holder.id, --holder.quantity);
        } else {
            Toast.makeText(mActivity, "There is no item of this product", Toast.LENGTH_SHORT).show();
        }

    }

    private String formatePrice(double price) {
        DecimalFormat formater = new DecimalFormat("0.0");
        return formater.format(price);
    }

    static class SalesHolder {
        int id;
        int quantity;
    }


}
