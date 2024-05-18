package com.example.android.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.android.databinding.ActivityMainBinding;
import com.example.android.firebase.Constants;
import com.example.android.firebase.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        loadUserDetails();

        getToken();

        Out();
    }

    private void loadUserDetails()
    {
        binding.inputName.setText(preferenceManager.getString(Constants.KEY_NAME));

        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token)
    {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));


        documentReference.update(Constants.KEY_TOKE, token)
                .addOnSuccessListener(command -> Thongbao("Bạn đã đăng nhập"))
                .addOnFailureListener(command -> Thongbao("Không thể cập nhật mã thông báo"));
    }
    private void Thongbao(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void Out()
    {
        Thongbao("Thoát.....");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> updates = new HashMap<>();

        updates.put(Constants.KEY_TOKE, FieldValue.delete());

        documentReference.update(updates)
                .addOnSuccessListener(command -> {
                    preferenceManager.clear();

                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    startActivity(intent);
                    finish();

                })
                .addOnFailureListener(command -> {
                    Thongbao("Không thể đăng xuất");
                });
    }
}