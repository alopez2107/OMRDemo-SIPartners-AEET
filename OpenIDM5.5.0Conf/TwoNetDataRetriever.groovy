return openidm.query("managed/user", ["_queryFilter" : request.queryFilter.toString() ], ["authCode2Net", "my2netDevices/*/*"])
