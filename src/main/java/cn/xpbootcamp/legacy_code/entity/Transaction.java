package cn.xpbootcamp.legacy_code.entity;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.utils.IdGenerator;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

import javax.transaction.InvalidTransactionException;
import java.time.Instant;
import java.util.Optional;

public class Transaction {
    private final String id;
    private final Long buyerId;
    private final Long sellerId;
    private final String orderId;
    private final Double amount;
    private String receiptId;
    private STATUS status = STATUS.TO_BE_EXECUTED;
    private boolean isLocked = false;
    private Instant createdTimestamp;

    public Transaction(String id, Long buyerId, Long sellerId, String orderId, Double amount, Instant createdTimestamp) throws InvalidTransactionException {
        if (buyerId == null || (sellerId == null || amount < 0.0)) {
            throw new InvalidTransactionException("This is an invalid params");
        }
        this.id = initTransactionId(id);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.orderId = orderId;
        this.amount = amount;
        this.createdTimestamp = createdTimestamp;
    }

    public boolean lock() {
        isLocked = RedisDistributedLock.getSingletonInstance().lock(this.id);
        return isLocked;
    }

    public String getId() {
        return id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public boolean isExecuted() {
        return this.status == STATUS.EXECUTED;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public Instant getCreatedTimestamp() {
        return createdTimestamp;
    }

    private String initTransactionId(String preAssignedId) {
        String holder =
                (Optional.ofNullable(preAssignedId)
                        .map(string -> !preAssignedId.isEmpty())
                        .orElse(false))
                        ? preAssignedId : IdGenerator.generateTransactionId();


        if (!holder.startsWith("t_")) {
            holder = "t_" + preAssignedId;
        }
        return holder;
    }

    public void unlock() {
        isLocked = RedisDistributedLock.getSingletonInstance().unlock(this.id);
    }
}
