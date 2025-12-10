package io.code.tdd.point;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.code.tdd.database.PointHistoryTable;
import io.code.tdd.database.UserPointTable;
import io.code.tdd.exception.InsufficientBalanceException;
import io.code.tdd.exception.MaxBalanceExceededException;
import io.code.tdd.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
    private static final long MAX_BALANCE = 10_000L;

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // //유저별 LOCK 저장 - synchronized
    // private final Map<Long, Object> locks = new ConcurrentHashMap<>();

    // //유저별 LOCK 반환 - synchronized
    //     private Object getLock(long userId) {
    //     return locks.computeIfAbsent(userId, id -> new Object());
    // }

    //유저별 LOCK 저장 - ReentrantLock
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    private ReentrantLock getLock(long userId) {
        return locks.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> getUserPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    // // synchronized 방식
    // public UserPoint chargePoint(long userId, long amount) {
        
    //     synchronized(getLock(userId)){
    //         System.out.println(Thread.currentThread().getName() + " 실행 시작");
    //         UserPoint currentPoint = Optional.ofNullable(
    //         userPointTable.selectById(userId)
    //         ).orElseGet(() -> UserPoint.empty(userId));

    //         if(amount + currentPoint.point() > MAX_BALANCE){
    //             throw new MaxBalanceExceededException(currentPoint.point());
    //         }

    //         long newAmount = currentPoint.point() + amount;
    //         UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);

    //         pointHistoryTable.insert(
    //             userId,
    //             amount,
    //             TransactionType.CHARGE,
    //             System.currentTimeMillis()
    //         );
    //         System.out.println(Thread.currentThread().getName() + " 실행 끝: " + updatedPoint.point());

    //         return updatedPoint;
    //     }
    // }
    //ReentrantLock 방식
    public UserPoint chargePoint(long userId, long amount) {
        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 실행 시작");
            UserPoint currentPoint = Optional.ofNullable(
            userPointTable.selectById(userId)
            ).orElseGet(() -> UserPoint.empty(userId));

            if(amount + currentPoint.point() > MAX_BALANCE){
                throw new MaxBalanceExceededException(currentPoint.point());
            }

            long newAmount = currentPoint.point() + amount;
            UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);

            pointHistoryTable.insert(
                userId,
                amount,
                TransactionType.CHARGE,
                System.currentTimeMillis()
            );
            System.out.println(Thread.currentThread().getName() + " 실행 끝: " + updatedPoint.point());

            return updatedPoint;
        } finally {
            lock.unlock();
        }
    }

    public UserPoint usePoint(long userId, long amount) {
            UserPoint currentPoint = Optional.ofNullable(
            userPointTable.selectById(userId)
        )
        .orElseThrow(() -> new UserNotFoundException(userId));

        if (currentPoint.point() < amount) {
        throw new InsufficientBalanceException(
            currentPoint.point()
        );
    }

    long newAmount = currentPoint.point() - amount;
    UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);

    pointHistoryTable.insert(
        userId,
        amount,
        TransactionType.CHARGE,
        System.currentTimeMillis()
    );

    return updatedPoint;
    }
    
}
