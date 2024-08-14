package example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository repository;

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
}