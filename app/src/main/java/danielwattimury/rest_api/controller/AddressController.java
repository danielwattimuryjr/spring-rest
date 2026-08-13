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

import danielwattimury.rest_api.dto.AddressRequestDto;
import danielwattimury.rest_api.dto.AddressResponseDto;
import danielwattimury.rest_api.dto.PagingRequestDto;
import danielwattimury.rest_api.responses.Pagination;
import danielwattimury.rest_api.responses.Response;
import danielwattimury.rest_api.security.UserPrincipal;
import danielwattimury.rest_api.service.AddressService;

@RestController
@RequestMapping("/contacts/{contactId}/addresses")
public class AddressController {

        private final AddressService addressService;

        public AddressController(AddressService addressService) {
                this.addressService = addressService;
        }

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<Response<AddressResponseDto>> post(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Integer contactId,
                        @RequestBody AddressRequestDto request) {
                request.setContactId(contactId);
                AddressResponseDto address = addressService.createAddress(request, principal.getUserId());
                Response<AddressResponseDto> successfulResponse = Response.successfulResponse(
                                HttpStatus.CREATED, "Address created successfully", address);

                return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
        }

        @PutMapping(path = "/{addressId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<AddressResponseDto> put(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Integer contactId,
                        @PathVariable Integer addressId,
                        @RequestBody AddressRequestDto request) {
                request.setContactId(contactId);
                AddressResponseDto address = addressService.updateAddress(request, addressId, principal.getUserId());

                return Response.successfulResponse("Address updated successfully", address);
        }

        @GetMapping(path = "/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<AddressResponseDto> getOne(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Integer contactId,
                        @PathVariable Integer addressId) {
                AddressResponseDto address = addressService.getAddressById(contactId, addressId, principal.getUserId());

                return Response.successfulResponse("Address retrieved successfully", address);
        }

        @DeleteMapping(path = "/{addressId}")
        public Response<Void> delete(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Integer contactId,
                        @PathVariable Integer addressId) {
                addressService.deleteAddressById(contactId, addressId, principal.getUserId());

                return Response.successfulResponse("Address deleted successfully");
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<List<AddressResponseDto>> getAll(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Integer contactId,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "10") Integer size) {

                PagingRequestDto paginationRequest = PagingRequestDto.builder()
                                .size(size)
                                .page(page)
                                .build();

                Page<AddressResponseDto> getAllAddressResult = addressService.getAllAddresses(principal.getUserId(),
                                contactId,
                                paginationRequest);

                Pagination pagination = Pagination.builder()
                                .page(getAllAddressResult.getNumber())
                                .size(getAllAddressResult.getSize())
                                .totalElements(getAllAddressResult.getTotalElements())
                                .totalPages(getAllAddressResult.getTotalPages())
                                .build();

                return Response.successfulResponse("Addresses retrieved successfulyy", getAllAddressResult.getContent(),
                                pagination);
        }

}
