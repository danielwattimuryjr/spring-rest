package danielwattimury.rest_api.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.dto.AddressRequestDto;
import danielwattimury.rest_api.dto.AddressResponseDto;
import danielwattimury.rest_api.dto.PagingRequestDto;
import danielwattimury.rest_api.entity.Address;
import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.repository.AddressRepository;
import danielwattimury.rest_api.repository.ContactRepository;
import jakarta.transaction.Transactional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    private final ContactRepository contactRepository;

    private final ValidationService validationService;

    public AddressService(AddressRepository addressRepository, ValidationService validationService,
            ContactRepository contactRepository) {
        this.addressRepository = addressRepository;
        this.validationService = validationService;
        this.contactRepository = contactRepository;
    }

    private AddressResponseDto toAddressResponse(Address address) {
        return AddressResponseDto.builder()
                .id(address.getId().toString())
                .street(address.getStreet())
                .city(address.getCity())
                .province(address.getProvince())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .build();
    }

    @Transactional
    public AddressResponseDto createAddress(AddressRequestDto request, Integer userId) {
        validationService.validate(request);

        Contact contact = contactRepository.findByIdAndUserId(request.getContactId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contact not found"));

        Address address = new Address();
        address.setCity(request.getCity());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setProvince(request.getProvince());
        address.setStreet(request.getStreet());
        address.setContact(contact);
        Address savedAddress = addressRepository.save(address);

        return toAddressResponse(savedAddress);
    }

    @Transactional
    public AddressResponseDto updateAddress(AddressRequestDto request, Integer addressId, Integer userId) {
        validationService.validate(request);

        Contact contact = contactRepository.findByIdAndUserId(request.getContactId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contact not found"));
        Address address = addressRepository.findFirstByContactAndId(contact, addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Address not found"));

        address.setCity(request.getCity());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setProvince(request.getProvince());
        address.setStreet(request.getStreet());
        addressRepository.save(address);

        return toAddressResponse(address);
    }

    public AddressResponseDto getAddressById(Integer contactId, Integer addressId, Integer userId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contact not found"));
        Address address = addressRepository.findFirstByContactAndId(contact, addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Address not found"));

        return toAddressResponse(address);

    }

    public void deleteAddressById(Integer contactId, Integer addressId, Integer userId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contact not found"));
        Address address = addressRepository.findFirstByContactAndId(contact, addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Address not found"));

        addressRepository.delete(address);
    }

    public Page<AddressResponseDto> getAllAddresses(Integer userId, Integer contactId, PagingRequestDto pagination) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contact not found"));

        Pageable pageable = PageRequest.of(pagination.getPage(), pagination.getSize());
        Page<Address> addresses = addressRepository.findAllByContact(contact, pageable);
        List<AddressResponseDto> addressResponse = addresses.getContent().stream()
                .map(this::toAddressResponse)
                .toList();

        return new PageImpl<>(addressResponse, pageable, addresses.getTotalElements());
    }

}
