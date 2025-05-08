package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database.AppDatabase;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Event;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter.EventAdapter;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view.TicketActivity;

public class OrganizationEventFragment extends Fragment {
    private EditText eventStartDate, eventStartTime, eventEndDate, eventEndTime;
    AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();  // Executor để chạy background task
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organization_event, container, false);
        FloatingActionButton btnAddEvent = view.findViewById(R.id.btn_add_event);
        btnAddEvent.setOnClickListener(v -> themSuKien());
        db = AppDatabase.getInstance(getContext());
        recyclerView = view.findViewById(R.id.recycler_org_events);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter();
        recyclerView.setAdapter(eventAdapter);
        db.eventDao().getOrganizedEvents().observe(getViewLifecycleOwner(), events -> {
            eventAdapter.setEventList(events);
        });
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());

        eventAdapter.setOnItemClickListener(new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                Intent intent = new Intent(getContext(), TicketActivity.class);
                intent.putExtra("event_id", event.id); // truyền ID sự kiện (hoặc các thông tin khác nếu cần)
                intent.putExtra("event_title", event.title);
                intent.putExtra("event_description", event.description);
                intent.putExtra("event_location", event.location);
                intent.putExtra("event_start", event.startTime);
                intent.putExtra("event_end", event.endTime);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Event event, View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Tùy chọn")
                        .setItems(new CharSequence[]{"Cập nhật", "Xóa"}, (dialog, which) -> {
                            if (which == 0) {
                                // Cập nhật
                                hienDialogCapNhat(event);
                            } else if (which == 1) {
                                executorService.execute(() -> {
                                    List<String> emails = db.ticketDAO().getEmailsByEventId(event.getId());

                                    // Gửi mail hủy
                                    sendCancellationEmail(event, emails,account);

                                    // Xóa sự kiện
                                    db.eventDao().delete(event);
                                });
                            }
                        })
                        .show();
            }
        });

        return view;
    }
    private void themSuKien() {
        // Khởi tạo LayoutInflater để tạo view từ file XML
        View dialogView = getLayoutInflater().inflate(R.layout.popup_add_event, null);
        eventStartDate = dialogView.findViewById(R.id.event_start_date);
        eventStartTime = dialogView.findViewById(R.id.event_start_time);
        eventEndDate = dialogView.findViewById(R.id.event_end_date);
        eventEndTime = dialogView.findViewById(R.id.event_end_time);

        eventStartDate.setOnClickListener(v -> showDatePickerDialog(eventStartDate));
        eventStartTime.setOnClickListener(v -> showTimePickerDialog(eventStartTime));
        eventEndDate.setOnClickListener(v -> showDatePickerDialog(eventEndDate));
        eventEndTime.setOnClickListener(v -> showTimePickerDialog(eventEndTime));

        final EditText eventTitle = dialogView.findViewById(R.id.event_title);
        final EditText eventDescription = dialogView.findViewById(R.id.event_description);
        final EditText eventLocation = dialogView.findViewById(R.id.event_location);

        final RadioGroup eventTypeGroup = dialogView.findViewById(R.id.event_type);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Event")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Lấy thông tin sự kiện từ các trường
                    String title = eventTitle.getText().toString();
                    String description = eventDescription.getText().toString();
                    String location = eventLocation.getText().toString();

                    // Sửa lỗi lấy ngày giờ từ DatePicker và TimePicker
                    String startDate = eventStartDate.getText().toString();
                    String startTime = eventStartTime.getText().toString();
                    String endDate = eventEndDate.getText().toString();
                    String endTime = eventEndTime.getText().toString();
                    // Kiểm tra nếu "Personal" được chọn, nếu đúng thì isPersonal = true, ngược lại false
                    boolean isPersonal = ((RadioButton) dialogView.findViewById(eventTypeGroup.getCheckedRadioButtonId()))
                            .getText().toString().equals("Personal");
                    // Tạo đối tượng Event
                    Event newEvent = new Event();
                    newEvent.title = title;
                    newEvent.description = description;
                    newEvent.location = location;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    try {
                        Date startDateTime = sdf.parse(startDate + " " + startTime);
                        Date endDateTime = sdf.parse(endDate + " " + endTime);

                        newEvent.startTime = startDateTime.getTime();
                        newEvent.endTime = endDateTime.getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    newEvent.isPersonal = isPersonal;  // Nếu "Personal" được chọn, isPersonal = true, nếu không = false

                    // Thêm sự kiện vào cơ sở dữ liệu
                    executorService.execute(() -> db.eventDao().insert(newEvent));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    // Chỉnh sửa ngày cho đúng định dạng
                    String date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                    editText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    // Chỉnh sửa giờ phút cho đúng định dạng
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    editText.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }
    private void hienDialogCapNhat(Event event) {
        View dialogView = getLayoutInflater().inflate(R.layout.popup_add_event, null);

        eventStartDate = dialogView.findViewById(R.id.event_start_date);
        eventStartTime = dialogView.findViewById(R.id.event_start_time);
        eventEndDate = dialogView.findViewById(R.id.event_end_date);
        eventEndTime = dialogView.findViewById(R.id.event_end_time);
        final EditText eventTitle = dialogView.findViewById(R.id.event_title);
        final EditText eventDescription = dialogView.findViewById(R.id.event_description);
        final EditText eventLocation = dialogView.findViewById(R.id.event_location);
        final RadioGroup eventTypeGroup = dialogView.findViewById(R.id.event_type);

//        // Tách ngày & giờ từ chuỗi thời gian
//        String[] startParts = event.startTime.split(" ");
//        String[] endParts = event.endTime.split(" ");

//        eventStartDate.setText(startParts[0]);
//        eventStartTime.setText(startParts[1]);
//        eventEndDate.setText(endParts[0]);
//        eventEndTime.setText(endParts[1]);

        eventTitle.setText(event.title);
        eventDescription.setText(event.description);
        eventLocation.setText(event.location);
        ((RadioButton) dialogView.findViewById(event.isPersonal ? R.id.radio_personal : R.id.radio_organization)).setChecked(true);

        eventStartDate.setOnClickListener(v -> showDatePickerDialog(eventStartDate));
        eventStartTime.setOnClickListener(v -> showTimePickerDialog(eventStartTime));
        eventEndDate.setOnClickListener(v -> showDatePickerDialog(eventEndDate));
        eventEndTime.setOnClickListener(v -> showTimePickerDialog(eventEndTime));

        new AlertDialog.Builder(getContext())
                .setTitle("Cập nhật sự kiện")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    event.title = eventTitle.getText().toString();
                    event.description = eventDescription.getText().toString();
                    event.location = eventLocation.getText().toString();
//                    event.startTime = eventStartDate.getText().toString() + " " + eventStartTime.getText().toString();
//                    event.endTime = eventEndDate.getText().toString() + " " + eventEndTime.getText().toString();
                    event.isPersonal = ((RadioButton) dialogView.findViewById(eventTypeGroup.getCheckedRadioButtonId())).getText().toString().equals("Personal");

                    executorService.execute(() -> db.eventDao().update(event));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void sendCancellationEmail(Event event,List<String> attendeeEmails, GoogleSignInAccount account) {

        for (String email : attendeeEmails) {
            new Thread(() -> {
                try {
                    Gmail service = new Gmail.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            GsonFactory.getDefaultInstance(),
                            GoogleAccountCredential.usingOAuth2(
                                    getContext(),
                                    java.util.Collections.singleton("https://www.googleapis.com/auth/gmail.send")
                            ).setSelectedAccount(account.getAccount())
                    ).setApplicationName("Health Tracker").build();

                    String subject = "Thông báo hủy sự kiện: " + event.title;
                    String body = "Kính gửi bạn,\n\n" +
                            "Chúng tôi rất tiếc phải thông báo rằng sự kiện \"" + event.title + "\", dự kiến diễn ra tại " + event.location +
                            " vào lúc " + event.startTime + ", đã bị hủy bỏ vì lý do ngoài ý muốn.\n\n" +
                            "🔹 Mô tả sự kiện: " + event.description + "\n\n" +
                            "Chúng tôi thành thật xin lỗi vì sự bất tiện này và mong nhận được sự cảm thông từ bạn.\n\n" +
                            "Mọi thắc mắc, xin vui lòng liên hệ với chúng tôi để được hỗ trợ.\n\n" +
                            "Trân trọng,\n" +
                            "Ban tổ chức sự kiện";


                    MimeMessage mimeMessage = createEmail(email, account.getEmail(), subject, body);
                    Message message = createMessageWithEmail(mimeMessage);
                    service.users().messages().send("me", message).execute();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), 1001);  // thêm xử lý nếu cần quyền
                } catch (Exception e) {
                    Log.e("Email", "Lỗi gửi mail hủy", e);
                }
            }).start();
        }
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
