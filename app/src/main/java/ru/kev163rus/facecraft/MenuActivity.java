package ru.kev163rus.facecraft;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MenuActivity extends Activity implements View.OnClickListener  {

    private final int CAMERA_RESULT = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView imageViewUserPhoto;
    private SurfaceView surfaceViewUserPhoto;
    String mCurrentPhotoPath;
    private Paint mPaint = new Paint();
    Bitmap bitmapCurrentUserPhoto, bitmapNewUserFace, bitmapResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button buttonExit_Result = (Button) findViewById(R.id.buttonCreatePhoto);
        buttonExit_Result.setOnClickListener(this);

        imageViewUserPhoto = (ImageView) findViewById(R.id.imageViewUserPhoto);
//        surfaceViewUserPhoto = (SurfaceView) findViewById(R.id.surfaceViewUserPhoto);

        bitmapResult = BitmapFactory.decodeResource(getResources(), R.drawable.stivewithemptyhead).copy(Bitmap.Config.ARGB_8888, true);
        imageViewUserPhoto.setImageBitmap(bitmapResult);

//        mCurrentPhotoPath = "/storage/sdcard1/PICTURES/JPEG_20160104_172232.jpg";
//        File imageFile = new File(mCurrentPhotoPath);
//        if (imageFile.exists()) {
//            Bitmap bitmapCurrentUserPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath);
//            createNewUserFace(bitmapCurrentUserPhoto);
//            bitmapCurrentUserPhoto.recycle();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageViewUserPhoto.setImageBitmap(imageBitmap);
//            imageViewUserPhoto.setImageDrawable(Drawable.createFromPath(mCurrentPhotoPath));
            File imageFile = new File(mCurrentPhotoPath);
            if (imageFile.exists()) {
                bitmapCurrentUserPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath);
                createNewUserFace(bitmapCurrentUserPhoto);
                bitmapCurrentUserPhoto.recycle();
            }
        }
    }

    private void createNewUserFace(Bitmap givenCurrentUserPhoto){

        int widthGivenBitmap = givenCurrentUserPhoto.getWidth();
        int heightGivenBitmap = givenCurrentUserPhoto.getHeight();
        int countWidth, countHeight, stepWidth, stepHeight, pixelColor, scalledWidth, scalledHeight;

        stepWidth = 4;
        stepHeight = 4;

        scalledWidth = 90;
        scalledHeight = 90;
        bitmapNewUserFace = Bitmap.createScaledBitmap(givenCurrentUserPhoto, scalledWidth, scalledHeight, false);

        pixelColor = bitmapNewUserFace.getPixel(1,1);
        for (countWidth = 1; countWidth < scalledWidth; countWidth++){
            for (countHeight = 1; countHeight < scalledHeight; countHeight++){
                if (countWidth % stepWidth == 0 || countHeight % stepHeight == 0){
                    pixelColor = bitmapNewUserFace.getPixel(stepWidth * (countWidth/stepWidth), stepHeight * (countHeight/stepHeight));
                }
                bitmapNewUserFace.setPixel(countWidth, countHeight, pixelColor);
            }

        }


        //bitmapResult = BitmapFactory.decodeResource(getResources(), R.drawable.stivewithemptyhead).copy(Bitmap.Config.ARGB_8888, true);

        Canvas c = new Canvas(bitmapResult);
        c.drawBitmap(bitmapResult, 0, 0, new Paint());
        c.drawBitmap(bitmapNewUserFace, 45, 0, new Paint());

        imageViewUserPhoto.setImageBitmap(bitmapResult);

        givenCurrentUserPhoto.recycle();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bitmapNewUserFace != null) {
            bitmapNewUserFace.recycle();
            bitmapNewUserFace = null;
        }
        if (bitmapCurrentUserPhoto != null) {
            bitmapCurrentUserPhoto.recycle();
            bitmapCurrentUserPhoto = null;
        }
        if (bitmapResult != null) {
            bitmapResult.recycle();
            bitmapResult = null;
        }
//        Toast.makeText(getApplicationContext(), "onStop()", Toast.LENGTH_SHORT).show();
    }


    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("Ошибка", "Невозможно создать файл.");
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FACECRAFT_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // For ChinePhones.
        if (!storageDir.exists() || storageDir.canWrite()){
            String newPath = storageDir.getAbsolutePath().replace("sdcard0","sdcard1");
            storageDir = new File(newPath);
        }
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
        File image = new File(storageDir,imageFileName);

        Log.i("Информация", "Путь для сохранения файла: " + storageDir.getAbsolutePath());

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();

        return image;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonCreatePhoto:
                dispatchTakePictureIntent();
                break;
            default:
                finish();
                System.exit(0);
                break;
        }

    }

}
