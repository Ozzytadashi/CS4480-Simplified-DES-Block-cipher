package default_package;

public class SDES {
    // Permutation tables
    private static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    private static final int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    private static final int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    private static final int[] IP_INV = {4, 1, 3, 5, 7, 2, 8, 6};
    private static final int[] EP = {4, 1, 2, 3, 2, 3, 4, 1};
    private static final int[] P4 = {2, 4, 3, 1};

    // S-boxes
    private static final String[][] S0 = {
        {"01", "00", "11", "10"},
        {"11", "10", "01", "00"},
        {"00", "01", "01", "11"},
        {"11", "01", "11", "10"}
    };
    private static final String[][] S1 = {
        {"00", "01", "10", "11"},
        {"10", "00", "01", "11"},
        {"11", "00", "01", "00"},
        {"10", "01", "00", "11"}
    };

    // Utility methods
    public static String permute(String input, int[] table) {
        StringBuilder result = new StringBuilder();
        for (int index : table) result.append(input.charAt(index - 1));
        return result.toString();
    }

    public static String leftShift(String key, int shifts) {
        String left = key.substring(0, 5);
        String right = key.substring(5);
        left = left.substring(shifts) + left.substring(0, shifts);
        right = right.substring(shifts) + right.substring(0, shifts);
        return left + right;
    }

    public static String xor(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) result.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
        return result.toString();
    }

    public static int binaryToInt(String bits) {
        return Integer.parseInt(bits, 2);
    }

    public static String sBox(String input, int box) {
        int row = binaryToInt("" + input.charAt(0) + input.charAt(3));
        int col = binaryToInt("" + input.charAt(1) + input.charAt(2));
        return box == 0 ? S0[row][col] : S1[row][col];
    }

    public static String generateRoundKey(String rawKey, int round) {
        String p10Key = permute(rawKey, P10);
        String shifted = leftShift(p10Key, round == 1 ? 1 : 3);
        return permute(shifted, P8);
    }

    public static String fk(String input, String subKey) {
        String left = input.substring(0, 4);
        String right = input.substring(4);
        String ep = permute(right, EP);
        String xored = xor(ep, subKey);
        String s0 = sBox(xored.substring(0, 4), 0);
        String s1 = sBox(xored.substring(4), 1);
        String p4 = permute(s0 + s1, P4);
        String leftXor = xor(left, p4);
        return leftXor + right;
    }

    public static String encryptSDES(String rawKey, String plainText) {
        String k1 = generateRoundKey(rawKey, 1);
        String k2 = generateRoundKey(rawKey, 2);
        String ip = permute(plainText, IP);
        String round1 = fk(ip, k1);
        String swapped = round1.substring(4) + round1.substring(0, 4);
        String round2 = fk(swapped, k2);
        return permute(round2, IP_INV);
    }

    public static String decryptSDES(String rawKey, String cipherText) {
        String k1 = generateRoundKey(rawKey, 1);
        String k2 = generateRoundKey(rawKey, 2);
        String ip = permute(cipherText, IP);
        String round1 = fk(ip, k2);
        String swapped = round1.substring(4) + round1.substring(0, 4);
        String round2 = fk(swapped, k1);
        return permute(round2, IP_INV);
    }

    public static void main(String[] args) {
        System.out.println(" SDES Part 1 — Test Cases:");
        String[][] knownTests = {
            {"0000000000", "10101010", "00010001"},
            {"1110001110", "10101010", "11001010"},
            {"1110001110", "01010101", "01110000"},
            {"1111111111", "10101010", "00000100"}
        };
        for (String[] row : knownTests) {
            String ct = encryptSDES(row[0], row[1]);
            System.out.printf("Key: %s, PT: %s → CT: %s (Expected: %s)\n", row[0], row[1], ct, row[2]);
        }

        System.out.println("\n SDES Part 1 — Encryption Cases:");
        String[][] encryptCases = {
            {"0000000000", "00000000"},
            {"1111111111", "11111111"},
            {"0000011111", "00000000"},
            {"0000011111", "11111111"}
        };
        for (String[] row : encryptCases) {
            String ct = encryptSDES(row[0], row[1]);
            System.out.printf("Key: %s, PT: %s → CT: %s\n", row[0], row[1], ct);
        }

        System.out.println("\n SDES Part 1 — Decryption Cases:");
        String[][] decryptCases = {
            {"1000101110", "00011100"},
            {"1000101110", "11000010"},
            {"0010011111", "10011101"},
            {"0010011111", "10010000"}
        };
        for (String[] row : decryptCases) {
            String pt = decryptSDES(row[0], row[1]);
            System.out.printf("Key: %s, CT: %s → PT: %s\n", row[0], row[1], pt);
        }
    }
}
