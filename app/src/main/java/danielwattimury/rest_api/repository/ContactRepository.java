package danielwattimury.rest_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import danielwattimury.rest_api.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

}
