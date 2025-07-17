package br.com.cdb.bancodigital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DEBITO")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartaoDebito extends Cartao {

    @Column(name = "limite_diario", nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteDiario;

    @Column(name = "gasto_diario", nullable = false, precision = 12, scale = 2)
    private BigDecimal gastoDiario = BigDecimal.ZERO;
}
