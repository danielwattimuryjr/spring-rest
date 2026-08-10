package danielwattimury.rest_api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.ContactRequestDto;
import danielwattimury.rest_api.model.ContactResponseDto;
import danielwattimury.rest_api.repository.ContactRepository;
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

    @Transactional
    public ContactResponseDto createContact(ContactRequestDto request, String username) {
        validationService.validate(request);

        User user = userService.getUserOrFail(username);

        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setUser(user);
        contactRepository.save(contact);

        return ContactResponseDto.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .build();
    }

    @Transactional
    public ContactResponseDto updateContact(ContactRequestDto request, String username, Integer idContanct) {
        validationService.validate(request);

        User user = userService.getUserOrFail(username);

        Contact contact = contactRepository
                .findByIdAndUserId(idContanct, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contactRepository.save(contact);

        return ContactResponseDto.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .build();
    }

}
