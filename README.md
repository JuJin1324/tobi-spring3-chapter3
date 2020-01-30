# 3장 템플릿
토비의 스프링 3.1 Vol.1 스프링 이해와 원리

## 기초 셋팅
[1장 오브젝트와 의존관계](https://github.com/JuJin1324/tobi-spring3-chapter1)의 기초 셋팅을 모두 가져옴(Maven 포함)

## 3.1 다시 보는 초난감 DAO
2장까지의 UserDao에서 부족한점 : 예외상황에 대한 처리

### 3.1.1 예외처리 기능을 갖춘 DAO
Connection 및 PreparedStatement 메서드 사용 시에 예외가 발생하여 메서드를 나가게되어 DB 커넥션을 제대로 닫아주지 않게되면 
DB 커넥션에 대한 리소스를 반납하지 않게된다. 보통 서버는 DB 커넥션 몇개를 미리 만들어서 DB 풀로 가지고 있는다. 만약 DB 커넥션을 제대로 닫지 않게되면
해당 커넥션은 DB 풀에 반납이 되지 않고 그럼 DB 풀에 있는 커넥션은 모두 연결 상태가 되고 재사용을 불가능해진다. 

## 변하는 것과 변하지 않는 것
### 3.2.1 JDBC try/catch/finally 코드의 문제점
복잡한 try/catch/finally 블록이 2중으로 중접되어 있으며 모든 메서드마다 반복됨.
```java
public void deleteAll() throws SQLException {
    Connection c = null;
    PreparedStatement ps = null;
    try {
        c = dataSource.getConnection();

        /* 변하는 부분 */        
        ps = c.preparedStatement("delete from users");
        /* --------- */

        ps.executeUpdate();
    } catch (SQLException e) {
        /* 예외 발생시 이를 메서드 밖으로 던지기 */
        throw e;
    } finally {
        /* 적절히 닫아주기 */
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
            }
        }
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
            }
        }
    }
}
```

### 템플릿 메서드 패턴의 적용
템플릿 메서드 패턴은 상속을 통해 기능을 확장해서 사용하는 패턴.
* 기능 확장이 필요한 오브젝트(여기서는 PreparedStatment) 생성 부분을 별도의 메서드로 분리

* 메서드를 abstract(추상) 메서드로 변경

* UserDao 클래스의 하위클래스 생성(DeleteAllUserDao) 및 상속

* 하위클래스인 DeleteAllUserDao에서 PreparedStatement 생성 메서드(2번의 추상메서드)를 구현.

템플릿 메서드 패턴의 단점
* DAO 로직마다 상속을 통해 새로운 클래스를 만들어야함. 현재 UserDao에 있는 메서드가 (add, get, deleteAll, getCount) 4개인데 각 메서드를 위해
상속한 새로운 클래스 4개를 만들어야함.

* 서브클래스들이 이미 클래스 레벨에서 컴파일 시점에 그 관계가 결정되어 있음. 따라서 그 관계에 대한 유연성이 떨어짐.

### 전략 패턴의 적용
기능 확장이 필요한 오브젝트를 외부 인터페이스를 통해서 주입받도록 함.    
클래스 레벨에서는 인터페이스를 통해서만 의존하도록 만드는 패턴.

* 컨텍스트(Context) : 전반적 기능이 동작하는 클래스 ex) UserDao 혹은 메서드

* 전략(Strategy) : 기능 확장이 가능하도록 컨텍스트 내부에 선언된 인터페이스 ex) StatementStrategy

* 클라이언트(Client) : 컨텍스트 클래스에 전략 인터페이스를 상속받은 클래스 중 선택하여 DI(의존 주입)을 해주는 클래스 혹은 설정파일 
ex) DaoFactory, applicationContext.xml

## 3.3 JDBC 전략 패턴의 최적화
자주 변하는 부분은 전략(Strategy) 메서드로 분리, 자주 변하지 않는 부분은 템플릿(Template) 메서드로 분리하여 전략 메서드를 주입받음.

