package io.github.asinrus.race.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public class CustomerService {
    private final CustomerRepository repository;
    private CustomerService self;

    @Autowired
    void setSelf(@Lazy CustomerService customerService) {
        self = customerService;
    }

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String changeName(Long id, String name) {
        Customer entity = repository.findById(id).orElseThrow();
        entity.setName(name);
        repository.save(entity);
        return name;
    }

    @Async
    public CompletableFuture<String> changeNameAsync(Long id, String name) {
        return CompletableFuture.supplyAsync(() -> self.changeName(id, name));
    }
}