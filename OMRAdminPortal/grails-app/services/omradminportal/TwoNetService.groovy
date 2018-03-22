package omradminportal

import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.xml.DOMBuilder
import groovy.xml.MarkupBuilder

@Transactional
class TwoNetService {
    def networkConfig

    def createVirtualHub(String authCode, String activationDate) {
        def connection = new URL(networkConfig.twoNetBaseURL + "/vhub/create")
        def req = this.getCreateVirtualHubRequestt(authCode, activationDate)
        println req
        def result = connection.openConnection().with {
            setRequestMethod("PUT")
            doOutput = true
            setRequestProperty("Content-Type", "application/xml")
            setRequestProperty("customerId", networkConfig.accountId)
            setRequestProperty("authKey", networkConfig.twoNetAuthKey)
            outputStream.withWriter {writer ->
                writer << req
            }
            inputStream.withReader { reader ->
                 reader.text
            }
        }
        println result
        XmlSlurper slurper = new XmlSlurper()
        def response = slurper.parseText(result)
        println response.name()
        println response.VirtualHubIdentifier.virtualHubId.text()
        return response.VirtualHubIdentifier.virtualHubId.text()
    }

    def registerDevice(String vHubId, String serialNumber, String modelName, String macAddress, String airInterface) {
        def connection = new URL(networkConfig.twoNetBaseURL + "/device/register")
        def req = this.getRegisterDeviceToVHubRequest(vHubId, serialNumber, modelName, macAddress, airInterface)
        println req
        def result = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/xml")
            setRequestProperty("customerId", networkConfig.accountId)
            setRequestProperty("authKey", networkConfig.twoNetAuthKey)
            outputStream.withWriter {writer ->
                writer << req
            }
            inputStream.withReader { reader ->
                reader.text
            }
        }
        println result
        return result
    }

    protected String getCreateVirtualHubRequestt(String authnCode, String dateOfActivation) {
        StringWriter writer = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(writer)

        builder.AddVirtualHub(xmlns: "urn:com.twonet.sp.cuc.revisiony", {
            accountId networkConfig.accountId
            appId networkConfig.mobileAppId
            authCode authnCode
            activationTime dateOfActivation
        })
        println writer.toString()
        return writer.toString()
    }

    protected String getRegisterDeviceToVHubRequest(String vHubId, String serialNumber, String modelName, String macAddress, String airInterface) {
        StringWriter writer = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(writer)
        builder.RegisterDevice(xmlns: "urn:com.twonet.sp.cuc.revisiony") {
            deviceIdentifier {
                serialNumberAndModel {
                    deviceSerialNumber serialNumber
                    deviceModelName modelName
                }
                airInterfaceAndAddress {
                    deviceAddress macAddress
                    airInterfaceType airInterface
                }
            }
            hub {
                "HubId" vHubId
                "HubIdType" "VHID"
            }
        }
        println writer.toString()
        return writer.toString()
    }
}
