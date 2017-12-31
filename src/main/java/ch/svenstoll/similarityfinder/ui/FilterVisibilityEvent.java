/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import java.util.EventObject;

/**
 * A subclass of {@code EventObject} that can be used to notify instances of {@code
 * IFilterVisibilityListener} that a filter visibility change was requested by a {@code
 * SideBarController} instance.
 */
public class FilterVisibilityEvent extends EventObject {
    /**
     * Constructs a {@code FilterVisibilityEvent} instance.
     *
     * @param source the {@code SideBarController} instance on which the {@code
     *               FilterVisibilityEvent} initially occurred
     * @throws IllegalArgumentException if source was {@code null}
     */
    public FilterVisibilityEvent(SideBarController source) {
        super(source);
    }
}

