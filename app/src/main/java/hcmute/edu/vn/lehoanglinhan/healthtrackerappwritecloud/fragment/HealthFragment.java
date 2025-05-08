package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.api.services.gmail.GmailScopes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database.AppDatabase;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.HealthData;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter.HistoryAdapter;

public class HealthFragment extends Fragment {
    TextView caloriesTV, stepTV;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private AppDatabase appDatabase;
    private GoogleSignInClient googleSignInClient;
    private FitnessOptions fitnessOptions;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.health_fragment, container, false);
        Context context = requireContext(); // Đảm bảo context không bị null

        setupGoogleSignIn(context); // Khởi tạo Google Sign-In
        setupFitnessOptions(); // Khởi tạo Fitness API

        // Kiểm tra tài khoản đăng nhập
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account == null) {
            requestGoogleSignIn(context);
            return view;
        }

        // Kiểm tra quyền truy cập Google Fit
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(requireActivity(), 1, account, fitnessOptions);
            return view;
        }
        langNgheSuKien(getContext(),account);
        resetCuoiNgay(getContext());
        lichSu(view);
        return view;
    }

    private void setupGoogleSignIn(Context context) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(GmailScopes.GMAIL_SEND))
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
    }

    private void setupFitnessOptions() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .build();
    }

    private void requestGoogleSignIn(Context context) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }
    private void langNgheSuKien(Context context, GoogleSignInAccount account) {
        Fitness.getSensorsClient(context, account)
                .findDataSources(
                        new DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_DISTANCE_DELTA) // Thêm distance
                                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                                .build()
                )
                .addOnSuccessListener(dataSources -> {
                    for (DataSource dataSource : dataSources) {
                        DataType type = dataSource.getDataType();

                        if (type.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                            // Lắng nghe số bước chân
                            Fitness.getSensorsClient(context, account)
                                    .add(
                                            new SensorRequest.Builder()
                                                    .setDataSource(dataSource)
                                                    .setDataType(type)
                                                    .setSamplingRate(1, TimeUnit.SECONDS)
                                                    .build(),
                                            dataPoint -> {
                                                int steps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                                                Log.d("StepCounter", "Số bước chân: " + steps);

                                                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                                                SharedPreferences prefs = requireContext().getSharedPreferences("steps_prefs", Context.MODE_PRIVATE);
                                                String savedDate = prefs.getString("date", "");

                                                int oldSteps = 0;
                                                if (currentDate.equals(savedDate)) {
                                                    oldSteps = prefs.getInt("daily_steps", 0);
                                                }

                                                int newSteps = oldSteps + steps;
                                                prefs.edit()
                                                        .putInt("daily_steps", newSteps)
                                                        .putString("date", currentDate)
                                                        .apply();

                                                Log.d("StepCounter", "Cộng dồn số bước hôm nay: " + newSteps);

                                                // Tính calo từ bước
                                                float calories = calculateCaloriesFromSteps(newSteps);
                                                Log.d("Calories", "Calo tiêu thụ: " + calories + " calo");

                                                requireActivity().runOnUiThread(() -> {
                                                    caloriesTV.setText(String.valueOf(calories));
                                                    stepTV.setText(String.valueOf(newSteps));
                                                });
                                            }

                                    )
                                    .addOnSuccessListener(aVoid -> Log.d("StepCounter", "Đã kết nối cảm biến bước chân"))
                                    .addOnFailureListener(e -> Log.e("StepCounter", "Không kết nối cảm biến bước chân", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Sensor", "Không tìm thấy nguồn dữ liệu cảm biến", e));
    }

    private float calculateCaloriesFromSteps(int steps) {
        float caloriesPerStep = 0.04f; // Hệ số calo cho mỗi bước đi
        return steps * caloriesPerStep;
    }
    private void resetCuoiNgay(Context context) {
        SharedPreferences stepPrefs = context.getSharedPreferences("steps_prefs", Context.MODE_PRIVATE);
        int steps = stepPrefs.getInt("daily_steps", 0);
        float calories = calculateCaloriesFromSteps(steps);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String savedDate = stepPrefs.getString("date", "");

        if (!currentDate.equals(savedDate)) {
            // Nếu ngày hiện tại khác với ngày đã lưu, lưu vào CSDL
            HealthData healthData = new HealthData(savedDate, steps, calories);

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                db.healthDataDAO().insert(healthData);

                // Reset SharedPreferences
                stepPrefs.edit().putInt("daily_steps", 0).putString("date", currentDate).apply();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        stepTV.setText("0");
                        caloriesTV.setText("0");
                    });
                }
            }).start();
        }

    }
    private void lichSu(View view){
        recyclerView = view.findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter với danh sách rỗng ban đầu
        historyAdapter = new HistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(historyAdapter);

        // Truy vấn dữ liệu từ Room
        appDatabase = AppDatabase.getInstance(getContext());

        // Lấy toàn bộ dữ liệu lịch sử và cập nhật RecyclerView
        new Thread(() -> {
            List<HealthData> historyList = appDatabase.healthDataDAO().getAll();
            getActivity().runOnUiThread(() -> {
                historyAdapter.updateHistoryList(historyList);
            });
        }).start();
    }
}
