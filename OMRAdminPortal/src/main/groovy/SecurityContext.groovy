package omradminportal

class SecurityContext {
    OMRUser loggedInUser
    List<String> roles
    String ssoCookie
    String selectedDeviceSN


    boolean isAdmin() {
        return roles?.contains("omrAdmin")
    }
}
