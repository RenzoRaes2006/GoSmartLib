package ap.be.backend.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.exception.InvalidImportDataException;

/**
 * Verwerkt het importeren van boeken via een Excel-bestand (.xlsx of .xls).
 *
 * Het importproces verloopt in twee stappen:
 *   1. Voorvertoning: het Excel-bestand wordt ingelezen en getoond aan de beheerder,
 *      zonder iets op te slaan in de database.
 *   2. Bevestiging: de goedgekeurde boeken worden definitief opgeslagen. Bestaat een
 *      boek al in de database, dan worden enkel de exemplaren bijgeteld.
 */
@Service
public class BoekImportService {

    private final BoekRepository boekRepository;

    public BoekImportService(BoekRepository boekRepository) {
        this.boekRepository = boekRepository;
    }

    /**
     * Leest een Excel-bestand in en geeft de inhoud terug als voorvertoning.
     *
     * Slaat niets op in de database. Werpt een uitzondering als het bestand
     * geen geldig Excel-formaat heeft.
     *
     * @param file het geüploade Excel-bestand
     * @return lijst van Boek-objecten op basis van de bestandsinhoud
     * @throws IllegalArgumentException als het bestand geen .xlsx of .xls is
     */
    public List<Boek> parseerVoorPreview(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && (filename.endsWith(".xls") || filename.endsWith(".xlsx"))) {
            return extractBoekenUitExcel(file);
        } else {
            throw new IllegalArgumentException(
                    "Ongeldig bestandstype. Alleen Excel (.xlsx of .xls) wordt ondersteund.");
        }
    }

    /**
     * Slaat de goedgekeurde boeken definitief op in de database.
     *
     * Bestaat een boek al (gecontroleerd op ISBN en daarna op titel + auteur),
     * dan worden enkel de exemplaren bijgeteld. Ontbreekt het ISBN bij het
     * bestaande boek maar heeft het nieuwe wel een ISBN, dan wordt het ISBN
     * aangevuld. Nieuwe boeken worden volledig opgeslagen.
     *
     * @param goedgekeurdeBoeken de lijst van boeken die de beheerder heeft goedgekeurd
     * @return het totaal aantal toegevoegde exemplaren
     */
    public int bevestigImport(List<Boek> goedgekeurdeBoeken) {
        int totaalExemplarenToegevoegd = 0;

        for (Boek nieuwBoek : goedgekeurdeBoeken) {
            Optional<Boek> bestaandBoekOpt = Optional.empty();

            if (nieuwBoek.getIsbn() != null && !nieuwBoek.getIsbn().trim().isEmpty()) {
                bestaandBoekOpt = boekRepository.findByIsbn(nieuwBoek.getIsbn());
            }

            if (bestaandBoekOpt.isEmpty()) {
                bestaandBoekOpt = boekRepository.findByTitelAndAuteur(nieuwBoek.getTitel(), nieuwBoek.getAuteur());
            }

            int toeTeVoegenExemplaren = nieuwBoek.getAantalExemplaren() > 0 ? nieuwBoek.getAantalExemplaren() : 1;

            if (bestaandBoekOpt.isPresent()) {
                Boek bestaandBoek = bestaandBoekOpt.get();
                bestaandBoek.setAantalExemplaren(bestaandBoek.getAantalExemplaren() + toeTeVoegenExemplaren);

                if ((bestaandBoek.getIsbn() == null || bestaandBoek.getIsbn().isEmpty())
                        && nieuwBoek.getIsbn() != null && !nieuwBoek.getIsbn().isEmpty()) {
                    bestaandBoek.setIsbn(nieuwBoek.getIsbn());
                }

                boekRepository.save(bestaandBoek);
            } else {
                nieuwBoek.setAantalExemplaren(toeTeVoegenExemplaren);
                boekRepository.save(nieuwBoek);
            }

            totaalExemplarenToegevoegd += toeTeVoegenExemplaren;
        }

        return totaalExemplarenToegevoegd;
    }

    /**
     * Leest het Excel-bestand rij voor rij uit en zet elke rij om naar een Boek-object.
     *
     * De eerste rij wordt behandeld als kolomnamen (header). Lege rijen worden
     * overgeslagen. Als een rij geen titel of auteur heeft, wordt een uitzondering
     * gegooid. Als een boek al in de database staat, wordt het gemarkeerd met
     * bestaatAl=true zodat de frontend dit kan tonen in de voorvertoning.
     *
     * @param file het te verwerken Excel-bestand
     * @return lijst van Boek-objecten op basis van de bestandsinhoud
     * @throws InvalidImportDataException als verplichte velden ontbreken of het
     *         bestand niet gelezen kan worden
     */
    private List<Boek> extractBoekenUitExcel(MultipartFile file) {
        List<Boek> boekenOmOpTeSlaan = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rijNummer = 1;
            boolean isHeader = true;
            Map<String, Integer> headerMap = new HashMap<>();

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                if (isHeader) {
                    for (Cell cell : currentRow) {
                        headerMap.put(dataFormatter.formatCellValue(cell).trim(), cell.getColumnIndex());
                    }
                    isHeader = false;
                    continue;
                }

                rijNummer++;

                boolean isEmptyRow = true;
                for (int c = currentRow.getFirstCellNum(); c < currentRow.getLastCellNum(); c++) {
                    Cell cell = currentRow.getCell(c);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        isEmptyRow = false;
                        break;
                    }
                }
                if (isEmptyRow)
                    continue;

                try {
                    Boek boek = new Boek();
                    String titel = getWaarde(currentRow, headerMap.get("Titel"), dataFormatter);
                    String auteur = getWaarde(currentRow, headerMap.get("Auteur"), dataFormatter);
                    String isbn = getWaarde(currentRow, headerMap.get("ISBN"), dataFormatter);
                    if (isbn != null) {
                        isbn = isbn.trim();

                        // Excel slaat lange nummers soms op in wetenschappelijke notatie (bv. 9.78E12).
                        // BigDecimal zet dit terug naar de volledige numerieke string.
                        if (isbn.toUpperCase().contains("E")) {
                            try {
                                String parseableIsbn = isbn.replace(",", ".");
                                isbn = new java.math.BigDecimal(parseableIsbn).toPlainString();
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                    if (titel == null || titel.isEmpty())
                        throw new InvalidImportDataException("Titel ontbreekt in rij " + rijNummer);
                    if (auteur == null || auteur.isEmpty())
                        throw new InvalidImportDataException("Auteur ontbreekt in rij " + rijNummer);

                    Optional<Boek> dbCheck = Optional.empty();
                    if (isbn != null && !isbn.isEmpty()) {
                        dbCheck = boekRepository.findByIsbn(isbn);
                    }
                    if (dbCheck.isEmpty()) {
                        dbCheck = boekRepository.findByTitelAndAuteur(titel, auteur);
                    }

                    if (dbCheck.isPresent()) {
                        boek.setBestaatAl(true);
                        boek.setTitel(dbCheck.get().getTitel());
                        boek.setAuteur(dbCheck.get().getAuteur());
                        boek.setIsbn(dbCheck.get().getIsbn());

                        String exemplarenStr = getWaarde(currentRow, headerMap.get("Aantal exemplaren"), dataFormatter);
                        boek.setAantalExemplaren(
                                (exemplarenStr != null && !exemplarenStr.isEmpty()) ? Integer.parseInt(exemplarenStr)
                                        : 1);

                    } else {
                        boek.setBestaatAl(false);
                        boek.setTitel(titel);
                        boek.setAuteur(auteur);
                        if (isbn == null || isbn.trim().isEmpty()) {
                            boek.setIsbn(null);
                        } else {
                            boek.setIsbn(isbn.trim());
                        }
                        boek.setUitgever(getWaarde(currentRow, headerMap.get("Uitgever"), dataFormatter));
                        boek.setUitgaveJaar(getWaarde(currentRow, headerMap.get("Uitgavejaar"), dataFormatter));

                        String paginaStr = getWaarde(currentRow, headerMap.get("Pagina"), dataFormatter);
                        boek.setPagina((paginaStr != null && !paginaStr.isEmpty()) ? Integer.parseInt(paginaStr) : 0);

                        String exemplarenStr = getWaarde(currentRow, headerMap.get("Aantal exemplaren"), dataFormatter);
                        boek.setAantalExemplaren(
                                (exemplarenStr != null && !exemplarenStr.isEmpty()) ? Integer.parseInt(exemplarenStr)
                                        : 1);

                        String genreString = getWaarde(currentRow, headerMap.get("Genre"), dataFormatter);
                        boek.setGenre(genreString != null && !genreString.isEmpty()
                                ? new ArrayList<>(List.of(genreString.split("\\s*,\\s*")))
                                : new ArrayList<>());

                        String thematischeTagString = getWaarde(currentRow, headerMap.get("Thematische tags"),
                                dataFormatter);
                        boek.setThematischeTag(thematischeTagString != null && !thematischeTagString.isEmpty()
                                ? new ArrayList<>(List.of(thematischeTagString.split("\\s*,\\s*")))
                                : new ArrayList<>());

                        String populariteitStr = getWaarde(currentRow, headerMap.get("Populariteit"), dataFormatter);
                        if (populariteitStr != null && !populariteitStr.isEmpty()) {
                            populariteitStr = populariteitStr.replace(",", ".");
                            boek.setPopulariteit(Double.parseDouble(populariteitStr));
                        } else {
                            boek.setPopulariteit(0.0);
                        }

                        boek.setBeschrijving(getWaarde(currentRow, headerMap.get("Beschrijving"), dataFormatter));
                        boek.setLocatie(getWaarde(currentRow, headerMap.get("Locatie"), dataFormatter));
                        boek.setAfbeelding(getWaarde(currentRow, headerMap.get("Afbeelding URL"), dataFormatter));
                        boek.setLeesniveau(getWaarde(currentRow, headerMap.get("Leesniveau"), dataFormatter));
                    }

                    boekenOmOpTeSlaan.add(boek);

                } catch (InvalidImportDataException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvalidImportDataException(
                            "Onverwachte fout in rij " + rijNummer + ": " + e.getMessage());
                }
            }

            return boekenOmOpTeSlaan;

        } catch (InvalidImportDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidImportDataException(
                    "Kan bestand niet inlezen. Controleer of het een geldig Excel-bestand is.");
        }
    }

    /**
     * Leest de celwaarde op een bepaalde kolompositie uit een rij.
     *
     * Numerieke cellen worden via BigDecimal omgezet naar een exacte string
     * om afrondingsfouten bij grote getallen (zoals ISBNs) te vermijden.
     * Eindigt de waarde op ".0", dan wordt dat achtervoegsel verwijderd.
     *
     * @param row       de te lezen Excel-rij
     * @param colIndex  de kolomindex (null als de kolom niet in de header staat)
     * @param formatter de DataFormatter voor niet-numerieke cellen
     * @return de celwaarde als string, of null als de cel leeg of afwezig is
     */
    private String getWaarde(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null)
            return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            String exacteWaarde = java.math.BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();

            if (exacteWaarde.endsWith(".0")) {
                return exacteWaarde.substring(0, exacteWaarde.length() - 2);
            }
            return exacteWaarde;
        }

        return formatter.formatCellValue(cell).trim();
    }
}
