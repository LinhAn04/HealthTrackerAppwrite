package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.controlelr.Appwrite;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import io.appwrite.models.File;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<File> files;
    private final OnFileClickListener onFileClickListener;
    private final OnFileDeleteListener onFileDeleteListener;

    public interface OnFileClickListener {
        void onFileClick(File file);
    }

    public interface OnFileDeleteListener {
        void onFileDeleted(File file);
    }

    public FileAdapter(List<File> files, OnFileClickListener clickListener, OnFileDeleteListener deleteListener) {
        this.files = files;
        this.onFileClickListener = clickListener;
        this.onFileDeleteListener = deleteListener;
    }

    public void updateFiles(List<File> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files.get(position);
        holder.bind(file, onFileClickListener, onFileDeleteListener);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFilePreview;
        private final TextView tvFileName;
        private final AppCompatButton btnDeleteFile;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFilePreview = itemView.findViewById(R.id.iv_file_preview);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            btnDeleteFile = itemView.findViewById(R.id.btn_delete_file);
        }

        public void bind(File file, OnFileClickListener clickListener, OnFileDeleteListener deleteListener) {
            tvFileName.setText(file.getName());

            // Hiển thị hình xem trước
            String mimeType = file.getMimeType();
            Log.d("FileAdapter", "MIME type for file " + file.getName() + ": " + mimeType);

            if (mimeType != null && mimeType.startsWith("image/")) {
                String previewUrl = "https://fra.cloud.appwrite.io/v1/storage/buckets/6815d8f00034f84ff79b/files/" + file.getId() + "/preview?project=6815a995001d1fe9a6eb";
                Log.d("FileAdapter", "Preview URL: " + previewUrl);
                Glide.with(itemView.getContext())
                        .load(previewUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.image)
                        .into(ivFilePreview);
            } else if (mimeType != null && mimeType.startsWith("video/")) {
                ivFilePreview.setImageResource(R.drawable.ic_video);
            } else if (mimeType != null && mimeType.equals("application/pdf")) {
                ivFilePreview.setImageResource(R.drawable.image);
            } else {
                ivFilePreview.setImageResource(R.drawable.image);
            }

            // Xử lý bấm item
            itemView.setOnClickListener(v -> clickListener.onFileClick(file));

            // Xử lý bấm nút xóa
            btnDeleteFile.setOnClickListener(v -> {
                Appwrite.deleteFile("6815d8f00034f84ff79b", file.getId(), (result, error) -> {
                    itemView.post(() -> {
                        if (error != null) {
                            Log.e("FileAdapter", "Xóa tệp thất bại: " + error.getMessage(), error);
                            return;
                        }
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            files.remove(position);
                            notifyItemRemoved(position);
                            if (deleteListener != null) {
                                deleteListener.onFileDeleted(file);
                            }
                        }
                    });
                });
            });
        }
    }
}