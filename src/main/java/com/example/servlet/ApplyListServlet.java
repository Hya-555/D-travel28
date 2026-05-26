package com.example.servlet;

import com.example.utils.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/applyList")
public class ApplyListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            Connection conn = DBUtil.getConnection();
            String sql = "SELECT t.id,u.full_name,g.group_code,"
                    + "t.total_amount,t.deposit_amount,t.deposit_paid,t.status "
                    + "FROM tour_application t "
                    + "JOIN user_applicant u ON t.applicant_id=u.id "
                    + "JOIN tour_group g ON t.group_id=g.id";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            out.println("<h2>📋 旅游申请列表</h2>");
            out.println("<table border='1' width='800'>");
            out.println("<tr><th>ID</th><th>姓名</th><th>团号</th><th>总金额</th><th>订金</th><th>状态</th><th>操作</th></tr>");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("full_name");
                String code = rs.getString("group_code");
                double total = rs.getDouble("total_amount");
                double deposit = rs.getDouble("deposit_amount");
                int paid = rs.getInt("deposit_paid");
                int status = rs.getInt("status");

                String statusStr = switch (status) {
                    case 1 -> "申请中";
                    case 2 -> "已付订金";
                    case 3 -> "已完成";
                    case 4 -> "已取消";
                    default -> "未知";
                };

                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + name + "</td>");
                out.println("<td>" + code + "</td>");
                out.println("<td>" + total + "</td>");
                out.println("<td>" + deposit + "</td>");
                out.println("<td>" + statusStr + "</td>");
                out.println("<td>");
                if (paid == 0) {
                    out.println("<a href='pay.jsp?applyId=" + id + "'>支付订金</a> | ");
                }
                out.println("<a href='cancelApply?applyId=" + id + "'>取消</a>");
                out.println("</td>");
                out.println("</tr>");
            }
            out.println("</table>");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}