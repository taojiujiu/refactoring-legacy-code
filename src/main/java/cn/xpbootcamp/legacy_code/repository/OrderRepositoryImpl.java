package cn.xpbootcamp.legacy_code.repository;

import cn.xpbootcamp.legacy_code.entity.Order;
import cn.xpbootcamp.legacy_code.entity.User;

public class OrderRepositoryImpl implements OrderRepository{
    @Override
    public Order find(String id) {
        // Here is connecting to database server, please do not invoke directly
        throw new RuntimeException("Database server is connecting......");
    }
}
