package app;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Java program to calculate SHA hash value

class CryptoUtilities {

  /**
   * calculates the SHA256 of a byte[]
   * returns the string representation of the SHA256 hash
   */
  public static String getSHA256(byte[] input)
    {

        try {
            // calculating the hash of a byte[]
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input);

            String hashtext = toHexString(messageDigest);

            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown"
                               + " for incorrect algorithm: " + e);

            return null;
        }
    }

    /**
    *  returns the hex-string representation of a byte[]
    */
    public static String toHexString(byte[] bytehash) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < bytehash.length; i++) {
           byte2hex(bytehash[i], buf);
        }
        return buf.toString();
    }


    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     * This code from http://java.sun.com/j2se/1.4.2/docs/guide/security/jce/JCERefGuide.html#HmacEx
     */
    public static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

}
