package discovery

import discovery.sonos.SonosDeviceFactory
import discovery.sonos.SonosSystem
import discovery.sonos.constants.SonosDeviceType

import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class Discovery {
    public static void main(String... args) {
        println "Starting"
//        SonosSystem sonosSystem = new SonosDeviceFactory().getSonosSystem()
//
//        if (sonosSystem.allDevices.size() > 0) {
//            println "Sonos system found with ${sonosSystem.allDevices.size()} devices!"
//        }
//
//        println sonosSystem.getDevicesByType(SonosDeviceType.PLAY3).first().hostname
//
//        println "Sub count : " + sonosSystem.getDevicesByType(SonosDeviceType.SUB).size()
//        println "Play1 count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAY1).size()
//        println "Play3 count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAY3).size()
//        println "Play5 count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAY5).size()
//        println "Playbar count : " + sonosSystem.getDevicesByType(SonosDeviceType.PLAYBAR).size()
//        println "Bridge count : " + sonosSystem.getDevicesByType(SonosDeviceType.BRIDGE).size()

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(new FileReader("src/main/groovy/discovery/script.js"));


        Invocable invocable = (Invocable) engine;

        Object result = invocable.invokeFunction("hello", "Peter Parker");
        System.out.println(result);
        System.out.println(result.getClass());

        println "Finished"
    }
}
