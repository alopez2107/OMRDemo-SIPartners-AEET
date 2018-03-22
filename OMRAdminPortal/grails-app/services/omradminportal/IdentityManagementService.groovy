package omradminportal

import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Transactional
class IdentityManagementService {

    def networkConfig

    public IdentityManagementService() {
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

    def getAccessTokensForUMA(String loginID, String password) {
        def connection = new URL(networkConfig.openAMURL + networkConfig.accessTokenService + "?grant_type=password&username=" + loginID + "&password=" + password + "&scope=uma_protection&client_id=" + networkConfig.umaRSClientID + "&client_secret=" + networkConfig.umaRSClientSecret)
        def accessTokenResp = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            outputStream.withWriter {writer ->
                writer << "{}"
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        return accessTokenResp.access_token
    }

    UMAResourceSet registerUMAResourceSet(String loginID, String accessToken, String devSerial) {
        def connection = new URL(networkConfig.openAMURL + networkConfig.registerUMAResourceSetService)
        def req = createUMARegisterResourceSetRequest(loginID, devSerial)
        def registerUMAResourceSet = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer " + accessToken)
            outputStream.withWriter {writer ->
                writer << req
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        UMAResourceSet rs = new UMAResourceSet()
        rs.id = registerUMAResourceSet._id
        rs.userAccessPolicyURI = registerUMAResourceSet.user_access_policy_uri
        return rs
    }

    def createUserAccessPolicy(String ssoCookie, String policyId, String resourceOwnerID, String requetPartyID) {
        def connection = new URL(networkConfig.openAMURL + "/openam/json/users/${resourceOwnerID}/uma/policies/${policyId}")
        def req = createUMAAccessPolicyRequest(requetPartyID, policyId)
        def createAccessPolicy = connection.openConnection().with {
            setRequestMethod("PUT")
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("iPlanetDirectoryPro", ssoCookie)
            outputStream.withWriter {writer ->
                writer << req
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println createAccessPolicy
        return createAccessPolicy._id
    }

    def authenticate(String userName, String password) {
        def connection = new URL(this.networkConfig.openAMURL + "/openam/json/realms/root/authenticate")
        def authResp = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-OpenAM-Username", userName)
            setRequestProperty("X-OpenAM-Password", password)
            outputStream.withWriter {writer ->
                writer << '{}'
            }
            if (responseCode == 401) {
                println "Authentication Failed"
                return ["INVALID", null]
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println authResp

        OMRUser loggedInUser = this.find(userName)
        loggedInUser.devices.each {device ->
            if (device.umaROCredential == null || (device.umaROCredential != null && device.umaROCredential == '')) {
                loggedInUser = setUMAROCredentials(loggedInUser, device.idmId, password)
            }
        }

        loggedInUser.umaAccessToken = this.getAccessTokensForUMA(userName, password)
        println "Access Token for " + userName + " is " + loggedInUser.umaAccessToken
        return [authResp.tokenId, loggedInUser]
    }

    def logout(String ssoCookie) {
        def connection = new URL(this.networkConfig.openAMURL + "/openam/json/realms/root/sessions/?_action=logout")
        def authResp = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Cache-Control", "no-cache")
            setRequestProperty("iplanetDirectoryPro", ssoCookie)
            outputStream.withWriter {writer ->
                writer << '{}'
            }
            if (responseCode == 401) {
                println "Authentication Failed"
                return new JsonSlurper().parseText(authnFailureOutput)
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println authResp.result
    }

    OMRUser find(String id) {
        println "Looking for Id: ${id}"
        def connection = new URL(this.networkConfig.openIDMURL + "/openidm/managed/user?_queryFilter=userName" +
                URLEncoder.encode(" eq ", 'UTF-8') + "'" + id + "'&_fields=*,roles/*/name,my2netDevices/*/*,medicalAssignee/*/userName")
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
        OMRUser omrUser = this.createOMRUser(queryResp.result[0])
        return omrUser
    }

    OMRUser getUserByID(String id) {
        println "Looking for Id: ${id}"
        def connection = new URL(this.networkConfig.openIDMURL + "/openidm/managed/user/" + id + "?_fields=*,roles/*/name,my2netDevices/*/*")
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
        OMRUser user = createOMRUser(queryResp)
        return user
    }

    String findUserNameByID(String id) {
        println "Looking for Id: ${id}"
        def connection = new URL(this.networkConfig.openIDMURL + "/openidm/" + id)
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
        return queryResp.userName
    }

    OMRUser update2NetInfo(String umaResourceID, String loginId, String idmId, String vHubId, String devIdmId, String authCode, boolean activationStatus) {
        def updAuthCode = createSetAuthCodeRequest(authCode)
        def activationStatusReq = createUpdateActivationStatus(vHubId, activationStatus, umaResourceID)
        def rest = new RESTClient(networkConfig.openIDMURL)
        rest.ignoreSSLIssues()
        rest.headers = ['X-OpenIDM-Username':networkConfig.openIDMAdminUserID, 'X-OpenIDM-Password':networkConfig.openIDMAdminPassword]

        def response = rest.patch(
                contentType : ContentType.JSON,
                path : "/openidm/managed/user/" + idmId,
                body : updAuthCode
        )

        println response.getData()

        def devResponse = rest.patch(
                contentType : ContentType.JSON,
                path : "/openidm/managed/TwoNetDevice/" + devIdmId,
                body : activationStatusReq
        )

        println devResponse
        return find(loginId)
    }

    private OMRUser createOMRUser(response) {
        OMRUser user = new OMRUser()
        println response
        user.idmId = response._id
        user.loginId = response.userName
        user.firstName = response.givenName
        user.lastName = response.sn
        user.emailAddress = response.mail
        user.phoneNumber = response.telephoneNumber
        List<String> roles = new ArrayList<String>()
        response.roles.each { role ->
            roles.add(role.name)
        }
        user.roles = roles
        List<OMRDevice> devices = new ArrayList<OMRDevice>()
        response.my2netDevices?.each { device ->
            println "Device found " + device
            OMRDevice dev = new OMRDevice()
            dev.name = device.name
            dev.modelName = device.model
            dev.devSerial = device.serialNumber
            dev.macAddress = device.macAddress
            dev.activeStatus = device.activeStatus
            if (dev.activeStatus) {
                dev.vHubId = device.vHubID
                user.authCode = response.authCode2Net
            }
            dev.idmId = device._id
            dev.umaResourceID = device.umaResourceID
            dev.umaROCredential = device.umaROCredential
            devices.add(dev)
        }
        user.devices = devices
        if (response.medicalAssignee != null) {
            // Medical assignee
            String medicalAssigneeUserName = this.findUserNameByID(response.medicalAssignee?._ref)
            println "Medical assignee is " + medicalAssigneeUserName
            user.medicalAssignee = medicalAssigneeUserName
        }
        return user
    }

    List<OMRUser> getMyPatients(String idmId) {
        List<OMRUser> patients = []
        String medAssignee = "managed/user/" + idmId
        // https://openidm.aeet.fridam.aeet-forgerock.com/openidm/managed/user?_queryId=query-all&_fields=*,medicalAssignee/*/*
        def connection = new URL(this.networkConfig.openIDMURL + "/openidm/managed/user?_queryId=query-all&_fields=*,medicalAssignee/*/*")
        def queryResp = connection.openConnection().with {
            setRequestMethod("GET")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-OpenIDM-Username", this.networkConfig.openIDMAdminUserID)
            setRequestProperty("X-OpenIDM-Password", this.networkConfig.openIDMAdminPassword)
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        queryResp.result.each { user ->
            println "Patient " + user.userName + " found!"
            if (user.medicalAssignee?._ref == medAssignee) {
               println "Match Found " + user.userName + " is a patient of yours"
               patients << createOMRUser(user)
            }
        }
        return patients
    }


    // UMA -- Grant flow methods --  ***********
    // First Step: Obtain a PAT from the Resource Owner which will be passed to the getPermisionRequestTicket method below.

    def getPermisionRequestTicket(String accessToken, String resourceId) {
        println "Getting PermissionRequestTicket for ${accessToken} ${resourceId}"
        def umaPermissionReq = this.createUMAPermissionRequestTicketReq(resourceId)
        def connection = new URL(this.networkConfig.openAMURL + "/openam/uma/permission_request")
        def permTicketResp = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer " + accessToken)
            outputStream.withWriter {writer ->
                writer << umaPermissionReq
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println "Response: \n" + permTicketResp
        println "Permission Ticket " + permTicketResp.ticket
        return permTicketResp.ticket
    }

    def getUMAClaimToken(String loginID, String password) {
        def connection = new URL(networkConfig.openAMURL + networkConfig.accessTokenService + "?grant_type=password&username=" + loginID + "&password=" + password + "&scope=openid")
        def authorizationHeader = networkConfig.umaClientID + ":" + networkConfig.umaClientSecret
        def umaClaimTokenResp = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Authorization", "Basic " + authorizationHeader.encodeAsBase64())
            outputStream.withWriter {writer ->
                writer << "{}"
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println "UMA Claim Token response: \n" + umaClaimTokenResp
        if (umaClaimTokenResp.id_token == null) {
            // Process error
        }
        return umaClaimTokenResp.id_token
    }

    def getUMARequestingPartyAccessToken(String permissionTicket, String claimsToken) {
        String granType = "urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Auma-ticket"
        String claim_token_format = "http%3A%2F%2Fopenid.net%2Fspecs%2Fopenid-connect-core-1_0.html%23IDToken"

        def connection = new URL(networkConfig.openAMURL + networkConfig.accessTokenService + "?grant_type=" + granType + "&ticket=" + permissionTicket + "&claim_token=" + claimsToken + "&claim_token_format=" + claim_token_format + "&scope=receive")
        def authorizationHeader = networkConfig.umaClientID + ":" + networkConfig.umaClientSecret
        def umaRPTokenResp = connection.openConnection().with {
            setRequestMethod("POST")
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Authorization", "Basic " + authorizationHeader.encodeAsBase64())
            outputStream.withWriter {writer ->
                writer << "{}"
            }
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println "UMA Claim Token response: \n" + umaRPTokenResp
        if (umaRPTokenResp.access_token == null) {
            // Process access denied
            return "UNAUTHORIZED"
        }
        return umaRPTokenResp.access_token
    }


    // End -- UMA Grant Flow methods -- *************

    // Devices Service Methods

    List<OMRReading> getDeviceReadings(String devIdmId, List<String> devReadingProperties) {
        println "Looking for Device with Id: ${devIdmId}"
        def connection = new URL(this.networkConfig.openIDMURL + "/openidm/managed/TwoNetDevice/" + devIdmId + "?_fields=deviceReadings/*/*")
        def queryResp = connection.openConnection().with {
            setRequestMethod("GET")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-OpenIDM-Username", this.networkConfig.openIDMAdminUserID)
            setRequestProperty("X-OpenIDM-Password", this.networkConfig.openIDMAdminPassword)
            inputStream.withCloseable { inStream ->
                new JsonSlurper().parse(inStream as InputStream)
            }
        }
        println "Readings response: \n" + queryResp
        List<OMRReading> readings = []
        queryResp.deviceReadings.each {readingItem ->
            OMRReading reading = new OMRReading()
            JsonSlurper slurper = new JsonSlurper()
            def data = slurper.parseText(readingItem.readingData)
            reading.devReading = [:]
            devReadingProperties.each { prop ->
                reading.devReading.put(prop, data[prop])
            }
            readings << reading
            reading.dateOfReading = readingItem.hubReceiveTime
        }
        return readings
    }


    // -- End Devices Service Methods section -- *************

    protected String createSetAuthCodeRequest(String authCode) {
        def operations = [[operation: 'replace', field: '/authCode2Net', value: authCode]]
        JsonBuilder builder = new JsonBuilder(operations)
        String payload = builder.toPrettyString()
        println payload
        return payload
    }

    protected String createUpdateActivationStatus(String vHubId, boolean activationStatus, String umaResourceID) {
        def operations = [[operation: 'replace', field: '/activeStatus', value: activationStatus],
                          [operation: 'replace', field: '/vHubID', value: vHubId],
                          [operation: 'replace', field: '/umaResourceID', value: umaResourceID]]
        JsonBuilder builder = new JsonBuilder(operations)
        String payload = builder.toPrettyString()
        println payload
        return payload
    }

    protected String createSetUMAROCredentialsReq(String umaROCredential) {
        def operations = [[operation: 'replace', field: '/umaROCredential', value: umaROCredential]]
        JsonBuilder builder = new JsonBuilder(operations)
        String payload = builder.toPrettyString()
        println payload
        return payload
    }

    protected String createUMARegisterResourceSetRequest(String loginID, String devSerial) {
        List<String> scopes = ["receive"]
        String resourceName = "OMRUMAResource" + loginID + devSerial
        String resourceType = "omrDevice"
        String resourceUri = "http://aeet-apps.fridam.aeet-forgerock.com/"+ loginID + "/" + devSerial
        JsonBuilder jsonBuilder = new JsonBuilder()

        jsonBuilder {
            resource_scopes scopes
            name resourceName
            type resourceType
            uri resourceUri
        }
        println jsonBuilder.toPrettyString()
        return jsonBuilder.toPrettyString()
    }

    protected String createUMAAccessPolicyRequest(String requestingParty, String policyID) {
        JsonBuilder builder = new JsonBuilder()
        println "Requesting Party " + requestingParty
        println "Policy ID " + policyID
        List permissionsMap = [[subject: requestingParty, scopes: ["receive"]]]
        builder {
            policyId policyID
            permissions permissionsMap
        }
        println builder.toPrettyString()
        return builder.toPrettyString()
    }

    protected String createUMAPermissionRequestTicketReq(String resourceId) {
        List resources = [[resource_id: resourceId, resource_scopes: ["receive"]]]
        JsonBuilder builder = new JsonBuilder(resources)
        println builder.toPrettyString()
        return builder.toPrettyString()
    }

    protected OMRUser setUMAROCredentials(OMRUser user, String devIdmId, String credential) {
        def updUMAROCred = createSetUMAROCredentialsReq(credential)
        def rest = new RESTClient(networkConfig.openIDMURL)
        rest.ignoreSSLIssues()
        rest.headers = ['X-OpenIDM-Username':networkConfig.openIDMAdminUserID, 'X-OpenIDM-Password':networkConfig.openIDMAdminPassword]

        def devResponse = rest.patch(
                contentType : ContentType.JSON,
                path : "/openidm/managed/TwoNetDevice/" + devIdmId,
                body : updUMAROCred
        )

        println devResponse
        return find(user.loginId)
    }
}
