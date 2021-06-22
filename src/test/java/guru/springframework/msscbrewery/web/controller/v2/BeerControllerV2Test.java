package guru.springframework.msscbrewery.web.controller.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.services.v2.BeerServiceV2;
import guru.springframework.msscbrewery.web.model.v2.BeerDtoV2;
import guru.springframework.msscbrewery.web.model.v2.BeerStyleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.springframework.guru", uriPort = 80)
@WebMvcTest(BeerControllerV2.class)
@ComponentScan(basePackages = "guru.springframework.sfgrestdocsexample.web.mappers")
public class BeerControllerV2Test {

    @MockBean
    BeerServiceV2 beerService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    BeerDtoV2 beerToReturn;
    UUID beerUID;


    @BeforeEach
    public void setUp() throws Exception {
        beerUID = UUID.randomUUID();
        beerToReturn = BeerDtoV2
                .builder()
                .beerName("ksiazece")
                .beerStyle(BeerStyleEnum.ALE)
                .upc(11l)
               // .id(beerUID)
                .build();

    }

    @Test
    public void testGetBeer() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willReturn(beerToReturn);

        mockMvc.perform(get("/api/v2/beer/{beerId}", beerUID.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("v2/beer-get",
                        pathParameters(
                                parameterWithName("beerId").description("UUID of desired beer")
                        )
                        ,
                        responseFields(
                                fieldWithPath("id").description("Id of Beer"),
                                fieldWithPath("beerName").description("Name of the beer"),
                                fieldWithPath("beerStyle").description("Style of the beer"),
                                fieldWithPath("upc").description("upc of the beer")
                        )

                ));

    }

    @Test
    public void testHandlePost() throws Exception {
        String beerDtoJson = objectMapper.writeValueAsString(beerToReturn);
        beerToReturn.setId(beerUID);
        given(beerService.saveNewBeer(any(BeerDtoV2.class))).willReturn(beerToReturn);

        ConstrainedFields fields = new ConstrainedFields(BeerDtoV2.class);

        mockMvc.perform(post("/api/v2/beer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isCreated())
                .andDo(document("/v2/beer-new",
                        requestFields(
                                fields.withPath("id").description("ID of the beer as UUID"),
                                fields.withPath("beerName").description("Name of the beer"),
                                fields.withPath("beerStyle").description("Style of the beer"),
                                fields.withPath("upc").description("upc")
                        )));

        verify(beerService).saveNewBeer(any());
    }

    @Test
    public void testHandleUpdate() throws Exception {

        String beerDtoJson = objectMapper.writeValueAsString(beerToReturn);
        ConstrainedFields fields = new ConstrainedFields(BeerDtoV2.class);

        mockMvc.perform(put("/api/v2/beer/{beerId}", beerUID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isNoContent())
                .andDo(document("/v2/beer-update",
                        requestFields(
                                fields.withPath("id").description("ID of the beer as UUID"),
                                fields.withPath("beerName").description("Name of the beer"),
                                fields.withPath("beerStyle").description("Style of the beer"),
                                fields.withPath("upc").description("upc")
                        )));

        verify(beerService).updateBeer(any(), any());

    }

    @Test
    public void testDeleteBeer() throws Exception {
        mockMvc.perform(delete("/api/v2/beer/{beerId}", beerUID.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("v2/beer-delete",
                        pathParameters(
                                parameterWithName("beerId").description("UUID of desired beer to delete")
                        )));

        verify(beerService).deleteById(any());

    }

    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }

}