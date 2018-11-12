package pmikolajczyk.keyholder.main_menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import pmikolajczyk.keyholder.BaseActivity;
import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.import_export.ImportExportActivity;
import pmikolajczyk.keyholder.keystore.KeyStoreActivity;
import pmikolajczyk.keyholder.settings.SettingsActivity;

public class MainMenuActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void openKeyStore(View view) {
        openNewActivity(KeyStoreActivity.class);
    }

    public void openImportExport(View view) {
        openNewActivity(ImportExportActivity.class);
    }

    public void openSettings(View view) {
        openNewActivity(SettingsActivity.class);
    }

    private void openNewActivity(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    public void onLogout(View view) {
        System.exit(0);
    }
}
