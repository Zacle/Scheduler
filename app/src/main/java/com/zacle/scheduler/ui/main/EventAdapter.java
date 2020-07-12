package com.zacle.scheduler.ui.main;

import android.animation.ObjectAnimator;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import butterknife.OnClick;

import static com.zacle.scheduler.utils.EventStatus.COMING;
import static com.zacle.scheduler.utils.EventStatus.RUNNING;

public class EventAdapter extends ListAdapter<Event, EventAdapter.EventHolder> {
    private OnItemClickListener listener;

    private SparseBooleanArray expandState = new SparseBooleanArray();

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

        final boolean isExpanded = expandState.get(position);

        holder.getExpandableLayout().setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.getExpand().setRotation(isExpanded ? 180f : 0f);
        holder.getExpand().setOnClickListener(v -> onClickExpand(holder.getExpandableLayout(),
                holder.getExpand(), position));
    }

    private void onClickExpand(final LinearLayout expandableLayout, final ImageView expand, final  int i) {

        if (expandableLayout.getVisibility() == View.VISIBLE) {
            createRotateAnimator(expand, 180f, 0f).start();
            expandableLayout.setVisibility(View.GONE);
            expandState.put(i, false);
        } else {
            createRotateAnimator(expand, 0f, 180f).start();
            expandableLayout.setVisibility(View.VISIBLE);
            expandState.put(i, true);
        }
    }


    private ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    public Event getEventAt(int position) {
        return getItem(position);
    }

    public void initExpandable(int size) {
        for (int i = 0; i < size; i++) {
            expandState.append(i, false);
        }
    }

    public class EventHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.schedule_status) ImageView status;
        @BindView(R.id.event_name) TextView name;
        @BindView(R.id.event_date) TextView date;
        @BindView(R.id.expand) ImageView expand;
        @BindView(R.id.expandableLayout) LinearLayout expandableLayout;
        @BindView(R.id.lunch_event) Button lunch;
        @BindView(R.id.edit_event) Button edit;
        @BindView(R.id.delete_event) Button delete;

        public EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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

        @OnClick(R.id.lunch_event)
        public void lunchMap() {
            int position = getAdapterPosition();
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onLunchClick(getItem(position));
            }
        }

        @OnClick(R.id.edit_event)
        public void editEvent() {
            int position = getAdapterPosition();
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onEditClick(getItem(position));
            }
        }

        @OnClick(R.id.delete_event)
        public void deleteEvent() {
            int position = getAdapterPosition();
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(getItem(position));
            }
        }

        public LinearLayout getExpandableLayout() {
            return expandableLayout;
        }

        public ImageView getExpand() {
            return expand;
        }
    }

    public interface OnItemClickListener {
        void onLunchClick(Event event);
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
