package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "health_data")
public class HealthData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date; // YYYY-MM-DD
    public int steps;
    public float calories;
    public float distance;

    public HealthData() {
    }

    public HealthData(String date, int steps, float calories) {
        this.date = date;
        this.steps = steps;
        this.calories = calories;
//        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

//    public float getDistance() {
//        return distance;
//    }
//
//    public void setDistance(float distance) {
//        this.distance = distance;
//    }

}

