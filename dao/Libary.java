package  dao;

import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class Libary extends JFrame {
    JButton stu;
    JButton ter;
    JButton admin;
    JPanel panel1;
    JPanel panel2;
    JPanel panel3;
    JTextField jtext1;
    JPasswordField jtext2;
    JButton login;
    JButton exit;
    String currentUserType;

    public Libary() throws Exception {
        setTitle("图书馆管理系统");
        setLayout(new GridLayout(3, 1));
        setSize(700, 600);
        setLocationRelativeTo(null);

        // 自定义带背景图片的面板
        JPanel bgPanel = new JPanel() {
            Image bg = new ImageIcon("D:\\17097\\lib\\dao\\fujjutlib.png").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);//大小设定
                // 画一个带透明度的白色矩形覆盖图片，使图片变淡
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(255, 255, 255, 65)); // 最后一位为透明度，越大越淡,0-255
                g2d.fillRect(0, 0, getWidth(), getHeight());//白色透明面板
            }
        };

        bgPanel.setLayout(new GridLayout(3, 1));
        setContentPane(bgPanel);

        Container contain = getContentPane();
        contain.setLayout(new GridLayout(3, 1));

        // 用户类型按钮
        panel1 = new JPanel(new FlowLayout());
        stu = new JButton("学生");
        stu.setFont(new Font("仿宋",1,20));
        ter = new JButton("教师");
        ter.setFont(new Font("仿宋",1,20));
        admin = new JButton("管理员");
        admin.setFont(new Font("仿宋",1,20));
        panel1.setOpaque(false);//面板设置为透明
        panel1.add(stu);
        panel1.add(ter);
        panel1.add(admin);
        contain.add(panel1);

        // 用户名密码输入
        panel2 = new JPanel();
        panel2.setLayout(null);
        panel2.setPreferredSize(new Dimension(700, 200));
        int labelWidth = 100, labelHeight = 50, fieldWidth = 200, fieldHeight = 50, panelWidth = 700;
        JLabel j1 = new JLabel("用户名：");
        j1.setFont(new Font("仿宋",1,20));
        j1.setBounds((panelWidth - fieldWidth) / 2 - labelWidth, 40, labelWidth, labelHeight);
        panel2.setOpaque(false);//面板设置为透明
        panel2.add(j1);
        jtext1 = new JTextField();
        jtext1.setBounds((panelWidth - fieldWidth) / 2, 40, fieldWidth, fieldHeight);
        panel2.add(jtext1);
        JLabel j2 = new JLabel("密码: ");
        j2.setFont(new Font("仿宋",1,20));
        j2.setBounds((panelWidth - fieldWidth) / 2 - labelWidth, 100, labelWidth, labelHeight);
        panel2.add(j2);
        jtext2 = new JPasswordField();
        jtext2.setBounds((panelWidth - fieldWidth) / 2, 100, fieldWidth, fieldHeight);
        panel2.add(jtext2);
        contain.add(panel2);

        // 登录退出按钮
        panel3 = new JPanel();
        panel3.setLayout(null);
        login = new JButton("登录");
        login.setFont(new Font("仿宋",1,20));
        login.setBounds(170, 120, 100, 45);
        exit = new JButton("退出"); // 退出按钮
       exit.setFont(new Font("仿宋",1,20));
        exit.setBounds(380, 120, 100, 45);
        panel3.setOpaque(false);//设置透明
        panel3.add(login);
        panel3.add(exit);
        contain.add(panel3);

        // 用户类型按钮事件
        stu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                currentUserType = "学生";
                stu.setBackground(Color.RED);
                ter.setBackground(null);
                admin.setBackground(null);
                JOptionPane.showMessageDialog(Libary.this,"你选择的用户类型： 学生");
            }
        });
        ter.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                currentUserType = "教师";
                ter.setBackground(Color.RED);
                stu.setBackground(null);
                admin.setBackground(null);
                JOptionPane.showMessageDialog(Libary.this,"你选择的用户类型： 教师");
            }
        });
        admin.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                currentUserType = "管理员";
                admin.setBackground(Color.RED);
                stu.setBackground(null);
                ter.setBackground(null);
                JOptionPane.showMessageDialog(Libary.this,"你选择的用户类型： 管理员");
            }
        });

        // 登录按钮事件
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = jtext1.getText().trim();
                String password = new String(jtext2.getPassword()).trim();

                if (currentUserType == null) {
                    JOptionPane.showMessageDialog(Libary.this, "请选择用户类型");
                    return;
                }
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(Libary.this, "请输入用户名和密码");
                    return;
                }

                String tableName = "";
                String id = "";
                String mypassword = "";
                if ("学生".equals(currentUserType)) {
                    tableName = "userstu";
                    id = "sno";
                    mypassword = "spassword";
                } else if ("教师".equals(currentUserType)) {
                    tableName = "userteacher";
                    id = "teachersno";
                    mypassword = "teacherpassword";
                } else if ("管理员".equals(currentUserType)) {
                    tableName = "useradmin";
                    id = "adminno";
                    mypassword = "adminpassword";
                }

                jdbc j;
                try {
                    j = new jdbc();
                    String sql = String.format("SELECT * FROM %s WHERE %s = ? AND %s = ?", tableName, id, mypassword);
                    PreparedStatement sta = j.connection.prepareStatement(sql);
                    sta.setString(1, username);
                    sta.setString(2, password);
                    ResultSet rs = sta.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(Libary.this, currentUserType + "登录成功");
                        if ("学生".equals(currentUserType)) {
                            new Student(username, password, j.connection);
                        } else if ("教师".equals(currentUserType)) {
                            new Teacher(username, password, j.connection);

                        } else if ("管理员".equals(currentUserType)) {
                            new Admin(username, password, j.connection);

                        }
                    } else {
                        JOptionPane.showMessageDialog(Libary.this, currentUserType + "登录失败，用户名或密码错误");
                    }
                    rs.close();
                    sta.close();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Libary.this, "数据库连接错误：" + ex.getMessage());
                }
            }
        });

        // 退出按钮事件
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        new Libary();
    }
}