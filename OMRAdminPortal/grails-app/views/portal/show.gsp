<!doctype html>
<html>
<head>
    <meta name="layout" content="feature-main"/>
    <title>Welcome to the ACME Medical Portal</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>

    <div class="row" style="width:1170px;height:185px;">
        <div class="col-sm-10">
            &nbsp;&nbsp;
        </div>
        <div class="col-sm-2" >
            <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="logout">Logout</g:link>
        </div>
    </div>
    <g:if test="${errorMessage != null}" >
        <div class="row" style="font: x-small;color: white; background: #007399">
            ${errorMessage}
        </div>
    </g:if>
    <div class="row" style="width:1170px;height: 300px;">
        <div class="col-lg-5" >
            <g:each in="${uiConfig.userDomainProperties}" var="p" status="i">
                <div class="row" style="height: 35px;">
                    <div class="col-md-6" >
                        <label for="${uiConfig.userFieldLabelMappings[p]}" style="color:black">${p}</label>
                    </div>
                    <div class="col-md-6" >
                        <h5 style="color: #007399"> <g:fieldValue bean="${patient}" field="${uiConfig.userFieldLabelMappings[p]}"/></h5>
                    </div>
                </div>
            </g:each>
        </div>
        <div class="col-lg-7" >
            <g:each in="${patient.devices?}" var="device" status="i" >
                <div class="row" >
                    <div class="col-md-4" >
                        <asset:image src="${uiConfig.deviceImages[device.modelName]}" />
                    </div>
                    <div class="col-md-8" >
                        <g:each in="${uiConfig.deviceDomainProperties}" var="dp" status="di">
                            <div class="row" style="height: 35px;">
                                <div class="col-md-6" >
                                    <label for="${uiConfig.deviceFieldLabelMappings[dp]}" style="color:black">${dp}</label>
                                </div>
                                <div class="col-md-6" >
                                    <g:if test="${dp=="Active Status"}" >
                                        <g:if test="${device.activeStatus == false}" >
                                            <asset:image src="statusInnactive.png" />
                                        </g:if>
                                        <g:else>
                                            <asset:image src="statusActive.png" />
                                        </g:else>
                                    </g:if>
                                    <g:else>
                                        <h5 style="color: #007399"> <g:fieldValue bean="${device}" field="${uiConfig.deviceFieldLabelMappings[dp]}"/></h5>
                                    </g:else>
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
            </g:each>
        </div>
    </div>
    <div class="row" style="width: 1170px;" >
        <div class="col-lg-3" >
            <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="myPatients">Back to Patients</g:link>
        </div>
        <div class="col-lg-3" >
            <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="patientData" params="[id: patient.idmId]">Patient Data</g:link>
        </div>
        <div class="col-lg-3" >
            <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="devicePrescriptions" params="[patient: patient]">Prescribe Device</g:link>
        </div>
        <div class="col-lg-3" >
            <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="appointmentHistory">Appointment History</g:link>
        </div>
    </div>
</body>
</html>
