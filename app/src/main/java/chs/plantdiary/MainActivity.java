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

/* login activity is the initial screen */
public class MainActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button loginButton;
    private Button goToRegisterButton;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        username = (EditText) findViewById(R.id.username_editText);
        password = (EditText) findViewById(R.id.password_editText);
        loginButton = (Button) findViewById(R.id.loginButton);
        goToRegisterButton = (Button) findViewById(R.id.gotoregisterbutton);

        /* verifica daca user-ul e deja logat, daca da -> redirectionat catre homepage layout */
        if (mFirebaseAuth.getCurrentUser() != null){
            Toast toast = Toast.makeText(MainActivity.this, "You are already logged in!", Toast.LENGTH_SHORT);
            toast.show();

            Intent goToLoggedInActivity = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(goToLoggedInActivity);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = username.getText().toString();
                String pass = password.getText().toString();

                if(email.isEmpty() && pass.isEmpty()){
                    username.setError("Please provide an email address");
                    username.requestFocus();
                    password.setError("Please provide a password");
                    password.requestFocus();
                    Toast toast = Toast.makeText(MainActivity.this, "Fields are empty!", Toast.LENGTH_SHORT);
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
                    mFirebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast toast = Toast.makeText(MainActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT);
                                toast.show();
                                Intent goToLoggedInActivity = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(goToLoggedInActivity);
                            }
                            else {
                                Toast toast = Toast.makeText(MainActivity.this, "Login failed, please try again!", Toast.LENGTH_SHORT);
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
                Intent goToRegister = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(goToRegister);
            }
        });
    }
}
