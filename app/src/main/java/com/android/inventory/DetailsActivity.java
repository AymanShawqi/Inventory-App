package com.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.inventory.data.ProductContract.ProductEntry;
import com.android.inventory.utilits.ImageOperations;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int EXISTING_PRODUCT_LOADER = 0;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mPhoneEditText;
    private EditText mSupplierNameEditText;
    private EditText mQuantityEditText;
    private ImageView mImageView;
    private byte[] mImageByte = null;
    private Uri mCurrentUri;
    private int mQuantity;
    private String mPhoneString = null;

    private boolean mProdHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProdHasChanged = true;
            return false;
        }
    };

    private DialogInterface.OnClickListener openCameraClickListener;
    private DialogInterface.OnClickListener openGalleryClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toast.makeText(this, "Just click on the image to change it", Toast.LENGTH_LONG).show();

        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        if (mCurrentUri == null) {
            setTitle(getString(R.string.add_product));
            invalidateOptionsMenu();
            mQuantity = 0;
        } else {
            setTitle(getString(R.string.edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.name_edit);
        mPriceEditText = (EditText) findViewById(R.id.price_edit);
        mPhoneEditText = (EditText) findViewById(R.id.supplier_phone);
        mSupplierNameEditText = (EditText) findViewById(R.id.supplier_name);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_edit);

        ImageButton increaseQuantity = (ImageButton) findViewById(R.id.increase_quantity);
        ImageButton decreaseQuantity = (ImageButton) findViewById(R.id.decrease_quantity);
        Button callSupplier = (Button) findViewById(R.id.call);
        mImageView = (ImageView) findViewById(R.id.photo);

        openCameraClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openCamera();
            }
        };

        openGalleryClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openGallery();
            }
        };

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeImageDialog();
            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuantity++;
                mQuantityEditText.setText(Integer.toString(mQuantity));
            }
        });

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantity > 0) {
                    mQuantity--;
                    mQuantityEditText.setText(Integer.toString(mQuantity));
                }
                Toast.makeText(DetailsActivity.this, "you should give value to quantity", Toast.LENGTH_SHORT).show();
            }
        });

        callSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSupplierCall();
            }
        });

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentUri == null) {
            MenuItem item = menu.findItem(R.id.delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.save:
                saveProduct();
                return true;
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                handleHomeClick();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        mPhoneString = mPhoneEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(mPhoneString) || TextUtils.isEmpty(quantityString)
                || TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, "You must fill empty field", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceString);
        int quantity = Integer.parseInt(quantityString);
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_Product_NAME, nameString);
        values.put(ProductEntry.COLUMN_Product_PRICE, price);
        values.put(ProductEntry.COLUMN_Product_SUPPLIER_PHONE, mPhoneString);
        values.put(ProductEntry.COLUMN_Product_SUPPLIER_NAME, supplierName);
        values.put(ProductEntry.COLUMN_Product_QUANTITY, quantity);

        if (mImageByte != null) {
            values.put(ProductEntry.COLUMN_Product_IMAGE, mImageByte);
        }

        if (mCurrentUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null)
                Toast.makeText(this, getString(R.string.details_insert_product_fail), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.details_insert_product_successful), Toast.LENGTH_SHORT).show();
        } else {
            int rowUpdated = getContentResolver().update(mCurrentUri, values, null, null);
            if (rowUpdated == 0)
                Toast.makeText(this, getString(R.string.details_update_product_fail), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.details_update_product_successful), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                deleteProduct();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentUri != null) {
            int deletedRow = getContentResolver().delete(mCurrentUri, null, null);
            if (deletedRow == 0)
                Toast.makeText(this, R.string.details_delete_product_fail, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.details_delete_product_successful, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardBtnClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardBtnClickListener);
        builder.setNegativeButton(R.string.editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void handleHomeClick() {
        if (!mProdHasChanged) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            DialogInterface.OnClickListener discardBtnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                }
            };
            showUnsavedChangesDialog(discardBtnClickListener);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_Product_NAME,
                ProductEntry.COLUMN_Product_QUANTITY,
                ProductEntry.COLUMN_Product_PRICE,
                ProductEntry.COLUMN_Product_SUPPLIER_PHONE,
                ProductEntry.COLUMN_Product_IMAGE,
                ProductEntry.COLUMN_Product_SUPPLIER_NAME
        };
        return new CursorLoader(this, mCurrentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1)
            return;

        if (cursor.moveToNext()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_SUPPLIER_PHONE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_Product_IMAGE);
            int supplierNameColumnIndex=cursor.getColumnIndex(ProductEntry.COLUMN_Product_NAME);

            String nameString = cursor.getString(nameColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String supplierPhone = cursor.getString(supplierColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);

            byte[] imageByte = cursor.getBlob(imageColumnIndex);
            if (imageByte.length > 0) {
                Bitmap bitmap = ImageOperations.convertImageByteToBitmap(imageByte);
                mImageView.setImageBitmap(bitmap);
            }

            mNameEditText.setText(nameString);
            mQuantityEditText.setText(Integer.toString(mQuantity));
            mPriceEditText.setText(" " + price);
            mPhoneEditText.setText(supplierPhone);
            mSupplierNameEditText.setText(supplierName);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mPhoneEditText.setText("");
        mSupplierNameEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        if (!mProdHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardBtnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardBtnClickListener);
    }

    private boolean isTelephoneReady() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    private void handleSupplierCall() {
        if (isTelephoneReady()) {
            mPhoneString = mPhoneEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(mPhoneString)) {
                String phoneNumber = String.format("tel:%s", mPhoneString);
                Intent implicitIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
                startActivity(implicitIntent);
            } else {
                Toast.makeText(this, "The number is not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "The telephone is not ready", Toast.LENGTH_SHORT).show();
        }

    }

    void showChangeImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Choose Image");
        builder.setNegativeButton("Open Camera", openCameraClickListener);
        builder.setPositiveButton("Open Gallery", openGalleryClickListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void openGallery() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, uri);
        startActivityForResult(pickPhoto, 1);
    }

    void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 0);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0://camera
                if (resultCode == RESULT_OK) {
                    Bundle exteras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) exteras.get("data");
                    mImageView.setImageBitmap(imageBitmap);
                    mImageByte = ImageOperations.convertBitmapToByteArray(imageBitmap);
                }
                break;
            case 1://Gallery
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    mImageView.setImageURI(selectedImageUri);
                    Bitmap imageBitmap = ImageOperations.convertImageUriToBitmap(selectedImageUri, this);
                    mImageByte = ImageOperations.convertBitmapToByteArray(imageBitmap);
                }
                break;
            default:
                Toast.makeText(this, "Error in choosing product image", Toast.LENGTH_LONG).show();
        }

    }
}
