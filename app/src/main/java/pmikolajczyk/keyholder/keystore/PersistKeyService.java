package pmikolajczyk.keyholder.keystore;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.security.SecureRandom;
import java.util.regex.Pattern;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.crypto.CryptoService;
import pmikolajczyk.keyholder.crypto.CryptoServiceFactory;

public class PersistKeyService extends IntentService {
    private static CryptoService cryptoService = CryptoServiceFactory.getCryptoService();

    private boolean ifGeneratePassword;
    private boolean ifCustomPassword;
    private boolean ifOldPassword;
    private boolean ifIncludeUsername;
    private boolean ifIncludeLoginPage;
    private Key newKey;
    private Key oldKey;

    private String KeyToDelete = null;
    private boolean editMode;
    private SharedPreferences sharedPreferences;
    private String queryType;

    public PersistKeyService() {
        super("PersistKeyService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        retrieveType(intent);
        if (queryType.equals(getString(R.string.intent_add_edit_query_type_add_key))
                || queryType.equals(getString(R.string.intent_add_edit_query_type_edit_key)))
            handleAddEditKey(intent);
    }

    private void retrieveType(Intent intent) {
        queryType = intent.getStringExtra(getString(R.string.intent_add_edit_query_type));
    }

    private void handleAddEditKey(Intent intent) {
        retrieveIntentData(intent);
        if (dataIsInvalid()) return;
        handleData();
        getPreferences();
        deleteOldKey();
        persistNewKey();
    }

    private void retrieveIntentData(@NonNull Intent intent) {
        ifIncludeUsername = intent.getBooleanExtra(getString(R.string.intent_add_edit_extra_if_user_name), false);
        ifIncludeLoginPage = intent.getBooleanExtra(getString(R.string.intent_add_edit_extra_if_login_page), false);
        ifGeneratePassword = intent.getBooleanExtra(getString(R.string.intent_add_edit_extra_if_generate_password), false);
        ifCustomPassword = intent.getBooleanExtra(getString(R.string.intent_add_edit_extra_if_custom_password), false);
        ifOldPassword = intent.getBooleanExtra(getString(R.string.intent_add_edit_extra_if_old_password), false);
        newKey = intent.getParcelableExtra(getString(R.string.intent_add_edit_extra_key));
        retrieveModeInfo(intent);
    }

    private void retrieveModeInfo(@NonNull Intent intent) {
        editMode = queryType.equals(getString(R.string.intent_add_edit_query_type_edit_key));
        if (editMode)
            oldKey = intent.getParcelableExtra(getString(R.string.intent_add_edit_extra_old_key));
    }

    private boolean dataIsInvalid() {
        if (newKey.name == null || newKey.name.equals("")) return true;

        int lengthLowerbound = getResources().getInteger(R.integer.key_length_min);
        int lengthUpperbound = getResources().getInteger(R.integer.key_length_max);
        int gotLength = (ifGeneratePassword) ? newKey.valueLength : newKey.value.length();

        if ((gotLength < lengthLowerbound || gotLength > lengthUpperbound)) return true;
        return false;
    }

    private void handleData() {
        trimFields();
        handleName();
        handleUsername();
        handleLoginPage();
        handleValue();
        handleValueLength();
    }

    private void handleName() {
        if (!editMode) return;
        String oldName = oldKey.name;
        String newName = newKey.name;
        if (!oldName.equals(newName)) KeyToDelete = oldName;
    }

    private void handleValue() {
        if (ifGeneratePassword) newKey.value = generatePassword(newKey.valueLength);
        if (ifOldPassword) newKey.value = cryptoService.decrypt(oldKey.value);
    }

    private void handleValueLength() {
        if (ifCustomPassword || ifOldPassword) newKey.valueLength = newKey.value.length();
    }

    private void handleUsername() {
        if (!ifIncludeUsername)
            newKey.username = "";
    }

    private void handleLoginPage() {
        if (!ifIncludeLoginPage)
            newKey.loginPage = "";
    }

    private void trimFields() {
        newKey.name = newKey.name.trim();
        newKey.value = newKey.value.trim();
        newKey.username = newKey.username.trim();
        newKey.loginPage = newKey.loginPage.trim();
    }

    private String generatePassword(int length) {
        byte[] password = new byte[4 * newKey.valueLength];
        String encoded;

        while (true) {
            new SecureRandom().nextBytes(password);
            encoded = Base64.encodeToString(password, Base64.NO_WRAP);
            if (isStrong(encoded, length)) break;
        }
        return encoded;
    }

    private boolean isStrong(String encoded, int length) {
        Pattern strongPassword = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
        Pattern alphaNumerical = Pattern.compile("^[a-zA-Z0-9]*$");
        return strongPassword.matcher(encoded.substring(0, length)).matches() &&
                alphaNumerical.matcher(encoded.substring(0, length)).matches();
    }

    private void getPreferences() {
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
    }

    private void deleteOldKey() {
        if (KeyToDelete == null) return;
        sharedPreferences.edit()
                .remove(KeyToDelete)
                .apply();
    }

    private void persistNewKey() {
        newKey.value = cryptoService.encrypt(newKey.value);
        sharedPreferences.edit()
                .putString(newKey.name, newKey.toString())
                .apply();
    }
}
