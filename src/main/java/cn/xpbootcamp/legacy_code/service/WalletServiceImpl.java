package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.entity.Transaction;
import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import cn.xpbootcamp.legacy_code.repository.UserRepositoryImpl;

import java.util.UUID;

public class WalletServiceImpl implements WalletService {
    private UserRepository userRepository = new UserRepositoryImpl();

    public String moveMoney(Transaction transaction) {
        User buyer = userRepository.find(transaction.getBuyerId());
        if (buyer.getBalance() >= transaction.getAmount()) {
            User seller = userRepository.find(transaction.getSellerId());
            seller.setBalance(seller.getBalance() + transaction.getAmount());
            buyer.setBalance(buyer.getBalance() - transaction.getAmount());

            transaction.setStatus(STATUS.EXECUTED);
            transaction.setReceiptId(UUID.randomUUID().toString() + transaction.getId());
            return transaction.getReceiptId();

        } else {
            transaction.setStatus(STATUS.FAILED);
            return null;
        }
    }
}
