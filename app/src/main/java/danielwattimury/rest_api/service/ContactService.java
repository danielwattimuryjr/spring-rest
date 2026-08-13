package danielwattimury.rest_api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import danielwattimury.rest_api.dto.ContactRequestDto;
import danielwattimury.rest_api.dto.ContactResponseDto;
import danielwattimury.rest_api.dto.ContactSearchDto;
import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.exceptions.ResourceNotFoundException;
import danielwattimury.rest_api.repository.ContactRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

@Service
public class ContactService {

        private final ContactRepository contactRepository;

        private final ValidationService validationService;

        private final UserService userService;

        public ContactService(ContactRepository contactRepository, ValidationService validationService,
                        UserService userService) {
                this.contactRepository = contactRepository;
                this.validationService = validationService;
                this.userService = userService;
        }

        private ContactResponseDto toContactResponse(Contact contact) {
                return ContactResponseDto.builder()
                                .id(contact.getId())
                                .firstName(contact.getFirstName())
                                .lastName(contact.getLastName())
                                .phone(contact.getPhone())
                                .email(contact.getEmail())
                                .build();
        }

        @Transactional
        public ContactResponseDto createContact(ContactRequestDto request, Integer userId) {
                validationService.validate(request);

                User user = userService.getUserOrFail(userId);

                Contact contact = new Contact();
                contact.setFirstName(request.getFirstName());
                contact.setLastName(request.getLastName());
                contact.setPhone(request.getPhone());
                contact.setEmail(request.getEmail());
                contact.setUser(user);
                contactRepository.save(contact);

                return toContactResponse(contact);
        }

        @Transactional
        public ContactResponseDto updateContact(ContactRequestDto request, Integer userId, Integer contactId) {
                validationService.validate(request);

                Contact contact = contactRepository
                                .findByIdAndUserId(contactId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

                contact.setFirstName(request.getFirstName());
                contact.setLastName(request.getLastName());
                contact.setPhone(request.getPhone());
                contact.setEmail(request.getEmail());
                contactRepository.save(contact);

                return toContactResponse(contact);
        }

        public ContactResponseDto getContactById(Integer userId, Integer contactId) {
                Contact contact = contactRepository
                                .findByIdAndUserId(contactId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

                return toContactResponse(contact);
        }

        @Transactional
        public void deleteContactById(Integer userId, Integer contactId) {
                Contact contact = contactRepository
                                .findByIdAndUserId(contactId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

                contactRepository.delete(contact);
        }

        public Page<ContactResponseDto> searchContact(Integer userId, ContactSearchDto request) {
                Specification<Contact> specification = (root, query, builder) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        predicates.add(builder.equal(root.get("user").get("id"), userId));

                        if (Objects.nonNull(request.getName())) {
                                predicates.add(builder.or(
                                                builder.like(root.get("firstName"), "%" + request.getName() + "%"),
                                                builder.like(root.get("lastName"), "%" + request.getName() + "%")));
                        }

                        if (Objects.nonNull(request.getEmail())) {
                                predicates.add(builder.like(root.get("email"), "%" + request.getEmail() + "%"));
                        }

                        if (Objects.nonNull(request.getPhone())) {
                                predicates.add(builder.like(root.get("phone"), "%" + request.getPhone() + "%"));
                        }

                        return builder.and(predicates.toArray(new Predicate[0]));
                };

                Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

                Page<Contact> contacts = contactRepository.findAll(specification, pageable);

                List<ContactResponseDto> contactResponse = contacts.getContent().stream()
                                .map(this::toContactResponse)
                                .toList();

                return new PageImpl<>(contactResponse, pageable, contacts.getTotalElements());
        }

}
