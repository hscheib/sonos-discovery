class SimpleServiceDiscoveryProtocol {

    String ssdpMulticastIp = "239.255.255.250"
    int discoveryTimeout = 10

    SimpleServiceDiscoveryProtocol() {}

    public void sendDiscovery() {
        int ssdpUdpPort = 1900
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ssdpMulticastIp), ssdpUdpPort)
        MulticastSocket socket = new MulticastSocket(null)
        try {
            String myLocalIp = InetAddress.localHost.hostAddress
            socket.bind(new InetSocketAddress(myLocalIp, 1901))
            StringBuilder packet = new StringBuilder()
            packet.append("M-SEARCH * HTTP/1.1\r\n")
            packet.append("HOST: $ssdpMulticastIp:$ssdpUdpPort\r\n")
            packet.append("MAN: \"ssdp:discover\"\r\n")
            packet.append("MX: ").append("$discoveryTimeout").append("\r\n")
            packet.append("ST: ").append("ssdp:all").append("\r\n").append("\r\n")
            byte[] data = packet.toString().bytes
            socket.send(new DatagramPacket(data, data.length, socketAddress))
        } catch (IOException e) {
            throw e
        } finally {
            socket.disconnect()
            socket.close()
        }
    }


    public listenForDiscoveryResponses(String deviceType) {
        def devices = []
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
                if (result.SERVER.contains(deviceType)) {
                    devices.add(result)
                }
            } catch (SocketTimeoutException e) {
            }
        }
        recSocket.disconnect()
        recSocket.close()
        devices
    }

    private Object parseData(String originaldata) {
        originaldata.split('\r\n').inject([:]) { map, token ->
            try {
                if (token.contains("Location") || token.contains("LOCATION") || token.contains("USN") || token.contains("UUID")) {
                    //These tokens have multiple ':' that need to be accounted for
                    token.split(':').with {
                        def value = ""
                        it.eachWithIndex { String entry, int i ->
                            if (i == 0) {
                                // index 0 is the key
                            } else if (i == 1) {
                                value += entry
                            } else {
                                value += ":" + entry
                            }
                        }
                        map[it[0].toUpperCase()] = value
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
}
