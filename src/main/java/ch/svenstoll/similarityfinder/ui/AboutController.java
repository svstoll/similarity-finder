/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * A JavaFx controller class that controls the view of the about dialog.
 */
public class AboutController implements Controller, Stageable {
    @NotNull
    private static final String GITHUB_REPO_URL = "https://github.com/svstoll/similarity-finder";
    @NotNull
    private static final String LICENSES_URL
            = "https://github.com/svstoll/similarity-finder/blob/master/README.md";

    @FXML
    private Pane aboutRootPane;
    @FXML
    private Hyperlink gitHubLink;
    @FXML
    private Hyperlink licensesLink;

    /**
     * Initializes JavaFX UI controls of this {@code AboutController}. This method will be called
     * when the fxml file is loaded by a {@code FXMLLoader}.
     */
    @FXML
    private void initialize() {
        initializeHyperlinks();
        aboutRootPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getRoot() == aboutRootPane) {
                initializeSceneShortcuts(newValue);
            }
        });
    }

    /**
     * Initializes the hyperlinks specified in the fxml file of this {@code AboutController}.
     */
    private void initializeHyperlinks() {
        gitHubLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI(GITHUB_REPO_URL));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });

        licensesLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI(LICENSES_URL));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Initializes shortcuts that can be used in the {@code Scene} of this {@code AboutController}:
     * <p><ul>
     * <li>ESC: Closes the stage of this {@code AboutController}.
     * </ul><p>
     * <p>NOTE: These shortcuts should only be initialized if the view of this {@code
     * AboutController} has been opened in a separate stage.</p>
     *
     * @param scene the {@code Scene} where the {@link #aboutRootPane} is currently shown
     * @throws IllegalArgumentException if {@code scene} was {@code null}
     */
    private void initializeSceneShortcuts(Scene scene) {
        Validate.notNull(scene);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) { // Esc
                StageUtil.closeStage(aboutRootPane);
                event.consume();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openInNewStage() {
        Pane rootPane = loadFxml();
        if (rootPane != null) {
            StageUtil.openNewStage(rootPane, "About", Modality.APPLICATION_MODAL, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Pane loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxml/about.fxml"));
        fxmlLoader.setControllerFactory(param -> this);

        Pane rootPane = null;
        try {
            rootPane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootPane;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized @Nullable Pane getRoot() {
        return aboutRootPane;
    }
}
