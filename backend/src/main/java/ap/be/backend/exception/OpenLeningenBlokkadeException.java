package ap.be.backend.exception;

public class OpenLeningenBlokkadeException extends RuntimeException {
    public OpenLeningenBlokkadeException(int aantalOpenLeningen) {
        super("Er " + (aantalOpenLeningen == 1 ? "is nog 1 open uitlening" : "zijn nog " + aantalOpenLeningen + " open uitleningen") +
              " bij vertrekkende leerlingen. Vink 'Markeer als verloren' aan om de rollover door te zetten.");
    }
}
