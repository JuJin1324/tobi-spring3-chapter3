package study.tobi.spring3.chapter3.template;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-28
 */

public class Calculator {

    /* 클라이언트 */
    public Integer calcSum(final String filepath) throws IOException {

        /* 콜백 메서드 사용 */
//        return fileReadTemplate(filepath, new BufferedReaderCallback() {
//            @Override
//            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
//                Integer sum = 0;
//                String line;
//                while ((line = br.readLine()) != null) {
//                    sum += Integer.valueOf(line);
//                }
//                return sum;
//            }
//        });
        return lineReadTemplate(filepath, new LineCallback<Integer>() {
            @Override
            public Integer doSomethingWithLine(String line, Integer value) {
                return value + Integer.valueOf(line);
            }
        }, 0);
    }

    /* 클라이언트 */
    public Integer calcMultiply(String filepath) throws IOException {
//        return fileReadTemplate(filepath, new BufferedReaderCallback() {
//            @Override
//            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
//                Integer multiple = 1;
//                String line;
//                while ((line = br.readLine()) != null) {
//                    multiple *= Integer.valueOf(line);
//                }
//                return multiple;
//            }
//        });

        return lineReadTemplate(filepath, new LineCallback<Integer>() {
            @Override
            public Integer doSomethingWithLine(String line, Integer value) {
                return value * Integer.valueOf(line);
            }
        }, 1);
    }

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

    public <T> T lineReadTemplate(String filepath, LineCallback<T> callback, T initValue) throws IOException {
        BufferedReader br = null;
        T res = initValue;

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

    public String concatenate(String filepath) throws IOException {
        return lineReadTemplate(filepath, new LineCallback<String>() {
            @Override
            public String doSomethingWithLine(String line, String value) {
                return value + line;
            }
        }, "");
    }
}
