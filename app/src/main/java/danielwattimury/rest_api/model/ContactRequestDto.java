package danielwattimury.rest_api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactRequestDto {

    @NotBlank
    @Size(max = 100, min = 3)
    private String firstName;

    @Size(max = 100, min = 3)
    private String lastName;

    @Size(max = 15)
    private String phone;

    @Email
    @Size(max = 100)
    private String email;

}
