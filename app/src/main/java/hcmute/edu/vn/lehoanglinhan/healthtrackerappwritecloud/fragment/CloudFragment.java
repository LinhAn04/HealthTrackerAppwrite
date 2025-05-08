package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter.FileAdapter;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.controlelr.Appwrite;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view.FilePreviewActivity;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view.MainActivity;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view.UploadFileActivity;
import io.appwrite.models.File;

public class CloudFragment extends Fragment {

    private static final String BUCKET_ID = "6815d8f00034f84ff79b";
    private RecyclerView rvUploadedFiles;
    private Spinner spinnerFilter;
    private EditText etSearch;
    private AppCompatButton logoutButton;
    private AppCompatButton uploadFileButton;
    private FileAdapter fileAdapter;
    private List<File> allFiles = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_uploaded_files, container, false);

        // ❗ Khởi tạo Appwrite trước khi sử dụng
        Appwrite.init(requireContext());

        // Ánh xạ views
        rvUploadedFiles = view.findViewById(R.id.rv_uploaded_files);
        spinnerFilter = view.findViewById(R.id.spinner_filter);
        etSearch = view.findViewById(R.id.et_search);
        logoutButton = view.findViewById(R.id.logoutButton);
        uploadFileButton = view.findViewById(R.id.uploadFileButton);

        // Thiết lập RecyclerView
        rvUploadedFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        fileAdapter = new FileAdapter(
                new ArrayList<>(),
                // Xử lý click vào item để xem trước
                file -> {
                    Intent intent = new Intent(requireContext(), FilePreviewActivity.class);
                    intent.putExtra("fileId", file.getId());
                    intent.putExtra("fileName", file.getName());
                    startActivity(intent);
                },
                // Xử lý xóa file
                file -> Appwrite.deleteFile(BUCKET_ID, file.getId(), (result, error) -> {
                    if (error != null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Xóa file thất bại", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Đã xóa file", Toast.LENGTH_SHORT).show();
                        loadFiles();
                    });
                })
        );
        rvUploadedFiles.setAdapter(fileAdapter);

        // Load file ban đầu
        loadFiles();

        // Spinner filter xử lý tại đây (tuỳ chỉnh nếu cần)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.file_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterFiles();
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFiles();
            }
            public void afterTextChanged(Editable s) {}
        });

        // Nút logout
        logoutButton.setOnClickListener(v -> {
            Appwrite.onLogout((result, error) -> {
                requireActivity().runOnUiThread(() -> {
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                });
            });
        });

        // Nút upload file
        uploadFileButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), UploadFileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadFiles() {
        Appwrite.listFiles(BUCKET_ID, (files, error) -> {
            if (error != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi tải file", Toast.LENGTH_SHORT).show());
                return;
            }
            if (files != null) {
                allFiles = files;
                requireActivity().runOnUiThread(this::filterFiles);
            }
        });
    }

    private void filterFiles() {
        String keyword = etSearch.getText().toString().trim().toLowerCase();
        String selectedFilter = spinnerFilter.getSelectedItem().toString();

        List<File> filtered = new ArrayList<>();
        for (File file : allFiles) {
            boolean matchName = file.getName().toLowerCase().contains(keyword);
            boolean matchType = selectedFilter.equals("Tất cả") ||
                    file.getName().toLowerCase().endsWith(selectedFilter.toLowerCase());
            if (matchName && matchType) {
                filtered.add(file);
            }
        }
        fileAdapter.updateFiles(filtered);
    }
}
