package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.entity.Order;
import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.repository.OrderRepositoryImpl;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import javax.transaction.InvalidTransactionException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class WalletTransactionTest {
    WalletTransaction transaction;
    RedisDistributedLock redisDistributedLock;
    OrderRepositoryImpl orderRepository;
    String orderIdTest = "orderIdTest";

    @BeforeEach
    void init() {
        redisDistributedLock = Mockito.mock(RedisDistributedLock.class);
        orderRepository = Mockito.mock(OrderRepositoryImpl.class);
        when(orderRepository.find(anyString())).thenReturn(new Order(orderIdTest, 30));
        setMock(redisDistributedLock);
        when(redisDistributedLock.lock(anyString())).thenReturn(true);
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
        transaction = new WalletTransaction("preAssignedId", null, 2L, 3L, "orderId",orderRepository);
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> transaction.execute());

        String expectedMessage = "This is an invalid transaction";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void should_throw_InvalidTransactionException_when_execute_given_sellerId_is_null() {
        transaction = new WalletTransaction("preAssignedId", 1L, null, 3L, "orderId",orderRepository);
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
        transaction = new WalletTransaction("preAssignedId", 1L, 2L, 3L, "orderId",orderRepository);
        when(orderRepository.find(anyString())).thenReturn(new Order(orderIdTest,-1));
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> transaction.execute());

        String expectedMessage = "This is an invalid transaction";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void should_return_false_when_execute_given_order_is_locked() throws InvalidTransactionException {
        transaction = new WalletTransaction("preAssignedId", 1L, 2L, 3L, "orderId",orderRepository);
        when(redisDistributedLock.lock(anyString())).thenReturn(false);
        assertFalse(transaction.execute());

    }

}
