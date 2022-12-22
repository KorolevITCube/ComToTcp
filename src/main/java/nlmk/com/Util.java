package nlmk.com;

public class Util {
    public static String convertBytesToString(Byte[] array){
        String forLog = "";
        for(Byte a : array){
            forLog += Integer.toHexString(Byte.toUnsignedInt(a)).toString() + " ";
        }
        return forLog;
    }

    public static String convertBytesToString(byte[] array){
        String forLog = "";
        for(Byte a : array){
            forLog += Integer.toHexString(Byte.toUnsignedInt(a)).toString() + " ";
        }
        return forLog;
    }
}
