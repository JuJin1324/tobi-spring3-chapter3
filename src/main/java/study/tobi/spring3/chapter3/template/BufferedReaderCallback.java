package study.tobi.spring3.chapter3.template;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-28
 */
public interface BufferedReaderCallback {

    Integer doSomethingWithReader(BufferedReader br) throws IOException;
}
