package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import cn.xpbootcamp.legacy_code.repository.UserRepositoryImpl;

import java.util.UUID;

public class WalletServiceImpl implements WalletService {
    private UserRepository userRepository = new UserRepositoryImpl();

    public String moveMoney(String walletTransactionId, long buyerId, long sellerId, double amount) {
        User buyer = userRepository.find(buyerId);
        if (buyer.getBalance() >= amount) {
            User seller = userRepository.find(sellerId);
            seller.setBalance(seller.getBalance() + amount);
            buyer.setBalance(buyer.getBalance() - amount);
            return UUID.randomUUID().toString() + walletTransactionId;
        } else {
            return null;
        }
    }
}
