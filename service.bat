:g1
	echo begin
	jre\bin\java.exe -Djava.security.policy=client.policy -jar RmiClient.jar
	set res=%errorlevel%
	echo res: %res%
	if %res%==0 exit
	echo loop
	goto :g1
