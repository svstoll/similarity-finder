package ch.svenstoll.similarityfinder.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit.ApplicationTest;

import static junit.framework.TestCase.assertEquals;

public class AboutControllerTest extends ApplicationTest {
    private FxRobot robot;

    @Override
    public void start(Stage primaryStage) {
        AboutController aboutController = new AboutController();
        Parent root = aboutController.loadFxml();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Before
    public void setUp() {
        robot = new FxRobot();
    }

    @Test
    public void closeAboutStage_givenShortcutIsPressed_shouldCloseStage() {
        // When:
        robot.push(KeyCode.ESCAPE);

        // Then:
        assertEquals(0, listWindows().size());
    }
}
