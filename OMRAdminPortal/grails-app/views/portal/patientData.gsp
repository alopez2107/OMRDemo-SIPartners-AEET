<!doctype html>
<html>
<head>
    <meta name="layout" content="feature-main"/>
    <title>Welcome to the ACME MEdical Portal</title>
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
    <div class="row" style="width: 1170px; height: 350px;" >
        <div class="col-md-6" >
            <table>
                <thead>
                <tr>
                    <g:each in="${uiConfig.deviceDomainProperties}" var="p" status="i">
                        <g:sortableColumn property="${uiConfig.deviceFieldLabelMappings[p]}" title="${p}" />
                    </g:each>
                </tr>
                </thead>
                <tbody>
                <g:each in="${patient.devices?}" var="bean" status="i">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                        <g:each in="${uiConfig.deviceDomainProperties}" var="p" status="j">
                            <g:if test="${p=="Name"}">
                                <td><g:link action="showDevReadings" id="${bean.idmId}" params="[patientId: patient.idmId]"><f:display bean="${bean}" property="${uiConfig.deviceFieldLabelMappings[p]}" displayStyle="table" /></g:link></td>
                            </g:if>
                            <g:else>
                                <td><f:display bean="${bean}" property="${uiConfig.deviceFieldLabelMappings[p]}" displayStyle="table" /></td>
                            </g:else>
                        </g:each>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
        <g:if test="${selectedDevice != null}" >
            <div class="col-md-6" >
                <table>
                    <thead>
                    <tr>
                        <g:each in="${uiConfig.deviceReadingsDomainProperties}" var="p" status="i">
                            <g:sortableColumn property="${uiConfig.deviceReadingsLabelMappings[p]}" title="${p}" />
                        </g:each>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${readings}" var="bean" status="i">
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                            <g:each in="${uiConfig.deviceReadingsDomainProperties}" var="p" status="j">
                                <g:if test="${p != 'Date Of Reading'}" >
                                    <td style="font: x-small; color: black;">${bean.devReading[uiConfig.deviceReadingsLabelMappings[p]]}</td>
                                </g:if>
                                <g:else>
                                    <td><f:display bean="${bean}" property="${uiConfig.deviceReadingsLabelMappings[p]}" displayStyle="table" /></td>
                                </g:else>
                            </g:each>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <div class="pagination" style="height: 40px;">
                    <g:paginate max="5" total="${readingsCount ?: 0}" params="[patientId: patient.idmId, id: selectedDevice.idmId]"/>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="col-md-6" style="font:x-small;color: black">
                Select a device to Display its Readings.
            </div>
        </g:else>
    </div>
    <div class="row" style="height: 30px; width: 1170px;" >
        <g:link class="btn btn-block btn-sm" style="background: #007399; color: white" action="showPatient" id="${patient.idmId}">Back to ${patient.firstName} ${patient.lastName}</g:link>
    </div>
</body>
</html>
