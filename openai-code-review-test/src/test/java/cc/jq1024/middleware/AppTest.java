package cc.jq1024.middleware;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AppTest {

    @Test
    public void test() {
        System.out.println(Integer.parseInt("aaaa"));
        System.out.println(Integer.parseInt("aaaa1"));
        System.out.println(Integer.parseInt("aaaa2"));
        System.out.println(Integer.parseInt("aaaa3"));
    }

}

