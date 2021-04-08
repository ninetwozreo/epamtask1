package com.epam.task1.solution;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
/**
 * @Author shark
 * 用于控制 访问峰值的服务 的令牌管理中心
 */

public class TokenServer {


    /**
     * 每秒最多请求数量
     */
    private int maxFlowRate;

    /**
     * 每秒平均请求数量
     */
    private int avgFlowRate;

    /**
     * 队列来缓存桶数量
     */
    private LinkedBlockingQueue<Byte> tokenQueue;

    /**
     * 由定时任务持续生成令牌。这样的问题在于会极大的消耗系统资源，如，某接口需要分别对每个用户做访问频率限制。
     * 假设系统中存在6W用户，则至多需要开启6W个定时任务来维持每个桶中的令牌数，这样的开销是巨大的。
     * 可以做成延迟计算的形式，每次请求令牌的时候，看当前时间是否晚与下一次生成令牌的时间，计算该段时间的令牌数，
     * 加入令牌桶，更新数据。
     */
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * The Mutex do not use directly.
     */
    private volatile Object mutexDoNotUseDirectly = new Object();
    /**
     * The Is start.
     */
    private volatile boolean isStart = false;

    /**
     * The constant A_CHAR.
     */
    private static final Byte A_CHAR = 'a';

    /**
     * Instantiates a new Token bucket.
     */
    private TokenServer() {
    }

    /**
     * New builder token bucket.
     *
     * @return the token bucket
     */
    public static TokenServer newBuilder() {
        return new TokenServer();
    }

    /**
     * 每秒内最大请求数量设置
     *
     * @param maxFlowRate 每秒内最大请求数量
     * @return 当前令牌同
     */
    public TokenServer maxFlowRate(int maxFlowRate) {
        this.maxFlowRate = maxFlowRate;
        return this;
    }

    /**
     * 每秒平均请求数量设置
     *
     * @param avgFlowRate 每秒平均请求数量
     * @return 当前令牌同
     */
    public TokenServer avgFlowRate(int avgFlowRate) {
        this.avgFlowRate = avgFlowRate;
        return this;
    }

    /**
     * 构造者模式
     *
     * @return the token bucket
     */
    public TokenServer build() {
        //初始化
        init();
        //返回当前对象
        return this;
    }

    /**
     * 初始化
     */
    private void init() {
        //初始化桶队列大小
        if (maxFlowRate > 0) {
            tokenQueue = new LinkedBlockingQueue<>(maxFlowRate);
        }
        //初始化令牌生产者
        TokenProducer tokenProducer = new TokenProducer(avgFlowRate, this);
        //每秒执行一次增加令牌操作
        scheduledExecutorService.scheduleAtFixedRate(tokenProducer, 0, 1, TimeUnit.SECONDS);
        //系统启动
        isStart = true;

    }

    /**
     * 停止任务
     */
    public void stop() {
        isStart = false;
        scheduledExecutorService.shutdown();
    }

    /**
     * 查看任务是否执行
     *
     * @return the boolean
     */
    public boolean isStarted() {
        return isStart;
    }

    /**
     * 增加令牌
     *
     * @param tokenNum the token num
     */
    private void addTokens(Integer tokenNum) {
        // 若是桶已经满了，就不再家如新的令牌
        for (int i = 0; i < tokenNum; i++) {
            tokenQueue.offer(A_CHAR);
        }
    }

    /**
     * 获取令牌
     * true:获取到1个令牌，非阻塞
     * false:未获取到令牌，非阻塞
     * @return boolean
     */
    public boolean tryAcquire() {
        synchronized (mutexDoNotUseDirectly) {
            // 否存在足够的桶数量
            if (tokenQueue.size() > 0) {
//                System.out.println("当前令牌数量"+tokenQueue.size());
                //队列不为空时返回队首值并移除,队列为空时返回null。非阻塞立即返回。
                Byte poll = tokenQueue.poll();
                if (poll != null) {
                    //获取到令牌
                    return true;
                }
            }
        }
        //未获取到令牌
        return false;
    }

    /**
     * 令牌生产者 <br>
     */
    private class TokenProducer implements Runnable {

        /**
         * 每次加入令牌的数量
         */
        private int tokenNum;
        /**
         * 当前令牌桶
         */
        private TokenServer TokenServer;

        /**
         * 令牌生产者构造方法
         *
         * @param tokenNum    每次加入令牌的数量
         * @param TokenServer 当前令牌桶
         */
        private TokenProducer(int tokenNum, TokenServer TokenServer) {
            this.tokenNum = tokenNum;
            this.TokenServer = TokenServer;
        }

        @Override
        public void run() {
            //增加令牌
//            System.out.println("增加了"+tokenNum);
            TokenServer.addTokens(tokenNum);
        }
    }

}
