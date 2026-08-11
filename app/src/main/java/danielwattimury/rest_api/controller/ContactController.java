package danielwattimury.rest_api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.enums.ResponseStatus;
import danielwattimury.rest_api.model.ContactRequestDto;
import danielwattimury.rest_api.model.ContactResponseDto;
import danielwattimury.rest_api.model.ContactSearchDto;
import danielwattimury.rest_api.model.PagingResponse;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.security.UserPrincipal;
import danielwattimury.rest_api.service.ContactService;

@RestController
@RequestMapping("/contacts")
public class ContactController {

        private final ContactService contactService;

        public ContactController(ContactService contactService) {
                this.contactService = contactService;
        }

        @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<WebResponse<ContactResponseDto>> post(
                        @RequestBody ContactRequestDto request,
                        @AuthenticationPrincipal UserPrincipal principal) {
                ContactResponseDto contact = contactService.createContact(request, principal.getUserId());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(WebResponse.<ContactResponseDto>builder()
                                                .status(ResponseStatus.SUCCESS)
                                                .message("Contact added successfully")
                                                .data(contact)
                                                .build());

        }

        @PutMapping(path = "/{contactId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ContactResponseDto> put(@RequestBody ContactRequestDto request,
                        @PathVariable Integer contactId, @AuthenticationPrincipal UserPrincipal principal) {
                ContactResponseDto contact = contactService.updateContact(request, principal.getUserId(), contactId);

                return WebResponse.<ContactResponseDto>builder()
                                .status(ResponseStatus.SUCCESS)
                                .message("Contact updated successfully")
                                .data(contact)
                                .build();
        }

        @GetMapping(path = "/{contactId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ContactResponseDto> getOne(@PathVariable Integer contactId,
                        @AuthenticationPrincipal UserPrincipal principal) {
                ContactResponseDto contact = contactService.getContactById(principal.getUserId(), contactId);

                return WebResponse.<ContactResponseDto>builder()
                                .status(ResponseStatus.SUCCESS)
                                .message("Contact retrieved successfully")
                                .data(contact)
                                .build();
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<ContactResponseDto>> search(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) String phone,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "10") Integer size) {

                ContactSearchDto request = ContactSearchDto.builder()
                                .email(email)
                                .name(name)
                                .phone(phone)
                                .size(size)
                                .page(page)
                                .build();

                Page<ContactResponseDto> searchContactResponse = contactService.searchContact(principal.getUserId(),
                                request);
                return WebResponse.<List<ContactResponseDto>>builder()
                                .status(ResponseStatus.SUCCESS)
                                .message("Contact retrieved successfully")
                                .data(searchContactResponse.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(searchContactResponse.getNumber())
                                                .totalPage(searchContactResponse.getTotalPages())
                                                .size(searchContactResponse.getSize())
                                                .build())
                                .build();
        }

        @DeleteMapping(path = "/{contactId}")
        public ResponseEntity<Void> delete(
                        @PathVariable Integer contactId,
                        @AuthenticationPrincipal UserPrincipal principal) {
                contactService.deleteContactById(principal.getUserId(), contactId);

                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

}
