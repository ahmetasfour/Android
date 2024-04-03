package com.example.vize;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        Button signInButton = findViewById(R.id.buttonSignIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate email and password inputs
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (!isValidEmail(email)) {
                    editTextEmail.setError("Invalid email");
                    editTextEmail.requestFocus();
                    return;
                }

                if (!isValidPassword(password)) {
                    editTextPassword.setError("Invalid password: Password must be at least 6 characters long and contain at least one uppercase letter, one lowercase letter, and one digit");
                    editTextPassword.requestFocus();


                    return;
                }

                // If inputs are valid, proceed to HelloActivity
                Intent intent = new Intent(MainActivity.this, HelloActivity.class);
                startActivity(intent);
            }
        });
    }

    // Email validation method
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Password validation method
    private boolean isValidPassword(String password) {
        // Define your password validation rules here
        // For example, minimum length of 6 characters, at least one uppercase letter, one lowercase letter, and one digit
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$";
        return password.matches(passwordRegex);
    }
}
