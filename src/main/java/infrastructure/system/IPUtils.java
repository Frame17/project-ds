package infrastructure.system;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;

public class IPUtils {

    /**
     * Returns the int Representation corresponding to the given inetAddress
     * @param inetAddress
     * @return
     */
    public static int getIntRepresentation(InetAddress inetAddress){
        return InetAddresses.coerceToInteger(inetAddress);
    }
}
