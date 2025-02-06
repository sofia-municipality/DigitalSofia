<%
  if not "user_name" in data: 
    return STOP_RENDERING

  if not "payment_status" in data: 
    return STOP_RENDERING

%>

<html>
  <head>
    <title>Digital Sofia – плащане на МДТ</title>
    <style>
        body {
          margin: 10;
          font-family: Roboto,"Helvetica Neue",sans-serif;
          display: flex;
          flex-direction: column;
        }
				header {
					padding: 20;
					height: 10vh;
					display: flex;
					color: white;
					background: linear-gradient(to bottom,#014f86,#468faf 70%,#fff 68%);
				}
        .desclaimer{
          font-style: italic;
          font-size: small;   
        }
    </style>
  </head>
  <body>
    <header>
      <h3>Digital Sofia – Грешка при нотифициране на Матеус</h3>
    </header>
    <section>
      <p>Здравейте,</p>
			<p>Получи се грешка при опит за нотифициране на Матеус за състоянието на плащане. Получена е следната информация:</p>
			<code>
				Грешка: ${data["error"]}
			</code>
			<p>Подробна инфомация:</p>
			<ul>
				<li>user_email: ${data["user_email"]}</li>
				<li>user_name:  ${data["user_name"]}</li>
				<li>person_identifier: ${data["person_identifier"]}</li>
				<li>payment_status: ${data["payment_status"]}</li>
				<li>debug_info: ${data["debug_info"]}</li>
			</ul>
			
    </section>
    <footer>
      <h4>Digital Sofia</h4>
    </footer>
  </body>
</html>

