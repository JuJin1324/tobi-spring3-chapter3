package study.tobi.spring3.chapter3.db.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import study.tobi.spring3.chapter3.db.access.AccountDao;
import study.tobi.spring3.chapter3.db.access.MessageDao;
import study.tobi.spring3.chapter3.db.access.UserDao;

import javax.sql.DataSource;

/**
 * Created by Yoo Ju Jin(jujin1324@daum.net)
 * Created Date : 2020/01/07
 */

@Configuration
public class TestDaoFactory {
    private static final String MYSQL_TESTDB_URL = "jdbc:mysql://localhost:3306/testdb?useSSL=false";

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
        dataSource.setUrl(MYSQL_TESTDB_URL);
        dataSource.setUsername("scott");
        dataSource.setPassword("tiger");

        return dataSource;
    }

    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setDataSource(dataSource());

        return userDao;
    }

    @Bean
    public AccountDao accountDao() {
        AccountDao accountDao = new AccountDao();
        accountDao.setDataSource(dataSource());

        return accountDao;
    }

    @Bean
    public MessageDao messageDao() {
        MessageDao messageDao = new MessageDao();
        messageDao.setDataSource(dataSource());

        return messageDao;
    }
}
