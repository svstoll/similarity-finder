package ch.svenstoll.similarityfinder.domain;

import ch.svenstoll.similarityfinder.dao.FilterConfigsAccess;
import ch.svenstoll.similarityfinder.dao.FilterConfigsAccessException;
import javafx.collections.FXCollections;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

public class FilterConfigsManagerTest {
    private FilterConfigsManager configsManager;
    private FilterConfigsAccess configsAccess;
    private Filter filter;

    @Before
    public void setUp() {
        configsAccess = mock(FilterConfigsAccess.class);
        filter = mock(Filter.class);
        configsManager = new FilterConfigsManager(configsAccess, filter);

        verify(configsAccess, timeout(500).times(1)).retrieveFilterConfigsFromFile();
    }

    @Test
    public void constructor_givenLoadingConfigsAsyncFails_shouldHaveEmptyConfigsList() {
        // Given:
        willThrow(new FilterConfigsAccessException()).given(configsAccess).retrieveFilterConfigsFromFile();

        // When:
        FilterConfigsManager configsManager = new FilterConfigsManager(configsAccess, filter);

        // Then:
        verify(configsAccess, timeout(50).times(2)).retrieveFilterConfigsFromFile();
        assertEquals(0, configsManager.getFilterConfigs().size());
    }

    @Test
    public void constructor_givenLoadingConfigsAsyncIsSuccessful_shouldUpdateConfigsList() {
        // Given:
        FilterConfig c1 = new FilterConfig("test config 1", LocalDateTime.now(), null);
        FilterConfig c2 = new FilterConfig("test config 2", LocalDateTime.now(), null);
        FilterConfig c3 = new FilterConfig("test config 3", LocalDateTime.now(), null);
        List<FilterConfig> expected = Arrays.asList(c1, c2, c3);
        when(configsAccess.retrieveFilterConfigsFromFile()).thenReturn(expected);

        // When:
        FilterConfigsManager configsManager = new FilterConfigsManager(configsAccess, filter);

        // Then:
        verify(configsAccess, timeout(500).times(2)).retrieveFilterConfigsFromFile();
        assertThat(configsManager.getFilterConfigs(), containsInAnyOrder(expected.toArray()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfig_givenNameIsNotProvided_shouldThrowException() {
        // When:
        configsManager.addFilterConfig(null);
    }

    @Test(expected = FilterConfigsAccessException.class)
    public void addConfig_givenAddingConfigFails_shouldThrowException() {
        // Given:
        willThrow(new FilterConfigsAccessException()).given(configsAccess)
                .saveFilterConfigsToFile(anyList());

        // When:
        configsManager.addFilterConfig("test");
    }

    @Test
    public void addConfig_givenNameIsProvided_shouldAddConfig() {
        // Given:
        String configName = "test config";
        LocalDateTime beforeConfigCreation = LocalDateTime.now();

        // When:
        configsManager.addFilterConfig(configName);

        // Then:
        Optional<FilterConfig> createdConfig = configsManager.getFilterConfigs()
                .filtered(config -> beforeConfigCreation.isBefore(config.getLastEdited())
                        || beforeConfigCreation.isEqual(config.getLastEdited()))
                .stream().findFirst();

        verify(configsAccess, times(1)).saveFilterConfigsToFile(anyList());
        assertTrue(createdConfig.isPresent());
        assertEquals(configName, createdConfig.get().getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadConfig_givenConfigIsNotProvided_shouldThrowException() {
        // When:
        configsManager.loadFilterConfig(null);
    }

    @Test
    public void loadConfig_givenConfigIsProvided_shouldRestoreConfig() {
        // Given:
        FilterConfig config = new FilterConfig("config", LocalDateTime.now(), null);

        // When:
        configsManager.loadFilterConfig(config);

        // Then:
        verify(filter, times(1)).restoreFromConfig(config);
    }


    @Test(expected = IllegalArgumentException.class)
    public void deleteConfig_givenConfigIsNotProvided_shouldThrowException() {
        // When:
        configsManager.removeFilterConfig(null);
    }

    @Test(expected = FilterConfigsAccessException.class)
    public void deleteConfig_givenDeletingConfigFails_shouldThrowException() {
        // Given:
        FilterConfig c1 = new FilterConfig("test config 1", LocalDateTime.now(), null);
        FilterConfig c2 = new FilterConfig("test config 2", LocalDateTime.now(), null);
        FilterConfig c3 = new FilterConfig("test config 3", LocalDateTime.now(), null);
        List<FilterConfig> initialConfigs = Arrays.asList(c1, c2, c3);
        configsManager.filterConfigsProperty().setValue(
                FXCollections.observableArrayList(initialConfigs));
        willThrow(new FilterConfigsAccessException()).given(configsAccess)
                .saveFilterConfigsToFile(anyList());

        // When:
        configsManager.removeFilterConfig(c1);

        // Then:
        assertThat(configsManager.getFilterConfigs(), containsInAnyOrder(initialConfigs.toArray()));
    }

    @Test
    public void deleteConfig_givenConfigIsNotInList_shouldLeaveConfigsListUnchanged() {
        // Given:
        FilterConfig config = new FilterConfig("config", LocalDateTime.now(), null);

        // When:
        configsManager.removeFilterConfig(config);

        // Then:
        verify(configsAccess, times(0)).saveFilterConfigsToFile(anyList());
        assertEquals(0, configsManager.getFilterConfigs().size());
    }

    @Test
    public void deleteConfig_givenConfigIsProvided_shouldDeleteConfig() {
        // Given:
        FilterConfig c1 = new FilterConfig("test config 1", LocalDateTime.now(), null);
        FilterConfig c2 = new FilterConfig("test config 2", LocalDateTime.now(), null);
        FilterConfig c3 = new FilterConfig("test config 3", LocalDateTime.now(), null);
        List<FilterConfig> initialConfigs = Arrays.asList(c1, c2, c3);
        configsManager.filterConfigsProperty().setValue(
                FXCollections.observableArrayList(initialConfigs));

        // When:
        configsManager.removeFilterConfig(c1);

        // Then:
        FilterConfig[] expected = {c2, c3};
        verify(configsAccess, times(1)).saveFilterConfigsToFile(anyList());
        assertThat(configsManager.getFilterConfigs(), containsInAnyOrder(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void overwriteConfig_givenConfigIsNotProvided_shouldThrowException() {
        // When:
        configsManager.overwriteFilterConfig(null);
    }

    @Test(expected = FilterConfigsAccessException.class)
    public void overwriteConfig_givenOverwritingConfigFails_shouldThrowException() {
        // When:
        FilterConfig c1 = new FilterConfig("test config 1", LocalDateTime.now(), null);
        FilterConfig c2 = new FilterConfig("test config 2", LocalDateTime.now(), null);
        FilterConfig c3 = new FilterConfig("test config 3", LocalDateTime.now(), null);
        List<FilterConfig> initialConfigs = Arrays.asList(c1, c2, c3);
        configsManager.filterConfigsProperty().setValue(
                FXCollections.observableArrayList(initialConfigs));
        willThrow(new FilterConfigsAccessException()).given(configsAccess)
                .saveFilterConfigsToFile(anyList());

        // When:
        configsManager.overwriteFilterConfig(c1);

        // Then:
        verify(configsAccess, times(1)).saveFilterConfigsToFile(anyList());
        assertThat(configsManager.getFilterConfigs(), containsInAnyOrder(initialConfigs.toArray()));
    }

    @Test
    public void overwriteConfig_givenConfigIsProvided_shouldOverwriteConfig() {
        // Given:
        FilterConfig c1
                = new FilterConfig("test config 1", LocalDateTime.of(2000, 1, 1, 1, 1), null);
        FilterConfig c2
                = new FilterConfig("test config 2", LocalDateTime.of(2000, 2, 2, 2, 2), null);
        FilterConfig c3
                = new FilterConfig("test config 3", LocalDateTime.of(2000, 3, 3, 3, 3), null);
        List<FilterConfig> initialConfigs = Arrays.asList(c1, c2, c3);
        configsManager.filterConfigsProperty().setValue(
                FXCollections.observableArrayList(initialConfigs));

        LocalDateTime beforeOverwrite = LocalDateTime.now();

        // When:
        configsManager.overwriteFilterConfig(c1);

        // Then:
        Optional<FilterConfig> createdConfig = configsManager.getFilterConfigs()
                .filtered(config -> beforeOverwrite.isBefore(config.getLastEdited())
                        || beforeOverwrite.isEqual(config.getLastEdited()))
                .stream().findFirst();
        verify(configsAccess, times(1)).saveFilterConfigsToFile(anyList());
        assertEquals(3, configsManager.getFilterConfigs().size());
        assertTrue(createdConfig.isPresent());
        assertEquals("test config 1", createdConfig.get().getName());
    }
}
