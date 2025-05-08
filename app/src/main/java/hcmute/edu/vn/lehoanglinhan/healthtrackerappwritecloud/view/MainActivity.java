package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.controlelr.Appwrite;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database.AppDatabase;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.enums.OAuthProvider;
import io.appwrite.services.Account;

import java.util.Arrays;
import java.util.List;
import android.Manifest;
public class MainActivity extends AppCompatActivity {
    private ImageButton btnGoogleLogin;

    List<String> scopes;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        MyChannel.createNotificationChannel(this);
        db = AppDatabase.getInstance(this);

        scopes = Arrays.asList("https://www.googleapis.com/auth/fitness.activity.read");
        scopes = Arrays.asList("https://www.googleapis.com/auth/fitness.location.read");

        Appwrite.init(getApplicationContext());
        checkCurrentSession();
//        scheduleNextEventReminder(this);
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
                                Intent intent = new Intent(MainActivity.this, OverviewActivity.class);
                                startActivity(intent);
//                                startActivity(new Intent(MainActivity.this, UploadedFilesActivity.class));
                                finish();
                            });
                        })
                );
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi Google login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Quyền thông báo đã được cấp!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Quyền thông báo chưa được cấp!", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkCurrentSession() {
        Appwrite.checkCurrentSession((session, error) -> {
            if (session != null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã đăng nhập, chuyển đến màn hình chính", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, OverviewActivity.class));
                    finish();
                });
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền thông báo đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền thông báo bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    public void scheduleEventReminder(Context context, String eventTitle, long eventStartTimeMillis) {
//        Intent intent = new Intent(context, EventReminderReceiver.class);
//        intent.putExtra("eventTitle", eventTitle);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        if (alarmManager != null) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, eventStartTimeMillis - 5 * 60 * 1000, pendingIntent); // 5 phút trước sự kiện
//        }
//    }
//    public void scheduleNextEventReminder(Context context) {
//        new Thread(() -> {
//            EventDAO eventDao = db.eventDao();
//            long currentTime = System.currentTimeMillis();
//            List<Event> upcomingEvent = eventDao.getUpcomingEvents(currentTime);
//            if (upcomingEvent != null && !upcomingEvent.isEmpty()) {
//                for (Event event : upcomingEvent) {
//                    scheduleEventReminder(context, event.getTitle(), event.getStartTime());
//                }
//            }
//        }).start();
//    }


}