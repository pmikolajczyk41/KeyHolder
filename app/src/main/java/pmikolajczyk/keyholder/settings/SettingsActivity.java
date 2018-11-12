package pmikolajczyk.keyholder.settings;

import android.os.Bundle;

import pmikolajczyk.keyholder.BaseActivity;
import pmikolajczyk.keyholder.R;

public class SettingsActivity extends BaseActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.label_title_settings));
        displayFragmentAsContent();
    }

    private void displayFragmentAsContent() {
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}