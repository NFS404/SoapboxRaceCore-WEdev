<html>
<head></head>
<body>
	<form action="Engine.svc/Teams/teamCreate" method="post">
		    Your token: <input type="text" name="teamModerationToken" /><br /><br /> 
			<i>New team data:</i><br /> 
			Team Name: <input type="text" name="teamName" /><br />
			Team Leader nickname: <input type="text" name="leaderName" /><br />
			Open-Entry team?<br />
			<input type="radio" name="openEntry" value="true"/> Yes (everybody can join)<br />
		    <input type="radio" name="openEntry" value="false"/> No (invite-based)<br />
			<input type="submit" value="Create!" /><br /><br />
			<i>Note: if team is invite-based, Team Leader must find his new crew and send the invites to them.</i><br /><br />
			<i>World Evolved v2 Teams Test</i>
	</form>
</body>
</html>