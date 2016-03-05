package discovery.hue

import discovery.protocol.SimpleServiceDiscoveryProtocol

class HueSystemDetector {

    Optional<String> detectHueIpAddress() {
        String ipAddress

        List<Map<String, String>> discoveryResponses = SimpleServiceDiscoveryProtocol.
                discoverDevicesByContainsKeyValue('hue-bridgeid', '*')

        if (discoveryResponses) {
            ipAddress = parseIpFromDiscoveryResponses(discoveryResponses)
        }

        return Optional.ofNullable(ipAddress)
    }

    private String parseIpFromDiscoveryResponses(List<Map<String, String>> discoveryResponses) {
        try {
            return new URL(discoveryResponses.first().get("LOCATION")).getHost()
        } catch (Throwable ignored) { }
        return null
    }
}
