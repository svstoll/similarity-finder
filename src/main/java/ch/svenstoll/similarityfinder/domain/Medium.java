/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * A representation of a medium that publishes articles.
 */
public final class Medium implements Selectable {
    @NotNull
    private final String name;
    @NotNull
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    /**
     * Constructs a {@code Medium} instance.
     *
     * @param name the name of the medium which must not be empty
     * @throws IllegalArgumentException if the {@code name} was {@code null} or empty
     */
    public Medium(@NotNull String name) {
        Validate.notNull(name, "Name must not be null.");
        this.name = Validate.notEmpty(name, "Name must not be empty.");
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }

    public @NotNull BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public @NotNull String toString() {
        return getName();
    }
}
