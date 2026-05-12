<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
<h2>支付订金</h2>
<form action="payDeposit" method="post">
    <input type="hidden" name="applyId" value="<%=request.getParameter("applyId")%>">
    <button type="submit">确认支付</button>
</form>
</body>
</html>