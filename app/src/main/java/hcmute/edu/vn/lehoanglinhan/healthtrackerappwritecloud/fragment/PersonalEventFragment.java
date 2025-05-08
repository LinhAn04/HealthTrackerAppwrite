package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database.AppDatabase;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Event;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view.EventAdapter;

public class PersonalEventFragment extends Fragment {
    private EditText eventStartDate, eventStartTime, eventEndDate, eventEndTime;
    AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();  // Executor để chạy background task

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personal_event, container, false);
        FloatingActionButton btnAddEvent = view.findViewById(R.id.btn_add_event);
        btnAddEvent.setOnClickListener(v -> themSuKien());
        db = AppDatabase.getInstance(getContext());
        recyclerView = view.findViewById(R.id.recycler_personal_events);

        // Set up RecyclerView with LinearLayoutManager and Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter();
        recyclerView.setAdapter(eventAdapter);
        db.eventDao().getPersonalEvents().observe(getViewLifecycleOwner(), events -> {
            // Update adapter with new data
            eventAdapter.setEventList(events);
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
                    newEvent.startTime = startDate + " " + startTime;
                    newEvent.endTime = endDate + " " + endTime;
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
}
