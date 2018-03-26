import omradminportal.IdentityManagementService
import omradminportal.NetworkConfiguration
import omradminportal.SecurityContext
import omradminportal.TwoNetService
import omradminportal.UIConfiguration

// Place your Spring DSL code here
beans = {
    networkConfig(NetworkConfiguration) { bean ->
        openAMURL = "http(s)://<OpenAM Host>:<OpenAM Port>"
        accessTokenService = "/openam/oauth2/access_token"
        registerUMAResourceSetService = "/openam/uma/resource_set"
        openIDMURL = "http(s)://<OpenIDM Host>:<OpenIDM Port>"
        openIDMAdminUserID = "<OpenIDM Admin UserID>"
        openIDMAdminPassword = "<OpenIDM Admin Password>"
        partnerOrgResourceName = "partnerOrg"
        policyResourceName = "IoTManagementPolicy"
        bean.singleton = true
        twoNetAuthKey = "<2Net API Key>"
        accountId = "<2Net Account ID>"
        mobileAppId = "<2Net Mobile Application ID>"
        twoNetBaseURL = "https://twonet-int-gateway.qualcomm.com/demo3/cuc/rest/revY"
        umaRedirectURI = "http://aeet-apps.fridam.aeet-forgerock.com:8787/portal/activateDevice"
        umaRSClientID = "Uma-Resource-Server"
        umaRSClientSecret = "<Uma-RS-Secret>"
        umaClientID = "UmaClient"
        umaClientSecret = "<UmaClient Secret>"
        umaScopes = "openid%20uma_protection%20uma_authorization%20profile"
    }

    securityContext(SecurityContext) { bean ->

    }

    idmService(IdentityManagementService) { bean ->
        networkConfig = ref("networkConfig")
    }

    uiConfig(UIConfiguration) { bean ->
        deviceImages = ["Nonin 3230" : "nonin3230xsmall.png"]

        userDomainProperties = [
            "User Name", "First Name", "Last Name", "E-Mail", "Contact Phone"
        ]

        userFieldLabelMappings = [
            "User Name": "loginId",
            "First Name": "firstName",
            "Last Name": "lastName",
            "E-Mail": "emailAddress",
            "Contact Phone": "phoneNumber"
        ]

        deviceDomainProperties = [
            "Name", "Serial Number", "Model", "Active Status"
        ]

        deviceFieldLabelMappings = [
            "Name": "name",
            "Serial Number": "devSerial",
            "Model": "modelName",
            "Active Status": "activeStatus"
        ]

        deviceReadingsDomainProperties = [
            "SP O2", "Heart Rate", "Date Of Reading"
        ]

        deviceReadingsLabelMappings = [
            "SP O2" : "spO2",
            "Heart Rate" : "pulseRate",
            "Date Of Reading" : "dateOfReading"
        ]

        deviceReadingDataProperties = [
            "spO2", "pulseRate"
        ]

    }

    twoNetService(TwoNetService) { bean ->
        networkConfig = ref("networkConfig")

    }


}
