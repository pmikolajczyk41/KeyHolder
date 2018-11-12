package pmikolajczyk.keyholder.import_export;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.crypto.CryptoException;
import pmikolajczyk.keyholder.crypto.CryptoService;
import pmikolajczyk.keyholder.crypto.CryptoServiceFactory;
import pmikolajczyk.keyholder.keystore.Key;

public class ImportKeysService extends IntentService {
    private Handler handler = new Handler();
    private String importedData;

    public ImportKeysService() {
        super("ImportKeysService");
    }

    private String bundleName;
    private String encryptionSeed;
    private boolean ifAddKeys;
    private boolean ifReplaceKeys;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            retrieveData(intent);
            importData();
        }
    }

    private void retrieveData(Intent intent) {
        bundleName = intent.getStringExtra(getString(R.string.intent_import_extra_bundle_name));
        encryptionSeed = intent.getStringExtra(getString(R.string.intent_import_extra_encryption_seed));
        ifAddKeys = intent.getBooleanExtra(getString(R.string.intent_import_extra_if_add_keys), true);
        ifReplaceKeys = intent.getBooleanExtra(getString(R.string.intent_import_extra_if_replace_keys), false);
    }

    private void importData() {
        if (!isExternalStorageReadable())
            handler.post(() ->
                    Toast.makeText(ImportKeysService.this, getString(R.string.toast_no_external_storage), Toast.LENGTH_SHORT).show());
        else {
            File appFolder = getAppFolder();
            File bundle = getBundle(appFolder);
            readBundle(bundle);
        }
    }

    private File getAppFolder() {
        File appFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), getString(R.string.app_name));
        return appFolder;
    }

    private File getBundle(File appFolder) {
        return new File(appFolder, bundleName);
    }

    private void readBundle(File bundle) {
        if (bundle == null || !bundle.exists()) {
            handler.post(() ->
                    Toast.makeText(ImportKeysService.this, getString(R.string.toast_no_file), Toast.LENGTH_SHORT).show());
            return;
        }
        importedData = retrieveBundleData(bundle);
        if (dataIsInvalid(importedData)) {
            handler.post(() ->
                    Toast.makeText(ImportKeysService.this, getString(R.string.toast_wrong_seed), Toast.LENGTH_SHORT).show());
            return;
        }
        updateKeyStore();
    }

    private String retrieveBundleData(File bundle) {
        int length = (int) bundle.length();
        byte[] bytes = new byte[length];

        try (FileInputStream in = getFileInputStream(bundle)) {
            if (in != null) in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(bytes);
    }

    @Nullable
    private FileInputStream getFileInputStream(File bundle) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(bundle);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return in;
    }

    private boolean dataIsInvalid(String data) {
        CryptoService cryptoService = CryptoServiceFactory.getCryptoService();
        String control_word = data.substring(0, data.indexOf("&&&"));
        String decrypted = cryptoService.decrypt(control_word, encryptionSeed);
        return !getString(R.string.import_export_control_word).equals(decrypted);
    }

    private void updateKeyStore() {
        if (ifReplaceKeys) clearKeyStore();
        CryptoService cryptoService = CryptoServiceFactory.getCryptoService();
        String[] NewKeys = importedData.split("&&&");
        try {
            for (int position = 1; position < NewKeys.length; position++)
                addNewKey(cryptoService.decrypt(NewKeys[position], encryptionSeed));
        } catch (CryptoException e) {
            handler.post(() -> Toast.makeText(ImportKeysService.this,
                    getString(R.string.toast_failure), Toast.LENGTH_LONG).show());
        }
    }

    private void clearKeyStore() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    private void addNewKey(String keyDescription) {
        Key newKey = convertKeyDescription(keyDescription);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_keys), MODE_PRIVATE);
        sharedPreferences.edit().putString(newKey.name, newKey.toString()).apply();
    }

    private Key convertKeyDescription(String keyDescription) {
        Key key = Key.fromString(keyDescription);
        key.value = CryptoServiceFactory.getCryptoService().encrypt(key.value);
        return key;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

//
//    private void importData() {
//        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
//        appFolderTask.addOnSuccessListener(driveFolder -> {
//            Query query = createQuery(driveFolder);
//            Task<MetadataBuffer> queryTask = getDriveResourceClient().queryChildren(driveFolder, query);
//            queryTask.addOnSuccessListener(MAIN_THREAD,
//                    metadataBuffer -> {
//                        Log.e(TAG, "importData: SUCCESS");
//                        handleFileMetadata(metadataBuffer.get(0));
//                        metadataBuffer.release();
//                    })
//                    .addOnFailureListener(MAIN_THREAD, e -> Log.e(TAG, "importData: FAILURE"));
//        });
//    }
//
//    private void handleFileMetadata(Metadata metadata) {
//        Task<DriveContents> openTask =
//                getDriveResourceClient().openFile(metadata.getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);
//        openTask.addOnSuccessListener(task -> {
//            readFile(task.getInputStream());
//            closeFile(task);
//        });
//    }
//
//    private void readFile(InputStream inputStream) {
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(inputStream))) {
//            StringBuilder builder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                builder.append(line).append("\n");
//            }
//            Log.e(TAG, "readFile: " + builder.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closeFile(DriveContents driveContents) {
//        getDriveResourceClient().discardContents(driveContents);
//    }
//
//    private Query createQuery(DriveFolder driveFolder) {
//        return new Query.Builder()
//                .addFilter(Filters.and(Filters.eq(SearchableField.TITLE, bundleName),
//                        Filters.in(SearchableField.PARENTS, driveFolder.getDriveId())))
//                .build();
//    }
//
//    private DriveResourceClient getDriveResourceClient() {
//        return Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
//    }
}
