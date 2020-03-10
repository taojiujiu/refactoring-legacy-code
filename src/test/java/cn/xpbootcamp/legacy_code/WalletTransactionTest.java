package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.transaction.InvalidTransactionException;

import static org.junit.jupiter.api.Assertions.*;

class WalletTransactionTest {
    WalletTransaction transaction;

    @Test
    void should_throw_InvalidTransactionException_when_execute_given_buyerId_is_null() {
        transaction = new WalletTransaction("preAssignedId", null, 2L, 3L, "orderId");
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> transaction.execute());

        String expectedMessage = "This is an invalid transaction";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void should_throw_InvalidTransactionException_when_execute_given_sellerId_is_null() {
        transaction = new WalletTransaction("preAssignedId", 1L, null, 3L, "orderId");
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> transaction.execute());

        String expectedMessage = "This is an invalid transaction";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    //todo: Add amount is lesser than zero test case
//    @Test
//    void should_throw_InvalidTransactionException_when_execute_given_amount_is_null() {
//        // todo: Need mock amount is lesser than amount
//        transaction = new WalletTransaction("preAssignedId", 1L, 2L, 3L, "orderId");
//        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
//                () -> transaction.execute());
//
//        String expectedMessage = "This is an invalid transaction";
//        String actualMessage = exception.getMessage();
//
//        assertTrue(actualMessage.contains(expectedMessage));
//    }

}
