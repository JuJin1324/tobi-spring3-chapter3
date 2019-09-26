package study.tobi.spring3.chapter3.user.dao;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.dao.EmptyResultDataAccessException;
import study.tobi.spring3.chapter3.user.entity.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 08/09/2019
 */

@NoArgsConstructor
@Setter
public class UserDao {

    private JdbcContext jdbcContext;
    private DataSource dataSource;

    public User get(String id) throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("select id, name, password from users where id = ?");
        ps.setString(1, id);

        @Cleanup
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }

        if (user == null) throw new EmptyResultDataAccessException(1);

        return user;
    }

    /* 클라이언트 : 전략 인터페이스인 StatementStrategy의 구현체를 컨텍스트로 주입 */
    public void deleteAll() throws SQLException {
        jdbcContext.workWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {
                return connection.prepareStatement("delete from users");
            }
        });
    }

    public void add(final User user) throws SQLException {

        /* spring3 에서는 람다를 사용하지 않는다. ArrayIndexOutOfBoundsException 오류 발생 */
        jdbcContext.workWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("insert into users(id, name, password) values (?, ?, ?)");
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());

                return ps;
            }
        });
    }

    public int getCount() throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("select count(*) from users");

        @Cleanup
        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        return count;
    }

}