### 익명 내부 클래스
이름을 갖지 않는 클래스. 클래스 선언과 오브젝트 생성이 결합된 형태로 만들어진다. 
* 선언방식 : `new 인터페이스이름() { 클래스 본문 }`

### 람다(Lambda)
메서드가 한 개인 익명 내부 클래스를 클래스 및 메서드명을 생략하여 익명 함수의 형태로 선언하는 방식. 해당 내부 클래스가 구현하는 인터페이스에 한 개의 메서드만 
선언한다는 의미의 `@FunctionalInterface` 애노테이션 선언

주의 : spring framework 3 버전에서는 람다식을 사용하지 않는다. 사용시 junit test에서 ArrayIndexOutOfBoundsException 발생.

요구 : spring framework 버전 4.3.25.RELEASE 로 변경, junit 버전 4.12로 변경 

선언방식 
* 본문 2줄 이상 : `(메서드의 파라미터) -> { 메서드 본문 }`
* 본문 1줄 : `() -> 본문`
* 본문 1줄이면서 리턴까지 하는 경우 역시 2번을 사용하면 알아서 리턴.

## 3.4 컨텍스트와 DI
### 3.4.1 JdbcContext의 분리
전략 패턴의 구조로 본 구성도
* UserDao의 메서드(add, deleteAll...등) : 클라이언트
* jdbcContextWithStatementStrategy() 메서드 : 컨텍스트
* jdbcContextWithStatementStrategy() 에서 주입받는 익명 클래스 : 개별 전략

클래스 분리
* jdbcContextWithStatementStrategy() 메서드를 여러 DAO 클래스에서 사용할 수 있도록 클래스로 분리 => 클래스명 : JdbcContext
* Connection 객체가 필요한 DataSource 객체는 UserDao가 아닌 JdbcContext가 필요하게 된다. (UserDao에서 아직 JdbcContext 객체를 사용하지 않는 메서드는 일단 논외)

빈 의존관계 변경
UserDao -> JdbcContext를 의존하고 있음. 하지만 기존의 의존관계와 다르게 UserDao가 의존하고 있는 JdbcContext는 인터페이스가 아니라 구체 클래스임.
    * JdbcContext는 그 자체로 독립적인 JDBC 컨텍스트(로직의 구현)를 제공해주는 서비스 오브젝트로 의미가 있을 뿐 구현 방법이 바뀔 가능성이 없음.

### 3.4.2 JdbcContext의 특별한 DI  
JdbcContext를 Bean 객체로 만든 이유
* JdbcContext를 싱글톤으로 관리하기 위해서 : JdbcContext는 DataSource 객체를 멤버변수로 가지지만 해당 값은 읽기 전용 값으로 상태 변화가 없음으로 JdbcContext는
상태를 가지지 않는다.
* JdbcContext 가 DI를 통해서 다른 빈(UserDao, AccountDao, MessageDao 등)에 의존하고 있기 때문인다.
* DI를 위해서는 주입되는 오브젝트와 주입받는 오브젝트 양쪽 모두 스프링 빈으로 등록돼야 한다.
* DB 인터페이스를 JDBC로 가져가게 되면 MySQL, MSSQL, Oracle 등의 어떤 DB로 교체해도 JdbcContext를 사용할 수 있으며 DB인터페이스가 변경되는 경우(JPA, 하이버네이트)
에는 JdbcContext 자체를 교체해야하기 때문에 굳이 인터페이스로 둘 이유가 없다.

코드를 이용하는 수동 DI
* UserDao와 JdbcContext는 그 관계가 긴밀한 만큼 JdbcContext를 스프링 컨테이너에 빈으로 등록하지 않고 UserDao의 DataSource의 Setter 메서드에서
JdbcContext 객체를 생성하여 파라미터로 전달받은 DataSource를 JdbcContext에 DI할 수도 있다.
* 해당 방법은 JdbcContext를 Dao(UserDao, AccountDao, MessageDao 등) 마다 JdbcContext를 만드는 방식이 된다.
* 장점 : DI의 근본적인 원칙에 부합하지 않는 구체적인 클래스와의 관계가 설정에 드러나지 않음.
* 단점 : JdbcContext 싱글톤 객체로서 사용 불가능.

