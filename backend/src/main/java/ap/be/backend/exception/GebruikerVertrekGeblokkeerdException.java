package ap.be.backend.exception;

/**
 * Gegooid wanneer een leerling of leerkracht als vertrokken gemarkeerd wordt
 * terwijl er nog open uitleningen op zijn/haar naam staan.
 */
public class GebruikerVertrekGeblokkeerdException extends RuntimeException {
    public GebruikerVertrekGeblokkeerdException(int aantalOpenLeningen) {
        super("Vertrek geblokkeerd: er " +
                (aantalOpenLeningen == 1
                        ? "is nog 1 open uitlening"
                        : "zijn nog " + aantalOpenLeningen + " open uitleningen")
                + " op naam van deze gebruiker. Lever ze eerst in of markeer ze als verloren.");
    }
}
