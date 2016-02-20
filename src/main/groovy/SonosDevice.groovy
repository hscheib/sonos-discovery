import wslite.soap.SOAPClient

class SonosDevice {

    SOAPClient soapClient
    List usnList = []
    String deviceDescriptionLocation
    String hostname

// example SOAP action
// new SonosDevice("http://192.168.1.124:1400/DeviceProperties/Control").subscribe('urn:schemas-upnp-org:service:DeviceProperties:1#GetZoneAttributes')

    SonosDevice(List<Map> deviceResponses) {
        parseDeviceResponses(deviceResponses)
    }

    def subscribe(soapAction) {
        def response = soapClient.send(SOAPAction: soapAction) {

        }
        println response.httpResponse.contentAsString
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
        soapClient = new SOAPClient(hostname)
    }
}