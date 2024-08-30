package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDTO {
    int id;
    String code;
    String name;
    String sign;

    public CurrencyDTO(String code, String name, String sign) {
        this.code = code;
        this.name = name;
        this.sign = sign;
    }
}
