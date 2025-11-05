public class SDES {
    // Permutation tables
    private static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    private static final int[] P8  = {6, 3, 7, 4, 8, 5, 10, 9};
    private static final int[] IP  = {2, 6, 3, 1, 4, 8, 5, 7};
    private static final int[] IP_INV = {4, 1, 3, 5, 7, 2, 8, 6};
    private static final int[] EP  = {4, 1, 2, 3, 2, 3, 4, 1};
    private static final int[] P4  = {2, 4, 3, 1};

    // Standard S-DES S-boxes
    private static final String[][] S0 = {
        {"01","00","11","10"},
        {"11","10","01","00"},
        {"00","10","01","11"},
        {"11","01","11","10"}
    };

    private static final String[][] S1 = {
        {"00","01","10","11"},
        {"10","00","01","11"},
        {"11","00","01","00"},
        {"10","01","00","11"}
    };

    // Permutation utility
    private static String permute(String input, int[] table) {
        StringBuilder sb = new StringBuilder();
        for (int i : table) sb.append(input.charAt(i - 1));
        return sb.toString();
    }

    // Left shift 5-bit halves
    private static String leftShift(String key, int shifts) {
        String left  = key.substring(0, 5);
        String right = key.substring(5);
        left  = left.substring(shifts) + left.substring(0, shifts);
        right = right.substring(shifts) + right.substring(0, shifts);
        return left + right;
    }

    // XOR utility
    private static String xor(String a, String b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length(); i++)
            sb.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
        return sb.toString();
    }

    // Convert 2-bit binary string to int
    private static int binaryToInt(String bits) {
        return Integer.parseInt(bits, 2);
    }

    // S-box lookup
    private static String sBox(String input, String[][] box) {
        int row = binaryToInt("" + input.charAt(0) + input.charAt(3));
        int col = binaryToInt("" + input.charAt(1) + input.charAt(2));
        return box[row][col];
    }

    // Generate round keys K1 and K2
    private static String[] generateRoundKeys(String rawKey) {
        String p10Key = permute(rawKey, P10);

        // Round 1: LS-1
        String round1Shift = leftShift(p10Key, 1);
        String k1 = permute(round1Shift, P8);

        // Round 2: LS-2 on result of round1Shift
        String round2Shift = leftShift(round1Shift, 2);
        String k2 = permute(round2Shift, P8);

        return new String[]{k1, k2};
    }

    // Fk function
    private static String fk(String input, String subKey) {
        String left  = input.substring(0, 4);
        String right = input.substring(4);

        String ep = permute(right, EP);
        String xored = xor(ep, subKey);

        String s0 = sBox(xored.substring(0, 4), S0);
        String s1 = sBox(xored.substring(4, 8), S1);

        String p4 = permute(s0 + s1, P4);
        String leftXor = xor(left, p4);

        return leftXor + right;
    }

    // Encrypt a single 8-bit block
    public static String encryptSDES(String rawKey, String plainText) {
        String[] keys = generateRoundKeys(rawKey);
        String ip = permute(plainText, IP);

        String round1 = fk(ip, keys[0]);
        String swapped = round1.substring(4) + round1.substring(0, 4);

        String round2 = fk(swapped, keys[1]);
        return permute(round2, IP_INV);
    }

    // Decrypt a single 8-bit block
    public static String decryptSDES(String rawKey, String cipherText) {
        String[] keys = generateRoundKeys(rawKey);
        String ip = permute(cipherText, IP);

        String round1 = fk(ip, keys[1]);
        String swapped = round1.substring(4) + round1.substring(0, 4);

        String round2 = fk(swapped, keys[0]);
        return permute(round2, IP_INV);
    }

    // Test cases
    public static void main(String[] args) {
        System.out.println("S-DES Test Cases:");

        String[][] tests = {
            {"0000000000", "10101010", "00010001"},
            {"1110001110", "10101010", "11001010"},
            {"1110001110", "01010101", "01110000"},
            {"1111111111", "10101010", "00000100"}
        };

        for (String[] t : tests) {
            String ct = encryptSDES(t[0], t[1]);
            String pt = decryptSDES(t[0], ct);
            System.out.printf("Key: %s, PT: %s → CT: %s, Decrypted: %s (Expected CT: %s)\n",
                    t[0], t[1], ct, pt, t[2]);
        }
    }
}
