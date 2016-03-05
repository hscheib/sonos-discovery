package discovery.protocol

import com.google.common.base.Stopwatch
import groovy.util.logging.Slf4j

import java.util.concurrent.TimeUnit

import static discovery.sonos.SonosSystemFactory.containsIgnoreCase

@Slf4j
class SimpleServiceDiscoveryProtocol {

    public static final String SSDP_MULTICAST_IP = "239.255.255.250"
    public static final int SSDP_UDP_PORT = 1900
    public static final int SSDP_TIMEOUT_IN_SECONDS = 10
    public static final InetSocketAddress SSDP_SOCKET = new InetSocketAddress(SSDP_MULTICAST_IP, SSDP_UDP_PORT)
    public static final int LOCALHOST_UDP_PORT = 1901
    public static final InetSocketAddress LOCALHOST_SOCKET = new InetSocketAddress(InetAddress.getLocalHost(), LOCALHOST_UDP_PORT)

    static List<Map<String, String>> discoverDevicesByServerField(String deviceName) {
        discoverDevicesByContainsKeyValue("SERVER", deviceName)
    }

    static List<Map<String, String>> discoverDevicesByContainsKeyValue(String discoveryResponseKey, String deviceIdentifier){
        sendDiscoveryMulticast()
        List<DatagramPacket> discoveryResponsePackets = captureDiscoveryResponsePackets(deviceIdentifier)

        List<Map<String, String>> discoveryResponses = []
        discoveryResponsePackets.each { packet ->
            Map<String, String> discoveryResponse = parseRawDiscoveryResponseToMap(packet.data)

            boolean responseHasKeyWeCareAbout = discoveryResponse.keySet().any { containsIgnoreCase(it, discoveryResponseKey) }
            if(responseHasKeyWeCareAbout) {
                if(deviceIdentifier == '*') {
                    discoveryResponses << discoveryResponse
                } else if (containsIgnoreCase(discoveryResponse.get(discoveryResponseKey), deviceIdentifier)) {
                    discoveryResponses << discoveryResponse
                }
            }
        }
        return discoveryResponses
    }

    private static void sendDiscoveryMulticast() {
        MulticastSocket socket = null
        try {
            socket = new MulticastSocket(LOCALHOST_SOCKET)
            socket.send(buildDiscoveryDatagramPacket())
        } catch (IOException e) {
            log.error("Failed to send discovery datagram due to: $e.message", e)
        } finally {
            socket?.disconnect()
            socket?.close()
        }
    }

    private static DatagramPacket buildDiscoveryDatagramPacket() {
        String discoveryMessage = "M-SEARCH * HTTP/1.1\r\n" +
                        "HOST: $SSDP_MULTICAST_IP:$SSDP_UDP_PORT\r\n" +
                        'MAN: "ssdp:discover"\r\n' +
                        "MX: $SSDP_TIMEOUT_IN_SECONDS\r\n" +
                        "ST: ssdp:all\r\n"

        return new DatagramPacket(
                discoveryMessage.bytes,
                discoveryMessage.bytes.length,
                SSDP_SOCKET
        )
    }


    /**
     * This method blocks for a small window of time while it captures datagram packets from
     * the local network to analyze for the given device type data
     *
     * @param deviceType the device type displayed on the desired datagram packet to capture
     * @return a list of maps of key-value discovery responses
     */
    private static List<DatagramPacket> captureDiscoveryResponsePackets(String deviceType) {
        List<DatagramPacket> responsePackets = []

        MulticastSocket receiveSocket = new MulticastSocket(LOCALHOST_SOCKET)
        receiveSocket.with {
            setTimeToLive(10)
            setSoTimeout(1000)
            joinGroup(SSDP_SOCKET.address)
        }

        log.info "Discovering for $SSDP_TIMEOUT_IN_SECONDS seconds..."

        Stopwatch timer = Stopwatch.createStarted()
        while (timer.elapsed(TimeUnit.SECONDS) < SSDP_TIMEOUT_IN_SECONDS) {
            try {
                byte[] buffer = new byte[2048]
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length)

                receiveSocket.receive(datagramPacket)

                responsePackets << datagramPacket
            } catch (SocketTimeoutException ignored) { }
        }

        receiveSocket.disconnect()
        receiveSocket.close()

        return responsePackets
    }

    /**
     * This parses responses like the following:
     *
     * <pre>
     *     HTTP/1.1 200 OK
     *     HOST: 239.255.255.250:1900
     *     EXT:
     *     CACHE-CONTROL: max-age=100
     *     LOCATION: http://192.168.1.131:80/description.xml
     *     SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/1.10.0
     *     hue-bridgeid: 001788FFFE24F29C
     *     ST: urn:schemas-upnp-org:device:basic:1
     *     USN: uuid:2f402f80-da50-11e1-9b23-00178824f29c
     * </pre>
     *
     * into a key-value map, ignoring lines that do not contain a colon (:).
     *
     */
    private static Map<String, String> parseRawDiscoveryResponseToMap(byte[] rawHttpResponse) {
        return new String(rawHttpResponse)
                .split('\r\n')
                .inject([:]) { LinkedHashMap returnMap, String line ->
                    if(line.contains(':')){
                        List<String> splitLine = line.split(':', 2)*.trim().toList()

                        if(containsOnlyTwoEntriesThatAreNotFalsy(splitLine)){
                            returnMap << [(splitLine[0].toUpperCase()): splitLine[1]]
                        }
                    }
                    return returnMap
                }
    }

    private static boolean containsOnlyTwoEntriesThatAreNotFalsy(List list) {
        list.size() == 2 && list[0] && list[1]
    }
}
