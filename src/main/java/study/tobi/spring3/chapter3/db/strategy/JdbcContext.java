package study.tobi.spring3.chapter3.db.strategy;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@NoArgsConstructor
@Setter
public class JdbcContext {
    /* setter를 통한 DI */
    DataSource dataSource;

    /*
     * 컨텍스트 : PreparedStatement를 실행하는 JDBC의 작업 흐름
     */
    public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();
        @Cleanup
        PreparedStatement ps = stmt.makePreparedStatement(c);

        ps.executeUpdate();
    }

    public void executeSql(final String query) throws SQLException {
        workWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {
                return connection.prepareStatement(query);
            }
        });
    }
}