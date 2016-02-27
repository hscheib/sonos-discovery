package discovery.sonos

import discovery.protocol.SimpleServiceDiscoveryProtocol
import discovery.sonos.constants.SonosDeviceType
import discovery.sonos.device.SonosDevice

class SonosDeviceFactory {

    List<Map<String, String>> sonosDiscoveryResponses

    SonosDeviceFactory() {
        new SimpleServiceDiscoveryProtocol().with {
            sendDiscovery()
            sonosDiscoveryResponses = listenForDiscoveryResponses("Sonos")
        }
    }

    SonosSystem getSonosSystem() {
        Map<String, SonosDevice> deviceMap = SonosDeviceType.values().collectEntries { SonosDeviceType deviceType ->
            findDevicesForSonosDeviceType(deviceType)
        }

        return new SonosSystem(deviceMap)
    }

    private Map<String, SonosDevice> findDevicesForSonosDeviceType(SonosDeviceType deviceType) {
        Map<String, SonosDevice> deviceMap = [:]

        List<Map<String, String>> responses = sonosDiscoveryResponses.findAll() { Map<String, String> response ->
            response.SERVER.toUpperCase().contains(deviceType.sonosCode)
        }

        List<String> locations = getLocationsForResponses(responses)
        if (locations.size() > 1) {
            List<SonosDevice> multiDeviceList = getMultiDeviceList(locations, responses, deviceType)
            multiDeviceList.eachWithIndex { SonosDevice sonosDevice, index ->
                deviceMap[deviceType.name() + "_$index"] = sonosDevice
            }
        } else {
            if (responses.size() > 0) {
                deviceMap[deviceType.name()] = new SonosDevice(responses, deviceType)
            }
        }

        return deviceMap
    }

    private List<SonosDevice> getMultiDeviceList(List<String> addressesForDeviceType, responses, SonosDeviceType deviceType) {
        return addressesForDeviceType.collect { String location ->
            List<Map<String, String>> locationResponsesForSingleLocation = responses.findAll() { Map<String, String> response ->
                response.LOCATION.toUpperCase().contains(location.toUpperCase())
            }

            new SonosDevice(locationResponsesForSingleLocation, deviceType)
        }
    }

    private List<String> getLocationsForResponses(List<Map<String, String>> responses) {
        List<String> responsesForSingleLocation = []

        responses.each { Map<String, String> response ->
            response.each { key, val ->
                if (key.equals("LOCATION") && !responsesForSingleLocation.contains(val)) {
                    responsesForSingleLocation << val
                }
            }
        }

        return responsesForSingleLocation
    }
}
