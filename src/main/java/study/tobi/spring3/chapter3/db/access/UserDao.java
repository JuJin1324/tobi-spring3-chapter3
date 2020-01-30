package study.tobi.spring3.chapter3.db.access;

import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import study.tobi.spring3.chapter3.db.entity.User;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 08/09/2019
 */

@NoArgsConstructor
public class UserDao {

    private JdbcTemplate    jdbcTemplate;
    private RowMapper<User> userRowMapper;

    public void setDataSource(DataSource dataSource) {

        jdbcTemplate = new JdbcTemplate(dataSource);
        userRowMapper = (resultSet, i) -> {
            /*
             * 이미 queryForObject 템플릿 내부에서 resultSet을 한번 next() 호출 후에
             * RowMapper 콜백 메서드를 호출하기 때문에 따로 resultSet.next() 메서드를 호출하지 않고
             * 바로 사용한다.
             */
            User user = new User();
            user.setId(resultSet.getString("id"));
            user.setName(resultSet.getString("name"));
            user.setPassword(resultSet.getString("password"));

            return user;
        };
    }

    public User get(String id) {
        return jdbcTemplate.queryForObject("select * from users where id = ?",
                new Object[]{id},
                userRowMapper);
    }

    /* 클라이언트 : 전략 인터페이스인 StatementStrategy의 구현체를 컨텍스트로 주입 */
    public void deleteAll() {
        jdbcTemplate.update("delete from users");
    }

    public void add(final User user) {
        jdbcTemplate.update("insert into users(id, name, password) values (?, ?, ?)",
                user.getId(), user.getName(), user.getPassword());
    }

    public int getCount() {
        return jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
    }

    public List<User> getAll() {
        return jdbcTemplate.query("select * from users order by id",
                userRowMapper);
    }
}
