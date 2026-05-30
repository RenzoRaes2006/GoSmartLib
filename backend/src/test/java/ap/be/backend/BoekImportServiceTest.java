package ap.be.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekImportService;
import ap.be.backend.service.ExemplaarService;
import ap.be.backend.exception.InvalidImportDataException;

@ExtendWith(MockitoExtension.class)
public class BoekImportServiceTest {

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private ExemplaarService exemplaarService;

    @InjectMocks
    private BoekImportService boekImportService;

    // Helper-methode om razendsnel een nep-Excel bestand in het geheugen te maken
    private MockMultipartFile createMockExcelFile(List<String[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Boeken");
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i);
                String[] rowData = rows.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    row.createCell(j).setCellValue(rowData[j]);
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return new MockMultipartFile("file", "test.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
        }
    }

    // --- TESTEN VOOR FASE 1: DE PREVIEW PARSER ---

    @Test
    void givenGeldigBestand_whenParseerVoorPreview_thenReturnsLijstMetBoeken() throws Exception {
        // Arrange: Een Excel met 1 geldig boek
        List<String[]> excelData = Arrays.asList(
                new String[] { "Titel", "Auteur", "ISBN", "Aantal exemplaren" },
                new String[] { "Clean Code", "Robert C. Martin", "9780132350884", "3" });
        MockMultipartFile file = createMockExcelFile(excelData);

        // De repository vindt niets, dus het boek is 'Nieuw'
        when(boekRepository.findByIsbn("9780132350884")).thenReturn(Optional.empty());
        when(boekRepository.findByTitelAndAuteur("Clean Code", "Robert C. Martin")).thenReturn(Optional.empty());

        // Act
        List<Boek> previewLijst = boekImportService.parseerVoorPreview(file);

        // Assert
        assertEquals(1, previewLijst.size(), "Er moet exact 1 boek geparseerd zijn");
        Boek geparseerdBoek = previewLijst.get(0);
        assertEquals("Clean Code", geparseerdBoek.getTitel());
        assertEquals(3, geparseerdBoek.getImportAantal());
        assertFalse(geparseerdBoek.isBestaatAl(), "Het boek is nieuw, dus bestaatAl moet false zijn");
    }

    @Test
    void givenBestandZonderTitel_whenParseerVoorPreview_thenThrowsInvalidImportDataException() throws Exception {
        // Arrange: Een Excel waar de titel leeg is op rij 2
        List<String[]> excelData = Arrays.asList(
                new String[] { "Titel", "Auteur", "ISBN" },
                new String[] { "", "J.K. Rowling", "1234567890" });
        MockMultipartFile file = createMockExcelFile(excelData);

        // Act & Then: Er moet een InvalidImportDataException gegooid worden
        InvalidImportDataException ex = assertThrows(InvalidImportDataException.class, () -> {
            boekImportService.parseerVoorPreview(file);
        });

        assertTrue(ex.getMessage().contains("Titel ontbreekt in rij 2"),
                "De error message moet specifiek de rij noemen");
    }

    // --- TESTEN VOOR FASE 2: DE BEVESTIGING (UPSERT) ---

    @Test
    void givenNieuwBoek_whenBevestigImport_thenSavesNewBookAndReturnsCount() {
        // Arrange: Een goedgekeurd boek vanuit de frontend
        Boek nieuwBoek = new Boek();
        nieuwBoek.setTitel("Spring Boot in Action");
        nieuwBoek.setAuteur("Craig Walls");
        nieuwBoek.setIsbn("9781617292545");
        nieuwBoek.setImportAantal(2);

        when(boekRepository.findByIsbn("9781617292545")).thenReturn(Optional.empty());
        when(boekRepository.findByTitelAndAuteur("Spring Boot in Action", "Craig Walls")).thenReturn(Optional.empty());
        when(boekRepository.save(nieuwBoek)).thenReturn(nieuwBoek);

        // Act
        int result = boekImportService.bevestigImport(List.of(nieuwBoek));

        // Assert
        assertEquals(2, result, "Het totaal aantal toegevoegde exemplaren moet 2 zijn");
        verify(boekRepository, times(1)).save(nieuwBoek); // Controleer of save() is aangeroepen
        verify(exemplaarService, times(1)).genereerExemplaren(nieuwBoek, 2);
    }

    @Test
    void givenBestaandBoek_whenBevestigImport_thenUpdatesStockOnly() {
        // Arrange: Een boek dat AL in de database zit met 3 exemplaren
        Boek dbBoek = new Boek();
        dbBoek.setId(1L);
        dbBoek.setTitel("De Hobbit");
        dbBoek.setAuteur("Tolkien");
        dbBoek.setIsbn("12345");

        // Het boek dat via de import binnenkomt heeft 2 extra exemplaren
        Boek importBoek = new Boek();
        importBoek.setTitel("De Hobbit");
        importBoek.setAuteur("Tolkien");
        importBoek.setIsbn("12345");
        importBoek.setImportAantal(2);

        when(boekRepository.findByIsbn("12345")).thenReturn(Optional.of(dbBoek));

        // Act
        int result = boekImportService.bevestigImport(List.of(importBoek));

        // Assert
        assertEquals(2, result, "De methode moet 2 extra exemplaren rapporteren");
        // Bij een bestaand boek worden er 2 extra exemplaren bijgemaakt (geen nieuwe boek-rij).
        verify(exemplaarService, times(1)).genereerExemplaren(dbBoek, 2);
        verify(boekRepository, times(1)).save(dbBoek); // We slaan het geüpdatete DB boek op, geen nieuwe!
    }
}