package com.example.android.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.android.R;
import com.example.android.databinding.ActivitySignInBinding;
import com.example.android.firebase.Constants;
import com.example.android.firebase.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        Eventhandling();
    }
    public void Eventhandling()
    {
        binding.textCreateNewAccount.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnSignIn.setOnClickListener(v -> {
            if (Kiemtra())
            {
                SignIn();
            }
        });
    }
    public void SignIn()
    {
        Progesbar(true);
        database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
                    {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                        preferenceManager.putString(Constants.KEY_SURNAME, preferenceManager.getString(Constants.KEY_SURNAME));
                        preferenceManager.putString(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                        preferenceManager.putString(Constants.KEY_PHONE, preferenceManager.getString(Constants.KEY_PHONE));
                        preferenceManager.putString(Constants.KEY_DATE, preferenceManager.getString(Constants.KEY_DATE));
                        preferenceManager.putString(Constants.KEY_PASSWORD_SAVE, preferenceManager.getString(Constants.KEY_PASSWORD_SAVE));
                        preferenceManager.putString(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }
                    else
                    {
                        Progesbar(false);
                        Thongbao("Lỗi trong việc đăng nhập");
                    }
                });

    }
    public Boolean Kiemtra()
    {
        if (binding.inputEmail.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập vào email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            Thongbao("Vui lòng nhập vào đúng địng dạng email");
            return false;
        }
        else if (binding.inputPassword.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập vào mật khẩu");
            return false;
        }
        else
        {
            return true;
        }
    }
    public void Progesbar(Boolean loading)
    {
        if (loading)
        {
            binding.progesbar.setVisibility(View.VISIBLE);
            binding.btnSignIn.setVisibility(View.INVISIBLE);
        }
        else
        {
            binding.progesbar.setVisibility(View.INVISIBLE);
            binding.btnSignIn.setVisibility(View.VISIBLE);
        }
    }
    public void Thongbao(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}