package danielwattimury.rest_api.dto;

import danielwattimury.rest_api.enums.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebResponse<T> {

    private ResponseStatus status;

    private String message;

    private T data;

    private PagingResponse paging;

}
