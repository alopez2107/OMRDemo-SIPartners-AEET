import omradminportal.IdentityManagementService
import omradminportal.NetworkConfiguration
import omradminportal.SecurityContext
import omradminportal.TwoNetService
import omradminportal.UIConfiguration

// Place your Spring DSL code here
beans = {
    networkConfig(NetworkConfiguration) { bean ->
        openAMURL = "https://openam.aeet.fridam.aeet-forgerock.com"
        accessTokenService = "/openam/oauth2/access_token"
        registerUMAResourceSetService = "/openam/uma/resource_set"
        openIDMURL = "https://openidm.aeet.fridam.aeet-forgerock.com"
        openIDMAdminUserID = "openidm-admin"
        openIDMAdminPassword = "openidm-admin"
        partnerOrgResourceName = "partnerOrg"
        policyResourceName = "IoTManagementPolicy"
        iecOpenAMURL = "http://myiot-am.forgerocklabs.net:8080"
        iecROUserID = "ahall"
        iecROUserPassword = "Passw0rd"
        iecClientID = "imb"
        iecClientSecret = "password2"
        bean.singleton = true
        twoNetAuthKey = "34UJ76KJ"
        accountId = "FORGE001"
        mobileAppId = "FORGE00120180216145352"
        twoNetBaseURL = "https://twonet-int-gateway.qualcomm.com/demo3/cuc/rest/revY"
        umaRedirectURI = "http://aeet-apps.fridam.aeet-forgerock.com:8787/portal/activateDevice"
        umaRSClientID = "Uma-Resource-Server"
        umaRSClientSecret = "password"
        umaClientID = "UmaClient"
        umaClientSecret = "Ilohcbe2107"
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
