package danielwattimury.rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponseDto {

    private String id;

    private String street;

    private String city;

    private String province;

    private String country;

    private String postalCode;

}
