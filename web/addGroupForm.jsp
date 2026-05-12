<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>添加旅游团</title>
    <style>
        form { width: 400px; margin: 50px auto; }
        div { margin: 10px 0; }
        label { display: inline-block; width: 120px; }
        input { padding: 5px; width: 200px; }
        button { padding: 8px 20px; background: #4CAF50; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <h2 style="text-align: center;">添加旅游团</h2>
    <form action="${pageContext.request.contextPath}/addGroup" method="post">
        <div>
            <label>旅游团代码：</label>
            <input type="text" name="groupCode" required placeholder="如 TG004">
        </div>
        <div>
            <label>路线名称：</label>
            <input type="text" name="routeName" required>
        </div>
        <div>
            <label>出发日期：</label>
            <input type="date" name="startDate" required>
        </div>
        <div>
            <label>结束日期：</label>
            <input type="date" name="endDate" required>
        </div>
        <div>
            <label>人数上限：</label>
            <input type="number" name="maxPeople" min="1" required>
        </div>
        <div>
            <label>成人价格：</label>
            <input type="number" step="0.01" name="adultPrice" required>
        </div>
        <div>
            <label>儿童价格：</label>
            <input type="number" step="0.01" name="childPrice" required>
        </div>
        <div style="text-align: center;">
            <button type="submit">添加旅游团</button>
        </div>
    </form>
</body>
</html>