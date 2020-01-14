package study.tobi.spring3.chapter3.db.strategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-26
 *
 * 탬플릿/콜백 패턴에서 콜백 메서드 선언 인터페이스를 명시하기 위해서
 * @FunctionalInterface 선언
 */

@FunctionalInterface
public interface StatementStrategy {

    PreparedStatement makePreparedStatement(Connection connection) throws SQLException;
}
