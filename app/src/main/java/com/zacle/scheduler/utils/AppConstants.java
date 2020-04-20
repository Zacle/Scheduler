package com.zacle.scheduler.utils;

public final class AppConstants {
    public static final String STATUS_CODE_SUCCESS = "success";
    public static final String STATUS_CODE_FAILED = "failed";

    public static final String DB_NAME = "scheduler.db";
    public static final String PREF_NAME = "scheduler_pref";
    public static final String MINUTES = "minutes";
    public static final String HOURS = "hours";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 50; // 1 mile, 1.6 km
    public static final int LOITERING_DELAY = 60 * 1000;

    private AppConstants() {}
}
