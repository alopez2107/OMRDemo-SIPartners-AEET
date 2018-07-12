package twonetendpoint

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugins.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ApplicationController implements PluginManagerAware {
    def devOps
    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }

    def process2NetData() {
        String qcl_json_data = params.qcl_json_data
        println qcl_json_data
        QLCJSONData data = createQLCJSONData(qcl_json_data)
        def dataJSON = [spO2 : data.deviceReadingOxigen, pulseRate : data.deviceReadingPulse ]
        JsonBuilder builder = new JsonBuilder(dataJSON)
        println builder.toPrettyString()
        DeviceReading reading = devOps.addReadingToDevice(data.devSerial, builder.toPrettyString(), data.hubReceivingTime, data.vHubId)
        render reading as JSON
    }

    private QLCJSONData createQLCJSONData(String data) {
        JsonSlurper dataParser = new JsonSlurper()
        def jsonData = dataParser.parseText(data)
        QLCJSONData devData = new QLCJSONData()

        devData.devSerial = jsonData.deviceDetails.serialNumber.value
        println "Device serial number " + devData.devSerial
        devData.devModel = jsonData.deviceDetails.decoderModel.value
        println "Device Model " + devData.devModel
        devData.deviceReadingOxigen = jsonData.records[0].spO2.value + " " + jsonData.records[0].spO2.unit
        println "Oxigen percentage " + devData.deviceReadingOxigen
        devData.deviceReadingPulse = jsonData.records[0].pulseRate.value + " " + jsonData.records[0].pulseRate.unit
        println "Pulse reading " + devData.deviceReadingPulse
        long hubRecTime = Long.parseLong(jsonData.twonetProperties.hubReceiveTime.value.toString())
        devData.hubReceivingTime = new Date(hubRecTime).toString()
        devData.vHubId = jsonData.twonetProperties.hubId.value
        return devData
    }
}
