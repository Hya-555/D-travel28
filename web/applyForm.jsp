<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>旅游申请报名</title>
    <style>
        form { width: 400px; margin: 50px auto; }
        div { margin: 15px 0; }
        label { display: inline-block; width: 100px; }
        input { padding: 5px; width: 200px; }
        button { padding: 8px 20px; background: #4CAF50; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <h2 style="text-align: center;">旅游申请报名</h2>
    <form action="${pageContext.request.contextPath}/applyTour" method="post">
        <div>
            <label>旅游团代码：</label>
            <input type="text" name="groupCode" required placeholder="如 TG001">
        </div>
        <div>
            <label>申请人姓名：</label>
            <input type="text" name="name" required>
        </div>
        <div>
            <label>联系电话：</label>
            <input type="tel" name="phone" required>
        </div>
        <div>
            <label>成人数量：</label>
            <input type="number" name="adultNum" min="1" required>
        </div>
        <div>
            <label>儿童数量：</label>
            <input type="number" name="childNum" min="0" required>
        </div>
        <div style="text-align: center;">
            <button type="submit">提交申请</button>
        </div>
    </form>
</body>
</html>