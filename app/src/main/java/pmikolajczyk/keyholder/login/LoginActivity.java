package pmikolajczyk.keyholder.login;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.Task;

import pmikolajczyk.keyholder.BaseActivity;
import pmikolajczyk.keyholder.R;
import pmikolajczyk.keyholder.main_menu.MainMenuActivity;

public class LoginActivity extends BaseActivity {
    private static final int RC_RESOLVE = 1;
    private static final int RC_SAVE = 2;
    private static final int RC_SIGN_IN = 3;

    private SignInButton signInButton;

    private CredentialRequest credentialRequest;
    private CredentialsClient credentialsClient;

    public static GoogleSignInAccount signInAccount;
    private boolean deviceSecure = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_login);
        initializeWidgets();
    }

    private void initializeWidgets() {
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener((View view) -> createNewAccount());
        setUpEnableMultipleCredential();
    }

    private void setUpEnableMultipleCredential() {
        String keyName = getString(R.string.preference_multiple_credentials);
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(keyName, false))
            signInButton.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestLockedScreen();
        if(deviceSecure)
            setUpSmartLock();
    }

    private void requestLockedScreen() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null && !keyguardManager.isDeviceSecure())
            buildAlertDialog().show();
        else deviceSecure = true;
    }

    private AlertDialog.Builder buildAlertDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(R.string.title_alert_lock_screen)
                .setMessage(R.string.message_alert_lock_screen)
                .setPositiveButton(R.string.button_positive_alert_lock_screen, (dialog, which) -> {
                    startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
                    //requestLockedScreen();
                })
                .setNegativeButton(R.string.button_negative_alert_lock_screen, (dialog, which) -> System.exit(0))
                .setOnCancelListener(dialog -> System.exit(0));
    }

    private void setUpSmartLock() {
        setUpCredentialRequest();
        setUpCredentialsClient();
        requestCredentials();
    }

    private void setUpCredentialRequest() {
        credentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build();
    }

    private void setUpCredentialsClient() {
        credentialsClient = Credentials.getClient(this);
        setUpAutoLogin();
    }

    private void setUpAutoLogin() {
        String keyName = getString(R.string.preference_auto_login);
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(keyName, false))
            credentialsClient.disableAutoSignIn();
    }

    private void requestCredentials() {
        credentialsClient.request(credentialRequest).addOnCompleteListener((task) -> {
            if (task.isSuccessful())
                onCredentialRetrieved(task.getResult().getCredential());
            else onCredentialRequestFailure(task);
        });
    }

    private void onCredentialRetrieved(Credential credential) {
        String accountType = credential.getAccountType();
        if (accountType != null && accountType.equals(IdentityProviders.GOOGLE)) signIn();
    }

    private void signIn() {
        GoogleSignInClient signInClient = createGoogleSingInClient();
        Task<GoogleSignInAccount> task = signInClient.silentSignIn();
        handleSilentSignIn(task);
    }

    @NonNull
    private GoogleSignInOptions createGoogleSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
    }

    @NonNull
    private GoogleSignInClient createGoogleSingInClient() {
        GoogleSignInOptions gso = createGoogleSignInOptions();
        return GoogleSignIn.getClient(this, gso);
    }

    private void handleSilentSignIn(Task<GoogleSignInAccount> task) {
        if (task.isSuccessful()) {
            handleSignedInAccount(task.getResult());
        } else {
            task.addOnCompleteListener(completedTask -> {
                try {
                    handleSignedInAccount(completedTask.getResult(ApiException.class));
                } catch (ApiException ignored) {
                }
            });
        }
    }

    private void handleSignedInAccount(GoogleSignInAccount googleSignInAccount) {
        signInAccount = googleSignInAccount;
        saveGoogleAccount(signInAccount);
        onPositiveLogin();
    }

    private void saveGoogleAccount(GoogleSignInAccount signInAccount) {
        credentialsClient.save(createCredential(signInAccount))
                .addOnCompleteListener(completedTask -> {
                    if (!completedTask.isSuccessful()) {
                        Exception e = completedTask.getException();
                        if (e instanceof ResolvableApiException) {
                            try {
                                ((ResolvableApiException) e).startResolutionForResult(this, RC_SAVE);
                            } catch (IntentSender.SendIntentException exception) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private Credential createCredential(GoogleSignInAccount googleSignInAccount) {
        return new Credential.Builder(googleSignInAccount.getEmail())
                .setAccountType(IdentityProviders.GOOGLE)
                .setName(googleSignInAccount.getDisplayName())
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_RESOLVE) {
            if (resultCode == RESULT_OK)
                onCredentialRetrieved(data.getParcelableExtra(Credential.EXTRA_KEY));
        }
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignedInAccount(task.getResult());
        }
    }

    private void createNewAccount() {
        GoogleSignInClient signInClient = createGoogleSingInClient();
        signOutLastUser(signInClient);
    }

    private void signOutLastUser(GoogleSignInClient signInClient) {
        signInClient.signOut().addOnCompleteListener(task -> prepareCreateNewAccountDialog());
    }

    private void prepareCreateNewAccountDialog() {
        GoogleSignInClient signInClient = createGoogleSingInClient();
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onCredentialRequestFailure(Task<CredentialRequestResponse> task) {
        Exception exception = task.getException();
        if (exception instanceof ResolvableApiException)
            resolveResult((ResolvableApiException) exception, RC_RESOLVE);
        else if (exception instanceof ApiException) finish();
    }

    private void resolveResult(ResolvableApiException rae, int requestCode) {
        try {
            rae.startResolutionForResult(LoginActivity.this, requestCode);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }

    }

    private void onPositiveLogin() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
