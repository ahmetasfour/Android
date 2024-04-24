package com.example.vize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.app.DatePickerDialog;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private int userAge = -1;
    private EditText editTextPassword;
    private Button signUpButton;
    private TextView signInTextView; // TextView for navigating to the login page
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this layout has the TextView with id `textViewSignIn`

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        signUpButton = findViewById(R.id.buttonSignUp);
        signInTextView = findViewById(R.id.textViewSignIn); // Connect the TextView

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginActivity();
            }
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            editTextEmail.setError("Invalid email");
            return;
        }

        if (!isValidPassword(password)) {
            editTextPassword.setError("Invalid password. Must be at least 8 characters, with one uppercase, one lowercase, one digit, and one special character.");
            return;
        }

        if (userAge == -1) {
            Toast.makeText(MainActivity.this, "Please select your birthdate", Toast.LENGTH_LONG).show();
            return;
        }



        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Sign up failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,}$";
        return password.matches(passwordRegex);
    }
    public void showDatePickerDialog(View v) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar dob = Calendar.getInstance();
                    dob.set(year, month, dayOfMonth);
                    Calendar today = Calendar.getInstance();
                    userAge = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                        userAge--;  // Adjust age if birthday hasn't occurred yet this year
                    }
                    // Optional: Update a TextView to display selected age
                    TextView ageDisplay = findViewById(R.id.textViewAge);
                    ageDisplay.setText("Age: " + userAge);
                }, 2000, 0, 1); // Default date can be adjusted as needed
        datePickerDialog.show();
    }


}
