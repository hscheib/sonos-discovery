package discovery.sonos.device

import discovery.sonos.constants.SonosDeviceType
import groovy.util.logging.Slf4j
import groovy.xml.XmlUtil
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse

@Slf4j
class SonosDevice {

    SOAPClient soapClient
    List usnList = []
    String deviceDescriptionLocation
    String hostname
    SonosDeviceType deviceType

// example SOAP action
// new SonosDevice("http://192.168.1.124:1400/DeviceProperties/Control").subscribe('urn:schemas-upnp-org:service:DeviceProperties:1#GetZoneAttributes')

    SonosDevice(List<Map> deviceResponses, SonosDeviceType deviceType) {
        this.deviceType = deviceType
        parseDeviceResponses(deviceResponses)
    }

    def getZoneAttributes() {
        String method = "GetZoneAttributes"
        SOAPResponse response = soapClient.send(SOAPAction: "urn:schemas-upnp-org:service:DeviceProperties:1#$method") {

        }
        def x = response.body
        def rootNode =  new XmlSlurper().parseText(response.text)

        log.info "SOAP response for $method:\n${XmlUtil.serialize(response.text)}"
    }

    private List<Map> parseDeviceResponses(List<Map> responses) {
        responses.eachWithIndex { Map response, index ->
            response.each { key, val ->
                if (index == 0 && key.equals("LOCATION")) {
                    parseLocation(val)
                } else if (key.equals("USN")) {
                    usnList.add(val)
                }
            }
        }
    }

    private void parseLocation(String location) {
        URL url = new URL(location);
        hostname = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort()
        deviceDescriptionLocation = url.getPath()
        soapClient = new SOAPClient(hostname +"/DeviceProperties/Control" )
    }
}