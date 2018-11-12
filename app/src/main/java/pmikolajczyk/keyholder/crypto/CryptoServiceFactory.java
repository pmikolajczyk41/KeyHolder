package pmikolajczyk.keyholder.crypto;

public class CryptoServiceFactory {
    public static CryptoService getCryptoService(){
        return new CryptoServiceImpl();
    }
}
