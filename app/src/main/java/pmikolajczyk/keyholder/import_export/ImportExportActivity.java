package pmikolajczyk.keyholder.import_export;

import android.os.Bundle;
import android.view.View;

import pmikolajczyk.keyholder.BaseActivity;
import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.import_export.dialogs.ExportFragment;
import pmikolajczyk.keyholder.import_export.dialogs.ImportFragment;

public class ImportExportActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(getString(R.string.label_title_import_export));
    }

    public void onExport(View view) {
        new ExportFragment().show(getFragmentManager(), "ExportFragment");
    }

    public void onImport(View view) {
        new ImportFragment().show(getFragmentManager(), "ImportFragment");
    }
}
