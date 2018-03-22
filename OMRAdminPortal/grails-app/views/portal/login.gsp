<!doctype html>
<html>
<head>
    <meta name="layout" content="home-main"/>
    <title>Welcome to the ACME MEdical Portal</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>

    <div class="row" style="width:1170px;height:125px;">
        <h1>&nbsp;</h1>
    </div>
    <div class="row" style="width:1170px;height:410px;">
        <div class="col-md-9" >
            <g:if test="${errorMessage != null}" >
                <h5>${errorMessage}</h5>
            </g:if>
            <g:else>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </g:else>
        </div>

        <div class="col-md-3" >
            <g:form url="[action:'authenticate']" >
                <div class="row" style="width: 250px; height: 35px;">
                    <div class="col-md-12">
                        <label for="userName" style="color: black">User Name</label>
                        <g:textField name="userName" />
                    </div>
                </div>
                <div class="row" style="width: 250px;height: 20px;">
                    <h3>&nbsp;&nbsp;&nbsp;</h3>
                </div>
                <div class="row" style="width: 250px; height: 35px;">
                    <div class="col-md-12">
                        <label for="password" style="color: black">Password</label>
                        <g:passwordField name="password" />
                    </div>
                </div>
                <div class="row" style="width: 250px;height: 20px;">
                    <h3>&nbsp;&nbsp;&nbsp;</h3>
                </div>
                <div class="row" style="width:250px;height: 20px;">
                    <div class="col-md-12">
                        &nbsp;&nbsp;&nbsp;
                    </div>
                </div>
                <div class="row" style="width: 250px;height: 20px;">
                    <h3>&nbsp;&nbsp;&nbsp;</h3>
                </div>
                <div class="row" style="width:250px; height:45px;" >
                    <div class="col-md-6">
                        <g:submitButton class="btn btn-block btn-sm" style="background: #007399; color: white" name="login" value="Login" />
                    </div>
                    <div class="col-md-6">
                        <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="index" >Cancel</g:link>
                    </div>
                </div>
            </g:form>
        </div>

    </div>

</body>
</html>
