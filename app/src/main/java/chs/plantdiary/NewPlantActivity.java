package chs.plantdiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import io.grpc.Context;
import java.util.UUID;

public class NewPlantActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 10;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private Button chooseImageButton;
    private Button saveButton;
    private Button captureImage;

    private EditText editTextPlantName;
    private EditText editTextSun;
    private EditText editTextWater;
    private EditText editTextTemp;
    private EditText editTextFertilizer;
    private EditText editTextSoil;
    private ProgressBar progressBar;
    private ImageView imageView;
    private Uri imageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDataBaseRef;
    private StorageTask uploadTask;
    private FirebaseAuth mFirebaseAuth;
    private String filePath;
    private String currentPhotoPath;
    private File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newplant);

        chooseImageButton = (Button) findViewById(R.id.chooseimagebutton);
        saveButton = (Button) findViewById(R.id.savebutton);
        captureImage = (Button) findViewById(R.id.takepicture);

        editTextPlantName = (EditText) findViewById(R.id.nameDataText);
        editTextSun = (EditText)findViewById(R.id.sunDataText);
        editTextWater = (EditText)findViewById(R.id.waterDataText);
        editTextTemp = (EditText)findViewById(R.id.tempDataText);
        editTextFertilizer = (EditText)findViewById(R.id.fertilizerDataText);
        editTextSoil = (EditText)findViewById(R.id.soilDataText);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        imageView = (ImageView) findViewById(R.id.imageview);

        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        String uId = currentFirebaseUser.getUid().toString();

        // creates a new folder in uploads for each user
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads/" + uId + "/");
        mDataBaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        /* buton pentru selectarea imaginii de pe telefon*/
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        /* buton pentru pornire camera */
        captureImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                askCameraPermissions();
            }
        });

        /* salvare in baza de date a pozei si a datelor introduse */
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* cat timp e in progress upload-ul, nu se va intampla nimic daca se apasa save din nou*/
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(NewPlantActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* cazul in care poza e facuta cu camera */
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            File f = new File(currentPhotoPath);
            imageUri = Uri.fromFile(f);
            //filePath = imageUri.getPath();
            //filePath = data.getData();
            imageView.setImageURI(imageUri);
        }

        /* cazul in care poza e luata de pe telefon */
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            imageView.setImageURI(imageUri);
        }
    }

    /* verificare daca exista permisiune pentru folosirea camerei */
    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }else {
            dispatchTakePictureIntent();
        }

    }

    /* daca nu exista permisiune pentru folosirea camerei, se va cere aceasta permisiune  */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                Toast.makeText(this, "Need permission to use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //get extension of file (image)
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "chs.plantdiary.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    /* incarcare in baza de date a pozei si informatiilor */
    private void uploadFile(){

        /* daca nu e selectat nicio imagine */
        if(imageUri == null && photoFile == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        } else if (imageUri == null) {  /* in cazul in care poza e facuta cu camera, imageUri va fi null, dar din calea unde e stocata poza (pe telefon) se poate crea un imageUri */
            imageUri = Uri.fromFile(photoFile);
        }

        /* se genereaza titlul pozei care va fi urcat in baza de date*/
        StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + "plantdiary");

        uploadTask = fileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Handler handler = new Handler();
                        //delay pt reset la progressbar
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(0);
                            }
                        }, 500);

                        Toast.makeText(NewPlantActivity.this, "Upload succesfully", Toast.LENGTH_SHORT).show();

                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;

                                String plantName = editTextPlantName.getText().toString().trim();
                                String sun = editTextSun.getText().toString().trim();
                                String water = editTextWater.getText().toString().trim();
                                String temp = editTextTemp.getText().toString().trim();
                                String fertilizer = editTextFertilizer.getText().toString().trim();
                                String soil = editTextSoil.getText().toString().trim();

                                Plants upload = new Plants(plantName, sun, water, temp, fertilizer, soil, downloadUrl.toString());

                                //String  uploadId = mDataBaseRef.push().getKey();
                                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                                String uId = currentFirebaseUser.getUid().toString();
                                String plantIdDb = UUID.randomUUID().toString();
                                mDataBaseRef.child(uId).child(plantIdDb).setValue(upload);

                                /* sterge datele din edittext si poza dupa ce acestea au fost incarcate in baza de date */
                                clearEditTextsAndImageView();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewPlantActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot snapshot) {
                        //update progress bar
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.setProgress((int)progress);

                    }
                });

    }

    private void clearEditTextsAndImageView(){
        editTextPlantName.setText("");
        editTextSun.setText("");
        editTextWater.setText("");
        editTextTemp.setText("");
        editTextFertilizer.setText("");
        editTextSoil.setText("");

        imageView.setImageDrawable(null);
    }
}