package com.epam.task1;

import com.epam.task1.solution.SolutionController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Optional;

/**
 * to be completed
 *
 * @Author shark
 */
@SpringBootTest
class Task1ApplicationTests {

    private SolutionController so = new SolutionController();
    private int threadCount = 100; //子线程数
    private volatile int failTimes = 0; //繁忙次数
    private volatile int reqTimes = 0; //繁忙次数
    private volatile long enTime = 0L; //消耗时间
    private volatile long bt = 0L; //开始时间

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


    /**
     * 多线程测试是否会阻塞，返回服务器繁忙表示tps已达到限定值100
     */
    @Test
    public void testTps() {
        bt=new Date().getTime();

        for (int a = 1; a <= threadCount; a++) {
            Mythread mythread = new Mythread(a);
            Thread thread = new Thread(mythread);
            thread.start();
        }

        try {
            long waiTime = 60000L;
            Thread.sleep(waiTime);
            System.out.println("繁忙次数：" + failTimes);
            System.out.println("共请求次数：" + reqTimes);
            System.out.println("耗时：" + enTime / 1000 + "秒");
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            //business
            try {
                //every thread get 1500 req
                for (int i = 0; i < 1500; i++) {
                    Optional a = so.getTemperature("江苏", "南京", "六合");
                    if (a.get().equals("服务器繁忙，请稍后再试")) {
                        failTimes++;
                    }
                    reqTimes++;
                    long curt = new Date().getTime();
                    enTime= curt-bt;
//                    Assertions.assertEquals("服务器繁忙，请稍后再试", a.get(), "线程" + this.a + "第" + (i + 1) + "次请求成功,温度：" + a.get());
//                    Assertions.assertEquals("请求超时", a.get(), "线程" + this.a + "第" + (i + 1) + "次请求成功,温度：" + a.get());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
