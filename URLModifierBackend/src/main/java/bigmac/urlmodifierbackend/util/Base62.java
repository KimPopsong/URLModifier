package bigmac.urlmodifierbackend.util;

/**
 * Base62를 만드는 클래스
 */
public class Base62 {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62.length();

    public static String encode(long value)
    {
        StringBuilder sb = new StringBuilder();

        while (value > 0)
        {
            sb.append(BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }

        return sb.reverse().toString();
    }

    public static long decode(String str)
    {
        long result = 0;
        for (int i = 0; i < str.length(); i++)
        {
            result = result * BASE + BASE62.indexOf(str.charAt(i));
        }
        return result;
    }
}
