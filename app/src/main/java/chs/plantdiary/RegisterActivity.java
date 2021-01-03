package chs.plantdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button registerButton;
    private Button goToLoginButton;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();

        username = (EditText) findViewById(R.id.username_editText);
        password = (EditText) findViewById(R.id.password_editText);
        registerButton = (Button) findViewById(R.id.registerButton);
        goToLoginButton = (Button) findViewById(R.id.gotologinbutton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = username.getText().toString();
                String pass = password.getText().toString();

                if(email.isEmpty() && pass.isEmpty()){
                    Toast toast = Toast.makeText(RegisterActivity.this, "Fields are empty!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if(email.isEmpty()){
                    username.setError("Please provide a username");
                    username.requestFocus();
                }

                if(pass.isEmpty()){
                    password.setError("Please provide a password");
                    password.requestFocus();
                }

                if(!email.isEmpty() && !pass.isEmpty()){
                    mFirebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(RegisterActivity.this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Toast toast = Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                    else {
                                        Toast toast = Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT);
                                        toast.show();

                                        Intent goToLoggedInActivity = new Intent(RegisterActivity.this, HomeActivity.class);
                                        startActivity(goToLoggedInActivity);
                                    }
                                }
                            });
                }
                else{
                    Toast toast = Toast.makeText(RegisterActivity.this, "Error Occurred! Please, try again!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToLogin = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(goToLogin);
            }
        });
    }
}