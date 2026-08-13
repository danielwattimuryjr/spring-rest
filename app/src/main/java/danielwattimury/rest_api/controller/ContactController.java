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

import danielwattimury.rest_api.dto.ContactRequestDto;
import danielwattimury.rest_api.dto.ContactResponseDto;
import danielwattimury.rest_api.dto.ContactSearchDto;
import danielwattimury.rest_api.responses.Pagination;
import danielwattimury.rest_api.responses.Response;
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
        public ResponseEntity<Response<ContactResponseDto>> post(
                        @RequestBody ContactRequestDto request,
                        @AuthenticationPrincipal UserPrincipal principal) {
                ContactResponseDto contact = contactService.createContact(request, principal.getUserId());
                Response<ContactResponseDto> successfulResponse = Response
                                .successfulResponse(HttpStatus.CREATED, "Contact added successfully", contact);

                return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
        }

        @PutMapping(path = "/{contactId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<ContactResponseDto> put(@RequestBody ContactRequestDto request,
                        @PathVariable Integer contactId, @AuthenticationPrincipal UserPrincipal principal) {
                ContactResponseDto contact = contactService.updateContact(request, principal.getUserId(), contactId);

                return Response.successfulResponse("Contact updated successfully", contact);
        }

        @GetMapping(path = "/{contactId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<ContactResponseDto> getOne(@PathVariable Integer contactId,
                        @AuthenticationPrincipal UserPrincipal principal) {
                ContactResponseDto contact = contactService.getContactById(principal.getUserId(), contactId);

                return Response.successfulResponse("Contact retrieved successfully", contact);
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<List<ContactResponseDto>> search(
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

                Pagination pagination = Pagination.builder()
                                .page(searchContactResponse.getNumber())
                                .size(searchContactResponse.getSize())
                                .totalElements(searchContactResponse.getTotalElements())
                                .totalPages(searchContactResponse.getTotalPages())
                                .build();

                return Response.successfulResponse("Contact retrieved successfully", searchContactResponse.getContent(),
                                pagination);
        }

        @DeleteMapping(path = "/{contactId}")
        public Response<Void> delete(
                        @PathVariable Integer contactId,
                        @AuthenticationPrincipal UserPrincipal principal) {
                contactService.deleteContactById(principal.getUserId(), contactId);

                return Response.successfulResponse("User deleted successfully");
        }

}
