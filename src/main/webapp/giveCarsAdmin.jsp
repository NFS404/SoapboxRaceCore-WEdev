<html>
<head></head>
<body>
	<form action="Engine.svc/personas/giveCarsBundle" method="post">
		Token: <input type="text" name="adminToken" /><br /> 
		Nickname: <input type="text" name="playerName" /><br /><br /> 
		<input type="submit" value="Proceed" />
	</form>
	<form action="Engine.svc/personas/forceCheckObsoleteRecords" method="post">
		Force-check the obsolete player records:<br />
		Token: <input type="text" name="adminToken" /><br /> 
		<br /> 
		<input type="submit" value="Proceed" />
	</form>
</body>
</html>