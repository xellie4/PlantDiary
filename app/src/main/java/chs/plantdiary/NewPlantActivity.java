package chs.plantdiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

        chooseImageButton = findViewById(R.id.chooseimagebutton);
        saveButton = findViewById(R.id.savebutton);
        captureImage = findViewById(R.id.takepicture);

        editTextPlantName = findViewById(R.id.nameDataText);
        editTextSun = findViewById(R.id.sunDataText);
        editTextWater = findViewById(R.id.waterDataText);
        editTextTemp = findViewById(R.id.tempDataText);
        editTextFertilizer = findViewById(R.id.fertilizerDataText);
        editTextSoil = findViewById(R.id.soilDataText);

        progressBar = findViewById(R.id.progressbar);
        imageView = findViewById(R.id.imageview);

        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        String uId = currentFirebaseUser.getUid().toString();

        // le pune dupa aia pe toate (pozele) in primul plantIdStorage -> nu e ok, ar trebui sa puna doar pozele de la aceeasi planta in folderu asta uploads/uid/plantidstorage
        //String plantIdStorage = UUID.randomUUID().toString();
        //mStorageRef = FirebaseStorage.getInstance().getReference("uploads/" + uId + "/" + plantIdStorage + "/");

        // creates a new folder in uploads for each user
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads/" + uId + "/");
        mDataBaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        captureImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(NewPlantActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
            }
        });
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            File f = new File(currentPhotoPath);
            imageUri = Uri.fromFile(f);
            //filePath = imageUri.getPath();
            //filePath = data.getData();
            imageView.setImageURI(imageUri);
        }

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            imageView.setImageURI(imageUri);
        }
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

    private void uploadFile(){
        //String extension = "";
        if(imageUri == null && photoFile == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        } else if (imageUri == null) {
            imageUri = Uri.fromFile(photoFile);
            //extension = photoFile.getAbsolutePath().substring(photoFile.getAbsolutePath().lastIndexOf("."));
        }
        /*else if(photoFile == null){
            extension =   "." + getFileExtension(imageUri);
        }
         */
        //extension = photoFile.getAbsolutePath().substring(photoFile.getAbsolutePath().lastIndexOf("."));
        //StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + extension);
        StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + "plantdiary");
                //StorageReference fileRef = mStorageRef.child(UUID.randomUUID().toString());
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
}