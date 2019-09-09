package com.example.asus.contacts;

import android.Manifest;
import android.app.Activity;
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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ViewContactDetailsActivity  extends Activity {

    ImageView detailsImageView;
    TextView nameTextView,numberTextView;
    Button editBtn, deleteBtn;

    final int REQUEST_CODE_CAMERA = 111;
    final int REQUEST_CODE_GALLERY = 222;
    final int CROP_IMAGE_REQUEST_CODE = 999;

    ImageView updateImageView;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact_details);

        detailsImageView = findViewById(R.id.detailsImageViewId);
        nameTextView = findViewById(R.id.nameDetailsTVId);
        numberTextView = findViewById(R.id.numberDetailsTVId);
        editBtn = findViewById(R.id.editBtnId);
        deleteBtn = findViewById(R.id.deleteBtnId);

        final DBHelper dbHelper = new DBHelper(getApplicationContext());

        Bundle bundle = getIntent().getExtras();

        final int id = getIntent().getIntExtra("ID",0);

        final byte[] byteImage = bundle.getByteArray("IMAGE");
        final Bitmap bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);

        final String name = bundle.getString("NAME");
        final String number = bundle.getString("NUMBER");

        detailsImageView.setImageBitmap(bitmap);
        nameTextView.setText(name);
        numberTextView.setText(number);

        //edit button action
        editBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewContactDetailsActivity.this);
                LayoutInflater inflater = LayoutInflater.from(ViewContactDetailsActivity.this);
                View view = inflater.inflate(R.layout.dialog_update_contact_layout,null);

                builder.setView(view);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                updateImageView = view.findViewById(R.id.updateImageViewId);
                final EditText updateNameET = view.findViewById(R.id.updateNameEditTextId);
                final EditText updateNumberET = view.findViewById(R.id.updateNumberEditTextId);
                Button updateBtn = view.findViewById(R.id.updateContactBtnId);
                Button cancelBtn = view.findViewById(R.id.cancelBtnId);

                updateImageView.setImageBitmap(bitmap);
                updateNameET.setText(name);
                updateNumberET.setText(number);

                //image view click event
                updateImageView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){

                        CharSequence[] items = {"Take photo from camera","Take photo from gallery"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewContactDetailsActivity.this);
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
                                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galleryIntent,REQUEST_CODE_GALLERY);
                                }

                            }
                        });
                        builder.show();

                    }
                });


                //update button action
                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int contactId = id;
                        byte[] byteImage = imageViewToByteArray(updateImageView);

                        String nameString = updateNameET.getText().toString().trim();
                        String numberString = updateNumberET.getText().toString().trim();

                        //check validating inputs
                        if(inputsAreCorrect(nameString,numberString)){
                            dbHelper.updateData(contactId,byteImage,nameString,numberString);
                            setResult(RESULT_OK);
                            finish();
                            alertDialog.dismiss();
                        }

                    }

                    //validating inputs
                    private boolean inputsAreCorrect(String nameString,String numberString){

                        if(nameString.isEmpty()){
                            updateNameET.setError("Name can't be empty");
                            updateNameET.requestFocus();
                            return false;
                        }
                        if(numberString.isEmpty()){
                            updateNumberET.setError("Number can't be empty");
                            updateNumberET.requestFocus();
                            return false;
                        }
                        return true;
                    }

                });

                //cancel button action
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

            }
        });

        //delete button action
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewContactDetailsActivity.this);
                builder.setIcon(R.drawable.alert_sign_red_border);
                builder.setTitle("Warning...!!");
                builder.setMessage("Are You Sure to Delete the Contact ?");

                //for yes button action
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int contactId = id;
                        dbHelper.deleteData(contactId);
                        setResult(300);
                        finish();
                    }
                });

                //for no button action
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });
    }

    //convert the image into byte array.
    private byte[] imageViewToByteArray(ImageView updateImageView) {
        Bitmap bitmap = ((BitmapDrawable)updateImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return byteArray;
    }

    //taking permission to access gallery and pick image
 //   @Override
 //   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

//        if (requestCode == REQUEST_CODE_GALLERY) {
//
//            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(intent,REQUEST_CODE_GALLERY);
//            }
//            else {
//                Toast.makeText(this, "You don't have permission to access gallery", Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }

    //    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  //  }

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
            updateImageView.setImageBitmap(bitmap);
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
}
