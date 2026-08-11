package danielwattimury.rest_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactSearchDto {

    private String name;

    private String phone;

    private String email;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

}
