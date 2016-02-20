class SonosDeviceFactory {

    List<Map> sonosDiscoveryResponses

    SonosDeviceFactory() {
        SimpleServiceDiscoveryProtocol ssdp = new SimpleServiceDiscoveryProtocol()
        ssdp.sendDiscovery()
        sonosDiscoveryResponses = ssdp.listenForDiscoveryResponses("Sonos")
    }

    public SonosDevice getByType(sonosDeviceType) {
        List<Map> deviceTypeReponses = sonosDiscoveryResponses.findAll() {
            it.SERVER.toUpperCase().contains(sonosDeviceType)
        }
        return new SonosDevice(deviceTypeReponses)
    }
}
