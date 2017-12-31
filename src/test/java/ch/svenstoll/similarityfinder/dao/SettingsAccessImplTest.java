package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.Settings;
import org.junit.Before;
import org.junit.Test;

import java.util.prefs.Preferences;

import static ch.svenstoll.similarityfinder.dao.SettingsAccessImpl.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

public class SettingsAccessImplTest {
    private SettingsAccessImpl settingsAccess;
    private Preferences preferences;

    @Before
    public void setUp() {
        preferences = mock(Preferences.class);
        settingsAccess = new SettingsAccessImpl(preferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void storeSettings_givenSettingsAreNotProvided_shouldThrowException() {
        // When:
        settingsAccess.storeSettings(null);
    }

    @Test
    public void storeSettings_givenSettingsAreProvided_shouldStoreSettings() {
        // Given:
        String dbServerAddress = "dbServerAddress";
        String dbUser = "user";
        String dbPassword = "password";
        int maxContributions = 1000;
        boolean firstLaunch = false;

        Settings settings = mock(Settings.class);
        given(settings.getDbAddress()).willReturn(dbServerAddress);
        given(settings.getDbUser()).willReturn(dbUser);
        given(settings.getDbPassword()).willReturn(dbPassword);
        given(settings.getMaxArticles()).willReturn(maxContributions);
        given(settings.isFirstLaunch()).willReturn(firstLaunch);

        // When:
        settingsAccess.storeSettings(settings);

        // Then:
        verify(preferences, times(1)).put(anyString(), eq(dbServerAddress));
        verify(preferences, times(1)).put(anyString(), eq(dbUser));
        verify(preferences, times(1)).put(anyString(), eq(dbPassword));
        verify(preferences, times(1)).putInt(anyString(), eq(maxContributions));
        verify(preferences, times(1)).putBoolean(anyString(), eq(firstLaunch));
    }

    @Test(expected = SettingsAccessException.class)
    public void storeSettings_givenStoringFails_shouldThrowException() {
        // Given:
        willThrow(IllegalStateException.class).given(preferences).put(anyString(), any());
        willThrow(IllegalStateException.class).given(preferences).putInt(anyString(), anyInt());
        willThrow(IllegalStateException.class).given(preferences)
                .putBoolean(anyString(), anyBoolean());

        Settings settings = mock(Settings.class);

        // When:
        settingsAccess.storeSettings(settings);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveSettings_givenSettingsAreNotProvided_shouldThrowException() {
        // When:
        settingsAccess.retrieveSettings(null);
    }

    @Test
    public void retrieveSettings_givenSettingsAreProvided_shouldRetrieveSettings() {
        // Given:
        String dbServerAddress = "dbServerAddress";
        String dbUser = "user";
        String dbPassword = "password";
        int maxContributions = 1000;
        boolean firstLaunch = false;

        given(preferences.get(eq(DB_ADDRESS_KEY), anyString())).willReturn(dbServerAddress);
        given(preferences.get(eq(DB_USER_KEY), anyString())).willReturn(dbUser);
        given(preferences.get(eq(DB_PASSWORD_KEY), anyString())).willReturn(dbPassword);
        given(preferences.getInt(eq(MAX_CONTRIBUTIONS_KEY), anyInt())).willReturn(maxContributions);
        given(preferences.getBoolean(eq(FIRST_LAUNCH_KEY), anyBoolean())).willReturn(firstLaunch);

        Settings settings = mock(Settings.class);

        // When:
        settingsAccess.retrieveSettings(settings);

        // Then:
        verify(settings, times(1)).setDbAddress(eq(dbServerAddress));
        verify(settings, times(1)).setDbUser(eq(dbUser));
        verify(settings, times(1)).setDbPassword(eq(dbPassword));
        verify(settings, times(1)).setMaxArticles(eq(maxContributions));
        verify(settings, times(1)).setFirstLaunch(eq(firstLaunch));
    }

    @Test(expected = SettingsAccessException.class)
    public void retrieveSettings_givenRetrievingFails_shouldThrowException() {
        // Given:
        willThrow(IllegalStateException.class).given(preferences).get(anyString(), any());
        willThrow(IllegalStateException.class).given(preferences).getInt(anyString(), anyInt());
        willThrow(IllegalStateException.class)
                .given(preferences).getBoolean(anyString(), anyBoolean());

        Settings settings = mock(Settings.class);

        // When:
        settingsAccess.retrieveSettings(settings);
    }
}
