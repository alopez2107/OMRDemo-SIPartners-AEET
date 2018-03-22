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
        <div class="col-md-8" >
            <table>
                <thead>
                <tr>
                    <g:each in="${uiConfig.userDomainProperties}" var="p" status="i">
                        <g:sortableColumn property="${uiConfig.userFieldLabelMappings[p]}" title="${p}" />
                    </g:each>
                </tr>
                </thead>
                <tbody>
                <g:each in="${patientsList}" var="bean" status="i">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                        <g:each in="${uiConfig.userDomainProperties}" var="p" status="j">
                            <g:if test="${p=="User Name"}">
                                <td><g:link action="showPatient" id="${bean.idmId}"><f:display bean="${bean}" property="${uiConfig.userFieldLabelMappings[p]}" displayStyle="table" /></g:link></td>
                            </g:if>
                            <g:else>
                                <td><f:display bean="${bean}" property="${uiConfig.userFieldLabelMappings[p]}" displayStyle="table" /></td>
                            </g:else>
                        </g:each>
                    </tr>
                </g:each>
                </tbody>
            </table>
            <div class="pagination pagination-lg" style="height: 30px;">
                <g:paginate next="Next" prev="Prev" max="5" total="${patientListCount ?: 0}" />
            </div>
        </div>
        <div class="col-md-4" >
            <div class="row" >
                Button 1
            </div>
            <div class="row" >
                Button 2
            </div>
            <div class="row" >
                Button 3
            </div>
        </div>
    </div>

</body>
</html>
