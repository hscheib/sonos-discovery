package discovery

import discovery.sonos.SonosDeviceFactory
import discovery.sonos.SonosSystem
import discovery.sonos.constants.SonosDeviceTypes

class Discovery {
    public static void main(String... args) {
        println "Starting"
        SonosSystem sonosSystem = new SonosDeviceFactory().getSonosSystem()

        if (sonosSystem.allDevices.size() > 0) {
            println "Sonos system found with $sonosSystem.allDevices.size devices!"
        }

        println "Sub count : " + sonosSystem.getDevicesByType(SonosDeviceTypes.SUB).size()
        println "Play1 count : " + sonosSystem.getDevicesByType(SonosDeviceTypes.PLAY1).size()
        println "Bridge count : " + sonosSystem.getDevicesByType(SonosDeviceTypes.BRIDGE).size()
        println "Play5 count : " + sonosSystem.getDevicesByType(SonosDeviceTypes.PLAY5).size()
        println "Finished"
    }
}
