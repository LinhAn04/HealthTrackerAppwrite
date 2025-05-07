package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;

public class FilePreviewActivity extends AppCompatActivity {

    private static final String BUCKET_ID = "6815d8f00034f84ff79b";
    private static final String PROJECT_ID = "6815a995001d1fe9a6eb";
    private static final int STORAGE_PERMISSION_CODE = 100;
    private ImageView ivFilePreview;
    private VideoView vvPreview;
    private WebView wvPreview;
    private TextView tvFileInfo;
    private AppCompatButton btnShare;
    private AppCompatButton btnDownload;
    private String fileId;
    private String fileName;
    private String mimeType;
    private long dateCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_preview);

        // Ánh xạ views
        ivFilePreview = findViewById(R.id.vv_file_preview);
        vvPreview = findViewById(R.id.vv_preview);
        wvPreview = findViewById(R.id.wv_preview);
        tvFileInfo = findViewById(R.id.tv_file_info);
        btnShare = findViewById(R.id.btn_share);
        btnDownload = findViewById(R.id.btn_download);

        // Lấy dữ liệu từ Intent
        fileId = getIntent().getStringExtra("fileId");
        fileName = getIntent().getStringExtra("fileName");
        mimeType = getIntent().getStringExtra("mimeType");
        dateCreated = getIntent().getLongExtra("dateCreated", 0);

        if (fileId == null || fileName == null || mimeType == null) {
            Toast.makeText(this, "Không tìm thấy thông tin tệp", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị thông tin tệp
        tvFileInfo.setText("Tên tệp: " + fileName);

        // Xử lý hiển thị nội dung dựa trên MIME type
        if (mimeType.startsWith("image/")) {
            displayImage();
        } else if (mimeType.startsWith("video/")) {
            displayVideo();
        } else if (mimeType.equals("application/pdf")) {
            displayPDF();
        } else {
            Toast.makeText(this, "Định dạng tệp không được hỗ trợ", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý nút chia sẻ
        btnShare.setOnClickListener(v -> shareFile());

        // Xử lý nút tải xuống
        btnDownload.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                downloadFile();
            } else {
                requestStoragePermission();
            }
        });
    }

    private void displayImage() {
        ivFilePreview.setVisibility(View.VISIBLE);
        vvPreview.setVisibility(View.GONE);
        wvPreview.setVisibility(View.GONE);

        String previewUrl = "https://fra.cloud.appwrite.io/v1/storage/buckets/" + BUCKET_ID + "/files/" + fileId + "/preview?project=" + PROJECT_ID;
        Log.e("FilePreviewActivity", "Image Preview URL: " + previewUrl);

        Glide.with(this)
                .load(previewUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.image)
                .into(ivFilePreview);
    }

    private void displayVideo() {
        ivFilePreview.setVisibility(View.GONE);
        vvPreview.setVisibility(View.VISIBLE);
        wvPreview.setVisibility(View.GONE);

        String videoUrl = "https://fra.cloud.appwrite.io/v1/storage/buckets/" + BUCKET_ID + "/files/" + fileId + "/view?project=" + PROJECT_ID;
        Log.d("FilePreviewActivity", "Video URL: " + videoUrl);

        vvPreview.setVideoURI(Uri.parse(videoUrl));
        vvPreview.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            vvPreview.start();
        });
        vvPreview.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Lỗi phát video", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void displayPDF() {
        ivFilePreview.setVisibility(View.GONE);
        vvPreview.setVisibility(View.GONE);
        wvPreview.setVisibility(View.VISIBLE);

        String pdfUrl = "https://fra.cloud.appwrite.io/v1/storage/buckets/" + BUCKET_ID + "/files/" + fileId + "/view?project=" + PROJECT_ID;
        Log.d("FilePreviewActivity", "PDF URL: " + pdfUrl);

        wvPreview.getSettings().setJavaScriptEnabled(true);
        wvPreview.getSettings().setBuiltInZoomControls(true);
        wvPreview.setWebViewClient(new WebViewClient());
        String googleDocsUrl = "https://docs.google.com/viewer?url=" + Uri.encode(pdfUrl);
        wvPreview.loadUrl(googleDocsUrl);
    }

    private void shareFile() {
        String fileUrl = "https://fra.cloud.appwrite.io/v1/storage/buckets/" + BUCKET_ID + "/files/" + fileId + "/view?project=" + PROJECT_ID;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ tệp: " + fileName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Xem tệp tại: " + fileUrl);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ tệp"));
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile();
            } else {
                Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile() {
        new Thread(() -> {
            try {
                String fileUrl = "https://fra.cloud.appwrite.io/v1/storage/buckets/" + BUCKET_ID + "/files/" + fileId + "/view?project=" + PROJECT_ID;
                InputStream inputStream = new URL(fileUrl).openStream();
                // Sử dụng thư mục Downloads của ứng dụng
                java.io.File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadDir == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Không thể truy cập thư mục Downloads", Toast.LENGTH_SHORT).show());
                    return;
                }
                java.io.File file = new java.io.File(downloadDir, fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                runOnUiThread(() -> Toast.makeText(this, "Đã tải xuống: " + fileName, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e("FilePreviewActivity", "Lỗi tải xuống: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Tải xuống thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}