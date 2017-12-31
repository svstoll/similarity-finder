package ch.svenstoll.similarityfinder.domain;

import ch.svenstoll.similarityfinder.dao.DatabaseAccessException;
import ch.svenstoll.similarityfinder.dao.DatabaseAccess;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


import static ch.svenstoll.similarityfinder.domain.Filter.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

public class FilterTest {
    private Filter filter;
    private DatabaseAccess dataAccess;
    private FilteredSimilarities similarities;
    private SimilarityDetector detector;
    private Settings settings;

    @Before
    public void setup() {
        dataAccess = mock(DatabaseAccess.class);
        similarities = mock(FilteredSimilarities.class);
        detector = mock(SimilarityDetector.class);
        settings = mock(Settings.class);
        filter = new Filter(dataAccess, similarities, detector, settings);

        verify(dataAccess, timeout(50).times(1)).queryAllMedia();
    }

    @Test
    public void constructor_givenDatabaseAccessExceptionOccurs_shouldHaveEmptyMediaProperty() {
        // Given:
        willThrow(new DatabaseAccessException()).given(dataAccess).queryAllMedia();

        // When:
        Filter filter = new Filter(dataAccess, similarities, detector, settings);

        // Then:
        // Due to the @Before annotation, the queryAllMedia method has already been called once.
        // Therefore, we need to verify that this method has been called twice during the
        // execution of this test.
        verify(dataAccess, timeout(50).times(2)).queryAllMedia();
        assertEquals(0, filter.mediaProperty().size());
    }

    @Test
    public void constructor_givenDatabaseAccessReturnsMedia_shouldHaveUpdatedMediaProperty() {
        // Given:
        List<Medium> expected = Arrays.asList(new Medium("1"), new Medium("2"), new Medium("3"));
        given(dataAccess.queryAllMedia()).willReturn(expected);

        // When:
        filter = new Filter(dataAccess, similarities, detector, settings);

        // Then:
        // Due to the @Before annotation the queryAllMedia method has already been called once.
        // Therefore, we need to verify that this method has been called twice during the
        // execution of this test.
        verify(dataAccess, timeout(50).times(2)).queryAllMedia();
        assertThat(filter.getMedia(), containsInAnyOrder(expected.toArray()));
    }

    @Test(expected = DatabaseAccessException.class)
    public void
    findArticlesWithSimilarContent_givenDatabaseAccessExceptionOccurs_shouldThrowException() {
        // Given:
        willThrow(new DatabaseAccessException())
                .given(dataAccess).queryArticles(any(Filter.class));

        // When:
        filter.findArticlesWithSimilarContent();
    }

    @Test(expected = DetectionAbortedException.class)
    public void
    findArticlesWithSimilarContent_givenDetectionAbortedExceptionOccurs_shouldThrowException() {
        // Given:
        willThrow(new DetectionAbortedException())
                .given(detector).detectArticlesWithSimilarContents(anyList(), anyDouble());

        // When:
        filter.findArticlesWithSimilarContent();
    }

    @Test
    public void
    findArticlesWithSimilarContent_givenSuccessfulExecution_shouldUpdateFilteredSimilarities() {
        // When:
        ObservableList<Set<Article>> result
                = FXCollections.observableArrayList(filter.findArticlesWithSimilarContent());

        // Then:
        verify(similarities, times(1)).setSimilarities(result);
    }

    @Test
    public void reset_givenAllValuesAreChanged_shouldResetAllValuesToDefault() {
        // Given:
        filter.setSimilarityThreshold(0.25);
        filter.setFromDate(LocalDate.of(2000, 1, 1));
        filter.setToDate(LocalDate.of(2017, 1, 1));
        filter.setTitle("test");
        filter.setMinLetters(1000);
        filter.setRelevantOnly(true);

        // When:
        filter.reset();

        // Then:
        assertEquals(DEFAULT_SIMILARITY_THRESHOLD, filter.getSimilarityThreshold(), 0.001);
        assertEquals(DEFAULT_FROM_DATE, filter.getFromDate());
        assertEquals(DEFAULT_TO_DATE, filter.getToDate());
        assertEquals(DEFAULT_TITLE, filter.getTitle());
        assertEquals(DEFAULT_MIN_LETTERS, filter.getMinLetters());
        assertEquals(DEFAULT_RELEVANT_ONLY, filter.isRelevantOnly());
    }

    @Test(expected = IllegalArgumentException.class)
    public void restoreFromConfig_givenConfigIsNotProvided_shouldThrowException() {
        // When:
        filter.restoreFromConfig(null);
    }

    @Test
    public void restoreFromConfig_givenConfigIsProvided_shouldRestoreAllFilterValues() {
        // Given:
        double similarityThreshold = 0.5;
        Medium m1 = new Medium("medium 1");
        Medium m2 = new Medium("medium 2");
        m1.setSelected(true);
        m2.setSelected(true);
        List<Medium> selectedMedium = Arrays.asList(m1, m2);
        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2010, 1, 1);
        String title = "title";
        int minLetters = 1000;
        boolean relevantOnly = true;

        FilterConfig config = new FilterConfig("test config", LocalDateTime.now(), null);
        config.setSimilarityThreshold(similarityThreshold);
        config.setSelectedMedia(selectedMedium.stream().map(Medium::getName)
                .collect(Collectors.toList()));
        config.setFromDate(startDate);
        config.setToDate(endDate);
        config.setTitle(title);
        config.setMinLetters(minLetters);
        config.setRelevantOnly(relevantOnly);

        filter.mediaProperty().set(FXCollections.observableArrayList(selectedMedium));

        // When:
        filter.restoreFromConfig(config);

        // Then:
        List<Medium> actualSelectedMedium = Objects.requireNonNull(filter.getMedia())
                .stream().filter(Medium::isSelected).collect(Collectors.toList());
        assertEquals(similarityThreshold, filter.getSimilarityThreshold(), 0.001);
        assertThat(selectedMedium, containsInAnyOrder(actualSelectedMedium.toArray()));
        assertEquals(startDate, filter.getFromDate());
        assertEquals(endDate, filter.getToDate());
        assertEquals(title, filter.getTitle());
        assertEquals(minLetters, filter.getMinLetters());
        assertEquals(relevantOnly, filter.isRelevantOnly());
    }

    @Test
    public void getNamesOfSelectedMedia_givenMediaContainsNullValues_shouldReturnEmptyList() {
        // Given:
        filter.mediaProperty().setValue(null);

        // When:
        List<String> actual = filter.getNamesOfSelectedMedia();

        // Then:
        assertEquals(0, actual.size());
    }

    @Test
    public void getNamesOfSelectedMedia_givenMediaIsEmpty_shouldReturnEmptyList() {
        // Given
        filter.mediaProperty().setValue(FXCollections.observableArrayList());

        // When:
        List<String> actual = filter.getNamesOfSelectedMedia();

        // Then:
        assertEquals(0, actual.size());
    }

    @Test
    public void getNamesOfSelectedMedia_givenMediaIsNotEmpty_shouldReturnSelectedMediaNames() {
        // Given:
        Medium m1 = new Medium("medium 1");
        Medium m2 = new Medium("medium 2");
        Medium m3 = new Medium("medium 3");
        m1.setSelected(true);
        m2.setSelected(true);
        List<Medium> media = Arrays.asList(m1, m2, m3);
        filter.mediaProperty().setValue(FXCollections.observableArrayList(media));

        // When:
        List<String> actual = filter.getNamesOfSelectedMedia();

        // Then:
        List<String> expected = Arrays.asList("medium 1", "medium 2");
        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }
}
