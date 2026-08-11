package danielwattimury.rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagingResponseDto {

    private Integer currentPage;

    private Integer totalPage;

    private Integer size;

}
