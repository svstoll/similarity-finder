package ch.svenstoll.similarityfinder.ui;

import javafx.scene.Parent;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that should be implemented by all JavaFx controllers.
 */
public interface Controller {
    /**
     * Loads the fxml file of the {@code Controller} instance.
     *
     * @return the root of the controller
     */
    @Nullable Parent loadFxml();

    /**
     * Returns the root of the {@code Controller} instance.
     *
     * @return the root of the {@code Controller} instance.
     */
    @Nullable Parent getRoot();
}
