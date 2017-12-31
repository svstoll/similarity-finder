/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import ch.svenstoll.similarityfinder.dao.SettingsAccess;
import ch.svenstoll.similarityfinder.dao.SettingsAccessException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that can permanently store and provide general properties that are needed throughout
 * the application. Thus, only one instance of this class should be instantiated which is ensured
 * by the use of the dependency injection framework Guice. All public methods of this class are
 * thread safe.
 */
@Singleton
public final class Settings {
    @NotNull
    public static final String DEFAULT_DB_ADDRESS = "";
    @NotNull
    public static final String DEFAULT_DB_USER = "";
    @NotNull
    public static final String DEFAULT_DB_PASSWORD = "";
    public static final int DEFAULT_MAX_ARTICLES = 1000;

    @NotNull
    private final SettingsAccess settingsAccess;
    @NotNull
    private final List<SettingsUpdateListener> settingsUpdateListeners = new ArrayList<>();

    private boolean firstLaunch = true;
    @NotNull
    private String dbAddress = DEFAULT_DB_ADDRESS;
    @NotNull
    private String dbUser = DEFAULT_DB_USER;
    @NotNull
    private String dbPassword = DEFAULT_DB_PASSWORD;
    private int maxArticles = DEFAULT_MAX_ARTICLES;

    /**
     * Constructs a {@code Settings} instance and tries to retrieve any properties that have been
     * stored previously.
     *
     * @param settingsAccess an instance of {@code SettingsAccess} used to retrieve and
     *                       store the properties of this {@code Settings} instance
     * @throws IllegalArgumentException if {@code settingsAccess} was {@code null}
     * @throws SettingsAccessException if an error occurred while retrieving the properties of
     *                                 this {@code Settings} instance
     */
    @Inject
    public Settings(@NotNull SettingsAccess settingsAccess) {
        this.settingsAccess
                = Validate.notNull(settingsAccess, "SettingsAccessImpl must not be null.");
        settingsAccess.retrieveSettings(this);
    }

    /**
     * Saves the properties of this {@code Settings} instance. All subscribed instances of {@code
     * SettingsUpdateListener} will be notified about this update. This method is thread safe.
     *
     * @throws SettingsAccessException if an error occurred while storing the properties of this
     *                                 {@code Settings} instance
     */
    public synchronized void saveSettings() {
        settingsAccess.storeSettings(this);
        SettingsUpdateEvent event = new SettingsUpdateEvent(this);
        fireSettingsUpdatedEvent(event);
    }

    /**
     * Adds an instance of {@code SettingsUpdateListener} that will be notified whenever a
     * {@code SettingsUpdateEvent} has occurred. This method is thread safe.
     *
     * @param listener an instance of {@code SettingsUpdateListener} that will be notified
     * @throws IllegalArgumentException if the {@code listener} was {@code null}
     */
    public synchronized void addSettingsUpdatedListener(@NotNull SettingsUpdateListener listener) {
        Validate.notNull(listener, "Listener must not be null.");
        settingsUpdateListeners.add(listener);
    }

    /**
     * Notifies instances of {@code SettingsUpdateListener} that a {@code SettingsUpdateEvent}
     * has occurred.
     *
     * @param event the {@code SettingsUpdateEvent} to fire
     * @throws IllegalArgumentException if the {@code event} was {@code null}
     */
    private void fireSettingsUpdatedEvent(@NotNull SettingsUpdateEvent event) {
        Validate.notNull(event, "Event must not be null.");
        settingsUpdateListeners.forEach(listener -> listener.onSettingsUpdated(event));
    }

    public synchronized boolean isFirstLaunch() {
        return firstLaunch;
    }

    public synchronized void setFirstLaunch(boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
    }

    public synchronized @NotNull String getDbAddress() {
        return dbAddress;
    }

    public synchronized void setDbAddress(@NotNull String dbAddress) {
        Validate.notNull(dbAddress, "DbAddress must not be null.");
        this.dbAddress = dbAddress;
    }

    public synchronized @NotNull String getDbUser() {
        return dbUser;
    }

    public synchronized void setDbUser(@NotNull String dbUser) {
        Validate.notNull(dbUser, "DbUser must not be null.");
        this.dbUser = dbUser;
    }

    public synchronized @NotNull String getDbPassword() {
        return dbPassword;
    }

    public synchronized void setDbPassword(@NotNull String dbPassword) {
        Validate.notNull(dbPassword, "DbPassword must not be null.");
        this.dbPassword = dbPassword;
    }

    public synchronized int getMaxArticles() {
        return maxArticles;
    }

    public synchronized void setMaxArticles(int maxArticles) {
        this.maxArticles = maxArticles;
    }
}
