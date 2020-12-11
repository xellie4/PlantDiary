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

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button loginButton;
    private Button goToRegisterButton;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();

        username = (EditText) findViewById(R.id.username_editText);
        password = (EditText) findViewById(R.id.password_editText);
        loginButton = (Button) findViewById(R.id.loginButton);
        goToRegisterButton = (Button) findViewById(R.id.gotoregisterbutton);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser != null){
                    Toast toast = Toast.makeText(LoginActivity.this, "You logged in!", Toast.LENGTH_SHORT);
                    toast.show();
                    Intent goToLoggedInActivity = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(goToLoggedInActivity);
                }
                /*
                else{
                    Toast toast = Toast.makeText(MainActivity.this, "Something Went Wrong!!!", Toast.LENGTH_SHORT);
                    toast.show();
                }*/
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = username.getText().toString();
                String pass = password.getText().toString();
                if(email.isEmpty() && pass.isEmpty()){
                    Toast toast = Toast.makeText(LoginActivity.this, "Fields are empty!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if(email.isEmpty() && !pass.isEmpty()){
                    username.setError("Please provide an email address");
                    username.requestFocus();
                }
                if(pass.isEmpty() && !email.isEmpty()) {
                    password.setError("Please provide a password");
                    password.requestFocus();
                }
                if(!email.isEmpty() && !pass.isEmpty()){
                    mFirebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast toast = Toast.makeText(LoginActivity.this, "Logged in successful!", Toast.LENGTH_SHORT);
                                toast.show();
                                Intent goToLoggedInActivity = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(goToLoggedInActivity);
                            }
                            else {
                                Toast toast = Toast.makeText(LoginActivity.this, "Login failed, please try again!", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                }
            }
        });

        goToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRegister = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(goToRegister);
            }
        });
    }

    protected void onStart(){
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
