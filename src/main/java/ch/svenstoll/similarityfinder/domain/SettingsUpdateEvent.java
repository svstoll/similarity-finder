/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import org.jetbrains.annotations.NotNull;

import java.util.EventObject;

/**
 * A subclass of {@code EventObject} that can be used to notify instances of {@code
 * ISettingsUpdatedListener} that a {@code Settings} instance has been updated.
 */
public class SettingsUpdateEvent extends EventObject {
    /**
     * Constructs a {@code SettingsUpdateEvent} instance.
     *
     * @param source the {@code Settings} instance on which this {@code SettingsUpdateEvent}
     *               initially occurred
     * @throws IllegalArgumentException if source was {@code null}
     */
    public SettingsUpdateEvent(@NotNull Settings source) {
        super(source);
    }
}
