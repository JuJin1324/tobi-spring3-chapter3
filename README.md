# 3장 템플릿
토비의 스프링 3.1 Vol.1 스프링 이해와 원리

## 기초 셋팅
* [1장 오브젝트와 의존관계](https://github.com/JuJin1324/tobi-spring3-chapter1)의
기초 셋팅을 모두 가져옴(Maven 포함)

## 패턴을 통한 PreparedStatement 분리
* chapter1에서 UserDao -> Connection 의존관계를 탬플릿 메서드 패턴 적용 이후 전략 패턴으로 수정하였음.
* 이번에는 UserDao -> PreparedStatement 의존관계를 수정해보겠음.

### 템플릿 메서드 패턴
* 템플릿 메서드 패턴은 상속을 통해 기능을 확장해서 사용하는 패턴.
   * 기능 확장이 필요한 오브젝트(여기서는 PreparedStatment) 생성 부분을 별도의 메서드로 분리
   * 메서드를 abstract(추상) 메서드로 변경
   * UserDao 클래스의 하위클래스 생성(DeleteAllUserDao) 및 상속
   * 하위클래스인 DeleteAllUserDao에서 PreparedStatement 생성 메서드(2번의 추상메서드)를 구현.

### 전략 패턴 
* 기능 확장이 필요한 오브젝트를 외부 인터페이스를 통해서 주입받도록 함. 
* 클래스 레벨에서는 인터페이스를 통해서만 의존하도록 만드는 패턴.

* 컨텍스트(Context) : 전반적 기능이 동작하는 클래스 ex) UserDao 혹은 메서드
* 전략(Strategy) : 기능 확장이 가능하도록 컨텍스트 내부에 선언된 인터페이스 ex) StatementStrategy
* 클라이언트(Client) : 컨텍스트 클래스에 전략 인터페이스를 상속받은 클래스 중 선택하여 DI(의존 주입)을 해주는 클래스 혹은 설정파일  
ex) DaoFactory, applicationContext.xml

* <b>DI</b>의 가장 중요한 개념은 제 3자의 도움을 통해 두 오브젝트 사이의 유연한 관계가 설정되도록 만든다는 것이다.

### 익명 내부 클래스
* 선언방식 : `new 인터페이스이름() { 클래스 본문 }`
* ※ 주의 : spring framework 3 버전에서는 람다식을 사용하지 않는다. 사용시 junit test에서 ArrayIndexOutOfBoundsException 발생.

## 탬플릿과 콜백
* 전략 패턴의 컨텍스트를 <b>템플릿</b>이라 부르고, 익명 내부 클래스로 만들어지는 오브젝트를 <b>콜백</b>이라고 부른다.

### 콜백(callback) 
* 실행되는 것을 목적으로 다른 오브젝트의 메서드에 전달되는 오브젝트
* 파라미터로 전달되지만 값을 참조하기 위한 것이 아니라 <b>특정 로직을 담은 메서드를 실행</b>시키기 위해 사용한다.

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