package chs.plantdiary;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

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

        /*issue: even if i rerun the app after logging in and not signing out, i will start on the register page but the account is already logged in,
        so if i press on the register (main) screen: already an user? log in then it doesn't go to login screen, it does log in
         */
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent goToRegister = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(goToRegister);
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
                Intent goToHomePage = new Intent(HomeActivity.this, WateringCalendarActivity.class);
                startActivity(goToHomePage);
            }
        });

    }
}
