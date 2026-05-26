package com.example.servlet;

import com.example.utils.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/addGroup")
public class AddTourGroupServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 获取表单参数
        String groupCode = request.getParameter("groupCode");
        String routeName = request.getParameter("routeName");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        int maxPeople = Integer.parseInt(request.getParameter("maxPeople"));
        double adultPrice = Double.parseDouble(request.getParameter("adultPrice"));
        double childPrice = Double.parseDouble(request.getParameter("childPrice"));

        try {
            Connection conn = DBUtil.getConnection();
            String sql = "INSERT INTO tour_group(group_code, route_name, start_date, end_date, max_people, adult_price, child_price, status) VALUES(?,?,?,?,?,?,?,1)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupCode);
            pstmt.setString(2, routeName);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            pstmt.setInt(5, maxPeople);
            pstmt.setDouble(6, adultPrice);
            pstmt.setDouble(7, childPrice);
            pstmt.executeUpdate();

            out.write("<h3 style='color:green'>✅ 旅游团添加成功！</h3>");
            conn.close();
        } catch (Exception e) {
            out.write("<h3 style='color:red'>❌ 添加失败：" + e.getMessage() + "</h3>");
        }
    }
}