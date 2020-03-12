package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.entity.Transaction;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletTransactionServiceTest {
    private WalletTransactionService walletTransactionService;
    private RedisDistributedLock redisDistributedLock;
    private WalletServiceImpl walletService;
    private String orderIdTest = "orderIdTest";
    private Clock clock;
    private Instant now = Instant.now();

    @BeforeEach
    void init() {
        redisDistributedLock = Mockito.mock(RedisDistributedLock.class);
        walletService = Mockito.mock(WalletServiceImpl.class);
        clock = Mockito.mock(Clock.class);
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

        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> new Transaction("preAssignedId", null, 2L, orderIdTest, 30D,now));

        String expectedMessage = "This is an invalid params";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void should_throw_InvalidTransactionException_when_execute_given_sellerId_is_null() {

        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> new Transaction("preAssignedId", 1L, null, orderIdTest, 30D,now));

        String expectedMessage = "This is an invalid params";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void should_throw_InvalidTransactionException_when_execute_given_amount_is_null() {
        // todo: Need mock amount is lesser than amount
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> new Transaction("preAssignedId", 1L, 2L, orderIdTest, -30D,now));


        String expectedMessage = "This is an invalid params";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void should_return_null_when_execute_given_order_is_locked() throws InvalidTransactionException {
        walletTransactionService = new WalletTransactionService(new Transaction("preAssignedId", 1L, 2L, orderIdTest, 30D,now), clock, walletService);
        when(redisDistributedLock.lock(anyString())).thenReturn(false);
        assertNull(walletTransactionService.execute());
    }

    @Test
    void should_return_false_when_execute_given_order_created_time_earlier_than_current_time_20_days() throws InvalidTransactionException {
        walletTransactionService = new WalletTransactionService(new Transaction("preAssignedId", 1L, 2L, orderIdTest, 30D,now), clock, walletService);
        when(clock.instant()).thenReturn(now.plusMillis(1728000001));
        assertNull(walletTransactionService.execute());
    }

    @Test
    void should_return_false_when_execute_when_user_balance_not_enough() throws InvalidTransactionException {
        Transaction transaction = new Transaction("preAssignedId", 1L, 2L, orderIdTest, 30D,now);
                walletTransactionService = new WalletTransactionService(transaction, clock, walletService);
        when(walletService.moveMoney(transaction)).thenReturn(null);
        assertNull(walletTransactionService.execute());
        verify(redisDistributedLock).unlock(anyString());
    }

    @Test
    void should_return_true_when_execute_when_user_balance_enough() throws InvalidTransactionException {
        Transaction transaction = new Transaction("preAssignedId", 1L, 2L, orderIdTest, 30D,now);
        walletTransactionService = new WalletTransactionService(transaction, clock, walletService);
        when(walletService.moveMoney(transaction)).thenReturn("");
        assertNotNull(walletTransactionService.execute());
        verify(redisDistributedLock).unlock(anyString());
    }


}
