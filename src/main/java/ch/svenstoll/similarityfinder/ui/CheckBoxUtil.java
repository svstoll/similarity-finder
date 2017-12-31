/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.domain.Selectable;
import org.jetbrains.annotations.NotNull;
import javafx.scene.control.CheckBox;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * A class that offers utility methods for the JavaFX {@code CheckBox} class.
 */
public class CheckBoxUtil {
    /**
     * Adjusts the selection state of a {@code CheckBox} based on a list of {@code Selectable}
     * items. If all items are selected, the check box will be set to selected. If some item are
     * selected, it will be set to indeterminate. If no item is selected, it will be set to not
     * selected.
     *
     * @param checkBox a {@code CheckBox} instance whose selection state will be adjusted
     * @param list a list of {@code Selectable} instances
     * @throws IllegalArgumentException if any parameter was {@code null} or {@code list}
     *                                  contained {@code null} elements
     */
    public static void adjustCheckBoxSelectionState(@NotNull CheckBox checkBox,
                                                    @NotNull List<? extends Selectable> list) {
        Validate.notNull(checkBox, "CheckBox must not be null.");
        Validate.notNull(list, "List must not be null.");
        Validate.noNullElements(list, "List must not contain null elements.");

        boolean allSelected = true;
        boolean someSelected = false;
        for (Selectable selectable : list) {
            if (!selectable.isSelected()) {
                allSelected = false;
            } else {
                someSelected = true;
            }
        }
        if (someSelected) {
            if (allSelected) {
                checkBox.setIndeterminate(false);
                checkBox.setSelected(true);
            } else {
                checkBox.setIndeterminate(true);
                checkBox.setSelected(false);
            }
        } else {
            checkBox.setIndeterminate(false);
            checkBox.setSelected(false);
        }
    }
}
