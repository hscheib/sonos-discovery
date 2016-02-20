class Discovery {
    //if on wireless network add to VM args -Djava.net.preferIPv4Stack=true
    public static void main(String... args) {
        println "Starting"
        SonosDevice sub = new SonosDeviceFactory().getByType(SonosDeviceTypes.SUB)
        println sub.dump()
        println "Finished"
    }
}
