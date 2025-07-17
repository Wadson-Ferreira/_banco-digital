package br.com.cdb.bancodigital.dto.externo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BrasilApiCepDTO {
    private String cep;

    @JsonProperty("state")
    private String estado;

    @JsonProperty("city")
    private String cidade;

    @JsonProperty("neighborhood")
    private String bairro;

    @JsonProperty("street")
    private String rua;
}