package ap.be.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ap.be.backend.entity.School;
import ap.be.backend.repository.SchoolRepository;

public class SchoolTesten extends AbstractIntegrationTest {

    @MockBean
    private SchoolRepository schoolRepository;

    @Test
    void givenSchoolsInDatabase_whenGetAllSchools_thenReturnJsonArrayOfSchools() throws Exception {
        // given: setup the mock database response
        School school1 = new School(1L, "GO! Atheneum", "Adres 1", "api-1");
        School school2 = new School(2L, "GO! Lyceum", "Adres 2", "api-2");
        List<School> allSchools = Arrays.asList(school1, school2);
        
        given(schoolRepository.findAll()).willReturn(allSchools);

        // when: executing the get request
        // then: verify the response status and content
        mockMvc.perform(get("/api/scholen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].naam").value("GO! Atheneum"));
    }

    @Test
    void givenValidSchoolData_whenCreateSchool_thenReturnSavedSchool() throws Exception {
        // given: setup the new school and the expected saved school
        School newSchool = new School(null, "GO! Topsport", "Adres 3", "api-3");
        School savedSchool = new School(3L, "GO! Topsport", "Adres 3", "api-3");
        
        given(schoolRepository.save(any(School.class))).willReturn(savedSchool);

        // when: executing the post request
        // then: verify the status and the returned json object
        mockMvc.perform(post("/api/scholen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSchool)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.naam").value("GO! Topsport"));
    }

    @Test
    void givenExistingSchoolId_whenGetLiveData_thenReturnSimulatedData() throws Exception {
        // given: an existing school in the mock database
        Long schoolId = 1L;
        School existingSchool = new School(schoolId, "GO! Spectrumschool", "Adres 4", "api-4");
        
        given(schoolRepository.findById(schoolId)).willReturn(Optional.of(existingSchool));

        // when: requesting the live data endpoint
        // then: verify the simulated smartschool data structure
        mockMvc.perform(get("/api/scholen/{id}/live-data", schoolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolInfo.naam").value("GO! Spectrumschool"))
                .andExpect(jsonPath("$.liveSmartschoolData").exists())
                .andExpect(jsonPath("$.liveSmartschoolData.klassen").isArray());
    }

    @Test
    void givenExistingSchoolId_whenDeleteSchool_thenCallRepositoryAndReturnNoContent() throws Exception {
        // given: an existing school id that is confirmed by the repository
        Long schoolId = 1L;
        given(schoolRepository.existsById(schoolId)).willReturn(true);

        // when: executing the delete request
        // then: expect a 204 no content status
        mockMvc.perform(delete("/api/scholen/{id}", schoolId))
                .andExpect(status().isNoContent());

        // then: also verify that the deletebyid method was executed
        then(schoolRepository).should().deleteById(schoolId);
    }
}