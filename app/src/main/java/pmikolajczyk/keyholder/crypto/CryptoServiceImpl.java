package pmikolajczyk.keyholder.crypto;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoServiceImpl implements CryptoService {
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String transformation = "AES/GCM/NoPadding";
    private static final String digestAlgorithm = "SHA-256";
    private static final String keyAlias = "keyholder.crypto.keyAlias";
    private static final String generateKeyAlgorithm = "AES";
    private static final String ivSeparator = "@@@@@";
    private KeyStore keyStore;

    @Override
    public String encrypt(String plaintext) {
        initializeKeyStore();
        return encryptWithKey(plaintext, getKey());
    }

    @Override
    public String encrypt(String plaintext, String seed) {
        Key key = generateKeyFromSeed(seed);
        return encryptWithKey(plaintext, key);
    }

    private String encryptWithKey(String plaintext, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            String result = wrapWithIV(ciphertext, cipher.getIV());

            Log.e("CRYPTO", "encryptWithKey: " + plaintext);
            Log.e("CRYPTO", "encryptWithKey: " + result);
            Log.e("CRYPTO", "encryptWithKey: " + decryptWithKey(result, key));

            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    private String wrapWithIV(byte[] ciphertext, byte[] iv) {
        String ciphertextString = Base64.encodeToString(ciphertext, Base64.NO_WRAP);
        String ivString = Base64.encodeToString(iv, Base64.NO_WRAP);
        return concatIV(ciphertextString, ivString);
    }

    private String concatIV(String ciphertextString, String ivString) {
        return ivString.trim()
                + ivSeparator.trim()
                + ciphertextString.trim();
    }

    @Override
    public String decrypt(String ciphertext) {
        initializeKeyStore();
        return decryptWithKey(ciphertext, getKey());
    }

    @Override
    public String decrypt(String ciphertext, String seed) {
        Key key = generateKeyFromSeed(seed);
        return decryptWithKey(ciphertext, key);
    }

    private String decryptWithKey(String ciphertext, Key key) {
        try {
            byte[] iv = retrieveIV(ciphertext);
            ciphertext = retrieveCiphertext(ciphertext);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] plaintext = cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP));
            return new String(plaintext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    private byte[] retrieveIV(String ciphertext) {
        String ivString = ciphertext.split(ivSeparator)[0];
        return Base64.decode(ivString, Base64.NO_WRAP);
    }

    private String retrieveCiphertext(String ciphertext) {
        return ciphertext.split(ivSeparator)[1];
    }

    private void initializeKeyStore() {
        try {
            getKeyStore();
            if (!keyStore.containsAlias(keyAlias))
                generateKey();
        } catch (KeyStoreException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    private void getKeyStore() {
        try {
            keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    private void generateKey() {
        KeyGenerator keyGenerator = createKeyGenerator();
        keyGenerator.generateKey();
    }

    private KeyGenerator createKeyGenerator() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore);
            keyGenerator.init(new KeyGenParameterSpec.Builder(keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());
            return keyGenerator;
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    private SecretKey getKey() {
        try {
            return (SecretKey) keyStore.getKey(keyAlias, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    private SecretKey generateKeyFromSeed(String seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
            digest.update(Base64.encode(seed.getBytes(), Base64.NO_WRAP));
            return new SecretKeySpec(digest.digest(), generateKeyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e.getMessage());
        }
    }
}
