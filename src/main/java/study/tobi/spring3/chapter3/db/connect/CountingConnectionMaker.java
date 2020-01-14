package study.tobi.spring3.chapter3.db.connect;

import lombok.AccessLevel;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-15
 */

@Getter
public class CountingConnectionMaker implements ConnectionMaker {

    private int counter = 0;

    @Getter(AccessLevel.NONE)   /* Getter 생성 제외 */
    private ConnectionMaker realConnectionMaker;

    public CountingConnectionMaker(ConnectionMaker realConnectionMaker) {
        this.realConnectionMaker = realConnectionMaker;
    }

    public Connection makeConnection() throws ClassNotFoundException, SQLException {
        this.counter++;
        return realConnectionMaker.makeConnection();
    }
}
