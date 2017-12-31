package ch.svenstoll.similarityfinder.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static ch.svenstoll.similarityfinder.domain.Filter.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class FilterConfigTest {
    private static final double FILTER_1_SIMILARITY_THRESHOLD = 0.5;
    private static final List<String> FILTER_1_SELECTED_MEDIA
            = Arrays.asList("medium 1", "medium 2");
    private static final LocalDate FILTER_1_START_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate FILTER_1_END_DATE = LocalDate.of(2000, 1, 1);
    private static final String FILTER_1_TITLE = "title 1";
    private static final int FILTER_1_MIN_LETTERS = 1000;
    private static final boolean FILTER_1_RELEVANT_ONLY = true;

    @Test
    public void constructor_givenNameIsNotProvided_shouldCreateNewFilterConfig() {
        // Given:
        LocalDateTime lastEdited = LocalDateTime.now();
        Filter filter = getMockedFilter1();

        // When:
        FilterConfig config = new FilterConfig(null, lastEdited, filter);

        // Then:
        assertEquals(null, config.getName());
        assertEquals(lastEdited, config.getLastEdited());
        checkConfigBasedOnFilter1Values(config);
    }

    @Test
    public void constructor_givenLastEditedIsNotProvided_shouldCreateNewFilterConfig() {
        // Given:
        String name = "test name";
        Filter filter = getMockedFilter1();

        // When:
        FilterConfig config = new FilterConfig(name, null, filter);

        // Then:
        assertEquals(name, config.getName());
        assertEquals(null, config.getLastEdited());
        checkConfigBasedOnFilter1Values(config);
    }

    @Test
    public void constructor_givenFilterIsNotProvided_shouldCreateNewFilterConfig() {
        // Given:
        String name = "test name";
        LocalDateTime lastEdited = LocalDateTime.now();

        // When:
        FilterConfig config = new FilterConfig(name, lastEdited, null);

        // Then:
        assertEquals(name, config.getName());
        assertEquals(lastEdited, config.getLastEdited());
        checkConfigBasedOnDefaultFilterValues(config);
    }

    private Filter getMockedFilter1() {
        Filter filter = mock(Filter.class);
        given(filter.getSimilarityThreshold()).willReturn(FILTER_1_SIMILARITY_THRESHOLD);
        given(filter.getNamesOfSelectedMedia()).willReturn(FILTER_1_SELECTED_MEDIA);
        given(filter.getFromDate()).willReturn(FILTER_1_START_DATE);
        given(filter.getToDate()).willReturn(FILTER_1_END_DATE);
        given(filter.getTitle()).willReturn(FILTER_1_TITLE);
        given(filter.getMinLetters()).willReturn(FILTER_1_MIN_LETTERS);
        given(filter.isRelevantOnly()).willReturn(FILTER_1_RELEVANT_ONLY);

        return filter;
    }

    private void checkConfigBasedOnFilter1Values(FilterConfig config) {
        assertEquals(FILTER_1_SIMILARITY_THRESHOLD, config.getSimilarityThreshold(), 0.001);
        assertThat(config.getSelectedMedia(),
                containsInAnyOrder(FILTER_1_SELECTED_MEDIA.toArray()));
        assertEquals(FILTER_1_START_DATE, config.getFromDate());
        assertEquals(FILTER_1_END_DATE, config.getToDate());
        assertEquals(FILTER_1_TITLE, config.getTitle());
        assertEquals(FILTER_1_MIN_LETTERS, config.getMinLetters());
        assertEquals(FILTER_1_RELEVANT_ONLY, config.isRelevantOnly());
    }

    private void checkConfigBasedOnDefaultFilterValues(FilterConfig config) {
        assertEquals(DEFAULT_SIMILARITY_THRESHOLD, config.getSimilarityThreshold(), 0.001);
        assertEquals(0, config.getSelectedMedia().size());
        assertEquals(DEFAULT_FROM_DATE, config.getFromDate());
        assertEquals(DEFAULT_TO_DATE, config.getToDate());
        assertEquals(DEFAULT_TITLE, config.getTitle());
        assertEquals(DEFAULT_MIN_LETTERS, config.getMinLetters());
        assertEquals(DEFAULT_RELEVANT_ONLY, config.isRelevantOnly());
    }
}
