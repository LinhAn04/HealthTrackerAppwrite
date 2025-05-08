package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.HealthData;
@Dao
public interface HealthDataDAO {
    // Thêm 1 bản ghi
    @Insert
    void insert(HealthData healthData);

    // Lấy tất cả dữ liệu
    @Query("SELECT * FROM health_data ORDER BY date DESC")
    List<HealthData> getAll();

    // Lấy dữ liệu theo ngày cụ thể
    @Query("SELECT * FROM health_data WHERE date = :date LIMIT 1")
    HealthData getByDate(String date);
}
