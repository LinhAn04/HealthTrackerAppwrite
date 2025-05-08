package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.model.HealthData;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HealthData> historyList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTV, stepsTV, caloriesTV;

        public ViewHolder(View view) {
            super(view);
            dateTV = view.findViewById(R.id.dateTextView);
            stepsTV = view.findViewById(R.id.stepsTextView);
            caloriesTV = view.findViewById(R.id.caloriesTextView);
        }
    }

    public HistoryAdapter(List<HealthData> historyList) {
        this.historyList = historyList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HealthData data = historyList.get(position);
        holder.dateTV.setText(data.date);
        holder.stepsTV.setText("Steps: " + data.steps);
        holder.caloriesTV.setText("Calories: " + data.calories);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // Cập nhật dữ liệu khi có thay đổi
    public void updateHistoryList(List<HealthData> newList) {
        historyList = newList;
        notifyDataSetChanged();
    }
}
