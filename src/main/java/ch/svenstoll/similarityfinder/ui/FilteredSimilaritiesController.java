/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.domain.Article;
import ch.svenstoll.similarityfinder.domain.FilteredSimilarities;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * A JavaFx controller class that controls the view that displays articles with similar content.
 */
@Singleton
public final class FilteredSimilaritiesController implements Controller {
    @NotNull
    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @NotNull
    private final FilteredSimilarities filteredSimilarities;

    @FXML
    private Pane similaritiesRootPane;
    @FXML
    private TreeTableView<Article> similaritiesTableView;
    @FXML
    private TreeTableColumn<Article, Integer> idColumn;
    @FXML
    private TreeTableColumn<Article, String> titleColumn;
    @FXML
    private TreeTableColumn<Article, LocalDate> dateColumn;
    @FXML
    private TreeTableColumn<Article, String> mediumColumn;
    @FXML
    private TreeTableColumn<Article, String> lengthColumn;
    @FXML
    private Label counterLabel;
    @FXML
    private Button copyAllButton;

    /**
     * Constructs a {@code FilteredSimilaritiesController}.
     *
     * @param similarities the {@code FilteredSimilarities} instance that provides the data to be
     *                     displayed
     */
    @Inject
    public FilteredSimilaritiesController(@NotNull FilteredSimilarities similarities) {
        this.filteredSimilarities
                = Validate.notNull(similarities, "FilteredSimilarities must not be null.");
    }

    /**
     * Initializes JavaFX UI controls of this {@code FilteredSimilaritiesController}. This method
     * will be called when the fxml file is loaded by a {@code FXMLLoader}.
     */
    @FXML
    private void initialize() {
        filteredSimilarities.similaritiesProperty().addListener((InvalidationListener) observable ->
                Platform.runLater(this::updateSimilaritiesTableItems));

        initializeSimilaritiesTable();
    }

    /**
     * Initializes the {@link #similaritiesTableView}.
     */
    private void initializeSimilaritiesTable() {
        idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
        mediumColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("medium"));
        dateColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("publicationDate"));
        dateColumn.setCellFactory(column -> new TreeTableCell<Article, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(DATE_FORMATTER));
                }
            }
        });
        lengthColumn.setCellValueFactory(cellDataFeatures ->{
            String length = String.valueOf(Objects.requireNonNull(
                    cellDataFeatures.getValue().getValue().getContent()).length());
            return new ReadOnlyStringWrapper(length);
        });

        similaritiesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        similaritiesTableView.setRowFactory(treeTableView -> createRowWithContextMenu());

        updateSimilaritiesTableItems();
    }

    /**
     * Creates a {@code TreeTableRow} with a context menu that allows a user to copy the IDs of the
     * articles that are selected in the {@link #similaritiesTableView}. The context menu will
     * only be displayed if the row is not empty.
     *
     * @return a {@code TreeTableRow} with an adjusted {@code contextMenuProperty}
     */
    private @NotNull TreeTableRow<Article> createRowWithContextMenu() {
        final TreeTableRow<Article> row = new TreeTableRow<>();
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem copyIdItem = new MenuItem();

        copyIdItem.setOnAction(event -> copyIdsOfSelectedRowsToClipboard());
        contextMenu.setOnShowing(event -> {
            if (similaritiesTableView.getSelectionModel().getSelectedItems().size() <= 1) {
                copyIdItem.setText("Copy ID");
            } else {
                copyIdItem.setText("Copy IDs");
            }
        });
        contextMenu.getItems().add(copyIdItem);

        row.contextMenuProperty().bind(Bindings.when(row.emptyProperty())
                .then((ContextMenu) null).otherwise(contextMenu));

        return row;
    }

    /**
     * Copies the IDs of the articles that are selected in the {@link #similaritiesTableView} to
     * the system clipboard. The IDs will be separated by a comma.
     */
    private void copyIdsOfSelectedRowsToClipboard() {
        StringBuilder sb = new StringBuilder();
        similaritiesTableView.getSelectionModel().getSelectedItems().forEach(treeItem ->
                sb.append(treeItem.getValue().getId()).append(", "));

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        clipboard.setContent(content);
    }

    /**
     * Updates the items that are displayed in the {@link #similaritiesTableView}. The
     * {@link #counterLabel} and the state of the {@link #copyAllButton} will also be adjusted.
     */
    private void updateSimilaritiesTableItems() {
        List<Set<Article>> similarities = filteredSimilarities.getSimilarities();
        TreeItem<Article> root = generateSimilaritiesTree(similarities);
        similaritiesTableView.setShowRoot(false);
        similaritiesTableView.setRoot(root);

        if (filteredSimilarities.getSimilarities().size() == 0) {
            counterLabel.setText("");
            copyAllButton.setDisable(true);
        } else {
            String labelText = filteredSimilarities.countAllArticles() + " Similarities Found";
            counterLabel.setText(labelText);
            copyAllButton.setDisable(false);
        }
    }

    /**
     * Generates a tree with a maximum height of 2 that represents the given list of similarity
     * sets.
     * <p>
     * The root element of the tree will always be empty, because it will not be shown in the
     * {@link #similaritiesTableView}. The second level contains an arbitrary article from each
     * similarity set. For all remaining articles in a similarity set, a child node will be added
     * in the third level of the tree.
     * </p>
     *
     * @param similarities a list of sets that contain articles with similar content
     * @return an empty {@code TreeItem} that is the root of the similarities tree
     * @throws IllegalArgumentException if {@code similarities} was {@code null} or contained
     *                                  {@code null} elements.
     */
    private @NotNull TreeItem<Article> generateSimilaritiesTree(
            @NotNull List<Set<Article>> similarities) {
        Validate.notNull(similarities, "Similarities must not be null.");
        Validate.noNullElements(similarities, "Similarities must not contain null values.");

        final TreeItem<Article> root = new TreeItem<>();

        for (Set<Article> set : similarities) {
            Iterator<Article> iterator = set.iterator();

            final TreeItem<Article> childLevel1 = new TreeItem<>(iterator.next());
            root.getChildren().add(childLevel1);

            while (iterator.hasNext()) {
                final TreeItem<Article> childLevel2 = new TreeItem<>(iterator.next());
                childLevel1.getChildren().add(childLevel2);
            }
        }

        return root;
    }

    /**
     * Copies the ID of all articles that are present in the {@link #similaritiesTableView} to
     * the system clipboard. The IDs will be separated with a comma.
     */
    @FXML
    private void copyAllIds() {
        final List<Article> articles = new ArrayList<>();
        filteredSimilarities.getSimilarities().forEach(articles::addAll);
        articles.sort(Comparator.comparingInt(Article::getId));

        StringBuilder sb = new StringBuilder();
        articles.forEach(contribution -> sb.append(contribution.getId()).append(", "));

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        clipboard.setContent(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Pane loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(
                getClass().getClassLoader().getResource("fxml/filtered-similarities.fxml"));
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
    public @Nullable Pane getRoot() {
        return similaritiesRootPane;
    }
}
