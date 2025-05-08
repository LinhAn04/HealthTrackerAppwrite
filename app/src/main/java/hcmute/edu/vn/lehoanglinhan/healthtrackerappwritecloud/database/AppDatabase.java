package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Event;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.HealthData;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Ticket;

@Database(entities = {HealthData.class, Event.class, Ticket.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public abstract HealthDataDAO healthDataDAO();
    public abstract EventDAO eventDao();
    public abstract TicketDAO ticketDAO();
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "health_tracker_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
