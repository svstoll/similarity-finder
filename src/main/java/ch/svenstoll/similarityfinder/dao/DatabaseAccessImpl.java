/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.Article;
import ch.svenstoll.similarityfinder.domain.Filter;
import ch.svenstoll.similarityfinder.domain.Medium;
import com.google.inject.Inject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@code DatabaseAccess} that allows the processing of database queries.
 */
public final class DatabaseAccessImpl implements DatabaseAccess {
    @NotNull
    static final String MEDIUM_RELATION = "medium";
    @NotNull
    static final String MEDIUM_NAME_COLUMN = "name";
    @NotNull
    static final String ARTICLE_RELATION = "article";
    @NotNull
    static final String ARTICLE_TITLE_COLUMN = "title";
    @NotNull
    static final String ARTICLE_CONTENT_COLUMN = "content";
    @NotNull
    static final String ARTICLE_MEDIUM_COLUMN = "medium";
    @NotNull
    static final String ARTICLE_PUBLICATION_DATE_COLUMN = "publicationDate";
    @NotNull
    static final String ARTICLE_RELEVANT_COLUMN = "relevant";

    @NotNull
    private static final String MEDIA_QUERY
            = "SELECT " + MEDIUM_NAME_COLUMN + " " +
              "FROM " + MEDIUM_RELATION + ";";
    @NotNull
    private static final String ARTICLES_QUERY
            = "SELECT * " +
              "FROM " + ARTICLE_RELATION + " " +
              "WHERE " +
              "  (" + ARTICLE_MEDIUM_COLUMN + " IN(SELECT * FROM UNNEST(?)) OR ? = '{}') AND " +
              "  COALESCE(" + ARTICLE_PUBLICATION_DATE_COLUMN + " >= ?, TRUE) AND " +
              "  COALESCE(" + ARTICLE_PUBLICATION_DATE_COLUMN + " <= ?, TRUE) AND " +
              "  LOWER(" + ARTICLE_TITLE_COLUMN + ") LIKE LOWER(?) AND " +
              "  LENGTH(" + ARTICLE_CONTENT_COLUMN + ") >= ? AND " +
              "  (? = FALSE OR " + ARTICLE_RELEVANT_COLUMN + " = TRUE );";

    @NotNull
    private final DatabaseConnectionProvider connectionProvider;

    @Nullable
    private Connection connection;

