/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.Settings;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that should be implemented by classes that allow the storing and retrieval of the
 * application settings.
 */
public interface SettingsAccess {
    /**
     * Retrieves previously stored settings and assigns these properties to the specified
     * {@code settings}.
     * <p>
     * If the first launch property is not stored, the application is considered to be launched
     * for the first time. In this case, the first launch property will be stored and set to
     * {@code false}.
     * </p>
     *
     * @param settings the {@code Settings} whose properties should be retrieved
     * @throws IllegalArgumentException if {@code settings} was {@code null}
     * @throws SettingsAccessException if an error occurred while retrieving the properties of
     *                                 the {@code settings}
     */
    void retrieveSettings(@NotNull Settings settings);

    /**
     * Stores the properties of the specified {@code settings}.
     *
     * @param settings the {@code Settings} whose properties should be stored
     * @throws IllegalArgumentException if {@code settings} was {@code null}
     * @throws SettingsAccessException if an error occurred while storing the properties of
     *                                 the {@code settings}
     */
    void storeSettings(@NotNull Settings settings);
}
