package com.example.android.activitys;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.android.R;
import com.example.android.databinding.ActivitySignUpBinding;
import com.example.android.firebase.Constants;
import com.example.android.firebase.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String codedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        Eventhandling();

    }
    public void Eventhandling()
    {
        binding.newLogin.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnSignIn.setOnClickListener(v -> {
            if (Kiemtra())
            {
                SignUp();
            }
        });

        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void SignUp()
    {
        Progesbar(true);
        database = FirebaseFirestore.getInstance();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_SURNAME, binding.inputSurname.getText().toString());
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_PHONE, binding.inputPhone.getText().toString());
        user.put(Constants.KEY_PASSWORD_SAVE, binding.inputPasswordsave.getText().toString());
        user.put(Constants.KEY_DATE, binding.inputDate.getText().toString());
        user.put(Constants.KEY_IMAGE, codedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Progesbar(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                    preferenceManager.putString(Constants.KEY_SURNAME, binding.inputSurname.getText().toString());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                    preferenceManager.putString(Constants.KEY_DATE, binding.inputDate.getText().toString());
                    preferenceManager.putString(Constants.KEY_PHONE, binding.inputPhone.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSWORD_SAVE, binding.inputPasswordsave.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, codedImage);

                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    Thongbao("Đăng kí thành công");
                })
                .addOnFailureListener(e -> {
                    Progesbar(false);
                    Thongbao(e.getMessage());
                });
    }
    public Boolean Kiemtra()
    {
        if (!binding.checkbox.isChecked()) {
            Thongbao("Vui lòng đồng ý điều khoản");
            return false;
        }
        else if (codedImage == null)
        {
            Thongbao("Vui lòng thêm ảnh đại diện");
            return false;
        }
        else if (binding.inputEmail.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập vào email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            Thongbao("Vui lòng nhập vào đúng dạng email");
            return false;
        }
        else if (binding.inputSurname.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập vào định dạng tên");
            return false;
        }
        else if (binding.inputName.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập vào họ tên bạn");
            return false;
        }
        else if (binding.inputPhone.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập vào số điện thoại");
            return false;
        }
        else if (binding.inputDate.getText().toString().isEmpty())
        {
            Thongbao("Ngày không hợp lệ");
            return false;
        }
        else if (binding.inputPassword.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập vào mật khẩu");
            return false;
        }
        else if (binding.inputConfirmPassword.getText().toString().isEmpty())
        {
            Thongbao("Vui lòng nhập lại mật khẩu");
            return false;
        }
        else if (!binding.inputConfirmPassword.getText().toString().equals(binding.inputPassword.getText().toString()))
        {
            Thongbao("Mật khẩu bạn nhập không khớp");
            return false;
        }
        else
        {
            return true;
        }
    }

    public String codeImage(Bitmap bitmap)
    {
        int rong = 150;
        int dai = bitmap.getHeight() * rong / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, rong, dai,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    public final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK)
                {
                    if (result.getData() != null)
                    {
                        Uri imageUri = result.getData().getData();

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.addimage.setVisibility(View.GONE);
                            codedImage = codeImage(bitmap);
                        }catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    public void Progesbar(Boolean loading)
    {
        if (loading)
        {
            binding.progesBar.setVisibility(View.VISIBLE);
            binding.btnSignIn.setVisibility(View.INVISIBLE);
        }
        else
        {
            binding.progesBar.setVisibility(View.INVISIBLE);
            binding.btnSignIn.setVisibility(View.VISIBLE);
        }
    }
    public void Thongbao(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}