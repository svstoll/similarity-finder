/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

/**
 * An interface that can be used if something is selectable.
 */
public interface Selectable {
    /**
     * Returns whether the instance of {@code Selectable} is selected or not.
     *
     * @return {@code true} if the object is selected; {@code false} otherwise
     */
    boolean isSelected();

    /**
     * Sets the selected property of an instance of {@code Selectable}.
     *
     * @param selected the new selection state
     */
    void setSelected(boolean selected);
}
