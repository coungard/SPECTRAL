:g1
	echo begin
	java -jar Spectral.jar --service
	set res=%errorlevel%
	echo res: %res%
	if %res%==0 exit
	echo loop
	goto :g1