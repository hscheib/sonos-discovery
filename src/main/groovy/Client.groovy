class Client {

    //if on wireless network add to VM args -Djava.net.preferIPv4Stack=true
    static String ssdpMulticastIp = "239.255.255.250"

    public static void main(String... args) {
//        System.setProperty("java.net.preferIPv4Stack", "true");
        int discoveryTimeout = 10

        sendDiscovery(discoveryTimeout)

        def sonosPlayers = listenForSonosPlayerResponses(discoveryTimeout)

        def sub = find(sonosPlayers, "anvil")

        println sub.dump()

        println "done"
//        new Sub("http://192.168.1.124:1400/DeviceProperties/Control").subscribe('urn:schemas-upnp-org:service:DeviceProperties:1#GetZoneAttributes')
//        new Sub("http://192.168.1.124:1400/MediaRenderer/RenderingControl/Control").subscribe('urn:schemas-upnp-org:service:RenderingControl:1#GetVolumeDB')
    }

    private static find(sonosPlayers, String sonosType) {

        def players = sonosPlayers.findAll() {
            it.SERVER.toLowerCase().contains(sonosType)
        }
        if(sonosType.equals("anvil")) {
            return new Sub(players)
        }
    }

    private static listenForSonosPlayerResponses(int discoveryTimeout) {
        def sonosPlayers = []
        MulticastSocket recSocket = new MulticastSocket(null)
        recSocket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 1901))
        recSocket.setTimeToLive(10)
        recSocket.setSoTimeout(1000)
        recSocket.joinGroup(InetAddress.getByName(ssdpMulticastIp))
        def currentMs = System.currentTimeMillis()
        while (System.currentTimeMillis() - currentMs < (discoveryTimeout * 1000)) {
            byte[] buf = new byte[2048]
            DatagramPacket input = new DatagramPacket(buf, buf.length)
            try {
                recSocket.receive(input)
                def result = parseData(new String(input.data))
                if (result.SERVER.contains("Sonos")) {
                    sonosPlayers.add(result)
                }
            } catch (SocketTimeoutException e) {
            }
        }
        recSocket.disconnect()
        recSocket.close()
        sonosPlayers
    }

    private static Object parseData(String originaldata) {
        originaldata.split('\r\n').inject([:]) { map, token ->
            try {//Location: http://192.168.1.1:1780/WFADevice.xml
                if (token.contains("Location") || token.contains("LOCATION")) {
                    token.split(':').with {
                        def url = ""
                        it.eachWithIndex { String entry, int i ->
                            if (i == 0) {
                                // index 0 is the key
                            } else if (i == 1) {
                                url += entry
                            } else {
                                url += ":" + entry
                            }
                        }
                        map[it[0].toUpperCase()] = url
                    }
                } else if (token.contains("USN") || token.contains("UUID")) {
                    token.split(':').with {
                        def usn = ""
                        it.eachWithIndex { String entry, int i ->
                            if (i == 0) {
                                // index 0 is the key
                            } else if (i == 1) {
                                usn += entry
                            } else {
                                usn += ":" + entry
                            }
                        }
                        map[it[0].toUpperCase()] = usn
                    }
                } else {


                    token.split(':').with { map[it[0].toUpperCase()] = it[1] }
                }
            } catch (e) {
                //dont care
            }
            map
        }
    }

    /**
     * Uses Simple Service Discovery Protocol to send a discovery request to the network
     */
    private static void sendDiscovery(int timeout) {
        println "sending discover..."
        int ssdpUdpIp = 1900
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ssdpMulticastIp), ssdpUdpIp)
        MulticastSocket socket = new MulticastSocket(null)
        try {
            String myLocalIp = "192.168.1.130"
            socket.bind(new InetSocketAddress(myLocalIp, 1901))
            StringBuilder packet = new StringBuilder()
            packet.append("M-SEARCH * HTTP/1.1\r\n")
            packet.append("HOST: $ssdpMulticastIp:$ssdpUdpIp\r\n")
            packet.append("MAN: \"ssdp:discover\"\r\n")
            packet.append("MX: ").append("$timeout").append("\r\n")
            packet.append("ST: ").append("ssdp:all").append("\r\n").append("\r\n")
            //packet.append( "ST: " ).append( "urn:Belkin:device:controllee:1" ).append( "\r\n" ).append( "\r\n" )
            byte[] data = packet.toString().bytes
            socket.send(new DatagramPacket(data, data.length, socketAddress))
        } catch (IOException e) {
            throw e
        } finally {
            socket.disconnect()
            socket.close()
        }
    }
//ANVIL SUB
    //BR200 BRIDGE
    //ZPS9 PLAYBAR
}
