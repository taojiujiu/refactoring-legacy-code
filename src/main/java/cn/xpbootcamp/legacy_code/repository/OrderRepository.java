package cn.xpbootcamp.legacy_code.repository;

import cn.xpbootcamp.legacy_code.entity.Order;

public interface OrderRepository {
    Order find(String id);
}
