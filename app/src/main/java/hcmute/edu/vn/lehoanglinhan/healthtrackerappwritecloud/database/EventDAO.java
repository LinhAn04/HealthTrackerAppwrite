package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Event;

@Dao
public interface EventDAO {
    @Insert
    void insert(Event event);

    @Query("SELECT * FROM event_table WHERE isPersonal = 1")
    LiveData<List<Event>> getPersonalEvents();

    @Query("SELECT * FROM event_table WHERE isPersonal = 0")
    LiveData<List<Event>> getOrganizedEvents();

    @Delete
    void delete(Event event);

    @Update
    void update(Event event);
}
