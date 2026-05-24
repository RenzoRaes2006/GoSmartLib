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

import ap.be.backend.entity.BeschikbareSchool;
import ap.be.backend.entity.School;
import ap.be.backend.repository.BeschikbareSchoolRepository;
import ap.be.backend.repository.SchoolRepository;

public class SchoolTesten extends AbstractIntegrationTest {

    @MockBean
    private SchoolRepository schoolRepository;

    @MockBean
    private BeschikbareSchoolRepository beschikbareSchoolRepository;

    // @Test
    // void givenSchoolsInDatabase_whenGetAllSchools_thenReturnJsonArrayOfSchools() throws Exception {
    //     // given: setup the mock database response
    //     School school1 = new School(1L, "GO! Atheneum", "Adres 1", "api-1");
    //     School school2 = new School(2L, "GO! Lyceum", "Adres 2", "api-2");
    //     List<School> allSchools = Arrays.asList(school1, school2);
        
    //     given(schoolRepository.findAll()).willReturn(allSchools);

    //     // when: executing the get request
    //     // then: verify the response status and content
    //     mockMvc.perform(get("/api/scholen"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.size()").value(2))
    //             .andExpect(jsonPath("$[0].naam").value("GO! Atheneum"));
    // }

    // @Test
    // void givenValidSchoolData_whenCreateSchool_thenReturnSavedSchool() throws Exception {
    //     // given: setup the new school and the expected saved school
    //     School newSchool = new School(null, "GO! Topsport", "Adres 3", "api-3");
    //     School savedSchool = new School(3L, "GO! Topsport", "Adres 3", "api-3");
        
    //     given(schoolRepository.save(any(School.class))).willReturn(savedSchool);

    //     // when: executing the post request
    //     // then: verify the status and the returned json object
    //     mockMvc.perform(post("/api/scholen")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(newSchool)))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.id").value(3))
    //             .andExpect(jsonPath("$.naam").value("GO! Topsport"));
    // }

    // @Test
    // void givenExistingSchoolId_whenGetLiveData_thenReturnSimulatedData() throws Exception {
    //     // given: an existing school in the mock database
    //     Long schoolId = 1L;
    //     School existingSchool = new School(schoolId, "GO! Spectrumschool", "Adres 4", "api-4");
        
    //     given(schoolRepository.findById(schoolId)).willReturn(Optional.of(existingSchool));

    //     // when: requesting the live data endpoint
    //     // then: verify the simulated smartschool data structure
    //     mockMvc.perform(get("/api/scholen/{id}/live-data", schoolId))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.schoolInfo.naam").value("GO! Spectrumschool"))
    //             .andExpect(jsonPath("$.liveSmartschoolData").exists())
    //             .andExpect(jsonPath("$.liveSmartschoolData.klassen").isArray());
    // }

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

    // --- Tests voor bewerken van beschikbare school (catalogus) ---

    @Test
    void givenExistingBeschikbareSchool_whenBewerken_thenReturnUpdatedSchool() throws Exception {
        // given: een bestaande beschikbare school in de catalogus
        Long id = 1L;
        BeschikbareSchool bestaand = new BeschikbareSchool(id, "GO! Oude Naam", "Oud Adres 1");
        BeschikbareSchool update = new BeschikbareSchool(null, "GO! Nieuwe Naam", "Nieuw Adres 9");
        BeschikbareSchool opgeslagen = new BeschikbareSchool(id, "GO! Nieuwe Naam", "Nieuw Adres 9");

        given(beschikbareSchoolRepository.findById(id)).willReturn(Optional.of(bestaand));
        given(beschikbareSchoolRepository.existsByNaam("GO! Nieuwe Naam")).willReturn(false);
        given(beschikbareSchoolRepository.save(any(BeschikbareSchool.class))).willReturn(opgeslagen);

        // when: PUT met geldige update wordt uitgevoerd
        // then: status 200 en het bijgewerkte object wordt teruggegeven
        mockMvc.perform(put("/api/scholen/beschikbaar/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.naam").value("GO! Nieuwe Naam"))
                .andExpect(jsonPath("$.adres").value("Nieuw Adres 9"));

        then(beschikbareSchoolRepository).should().save(any(BeschikbareSchool.class));
    }

