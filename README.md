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

* 컨텍스트(Context) : 전반적 기능이 동작하는 클래스 ex) UserDao
* 전략(Strategy) : 기능 확장이 가능하도록 컨텍스트 내부에 선언된 인터페이스 ex) StatementStrategy
* 클라이언트(Client) : 컨텍스트 클래스에 전략 인터페이스를 상속받은 클래스 중 선택하여 DI(의존 주입)을 해주는 클래스 혹은 설정파일  
ex) DaoFactory, applicationContext.xml

* <b>DI</b>의 가장 중요한 개념은 제 3자의 도움을 통해 두 오브젝트 사이의 유연한 관계가 설정되도록 만든다는 것이다.


