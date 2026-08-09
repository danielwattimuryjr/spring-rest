package danielwattimury.rest_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import danielwattimury.rest_api.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

}
