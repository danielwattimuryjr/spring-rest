package danielwattimury.rest_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.model.PostContactRequest;
import danielwattimury.rest_api.model.PostContactResponse;
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
    public ResponseEntity<WebResponse<PostContactResponse>> post(
            @RequestBody PostContactRequest request,
            Authentication authentication) {
        PostContactResponse contact = contactService.createContact(request, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WebResponse.<PostContactResponse>builder()
                        .status("success")
                        .message("Contact added successfully")
                        .data(contact)
                        .build());

    }

}
