package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.controlelr.Appwrite;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import io.appwrite.models.InputFile;

public class UploadFileActivity extends AppCompatActivity {

    private static final String BUCKET_ID = "6815d8f00034f84ff79b"; // Bucket ID thực tế
    private Button selectFileButton, uploadButton;
    private TextView fileNameTextView;
    private ImageView previewImageView;
    private Uri selectedFileUri;
    private String selectedFileName;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> fileManagerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        // Ánh xạ views
        selectFileButton = findViewById(R.id.selectFileButton);
        uploadButton = findViewById(R.id.uploadButton);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        previewImageView = findViewById(R.id.previewImageView);

        // Khởi tạo ActivityResultLauncher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedFileUri = result.getData().getData();
                selectedFileName = getFileName(selectedFileUri);
                fileNameTextView.setText(selectedFileName != null ? selectedFileName : "Tệp đã chọn");
                displayFilePreview(selectedFileUri);
            }
        });

        fileManagerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedFileUri = result.getData().getData();
                selectedFileName = getFileName(selectedFileUri);
                fileNameTextView.setText(selectedFileName != null ? selectedFileName : "Tệp đã chọn");
                displayFilePreview(selectedFileUri);
            }
        });

        // Xử lý chọn tệp
        selectFileButton.setOnClickListener(v -> showFilePickerDialog());

        // Xử lý tải lên
        uploadButton.setOnClickListener(v -> {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Vui lòng chọn một tệp trước", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadFile();
        });
    }

    private void showFilePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_file_picker);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnGallery = dialog.findViewById(R.id.btn_select_gallery);
        Button btnFileManager = dialog.findViewById(R.id.btn_select_file_manager);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/* video/*");
                galleryLauncher.launch(intent);
                dialog.dismiss();
            });
        }

        if (btnFileManager != null) {
            btnFileManager.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                fileManagerLauncher.launch(intent);
                dialog.dismiss();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
    }

    private void displayFilePreview(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            Log.d("UploadFile", "MIME type: " + mimeType);

            if (mimeType != null && mimeType.startsWith("image/")) {
                // Hiển thị ảnh
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    if (bitmap != null) {
                        previewImageView.setImageBitmap(bitmap);
                        previewImageView.setVisibility(View.VISIBLE);
                    } else {
                        previewImageView.setVisibility(View.GONE);
                        Log.w("UploadFile", "Không thể giải mã ảnh");
                    }
                } else {
                    previewImageView.setVisibility(View.GONE);
                    Log.w("UploadFile", "InputStream null cho ảnh");
                }
            } else if (mimeType != null && mimeType.startsWith("video/")) {
                previewImageView.setVisibility(View.VISIBLE);
            } else {
                // Ẩn ImageView nếu không phải ảnh/video
                previewImageView.setVisibility(View.GONE);
                Log.d("UploadFile", "Tệp không phải ảnh hoặc video: " + mimeType);
            }
        } catch (Exception e) {
            previewImageView.setVisibility(View.GONE);
            Log.e("UploadFile", "Lỗi hiển thị xem trước: " + e.getMessage(), e);
        }
    }

    private void uploadFile() {
        try {
            // Mở InputStream từ Uri
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            if (inputStream == null) {
                Toast.makeText(this, "Không thể đọc tệp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo tệp tạm trong cache
            File tempFile = new File(getCacheDir(), selectedFileName != null ? selectedFileName : "temp_file_" + System.currentTimeMillis());
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                inputStream.close();
            }

            // Kiểm tra tệp tạm
            if (!tempFile.exists() || tempFile.length() == 0) {
                Toast.makeText(this, "Tệp tạm không hợp lệ", Toast.LENGTH_SHORT).show();
                Log.e("UploadFile", "Tệp tạm không tồn tại hoặc rỗng: " + tempFile.getAbsolutePath());
                return;
            }

            // Lấy MIME type
            String mimeType = getContentResolver().getType(selectedFileUri);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
                Log.w("UploadFile", "MIME type null, using default: " + mimeType);
            }

            Log.d("UploadFile", "Uploading file: " + selectedFileName + ", MIME: " + mimeType + ", Path: " + tempFile.getAbsolutePath());

            // Tạo InputFile từ tệp tạm
            InputFile inputFile = InputFile.Companion.fromFile(tempFile);
            String fileId = UUID.randomUUID().toString();

            // Gọi phương thức uploadFile với Bucket ID đúng
            Appwrite.uploadFile(BUCKET_ID, fileId, inputFile, selectedFileName, (result, error) -> {
                runOnUiThread(() -> {
                    // Xóa tệp tạm
                    if (tempFile.exists() && !tempFile.delete()) {
                        Log.w("UploadFile", "Không thể xóa tệp tạm: " + tempFile.getAbsolutePath());
                    }

                    // Xóa hình xem trước
                    previewImageView.setImageDrawable(null);
                    previewImageView.setVisibility(View.GONE);

                    if (error != null) {
                        String errorMessage = error.getMessage();
                        if (errorMessage != null && errorMessage.contains("Storage bucket with the requested ID could not be found")) {
                            Toast.makeText(this, "Không tìm thấy bucket lưu trữ. Vui lòng kiểm tra Bucket ID.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Tải lên thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        Log.e("UploadFile", "Upload error: " + errorMessage, error);
                        return;
                    }

                    Toast.makeText(this, "Tải lên thành công: " + result.getName(), Toast.LENGTH_SHORT).show();
                    fileNameTextView.setText("Chưa chọn tệp");
                    selectedFileUri = null;
                    selectedFileName = null;
                    startActivity(new Intent(UploadFileActivity.this, UploadedFilesActivity.class));
                });
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý tệp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("UploadFile", "Error processing file: " + e.getMessage(), e);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri != null && "content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.e("UploadFile", "Error getting file name: " + e.getMessage(), e);
            }
        }
        if (result == null) {
            result = uri != null ? uri.getLastPathSegment() : "unknown_file";
        }
        return result;
    }
}