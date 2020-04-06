package com.zacle.scheduler.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.zacle.scheduler.R;
import com.zacle.scheduler.data.database.entity.Event;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.zacle.scheduler.utils.EventStatus.COMING;
import static com.zacle.scheduler.utils.EventStatus.RUNNING;

public class EventAdapter extends ListAdapter<Event, EventAdapter.EventHolder> {
    private OnItemClickListener listener;

    public EventAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getTime().getTime() == newItem.getTime().getTime() &&
                    Double.compare(oldItem.getSourceLat(), newItem.getSourceLat()) == 0 &&
                    Double.compare(oldItem.getSourceLong(), newItem.getSourceLong()) == 0 &&
                    Double.compare(oldItem.getDestinationLat(), newItem.getDestinationLat()) == 0 &&
                    Double.compare(oldItem.getDestinationLong(), newItem.getDestinationLong()) == 0 &&
                    oldItem.getStatus().getCode() == newItem.getStatus().getCode();
        }
    };

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item, parent, false);
        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        Event event = getItem(position);
        holder.setStatus(event);
        holder.setName(event);
        holder.setDate(event);
    }

    public Event getEventAt(int position) {
        return getItem(position);
    }

    public class EventHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.schedule_status) ImageView status;
        @BindView(R.id.event_name) TextView name;
        @BindView(R.id.event_date) TextView date;

        public EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> onClick());
        }

        private void setStatus(Event event) {
            if (event.getStatus() == RUNNING) {
                status.setImageResource(R.drawable.ic_alarm_on);
            } else if (event.getStatus() == COMING) {
                status.setImageResource(R.drawable.ic_alarm);
            }
        }

        public void setName(Event event) {
            name.setText(event.getName());
        }

        public void setDate(Event event) {
            String pattern = "EEE, d MMM yy hh:mm aaa";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String formatedDate = simpleDateFormat.format(event.getTime());
            date.setText(formatedDate);
        }

        private void onClick() {
            int position = getAdapterPosition();
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onItemClick(getItem(position));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Event note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
