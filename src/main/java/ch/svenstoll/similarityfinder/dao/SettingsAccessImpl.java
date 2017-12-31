/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.Settings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.Validate;

import java.util.prefs.Preferences;

import static ch.svenstoll.similarityfinder.domain.Settings.*;

/**
 * An implementation of {@code SettingsAccess} that allows the storing and retrieval of the
 * application settings.
 */
@Singleton
public final class SettingsAccessImpl implements SettingsAccess {
    @NotNull
    static final String DB_ADDRESS_KEY = "DB_SERVER_ADDRESS";
    @NotNull
    static final String DB_USER_KEY = "DB_USER";
    @NotNull
    static final String DB_PASSWORD_KEY = "DB_PASSWORD";
    @NotNull
    static final String MAX_CONTRIBUTIONS_KEY = "MAX_CONTRIBUTIONS";
    @NotNull
    static final String FIRST_LAUNCH_KEY = "FIRST_LAUNCH";

    @NotNull
    private final Preferences preferences;

    /**
     * Creates a {@code SettingsAccessImpl}.
     *
     * @param preferences the {@code Preferences} which contains the stored properties of the
     *                    settings that are used throughout the application (if the application
     *                    has already been launched)
     * @throws IllegalArgumentException if {@code preferences} was {@code null}
     */
    @Inject
    public SettingsAccessImpl(@NotNull Preferences preferences) {
        this.preferences = Validate.notNull(preferences, "Preferences must not be null.");
    }

    /**
     * Retrieves previously stored settings from the {@code Preferences} node that is used by
     * this application. The retrieved properties will be assigned to the specified {@code
     * settings}.
     * <p>
     * If the first launch property is not stored, the application is considered to be launched
     * for the first time. In this case, the first launch property will be created in the
     * used {@code Preferences} node and set to {@code false}.
     * </p>
     *
     * @param settings the {@code Settings} whose properties should be retrieved
     * @throws IllegalArgumentException if {@code settings} was {@code null}
     * @throws SettingsAccessException if an error occurred while retrieving the properties of
     *                                 the {@code settings}
     */
    @Override
    public synchronized void retrieveSettings(@NotNull Settings settings) {
        Validate.notNull(settings, "Settings must not be null.");

        try {
            boolean firstLaunch = preferences.getBoolean(FIRST_LAUNCH_KEY, true);
            String dbAddress = preferences.get(DB_ADDRESS_KEY, DEFAULT_DB_ADDRESS);
            String dbUser = preferences.get(DB_USER_KEY, DEFAULT_DB_USER);
            String dbPassword = preferences.get(DB_PASSWORD_KEY, DEFAULT_DB_PASSWORD);
            int maxContributions
                    = preferences.getInt(MAX_CONTRIBUTIONS_KEY, DEFAULT_MAX_ARTICLES);

            settings.setFirstLaunch(firstLaunch);
            settings.setDbAddress(dbAddress);
            settings.setDbUser(dbUser);
            settings.setDbPassword(dbPassword);
            settings.setMaxArticles(maxContributions);

            if (firstLaunch) {
                preferences.putBoolean(FIRST_LAUNCH_KEY, false);
            }
        } catch (IllegalStateException e) {
            throw new SettingsAccessException(e.getMessage(), e);
        }
    }

    /**
     * Stores the properties of the specified {@code settings} in the {@code Properties} node
     * used by this application.
     *
     * @param settings the {@code Settings} whose properties should be stored
     * @throws IllegalArgumentException if {@code settings} was {@code null}
     * @throws SettingsAccessException if an error occurred while storing the properties of
     *                                 the {@code settings}
     */
    @Override
    public synchronized void storeSettings(@NotNull Settings settings) {
        Validate.notNull(settings, "Settings must not be null.");

        try {
            preferences.putBoolean(FIRST_LAUNCH_KEY, settings.isFirstLaunch());
            preferences.put(DB_ADDRESS_KEY, settings.getDbAddress());
            preferences.put(DB_USER_KEY, settings.getDbUser());
            preferences.put(DB_PASSWORD_KEY, settings.getDbPassword());
            preferences.putInt(MAX_CONTRIBUTIONS_KEY, settings.getMaxArticles());
        } catch (IllegalStateException e) {
            throw new SettingsAccessException(e.getMessage(), e);
        }
    }
}
