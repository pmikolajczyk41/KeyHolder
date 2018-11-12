package pmikolajczyk.keyholder.keystore.dialogs;

import android.app.AlertDialog;
import android.view.View;

import pmikolajczyk.keyholder.R;

public class AddNewKeyFragment extends AddEditFragment {
    @Override
    protected void initializeView() {
        super.initializeView();
        checkOldPassword.setVisibility(View.GONE);
    }

    protected void buildDialog(AlertDialog.Builder builder) {
        builder.setTitle(R.string.title_add_key)
                .setView(inflateView())
                .setPositiveButton(R.string.button_positive_add_new_key, ((dialog, which) -> {
                }))
                .setNegativeButton(R.string.button_negative_cancel, ((dialog, which) -> onNegativeAction()))
                .setCancelable(true);
    }

    @Override
    protected String getQueryType() {
        return getString(R.string.intent_add_edit_query_type_add_key);
    }
}
