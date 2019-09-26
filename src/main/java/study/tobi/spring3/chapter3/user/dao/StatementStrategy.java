package study.tobi.spring3.chapter3.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-26
 */
public interface StatementStrategy {

    PreparedStatement makePreparedStatement(Connection connection) throws SQLException;
}
