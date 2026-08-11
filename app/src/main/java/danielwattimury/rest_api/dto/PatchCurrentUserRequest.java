package danielwattimury.rest_api.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatchCurrentUserRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String password;

}
