package io.code.tdd.point;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.code.tdd.database.PointHistoryTable;
import io.code.tdd.database.UserPointTable;
import io.code.tdd.exception.InsufficientBalanceException;
import io.code.tdd.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> getUserPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint chargePoint(long userId, long amount) {
    
    UserPoint currentPoint = Optional.ofNullable(
        userPointTable.selectById(userId)
        ).orElseGet(() -> UserPoint.empty(userId));

    long newAmount = currentPoint.point() + amount;
    UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);

    pointHistoryTable.insert(
        userId,
        amount,
        TransactionType.CHARGE,
        System.currentTimeMillis()
    );

    return updatedPoint;
    }

    public UserPoint usePoint(long userId, long amount) {
            UserPoint currentPoint = Optional.ofNullable(
            userPointTable.selectById(userId)
        )
        .orElseThrow(() -> new UserNotFoundException(userId));

        if (currentPoint.point() < amount) {
        throw new InsufficientBalanceException(
                userId,
                amount
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
