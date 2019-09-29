package study.tobi.spring3.chapter3.user;

import org.junit.Before;
import org.junit.Test;
import study.tobi.spring3.chapter3.template.Calculator;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-28
 */
public class CalcSumTest {

    private String filepath;
    private Calculator calculator;

    @Before
    public void setUp() {
        filepath = getClass().getResource("/numbers.txt").getPath();
        calculator = new Calculator();
    }

    @Test
    public void sumOfNumbers() throws IOException {
        assertThat(calculator.calcSum(filepath), is(10));
    }

    @Test
    public void multiplyOfNumbers() throws IOException {
        assertThat(calculator.calcMultiply(filepath), is(24));
    }

    @Test
    public void concatenateStrings() throws IOException {
        assertThat(calculator.concatenate(filepath), is("1234"));
    }
}
