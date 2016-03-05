package discovery.sonos

import discovery.protocol.SimpleServiceDiscoveryProtocol
import discovery.sonos.constants.SonosDeviceType
import discovery.sonos.device.SonosDevice

class SonosSystemFactory {

    List<Map<String, String>> sonosDiscoveryResponses

    SonosSystemFactory() {
        sonosDiscoveryResponses = SimpleServiceDiscoveryProtocol.discoverDevicesByServerField('Sonos')
    }

    SonosSystem getSonosSystem() {
        Map<String, SonosDevice> deviceMap = SonosDeviceType.values().collectEntries { SonosDeviceType deviceType ->
            findDevicesForSonosDeviceType(deviceType)
        }

        return new SonosSystem(deviceMap)
    }

    private Map<String, SonosDevice> findDevicesForSonosDeviceType(SonosDeviceType deviceType) {
        List<Map<String, String>> responsesForDeviceType = sonosDiscoveryResponses.findAll() { Map<String, String> response ->
            containsIgnoreCase(response.SERVER, deviceType.sonosCode)
        }

        List<String> addressesForDeviceType = extractUniqueDeviceAddresses(responsesForDeviceType)
        List<SonosDevice> multiDeviceList = createSonosDevices(addressesForDeviceType, responsesForDeviceType, deviceType)

        int deviceCounter = 0
        return multiDeviceList.collectEntries() { SonosDevice sonosDevice ->
            [(deviceType.name() + "_${deviceCounter++}"): sonosDevice]
        }
    }

    private List<SonosDevice> createSonosDevices(List<String> addressesForDeviceType, responses, SonosDeviceType deviceType) {
        return addressesForDeviceType.collect { String location ->
            List<Map<String, String>> locationResponsesForSingleLocation = responses.findAll() { Map<String, String> response ->
                containsIgnoreCase(response.LOCATION, location)
            }

            new SonosDevice(locationResponsesForSingleLocation, deviceType)
        }
    }

    public static boolean containsIgnoreCase(String str1, String str2){
        if(!str1 || !str2) return false
        return str1.toUpperCase().contains(str2.toUpperCase())
    }

    private List<String> extractUniqueDeviceAddresses(List<Map<String, String>> responses) {
        List<String> responsesForSingleLocation = []

        responses.each { Map<String, String> response ->
            response.each { String key, String val ->
                if (key.equalsIgnoreCase("LOCATION") && !responsesForSingleLocation.contains(val)) {
                    responsesForSingleLocation << val
                }
            }
        }

        return responsesForSingleLocation
    }
}
