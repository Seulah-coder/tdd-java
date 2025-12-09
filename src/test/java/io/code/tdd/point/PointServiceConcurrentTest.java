package io.code.tdd.point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

import io.code.tdd.database.UserPointTable;
import io.code.tdd.exception.InsufficientBalanceException;
import io.code.tdd.exception.MaxBalanceExceededException;
import io.code.tdd.database.PointHistoryTable;

@SpringBootTest
public class PointServiceConcurrentTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Test
    void concurrent_charge_test() throws InterruptedException {
    long userId = 1L;
    userPointTable.insertOrUpdate(userId, 0L);

    int threads = 50;
    ExecutorService executor = Executors.newFixedThreadPool(threads); // 스레드 풀 생성
    CyclicBarrier barrier = new CyclicBarrier(threads); // 모든 스레드가 동시에 시작하도록 설정
    CountDownLatch doneLatch = new CountDownLatch(threads); // 모든 스레드가 종료될 때까지 대기

    for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + " 준비 완료");
                barrier.await(); // 모든 스레드 동시에 시작
                System.out.println(Thread.currentThread().getName() + " 실행 시작");
                pointService.chargePoint(userId, 100);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });
    }

    doneLatch.await(); // 모든 스레드 종료 대기

    UserPoint finalPoint = pointService.getUserPoint(userId);
    System.out.println("최종 포인트: " + finalPoint.point());
    assertEquals(threads * 100, finalPoint.point());
    }
    
}
