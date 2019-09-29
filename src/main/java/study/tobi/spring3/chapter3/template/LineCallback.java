package study.tobi.spring3.chapter3.template;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-29
 */
@FunctionalInterface
public interface LineCallback<T> {
    T doSomethingWithLine(String line, T value);
}
