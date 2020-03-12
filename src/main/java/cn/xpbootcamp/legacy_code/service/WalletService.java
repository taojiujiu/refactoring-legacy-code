package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.entity.Transaction;

public interface WalletService {
    String moveMoney(Transaction transaction);
}
