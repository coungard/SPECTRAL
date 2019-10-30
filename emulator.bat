cd C:\terminal4
:g1
	start cmd /k Call bot/starterPy.bat
	echo begin
	:: java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar Spectral.jar
	jre\bin\java.exe -jar Spectral.jar
	set res=%errorlevel%
	echo res: %res%
	if %res%==0 exit
	echo loop
	goto :g1