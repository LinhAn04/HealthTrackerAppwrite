package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.controlelr;

import android.content.Context;
import android.util.Log;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.File;
import io.appwrite.models.InputFile;
import io.appwrite.models.Session;
import io.appwrite.models.User;
import io.appwrite.services.Account;
import io.appwrite.services.Storage;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class Appwrite {
    private static Client client;
    private static Account account;
    private static Storage storage;

    public interface Callback<T> {
        void onComplete(T result, Exception error);
    }

    public static void init(Context context) {
        if (client == null) {
            client = new Client(context, "https://fra.cloud.appwrite.io/v1")
                    .setProject("6815a995001d1fe9a6eb")
                    .setSelfSigned(true); // Nếu bạn dùng SSL tự ký
            account = new Account(client);
            storage = new Storage(client);
        }
    }

    public static Account getAccount() {
        if (account == null) {
            throw new IllegalStateException("Appwrite chưa được khởi tạo.");
        }
        return account;
    }

    public static void checkCurrentSession(Callback<Session> callback) {
        account.getSession(
                "current",
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        Log.e("Appwrite", "Không có session hiện tại: " + error.getMessage(), error);
                        callback.onComplete(null, (Exception) error);
                    } else {
                        Log.d("Appwrite", "Session hiện tại: " + result);
                        callback.onComplete(result, null);
                    }
                })
        );
    }

    public static void onLogin(String email, String password, Callback<Session> callback) {
        account.createEmailPasswordSession(
                email,
                password,
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        Log.e("Appwrite", "Lỗi đăng nhập: " + error.getMessage(), error);
                        callback.onComplete(null, (Exception) error);
                        return;
                    }
                    Log.d("Appwrite", "Đăng nhập thành công: " + result.toString());
                    callback.onComplete(result, null);
                })
        );
    }

    public static void onRegister(String email, String password, Callback<User> callback) throws AppwriteException {
        String userId = UUID.randomUUID().toString();

        account.create(
                userId,
                email,
                password,
                email,
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        Log.e("Appwrite", "Lỗi đăng ký: " + error.getMessage(), error);
                        callback.onComplete(null, (Exception) error);
                        return;
                    }
                    Log.d("Appwrite", "Tạo người dùng thành công: " + result.toString());
                    callback.onComplete(result, null);
                })
        );
    }

    public static void getSession(Callback<Session> callback) {
        account.getSession(
                "current",
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        Log.e("Appwrite", "Lỗi lấy phiên: " + error.getMessage(), error);
                        callback.onComplete(null, (Exception) error);
                        return;
                    }
                    Log.d("Appwrite", "Lấy phiên thành công: " + result.toString());
                    callback.onComplete(result, null);
                })
        );
    }

    public static void onLogout(Callback<Void> callback) {
        account.deleteSession(
                "current", // sessionId
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }
                    Log.d("Appwrite", result.toString());
                })
        );
    }

    public static void uploadFile(String bucketId, String fileId, InputFile fileStream, String fileName, Callback<File> callback) {
        storage.createFile(
                bucketId,
                fileId,
                fileStream,
                Arrays.asList("read(\"any\")"),
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        Log.e("Appwrite", "Lỗi tải tệp lên: " + error.getMessage(), error);
                        callback.onComplete(null, (Exception) error);
                        return;
                    }
                    Log.d("Appwrite", "Tải tệp lên thành công: " + result.toString());
                    callback.onComplete(result, null);
                })
        );
    }

    public static void listFiles(String bucketId, Callback<List<File>> callback) {
        storage.listFiles(
                bucketId,
                null,
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        Log.e("Appwrite", "Lỗi liệt kê tệp: " + error.getMessage(), error);
                        callback.onComplete(null, (Exception) error);
                        return;
                    }
                    Log.d("Appwrite", "Liệt kê tệp thành công: " + result.toString());
                    callback.onComplete(result.getFiles(), null);
                })
        );
    }

    public static void deleteFile(String bucketId, String fileId, Callback<Void> callback) {
        storage.deleteFile(
                bucketId,
                fileId,
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        Log.e("Appwrite", "Delete file error: " + error.getMessage(), error);
                        callback.onComplete(null, (Exception) error);
                        return;
                    }
                    Log.d("Appwrite", "Delete file success");
                    callback.onComplete(null, null);
                })
        );
    }
}