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

@Setter
@NoArgsConstructor
public class UserDao {
    /* setter를 통한 DI */
    private DataSource dataSource;

    public void add(User user) throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?, ?, ?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();
    }

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

    public void deleteAll() throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("delete from users");
        ps.executeUpdate();
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
