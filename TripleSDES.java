package default_package;

public class TripleSDES {

    public static String encrypt(String key1, String key2, String plainText) {
        String step1 = SDES.encryptSDES(key1, plainText);
        String step2 = SDES.decryptSDES(key2, step1);
        return SDES.encryptSDES(key1, step2);
    }

    public static String decrypt(String key1, String key2, String cipherText) {
        String step1 = SDES.decryptSDES(key1, cipherText);
        String step2 = SDES.encryptSDES(key2, step1);
        return SDES.decryptSDES(key1, step2);
    }

    public static void main(String[] args) {
        System.out.println(" Triple SDES Encryption:");
        String[][] encryptCases = {
            {"0000000000", "0000000000", "00000000"},
            {"1000101110", "0110101110", "11010111"},
            {"1000101110", "0110101110", "10101010"},
            {"1111111111", "1111111111", "10101010"}
        };
        for (String[] row : encryptCases) {
            String ct = encrypt(row[0], row[1], row[2]);
            System.out.printf("K1: %s, K2: %s, PT: %s → CT: %s\n", row[0], row[1], row[2], ct);
        }

        System.out.println("\n Triple SDES Decryption:");
        String[][] decryptCases = {
            {"1000101110", "0110101110", "11100110"},
            {"1011101111", "0110101110", "01010000"},
            {"0000000000", "0000000000", "10000000"},
            {"1111111111", "1111111111", "10010010"}
        };
        for (String[] row : decryptCases) {
            String pt = decrypt(row[0], row[1], row[2]);
            System.out.printf("K1: %s, K2: %s, CT: %s → PT: %s\n", row[0], row[1], row[2], pt);
        }
    }
}