    /**
     * Creates a {@code DatabaseAccessImpl}. A shutdown hook will be added that tries to close
     * the database connection when the application is closed.
     *
     * @param connectionProvider An instance of {@code IConnectionProvider} that provides
     *                           connections to the database.
     * @throws IllegalArgumentException if {@code connectionProvider} was {@code null}
     */
    @Inject
    public DatabaseAccessImpl(@NotNull DatabaseConnectionProvider connectionProvider) {
        this.connectionProvider
                = Validate.notNull(connectionProvider, "ConnectionProvider must not be null.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DbUtils.close(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * Checks if a connection exists that has not been closed. If this is not the case, a new
     * connection will be requested.
     *
     * @throws DatabaseAccessException if there was an error while checking if the connection was
     *                                 closed or while requesting a new connection.
     */
    private void checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = connectionProvider.getDbConnection();
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e.getMessage(), e.getCause());
        }
    }

    /**
     * {@inheritDoc}
     */
    public @NotNull List<Medium> queryAllMedia() {
        List<Medium> mediumList = new ArrayList<>();

        checkConnection();

        try (Statement statement = Objects.requireNonNull(connection).createStatement()) {
            ResultSet resultSet = statement.executeQuery(MEDIA_QUERY);

            while (resultSet.next()) {
                mediumList.add(new Medium(resultSet.getString(MEDIUM_NAME_COLUMN)));
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e.getMessage(), e);
        }

        return mediumList;
    }

    /**
     * Queries the database for articles that meet the requirements that can be specified by
     * the provided {@code filter}.
     *
     * <p>
     * To prevent SQL injections, a {@code PreparedStatement} is used.
     * </p>
     *
     * @param filter a {@code Filter} that specifies the contributions to be returned
     * @return a list of {@code Article} instances that meet the requirements of the {@code filter}
     * @throws IllegalArgumentException if {@code filter} was {@code null}
     * @throws DatabaseAccessException if an error occurred while querying the database or the
     *                                 thread executing this method was interrupted
     */
    public @NotNull List<Article> queryArticles(@NotNull Filter filter) {
        Validate.notNull(filter, "Filter must not be null.");

        checkConnection();

        List<Article> articles;
        try (PreparedStatement statement
                     = Objects.requireNonNull(connection).prepareStatement(ARTICLES_QUERY)) {
            setArticlesQueryParameters(connection, statement, filter);
            ResultSet resultSet = statement.executeQuery();
            articles = generateArticlesFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new DatabaseAccessException(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabaseAccessException(e.getMessage(), e);
        }

        return articles;
    }

    /**
     * Sets the parameters for a {@code PreparedStatement} instance that was created using an
     * {@link #ARTICLES_QUERY}.
     *
     * @param connection a {@code Connection} that was used to prepare the {@code statement}
     * @param statement a {@code PreparedStatement} that was created using an
     *                  {@link #ARTICLES_QUERY}
     * @param filter a {@code Filter} that provides the parameter data
     * @throws IllegalArgumentException if any parameter was {@code null}
     * @throws SQLException if the query parameters could not be set
     */
    private void setArticlesQueryParameters(@NotNull Connection connection,
                                            @NotNull PreparedStatement statement,
                                            @NotNull Filter filter) throws SQLException {
        Validate.notNull(statement, "Statement must not be null.");
        Validate.notNull(filter, "Filter must not be null.");

        Array selectedMedia
                = connection.createArrayOf("varchar", filter.getNamesOfSelectedMedia().toArray());
        statement.setArray(1, selectedMedia);
        statement.setArray(2, selectedMedia);
        Date start = filter.getFromDate() != null ? Date.valueOf(filter.getFromDate()) : null;
        statement.setDate(3, start);
        Date end = filter.getToDate() != null ? Date.valueOf(filter.getToDate()) : null;
        statement.setDate(4, end);
        statement.setString(5, "%" + filter.getTitle() + "%");
        statement.setInt(6, filter.getMinLetters());
        statement.setBoolean(7, filter.isRelevantOnly());
    }

    /**
     * Generates a list of {@code Article} instances from a {@code ResultSet} that was
     * received using an {@link #ARTICLES_QUERY}. Multithreading is used to improve performance
     * while generating the content n-grams of the articles.
     *
     * @param resultSet a {@code ResultSet} that was received from an {@code #ARTICLES_QUERY}
     * @return a list of {@code Article} instances generated from the {@code resultSet}
     * @throws IllegalArgumentException if the {@code resultSet} was {@code null}
     * @throws SQLException if an error occurred while accessing the {@code resultSet}
     * @throws InterruptedException if the thread executing this method was interrupted
     */
    private @NotNull List<Article> generateArticlesFromResultSet(
            @NotNull ResultSet resultSet) throws SQLException, InterruptedException {
        Validate.notNull(resultSet, "Result must not be null.");

        List<Article> articles = Collections.synchronizedList(new ArrayList<>());

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String title = resultSet.getString(ARTICLE_TITLE_COLUMN);
            String content = resultSet.getString(ARTICLE_CONTENT_COLUMN);
            String mediaName = resultSet.getString(ARTICLE_MEDIUM_COLUMN);
            Date date = resultSet.getDate(ARTICLE_PUBLICATION_DATE_COLUMN);

            Runnable worker = () -> {
                // The worker is executed with minimal thread priority to keep the JavaFX thread
                // from starving which would result in UI freezes.
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

                Article article = new Article(id);
                article.setTitle(title);
                article.setContent(content);
                if (mediaName != null && !mediaName.isEmpty()) {
                    article.setMedium(new Medium(mediaName));
                }
                if (date != null) {
                    article.setPublicationDate(date.toLocalDate());
                }
                articles.add(article);
            };

            executorService.submit(worker);
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return articles;
    }
}
