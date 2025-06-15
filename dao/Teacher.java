package dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Teacher extends JFrame {
    JButton information;
    JButton Borrowbook;
    JButton Returnbook;
    JButton own;
    String username;
    String password;
    Connection connection;
    JTabbedPane tabbedPane;
    DefaultTableModel borrowTableModel;
    DefaultTableModel bookTableModel;
    JTable borrowTable;
    JTable bookTable;
    Borrow stuborrow;

    public Teacher() {}

    public Teacher(String username, String password, Connection connection) throws Exception {
        setusername(username);
        setpassword(password);
        this.connection = connection;
        setTitle("教师");
        setLayout(null);
        setBounds(100, 100, 900, 600);
        setLocationRelativeTo(null);
        Container con = getContentPane();
        con.setLayout(null);

        JLabel wel = new JLabel("欢迎！" + username);
        wel.setFont(new Font("仿宋", 1, 25));
        wel.setBounds(350, 0, 260, 30);
        con.add(wel);

        Borrowbook = new JButton("借书");
        Borrowbook.setBounds(90, 40, 90, 30);
        con.add(Borrowbook);

        Returnbook = new JButton("还书");
        Returnbook.setBounds(180, 40, 90, 30);
        con.add(Returnbook);

        own = new JButton("个人信息");
        own.setBounds(270, 40, 90, 30);
        con.add(own);

        String[] items = {"设置", "退出登录", "更改密码"};
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBounds(780, 40, 80, 30);
        con.add(comboBox);
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
                    String sql = "UPDATE userteacher SET teacherpassword = ? WHERE teachersno = ?";
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

        // 借阅信息表格
        borrowTableModel = new DefaultTableModel(
                new String[]{"书号", "书名", "用户名", "借阅时间", "归还时间", "状态", "应归还日期"},
                0
        );
        borrowTable = new JTable(borrowTableModel);
        JScrollPane borrowScrollPane = new JScrollPane(borrowTable);

        // 书架表格
        bookTableModel = new DefaultTableModel(
                new String[]{"书号", "书名", "作者", "价格", "出版日期", "出版社", "类型"},
                0
        );
        bookTable = new JTable(bookTableModel);
        JScrollPane bookScrollPane = new JScrollPane(bookTable);

        // 书架搜索栏（仿管理员界面）
        JPanel bookSearchPanel = new JPanel();
        bookSearchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel typeLabel = new JLabel("类型:");
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"全部", "计算机", "文学", "外语", "数学", "其他"});
        JLabel keywordLabel = new JLabel("关键字:");
        JTextField bookSearchField = new JTextField(12);
        JButton bookSearchBtn = new JButton("搜索");
        bookSearchPanel.add(typeLabel);
        bookSearchPanel.add(categoryBox);
        bookSearchPanel.add(keywordLabel);
        bookSearchPanel.add(bookSearchField);
        bookSearchPanel.add(bookSearchBtn);

        JPanel bookPanel = new JPanel(new BorderLayout());
        bookPanel.add(bookSearchPanel, BorderLayout.NORTH);
        bookPanel.add(bookScrollPane, BorderLayout.CENTER);

        // 标签页
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("借阅信息", borrowScrollPane);
        tabbedPane.addTab("书架", bookPanel);
        tabbedPane.setBounds(0, 75, 880, 450);
        con.add(tabbedPane);

        stuborrow = new Borrow(this, borrowTableModel, connection, username);

        // 搜索事件
        bookSearchBtn.addActionListener(e -> {
            String keyword = bookSearchField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            loadBookShelfData(keyword, category);
        });

        Borrowbook.addActionListener(e -> {
            Borrowbook.setBackground(Color.orange);
            Returnbook.setBackground(null);
            own.setBackground(null);
            stuborrow.borrowBook(this);
        });

        Returnbook.addActionListener(e -> {
            Returnbook.setBackground(Color.orange);
            Borrowbook.setBackground(null);
            own.setBackground(null);
            stuborrow.returnBook(this);
        });

        own.addActionListener(e -> {
            own.setBackground(Color.orange);
            Borrowbook.setBackground(null);
            Returnbook.setBackground(null);
            stuborrow.showPersonalInfo(this);
        });

        // 借阅信息标签页
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                stuborrow.showBorrowInfo(this);
            }
        });

        // 书架标签页
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                loadBookShelfData("", "全部");
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
    }

    // 书架搜索事件
    private void loadBookShelfData(String keyword, String category) {
        bookTableModel.setRowCount(0);
        String sql = "SELECT book_id, title, author, price, pub_date, publisher_name, category FROM Book WHERE 1=1";
        if (!"全部".equals(category)) {
            sql += " AND category = '" + category + "'";
        }
        if (!keyword.isEmpty()) {
            sql += " AND (book_id LIKE '%" + keyword + "%' OR title LIKE '%" + keyword + "%' OR author LIKE '%" + keyword + "%')";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookTableModel.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getDouble(4), rs.getString(5), rs.getString(6), rs.getString(7)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "获取图书列表失败: " + ex.getMessage());
        }
    }

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