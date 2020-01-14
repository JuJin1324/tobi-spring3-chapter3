package study.tobi.spring3.chapter3.db.access;

import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-12
 */

@NoArgsConstructor
@Setter
public class AccountDao {

    private DataSource dataSource;
}
