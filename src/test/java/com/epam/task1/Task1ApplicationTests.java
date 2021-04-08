package com.epam.task1;

import com.epam.task1.solution.SolutionController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

/**
 * to be completed
 *
 * @Author shark
 */
@SpringBootTest
class Task1ApplicationTests {

    private SolutionController so = new SolutionController();

    @Test
    void contextLoads() {
    }

    @Test
    void testWrongInput() {
        for (int i = 0; i < 10; i++) {
            Optional a = null;
            try {
                Optional<String> a1 = so.getTemperature("南京", "江苏", "南京");
                Optional<String> a2 = so.getTemperature("安徽", "南京", "南京");
                Optional<String> a3 = so.getTemperature("江苏", "南京", "苏州");
                Assertions.assertEquals("省份不存在", a1.get(), "失败");
                Assertions.assertEquals("该省不存在这个城市", a2.get(), "失败");
                Assertions.assertEquals("该城市不存在这个县", a3.get(), "失败");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testWithOutNet() {
        for (int i = 0; i < 10; i++) {
            Optional a = null;
            try {
                Optional<String> a1 = so.getTemperature("江苏", "南京", "南京");
                Assertions.assertEquals("请求超时", a1.get(), "测试失败请断网后测试");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testRightInput() {
        for (int i = 0; i < 300; i++) {
            Optional a = null;
            try {
                Optional a1 = so.getTemperature("江苏", "苏州", "苏州");
                Optional a2 = so.getTemperature("安徽", "宿州", "砀山");
                Optional a3 = so.getTemperature("江苏", "南京", "南京");
                System.out.println("执行结果苏州温度：" + a1);
                System.out.println("执行结果砀山温度：" + a2);
                System.out.println("执行结果南京温度：" + a3);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int threadCount = 100; //子线程数


    /**
     * 多线程测试是否会阻塞，返回服务器繁忙表示tps已达到限定值100
     */
    @Test
    public void testTps() {

        for (int a = 1; a <= threadCount; a++) {
            Mythread mythread = new Mythread(a);
            Thread thread = new Thread(mythread);
            thread.start();
        }


    }

    class Mythread implements Runnable {

        private SolutionController so = new SolutionController();


        private int a;

        public Mythread(int a) {
            this.a = a;
        }

        @Override
        public void run() {
            //执行业务代码
            try {

                for (int i = 0; i < 10000; i++) {
                    Optional a = so.getTemperature("江苏", "南京", "六合");
                    Assertions.assertEquals("服务器繁忙，请稍后再试",a.get(),"线程"+this.a+"第"+i+"次请求成功"+a);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