## 3.5 템플릿과 콜백
전략 패턴의 컨텍스트를 <b>템플릿</b>이라 부르고, 익명 내부 클래스로 만들어지는 오브젝트를 <b>콜백</b>이라고 부른다.

### 3.5.1 템플릿/콜백의 동작원리 
콜백 : 실행되는 것을 목적으로 다른 오브젝트의 메서드에 전달되는 오브젝트를 말한다. 파라미터로 전달되지만 값을 참조하기 위한 것이 아니라 특정 로직을 담은
메서드를 실행시키기 위해 사용된다. 자바에서 메서드 자체를 파라미터로 전달할 방법이 없기에 메서드가 담긴 오브젝트를 전달해야 한다. 그래서 <b>functional object</b>라고 한다.
java 8부터 interface(단일 메서드) + 람다(lambda)를 통해서 콜백으로 메서드를 직접 넘기는 것처럼 보이게하는 것이 가능해졌다. (java 8 + spring4 이상부터 지원)

템플릿 : 고정된 작업 흐름을 가진 코드를 재사용한다는 의미에서 붙인 이름
* 기존 전략 패턴이 클래스 범위에서 DI를 했다면 템플릿/콜백 패턴은 메서드 범위에서 DI를 한다.

템플릿/콜백 패턴의 일반적인 작업 흐름
1. 클라이언트에서 Callback 생성 
2. 템플릿 메서드를 호출하면서 Callback 전달
3. 템플릿 메서드 내부에서 Workflow 시작
4. 콜백에서 참조할 참조 정보 생성
5. Callback 메서드 호출하면서 참조정보 전달
6. 콜백 메서드에서 Client의 final 변수 참조
7. 콜백 내부 작업 수행
8. 콜백 작업의 결과 리턴
9. 템플릿의 Workflow 진행
10. 템플릿 Workflow 마무리

일반적인 DI 방식은 오브젝트를 다른 오브젝트에 Setter를 통해 주입하여 사용하다. 하지만 템플릿/콜백 패턴은 아직 구현이 안된 오브젝트를 
템플릿 메서드에 구현과 동시에 주입하는 방식으로 DI를 진행한다.

콜백 오브젝트가 내부 클래스로서 자신을 생성한 클라이언트 메서드 내의 정보를 직접 참조가 가능하다.

<b>JdbcContext에 적용된 템플릿/콜백</b>
Client : UserDao.add() 메서드
* Callback 인터페이스인 StatementStrategy 인터페이스를 익명클래스로 구현

Callback : StatementStrategy 인터페이스(메서드가 1개인 인터페이스 = Functional Interface) 
* User 오브젝트는 Client인 UserDao.add() 메서드에서, Connection 오브젝트는 탬플릿 메서드인 JdbcContext.workWithStatementStrategy()
메서드에서 직접 참조.

Template : JdbcContext.workWithStatementStrategy() 메서드
* 익명클래스로 구현된 StatementStrategy 인터페이스 객체를 통해 만들어진 PreparedStatement를 리턴 받아 사용 후 후처리

### 3.5.2 편리한 콜백의 재활용
메서드 호출부분에 매번 StatementStrategy 인터페이스 객체를 구현하기 때문에 상대적으로 코드를 작성하고 읽기가 조금 불편하다.

<b>콜백의 분리와 재활용</b>
코드의 복잡함을 유발하는 익명 내부 클래스 사용을 최소화
UserDao 클래스의 deleteAll() 메서드에서 익명 내부 클래스 사용 부분 메서드로 분리 
```java
/* 클라이언트 */
public void deleteAll() throws SQLException {
    executeSql("delete from users");
}

private void executeSql(final String query) throws SQLException {
    jdbc.workWithStatementStrategy(c -> c.prepareStatement(query));
}
```
executeSql 메서드에서 쿼리를 제외한 나머지 부분은 모두 고정적이기 때문에 쿼리는 매개변수로 받는 것으로 수정하였다.   
매개변수는 final 처리를 하여 콜백 메서드에서 직접 접근이 가능하도록 처리하였다.

