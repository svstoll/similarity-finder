/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Objects;

/**
 * A class that offers utility methods for the JavaFX {@code Stage} class.
 */
public final class StageUtil {
    /**
     * Opens the specified {@code root} in a new stage.
     *
     * @param root a {@code Parent} that will be set as the root of the scene that will be added
     *             to the new stage
     * @param title the title of the new stage
     * @param modality the {@code Modality} of the new stage
     * @param resizable a {@code boolean} that indicates if the new stage should be resizable
     * @throws IllegalArgumentException if any parameter was {@code null}
     */
    public static void openNewStage(@NotNull Parent root, @NotNull String title,
                                    @NotNull Modality modality, boolean resizable) {
        Validate.notNull(root, "Root must not be null.");
        Validate.notNull(title, "Title must not be null.");
        Validate.notNull(modality, "Modality must not be null.");

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(modality);
        stage.setResizable(resizable);
        stage.setTitle(title);
        setApplicationIconToStage(stage);

        // The stage needs to be shown first to infer its actual size.
        stage.show();
        if (resizable) {
            adjustStageSizeBounds(stage);
        }
        stage.centerOnScreen();

    }

    /**
     * Adjusts the size boundaries of the specified {@code stage}. The boundaries are inferred
     * from the root pane of the scene that belongs to the {@code stage}.
     *
     * @param stage the {@code stage} whose size boundaries are to be adjusted
     * @throws IllegalArgumentException if the {@code stage} was {@code null}
     * @throws NullPointerException if the scene of the {@code stage} was {@code null}
     */
    private static void adjustStageSizeBounds(@NotNull Stage stage) {
        Validate.notNull(stage, "Stage must not be null.");

        Pane rootPane;
        if (Objects.requireNonNull(stage.getScene()).getRoot() instanceof Pane) {
            rootPane = (Pane) stage.getScene().getRoot();
        } else {
            return;
        }

        // In order to set the bounds of the stage size correctly, the additional space that is
        // used by the window decoration of the OS needs to be respected.
        double stageDecorationWidth = stage.getWidth() - rootPane.getWidth();
        double stageDecorationHeight = stage.getHeight() - rootPane.getHeight();

        if (rootPane.getMinWidth() > 0) {
            stage.setMinWidth(rootPane.getMinWidth() + stageDecorationWidth);
        } else {
            stage.setMinWidth(stage.getWidth());
        }

        if (rootPane.getMinHeight() > 0) {
            stage.setMinHeight(rootPane.getMinHeight() + stageDecorationHeight);
        } else {
            stage.setMinHeight(stage.getHeight());
        }

        if (rootPane.getMaxWidth() > 0 && rootPane.getMaxWidth() >= rootPane.getMinWidth()) {
            stage.setMaxWidth(rootPane.getMaxWidth() + stageDecorationWidth);
        }

        if (rootPane.getMaxHeight() > 0 && rootPane.getMaxHeight() >= rootPane.getMinHeight()) {
            stage.setMaxHeight(rootPane.getMaxHeight() + stageDecorationHeight);
        }
    }

    /**
     * Sets the application icon to the specified {@code stage}.
     *
     * @param stage the {@code Stage} that will be iconified
     */
    public static void setApplicationIconToStage(@NotNull Stage stage) {
        URL iconUrl = StageUtil.class.getClassLoader().getResource("images/logo.png");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toString()));
        }
    }

    /**
     * Closes the stage of a {@code Parent} instance.
     *
     * @param parent the {@code Parent} whose stage will be closed
     * @throws NullPointerException if the {@code parent} has not been added to a scene or the scene
     *                              of the {@code parent} has not been added to a stage
     */
    public static void closeStage(@NotNull Parent parent) {
        Validate.notNull(parent, "Parent must not be null.");

        Stage stage = (Stage) Objects.requireNonNull(parent.getScene()).getWindow();
        Objects.requireNonNull(stage).close();
    }
}
