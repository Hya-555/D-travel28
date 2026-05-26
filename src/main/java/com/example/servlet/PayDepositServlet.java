package com.example.servlet;

import com.example.utils.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/payDeposit")
public class PayDepositServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            String applyId = request.getParameter("applyId");

            Connection conn = DBUtil.getConnection();
            String sql = "UPDATE tour_application SET deposit_paid=1 WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, applyId);
            pstmt.executeUpdate();

            // 插入支付记录
            String paySql = "INSERT INTO payment_record(application_id,pay_type,pay_amount) "
                    + "SELECT id,'订金',deposit_amount FROM tour_application WHERE id=?";
            pstmt = conn.prepareStatement(paySql);
            pstmt.setString(1, applyId);
            pstmt.executeUpdate();

            out.println("<h2>✅ 订金支付成功！</h2>");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}