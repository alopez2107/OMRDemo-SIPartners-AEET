package omradminportal

class OMRUser {
    String idmId
    String loginId
    String password
    String firstName
    String lastName
    String emailAddress
    String phoneNumber
    String authCode
    String medicalAssignee
    List<String> roles
    List<OMRDevice> devices
    String umaAccessToken

    static constraints = {
    }
}
