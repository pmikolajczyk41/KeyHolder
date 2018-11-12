package pmikolajczyk.keyholder.crypto;

public interface CryptoService {
    String encrypt(String plaintext);
    String encrypt(String plaintext, String seed);
    String decrypt(String ciphertext);
    String decrypt(String ciphertext, String seed);
}