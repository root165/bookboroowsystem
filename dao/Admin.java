package dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Admin extends JFrame {
    String username;
    String password;
    Connection connection;

    // 用户管理表格
    DefaultTableModel stuTableModel, teacherTableModel;
    JTable stuTable, teacherTable;

    // 图书管理表格
    DefaultTableModel bookTableModel;
    JTable bookTable;

    public Admin() {}

    public Admin(String username, String password, Connection connection) {
        setusername(username);
        setpassword(password);
        this.connection = connection;

        setTitle("管理员界面");
        setLayout(null);
        setBounds(100, 100, 1000, 700);
        setLocationRelativeTo(null);

        JLabel wel = new JLabel("欢迎管理员！" + username);
        wel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        wel.setBounds(320, 10, 400, 30);
        add(wel);

        String[] items = {"设置", "退出登录", "更改密码"};
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBounds(880, 30, 80, 20);
        add(comboBox);

        comboBox.addActionListener(e -> {
            String myselectitem = (String) comboBox.getSelectedItem();
            if ("退出登录".equals(myselectitem)) {
                JOptionPane.showMessageDialog(this, "正在退出登录！");
                setVisible(false);
            }
            if ("更改密码".equals(myselectitem)) {
                String newpassword = JOptionPane.showInputDialog(this, "确定要更改密码？ 请输入新密码！");
                if (newpassword == null || newpassword.trim().isEmpty()) {
                    return;
                } else {
                    String sql = "UPDATE useradmin SET adminpassword = ? WHERE adminno  = ?";
                    try (PreparedStatement stat = connection.prepareStatement(sql)) {
                        stat.setString(1, newpassword);
                        stat.setString(2, username);
                        int n = stat.executeUpdate();
                        if (n > 0) {
                            JOptionPane.showMessageDialog(this, "修改成功, 请重新登录！");
                            this.setVisible(false);
                        } else {
                            JOptionPane.showMessageDialog(this, "修改密码失败");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "mysql语句出错");
                    }
                }
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(10, 50, 970, 600);

        // 学生管理
        JPanel stuPanel = new JPanel(new BorderLayout());
        stuTableModel = new DefaultTableModel(
                new String[]{"学号", "姓名", "密码", "罚款"}, 0);
        stuTable = new JTable(stuTableModel);
        JScrollPane stuScroll = new JScrollPane(stuTable);
        stuPanel.add(stuScroll, BorderLayout.CENTER);

        JPanel stuBtnPanel = new JPanel();
        JButton addStu = new JButton("添加学生");
        JButton delStu = new JButton("删除学生");
        JButton resetStuPwd = new JButton("重置密码");
        JButton refreshStu = new JButton("刷新");
        stuBtnPanel.add(addStu);
        stuBtnPanel.add(delStu);
        stuBtnPanel.add(resetStuPwd);
        stuBtnPanel.add(refreshStu);
        stuPanel.add(stuBtnPanel, BorderLayout.SOUTH);

        // 教师管理
        JPanel teacherPanel = new JPanel(new BorderLayout());
        teacherTableModel = new DefaultTableModel(
                new String[]{"工号", "姓名", "密码", "罚款"}, 0);
        teacherTable = new JTable(teacherTableModel);
        JScrollPane teacherScroll = new JScrollPane(teacherTable);
        teacherPanel.add(teacherScroll, BorderLayout.CENTER);

        JPanel teacherBtnPanel = new JPanel();
        JButton addTeacher = new JButton("添加教师");
        JButton delTeacher = new JButton("删除教师");
        JButton resetTeacherPwd = new JButton("重置密码");
        JButton refreshTeacher = new JButton("刷新");
        teacherBtnPanel.add(addTeacher);
        teacherBtnPanel.add(delTeacher);
        teacherBtnPanel.add(resetTeacherPwd);
        teacherBtnPanel.add(refreshTeacher);
        teacherPanel.add(teacherBtnPanel, BorderLayout.SOUTH);

        // 图书管理
        JPanel bookPanel = new JPanel(new BorderLayout());
        bookTableModel = new DefaultTableModel(
                new String[]{"书号", "书名", "作者", "价格", "出版日期", "出版社", "类型"}, 0);
        bookTable = new JTable(bookTableModel);
        JScrollPane bookScroll = new JScrollPane(bookTable);

// 搜索栏
        JPanel bookSearchPanel = new JPanel();
        JTextField bookSearchField = new JTextField(10);
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"全部", "计算机", "文学", "外语", "数学", "其他"});
        JButton bookSearchBtn = new JButton("搜索");
        bookSearchPanel.add(new JLabel("类型:"));
        bookSearchPanel.add(categoryBox);
        bookSearchPanel.add(new JLabel("关键字:"));
        bookSearchPanel.add(bookSearchField);
        bookSearchPanel.add(bookSearchBtn);
        bookPanel.add(bookSearchPanel, BorderLayout.NORTH);
        bookPanel.add(bookScroll, BorderLayout.CENTER);

        JPanel bookBtnPanel = new JPanel();
        JButton addBook = new JButton("添加图书");
        JButton delBook = new JButton("删除图书");
        JButton updateBook = new JButton("修改图书");
        JButton refreshBook = new JButton("刷新");
        bookBtnPanel.add(addBook); bookBtnPanel.add(delBook); bookBtnPanel.add(updateBook); bookBtnPanel.add(refreshBook);
        bookPanel.add(bookBtnPanel, BorderLayout.SOUTH);

        // 借阅管理主面板
        JTabbedPane borrowTab = new JTabbedPane();

        // 学生借阅管理
        JPanel stuBorrowPanel = new JPanel(new BorderLayout());
        DefaultTableModel stuBorrowTableModel = new DefaultTableModel(
                new String[]{"借阅号", "学号", "书号", "借阅时间", "归还时间", "状态", "应归还日期"}, 0);
        JTable stuBorrowTable = new JTable(stuBorrowTableModel);
        JScrollPane stuBorrowScroll = new JScrollPane(stuBorrowTable);

        JPanel stuSearchPanel = new JPanel();
        JTextField stuUserField = new JTextField(10);
        JTextField stuBookField = new JTextField(10);
        JButton stuSearchBtn = new JButton("搜索");
        stuSearchPanel.add(new JLabel("学号:"));
        stuSearchPanel.add(stuUserField);
        stuSearchPanel.add(new JLabel("书号:"));
        stuSearchPanel.add(stuBookField);
        stuSearchPanel.add(stuSearchBtn);

        stuBorrowPanel.add(stuSearchPanel, BorderLayout.NORTH);
        stuBorrowPanel.add(stuBorrowScroll, BorderLayout.CENTER);

        // 教师借阅管理
        JPanel teaBorrowPanel = new JPanel(new BorderLayout());
        DefaultTableModel teaBorrowTableModel = new DefaultTableModel(
                new String[]{"借阅号", "工号", "书号", "借阅时间", "归还时间", "状态", "应归还日期"}, 0);
        JTable teaBorrowTable = new JTable(teaBorrowTableModel);
        JScrollPane teaBorrowScroll = new JScrollPane(teaBorrowTable);

        JPanel teaSearchPanel = new JPanel();
        JTextField teaUserField = new JTextField(10);
        JTextField teaBookField = new JTextField(10);
        JButton teaSearchBtn = new JButton("搜索");
        teaSearchPanel.add(new JLabel("工号:"));
        teaSearchPanel.add(teaUserField);
        teaSearchPanel.add(new JLabel("书号:"));
        teaSearchPanel.add(teaBookField);
        teaSearchPanel.add(teaSearchBtn);

        teaBorrowPanel.add(teaSearchPanel, BorderLayout.NORTH);
        teaBorrowPanel.add(teaBorrowScroll, BorderLayout.CENTER);

        borrowTab.addTab("学生借阅管理", stuBorrowPanel);
        borrowTab.addTab("教师借阅管理", teaBorrowPanel);

        JPanel borrowBtnPanel = new JPanel();
        JButton addBorrow = new JButton("添加借阅");
        JButton refreshBorrow = new JButton("刷新借阅");
        JButton statBorrow = new JButton("借阅统计");
        borrowBtnPanel.add(addBorrow);
        borrowBtnPanel.add(refreshBorrow);
        borrowBtnPanel.add(statBorrow);

        JPanel borrowMainPanel = new JPanel(new BorderLayout());
        borrowMainPanel.add(borrowTab, BorderLayout.CENTER);
        borrowMainPanel.add(borrowBtnPanel, BorderLayout.SOUTH);

        // 添加到tab
        tabbedPane.addTab("学生管理", stuPanel);
        tabbedPane.addTab("教师管理", teacherPanel);
        tabbedPane.addTab("图书管理", bookPanel);
        tabbedPane.addTab("借阅管理", borrowMainPanel);

        add(tabbedPane);

        // 学生借阅搜索
        stuSearchBtn.addActionListener(e -> {
            String user = stuUserField.getText().trim();
            String book = stuBookField.getText().trim();
            loadStuBorrowData(stuBorrowTableModel, user, book);
        });
        // 教师借阅搜索
        teaSearchBtn.addActionListener(e -> {
            String user = teaUserField.getText().trim();
            String book = teaBookField.getText().trim();
            loadTeaBorrowData(teaBorrowTableModel, user, book);
        });
        // 刷新按钮
        refreshBorrow.addActionListener(e -> {
            loadStuBorrowData(stuBorrowTableModel, "", "");
            loadTeaBorrowData(teaBorrowTableModel, "", "");
        });
        // 添加借阅
        addBorrow.addActionListener(e -> addBorrowRecord());

        // 图书搜索
        bookSearchBtn.addActionListener(e -> {
            String keyword = bookSearchField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            loadBookDataWithSearch(keyword, category);
        });

        refreshStu.addActionListener(e -> loadStudentData());
        addStu.addActionListener(e -> addStudent());
        delStu.addActionListener(e -> delStudent());
        resetStuPwd.addActionListener(e -> resetStudentPwd());

        refreshTeacher.addActionListener(e -> loadTeacherData());
        addTeacher.addActionListener(e -> addTeacher());
        delTeacher.addActionListener(e -> delTeacher());
        resetTeacherPwd.addActionListener(e -> resetTeacherPwd());

        refreshBook.addActionListener(e -> loadBookData());
        addBook.addActionListener(e -> addBook());
        delBook.addActionListener(e -> delBook());
        updateBook.addActionListener(e -> updateBook());

        statBorrow.addActionListener(e -> statBorrow());

        // 初始化数据
        loadStudentData();
        loadTeacherData();
        loadBookData();
        loadStuBorrowData(stuBorrowTableModel, "", "");
        loadTeaBorrowData(teaBorrowTableModel, "", "");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
    }

    //  数据加载方法
    private void loadStuBorrowData(DefaultTableModel model, String user, String book) {
        model.setRowCount(0);
        String sql = "SELECT borrowid, id, bookid, borrowdate, returndate, mystatus, due_date FROM borrowrecord WHERE id IN (SELECT sno FROM userstu)";
        if (!user.isEmpty()) sql += " AND id = '" + user + "'";
        if (!book.isEmpty()) sql += " AND bookid = '" + book + "'";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载学生借阅数据失败");
        }
    }

    private void loadTeaBorrowData(DefaultTableModel model, String user, String book) {
        model.setRowCount(0);
        String sql = "SELECT borrowid, id, bookid, borrowdate, returndate, mystatus, due_date FROM borrowrecord WHERE id IN (SELECT teachersno FROM userteacher)";
        if (!user.isEmpty()) sql += " AND id = '" + user + "'";
        if (!book.isEmpty()) sql += " AND bookid = '" + book + "'";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载教师借阅数据失败");
        }
    }

    private void loadBookDataWithSearch(String keyword, String category) {
        bookTableModel.setRowCount(0);
        String sql = "SELECT book_id, title, author, price, pub_date, publisher_name, category FROM Book WHERE 1=1";
        if (!"全部".equals(category)) {
            sql += " AND category = '" + category + "'";
        }
        if (!keyword.isEmpty()) {
            sql += " AND (book_id LIKE '%" + keyword + "%' OR title LIKE '%" + keyword + "%' OR author LIKE '%" + keyword + "%')";
        }
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                bookTableModel.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getDouble(4), rs.getString(5), rs.getString(6), rs.getString(7)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "图书搜索失败");
        }
    }

    private void loadStudentData() {
        stuTableModel.setRowCount(0);
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT sno, sname, spassword, IFNULL(penalty,0) FROM userstu")) {
            while (rs.next()) {
                stuTableModel.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载学生数据失败");
        }
    }

    private void loadTeacherData() {
        teacherTableModel.setRowCount(0);
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT teachersno, teachername, teacherpassword, IFNULL(penalty,0) FROM userteacher")) {
            while (rs.next()) {
                teacherTableModel.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载教师数据失败");
        }
    }

    private void loadBookData() {
        bookTableModel.setRowCount(0);
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT book_id, title, author, price, pub_date, publisher_name FROM Book")) {
            while (rs.next()) {
                bookTableModel.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getDouble(4), rs.getString(5), rs.getString(6)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载图书数据失败");
        }
    }

    private void addStudent() {
        JTextField sno = new JTextField();
        JTextField sname = new JTextField();
        JTextField spassword = new JTextField();
        Object[] msg = {"学号:", sno, "姓名:", sname, "密码:", spassword};
        int ok = JOptionPane.showConfirmDialog(this, msg, "添加学生", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO userstu (sno, sname, spassword) VALUES (?, ?, ?)")) {
                ps.setString(1, sno.getText());
                ps.setString(2, sname.getText());
                ps.setString(3, spassword.getText());
                ps.executeUpdate();
                loadStudentData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "添加学生失败：" + ex.getMessage());
            }
        }
    }

    private void delStudent() {
        int row = stuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的学生");
            return;
        }
        String sno = (String) stuTableModel.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this, "确定删除学号为 " + sno + " 的学生？", "确认", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM userstu WHERE sno = ?")) {
                ps.setString(1, sno);
                ps.executeUpdate();
                loadStudentData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除学生失败：" + ex.getMessage());
            }
        }
    }

    private void resetStudentPwd() {
        int row = stuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要重置密码的学生");
            return;
        }
        String sno = (String) stuTableModel.getValueAt(row, 0);
        String newPwd = JOptionPane.showInputDialog(this, "输入新密码：");
        if (newPwd != null && !newPwd.trim().isEmpty()) {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE userstu SET spassword = ? WHERE sno = ?")) {
                ps.setString(1, newPwd);
                ps.setString(2, sno);
                ps.executeUpdate();
                loadStudentData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "重置密码失败：" + ex.getMessage());
            }
        }
    }

    private void addTeacher() {
        JTextField tno = new JTextField();
        JTextField tname = new JTextField();
        JTextField tpassword = new JTextField();
        Object[] msg = {"工号:", tno, "姓名:", tname, "密码:", tpassword};
        int ok = JOptionPane.showConfirmDialog(this, msg, "添加教师", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO userteacher (teachersno, teachername, teacherpassword) VALUES (?, ?, ?)")) {
                ps.setString(1, tno.getText());
                ps.setString(2, tname.getText());
                ps.setString(3, tpassword.getText());
                ps.executeUpdate();
                loadTeacherData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "添加教师失败：" + ex.getMessage());
            }
        }
    }

    private void delTeacher() {
        int row = teacherTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的教师");
            return;
        }
        String teachno = (String) teacherTableModel.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this, "确定删除工号为 " + teachno + " 的教师？", "确认", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM userteacher WHERE teachersno = ?")) {
                ps.setString(1, teachno);
                ps.executeUpdate();
                loadTeacherData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除教师失败：" + ex.getMessage());
            }
        }
    }

    private void resetTeacherPwd() {
        int row = teacherTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要重置密码的教师");
            return;
        }
        String tno = (String) teacherTableModel.getValueAt(row, 0);
        String newPwd = JOptionPane.showInputDialog(this, "输入新密码：");
        if (newPwd != null && !newPwd.trim().isEmpty()) {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE userteacher SET teacherpassword = ? WHERE teachersno = ?")) {
                ps.setString(1, newPwd);
                ps.setString(2, tno);
                ps.executeUpdate();
                loadTeacherData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "重置密码失败：" + ex.getMessage());
            }
        }
    }

    private void addBook() {//添加图书
        JTextField bookid = new JTextField();
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField price = new JTextField();
        JTextField pubdate = new JTextField();
        JTextField publisher = new JTextField();
        JTextField category = new JTextField();
        Object[] msg = {"书号:", bookid, "书名:", title, "作者:", author, "价格:", price, "出版日期(yyyy-mm-dd):", pubdate, "出版社:", publisher, "类型:", category};
        int ok = JOptionPane.showConfirmDialog(this, msg, "添加图书", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Book (book_id, title, author, price, pub_date, publisher_name, category) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, bookid.getText());
                ps.setString(2, title.getText());
                ps.setString(3, author.getText());
                ps.setDouble(4, Double.parseDouble(price.getText()));
                ps.setString(5, pubdate.getText());
                ps.setString(6, publisher.getText());
                ps.setString(7, category.getText());
                ps.executeUpdate();
                loadBookData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "添加图书失败：" + ex.getMessage());
            }
        }
    }

    // 修改图书
    private void updateBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的图书");
            return;
        }
        String bookid = (String) bookTableModel.getValueAt(row, 0);
        JTextField title = new JTextField((String) bookTableModel.getValueAt(row, 1));
        JTextField author = new JTextField((String) bookTableModel.getValueAt(row, 2));
        JTextField price = new JTextField(bookTableModel.getValueAt(row, 3).toString());
        JTextField pubdate = new JTextField((String) bookTableModel.getValueAt(row, 4));
        JTextField publisher = new JTextField((String) bookTableModel.getValueAt(row, 5));
        JTextField category = new JTextField((String) bookTableModel.getValueAt(row, 6));
        Object[] msg = {"书名:", title, "作者:", author, "价格:", price, "出版日期(yyyy-mm-dd):", pubdate, "出版社:", publisher, "类型:", category};
        int ok = JOptionPane.showConfirmDialog(this, msg, "修改图书", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE Book SET title=?, author=?, price=?, pub_date=?, publisher_name=?, category=? WHERE book_id=?")) {
                ps.setString(1, title.getText());
                ps.setString(2, author.getText());
                ps.setDouble(3, Double.parseDouble(price.getText()));
                ps.setString(4, pubdate.getText());
                ps.setString(5, publisher.getText());
                ps.setString(6, category.getText());
                ps.setString(7, bookid);
                ps.executeUpdate();
                loadBookData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "修改图书失败：" + ex.getMessage());
            }
        }
    }
    private void delBook() {//删除图书
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的图书");
            return;
        }
        String bookid = (String) bookTableModel.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this, "确定删除书号为 " + bookid + " 的图书？", "确认", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM Book WHERE book_id = ?")) {
                ps.setString(1, bookid);
                ps.executeUpdate();
                loadBookData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除图书失败：" + ex.getMessage());
            }
        }
    }



    private void statBorrow() {
        String sql = "SELECT mystatus, COUNT(*) FROM borrowrecord GROUP BY mystatus";
        StringBuilder sb = new StringBuilder("借阅统计：\n");
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                sb.append(rs.getString(1)).append("：").append(rs.getInt(2)).append(" 条\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "统计失败：" + ex.getMessage());
        }
    }

    private void addBorrowRecord() {
        JTextField idField = new JTextField();
        JTextField bookidField = new JTextField();
        JTextField borrowdateField = new JTextField();
        JTextField dueDateField = new JTextField();
        Object[] msg = {
                "用户编号（学号/工号）:", idField,
                "书号:", bookidField,
                "借阅日期(yyyy-mm-dd):", borrowdateField,
                "应归还日期(yyyy-mm-dd):", dueDateField
        };
        int ok = JOptionPane.showConfirmDialog(this, msg, "添加借阅信息", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO borrowrecord (id, bookid, borrowdate, mystatus, due_date) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, idField.getText().trim());
                ps.setString(2, bookidField.getText().trim());
                ps.setString(3, borrowdateField.getText().trim());
                ps.setString(4, "正在借阅");
                ps.setString(5, dueDateField.getText().trim());
                ps.executeUpdate();
                loadStuBorrowData((DefaultTableModel)((JTable)((JScrollPane)((JPanel)((JTabbedPane)((JPanel)((JTabbedPane)getContentPane().getComponent(0)).getComponentAt(3)).getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getView()).getModel(), "", "");
                loadTeaBorrowData((DefaultTableModel)((JTable)((JScrollPane)((JPanel)((JTabbedPane)((JPanel)((JTabbedPane)getContentPane().getComponent(0)).getComponentAt(3)).getComponent(0)).getComponent(1)).getComponent(0)).getViewport().getView()).getModel(), "", "");
                JOptionPane.showMessageDialog(this, "添加借阅信息成功！");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "添加借阅信息失败：" + ex.getMessage());
            }
        }
    }

    // Getter 和 Setter
    public void setusername(String username) {
        this.username = username;
    }
    public void setpassword(String password) {
        this.password = password;
    }
    public String getpassword() {
        return password;
    }
    public String getusername() {
        return username;
    }
}