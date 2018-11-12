package pmikolajczyk.keyholder.import_export.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import pmikolajczyk.keyholder.R;

public abstract class ImportExportFragment extends DialogFragment {
    protected Dialog dialog;
    protected View view;
    protected EditText bundleNameEntry;
    protected EditText masterKeyEntry;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        buildDialog(builder);
        initializeView();
        dialog = builder.create();
        setValidation();
        return dialog;
    }

    protected void buildDialog(AlertDialog.Builder builder) {
        builder.setTitle(getTitle())
                .setView(inflateView())
                .setPositiveButton(getPositiveButtonTitle(), ((dialogInterface, which) -> {
                }))
                .setNegativeButton(R.string.button_negative_cancel, ((dialogInterface, which) -> {
                    onNegativeButtonClick();
                }))
                .setCancelable(true);
    }

    protected abstract int getTitle();

    protected abstract int getPositiveButtonTitle();

    protected void initializeView() {
        bundleNameEntry = view.findViewById(R.id.bundleNameEntry);
        masterKeyEntry = view.findViewById(R.id.keyEntry);
    }

    private void setValidation() {
        dialog.setOnShowListener((dialogInterface -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (entriesAreValid()) {
                    onPositiveAction();
                    dialog.dismiss();
                } else
                    setErrors();
            });
        }));
    }

    protected abstract void onPositiveAction();

    protected boolean entriesAreValid() {
        if (isStringInvalid(bundleNameEntry.getText().toString())) return false;
        if (isStringInvalid(masterKeyEntry.getText().toString())) return false;
        return true;
    }

    protected void setErrors() {
        if (isStringInvalid(bundleNameEntry.getText().toString()))
            bundleNameEntry.setError(getString(R.string.error_empty_text));
        if (isStringInvalid(masterKeyEntry.getText().toString()))
            masterKeyEntry.setError(getString(R.string.error_empty_text));
    }

    protected boolean isStringInvalid(String string) {
        return string == null || string.equals("");
    }

    protected void onNegativeButtonClick() {
        dismiss();
    }

    protected View inflateView() {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        view = layoutInflater.inflate(getLayout(), null);
        return view;
    }

    protected abstract int getLayout();
}

