package com.zacle.scheduler.ui.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.model.UserLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class CurrentLocationsAdapter extends RecyclerView.Adapter<CurrentLocationsAdapter.CurrentLocationsHolder> {

    private List<UserLocation> locations = new ArrayList<>();
    private Context context;

    public CurrentLocationsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CurrentLocationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_remaining_time_item, parent, false);
        return new CurrentLocationsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentLocationsHolder holder, int position) {
        UserLocation userLocation = locations.get(position);
        holder.setValues(userLocation);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void setLocations(List<UserLocation> locations) {
        this.locations = locations;
        notifyDataSetChanged();
    }

    public class CurrentLocationsHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_profile_time) ImageView profile;
        @BindView(R.id.username_time) TextView username;
        @BindView(R.id.remaining_time) TextView time;

        public CurrentLocationsHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setValues(UserLocation location) {
            setProfile(location);
            setUsername(location);
            setTime(location);
        }

        public void setProfile(UserLocation location) {
            Glide.with(context)
                    .load(location.getUser().getAvatarURL())
                    .centerCrop()
                    .into(profile);
        }

        public void setUsername(UserLocation location) {
            if (location.getUser() != null) {
                Timber.d("Username = %s", location.getUser().getUsername());
                username.setText(location.getUser().getUsername());
            }
        }

        public void setTime(UserLocation location) {
            if (location.getDuration() != null && location.getTime() != null) {
                long timeLeft = location.getTime().getTime();
                String duration = location.getDuration();
                if (new Date().getTime() - timeLeft > TimeUnit.HOURS.toMillis(6)) {
                    duration = context.getString(R.string.not_moved);
                } else if (location.getTimeLeft() <= TimeUnit.MINUTES.toSeconds(10)) {
                    duration = context.getString(R.string.user_arrived);
                }
                time.setText(duration);
            } else {
                time.setText(R.string.not_moved);
            }
        }
    }
}
