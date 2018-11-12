package pmikolajczyk.keyholder.locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.Locale;

import pmikolajczyk.keyholder.R;

public class LocaleResolver {
    public static Context updateLocale(Context context) {
        Locale locale = new Locale(getCurrentLanguage(context));
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    private static String getCurrentLanguage(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(getString(context, R.string.preference_language), getString(context, R.string.preference_language_default));
    }

    @NonNull
    private static String getString(Context context, int key) {
        return context.getResources().getString(key);
    }
}
