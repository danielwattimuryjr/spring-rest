package danielwattimury.rest_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.model.ContactRequestDto;
import danielwattimury.rest_api.model.ContactResponseDto;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.service.ContactService;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<ContactResponseDto>> post(
            @RequestBody ContactRequestDto request,
            Authentication authentication) {
        ContactResponseDto contact = contactService.createContact(request, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WebResponse.<ContactResponseDto>builder()
                        .status("success")
                        .message("Contact added successfully")
                        .data(contact)
                        .build());

    }

    @PutMapping(path = "/{idContact}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<ContactResponseDto>> put(@RequestBody ContactRequestDto request,
            @PathVariable Integer idContact, Authentication authentication) {
        ContactResponseDto contact = contactService.updateContact(request, authentication.getName(), idContact);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(WebResponse.<ContactResponseDto>builder()
                        .status("success")
                        .message("Contact updated successfully")
                        .data(contact)
                        .build());
    }

}
