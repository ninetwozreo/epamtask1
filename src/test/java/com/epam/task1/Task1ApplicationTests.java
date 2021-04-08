package com.epam.task1;

import com.epam.task1.solution.SolutionController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class Task1ApplicationTests {


    @Test
    void contextLoads() {
    }

//    @Test
//    void testTemp() {
//        for (int i = 0; i < 100; i++) {
//            Optional a = so.getTemperature("安徽", "宿州", "砀山s");
//            System.out.println("执行结果" + a);
//        }
//    }

    private int threadCount = 1; //子线程数


    /**
     * 多线程测试是否会阻塞，返回服务器繁忙表示tps已达到限定值
     * */
    @Test
    public void testTps() {
        for (int a = 1; a <= threadCount; a++) {
            Mythread mythread = new Mythread(a);
            Thread thread = new Thread(mythread);
            thread.start();
        }
        try {
//            countDownLatch.await(); //主线程等待 ,直到countDownLatch 为0
        } catch (Exception e) {

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
            //执行业务代码a
            try {


                for (int i = 0; i < 1000; i++) {
                    Optional a = so.getTemperature("江苏", "南京", "六合");
//                    Optional a = so.getTemperature("安徽", "宿州", "砀山");
                    System.out.println("线程" + this.a + "执行结果" + a);
                    Thread.sleep(50000000000000L);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
