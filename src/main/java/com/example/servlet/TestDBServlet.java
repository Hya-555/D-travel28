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
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/testDB")
public class TestDBServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tour_group")) {

            out.write("<h2>数据库连接成功！</h2>");
            out.write("<h3>旅游团列表：</h3>");
            while (rs.next()) {
                out.write("<p>" + rs.getString("group_code") + " — " + rs.getString("route_name") + "</p>");
            }
        } catch (Exception e) {
            out.write("<h2>数据库连接失败！</h2>");
            out.write("错误信息：" + e.getMessage());
            e.printStackTrace();
        }
    }
}