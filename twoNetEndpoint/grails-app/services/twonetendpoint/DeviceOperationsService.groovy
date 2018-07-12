package twonetendpoint

import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Transactional
class DeviceOperationsService {

    def networkConfig

    public DeviceOperationsService() {
        def hostnameVerifier = [
                verify: { hostname, session -> true }
        ]
        def trustManager = [
                checkServerTrusted: { chain, authType -> },
                checkClientTrusted: { chain, authType -> },
                getAcceptedIssuers: { null }
        ]
        SSLContext context = SSLContext.getInstance("SSL")
        context.init(null, [trustManager as X509TrustManager] as TrustManager[], null)

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier as HostnameVerifier)
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory())
    }


    def addReadingToDevice(String serialNumber, String readingData, String hubRecTime, String vHubId) {
        def connection = new URL(networkConfig.openIDMURL + "/openidm/managed/" + this.networkConfig.deviceReadingResourceName + "?_action=create")
        OMRDevice device = this.findDeviceBySerialNumberAndHubId(serialNumber, vHubId)
        def req = this.createDeviceReadingReq(device.deviceName, readingData, "managed/TwoNetDevice/" + device.idmId, hubRecTime)
        def result = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-OpenIDM-Username", networkConfig.openIDMAdminUserID)
            setRequestProperty("X-OpenIDM-Password", networkConfig.openIDMAdminPassword)
            outputStream.withWriter {writer ->
                writer << req
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        DeviceReading reading = this.createDeviceReadingObject(result, device)
        return reading
    }

    protected DeviceReading createDeviceReadingObject(result, device) {
        DeviceReading reading = new DeviceReading()
        reading.idmId = result._id
        reading.devName = result.devName
        reading.readingData = result.readingData
        reading.sourceDevice = device
        return reading
    }

    protected OMRDevice findDeviceBySerialNumberAndHubId(String serialNumber, String vHubId) {
        def connection = new URL(this.networkConfig.openIDMURL + "/openidm/managed/TwoNetDevice?_queryFilter=serialNumber" +
                URLEncoder.encode(" eq ", 'UTF-8') + "'" + serialNumber + "'&_fields=*")
        def queryResp = connection.openConnection().with {
            setRequestMethod("GET")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-OpenIDM-Username", this.networkConfig.openIDMAdminUserID)
            setRequestProperty("X-OpenIDM-Password", this.networkConfig.openIDMAdminPassword)
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println queryResp
        OMRDevice omrDevice = null
        queryResp.result.each {device ->
            if (device.vHubID == vHubId) {
                omrDevice = this.createOMRDevice(device)
            }
        }
        return omrDevice
    }

    protected OMRDevice createOMRDevice(result) {
        OMRDevice device = new OMRDevice()
        println "Device Data:\n" + result
        device.idmId = result._id
        device.deviceName = result.name
        device.serialNumber = result.serialNumber
        return device
    }

    protected String createDeviceReadingReq(String devName, String deviceReadingJSON, String devRefString, String hubRecTime) {
        def content = [devName: devName, readingData: deviceReadingJSON, sourceDevice: [_ref: devRefString], hubReceiveTime: hubRecTime]
        JsonBuilder builder =  new JsonBuilder(content)
        String payload = builder.toPrettyString()
        println payload
        return payload
    }
}
