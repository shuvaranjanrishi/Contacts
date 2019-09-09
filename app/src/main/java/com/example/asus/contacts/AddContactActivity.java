package com.example.asus.contacts;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

public class AddContactActivity extends AppCompatActivity {

    ImageView imageView;
    EditText nameET,numberEt;

    final int REQUEST_CODE_CAMERA = 111;
    final int REQUEST_CODE_GALLERY = 222;
    final int CROP_IMAGE_REQUEST_CODE = 999;

    DBHelper dbHelper;

    boolean selectedImage = false;
     Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.contact_app_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        imageView = findViewById(R.id.imageViewId);
        nameET = findViewById(R.id.nameEditTextId);
        numberEt = findViewById(R.id.numberEditTextId);

        dbHelper = new DBHelper(getApplicationContext());

        //taking image from gallery or camera by clicking on image
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                CharSequence[] items = {"Take photo from camera","Take photo from gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddContactActivity.this);
                builder.setTitle("Choose Action");

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){
                            //take photo from your camera
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File file = new File(Environment.getExternalStorageDirectory(),"file"+String.valueOf(System.currentTimeMillis())+".jpg");
                            uri = Uri.fromFile(file);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                            startActivityForResult(cameraIntent,REQUEST_CODE_CAMERA);

                        }else {
                            //take photo from gallery
                            Intent imageIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(imageIntent,REQUEST_CODE_GALLERY);
                        }

                    }
                });
                builder.show();

            }
        });
    }

    //get activity result to pick image and set
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK){
            CropImage();
        }

        else if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){

            uri = data.getData();
            CropImage();

        }else if(requestCode == CROP_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null){

            Bundle bundle = data.getExtras();
            Bitmap bitmap = bundle.getParcelable("data");
            imageView.setImageBitmap(bitmap);
            selectedImage = true;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void CropImage() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri,"image/*");

        cropIntent.putExtra("crop","true");
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("outputX",200);
        cropIntent.putExtra("outputY",200);
        cropIntent.putExtra("return-data",true);
        startActivityForResult(cropIntent,CROP_IMAGE_REQUEST_CODE);
    }

    public void saveContactBtnAction(View view) {

        byte[] byteImage;
        String name = nameET.getText().toString().trim();
        String number = numberEt.getText().toString().trim();

        if(selectedImage){
            byteImage = imageToByteArray(imageView);

        }else{
            //if no image selected then take a default image from resources.
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.person_icon_male);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);

            byteImage = byteArrayOutputStream.toByteArray();
            selectedImage = false;
        }

        if(inputsAreCorrect(name,number)){
            ContactHolder values = new ContactHolder(byteImage,name,number);
            long rowId = dbHelper.insertData(values);

            if(rowId != -1){
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    //convert the image into byte array.
    private byte[] imageToByteArray(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return byteArray;
    }

    //validating inputs
    public boolean inputsAreCorrect(String name,String number){

        if(name.isEmpty()){
            nameET.setError("Name can't be empty");
            nameET.requestFocus();
            return false;
        }
        if(number.isEmpty()){
            numberEt.setError("Number can't be empty");
            numberEt.requestFocus();
            return false;
        }
        return true;
    }
}
