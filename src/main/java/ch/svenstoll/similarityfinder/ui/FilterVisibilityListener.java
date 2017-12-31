/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

/**
 * A functional interface that allows a listener to observe if filter visibility change was
 * requested by a {@code SideBarController} instance.
 */
@FunctionalInterface
public interface FilterVisibilityListener {
    /**
     * This method will be called whenever the observed {@code SideBarController} instance fires a
     * {@code FilterVisibilityEvent}.
     *
     * @param event the {@code FilterVisibilityEvent} that was fired
     */
    void onFilterVisibilityChanged(FilterVisibilityEvent event);
}
