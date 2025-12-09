package io.code.tdd.point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import io.code.tdd.database.UserPointTable;
import io.code.tdd.exception.InsufficientBalanceException;
import io.code.tdd.exception.MaxBalanceExceededException;
import io.code.tdd.database.PointHistoryTable;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @Test
    void getUserPointTest(){
    //given
    UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
    when(userPointTable.selectById(1L)).thenReturn(userPoint);
    //when
    UserPoint point = pointService.getUserPoint(1L);

    //then
    assertEquals(1000L, point.point());
    }

    @Test
    void addPointTest(){

    //given
    long userId = 1L;
    long amount = 100L;

    UserPoint before = new UserPoint(userId, 500L, System.currentTimeMillis());
    UserPoint after = new UserPoint(userId, 600L, System.currentTimeMillis());

    when(userPointTable.selectById(userId)).thenReturn(before);
    when(userPointTable.insertOrUpdate(eq(userId), eq(600L))).thenReturn(after);

    //when
    UserPoint result = pointService.chargePoint(userId, amount);

    //then
    assertEquals(600L, result.point());
    }

    @Test
    void usePointTest(){

        //given
        long userId = 1L;
        long amount = 300L;

        UserPoint before = new UserPoint(userId, 500L, System.currentTimeMillis());
        UserPoint after = new UserPoint(userId, 200L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(before);
        when(userPointTable.insertOrUpdate(eq(userId), eq(200L))).thenReturn(after);

        //when
        UserPoint result = pointService.usePoint(userId, amount);

        //then
        assertEquals(after, result);

    }

    @Test
    void usePoint_InsufficientBalanceException_Test(){

        //given
        long userId = 1L;
        long amount = 600L;

        UserPoint before = new UserPoint(userId, 500L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(before);

        //when & then
        assertThrows(InsufficientBalanceException.class, () -> {
            pointService.usePoint(userId, amount);
        });

    }

    @Test
    void chargePoint_MaxBalanceExceeded_Test() {
        // given
        long userId = 1L;

        UserPoint before = new UserPoint(userId, 9_900L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(before);

        // when & then
        assertThrows(
            MaxBalanceExceededException.class,
            () -> pointService.chargePoint(userId, 200L)
        );
    }

    
}
