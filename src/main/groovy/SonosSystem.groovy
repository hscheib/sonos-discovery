class SonosSystem {
    List<SonosDevice> allDevices = []

    SonosSystem(devices) {
        allDevices = devices
    }

    public List<SonosDevice> getDevicesByType(String deviceType){
        allDevices.findAll(){
            it.deviceType == deviceType
        }
    }
}
