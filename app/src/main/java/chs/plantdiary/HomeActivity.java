package chs.plantdiary;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

//homepage cu butoanele: add new plant, gallery, watering calendar si scan for available devices
public class HomeActivity extends AppCompatActivity {
    private Button addNewPlantButton;
    private Button galleryButton;
    private Button wateringCalendarButton;
    private Button logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        addNewPlantButton = (Button) findViewById(R.id.cb1);
        galleryButton = (Button) findViewById(R.id.cb2);
        wateringCalendarButton = (Button) findViewById(R.id.cb3);
        logOutButton = (Button) findViewById(R.id.logout_button);


        addNewPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAddNewPlant = new Intent(HomeActivity.this, NewPlantActivity.class);
                startActivity(goToAddNewPlant);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToHomePage = new Intent(HomeActivity.this, GalleryActivity.class);
                startActivity(goToHomePage);
            }
        });

        wateringCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToHomePage = new Intent(HomeActivity.this, WaterPlantActivity.class);
                startActivity(goToHomePage);
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent goToLogin = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(goToLogin);
            }
        });
    }
}
