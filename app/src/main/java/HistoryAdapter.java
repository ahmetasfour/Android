import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vize.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<String> historyList;

    // Constructor for the adapter
    public HistoryAdapter(List<String> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String currentHistory = historyList.get(position);
        holder.historyTextView.setText(currentHistory);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // ViewHolder inner class
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView historyTextView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            historyTextView = itemView.findViewById(R.id.historyText); // Make sure your item_history.xml has a TextView with this ID
        }
    }
}