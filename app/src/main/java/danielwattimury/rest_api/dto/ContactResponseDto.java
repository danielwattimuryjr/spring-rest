package danielwattimury.rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactResponseDto {

    private Integer id;

    private String firstName;

    private String lastName;

    private String phone;

    private String email;

}
