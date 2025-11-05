import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Part3_3 {

    // Toggle: stop after first plausible key pair found
    private static final boolean STOP_AFTER_FIRST = true;

    // Convert 0..25 -> 'A'..'Z', else return '?'
    private static String toChar(int cascii) {
        return (cascii >= 0 && cascii <= 25) ? String.valueOf((char) ('A' + cascii)) : "?";
    }

    // Read ciphertext bits from file and remove whitespace/newlines
    private static String readCiphertextFromFile(String filename) {
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
        String filename = (args.length > 0) ? args[0] : "msg2.txt";
        String ciphertext = readCiphertextFromFile(filename);

        if (ciphertext.isEmpty()) {
            System.err.println("No ciphertext loaded from " + filename);
            return;
        }
        if (ciphertext.length() % 8 != 0) {
            System.err.println("Ciphertext length is not a multiple of 8: length = " + ciphertext.length());
            return;
        }

        // split into 8-bit blocks
        List<String> blocks = new ArrayList<>();
        for (int i = 0; i < ciphertext.length(); i += 8) {
            blocks.add(ciphertext.substring(i, i + 8));
        }
        final int numBlocks = blocks.size();

        System.out.println("Starting brute-force 2-key Triple S-DES search on " + numBlocks + " blocks...");
        long startTime = System.nanoTime();

        // --- VARIABLES TO STORE THE FINAL RESULT ---
        String foundK1 = null;
        String foundK2 = null;
        String decryptedMessage = null;
        // ---------------------------------------------

        long pairsTested = 0;
        int progressInterval = 10000; // print progress every 10k pairs

        outer:
        for (int k1 = 0; k1 < 1024; k1++) {
            String key1 = String.format("%10s", Integer.toBinaryString(k1)).replace(' ', '0');

            // Precompute D_K1(C) for all ciphertext blocks
            String[] d1 = new String[numBlocks];
            boolean precomputeOk = true;
            for (int i = 0; i < numBlocks; i++) {
                try {
                    d1[i] = SDES.decryptSDES(key1, blocks.get(i)); 
                    if (d1[i] == null || d1[i].length() != 8 || !d1[i].matches("[01]{8}")) {
                        precomputeOk = false;
                        break; 
                    }
                } catch (Throwable t) {
                    precomputeOk = false;
                    break;
                }
            }
            if (!precomputeOk) continue;

            for (int k2 = 0; k2 < 1024; k2++) {
                String key2 = String.format("%10s", Integer.toBinaryString(k2)).replace(' ', '0');
                pairsTested++;

                // progress report
                if ((pairsTested % progressInterval) == 0) {
                    long elapsedNanos = System.nanoTime() - startTime;
                    double elapsedSec = elapsedNanos / 1e9;
                    System.out.printf("Tested pairs: %,d (current K1=%s K2=%s) — elapsed: %.1fs%n",
                            pairsTested, key1, key2, elapsedSec);
                }

                // try this (k1, k2) pair — Decryption: D_K1(E_K2(D_K1(C)))
                StringBuilder decrypted = new StringBuilder();
                boolean valid = true;
                for (int i = 0; i < numBlocks; i++) {
                    try {
                        String mid = SDES.encryptSDES(key2, d1[i]);         // E_K2(D_K1(C))
                        String finalPt = SDES.decryptSDES(key1, mid);      // D_K1(...)
                        if (finalPt == null || finalPt.length() != 8 || !finalPt.matches("[01]{8}")) {
                            valid = false;
                            break;
                        }
                        int cascii = Integer.parseInt(finalPt, 2);
                        String ch = toChar(cascii);
                        if (ch.equals("?")) { // not an A-Z mapping
                            valid = false;
                            break;
                        }
                        decrypted.append(ch);
                    } catch (Throwable t) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    // Store the results when found
                    foundK1 = key1;
                    foundK2 = key2;
                    decryptedMessage = decrypted.toString();
                    
                    if (STOP_AFTER_FIRST) break outer;
                }
            }
        }

        long totalElapsed = System.nanoTime() - startTime;
        
        // --- FINAL PRINT SECTION (Prints only after loops are complete) ---
        if (foundK1 != null) {
            System.out.println("\n************************************************");
            System.out.println("✅ SUCCESS: Triple S-DES Key Found!");
            System.out.printf("   Key 1 (K1): %s%n", foundK1);
            System.out.printf("   Key 2 (K2): %s%n", foundK2);
            System.out.printf("   Decrypted Message: %s%n", decryptedMessage);
            System.out.printf("   Total Pairs Tested: %,d (Elapsed Time: %.1fs)%n",
                    pairsTested, totalElapsed / 1e9);
            System.out.println("************************************************\n");
        } else {
            System.out.println("\n❌ FAILURE: No plausible key pair found.");
        }
        
        System.out.printf("Brute-force complete. Total time: %.1fs%n", totalElapsed / 1e9);
    }
}