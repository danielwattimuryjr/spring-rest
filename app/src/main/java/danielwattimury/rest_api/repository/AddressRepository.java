package danielwattimury.rest_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import danielwattimury.rest_api.entity.Address;
import danielwattimury.rest_api.entity.Contact;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    Optional<Address> findFirstByContactAndId(Contact contact, Integer id);

    Page<Address> findAllByContact(Contact contact, Pageable pageable);

}
