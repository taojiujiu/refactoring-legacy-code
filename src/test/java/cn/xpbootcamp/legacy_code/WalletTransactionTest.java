package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.entity.Order;
import cn.xpbootcamp.legacy_code.repository.OrderRepositoryImpl;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.transaction.InvalidTransactionException;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletTransactionTest {
    WalletTransaction transaction;
    RedisDistributedLock redisDistributedLock;
    OrderRepositoryImpl orderRepository;
    WalletServiceImpl walletService;
    String orderIdTest = "orderIdTest";
    Clock clock;
    Instant now = Instant.now();

    @BeforeEach
    void init() {
        redisDistributedLock = Mockito.mock(RedisDistributedLock.class);
        orderRepository = Mockito.mock(OrderRepositoryImpl.class);
        walletService = Mockito.mock(WalletServiceImpl.class);
        clock = Mockito.mock(Clock.class);
        when(orderRepository.find(anyString())).thenReturn(new Order(orderIdTest, 30));
        setMock(redisDistributedLock);
        when(redisDistributedLock.lock(anyString())).thenReturn(true);
        when(clock.instant()).thenReturn(now);
    }

    @AfterEach
    void resetSingleton() throws Exception {
        Field instance = RedisDistributedLock.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private void setMock(RedisDistributedLock mock) {
        try {
            Field instance = RedisDistributedLock.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(instance, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void should_throw_InvalidTransactionException_when_execute_given_buyerId_is_null() {
        transaction = new WalletTransaction("preAssignedId", null, 2L,  orderIdTest, orderRepository, clock, walletService);
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> transaction.execute());

        String expectedMessage = "This is an invalid transaction";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void should_throw_InvalidTransactionException_when_execute_given_sellerId_is_null() {
        transaction = new WalletTransaction("preAssignedId", 1L, null,  orderIdTest, orderRepository, clock, walletService);
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> transaction.execute());

        String expectedMessage = "This is an invalid transaction";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    //todo: Add amount is lesser than zero test case
    @Test
    void should_throw_InvalidTransactionException_when_execute_given_amount_is_null() {
        // todo: Need mock amount is lesser than amount
        transaction = new WalletTransaction("preAssignedId", 1L, 2L,  orderIdTest, orderRepository, clock, walletService);
        when(orderRepository.find(anyString())).thenReturn(new Order(orderIdTest, -1));
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> transaction.execute());

        String expectedMessage = "This is an invalid transaction";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void should_return_false_when_execute_given_order_is_locked() throws InvalidTransactionException {
        transaction = new WalletTransaction("preAssignedId", 1L, 2L,  orderIdTest, orderRepository, clock, walletService);
        when(redisDistributedLock.lock(anyString())).thenReturn(false);
        assertFalse(transaction.execute());
    }

    @Test
    void should_return_false_when_execute_given_order_created_time_earlier_than_current_time_20_days() throws InvalidTransactionException {
        transaction = new WalletTransaction("preAssignedId", 1L, 2L,  orderIdTest, orderRepository, clock, walletService);
        when(clock.instant()).thenReturn(now.plusMillis(1728000001));
        assertFalse(transaction.execute());
        verify(redisDistributedLock).unlock(anyString());

    }

    @Test
    void should_return_false_when_execut_when_user_balance_not_enough() throws InvalidTransactionException {
        transaction = new WalletTransaction("preAssignedId", 1L, 2L,  orderIdTest, orderRepository, clock, walletService);
        when(walletService.moveMoney("t_preAssignedId", 1L, 2L, 30)).thenReturn(null);
        assertFalse(transaction.execute());
        verify(redisDistributedLock).unlock(anyString());
    }

    @Test
    void should_return_true_when_execut_when_user_balance_enough() throws InvalidTransactionException {
        transaction = new WalletTransaction("preAssignedId", 1L, 2L,  orderIdTest, orderRepository, clock, walletService);
        when(walletService.moveMoney("t_preAssignedId", 1L, 2L, 30)).thenReturn("");
        assertTrue(transaction.execute());
        verify(redisDistributedLock).unlock(anyString());

    }


}
