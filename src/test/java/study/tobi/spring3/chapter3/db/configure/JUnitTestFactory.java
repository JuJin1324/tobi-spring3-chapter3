package study.tobi.spring3.chapter3.db.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import study.tobi.spring3.chapter3.db.access.UserDao;

import javax.sql.DataSource;

/**
 * Created by Yoo Ju Jin(jujin1324@daum.net)
 * Created Date : 2019-09-12
 */

@Configuration
public class JUnitTestFactory {

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/spring3?useSSL=false";

    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setJdbcTemplate(jdbcTemplate());

        return userDao;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

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
