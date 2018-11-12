package pmikolajczyk.keyholder.import_export.dialogs;

import android.content.Intent;
import android.widget.RadioButton;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.import_export.ImportKeysService;

public class ImportFragment extends ImportExportFragment{
    private RadioButton checkAddKeys;
    private RadioButton checkReplaceKeys;

    @Override
    protected void initializeView() {
        super.initializeView();
        checkAddKeys = view.findViewById(R.id.checkAddKeys);
        checkReplaceKeys = view.findViewById(R.id.checkReplaceKeys);
    }

    @Override
    protected int getTitle(){
        return R.string.title_import;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.button_positive_import;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_import;
    }

    @Override
    protected void onPositiveAction() {
        Intent intent = new Intent(getActivity(), ImportKeysService.class);
        fillIntent(intent);
        getActivity().startService(intent);
    }

    private void fillIntent(Intent intent) {
        intent.putExtra(getString(R.string.intent_import_extra_bundle_name), bundleNameEntry.getText().toString());
        intent.putExtra(getString(R.string.intent_import_extra_encryption_seed), masterKeyEntry.getText().toString());
        intent.putExtra(getString(R.string.intent_import_extra_if_add_keys), checkAddKeys.isChecked());
        intent.putExtra(getString(R.string.intent_import_extra_if_replace_keys), checkReplaceKeys.isChecked());
    }
}
