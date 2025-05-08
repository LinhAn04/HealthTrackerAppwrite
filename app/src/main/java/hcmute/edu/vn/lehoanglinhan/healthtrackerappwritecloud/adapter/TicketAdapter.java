package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Ticket;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Ticket> ticketList;

    public TicketAdapter(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    @Override
    public TicketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.nameTextView.setText(ticket.getAttendeeName());
        holder.emailTextView.setText(ticket.getAttendeeEmail());
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;

        public TicketViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.ticketName);
            emailTextView = itemView.findViewById(R.id.ticketEmail);
        }
    }
}