<b>콜백과 템플릿의 결합</b>
executeSql 메서드는 범용성이 큼으로 UserDao 뿐만 아니라 다른 DAO 클래스에서도 사용이 가능하도록 JdbcContext 클래스로 옮긴다.

이제 UserDao의 deleteAll() 메서드를 호출시 다음과 같아진다.
1. <b>Client</b>인 executeSql() 메서드 호출

2. executeSql() 메서드에서 전달받은 매개변수인 쿼리를 <b>콜백</b> 익명 내부 클래스에서 생성 및 사용

3. <b>템플릿</b> 메서드인 workWithStatementStrategy()에 <b>콜백</b> 익명 내부 클래스 주입

executeSql 메서드를 JdbcContext로 이동시켜 UserDao에서는 데이터베이스에서 모든 Row를 delete 하기 위한 기능을 사용하기 위해서 구체적인 구현과 내부의 전략 패턴이나
코드에 의한 DI, 익명 내부 클래스 사용과 같은 기술들은 최대한 감춰두고, 필요한 기능을 제공하는 단순한 메서드로 해당 기능을 수행할 수 있게 되었다.

### 3.5.3 템플릿/콜백의 응용
스프링에는 다양한 자바 엔터프라이즈 기술에서 사용할 수 있도록 만들어져 제공되는 수십 가지 템플릿/콜백 클래스와 API가 있으니 잘 알아두고 사용할 줄 아는 것이 중요하다.

고정된 작업 흐름을 갖고 있으면서 여기저기서 자주 반복되는 코드가 있다 -> 메서드로 추출한다.

그중 일부 작업을 필요에 따라 바꾸어 사용해야 한다면 -> 바꾸어서 사용해야하는 작업을 인터페이스로 정리하고 
인터페이스를 사이에 두고 분리해서 전략 패턴을 적용하고 DI로 의존관계를 관리

바꾸어서 사용해야하는 작업이 한 클래스 내에서 메서드마다 모두 다르다면 -> 템플릿/콜백 패턴 적용

가장 전형적인 템플릿/콜백 패턴의 후보 : try/catch/finally 블록을 사용하는 코드

<b>테스트와 try/catch/finally</b>
템플릿/콜백 예제 만들어보기 
* numbers.txt 파일 준비   
```text
1
2
3
4
```

* numbers.txt 파일의 라인 숫자를 모두 합산하여 결과를 돌려주는 클래스 생성 및 테스트 클래스 작성
(main 아래 resources 디렉터리 혹은 test 아래 resources 디렉터리에 위치 시킨다.)

테스트 클래스 : CalcSumTest
* 주의 : `getClass().getResource()`의 매개변수로 책에서는 "numbers.txt" 를 넘겨주지만 실제로는 <b>"/numbers.txt"</b> 로 앞에 루트 경로를
표시해주어야 NullPointerException 이 나지 않는다. 
```java
public class CalcSumTest {

    @Test
    public void sumOfNumbers() throws IOException {
        Calculator calculator = new Calculator();
        int sum = calculator.calcSum(getClass().getResource("/numbers.txt").getPath());

        assertThat(sum, is(10));
    }
}
```

Caculator 클래스 : 파일을 읽는 도중 에러가 나도 파일을 반드시 닫아주도록 finally에 `br.close();` 선언
```java
public class Calculator {

    public Integer calcSum(String filepath) throws IOException { 
        BufferedReader br = null;
         try {
            Integer sum = 0;
            br = new BufferedReader(new FileReader(filepath));
            
            while ((line = br.readLine()) != null) {
                sum += Integer.valueOf(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             }
        }
        return sum;
    }
}
```

<b>중복의 제거와 템플릿/콜백 설계</b>
이번에는 파일에 있는 모든 숫자의 곱을 계산하는 기능을 추가해야 한다는 요구가 발생하였다. 
파일을 읽어서 처리하는 비슷한 기능이 새로 필요하게 될 시에 앞서 만든 코드를 복사/붙여넣기 해서 만들어서는 안된다.

