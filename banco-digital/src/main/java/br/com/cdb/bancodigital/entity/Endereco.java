package br.com.cdb.bancodigital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Endereco {
    @Column(length = 100, nullable = false)
    private String rua;

    @Column(length = 10, nullable = false)
    private String numero;

    @Column(length = 50)
    private String complemento;

    @Column(length = 50, nullable = false)
    private String bairro;

    @Column(length = 50, nullable = false)
    private String cidade;

    @Column(length = 2, nullable = false)
    private String estado;

    @Column(length = 8, nullable = false)
    private String cep;
}
