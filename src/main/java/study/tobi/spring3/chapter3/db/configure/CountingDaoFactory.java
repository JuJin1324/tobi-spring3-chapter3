package study.tobi.spring3.chapter3.db.configure;

import org.springframework.context.annotation.Configuration;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-15
 */

@Configuration
public class CountingDaoFactory {

    /*
     * UserDao 클래스에서
     * ConnectionMaker -> DataSource
     * 변경으로 인해 더이상 사용 안함.
     */

//    @Bean
//    public UserDao userDao() {
//        UserDao userDao = new UserDao();
//        userDao.setConnectionMaker(connectionMaker());
//
//        return userDao;
//    }

//    @Bean
//    public ConnectionMaker connectionMaker() {
//        return new CountingConnectionMaker(realConnectionMaker());
//    }

//    @Bean
//    public ConnectionMaker realConnectionMaker() {
//        return new HConnectionMaker();
//    }
}
