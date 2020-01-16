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

## 3.5 탬플릿과 콜백
전략 패턴의 컨텍스트를 <b>템플릿</b>이라 부르고, 익명 내부 클래스로 만들어지는 오브젝트를 <b>콜백</b>이라고 부른다.

### 콜백(callback) 
실행되는 것을 목적으로 다른 오브젝트의 메서드에 전달되는 오브젝트   
파라미터로 전달되지만 값을 참조하기 위한 것이 아니라 <b>특정 로직을 담은 메서드를 실행</b>시키기 위해 사용한다.

* 자바에선 메서드 자체를 파라미터로 전달할 방법이 없기 때문에 메서드가 담긴 오브젝트를 전달해야한다.
* java 8부터 interface(단일 메서드) + 람다(lambda)를 통해서 콜백으로 메서드처럼 넘기는 것이 가능해졌다.
(spring3 에서는 지원 안함)

* 콜백은 메서드 실행을 목적으로 넘기는 오브젝트이기 때문에 <b>functional object</b> 라고도 한다.

* 기존 전략 패턴이 클래스 범위에서 DI를 했다면 템플릿/콜백 패턴은 메서드 범위에서 DI를 한다.

### 코드 정리
* 고정된 작업 흐름을 갖고 있으면서 여기저기서 자주 반복되는 코드 -> 메서드로 분리

* 분리한 메서드 중 필요에 따라바꾸어 사용해야하는 경우가 있다면 -> 인터페이스를 사이에 두고 분리해서  
전략 패턴을 적용하고 DI로 의존관계 관리

* 바뀌는 부분이 애플리케이션 안에서 동시에 여러 종류가 만들어질 수 있다면 -> 템플릿/콜백 패턴 사용  
가장 전형적인 템플릿/콜백 패턴의 후보는 try/catch/finally 블록을 사용하는 코드이다.

## 템플릿 콜백 설계
* 테스트 클래스 기초 설계
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

* numbers.txt
```text
1
2
3
4
```

* Caculator 클래스
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
  
* 추가 요구 사항 발생
    - 파일에 있는 숫자들을 모두 곱해서 결과 값을 돌려주는 메서드 추가 요청
    - 해당 메서드 추가시 다음과 같음
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
* 템플릿/콜백 패턴 적용하기 위해서 살펴볼 점 
    - 템플릿에 담을 반복되는 작업 흐름은 어떤 것인지 살펴본다.
    - 템플릿이 콜백에게 <b>전달해줄 내부의 정보</b>는 무엇이고, 콜백이 탬플릿에게 <b>돌려줄 내용</b>은 무엇인지도 생각
    - 템플릿이 작업을 마친 뒤 클라이언트에게 전달해줘야 할 것도 생각
    
* 메서드 내부의 공통 부분
    - try/catch/finally 구문 모두 똑같이 사용 -> 탬플릿 부분
    - BufferedReader 사용
    - `while` 및 `String line` 변수를 통해 파일 읽기
    
* 다른 부분
    - 결과값 변수 : sum, multiply -> res 변수명으로 통일 가능
    - 결과도출 연산식 : `+=`, `*=` -> 연산이 바뀌는 부분에 대해서 콜백 메서드 사용
    - 해당 연산식을 콜백 메서드로 사용하기 위해서는 line의 각각 내용이 필요  
     -> 하지만 각각 line의 내용을 전달하기에는 계속해서 콜백 메서드를 불러야한다.
     -> 파일의 모든 내용을 담고 있는 BufferedReader를 콜백 메서드로 넘기는게 편할거 같다.
     -> 되돌려줄 값으로 연산의 결과를 넘겨준다.

* 템플릿 메서드 분리
```java
public class Calculator {
    ...
    
    /* 템플릿(=컨텍스트) 메서드 분리 */
    public Integer fileReadTemplate(String filepath, BufferedReaderCallback callback) throws IOException {
        Integer ret;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filepath));
            ret = callback.doSomethingWithReader(br);
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
        return ret;
    }
}
```

* 콜백 메서드 분리
```java
public interface BufferedReaderCallback {
    Integer doSomethingWithReader(BufferedReader br) throws IOException;
}
```

* calcSum 메서드에 템플릿/콜백 패턴 구현
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

### 다시 공통적인 패턴 발견
* calcSum 및 calcMultiply 메서드의 콜백을 비교
* calSum()    
```java
    Integer sum = 0;
    String line;
    while ((line = br.readLine()) != null) {
        sum += Integer.valueOf(line);
    }
    return sum;
```

* calcMultiply()
```java
    Integer multiply = 1;
    String line;
    while ((line = br.readLine()) != null) {
        multiply *= Integer.valueOf(line);
    }
    return multiply;
```    

* 해당 유사점(결과 변수에 초기값 선언, while/line 을 이용한 파일 읽기, 읽은 line을 통한 연산, 연산결과 반환)  
을 콜백 메서드로 변경

* 콜백 메서드 분리
```java
public interface LineCallback {
    Integer doSomethingWithLine(String line, Integer value);
}
```

* 템플릿 메서드 생성
```java
public Integer lineReadTemplate(String filepath, LineCallback callback, int initValue) throws IOException {
    BufferedReader br = null;
    Integer res = initValue;

    try {
        br = new BufferedReader(new FileReader(filepath));
        String line;
        while ((line = br.readLine()) != null) {
            res = callback.doSomethingWithLine(line, res);
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
    return res;
}
```

* 클라이언트 메서드 변경
```java
    /* 클라이언트 메서드 */
    public Integer calcMultiply(String filepath) throws IOException {
        return lineReadTemplate(filepath, new LineCallback() {
            @Override
            public Integer doSomethingWithLine(String line, Integer value) {
                return value * Integer.valueOf(line);   // 코드 관심이 보다 명확하게 들어남.
            }
        }, 1);
    }
```

## 제네릭스(Generics)을 이용한 콜백 인터페이스
* java 5에서 추가된 제네릭스을 사용하여 메서드에서 반환하는 결과의 타입이 Integer 뿐만이 아닌  
다양한 자료형으로 선택하여 반환하도록 변경할 수 있다.

* LineCallback 인터페이스 변경
```java
public interface LineCallback<T> {
    T doSomethingWithLine(String line, T value);
}
```

* Calculator 클래스의 lineReadTemplate 메서드 제네릭스 적용
```java
    public <T> T lineReadTemplate(String filepath, LineCallback<T> callback, T initValue) throws IOException {
        BufferedReader br = null;
        T res = initValue;
        ...
```

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