```java
public Integer calcMultiply(String filepath) throws IOException { 
    BufferedReader br = null;
     try {
        Integer multiply = 0;       // 바뀌는 부분 : sum -> multiply
        br = new BufferedReader(new FileReader(filepath));
        
        while ((line = br.readLine()) != null) {
            multiply *= Integer.valueOf(line);  // 바뀌는 부분 : += -> *=
        }
    } catch (IOException e) {
        e.printStackTrace();
        throw e;
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
    }
    return multiply;
}
```
템플릿/콜백 패턴 적용하기 위해서 살펴볼 점 
* 템플릿에 담을 반복되는 작업 흐름은 어떤 것인지 살펴본다 : bufferedReader 생성 및 파일 라인을 읽기위한 while문, try/catch/finally 부분
* 템플릿이 콜백에게 <b>전달해줄 내부의 정보</b>는 무엇이고, 콜백이 템플릿에게 <b>돌려줄 내용</b>은 무엇인지도 생각 : 내부정보 - BufferedReader / 돌려줄 내용 - 연산의 결과
* 템플릿이 작업을 마친 뒤 클라이언트에게 전달해줘야 할 것도 생각 : 연산의 결과
    
템플릿 메서드 분리
```java
public class Calculator {
    ...
    
    /* 템플릿(=컨텍스트) 메서드 분리 */
    public Integer fileReadTemplate(String filepath, BufferedReaderCallback callback) throws IOException {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filepath));
            return callback.doSomethingWithReader(br);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
```

콜백 메서드 분리
```java
@FunctionalInterface    /* 하나의 메서드만 가지는 콜백 인터페이스 선언 */
public interface BufferedReaderCallback {
    Integer doSomethingWithReader(BufferedReader br) throws IOException;
}
```

calcSum 메서드에 템플릿/콜백 패턴 구현
```java
public class Calculator {

    /* 클라이언트 */
    public Integer calcSum(final String filepath) throws IOException {

        /* 콜백 메서드 사용 */
        return fileReadTemplate(filepath, new BufferedReaderCallback() {
            @Override
            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
                Integer sum = 0;
                String line;
                while ((line = br.readLine()) != null) {
                    sum += Integer.valueOf(line);
                }
                return sum;
            }
        });
    }
    
    ...
}
```
<b>템플릿/콜백의 재설계</b>
calcMultiply() 메서드와 calcSum() 메서드는 처음 초기값 및 반복 연산을 제외하면 유사하다. 따라서 차이를 보이는 초기값 및 연산은 파라미터로 받는 것으로 
변경한다.
```java
public Integer calcSum(String filepath) throws IOException {
    return lineReadTemplate(filepath, 0, (line, value) -> value + Integer.parseInt(line));
}

public Integer calcMultiply(String filepath) throws IOException {
    return lineReadTemplate(filepath, 1, (line, value) -> value * Integer.parseInt(line));
}

public Integer lineReadTemplate(String filepath, int initValue, LineCallback callback) throws IOException {
    BufferedReader br = null;

    try {
        br = new BufferedReader(new FileReader(filepath));
        Integer res = initValue;
        String line;
        while ((line = br.readLine()) != null) {
            res = callback.doSomethingWithLine(line, res);
        }
        
        return res;
    } catch (IOException e) {
        System.out.println(e.getMessage());
        throw e;
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
```
라인 콜백 함수
```java
@FunctionalInterface
interface LineCallback {
    Integer doSomethingWithLine(String line, int value);
}
```

<b>제네릭스를 이용한 콜백 인터페이스</b>
Java 5에서 추가된 제네릭 특성을 통해서 조금 더 범용적인 콜백 클래스를 만들 수 있다.
```java
@FunctionalInterface
public inferface LineCallback<T> {
    T doSomethingWithLine(String line, T value);
}
```

