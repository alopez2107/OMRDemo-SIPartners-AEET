<!doctype html>
<html>
<head>
    <meta name="layout" content="feature-main"/>
    <title>Welcome to the ACME Medical Portal</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>

    <div class="row" style="width:1170px;height:160px;">
        <div class="col-sm-10">
            &nbsp;&nbsp;
        </div>
        <div class="col-sm-2" >
            <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="logout">Logout</g:link>
        </div>
    </div>
    <div class="row" style="width:1170px;height:180px;">
        <div class="col-md-3" >
            <g:link action="myPatients" ><asset:image src="myPatientsBtnTile.png" /></g:link>
        </div>
        <div class="col-sm-1" >
            <h2>&nbsp;&nbsp;</h2>
        </div>
        <div class="col-md-3" >
            <g:link action="myStaff" ><asset:image src="myStaffBtnTile.png" /></g:link>
        </div>
        <div class="col-sm-1" >
            <h2>&nbsp;&nbsp;</h2>
        </div>
        <div class="col-md-3" >
            <g:link action="myEquipment" ><asset:image src="myEquipmentBtnTile.png" /></g:link>
        </div>
    </div>
    <div class="row" style="height: 60px; width: 1170px;">
        <h3>&nbsp;&nbsp;</h3>
    </div>
    <div class="row" style="width:1170px;height:210px;">
        <div class="col-md-3" >
            <g:link action="mySchedule" ><asset:image src="myScheduleBtnTile.png" /></g:link>
        </div>
        <div class="col-sm-1" >
            <h2>&nbsp;&nbsp;</h2>
        </div>
        <div class="col-md-3" >
            <h4>&nbsp;&nbsp;</h4>
        </div>
        <div class="col-sm-1" >
            <h2>&nbsp;&nbsp;</h2>
        </div>
        <div class="col-md-3" >
            <g:link action="myOMRDevices" ><asset:image src="omrDevicesBtnTile.png" /></g:link>
        </div>
    </div>
</body>
</html>
