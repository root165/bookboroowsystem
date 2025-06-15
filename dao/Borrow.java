package dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Borrow {
    Connection connection;
    String username;
    JFrame jframe;
    DefaultTableModel tableM;
    DefaultTableModel tableMl;

    public Borrow(JFrame jframe, DefaultTableModel tableM, Connection connection, String username) {
        this.connection = connection;
        this.username = username;
        this.jframe = jframe;
        this.tableMl = tableM;
    }

    // 显示借阅信息（自动识别学生/教师）
    // 新增：自动更新逾期未归还状态
    private void updateOverdueStatus() {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE borrowrecord SET mystatus = '逾期未归还' WHERE mystatus = '正在借阅' AND due_date < CURDATE()")) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void showBorrowInfo(Object obj) {
        tableMl.setRowCount(0);
        updateOverdueStatus();
        String userTable, nameField, idField;
        if (obj instanceof Student) {
            userTable = "userstu";
            nameField = "sname";
            idField = "sno";
        } else if (obj instanceof Teacher) {
            userTable = "userteacher";
            nameField = "teachername";
            idField = "teachersno";
        } else {
            JOptionPane.showMessageDialog(jframe, "未知用户类型！");
            return;
        }
        String sql = String.format(
                "SELECT b.book_id, b.title, u.%s, borr.borrowdate, borr.returndate, borr.mystatus, borr.due_date " +
                        "FROM borrowrecord borr " +
                        "JOIN Book b ON borr.bookid = b.book_id " +
                        "JOIN %s u ON borr.id = u.%s " +
                        "WHERE borr.id = ?", nameField, userTable, idField);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableMl.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6),
                        rs.getString(7)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(jframe, "查询借阅信息失败");
        }
    }

    // 借书
    public void borrowBook(Object obj) {
        int maxTotal, maxOnce, day;
        String userTable, idField;
        if (obj instanceof Student) {
            userTable = "userstu";
            idField = "sno";
            maxTotal = 10;
            maxOnce = 3;
            day = 30;
        } else if (obj instanceof Teacher) {
            userTable = "userteacher";
            idField = "teachersno";
            maxTotal = 15;
            maxOnce = 5;
            day = 45;
        } else {
            JOptionPane.showMessageDialog(jframe, "未知用户类型！");
            return;
        }

        // 1. 检查当前未归还数量
        int currentBorrowed = 0;
        try (PreparedStatement countStmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM borrowrecord WHERE id = ? AND mystatus <> '已归还'")) {
            countStmt.setString(1, username);
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                currentBorrowed = rs.getInt(1);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(jframe, "查询当前借阅数量失败: " + ex.getMessage());
            return;
        }
        if (currentBorrowed >= maxTotal) {
            JOptionPane.showMessageDialog(jframe, "你当前在借图书已达" + maxTotal + "本上限，无法继续借书！");
            return;
        }

        // 2. 支持一次借多本，输入书号用英文逗号分隔
        String input = JOptionPane.showInputDialog(jframe, "请输入要借阅的书号（可用英文逗号分隔，每次最多" + maxOnce + "本）：");
        if (input == null || input.trim().isEmpty()) return;
        String[] bookIds = input.split(",");
        if (bookIds.length > maxOnce) {
            JOptionPane.showMessageDialog(jframe, "一次最多只能借" + maxOnce + "本书！");
            return;
        }
        if (currentBorrowed + bookIds.length > maxTotal) {
            JOptionPane.showMessageDialog(jframe, "你当前在借" + currentBorrowed + "本，再借" + bookIds.length + "本将超过" + maxTotal + "本上限！");
            return;
        }

        int successCount = 0;
        StringBuilder failMsg = new StringBuilder();

        for (String rawBookId : bookIds) {
            String bookId = rawBookId.trim();
            if (bookId.isEmpty()) continue;

            try {
                // 验证书籍是否存在
                try (PreparedStatement bookStmt = connection.prepareStatement(
                        "SELECT book_id FROM Book WHERE book_id = ?")) {
                    bookStmt.setString(1, bookId);
                    try (ResultSet bookRs = bookStmt.executeQuery()) {
                        if (!bookRs.next()) {
                            failMsg.append("书号[").append(bookId).append("]不存在！\n");
                            continue;
                        }
                    }
                }

                // 检查是否已借阅
                try (PreparedStatement checkStmt = connection.prepareStatement(
                        "SELECT bookid FROM borrowrecord WHERE bookid = ? AND id = ? and mystatus <> '已归还'")) {
                    checkStmt.setString(1, bookId);
                    checkStmt.setString(2, username);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            failMsg.append("你已借阅书号[").append(bookId).append("]，请勿重复借阅！\n");
                            continue;
                        }
                    }
                }

                // 设置应归还日期
                LocalDate dueDate = LocalDate.now().plus(day, ChronoUnit.DAYS);

                // 开始事务
                connection.setAutoCommit(false);
                try {
                    String status = "正在借阅";
                    try (PreparedStatement borrowStmt = connection.prepareStatement(
                            "INSERT INTO borrowrecord (id, bookid, borrowdate, mystatus, due_date) VALUES (?, ?, NOW(), ?, ?)")) {
                        borrowStmt.setString(1, username);
                        borrowStmt.setString(2, bookId);
                        borrowStmt.setString(3, status);
                        borrowStmt.setDate(4, java.sql.Date.valueOf(dueDate));
                        int rows = borrowStmt.executeUpdate();
                        if (rows > 0) {
                            successCount++;
                        } else {
                            failMsg.append("借阅书号[").append(bookId).append("]失败！\n");
                        }
                    }
                    connection.commit();
                } catch (SQLException ex) {
                    connection.rollback();
                    failMsg.append("借阅书号[").append(bookId).append("]失败: ").append(ex.getMessage()).append("\n");
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                failMsg.append("借阅书号[").append(bookId).append("]失败: ").append(ex.getMessage()).append("\n");
            }
        }

        StringBuilder resultMsg = new StringBuilder();
        if (successCount > 0) {
            resultMsg.append("成功借阅 ").append(successCount).append(" 本书！\n");
        }
        if (failMsg.length() > 0) {
            resultMsg.append(failMsg);
        }
        JOptionPane.showMessageDialog(jframe, resultMsg.toString());
        showBorrowInfo(obj);
    }

    // 还书
    public void returnBook(Object obj) {
        updateOverdueStatus();
        String userTable, idField;
        if (obj instanceof Student) {
            userTable = "userstu";
            idField = "sno";
        } else if (obj instanceof Teacher) {
            userTable = "userteacher";
            idField = "teachersno";
        } else {
            JOptionPane.showMessageDialog(jframe, "未知用户类型！");
            return;
        }
        String returnbookid = JOptionPane.showInputDialog(jframe, "输入要还书的bookid");
        if (returnbookid == null || returnbookid.isEmpty()) return;

        try (PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT due_date FROM borrowrecord WHERE bookid = ? AND id = ?  AND mystatus <> '已归还'")) {
            checkStmt.setString(1, returnbookid);
            checkStmt.setString(2, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                LocalDate returnDate = LocalDate.now();
                long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                if (overdueDays > 0) {
                    double penalty = overdueDays * 1.0;
                    try (PreparedStatement updatePenaltyStmt = connection.prepareStatement(
                            String.format("UPDATE %s SET penalty = penalty + ? WHERE %s = ?", userTable, idField))) {
                        updatePenaltyStmt.setDouble(1, penalty);
                        updatePenaltyStmt.setString(2, username);
                        updatePenaltyStmt.executeUpdate();
                        JOptionPane.showMessageDialog(jframe, "你已逾期 " + overdueDays + " 天，罚款 " + penalty + " 元");
                    }
                }
                String status = "已归还";
                try (PreparedStatement returnStmt = connection.prepareStatement(
                        "UPDATE borrowrecord SET returndate = CURDATE(), mystatus = ? WHERE bookid = ? AND id = ? AND returndate IS NULL")) {
                    returnStmt.setString(1, status);
                    returnStmt.setString(2, returnbookid);
                    returnStmt.setString(3, username);
                    int success = returnStmt.executeUpdate();
                    if (success > 0) {
                        JOptionPane.showMessageDialog(jframe, username + ": 还书成功");
                        showBorrowInfo(obj);
                    } else {
                        JOptionPane.showMessageDialog(jframe, "你未借阅该书！");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(jframe, "你未借阅该书！");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(jframe, "错误!!!");
        }
    }

    // 显示个人信息
    public void showPersonalInfo(Object obj) {

        String sql, nameField, idField;
        int maxTotal, maxOnce;
        if (obj instanceof Student) {
            sql = "SELECT sname, sno, penalty FROM userstu WHERE sno = ?";
            nameField = "sname";
            idField = "sno";
            maxTotal = 10;
            maxOnce = 3;
        } else if (obj instanceof Teacher) {
            sql = "SELECT teachername, teachersno, penalty FROM userteacher WHERE teachersno = ?";
            nameField = "teachername";
            idField = "teachersno";
            maxTotal = 15;
            maxOnce = 5;
        } else {
            JOptionPane.showMessageDialog(jframe, "未知用户类型！");
            return;
        }
        try (PreparedStatement personalstatement = connection.prepareStatement(sql)) {
            personalstatement.setString(1, username);
            ResultSet myset = personalstatement.executeQuery();
            if (myset.next()) {
                String name = myset.getString(nameField);
                String id = myset.getString(idField);
                double penalty = myset.getDouble("penalty");

                // 查询当前已借未还数量
                int borrowed = 0;
                try (PreparedStatement countStmt = connection.prepareStatement(
                        "SELECT COUNT(*) FROM borrowrecord WHERE id = ? AND mystatus <> '已归还'")) {
                    countStmt.setString(1, username);
                    ResultSet rs = countStmt.executeQuery();
                    if (rs.next()) {
                        borrowed = rs.getInt(1);
                    }
                }
                int canBorrow = maxTotal - borrowed;
                if (canBorrow < 0) canBorrow = 0;

                // 计算所有“正在借阅”且已逾期的罚款（未还书的动态罚款）
                double overdueUnpaid = 0;
                try (PreparedStatement overdueStmt = connection.prepareStatement(
                        "SELECT due_date FROM borrowrecord WHERE id = ? AND mystatus = '逾期未归还'")) {
                    overdueStmt.setString(1, username);
                    ResultSet rs = overdueStmt.executeQuery();
                    LocalDate today = LocalDate.now();
                    while (rs.next()) {
                        LocalDate due = rs.getDate("due_date").toLocalDate();
                        long overdueDays = ChronoUnit.DAYS.between(due, today);
                        if (overdueDays > 0) {
                            overdueUnpaid += overdueDays;
                        }
                    }
                }

                StringBuilder msg = new StringBuilder();
                msg.append("你的名字为：").append(name)
                        .append("\n账号为：").append(id)
                        .append("\n------------------------")
                        .append("\n当前已借：").append(borrowed).append(" 本")
                        .append("\n每次最多可借：").append(maxOnce).append(" 本")
                        .append("\n最多可借：").append(maxTotal).append(" 本")
                        .append("\n还能再借：").append(canBorrow).append(" 本")
                        .append("\n逾期罚款总金额：").append(penalty).append(" 元\n");

                if (overdueUnpaid > 0) {
                    msg.append("\n当前有逾期未还罚款：").append(overdueUnpaid).append(" 元（归还时需缴纳）");
                } else {
                    msg.append("\n无逾期未还罚款。");
                }

                // 如果有已累计罚款，弹出是否缴纳
                if (penalty > 0) {
                    int pay = JOptionPane.showConfirmDialog(jframe, msg + "\n是否现在缴纳全部已累计罚款？", "缴纳罚款", JOptionPane.YES_NO_OPTION);
                    if (pay == JOptionPane.YES_OPTION) {
                        String updateSql = obj instanceof Student ?
                                "UPDATE userstu SET penalty = 0 WHERE sno = ?" :
                                "UPDATE userteacher SET penalty = 0 WHERE teachersno = ?";
                        try (PreparedStatement payStmt = connection.prepareStatement(updateSql)) {
                            payStmt.setString(1, username);
                            int n = payStmt.executeUpdate();
                            if (n > 0) {
                                JOptionPane.showMessageDialog(jframe, "罚款缴纳成功，感谢配合！");
                            } else {
                                JOptionPane.showMessageDialog(jframe, "缴纳失败，请联系管理员！");
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(jframe, msg.toString());
                }
            }
        } catch (SQLException exce) {
            exce.printStackTrace();
        }
    }
}