package br.com.cdb.bancodigital.repository;

import br.com.cdb.bancodigital.entity.Apolice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import java.util.List;
import java.util.Optional;

public interface ApoliceRepository extends JpaRepository<Apolice, Long> {
    @Query("SELECT a FROM Apolice a WHERE a.cartaoCoberto.conta.cliente.id = :clienteId AND a.status = 'ATIVA'")
    List<Apolice> findApolicesAtivasByClienteId(Long clienteId);

    @Query("SELECT a FROM Apolice a WHERE a.cartaoCoberto.conta.cliente.id = :clienteId AND a.seguro.tipoSeguro = :tipoSeguro AND a.status = 'ATIVA'")
    Optional<Apolice> findApoliceAtivaByClienteIdAndTipo(Long clienteId,  TipoSeguro tipoSeguro);
}
