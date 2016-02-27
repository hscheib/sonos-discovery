package discovery.sonos.constants

import groovy.transform.TupleConstructor

@TupleConstructor
enum SonosDeviceType {
    SUB("ANVIL"),
    PLAYBAR("ZPS9"),
    BRIDGE("BR200"),
    PLAY5("ZPS5"),
    PLAY3("ZPS3"),
    PLAY1("ZPS1")

    String sonosCode
}
