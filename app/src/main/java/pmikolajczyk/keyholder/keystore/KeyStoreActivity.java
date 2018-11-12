package pmikolajczyk.keyholder.keystore;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pmikolajczyk.keyholder.BaseActivity;
import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.crypto.CryptoServiceFactory;
import pmikolajczyk.keyholder.keystore.dialogs.AddNewKeyFragment;
import pmikolajczyk.keyholder.keystore.dialogs.EditKeyFragment;

public class KeyStoreActivity extends BaseActivity implements KeyStoreAdapter.KeyActionsListener {
    private RecyclerView recyclerView;
    private KeyStoreAdapter adapter;
    private FloatingActionButton addKeyButton;
    private FloatingActionButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_store);
        initializeWidgets();
    }

    private void initializeWidgets() {
        initializeAdapter();
        initializeRecyclerView();
        initializeFloatingButtons();
    }

    private void initializeAdapter() {
        adapter = new KeyStoreAdapter(this);
        adapter.setKeyActionsListener(this);
    }

    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initializeFloatingButtons() {
        addKeyButton = findViewById(R.id.buttonAddKey);
        refreshButton = findViewById(R.id.buttonRefresh);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(getString(R.string.label_title_key_store));
        populateKeyStore();
        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    addKeyButton.hide();
                    refreshButton.hide();
                } else {
                    addKeyButton.show();
                    refreshButton.show();
                }
            }
        });
    }

    private void populateKeyStore() {
        adapter.removeAll();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
        Map<String, ?> Aliases = sharedPreferences.getAll();
        List<String> AliasNames = new ArrayList<>(Aliases.keySet());
        Collections.sort(AliasNames, String.CASE_INSENSITIVE_ORDER);
        for (String alias : AliasNames)
            adapter.add(Key.fromString((String) Aliases.get(alias)));
    }

    public void onAddNewKey(View view) {
        new AddNewKeyFragment().show(getFragmentManager(), "AddNewKeyFragment");
    }

    public void onRefresh(View view) {
        Intent intent = new Intent(this, KeyStoreActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCopyUsername(String username) {
        copyToClipBoard("username", username);
        Toast.makeText(this, R.string.toast_copied_username, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCopyPassword(String password, int valueLength) {
        String decrypted = CryptoServiceFactory.getCryptoService().decrypt(password);
        copyToClipBoard("password", decrypted.substring(0, valueLength));
        Toast.makeText(this, R.string.toast_copied_password, Toast.LENGTH_SHORT).show();
    }

    private void copyToClipBoard(String label, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(clipData);
    }

    @Override
    public void onEditKey(Key key) {
        EditKeyFragment editKeyFragment = new EditKeyFragment();
        editKeyFragment.setOldKey(key);
        editKeyFragment.show(getFragmentManager(), "EditKeyFragment");
    }

    @Override
    public void onDeleteKey(Key key) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
        sharedPreferences.edit().remove(key.name).apply();

        adapter.remove(key.name);

        Toast.makeText(this, String.format(getString(R.string.toast_key_removed), key.name), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGo(Key key) {
        String loginPage = key.loginPage;
        if (loginPage.equals("")) return;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginPage));
        startActivity(intent);
    }
}
