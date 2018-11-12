package pmikolajczyk.keyholder.keystore.dialogs;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.keystore.Key;

public class EditKeyFragment extends AddEditFragment {
    private Key oldKey = new Key();

    public void setOldKey(Key oldKey) {
        this.oldKey = oldKey;
    }

    @Override
    protected void buildDialog(AlertDialog.Builder builder) {
        builder.setTitle(R.string.title_edit_key)
                .setView(inflateView())
                .setPositiveButton(R.string.button_positive_edit_key, (dialog, which) -> {
                })
                .setNegativeButton(R.string.button_negative_cancel, (dialog, which) -> {
                    onNegativeAction();
                })
                .setCancelable(true);
    }

    @Override
    protected void initializeWidgets() {
        super.initializeWidgets();
        setOldValues();
        hideWidgets();
    }

    private void setOldValues() {
        setOldEntries();
        setChecks();
    }

    private void setOldEntries() {
        nameEntry.setText(oldKey.name);
        usernameEntry.setText(oldKey.username);
        loginPageEntry.setText(oldKey.loginPage);
    }

    private void setChecks() {
        checkUsername.setChecked(!isStringInvalid(usernameEntry.getText().toString()));
        checkLoginPage.setChecked(!isStringInvalid(loginPageEntry.getText().toString()));
        checkOldPassword.setChecked(true);
    }

    private void hideWidgets() {
        customPasswordInputLayout.setVisibility(View.GONE);
        generatePasswordLayout.setVisibility(View.GONE);
    }

    @Override
    protected void fillIntent(Intent intent) {
        super.fillIntent(intent);
        intent.putExtra(getString(R.string.intent_add_edit_extra_old_key), (Parcelable) oldKey);
    }

    @Override
    protected String getQueryType() {
        return getString(R.string.intent_add_edit_query_type_edit_key);
    }
}
