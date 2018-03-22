package omradminportal

import org.apache.commons.lang.RandomStringUtils

import javax.servlet.http.Cookie

class PortalController {
    static String tokenId
    static OMRUser loggedInUser

    def idmService
    def twoNetService
    def securityContext
    def uiConfig

    def index() {

    }

    def devices() {
        def decision = params.decision
        println "User Decision was " + decision
        render(view: "devices", model: [uiConfig: uiConfig, securityContext: securityContext])
    }

    def login() {

    }

    def logout() {
        String ssoCookie = request.getCookies().find { it.name == 'iplanetDirectoryPro' }?.value
        println "SSOCookie : $ssoCookie will be terminated."
        idmService.logout(ssoCookie)
        render(view: "index")
    }

    def authenticate() {
        String userName = params.userName
        String password = params.password
        (tokenId, loggedInUser) = idmService.authenticate(userName, password)
        println "Token ID is " + tokenId
        if (tokenId == "INVALID") {
            println "Error Detected : " + tokenId
            // RENDER VIEW ERROR
            render(view: "login", model: [errorMessage: "Authentication unsuccessful. Please retry."])
        } else {
            def openAMSessionCookie = new Cookie("iplanetDirectoryPro", tokenId)
            response.addCookie openAMSessionCookie
            securityContext.loggedInUser = loggedInUser
            securityContext.roles = loggedInUser.roles
            securityContext.ssoCookie = tokenId
            if (securityContext.isAdmin()) {
                securityContext.loggedInUser.password = password
                render(view: "admin", model: [securityContext: securityContext])
            } else {
                render(view: "devices", model: [securityContext: securityContext, uiConfig: uiConfig])
            }
        }
    }

    def activateDevice() {
        String umaResourceID = params.umaResourceID
        OMRDevice selectedDevice = findSelectedDevice()
        String charset = (('A'..'Z') + ('0'..'9')).join()
        Integer length = 15
        String authCode = RandomStringUtils.random(length, charset.toCharArray())
        String activationDate = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", TimeZone.getTimeZone("UTC"))
        String finalDate = new StringBuilder(activationDate).insert(activationDate.length()-2, ':').toString()

        String vHubId = twoNetService.createVirtualHub(authCode, finalDate)
        println "The Virtual Hub with ID ${vHubId} has been created."
        twoNetService.registerDevice(vHubId, selectedDevice.devSerial, selectedDevice.modelName, selectedDevice.macAddress, "BTLE")
        // After success modify user with Authentication Code and set deviceActiveStatus to true
        securityContext.loggedInUser = idmService.update2NetInfo(umaResourceID, securityContext.loggedInUser.loginId, securityContext.loggedInUser.idmId, vHubId, selectedDevice.idmId, authCode, true)
        render(view: "devices", model: [securityContext: securityContext, uiConfig: uiConfig])


    }

    def collectConsent() {
        String devSerial = params.devSerial
        securityContext.selectedDeviceSN = devSerial
        println "Selected Device Serial Number is " + securityContext.selectedDeviceSN
        render(view: "umaAuthorize", model: [securityContext: securityContext])
    }

    def authorize() {
        boolean decision = params.decision
        println "The user decision was " + decision
        if (decision == true) {
            UMAResourceSet rs = idmService.registerUMAResourceSet(securityContext.loggedInUser.loginId, securityContext.loggedInUser.umaAccessToken, securityContext.selectedDeviceSN)
            String policyId = idmService.createUserAccessPolicy(securityContext.ssoCookie, rs.id, securityContext.loggedInUser.loginId, securityContext.loggedInUser.medicalAssignee)
            println "Policy with ID ${policyId} has been created"
            forward(action: "activateDevice", params: [umaResourceID: policyId])
        }
        else {
            render(view: "devices", model: [securityContext: securityContext, uiConfig: uiConfig])
        }
    }

    def umaAuthorize() {
        render(view: "umaAuthorize", model: [securityContext: securityContext])
    }

    def admin() {

    }

    def showPatient() {
        String id = params.id
        OMRUser patient = idmService.getUserByID(id)
        render(view: "show", model: [patient: patient, uiConfig: uiConfig, securityContext: securityContext])
    }

