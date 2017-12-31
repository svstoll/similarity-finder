/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder;

import ch.svenstoll.similarityfinder.domain.Settings;
import ch.svenstoll.similarityfinder.ui.AppController;
import ch.svenstoll.similarityfinder.ui.StageUtil;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * The main class of this application where the JavaFX user interface will be launched.
 */
public final class App extends Application {
    private static final int APPLICATION_WIDTH = 1000;
    private static final int APPLICATION_HEIGHT = 750;
    private static final int APPLICATION_MIN_WIDTH = 850;
    private static final int APPLICATION_MIN_HEIGHT = 600;

    @NotNull
    private final Injector injector;
    @NotNull
    private final AppController appController;

    /**
     * Constructs an {@code App} instance and initializes the dependency injection framework Guice.
     */
    public App() {
        injector = Guice.createInjector(new ProductionModule());
        appController = injector.getInstance(AppController.class);
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The main entry point of this JavaFX applications. The start method is called after the
     * init method has returned, and after the system is ready for the application to begin running.
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage of this application
     */
    @Override
    public void start(Stage primaryStage) {
        Parent root = appController.loadFxml();

        if (root != null) {
            Scene scene = new Scene(root, APPLICATION_WIDTH, APPLICATION_HEIGHT);
            primaryStage.setScene(scene);
        }
        primaryStage.setMinWidth(APPLICATION_MIN_WIDTH);
        primaryStage.setMinHeight(APPLICATION_MIN_HEIGHT);
        primaryStage.setTitle("SimilarityFinder");
        StageUtil.setApplicationIconToStage(primaryStage);

        primaryStage.show();

        if (injector.getInstance(Settings.class).isFirstLaunch()) {
            appController.showFirstLaunchDialog();
        }
    }

    /**
     * This method is called when the application should stop and tries to shutdown ongoing
     * background tasks.
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     */
    @Override
    public void stop() {
        appController.stopOngoingBackgroundTasks();
    }
}