## 3.6 스프링의 JdbcTemplate
스프링은 JDBC를 이용하는 DAO에서 사용할 수 있도록 준비된 다양한 템플릿과 콜백을 제공한다.

스프링이 제공하는 JDBC 코드용 기본 템플릿은 <b>JdbcTemplate</b>이다.

### 3.6.1 update()
```java
private JdbcTemplate jdbcTemplate;

public void deleteAll() {
    jdbcTemplate.update("delete from users");
}

public void add(final User user) {
    jdbcTemplate.update("insert into users(id, name, password) values (?, ?, ?)",
            user.getId(), user.getName(), user.getPassword());
}
```

### 3.6.2 ~~queryForInt()~~ queryForObject()
```java
public int getCount() {
    return jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
}
```

### 3.6.3 queryForObject()


## 테스트 보완
### 네거티브 테스트
* 예외상황에 대한 테스트, 예를 들어 UserDao 클래스의 `get(String id)` 메서드 호출 시에 id가 없을 때는 어떻게 되는지,  
`List<User> getAll()` 메서드 호출시 결과가 하나로 없는 경우에는 어떻게 되는지를 검증하는 것이 네거티브 테스트이다.

* 같은 개발자가 만든 조회용 메서드들에 대해서도 어떤 메서드는 데이터가 없으면 null을 리턴하고,  
어떤 메서드는 빈(empty) 리스트 오브젝트를 리턴하고, 어떤 메서드는 예외를 던지고, 어떤 메서드는 NullPointerException  
같은 런타임 예외가 발생하면서 뻗어버리기도 한다. 그래서 미리 예외상황에 대한 일관성 있는 기준을 정해두고 이를 테스트로 만들어 검증해둬야 한다.

### UserDao의 getAll() 메서드 테스트
* `List<User> getAll()` 메서드 내부에서 JdbcTemplate 클래스의 `query()` 메서드를 사용하며 해당 메서드가  
예외적인 경우에는 크기가 0인 리스트 오브젝트를 리턴한다.
```java
    @Test
    public void getAllTest() {
        dao.deleteAll();
        /* 
         * dao.getAll 메서드가 query 메서드 사용으로 인해 예외발생 시에 이미 size가 0인 list 객체를  
         * 반환할 것을 알고 있다. 하지만 assertThat 메서드를 통해서 검증하고 있다.
         */
        List<User> users0 = dao.getAll();
        assertThat(users0.size(), is(0));
        ...
    }
```

* UserDao를 사용하는 사용자의 입장에서는 `getAll()` 메서드가 내부적으로 JdbcTemplate을 사용하지는, 개발자가  
직접 만든 JDBC 코드를 사용하는지 알 수 없기 때문에 getAll의 예외로 0이 나오는지는 위의 코드와 같이 명시적으로  
검증해줄 필요가 있다. => DAO의 테스트 클래스의 경우 각 예외 사항 및 정상 동작에 대한 검증을 넣는 방향으로 테스트 클래스를 작성한다.

* 스프링에서 지원하는 API의 경우 클래스 이름이 Template으로 끝나거나 인터페이스 이름이 Callback으로 끝난다면  
템플릿/콜백 패턴이 적용된 것으로 보면 된다.

## 정리
* 3장에서는 예외처리 및 안전한 리소스 반환을 보장해주는 DAO 코드를 만들었다.

* JDBC와 같이 예외가 발생할 가능성이 있으며 공유 리소스의 반환이 필요한 코드는 반드시 try/catch/finally 블록으로 관리해야 한다.

* 컨텍스트는 별도의 빈으로 등록해서 DI 받거나 클라이언트 클래스에서 직접 생성해서 사용한다.

* 콜백의 코드에도 일정한 패턴이 반복된다면 콜백을 템플릿(필드 변수 및 초기화)에 넣고 재활용하는 것이 편리하다.

* 템플릿과 콜백의 타입이 다양하게 바뀔 수 있다면 제네릭스(Generics)를 이용한다.

* 템플릿/콜백을 설계할 때는 템플릿과 콜백 사이에 주고받는 정보에 관심을 둬야한다.