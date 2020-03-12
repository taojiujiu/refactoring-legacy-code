package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.entity.Transaction;
import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.WalletService;

import java.time.Clock;

public class WalletTransactionService {
    private Transaction transaction;
    private Clock clock;
    private WalletService walletService;
    private final static long EXPIRED_TIME = 1728000000;

    public WalletTransactionService(Transaction transaction, Clock clock, WalletService walletService) {
        this.transaction = transaction;
        this.clock = clock;
        this.walletService = walletService;
    }

    public String execute() {

        if (this.transaction.isExecuted() || isExpired(transaction)) return null;
        try {
            if (!transaction.lock() || this.transaction.isExecuted()) return null;
            return walletService.moveMoney(this.transaction);
        } finally {
            if (transaction.isLocked()) {
                transaction.unlock();
            }
        }
    }

    private boolean isExpired(Transaction transaction) {
        if (clock.instant().toEpochMilli() - transaction.getCreatedTimestamp().toEpochMilli() > EXPIRED_TIME) {
            transaction.setStatus(STATUS.EXPIRED);
            return true;
        }
        return false;
    }

}
