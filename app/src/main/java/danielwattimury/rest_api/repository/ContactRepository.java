package danielwattimury.rest_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import danielwattimury.rest_api.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer>, JpaSpecificationExecutor<Contact> {

    Optional<Contact> findByIdAndUserId(Integer contactId, Integer userId);

}
