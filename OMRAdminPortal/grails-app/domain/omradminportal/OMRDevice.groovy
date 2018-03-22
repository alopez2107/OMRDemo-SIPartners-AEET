package omradminportal

class OMRDevice {
    String idmId
    String name
    String devSerial
    String macAddress
    String vHubId
    String modelName
    String umaResourceID
    String umaROCredential
    List<OMRReading> readings
    boolean activeStatus

    static constraints = {
    }
}
