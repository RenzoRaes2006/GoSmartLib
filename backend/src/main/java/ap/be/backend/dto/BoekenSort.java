package ap.be.backend.dto;

import org.springframework.data.domain.Sort;

public record BoekenSort(String sorteerOp, String sorteerRichting) {

    public Sort toSort() {
        if (sorteerOp != null && sorteerRichting != null) {
            return Sort.by(Sort.Direction.fromString(sorteerRichting), sorteerOp);
        }
        return Sort.by(Sort.Direction.ASC, "id");
    }
}
