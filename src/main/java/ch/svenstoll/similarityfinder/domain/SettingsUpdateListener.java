/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that allows a listener to observe if the a {@code Settings} instance
 * has been updated.
 */
@FunctionalInterface
public interface SettingsUpdateListener {
    /**
     * This method will be called whenever the observed {@code Settings} instance fires a
     * {@code SettingsUpdateEvent}.
     *
     * @param event the {@code SettingsUpdateEvent} that was fired
     */
    void onSettingsUpdated(@NotNull SettingsUpdateEvent event);
}
