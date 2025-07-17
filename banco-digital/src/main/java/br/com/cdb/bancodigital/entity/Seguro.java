package br.com.cdb.bancodigital.entity;

import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "seguro")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "apolices")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seguro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_seguro", nullable = false, length = 20)
    private TipoSeguro tipoSeguro;

    @Column(name = "custo_mensal", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoMensal;

    @OneToMany(
            mappedBy = "seguro",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Apolice> apolices = new ArrayList<>();
}
