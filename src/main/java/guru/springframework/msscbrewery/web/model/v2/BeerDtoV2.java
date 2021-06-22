package guru.springframework.msscbrewery.web.model.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.UUID;

/**
 * Created by jt on 2019-04-23.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDtoV2 {
    @Null
    private UUID id;
    @NotBlank
    @Size(min = 3, max = 100)
    private String beerName;
    @NotNull
    private BeerStyleEnum beerStyle;
    @Positive
    private Long upc;
}
