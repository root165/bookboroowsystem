package dao;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;


// 数据库连接类
class jdbc {
    Connection  connection;
    DataSource  dataSource;

    public jdbc() throws Exception {
        this.dataSource  = new MysqlDataSource();  // 获取数据源
        ((MysqlDataSource)  dataSource).setURL("jdbc:mysql://127.0.0.1:3306/lib?characterEncoding=utf8&useSSL=false");
        //127.0.0.1 为mysql客户端ip、 3306为端口、 lib为我要连接的数据库名字、编码方式、加密方式设为false;
        ((MysqlDataSource)  dataSource).setUser("root");//用户名
        ((MysqlDataSource)  dataSource).setPassword("123456");//密码

        // 与数据库建立网络连接
        this.connection  = dataSource.getConnection();//返回值为 Connection类的对象
        if (connection.isValid(3))  {
            System.out.println(" 数据库连接成功");
        }
    }
}

