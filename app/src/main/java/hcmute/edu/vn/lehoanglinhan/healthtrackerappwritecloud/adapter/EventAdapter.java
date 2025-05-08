package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.Event;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList = new ArrayList<>();
    private OnItemClickListener listener;
    private AdapterView.OnItemLongClickListener longClickListener;


    public interface OnItemClickListener {
        void onItemClick(Event event);
        void onItemLongClick(Event event, View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }



    public void setEventList(List<Event> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(eventList.get(holder.getAdapterPosition()));
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemLongClick(eventList.get(holder.getAdapterPosition()), v);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, locationText, timeText;

        EventViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.event_title);
            descriptionText = itemView.findViewById(R.id.event_description);
            locationText = itemView.findViewById(R.id.event_location);
            timeText = itemView.findViewById(R.id.event_time);
        }

        void bind(Event event) {
            titleText.setText(event.getTitle());
            descriptionText.setText(event.getDescription());
            locationText.setText(event.getLocation());
            timeText.setText(event.getStartTime() + " - " + event.getEndTime());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(event);
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(event, v); // gọi long click
                }
                return true; // trả về true để ngăn click thường
            });
        }
    }
}

