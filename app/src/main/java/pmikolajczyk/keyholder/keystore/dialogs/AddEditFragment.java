package pmikolajczyk.keyholder.keystore.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.keystore.Key;
import pmikolajczyk.keyholder.keystore.PersistKeyService;

public abstract class AddEditFragment extends DialogFragment {
    protected Dialog dialog;
    protected View view;
    protected EditText nameEntry;
    protected CheckBox checkLoginPage;
    protected EditText loginPageEntry;
    protected CheckBox checkUsername;
    protected EditText usernameEntry;
    protected RadioButton checkCustomPassword;
    protected TextInputLayout customPasswordInputLayout;
    protected EditText customPasswordEntry;
    protected RadioButton checkGeneratePassword;
    protected ConstraintLayout generatePasswordLayout;
    protected NumberPicker lengthPicker;
    protected RadioButton checkOldPassword;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        buildDialog(builder);
        initializeView();
        dialog = builder.create();
        setValidation();
        return dialog;
    }

    protected void setValidation() {
        dialog.setOnShowListener((dialogInterface) -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (entriesAreValid()) {
                    onPositiveAction();
                    dialog.dismiss();
                } else
                    setErrors();
            });
        });
    }

    protected boolean entriesAreValid() {
        if (isStringInvalid(nameEntry.getText().toString())) return false;
        if (checkUsername.isChecked() && isStringInvalid(usernameEntry.getText().toString()))
            return false;
        if (checkLoginPage.isChecked() && isStringInvalid(loginPageEntry.getText().toString()))
            return false;
        if (checkCustomPassword.isChecked() && isPasswordLengthInvalid(customPasswordEntry.getText().toString()))
            return false;
        return true;
    }

    private void setErrors() {
        if (isStringInvalid(nameEntry.getText().toString()))
            nameEntry.setError(getString(R.string.error_empty_text));
        if (checkUsername.isChecked() && isStringInvalid(usernameEntry.getText().toString()))
            usernameEntry.setError(getString(R.string.error_empty_text));
        if (checkLoginPage.isChecked() && isStringInvalid(loginPageEntry.getText().toString()))
            loginPageEntry.setError(getString(R.string.error_empty_text));
        if (checkCustomPassword.isChecked() && isPasswordLengthInvalid(customPasswordEntry.getText().toString()))
            customPasswordEntry.setError(getString(R.string.error_password_length));
    }

    protected boolean isStringInvalid(String string) {
        return string == null || string.equals("");
    }

    protected boolean isPasswordLengthInvalid(String password) {
        int lowerbound = getResources().getInteger(R.integer.key_length_min);
        int upperbound = getResources().getInteger(R.integer.key_length_max);
        return lowerbound > password.length() || password.length() > upperbound;
    }

    protected abstract void buildDialog(AlertDialog.Builder builder);

    protected View inflateView() {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        view = layoutInflater.inflate(R.layout.fragment_add_edit_key, null);
        return view;
    }

    protected void initializeView() {
        initializeWidgets();
        setActions();
        setNumberPicker();
    }

    protected void initializeWidgets() {
        nameEntry = view.findViewById(R.id.nameEntry);
        checkLoginPage = view.findViewById(R.id.checkLoginPage);
        loginPageEntry = view.findViewById(R.id.loginPageEntry);
        checkUsername = view.findViewById(R.id.checkUsername);
        usernameEntry = view.findViewById(R.id.usernameEntry);
        checkCustomPassword = view.findViewById(R.id.checkCustomPassword);
        customPasswordInputLayout = view.findViewById(R.id.customPasswordInputLayout);
        customPasswordEntry = view.findViewById(R.id.customPasswordEntry);
        checkGeneratePassword = view.findViewById(R.id.checkGeneratePassword);
        generatePasswordLayout = view.findViewById(R.id.generatePasswordLayout);
        lengthPicker = view.findViewById(R.id.lenghtPicker);
        checkOldPassword = view.findViewById(R.id.checkOldPassword);
    }

    protected void setActions() {
        checkLoginPage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loginPageEntry.setEnabled(isChecked);
        });
        checkUsername.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            usernameEntry.setEnabled(isChecked);
        }));
        checkCustomPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) customPasswordInputLayout.setVisibility(View.VISIBLE);
            else customPasswordInputLayout.setVisibility(View.GONE);
        });
        checkGeneratePassword.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) generatePasswordLayout.setVisibility(View.VISIBLE);
            else generatePasswordLayout.setVisibility(View.GONE);
        }));
    }

    protected void setNumberPicker() {
        lengthPicker.setMinValue(getActivity().getResources().getInteger(R.integer.key_length_min));
        lengthPicker.setMaxValue(getActivity().getResources().getInteger(R.integer.key_length_max));
        lengthPicker.setValue(getActivity().getResources().getInteger(R.integer.key_length_default));
    }

    protected void onNegativeAction() {
        dismiss();
    }

    protected void onPositiveAction() {
        Intent intent = new Intent(getActivity(), PersistKeyService.class);
        fillIntent(intent);
        getActivity().startService(intent);
    }

    protected void fillIntent(Intent intent) {
        setIntentType(intent);

        String name = nameEntry.getText().toString();

        String value = "";
        int valueLength = 0;

        if (checkCustomPassword.isChecked())
            value = customPasswordEntry.getText().toString();
        if (checkGeneratePassword.isChecked())
            valueLength = lengthPicker.getValue();

        String username = usernameEntry.getText().toString();
        String loginPage = loginPageEntry.getText().toString();

        intent.putExtra(getString(R.string.intent_add_edit_extra_if_generate_password), checkGeneratePassword.isChecked());
        intent.putExtra(getString(R.string.intent_add_edit_extra_if_custom_password), checkCustomPassword.isChecked());
        intent.putExtra(getString(R.string.intent_add_edit_extra_if_old_password), checkOldPassword.isChecked());
        intent.putExtra(getString(R.string.intent_add_edit_extra_if_user_name), checkUsername.isChecked());
        intent.putExtra(getString(R.string.intent_add_edit_extra_if_login_page), checkLoginPage.isChecked());
        intent.putExtra(getString(R.string.intent_add_edit_extra_key), (Parcelable) new Key(name, value, valueLength, username, loginPage));
    }

    protected void setIntentType(Intent intent) {
        intent.putExtra(getString(R.string.intent_add_edit_query_type), getQueryType());
    }

    protected abstract String getQueryType();
}
