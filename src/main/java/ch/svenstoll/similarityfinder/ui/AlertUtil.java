/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * A class that offers utility methods for the JavaFX {@code Alert} class.
 */
public final class AlertUtil {
    /**
     * Styles an alert using the main css file of the application.
     *
     * @param alert the alert to be styled
     */
    public static void styleAlert(@NotNull final Alert alert) {
        Validate.notNull(alert, "Alert must not be null.");

        DialogPane dialogPane = alert.getDialogPane();
        URL cssUrl = AlertUtil.class.getClassLoader().getResource("css/app.css");
        if (cssUrl != null) {
            dialogPane.getStylesheets().add(cssUrl.toString());
        }
    }
}
