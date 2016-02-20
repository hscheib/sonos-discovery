import wslite.soap.SOAPClient

class Sub {

    SOAPClient subSOAPClient
    List usnList = []
    String deviceDescriptionLocation
    String hostname

    Sub(List<Map> players) {
//        subSOAPClient = new SOAPClient(url)
        players.eachWithIndex { Map player, index ->

            player.each { key, val ->
                if (index == 0 && key.equals("LOCATION")) {
                    parseLocation(val)
                }
                if (key.equals("USN")) {
                    usnList.add(val)
                }
            }

        }
    }

    private void parseLocation(String location) {
        URL url = new URL(location);
        hostname = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort()
        deviceDescriptionLocation = url.getPath()

    }

    def subscribe(soapAction) {

        def response = subSOAPClient.send(SOAPAction: soapAction) {

        }

        println response.httpResponse.contentAsString

    }

}
