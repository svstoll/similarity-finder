package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.Article;
import ch.svenstoll.similarityfinder.domain.Filter;
import ch.svenstoll.similarityfinder.domain.Medium;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.sql.*;
import java.util.List;

import static ch.svenstoll.similarityfinder.dao.DatabaseAccessImpl.*;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class DatabaseAccessImplTest {
    private DatabaseAccessImpl databaseAccess;
    private DatabaseConnectionProvider connectionProvider;

    @Before
    public void setUp() {
        connectionProvider = mock(DatabaseConnectionProvider.class);
        databaseAccess = new DatabaseAccessImpl(connectionProvider);
    }

    @Test(expected = DatabaseAccessException.class)
    public void queryMedia_givenDbQueryFailed_shouldThrowException() throws SQLException {
        // Given:
        Connection connection = mock(Connection.class);
        given(connectionProvider.getDbConnection()).willReturn(connection);
        given(connection.createStatement()).willThrow(new SQLException());

        // When:
        databaseAccess.queryAllMedia();
    }

    @Test
    public void queryMedia_givenDbQuerySuccessful_shouldReturnMedia() throws SQLException {
        // Given:
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        given(connectionProvider.getDbConnection()).willReturn(connection);
        given(connection.createStatement()).willReturn(statement);
        given(statement.executeQuery(anyString())).willReturn(resultSet);
        given(resultSet.next()).willReturn(true).willReturn(false);
        given(resultSet.getString(ArgumentMatchers.eq(MEDIUM_NAME_COLUMN))).willReturn("media name");

        // When:
        List<Medium> result = databaseAccess.queryAllMedia();

        // Then:
        assertEquals(1, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void queryContributions_givenFilterNotProvided_shouldThrowException() {
        // When:
        databaseAccess.queryArticles(null);
    }

    @Test(expected = DatabaseAccessException.class)
    public void queryContributions_givenDbQueryFailed_shouldThrowException() throws SQLException {
        // Given:
        Filter filter = mock(Filter.class);

        Connection connection = mock(Connection.class);
        given(connectionProvider.getDbConnection()).willReturn(connection);

        given(connection.prepareStatement(anyString())).willThrow(new SQLException());

        // When:
        databaseAccess.queryArticles(filter);
    }

    @Test
    public void queryContributions_givenDatabaseQuerySuccessful_shouldReturnContributions()
            throws SQLException {
        // Given:
        Filter filter = mock(Filter.class);

        Connection connection = mock(Connection.class);
        given(connectionProvider.getDbConnection()).willReturn(connection);
        given(connection.createArrayOf(anyString(), any())).willReturn(mock(Array.class));

        PreparedStatement statement = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(statement);

        ResultSet resultSet = mock(ResultSet.class);
        given(statement.executeQuery()).willReturn(resultSet);

        given(resultSet.next()).willReturn(true).willReturn(false);
        given(resultSet.getString(ARTICLE_TITLE_COLUMN)).willReturn("title");
        given(resultSet.getString(ARTICLE_CONTENT_COLUMN)).willReturn("text");
        given(resultSet.getString(ARTICLE_MEDIUM_COLUMN)).willReturn("media name");
        given(resultSet.getDate(ARTICLE_PUBLICATION_DATE_COLUMN))
                .willReturn(Date.valueOf("2000-01-01"));

        // When:
        List<Article> result =  databaseAccess.queryArticles(filter);

        // Then:
        assertEquals(1, result.size());
    }
}
