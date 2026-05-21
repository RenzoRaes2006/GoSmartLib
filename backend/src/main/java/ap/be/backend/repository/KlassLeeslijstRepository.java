package ap.be.backend.repository;

import ap.be.backend.entity.KlassLeeslijst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KlassLeeslijstRepository extends JpaRepository<KlassLeeslijst, Long> {

    // Actieve (niet-gearchiveerde) leeslijsten van een leerkracht — voor de normale weergave
    List<KlassLeeslijst> findByLeerkrachtIdAndGearchiveerdFalseOrderByAangemaaktOpDesc(Long leerkrachtId);

    // Gearchiveerde leeslijsten van één leerkracht — voor zijn eigen archiefpagina
    List<KlassLeeslijst> findByLeerkrachtIdAndGearchiveerdTrueOrderByAangemaaktOpDesc(Long leerkrachtId);

    // Alle gearchiveerde leeslijsten van alle leerkrachten — alleen voor de bibbeheerder
    List<KlassLeeslijst> findByGearchiveerdTrueOrderByAangemaaktOpDesc();

    // Voor leerlingen: alleen zichtbare, niet-gearchiveerde klas-leeslijsten tonen
    List<KlassLeeslijst> findByKlasAndZichtbaarTrueAndGearchiveerdFalseOrderByAangemaaktOpDesc(String klas);

    // Voor leerlingen: alleen zichtbare, niet-gearchiveerde persoonlijk toegewezen leeslijsten tonen
    List<KlassLeeslijst> findByLeerlingIdAndZichtbaarTrueAndGearchiveerdFalseOrderByAangemaaktOpDesc(Long leerlingId);

    // Klas-gebonden leeslijsten die nog actief zijn — gebruikt bij rollover om te archiveren
    List<KlassLeeslijst> findByLeerkracht_IdInAndKlasIsNotNullAndGearchiveerdFalse(Collection<Long> leerkrachtIds);
}
