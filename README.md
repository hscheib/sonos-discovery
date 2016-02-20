# sonos-discovery

For implementation, see discovery.Discovery.main()

To run : gradlew run

If not using gradle, run discovery.Discovery.main() with the following VM args -Djava.net.preferIPv4Stack=true

Other notes: You cannot be running the native Sonos controller app, when running this app. They try to listen on the same port and an exception is thrown.