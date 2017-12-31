package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.domain.Article;
import ch.svenstoll.similarityfinder.domain.FilteredSimilarities;
import ch.svenstoll.similarityfinder.ui.FilteredSimilaritiesController;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeTableRow;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

public class FilteredSimilaritiesControllerTest extends ApplicationTest {
    private FilteredSimilaritiesController filteredSimilaritiesController;
    private FilteredSimilarities filteredSimilarities;
    private ObservableList<Set<Article>> similarities;
    private ListProperty<Set<Article>> similaritiesProperty;
    private FxRobot robot;

    @Override
    public void start(Stage primaryStage) {
        filteredSimilarities = mock(FilteredSimilarities.class);
        similarities = FXCollections.observableArrayList();
        similaritiesProperty = new SimpleListProperty<>(similarities);
        given(filteredSimilarities.similaritiesProperty()).willReturn(similaritiesProperty);
        given(filteredSimilarities.getSimilarities()).willReturn(similarities);

        filteredSimilaritiesController = new FilteredSimilaritiesController(filteredSimilarities);
        Parent root = filteredSimilaritiesController.loadFxml();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Before
    public void setUp() {
        robot = new FxRobot();
    }

    @After
    public void cleanUp() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    public void similaritiesTable_givenSimilaritiesAreEmpty_shouldAdjustGuiElements() {
        // Then:
        Set<TreeTableRow> rows = robot.lookup(".tree-table-row-cell").queryAll();
        long rowCount = rows.stream().filter(row -> row.getTreeItem() != null).count();

        assertEquals(0, rowCount);
        verifyThat("#counterLabel", hasText(""));
        verifyThat("#copyAllButton", isDisabled());
    }

    @Test
    public void similaritiesTable_givenSimilaritiesAreNotEmpty_shouldAdjustGuiElements() {
        // Given:
        Article c1 = new Article(1);
        Article c2 = new Article(2);
        Article c3 = new Article(3);
        Article c4 = new Article(4);
        Set<Article> set1 = new HashSet<>(Arrays.asList(c1, c2));
        Set<Article> set2 = new HashSet<>(Arrays.asList(c3, c4));
        List<Set<Article>> sets = Arrays.asList(set1, set2);

        similarities = FXCollections.observableArrayList(sets);
        given(filteredSimilarities.getSimilarities()).willReturn(similarities);
        given(filteredSimilarities.countAllArticles()).willReturn(set1.size() + set2.size());
        similaritiesProperty.set(similarities);

        robot.sleep(100);

        // Then:
        int expectedSize = 0;
        for (Set<Article> set : similarities) {
            expectedSize += set.size();
        }

        Set<TreeTableRow> rows = robot.lookup(".tree-table-row-cell").queryAll();
        long rowCount = rows.stream().filter(row -> row.getTreeItem() != null).count();

        assertEquals(similarities.size(), rowCount);
        verifyThat("#counterLabel", hasText(expectedSize + " Similarities Found"));
        verifyThat("#copyAllButton", isEnabled());
    }

    @Test
    public void contextMenu_givenCopyIdIsSelected_shouldCopyIdToSystemClipboard()
            throws IOException, UnsupportedFlavorException {
        // Given:
        Article c1 = new Article(1);
        Article c2 = new Article(2);
        Set<Article> set1 = new HashSet<>(Arrays.asList(c1, c2));
        List<Set<Article>> sets = Collections.singletonList(set1);

        similarities = FXCollections.observableArrayList(sets);
        given(filteredSimilarities.getSimilarities()).willReturn(similarities);
        similaritiesProperty.set(similarities);

        robot.sleep(100);

        // When:
        TreeTableRow firstRow = robot.lookup(".tree-table-row-cell").nth(0).query();
        robot.rightClickOn(firstRow);
        Node copyIdMenuItem = robot.lookup(".context-menu .menu-item").nth(0).query();
        robot.clickOn(copyIdMenuItem);

        // Then:
        Article copiedArticle = (Article) firstRow.getTreeItem().getValue();
        String expected = copiedArticle.getId() + ", ";
        String actual = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                .getData(DataFlavor.stringFlavor);
        assertEquals(expected, actual);
    }

    @Test
    public void copyAllIds_givenSimilaritiesAreNotEmpty_shouldCopyAllIdsToSystemClipboard()
            throws IOException, UnsupportedFlavorException {
        // Given:
        Article c1 = new Article(1);
        Article c2 = new Article(2);
        Set<Article> set1 = new HashSet<>(Arrays.asList(c1, c2));
        List<Set<Article>> sets = Collections.singletonList(set1);

        similarities = FXCollections.observableArrayList(sets);
        given(filteredSimilarities.getSimilarities()).willReturn(similarities);
        similaritiesProperty.set(similarities);

        robot.sleep(100);

        // When:
        robot.clickOn("#copyAllButton");

        // Then:
        String expected = "1, 2, ";
        String actual = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                .getData(DataFlavor.stringFlavor);
        assertEquals(expected, actual);
    }
}
