import twonetendpoint.DeviceOperationsService
import twonetendpoint.NetworkConfiguration

// Place your Spring DSL code here
beans = {
    networkConfig(NetworkConfiguration) { bean ->
        openAMURL = "https://openam.aeet.fridam.aeet-forgerock.com"
        accessTokenService = "/openam/oauth2/access_token"
        registerUMAResourceSetService = "/openam/uma/resource_set"
        openIDMURL = "https://openidm.aeet.fridam.aeet-forgerock.com"
        openIDMAdminUserID = "openidm-admin"
        openIDMAdminPassword = "openidm-admin"
        deviceReadingResourceName = "TwoNetDeviceReading"
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

    devOps(DeviceOperationsService) {
        networkConfig = ref("networkConfig")
    }

}
