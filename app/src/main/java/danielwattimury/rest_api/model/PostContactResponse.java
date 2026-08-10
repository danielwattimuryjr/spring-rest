package danielwattimury.rest_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostContactResponse {

    private Integer id;

    private String firstName;

    private String lastName;

    private String phone;

    private String email;

}
