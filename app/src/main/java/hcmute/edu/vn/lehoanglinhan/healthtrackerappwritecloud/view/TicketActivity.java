package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter.TicketAdapter;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database.AppDatabase;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Ticket;

public class TicketActivity extends AppCompatActivity {
    AppDatabase db;
    private RecyclerView recyclerView;
    private TicketAdapter ticketAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        db = AppDatabase.getInstance(getApplicationContext());
        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton btnAddTicket = findViewById(R.id.btn_add_ticket);
        int eventId = getIntent().getIntExtra("event_id", -1); // -1 là giá trị mặc định nếu không có dữ liệu
        String title = getIntent().getStringExtra("event_title");
        String description = getIntent().getStringExtra("event_description");
        String location = getIntent().getStringExtra("event_location");
        String startTime = getIntent().getStringExtra("event_start");
        String endTime = getIntent().getStringExtra("event_end");
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        btnAddTicket.setOnClickListener(v -> themTicket(account, eventId, title,description,location,startTime,endTime));


        // Lấy danh sách vé từ cơ sở dữ liệu và cập nhật giao diện
        LiveData<List<Ticket>> tickets = db.ticketDAO().getTicketsByEventId(eventId);
        tickets.observe(this, tickets1 -> {
            // Cập nhật dữ liệu cho RecyclerView
            ticketAdapter = new TicketAdapter(tickets1);
            recyclerView.setAdapter(ticketAdapter);
        });
    }
    private void themTicket(GoogleSignInAccount account, int eventId,
                            String title, String description, String location,
                            String startTime, String endTime) {
        // Tạo một AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_ticket, null);

        // Khởi tạo các thành phần trong dialog
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        Button btnSaveTicket = view.findViewById(R.id.btnSaveTicket);

        builder.setView(view)
                .setCancelable(true)  // Cho phép đóng dialog khi nhấn ngoài
                .setTitle("Thêm vé tham dự");

        // Tạo dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Lắng nghe sự kiện khi nhấn nút "Save Ticket"
        btnSaveTicket.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String emailText = edtEmail.getText().toString().trim();

            if (name.isEmpty() || emailText.isEmpty()) {
                Toast.makeText(TicketActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                // Lưu vé vào cơ sở dữ liệu
                Ticket ticket = new Ticket(eventId, name, emailText);
                new Thread(() -> {
                    db.ticketDAO().insertTicket(ticket);
                    // Gửi email xác nhận
                    sendEmail(account, name, emailText, title, description, location, startTime, endTime);
                    finish();
                    runOnUiThread(() -> {
                        Toast.makeText(TicketActivity.this, "Đã lưu vé!", Toast.LENGTH_SHORT).show();
                    });
                }).start();
                dialog.dismiss(); // Đóng dialog khi đã lưu vé
            }
        });
    }

    private void sendEmail(GoogleSignInAccount account, String attendeeName, String attendeeEmail,
                           String title, String description, String location,
                           String startTime, String endTime) {
        new Thread(() -> {
            try {
                Gmail service = new Gmail.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleAccountCredential.usingOAuth2(
                                getApplicationContext(),
                                java.util.Collections.singleton("https://www.googleapis.com/auth/gmail.send")
                        ).setSelectedAccount(account.getAccount())
                ).setApplicationName("Health Tracker").build();

                String subject = "Xác nhận vé tham dự sự kiện: " + title;
                String body = "Chào " + attendeeName + ",\n\n"
                        + "Bạn đã đăng ký thành công vé tham dự sự kiện:\n\n"
                        + "📌 Tên sự kiện: " + title + "\n"
                        + "📝 Mô tả: " + description + "\n"
                        + "📍 Địa điểm: " + location + "\n"
                        + "🕒 Thời gian: " + startTime + " đến " + endTime + "\n\n"
                        + "👤 Người tham dự: " + attendeeName + "\n"
                        + "✉️ Email: " + attendeeEmail + "\n\n"
                        + "Cảm ơn bạn đã đăng ký!\n"
                        + "Trân trọng.";

                MimeMessage email = createEmail(
                        attendeeEmail, account.getEmail(), subject, body);

                Message message = createMessageWithEmail(email);
                service.users().messages().send("me", message).execute();

                Log.d("EMAIL", "✅ Gửi email thành công");

            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), 2000);
            } catch (Exception e) {
                Log.e("EMAIL", "❌ Lỗi gửi email", e);
            }
        }).start();
    }


    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
