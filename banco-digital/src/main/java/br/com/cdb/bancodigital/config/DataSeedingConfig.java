package br.com.cdb.bancodigital.config;

import br.com.cdb.bancodigital.entity.Seguro;
import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import br.com.cdb.bancodigital.repository.SeguroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class DataSeedingConfig {

    // Este @Bean cria um CommandLineRunner. O Spring vai executar o método run()
    // automaticamente assim que a aplicação iniciar.
    @Bean
    CommandLineRunner initDatabase(SeguroRepository seguroRepository) {
        return args -> {
            // Verifica se a tabela de seguros já tem dados para não inserir duplicatas
            if (seguroRepository.count() == 0) {
                System.out.println("Pré-carregando dados de Seguros...");

                // O campo custoMensal na entidade Seguro é usado aqui como um valor base/referência.
                // A lógica de cálculo real (baseada em percentual) está no SeguroService.
                Seguro seguroFraude = new Seguro();
                seguroFraude.setTipoSeguro(TipoSeguro.FRAUDE);
                seguroFraude.setCustoMensal(new BigDecimal("10.00")); // Custo base de referência

                Seguro seguroViagem = new Seguro();
                seguroViagem.setTipoSeguro(TipoSeguro.VIAGEM);
                seguroViagem.setCustoMensal(new BigDecimal("50.00")); // Custo base de referência

                seguroRepository.saveAll(Arrays.asList(seguroFraude, seguroViagem));

                System.out.println("Dados de Seguros carregados.");
            }
        };
    }
}