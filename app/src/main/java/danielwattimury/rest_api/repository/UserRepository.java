package danielwattimury.rest_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import danielwattimury.rest_api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
