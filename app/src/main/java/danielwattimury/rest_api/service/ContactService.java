package danielwattimury.rest_api.service;

import org.springframework.stereotype.Service;

import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.PostContactRequest;
import danielwattimury.rest_api.model.PostContactResponse;
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
    public PostContactResponse createContact(PostContactRequest request, String username) {
        validationService.validate(request);

        User user = userService.getUserOrFail(username);

        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setUser(user);
        contactRepository.save(contact);

        return PostContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .build();
    }

}
