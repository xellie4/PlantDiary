package chs.plantdiary;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class HomeActivity extends AppCompatActivity {
    private Button addNewPlantButton;
    private Button galleryButton;
    private Button wateringScheduleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        addNewPlantButton = (Button) findViewById(R.id.cb1);
        galleryButton = (Button) findViewById(R.id.cb2);
        wateringScheduleButton = (Button) findViewById(R.id.cb3);

        addNewPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAddNewPlant = new Intent(HomeActivity.this, NewPlantActivity.class);
                startActivity(goToAddNewPlant);
            }
        });
        /*
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToHomePage = new Intent(LoginActivity.this, HomePageActivity.class);
                startActivity(goToHomePage);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToHomePage = new Intent(LoginActivity.this, HomePageActivity.class);
                startActivity(goToHomePage);
            }
        });*/
    }
}
