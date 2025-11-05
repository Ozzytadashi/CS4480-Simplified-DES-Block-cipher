package default_package;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Part3_3 {

    public static String toChar(int cascii) {
        return (cascii >= 0 && cascii <= 25) ? String.valueOf((char) ('A' + cascii)) : "?";
    }

    public static String readCiphertextFromFile(String filename) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String ciphertext = readCiphertextFromFile("msg2.txt");

        System.out.println("Brute-force Triple SDES Key Search:");
        for (int k1 = 0; k1 < 1024; k1++) {
            String key1 = String.format("%10s", Integer.toBinaryString(k1)).replace(' ', '0');
            for (int k2 = 0; k2 < 1024; k2++) {
                String key2 = String.format("%10s", Integer.toBinaryString(k2)).replace(' ', '0');
                StringBuilder decrypted = new StringBuilder();
                boolean valid = true;

                for (int i = 0; i < ciphertext.length(); i += 8) {
                    String block = ciphertext.substring(i, i + 8);
                    String pt = TripleSDES.decrypt(key1, key2, block);
                    int cascii = Integer.parseInt(pt, 2);
                    String ch = toChar(cascii);
                    if (ch.equals("?")) {
                        valid = false;
                        break;
                    }
                    decrypted.append(ch);
                }

                if (valid) {
                    System.out.println("K1: " + key1 + ", K2: " + key2 + " → Message: " + decrypted);
                }
            }
        }
    }
}

