package pmikolajczyk.keyholder.import_export;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.crypto.CryptoService;
import pmikolajczyk.keyholder.crypto.CryptoServiceFactory;
import pmikolajczyk.keyholder.keystore.Key;

public class ExportKeysService extends IntentService {
    private Handler handler = new Handler();

    public ExportKeysService() {
        super("ExportKeysService");
    }

    private String bundleName;
    private String encryptionSeed;
    private List<String> Keys;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            retrieveData(intent);
            exportData();
        }
    }

    private void retrieveData(Intent intent) {
        bundleName = intent.getStringExtra(getString(R.string.intent_export_extra_bundle_name));
        encryptionSeed = intent.getStringExtra(getString(R.string.intent_export_extra_encryption_seed));
        Keys = intent.getStringArrayListExtra(getString(R.string.intent_export_extra_keys));
    }

    private void exportData() {
        if (!isExternalStorageWritable())
            handler.post(() ->
                    Toast.makeText(ExportKeysService.this, getString(R.string.toast_no_external_storage), Toast.LENGTH_SHORT).show());
        else {
            File appFolder = createAppFolder();
            File newBundle = createNewBundle(appFolder);
            writeBundle(newBundle);
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File createAppFolder() {
        File appFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), getString(R.string.app_name));
        appFolder.mkdirs();
        hideFromMedia(appFolder);
        return appFolder;
    }

    private void hideFromMedia(File appFolder) {
        new File(appFolder, ".nomedia");
    }

    private File createNewBundle(File appFolder) {
        return new File(appFolder, bundleName);
    }

    private void writeBundle(File newBundle) {
        try (FileOutputStream stream = getFileOutputStream(newBundle)) {
            if (stream != null) stream.write(createBundle().getBytes());
            handler.post(() ->
                    Toast.makeText(ExportKeysService.this, getString(R.string.toast_saved)
                            + newBundle.getAbsolutePath(), Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private FileOutputStream getFileOutputStream(File newBundle) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(newBundle);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stream;
    }

    private String createBundle() {
        String separator = "&&&";
        CryptoService cryptoService = CryptoServiceFactory.getCryptoService();
        StringBuilder result = new StringBuilder(cryptoService.encrypt(getString(R.string.import_export_control_word), encryptionSeed));
        result.append(separator);
        for (String key : Keys) {
            String newDescription = convertKeyDescription(cryptoService, key);
            result.append(cryptoService.encrypt(newDescription, encryptionSeed)).append(separator);
        }
        return result.subSequence(0, result.length() - 1).toString();
    }

    private String convertKeyDescription(CryptoService cryptoService, String keyDescription) {
        Key oldKey = Key.fromString(keyDescription);
        oldKey.value = cryptoService.decrypt(oldKey.value).trim();
        return oldKey.toString();
    }


//    private void exportData() {
//        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
//        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
//        Tasks.whenAll(appFolderTask, createContentsTask)
//                .continueWithTask(task -> {
//                    DriveContents contents = createDriveContents(createContentsTask);
//                    MetadataChangeSet changeSet = createMetadataChangeSet();
//                    DriveFolder parent = appFolderTask.getResult();
//                    return getDriveResourceClient().createFile(parent, changeSet, contents);
//                })
//                .addOnSuccessListener(f -> Toast.makeText(getApplicationContext(), getString(R.string.toast_export_success), Toast.LENGTH_LONG).show())
//                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), getString(R.string.toast_failure), Toast.LENGTH_LONG).show());
//    }
//
//    @NonNull
//    private DriveContents createDriveContents(Task<DriveContents> createContentsTask) throws IOException {
//        DriveContents contents = createContentsTask.getResult();
//        OutputStream outputStream = contents.getOutputStream();
//        try (Writer writer = new OutputStreamWriter(outputStream)) {
//            writer.write(createBundle());
//        }
//        return contents;
//    }
//
//    private MetadataChangeSet createMetadataChangeSet() {
//        return new MetadataChangeSet.Builder()
//                .setTitle(bundleName)
//                .setMimeType("text/plain")
//                .setStarred(true)
//                .build();
//    }
//
//    private DriveResourceClient getDriveResourceClient() {
//        return Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
//    }
}
