package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.controlelr.Appwrite;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.enums.OAuthProvider;
import io.appwrite.services.Account;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageButton btnGoogleLogin;

    List<String> scopes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        scopes = Arrays.asList("https://www.googleapis.com/auth/fitness.activity.read");
        scopes = Arrays.asList("https://www.googleapis.com/auth/fitness.location.read");

        Appwrite.init(getApplicationContext());
        checkCurrentSession();

        btnGoogleLogin = findViewById(R.id.btn_google_login);

        btnGoogleLogin.setOnClickListener(v -> {
            Account account = Appwrite.getAccount();

            try {
                account.createOAuth2Session(
                        MainActivity.this,
                        OAuthProvider.GOOGLE,
                        null,
                        null,
                        scopes,
                        new CoroutineCallback<>((result, error) -> {
                            if (error != null) {
                                runOnUiThread(() -> Toast.makeText(this, "Lỗi đăng nhập Google: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                                return;
                            }
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, UploadedFilesActivity.class));
                                finish();
                            });
                        })
                );
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi Google login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkCurrentSession() {
        Appwrite.checkCurrentSession((session, error) -> {
            if (session != null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã đăng nhập, chuyển đến màn hình chính", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, UploadedFilesActivity.class));
                    finish();
                });
            }
        });
    }
}