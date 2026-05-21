package ap.be.backend.exception;

public class OpenLeningenBlokkadeException extends RuntimeException {
    public OpenLeningenBlokkadeException(int aantalOpenLeningen) {
        super("Er zijn nog " + aantalOpenLeningen + " open uitleningen voor vertrekkende leerlingen. " +
              "Gebruik forceerVerloren=true om deze als verloren te markeren en de rollover toch door te zetten.");
    }
}
