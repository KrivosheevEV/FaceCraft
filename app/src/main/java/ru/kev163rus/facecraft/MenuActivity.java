package ru.kev163rus.facecraft;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MenuActivity extends Activity implements View.OnClickListener  {

    private final int CAMERA_RESULT = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView imageViewUserPhoto;
    private ImageButton imageButtomSharePhoto;
    String mCurrentPhotoPath;
    Bitmap bitmapCurrentUserPhoto, bitmapNewUserFace, bitmapResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu);

        Button buttonExit_Result = (Button) findViewById(R.id.buttonCreatePhoto);
        buttonExit_Result.setOnClickListener(this);

        imageViewUserPhoto = (ImageView) findViewById(R.id.imageViewUserPhoto);

        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(20);
        numberPicker.setMinValue(0);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                createNewUserFace(mCurrentPhotoPath, newVal);
            }
        });

        imageButtomSharePhoto = (ImageButton) findViewById(R.id.imageButtonCopyPhoto);
        imageButtomSharePhoto.setOnClickListener(this);

        bitmapResult = BitmapFactory.decodeResource(getResources(), R.drawable.stivehd_emptyhead).copy(Bitmap.Config.ARGB_8888, true);
        imageViewUserPhoto.setImageBitmap(bitmapResult);

        mCurrentPhotoPath = "/storage/sdcard1/PICTURES/JPEG_20160104_172232.jpg";
        createNewUserFace(mCurrentPhotoPath, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            createNewUserFace(mCurrentPhotoPath, 0);
        }
    }

    private void createNewUserFace(String givenPhotoPath, int givenUserDelta){//Bitmap givenCurrentUserPhoto){

        if (givenPhotoPath == null) return;

        File imageFile = new File(givenPhotoPath);
        if (imageFile.exists()) {
            bitmapCurrentUserPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath);
        }else{
            return;
        }

        bitmapResult = BitmapFactory.decodeResource(getResources(), R.drawable.stivehd_emptyhead).copy(Bitmap.Config.ARGB_8888, true);

        int countWidth, countHeight, stepWidth, stepHeight, pixelColor, scalledWidth, scalledHeight;

        int widthOfHead = (bitmapResult.getWidth() / 2);
        int widthOfPixel = (widthOfHead / 16) + (givenUserDelta * 2);

        stepWidth = widthOfPixel;
        stepHeight = widthOfPixel;

        scalledWidth = widthOfHead;
        scalledHeight = widthOfHead;
        bitmapNewUserFace = Bitmap.createScaledBitmap(bitmapCurrentUserPhoto, scalledWidth, scalledHeight, false);

        pixelColor = bitmapNewUserFace.getPixel(1,1);
        for (countWidth = 1; countWidth < scalledWidth; countWidth++){
            for (countHeight = 1; countHeight < scalledHeight; countHeight++){
                if (countWidth % stepWidth == 0 || countHeight % stepHeight == 0){
                    pixelColor = bitmapNewUserFace.getPixel(stepWidth * (countWidth/stepWidth), stepHeight * (countHeight/stepHeight));
                }
                bitmapNewUserFace.setPixel(countWidth, countHeight, pixelColor);
            }

        }

        Canvas c = new Canvas(bitmapResult);
        c.drawBitmap(bitmapResult, 0, 0, new Paint());
        int leftPosotoinSecondImage = (bitmapResult.getWidth() / 2) - (widthOfHead / 2);
        c.drawBitmap(bitmapNewUserFace, leftPosotoinSecondImage, 0, new Paint());

        imageViewUserPhoto.setImageBitmap(bitmapResult);

        bitmapCurrentUserPhoto.recycle();
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

    private void saveUserPhoto(String givenUserPhotoPath){

        if (bitmapResult == null) return;

        String NewFullPath = createNewFullPath();

        try {
            File picFile = new File(NewFullPath);
            FileOutputStream picOut = new FileOutputStream(picFile);
            boolean DoIt = bitmapResult.compress(Bitmap.CompressFormat.PNG, 100, picOut);
            if (DoIt) {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.toastImageSaveIn) + NewFullPath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Whoops! Image not saved.", Toast.LENGTH_LONG)
                        .show();
            }
            picOut.flush();
            picOut.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG)
                    .show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG)
                    .show();
        }

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

        String FullPath = createNewFullPath();

        File image = new File(FullPath);

        return image;
    }

    private String createNewFullPath(){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FACECRAFT_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // For ChinePhones.
        if (!storageDir.exists() || !storageDir.canWrite()){
            String newPath;
            if (storageDir.getAbsolutePath().contains("sdcard0")){
                newPath = storageDir.getAbsolutePath().replace("sdcard0","sdcard1");
            }else{
                newPath = storageDir.getAbsolutePath().replace("sdcard1","sdcard0");
            }

            storageDir = new File(newPath);
        }

        String result = storageDir.getAbsolutePath() + "/" + imageFileName;
        return result;

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonCreatePhoto:
                dispatchTakePictureIntent();
                break;
            case R.id.imageButtonCopyPhoto:
                saveUserPhoto(mCurrentPhotoPath);
                break;
            default:
                finish();
                System.exit(0);
                break;
        }

    }


}
