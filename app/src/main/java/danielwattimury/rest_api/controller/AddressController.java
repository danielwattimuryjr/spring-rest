package danielwattimury.rest_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.dto.AddressRequestDto;
import danielwattimury.rest_api.dto.AddressResponseDto;
import danielwattimury.rest_api.dto.WebResponseDto;
import danielwattimury.rest_api.enums.ResponseStatus;
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
    public ResponseEntity<WebResponseDto<AddressResponseDto>> post(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Integer contactId,
            @RequestBody AddressRequestDto request) {
        request.setContactId(contactId);
        AddressResponseDto address = addressService.createAddress(request, principal.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WebResponseDto.<AddressResponseDto>builder()
                        .status(ResponseStatus.SUCCESS)
                        .message("Address created successfully")
                        .data(address)
                        .build());
    }

    @PutMapping(path = "/{addressId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponseDto<AddressResponseDto>> post(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Integer contactId,
            @PathVariable Integer addressId,
            @RequestBody AddressRequestDto request) {
        request.setContactId(contactId);
        AddressResponseDto address = addressService.updateAddress(request, addressId, principal.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(WebResponseDto.<AddressResponseDto>builder()
                        .status(ResponseStatus.SUCCESS)
                        .message("Address updated successfully")
                        .data(address)
                        .build());
    }

    @GetMapping(path = "/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponseDto<AddressResponseDto>> post(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Integer contactId,
            @PathVariable Integer addressId) {
        AddressResponseDto address = addressService.getAddressById(contactId, addressId, principal.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(WebResponseDto.<AddressResponseDto>builder()
                        .status(ResponseStatus.SUCCESS)
                        .message("Address retrieved successfully")
                        .data(address)
                        .build());
    }

}
