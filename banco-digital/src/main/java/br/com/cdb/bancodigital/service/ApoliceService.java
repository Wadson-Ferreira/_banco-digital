package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ApoliceMapper;
import br.com.cdb.bancodigital.dto.request.RenovarSeguroViagemRequestDTO;
import br.com.cdb.bancodigital.dto.response.ApoliceResponseDTO;
import br.com.cdb.bancodigital.entity.Apolice;
import br.com.cdb.bancodigital.entity.CartaoCredito;
import br.com.cdb.bancodigital.entity.enums.StatusApolice;
import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.ApoliceRepository;
import br.com.cdb.bancodigital.repository.CartaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApoliceService {
    private final ApoliceRepository apoliceRepository;
    private final CartaoRepository cartaoRepository;
    private final ApoliceMapper apoliceMapper;

    public ApoliceService(ApoliceRepository apoliceRepository, CartaoRepository cartaoRepository, ApoliceMapper apoliceMapper) {
        this.apoliceRepository = apoliceRepository;
        this.cartaoRepository = cartaoRepository;
        this.apoliceMapper = apoliceMapper;
    }

    @Transactional
    public void cancelarSeguro(Long apoliceId) {
        Apolice apolice = findById(apoliceId);

        if (apolice.getSeguro().getTipoSeguro() != TipoSeguro.FRAUDE) {
            throw new BusinessException("Apenas apólices de seguro contra fraude podem ser canceladas.");
        }
        if (apolice.getStatus() != StatusApolice.ATIVA) {
            throw new BusinessException("Apenas apólices ativas podem ser canceladas");
        }

        long diasTotais = ChronoUnit.DAYS.between(apolice.getDataContratacao(), apolice.getDataFimVigencia());
        long diasUsados = ChronoUnit.DAYS.between(apolice.getDataContratacao(), LocalDate.now());
        diasUsados = Math.max(diasUsados, 1);

        CartaoCredito cartao = apolice.getCartaoCoberto();
        BigDecimal custoTotalPago = cartao.getLimiteCredito().multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP);

        if (diasTotais > 0) {
            BigDecimal custoPorDia = custoTotalPago.divide(new BigDecimal(diasTotais), 4, RoundingMode.HALF_UP);
            BigDecimal valorEstorno = custoTotalPago.subtract(custoPorDia.multiply(new BigDecimal(diasUsados)))
                    .setScale(2, RoundingMode.HALF_UP);

            if (valorEstorno.compareTo(BigDecimal.ZERO) > 0) {
                cartao.setValorGastoMes(cartao.getValorGastoMes().subtract(valorEstorno));
            }
        }
        apolice.setStatus(StatusApolice.CANCELADA);
        cartao.setApolice(null);

        apoliceRepository.save(apolice);
        cartaoRepository.save(cartao);
    }

    @Transactional
    public ApoliceResponseDTO renovarSeguroViagem(Long apoliceId, RenovarSeguroViagemRequestDTO dto) {
        Apolice apolice = findById(apoliceId);
        if (apolice.getSeguro().getTipoSeguro() != TipoSeguro.VIAGEM){
            throw new BusinessException("Esta função é apenas para renovar seguros de viagem.");
        }
        if (apolice.getStatus() != StatusApolice.ATIVA){
            throw new BusinessException("Apenas apólices ativas podem ser renovadas.");
        }

        CartaoCredito cartao = apolice.getCartaoCoberto();
        BigDecimal custoDiario = cartao.getLimiteCredito().multiply(new BigDecimal("0.01"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal custoRenovacao = custoDiario.multiply(new BigDecimal(dto.getDiasAdicionais()));

        cartao.setValorGastoMes(cartao.getValorGastoMes().add(custoRenovacao));

        apolice.setDataFimVigencia(apolice.getDataFimVigencia().plusDays(dto.getDiasAdicionais()));
        apolice.setCondicoes(apolice.getCondicoes() + " | Renovado por mais "
                + dto.getDiasAdicionais() + " dias.");

        cartaoRepository.save(cartao);
        apolice = apoliceRepository.save(apolice);

        return apoliceMapper.toDto(apolice);
    }

    @Transactional(readOnly = true)
    public List<ApoliceResponseDTO> findApolicesByCliente(Long clienteId) {
        return apoliceRepository.findApolicesAtivasByClienteId(clienteId)
                .stream()
                .map(apoliceMapper::toDto)
                .collect(Collectors.toList());
    }

    private Apolice findById(Long id) {
        return apoliceRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Apólice não encontrada! Id: " + id));
    }
}