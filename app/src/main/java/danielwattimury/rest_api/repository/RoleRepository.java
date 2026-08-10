package danielwattimury.rest_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import danielwattimury.rest_api.entity.Role;
import danielwattimury.rest_api.enums.RoleEnum;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleEnum roleEnum);

}
