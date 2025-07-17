package br.com.cdb.bancodigital.entity;

import br.com.cdb.bancodigital.entity.enums.StatusApolice;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "apolice")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "cartaoCoberto")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Apolice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String numero;

    @Column(name = "data_contratacao", nullable = false)
    private LocalDate dataContratacao;

    @Column(name = "data_fim_vigencia", nullable = false)
    private LocalDate dataFimVigencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusApolice status;

    @Column(name = "valor_apolice", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorApolice;

    @OneToOne(mappedBy = "apolice")
    private CartaoCredito cartaoCoberto;

    @Column(columnDefinition = "TEXT")
    private String condicoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seguro_id", nullable = false)
    private Seguro seguro;
}
