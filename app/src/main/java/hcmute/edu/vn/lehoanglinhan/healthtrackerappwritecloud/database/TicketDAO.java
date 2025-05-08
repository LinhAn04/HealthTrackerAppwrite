package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Ticket;
@Dao
public interface TicketDAO {
    @Insert
    void insertTicket(Ticket ticket);

    @Query("SELECT * FROM ticket_table WHERE eventId = :eventId")
    LiveData<List<Ticket>> getTicketsByEventId(int eventId);
    @Query("SELECT attendeeEmail FROM ticket_table WHERE eventId = :eventId")
    List<String> getEmailsByEventId(int eventId); // Không dùng LiveData

}
