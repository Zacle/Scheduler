package com.zacle.scheduler.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class UserLocation implements Parcelable {

    private User user;
    private GeoPoint geoPoint;
    private @ServerTimestamp Date time;
    private String duration;
    private long timeLeft = 0;

    public UserLocation() {}

    public UserLocation(User user, GeoPoint geoPoint, Date time, String duration, long timeLeft) {
        this.user = user;
        this.geoPoint = geoPoint;
        this.time = time;
        this.duration = duration;
        this.timeLeft = timeLeft;
    }

    protected UserLocation(Parcel in) {
        user = in.readParcelable(User.class.getClassLoader());
        Double lat = in.readDouble();
        Double lng = in.readDouble();
        geoPoint = new GeoPoint(lat, lng);
    }

    public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", geo_point=" + geoPoint +
                ", timestamp=" + time +
                ", duration=" + duration +
                ", timeLeft=" + timeLeft +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeDouble(geoPoint.getLatitude());
        dest.writeDouble(geoPoint.getLongitude());
    }
}
