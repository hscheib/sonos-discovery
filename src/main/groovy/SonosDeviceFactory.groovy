class SonosDeviceFactory {

    List<Map> sonosDiscoveryResponses

    SonosDeviceFactory() {
        SimpleServiceDiscoveryProtocol ssdp = new SimpleServiceDiscoveryProtocol()
        ssdp.sendDiscovery()
        sonosDiscoveryResponses = ssdp.listenForDiscoveryResponses("Sonos")
    }

    public SonosSystem getSonosSystem() {
        Map deviceMap = [:]
        for (prop in new SonosDeviceTypes().getProperties()) {
            if (!(prop.getValue().class == Class.class)) {
                findDevicesForSonosDeviceType(prop, deviceMap)

            }
        }
        return new SonosSystem(deviceMap.values().flatten())
    }

    private void findDevicesForSonosDeviceType(Map.Entry sonosDeviceType, Map deviceMap) {
        List<Map> responses = sonosDiscoveryResponses.findAll() {
            it.SERVER.toUpperCase().contains(sonosDeviceType.getValue())
        }
        List locations = getLocationsForResponses(responses)
        if (locations.size() > 1) {
            List multiDeviceList = getMultiDeviceList(locations, responses, sonosDeviceType.getValue())
            multiDeviceList.eachWithIndex { entry, i ->
                deviceMap[sonosDeviceType.getKey() + "_$i"] = entry
            }
        } else {
            if (responses.size() > 0) {
                deviceMap[sonosDeviceType.getKey()] = new SonosDevice(responses, sonosDeviceType.getValue())
            }
        }
    }

    private getMultiDeviceList(locations, responses, deviceType) {
        def multiDeviceList = []
        locations.each { String location ->
            multiDeviceList.add(new SonosDevice(responses.findAll() {
                it.LOCATION.toUpperCase().contains(location.toUpperCase())
            }, deviceType))
        }
        multiDeviceList
    }

    private List getLocationsForResponses(List<Map> responses) {
        List locations = []
        responses.eachWithIndex { Map response, index ->
            response.each { key, val ->
                if (key.equals("LOCATION") && !locations.contains(val)) {
                    locations.add(val)
                }
            }
        }
        locations
    }
}
