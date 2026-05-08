package ap.be.backend.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageRequestParams(Integer page, Integer size) {

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page != null ? page : 0, size != null ? size : 10, sort);
    }
}
