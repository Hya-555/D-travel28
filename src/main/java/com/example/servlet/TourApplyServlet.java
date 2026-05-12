package com.example.servlet;

import com.example.utils.DBUtil;
import com.example.utils.DepositCalculator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

@WebServlet("/applyTour")
public class TourApplyServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. 获取表单参数
        String groupCode = request.getParameter("groupCode");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        int adultNum = Integer.parseInt(request.getParameter("adultNum"));
        int childNum = Integer.parseInt(request.getParameter("childNum"));

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 2. 校验旅游团是否可报名
            String checkGroupSql = "SELECT id, start_date, max_people, adult_price, child_price FROM tour_group WHERE group_code=? AND status=1";
            PreparedStatement pstmt = conn.prepareStatement(checkGroupSql);
            pstmt.setString(1, groupCode);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                out.write("<h3 style='color:red'>❌ 该旅游团不存在或已截止报名！</h3>");
                return;
            }

            int groupId = rs.getInt("id");
            LocalDate startDate = rs.getDate("start_date").toLocalDate();
            int maxPeople = rs.getInt("max_people");
            double adultPrice = rs.getDouble("adult_price");
            double childPrice = rs.getDouble("child_price");

            // 校验人数是否超出上限（简化，实际应统计已报名人数）
            int currentPeople = 0;
            String countSql = "SELECT SUM(total_adult + total_child) AS total FROM tour_application WHERE group_id=?";
            pstmt = conn.prepareStatement(countSql);
            pstmt.setInt(1, groupId);
            ResultSet countRs = pstmt.executeQuery();
            if (countRs.next()) {
                currentPeople = countRs.getInt("total");
            }
            if (currentPeople + adultNum + childNum > maxPeople) {
                out.write("<h3 style='color:red'>❌ 该团人数已满，无法报名！</h3>");
                return;
            }

            // 3. 计算总金额和订金
            double totalAmount = adultNum * adultPrice + childNum * childPrice;
            double depositAmount = DepositCalculator.calculateDeposit(startDate, totalAmount);
            double balanceAmount = totalAmount - depositAmount;

            // 4. 保存申请人信息
            String insertUserSql = "INSERT INTO user_applicant(full_name, phone) VALUES(?,?)";
            pstmt = conn.prepareStatement(insertUserSql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.executeUpdate();
            ResultSet userRs = pstmt.getGeneratedKeys();
            userRs.next();
            int userId = userRs.getInt(1);

            // 5. 保存申请记录
            String insertApplySql = "INSERT INTO tour_application(group_id, applicant_id, total_adult, total_child, total_amount, deposit_amount, balance_amount) VALUES(?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(insertApplySql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, adultNum);
            pstmt.setInt(4, childNum);
            pstmt.setDouble(5, totalAmount);
            pstmt.setDouble(6, depositAmount);
            pstmt.setDouble(7, balanceAmount);
            pstmt.executeUpdate();

            conn.commit();
            out.write("<h3 style='color:green'>✅ 申请提交成功！</h3>");
            out.write("<p>旅游团：" + groupCode + "</p>");
            out.write("<p>申请人：" + name + "</p>");
            out.write("<p>总金额：" + totalAmount + " 元</p>");
            out.write("<p>需付订金：" + depositAmount + " 元</p>");
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            out.write("<h3 style='color:red'>❌ 申请失败：" + e.getMessage() + "</h3>");
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}