package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ticket_table")
public class Ticket {
    @PrimaryKey(autoGenerate = true)
    public int ticketId;
    public int eventId;  // Liên kết với sự kiện
    public String attendeeName;
    public String attendeeEmail;

    public Ticket() {
    }

    public Ticket(int eventId, String attendeeName, String attendeeEmail) {
        this.eventId = eventId;
        this.attendeeName = attendeeName;
        this.attendeeEmail = attendeeEmail;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getAttendeeEmail() {
        return attendeeEmail;
    }

    public void setAttendeeEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }
}

