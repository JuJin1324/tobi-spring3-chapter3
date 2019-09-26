package study.tobi.spring3.chapter3.user.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-12
 *
 */

@Configuration
public class DaoFactory {

    private static final String MYSQL_URL = "jdbc:mysql://192.168.0.4:3306/spring3?useSSL=false";

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

//    @Bean
//    public ConnectionMaker connectionMaker() {
//        return new DConnectionMaker();
//    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

        dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
        dataSource.setUrl(MYSQL_URL);
        dataSource.setUsername("scott");
        dataSource.setPassword("tiger");

        return dataSource;
    }
}
