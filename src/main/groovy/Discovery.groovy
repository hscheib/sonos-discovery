package discovery

import discovery.sonos.SonosSystem
import discovery.sonos.SonosSystemFactory
import discovery.sonos.constants.SonosDeviceType
import groovy.util.logging.Slf4j

@Slf4j
class Discovery {
    public static void main(String... args) {
        log.info "~~~~ Sonos-Discovery Started ~~~~"
        SonosSystem sonosSystem = new SonosSystemFactory().getSonosSystem()

        if (sonosSystem.allDevices) {
            log.info "Sonos system found with ${sonosSystem.allDevices.size()} devices!"
        }

        sonosSystem.getDevicesByType(SonosDeviceType.PLAY3).first().getZoneAttributes()

        log.info "Sub count : " + sonosSystem.getDevicesByType(SonosDeviceType.SUB).size()
        log.info "Play1 count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAY1).size()
        log.info "Play3 count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAY3).size()
        log.info "Play5 count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAY5).size()
        log.info "Playbar count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAYBAR).size()
        log.info "Bridge count : " + sonosSystem.getDevicesByType(SonosDeviceType.BRIDGE).size()

        log.info "~~~~ Sonos-Discovery Finished ~~~~"
    }
}
