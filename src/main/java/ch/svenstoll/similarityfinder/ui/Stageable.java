package ch.svenstoll.similarityfinder.ui;

/**
 * An interface that may be implemented by a JavaFX controller whose view is intended to be
 * displayed in its own stage.
 */
public interface Stageable {
    /**
     * Opens a new JavaFX {@code Stage} that contains the view of the implementing controller class.
     */
    void openInNewStage();
}
