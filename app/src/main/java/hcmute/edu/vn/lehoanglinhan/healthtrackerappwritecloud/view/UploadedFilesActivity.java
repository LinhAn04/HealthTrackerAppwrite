package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.controlelr.Appwrite;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter.FileAdapter;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import io.appwrite.models.File;

public class UploadedFilesActivity extends AppCompatActivity {

    private static final String BUCKET_ID = "6815d8f00034f84ff79b";
    private RecyclerView rvUploadedFiles;
    private Spinner spinnerFilter;
    private EditText etSearch;
    private AppCompatButton logoutButton;
    private AppCompatButton uploadFileButton;
    private FileAdapter fileAdapter;
    private List<File> allFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_files);

        // Ánh xạ views
        rvUploadedFiles = findViewById(R.id.rv_uploaded_files);
        spinnerFilter = findViewById(R.id.spinner_filter);
        etSearch = findViewById(R.id.et_search);
        logoutButton = findViewById(R.id.logoutButton);
        uploadFileButton = findViewById(R.id.uploadFileButton);

        // Thiết lập RecyclerView
        rvUploadedFiles.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(
                new ArrayList<>(),
                // OnFileClickListener: Bấm item để xem trước
                file -> {
                    Intent intent = new Intent(UploadedFilesActivity.this, FilePreviewActivity.class);
                    intent.putExtra("fileId", file.getId());
                    intent.putExtra("fileName", file.getName());
                    intent.putExtra("mimeType", file.getMimeType());
                    startActivity(intent);
                },
                // OnFileDeleteListener: Xóa tệp
                file -> runOnUiThread(() -> Toast.makeText(this, "Đã xóa tệp: " + file.getName(), Toast.LENGTH_SHORT).show())
        );
        rvUploadedFiles.setAdapter(fileAdapter);

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.file_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        // Xử lý đăng xuất
        logoutButton.setOnClickListener(v -> {
            Appwrite.onLogout((result, error) -> {
                runOnUiThread(() -> {
                    if (error != null) {
                        Toast.makeText(this, "Đăng xuất thất bại: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UploadedFilesActivity.this, MainActivity.class));
                    finish();
                });
            });
        });

        // Xử lý chuyển sang UploadFileActivity
        uploadFileButton.setOnClickListener(v -> {
            Log.e("DEBUG", "selectFileButton = " + uploadFileButton);  // nếu null -> sai ID

            startActivity(new Intent(UploadedFilesActivity.this, UploadFileActivity.class));
        });

        // Lấy danh sách tệp
        loadFiles();

        // Xử lý bộ lọc
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterFiles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Xử lý tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterFiles();
            }
        });
    }

    private void loadFiles() {
        Appwrite.listFiles(BUCKET_ID, (result, error) -> {
            runOnUiThread(() -> {
                if (error != null) {
                    String errorMessage = error.getMessage();
                    if (errorMessage != null && errorMessage.contains("Storage bucket with the requested ID could not be found")) {
                        Toast.makeText(this, "Không tìm thấy bucket lưu trữ. Vui lòng kiểm tra Bucket ID: " + BUCKET_ID, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Lấy danh sách tệp thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                    allFiles = new ArrayList<>();
                    fileAdapter.updateFiles(allFiles);
                    return;
                }
                allFiles = result;
                fileAdapter.updateFiles(allFiles);
                Toast.makeText(this, "Đã tải " + allFiles.size() + " tệp", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void filterFiles() {
        String query = etSearch.getText().toString().toLowerCase();
        String category = spinnerFilter.getSelectedItem().toString();
        List<File> filteredFiles = new ArrayList<>();

        for (File file : allFiles) {
            boolean matchesCategory = category.equals("Tất cả") ||
                    (category.equals("Ảnh") && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) ||
                    (category.equals("Video") && file.getName().endsWith(".mp4")) ||
                    (category.equals("PDF") && file.getName().endsWith(".pdf"));
            boolean matchesQuery = file.getName().toLowerCase().contains(query);

            if (matchesCategory && matchesQuery) {
                filteredFiles.add(file);
            }
        }

        fileAdapter.updateFiles(filteredFiles);
    }
}