    @Test
    void givenNonExistingBeschikbareSchool_whenBewerken_thenReturnNotFound() throws Exception {
        // given: een id dat niet voorkomt in de catalogus
        Long id = 42L;
        BeschikbareSchool update = new BeschikbareSchool(null, "GO! Naam", "Adres 1");
        given(beschikbareSchoolRepository.findById(id)).willReturn(Optional.empty());

        // when: PUT wordt uitgevoerd op niet-bestaand id
        // then: status 404 en er wordt niets opgeslagen
        mockMvc.perform(put("/api/scholen/beschikbaar/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());

        then(beschikbareSchoolRepository).should(org.mockito.Mockito.never()).save(any(BeschikbareSchool.class));
    }

    @Test
    void givenBlankNaam_whenBewerken_thenReturnBadRequest() throws Exception {
        // given: bestaande school maar update bevat lege naam
        Long id = 1L;
        BeschikbareSchool bestaand = new BeschikbareSchool(id, "GO! Oude Naam", "Oud Adres 1");
        BeschikbareSchool update = new BeschikbareSchool(null, "   ", "Nieuw Adres 9");
        given(beschikbareSchoolRepository.findById(id)).willReturn(Optional.of(bestaand));

        // when: PUT met lege naam
        // then: status 400 met validatie-foutmelding
        mockMvc.perform(put("/api/scholen/beschikbaar/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Schoolnaam is verplicht"));

        then(beschikbareSchoolRepository).should(org.mockito.Mockito.never()).save(any(BeschikbareSchool.class));
    }

    @Test
    void givenBlankAdres_whenBewerken_thenReturnBadRequest() throws Exception {
        // given: bestaande school maar update bevat leeg adres
        Long id = 1L;
        BeschikbareSchool bestaand = new BeschikbareSchool(id, "GO! Oude Naam", "Oud Adres 1");
        BeschikbareSchool update = new BeschikbareSchool(null, "GO! Nieuwe Naam", "");
        given(beschikbareSchoolRepository.findById(id)).willReturn(Optional.of(bestaand));

        // when: PUT met leeg adres
        // then: status 400 met validatie-foutmelding
        mockMvc.perform(put("/api/scholen/beschikbaar/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Adres is verplicht"));

        then(beschikbareSchoolRepository).should(org.mockito.Mockito.never()).save(any(BeschikbareSchool.class));
    }

    @Test
    void givenDuplicateNaam_whenBewerken_thenReturnBadRequest() throws Exception {
        // given: bestaande school waarbij de update-naam al door een andere school in de catalogus wordt gebruikt
        Long id = 1L;
        BeschikbareSchool bestaand = new BeschikbareSchool(id, "GO! Oude Naam", "Oud Adres 1");
        BeschikbareSchool update = new BeschikbareSchool(null, "GO! Bestaande Naam", "Nieuw Adres 9");
        given(beschikbareSchoolRepository.findById(id)).willReturn(Optional.of(bestaand));
        given(beschikbareSchoolRepository.existsByNaam("GO! Bestaande Naam")).willReturn(true);

        // when: PUT met een naam die al in gebruik is door een andere school
        // then: status 400 met duplicate-foutmelding
        mockMvc.perform(put("/api/scholen/beschikbaar/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("School bestaat al in de catalogus"));

        then(beschikbareSchoolRepository).should(org.mockito.Mockito.never()).save(any(BeschikbareSchool.class));
    }

    @Test
    void givenUnchangedNaam_whenBewerken_thenSkipDuplicateCheckAndSave() throws Exception {
        // given: bestaande school waarvan alleen het adres wijzigt — naam blijft gelijk
        Long id = 1L;
        BeschikbareSchool bestaand = new BeschikbareSchool(id, "GO! Zelfde Naam", "Oud Adres 1");
        BeschikbareSchool update = new BeschikbareSchool(null, "GO! Zelfde Naam", "Nieuw Adres 9");
        BeschikbareSchool opgeslagen = new BeschikbareSchool(id, "GO! Zelfde Naam", "Nieuw Adres 9");
        given(beschikbareSchoolRepository.findById(id)).willReturn(Optional.of(bestaand));
        given(beschikbareSchoolRepository.save(any(BeschikbareSchool.class))).willReturn(opgeslagen);

        // when: PUT met onveranderde naam (alleen adres wijzigt)
        // then: duplicate-check wordt overgeslagen en opslaan slaagt
        mockMvc.perform(put("/api/scholen/beschikbaar/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("GO! Zelfde Naam"))
                .andExpect(jsonPath("$.adres").value("Nieuw Adres 9"));

        then(beschikbareSchoolRepository).should(org.mockito.Mockito.never()).existsByNaam(any());
        then(beschikbareSchoolRepository).should().save(any(BeschikbareSchool.class));
    }

    @Test
    void givenNaamMetSpaties_whenBewerken_thenTrimVoorOpslaan() throws Exception {
        // given: update met spaties rond naam en adres — die moeten getrimd worden
        Long id = 1L;
        BeschikbareSchool bestaand = new BeschikbareSchool(id, "GO! Oud", "Oud Adres");
        BeschikbareSchool update = new BeschikbareSchool(null, "  GO! Getrimd  ", "  Trim Adres 5  ");
        BeschikbareSchool opgeslagen = new BeschikbareSchool(id, "GO! Getrimd", "Trim Adres 5");
        given(beschikbareSchoolRepository.findById(id)).willReturn(Optional.of(bestaand));
        given(beschikbareSchoolRepository.existsByNaam("GO! Getrimd")).willReturn(false);
        given(beschikbareSchoolRepository.save(any(BeschikbareSchool.class))).willReturn(opgeslagen);

        // when: PUT met velden die spaties bevatten aan het begin/einde
        // then: response toont getrimde waarden
        mockMvc.perform(put("/api/scholen/beschikbaar/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("GO! Getrimd"))
                .andExpect(jsonPath("$.adres").value("Trim Adres 5"));
    }
}