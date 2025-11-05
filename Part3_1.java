
public class Part3_1 {

    public static String to8BitBinary(int value) {
        String binary = Integer.toBinaryString(value);
        while (binary.length() < 8) binary = "0" + binary;
        return binary;
    }

    public static void main(String[] args) {
        String plaintext = "CRYPTOGRAPHY";
        String key = "0111001101";

        System.out.println("Part 3.1: CASCII Encryption");
        System.out.println("Plaintext: " + plaintext);
        System.out.println("Key: " + key);
        System.out.print("Ciphertext (64 bits): ");

        StringBuilder finalCipher = new StringBuilder();

        for (int i = 0; i < 8; i++) { // Only first 8 characters
            char c = plaintext.charAt(i);
            int cascii = c - 'A'; // CASCII: A=0 to Z=25
            String pt = to8BitBinary(cascii);
            String ct = SDES.encryptSDES(key, pt);
            finalCipher.append(ct);
            System.out.print(ct);
        }

        System.out.println();
    }
}
