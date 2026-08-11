package danielwattimury.rest_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import danielwattimury.rest_api.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {

}