    def patientData() {
        String patientId = params.id
        OMRUser patient = idmService.getUserByID(patientId)
        String patientName = "${patient.firstName} ${patient.lastName}"
        render(view: "patientData", model: [patient: patient, patientName: patientName, devices: patient.devices, uiConfig: uiConfig])

    }

    def myPatients() {
        List<OMRUser> patients = idmService.getMyPatients(securityContext.getLoggedInUser().idmId)
        int offset = 0
        if (params.offset != null) {
            offset = params.getInt('offset')
        }
        int pageSize = 5
        if (params.max != null) {
            pageSize = params.getInt('max')
        }
        params.each {param ->
            println "Parameter : $param : value $param.value"
        }
        def patientList = this.getPage(offset, patients, pageSize)
        println "Patients list size ${patientList.size()} Page size: ${pageSize}"
        render(view: "myPatients", model: [patientsList: patientList, patientListCount: patients.size(), uiConfig: uiConfig, securityContext: securityContext])
    }

    def myStaff() {

    }

    def myEquipment() {

    }

    def mySchedule() {

    }

    def myOMRDevices() {

    }

    def showDevReadings() {
        String patientId = params.patientId
        OMRUser patient = idmService.getUserByID(patientId)
        String patientName = "${patient.firstName} ${patient.lastName}"
        String devId = params.id
        OMRDevice device = findSelectedDeviceFromPatient(devId, patient)
        String umaResID = device.umaResourceID
        String umaROCred = device.umaROCredential
        // Get PAT
        String pat = idmService.getAccessTokensForUMA(patient.loginId, umaROCred)
        String permTicket = idmService.getPermisionRequestTicket(pat, umaResID)
        String umaClaimToken = idmService.getUMAClaimToken(securityContext.loggedInUser.loginId, securityContext.loggedInUser.password)
        String rpAccessToken = idmService.getUMARequestingPartyAccessToken(permTicket, umaClaimToken)
        if (rpAccessToken == 'UNAUTHORIZED') {
            StringBuffer errorMessage = new StringBuffer()
            errorMessage.append("You are not authorized to access the patient's device data. Please request consent from the patient before attempting to access the data.")
            render(view: "show", model: [patient: patient, uiConfig: uiConfig, securityContext: securityContext, errorMessage: errorMessage.toString()])
        }
        else {
            List<OMRReading> readings = idmService.getDeviceReadings(devId, uiConfig.deviceReadingDataProperties)
            int offset = 0
            if (params.offset != null) {
                offset = params.getInt('offset')
            }
            int pageSize = 5
            if (params.max != null) {
                pageSize = params.getInt('max')
            }
            params.each {param ->
                println "Parameter : $param : value $param.value"
            }
            def readingsList = this.getPage(offset, readings, pageSize)
            println "Readings list size ${readingsList.size()} Page size: ${pageSize}"
            render(view: "patientData", model: [patient: patient, patientName: patientName, devices: patient.devices, readings: readingsList, readingsCount: readings.size(), uiConfig: uiConfig, selectedDevice: device ])
        }
    }

    protected OMRDevice findSelectedDevice() {
        OMRUser loggedInUser = securityContext.loggedInUser
        String selectedDeviceSN = securityContext.selectedDeviceSN
        OMRDevice theDevice = null
        loggedInUser.devices.each { device ->
            println device.devSerial + " was found in the user's list of devices."
            println "Currently selected device SN is " + selectedDeviceSN
            if (device.devSerial.equals(selectedDeviceSN)) {
                println "Match found!"
                theDevice = device
            }
        }
        return theDevice
    }

    protected OMRDevice findSelectedDeviceFromPatient(String devId, OMRUser patient) {
        OMRDevice theDevice = null
        patient.devices.each { device ->
            println device.devSerial + " was found in the user's list of devices."
            println "Currently selected device SN is " + device.devSerial
            if (device.idmId.equals(devId)) {
                println "Match found!"
                theDevice = device
            }
        }
        return theDevice
    }

    private List getPage(int offset, List items, int pageSize) {
        def result = []
        if (items != null && items.size() > 0) {
            int startIndex = offset
            int endIndex = startIndex + pageSize - 1
            if (endIndex >= items.size()) {
                endIndex = items.size() - 1
            }
            for (index in startIndex..endIndex) {
                result << items.get(index)
            }
        }
        return result
    }
}
