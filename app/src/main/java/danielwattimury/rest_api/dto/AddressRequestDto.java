package danielwattimury.rest_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRequestDto {

    @JsonIgnore
    @NotNull
    private Integer contactId;

    @Size(max = 200)
    private String street;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String province;

    @NotBlank
    @Size(max = 100)
    private String country;

    @Size(max = 10)
    private String postalCode;

}
