package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "event_table")
public class Event {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String title;

    public String description;
    public long  startTime;
    public long  endTime;
    public String location;
    public boolean isPersonal; // true = cá nhân, false = tổ chức

    public Event() {
    }

    public Event(@NonNull String title, String description, long  startTime, long  endTime, String location, boolean isPersonal) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.isPersonal = isPersonal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long  startTime) {
        this.startTime = startTime;
    }

    public long  getEndTime() {
        return endTime;
    }

    public void setEndTime(long  endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isPersonal() {
        return isPersonal;
    }

    public void setPersonal(boolean personal) {
        isPersonal = personal;
    }
}

