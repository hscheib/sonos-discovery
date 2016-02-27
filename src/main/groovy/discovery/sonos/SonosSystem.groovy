package discovery.sonos

import discovery.sonos.constants.SonosDeviceType
import discovery.sonos.device.SonosDevice

class SonosSystem {
    Collection<SonosDevice> allDevices = []

    SonosSystem(Map<String, SonosDevice> deviceMap) {
        allDevices = deviceMap.values()
    }

    List<SonosDevice> getDevicesByType(SonosDeviceType deviceType){
        allDevices.findAll() { sonosDevice ->
            sonosDevice.deviceType == deviceType
        }
    }
}
