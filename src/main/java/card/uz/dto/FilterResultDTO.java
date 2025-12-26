package card.uz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FilterResultDTO<T> {
    private List<T> content;
    private Long totalCount;
}
