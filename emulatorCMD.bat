:g1
	echo begin
	jre/bin/java.exe -Djava.security.policy=server.policy -jar Spectral.jar --service
	set res=%errorlevel%
	echo res: %res%
	if %res%==0 exit
	echo loop
	goto :g1