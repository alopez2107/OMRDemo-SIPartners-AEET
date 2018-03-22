<!doctype html>
<html>
<head>
    <meta name="layout" content="home-main"/>
    <title>Welcome to the ACME MEdical Portal</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>

    <div class="row" style="width:1170px;height:225px;">
        <h1>&nbsp;</h1>
    </div>
    <div class="row" style="width:1170px;height:410px;">
        <div class="col-md-3" >
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </div>
        <div class="col-md-6" >
            <div class="row" style="width:450px; height:245px;" >
                <div class="col-md-12">
                    <p style="background: #31708f; color: #E9E9E9">&nbsp;&nbsp;Dear ${securityContext.loggedInUser.firstName} ${securityContext.loggedInUser.lastName}:

                    <p style="background: #31708f; color: #E9E9E9">&nbsp;&nbsp;By activating the device - ${securityContext.selectedDeviceSN} - you agree to share &nbsp;&nbsp;your device readings with
                    the following agents:</p>

                    <p style="background: #31708f; color: #E9E9E9">&nbsp;&nbsp;${securityContext.loggedInUser.medicalAssignee}</p>

                    <p style="background: #31708f; color: #E9E9E9">&nbsp;&nbsp;Please select the option below to Allow or Deny your &nbsp;&nbsp;consent.</p>
                </div>
            </div>
            <div class="row" style="width:450px; height:45px;" >
                <div class="col-md-6">
                    <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="authorize" params="[decision: true]" >Allow</g:link>
                </div>
                <div class="col-md-6">
                    <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="devices" params="[decision: false]">Deny</g:link>
                </div>
            </div>
        </div>
        <div class="col-md-3" >
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </div>
    </div>

</body>
</html>
