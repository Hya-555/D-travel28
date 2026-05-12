package com.example.servlet;

import com.example.utils.DBUtil;
import com.example.utils.DepositCalculator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

@WebServlet("/cancelApply")
public class CancelApplyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            String applyId = request.getParameter("applyId");

            Connection conn = DBUtil.getConnection();
            String sql = "SELECT t.deposit_amount,g.start_date "
                    + "FROM tour_application t "
                    + "JOIN tour_group g ON t.group_id=g.id "
                    + "WHERE t.id=?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, applyId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();

            double paid = rs.getDouble("deposit_amount");
            LocalDate start = rs.getDate("start_date").toLocalDate();
            double fee = DepositCalculator.calculateCancelFee(start, paid);

            String update = "UPDATE tour_application SET status=4 WHERE id=?";
            pstmt = conn.prepareStatement(update);
            pstmt.setString(1, applyId);
            pstmt.executeUpdate();

            out.println("<h2>❌ 已取消申请</h2>");
            out.println("扣除手续费：" + fee);
            out.println("退款金额：" + (paid - fee));
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}