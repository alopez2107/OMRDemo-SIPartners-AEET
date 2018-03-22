<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>
        <g:layoutTitle default="AEET Portal"/>
    </title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <asset:stylesheet src="application.css"/>
    <style type="text/css" >

    .container {
        background-image: url(${resource(dir: 'images', file: 'portal-background.png')});
        background-size: contain;
        background-repeat: no-repeat;
        width: 1170px;
        height: 840px;
    }
    </style>
    <g:layoutHead/>

</head>
<body>

<div class="container" >
    <g:layoutBody/>
</div>

<asset:javascript src="application.js"/>

</body>
</html>
