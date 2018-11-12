package pmikolajczyk.keyholder.import_export.dialogs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.import_export.ExportKeysService;

import static android.content.Context.MODE_PRIVATE;

public class ExportFragment extends ImportExportFragment {
    private ListView listView;

    @Override
    protected int getTitle() {
        return R.string.title_export;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.button_positive_export;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        listView = view.findViewById(R.id.exportKeyChoice);
        populateKeyChoice();
    }

    private void populateKeyChoice() {
        List<String> Aliases = getAllKeys();
        ArrayAdapter<String> arrayAdapter = getStringArrayAdapter(Aliases);
        listView.setAdapter(arrayAdapter);
        selectAll();
    }

    private List<String> getAllKeys() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
        Map<String, ?> Aliases = sharedPreferences.getAll();
        List<String> AliasNames = new ArrayList<>(Aliases.keySet());
        Collections.sort(AliasNames, String.CASE_INSENSITIVE_ORDER);
        return AliasNames;
    }

    private void selectAll() {
        for (int position = 0; position < listView.getAdapter().getCount(); position++)
            listView.setItemChecked(position, true);
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter(List<String> aliases) {
        return new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, aliases);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_export;
    }

    @Override
    protected void onPositiveAction() {
        Intent intent = new Intent(getActivity(), ExportKeysService.class);
        fillIntent(intent);
        getActivity().startService(intent);
    }

    private void fillIntent(Intent intent) {
        intent.putExtra(getString(R.string.intent_export_extra_bundle_name), bundleNameEntry.getText().toString());
        intent.putExtra(getString(R.string.intent_export_extra_encryption_seed), masterKeyEntry.getText().toString());
        intent.putExtra(getString(R.string.intent_export_extra_keys), getChosenKeys());
    }

    private ArrayList<String> getChosenKeys() {
        ArrayList<String> ChosenKeys = new ArrayList<>();
        for (int position = 0; position < listView.getAdapter().getCount(); position++)
            if (listView.isItemChecked(position))
                ChosenKeys.add(getKeyDescription((String) listView.getAdapter().getItem(position)));
        return ChosenKeys;
    }

    private String getKeyDescription(String name) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
        return sharedPreferences.getString(name, "");
    }
}
