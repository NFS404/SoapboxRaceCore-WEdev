<html>
<head></head>
<body>
	<form action="Engine.svc/PromoCode/useDebug" method="post">
		Token: <input type="text" name="adminToken" /><br /> Nickname: <input
			type="text" name="nickname" /><br /><br /> 
			Premium Type: <br />
		    <input type="radio" name="premiumType" value="powerup"/> Power-Up<br />
		    <input type="radio" name="premiumType" value="base"/> Premium Base<br />
		    <input type="radio" name="premiumType" value="plus"/> Premium+<br />
		    <input type="radio" name="premiumType" value="full"/> Premium Full<br />
		    <input type="radio" name="premiumType" value="unlim"/> Premium Full+<br /><br />
		    Extra Money: <br/><input type="text" name="extraMoney" /><br/>
		    Premium Expiration date (begins from): <br/>
		    Year: <input type="text" name="timeYear" /><br/>
		    Month: <input type="text" name="timeMonth" /><br/>
		    Day: <input type="text" name="timeDay" /><br/>
			<input type="submit" value="Activate" />
	</form>
</body>
</html>