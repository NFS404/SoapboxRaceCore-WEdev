<html>
<head></head>
<body>
	<form action="Engine.svc/PromoCode/createPromoCode" method="post">
		    Your token: <input type="text" name="promoCodeToken" /><br /><br /> 
		    Premium Type: <br />
		    <input type="radio" name="codeType" value="powerup"/> Power-Up<br />
		    <input type="radio" name="codeType" value="base"/> Premium Base<br />
		    <input type="radio" name="codeType" value="plus"/> Premium+<br />
		    <input type="radio" name="codeType" value="full"/> Premium Full<br /><br />
		    <input type="radio" name="codeType" value="moneydrop"/> Money Drop<br />
		    <input type="radio" name="codeType" value="garage50"/> Garage50+<br />
		    <input type="radio" name="codeType" value="garage150"/> Garage150+<br />
		    <input type="submit" value="Generate Code" />
	</form>
</body>
</html>