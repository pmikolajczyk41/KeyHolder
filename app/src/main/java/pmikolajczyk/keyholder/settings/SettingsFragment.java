package pmikolajczyk.keyholder.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.locale.LocaleResolver;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ListPreference languagePreference;
    private SwitchPreference multipleCredentialsPreference;
    private CheckBoxPreference clearAllPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        initializePreferences();
        setSummaries();
    }

    private void initializePreferences() {
        languagePreference = (ListPreference) findPreference(getString(R.string.preference_language));
        multipleCredentialsPreference = (SwitchPreference) findPreference(getString(R.string.preference_multiple_credentials));
        clearAllPreference = (CheckBoxPreference) findPreference(getString(R.string.preference_clear_all));
    }

    private void setSummaries() {
        setMultipleCredentialsSummary();
        setLanguageSummary();
    }

    private void setMultipleCredentialsSummary() {
        multipleCredentialsPreference.setSummary(getString(R.string.label_multiple_credentials_description));
    }

    private void setLanguageSummary() {
        languagePreference.setSummary(getCurrentLanguage());
    }

    @NonNull
    private String getCurrentLanguage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences.getString(getString(R.string.preference_language), getString(R.string.preference_language_default));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preference_language)))
            onLanguageChanged();
        if (key.equals(getString(R.string.preference_clear_all)))
            onClearAll(sharedPreferences, key);
    }

    private void onLanguageChanged() {
        setLanguageSummary();
        LocaleResolver.updateLocale(getActivity());
        refreshActivity();
    }

    private void refreshActivity() {
        Intent refresh = new Intent(getActivity(), SettingsActivity.class);
        startActivity(refresh);
        getActivity().finish();
    }

    private void onClearAll(SharedPreferences sharedPreferences, String key) {
        clearAllPreference.setChecked(false);
        sharedPreferences.edit().putBoolean(key, false).apply();
        SharedPreferences keyPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
        keyPreferences.edit().clear().apply();
        getActivity().finish();
    }
}
