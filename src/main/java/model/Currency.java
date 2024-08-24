package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Currency {
    int id;
    String code;
    String name;
    String sign;

    public Currency(String code, String name, String sign) {
        this.code = code;
        this.name = name;
        this.sign = sign;
    }
}
