package com.enesgunumdogdu.memorybook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.enesgunumdogdu.memorybook.databinding.ActivityMemoryBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class MemoryActivity extends AppCompatActivity {
    private ActivityMemoryBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemoryBinding.inflate(getLayoutInflater());
        View view  =binding.getRoot();
        setContentView(view);

        registerLauncher();
        database = this.openOrCreateDatabase("Memories",MODE_PRIVATE,null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.matches("new")){
            //new memory
            binding.memoryText.setText("");
            binding.dateText.setText("");
            binding.button.setVisibility(View.VISIBLE);


            //binding.imageView.setImageResource(R.drawable.selectimage);

        }else {
            int memoryId = intent.getIntExtra("memoryId",0);
            binding.button.setVisibility(View.INVISIBLE);

            try{
                Cursor cursor = database.rawQuery("SELECT * FROM memories WHERE id = ?",new String[] {String.valueOf(memoryId)});
                int memoryNameIx = cursor.getColumnIndex("memoryname");
                int dateIx = cursor.getColumnIndex("date");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    binding.memoryText.setText(cursor.getString(memoryNameIx));
                    binding.dateText.setText(cursor.getString(dateIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }

                cursor.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void save(View view){

        String memoryName = binding.memoryText.getText().toString();
        String date = binding.dateText.getText().toString();
        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY,memoryname VARCHAR, date VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO memories (memoryname, date, image) VALUES (?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,memoryName);
            sqLiteStatement.bindString(2,date);
            sqLiteStatement.bindBlob(3,byteArray);
            sqLiteStatement.execute();


        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(MemoryActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//close all other activities and start this one
        startActivity(intent);


    }

    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio =(float) width/(float) height;

        if(bitmapRatio>1){
            //Landscape image
            width=maximumSize;
            height=(int) (width/bitmapRatio);
        }else{
            //portrait image
            height=maximumSize;
            width=(int)(height*bitmapRatio);
        }
        return image.createScaledBitmap(image,width,height,true);

    }

    public void selectImage(View view){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Android 33+ **Read_media_images
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){

                    Snackbar.make(view,"Permisson needed for gallery!", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();

                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }

            }else{
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }


        }else {
            //Android 32- **Read External Storage
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                    Snackbar.make(view,"Permisson need for gallery!", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();

                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }


            }else{
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                activityResultLauncher.launch(intentToGallery);

            }
        }

    }

    private void registerLauncher(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult !=null){
                        Uri imageData = intentFromResult.getData();
                        //binding.imageView.setImageURI(imageData);

                        try {
                            if(Build.VERSION.SDK_INT >=28){
                                ImageDecoder.Source source = ImageDecoder.createSource(MemoryActivity.this.getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            }else{
                                selectedImage = MediaStore.Images.Media.getBitmap(MemoryActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }


                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }else {
                    //permission denied
                    Toast.makeText(MemoryActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}