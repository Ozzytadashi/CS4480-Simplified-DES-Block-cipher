import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Part3_2 {

    // Convert 0..25 → 'A'..'Z', else return '?'
    public static String toChar(int cascii) {
        return (cascii >= 0 && cascii <= 25) ? String.valueOf((char) ('A' + cascii)) : "?";
    }

    // Read ciphertext from file and remove whitespace
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
        String ciphertext = readCiphertextFromFile("msg1.txt");
        if (ciphertext.isEmpty()) {
            System.err.println("No ciphertext loaded.");
            return;
        }

        // Ensure length is multiple of 8
        if (ciphertext.length() % 8 != 0) {
            System.err.println("Ciphertext length not multiple of 8: " + ciphertext.length());
            return;
        }

        System.out.println("Brute-force S-DES Key Search (printing plausible results only):");

        int found = 0;
        for (int key = 0; key < 1024; key++) {
            String rawKey = String.format("%10s", Integer.toBinaryString(key)).replace(' ', '0');
            StringBuilder decrypted = new StringBuilder();
            boolean valid = true;

            for (int i = 0; i < ciphertext.length(); i += 8) {
                String block = ciphertext.substring(i, i + 8);

                String pt;
                try {
                    pt = SDES.decryptSDES(rawKey, block); // returns 8-bit binary string
                } catch (Exception e) {
                    valid = false;
                    break;
                }

                if (pt == null || pt.length() != 8 || !pt.matches("[01]{8}")) {
                    valid = false;
                    break;
                }

                int cascii;
                try {
                    cascii = Integer.parseInt(pt, 2);
                } catch (NumberFormatException nfe) {
                    valid = false;
                    break;
                }

                String ch = toChar(cascii);
                if (ch.equals("?")) {
                    valid = false;
                    break;
                }
                decrypted.append(ch);
            }

            if (valid) {
                System.out.println("Key: " + rawKey + " → Message: " + decrypted);
                found++;
            }
        }

        System.out.println("Brute-force complete. Plausible keys found: " + found);
    }
}
