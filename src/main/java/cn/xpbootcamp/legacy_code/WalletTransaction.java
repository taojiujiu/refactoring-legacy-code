package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.repository.OrderRepository;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.utils.IdGenerator;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

import javax.transaction.InvalidTransactionException;
import java.time.Clock;

public class WalletTransaction {
    private String walletTransactionId;
    private Long buyerId;
    private Long sellerId;
    private String orderId;
    private Long createdTimestamp;
    private Double amount;
    private STATUS status;
    private OrderRepository orderRepository;
    Clock clock;
    WalletService walletService;

    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, String orderId, OrderRepository orderRepository, Clock clock, WalletService walletService) {
        initTransactionId(preAssignedId);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.orderId = orderId;
        this.status = STATUS.TO_BE_EXECUTED;
        this.clock = clock;
        this.createdTimestamp = clock.instant().toEpochMilli();
        this.orderRepository = orderRepository;
        this.walletService = walletService;
    }

    private void initTransactionId(String preAssignedId) {
        if (preAssignedId != null && !preAssignedId.isEmpty()) {
            this.walletTransactionId = preAssignedId;
        } else {
            this.walletTransactionId = IdGenerator.generateTransactionId();
        }
        if (!this.walletTransactionId.startsWith("t_")) {
            this.walletTransactionId = "t_" + preAssignedId;
        }
    }

    public boolean execute() throws InvalidTransactionException {
        this.amount = orderRepository.find(orderId).getAmount();
        if (buyerId == null || (sellerId == null || amount < 0.0)) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
        if (status == STATUS.EXECUTED) return true;
        boolean isLocked = false;
        try {
            isLocked = RedisDistributedLock.getSingletonInstance().lock(walletTransactionId);

            // 锁定未成功，返回false
            if (!isLocked) {
                return false;
            }
            if (status == STATUS.EXECUTED) return true; // double check
            long executionInvokedTimestamp = clock.instant().toEpochMilli();
            // 交易超过20天
            if (executionInvokedTimestamp - createdTimestamp > 1728000000) {
                this.status = STATUS.EXPIRED;
                return false;
            }
            String walletTransactionId = walletService.moveMoney(this.walletTransactionId, buyerId, sellerId, amount);
            if (walletTransactionId != null) {
                this.walletTransactionId = walletTransactionId;
                this.status = STATUS.EXECUTED;
                return true;
            } else {
                this.status = STATUS.FAILED;
                return false;
            }
        } finally {
            if (isLocked) {
                RedisDistributedLock.getSingletonInstance().unlock(walletTransactionId);
            }
        }
    }

}
