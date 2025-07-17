package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ApoliceMapper;
import br.com.cdb.bancodigital.dto.request.ContratarSeguroRequestDTO;
import br.com.cdb.bancodigital.dto.response.ApoliceResponseDTO;
import br.com.cdb.bancodigital.dto.response.SeguroResponseDTO;
import br.com.cdb.bancodigital.entity.Apolice;
import br.com.cdb.bancodigital.entity.Cartao;
import br.com.cdb.bancodigital.entity.CartaoCredito;
import br.com.cdb.bancodigital.entity.Seguro;
import br.com.cdb.bancodigital.entity.enums.StatusApolice;
import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.ApoliceRepository;
import br.com.cdb.bancodigital.repository.CartaoRepository;
import br.com.cdb.bancodigital.repository.SeguroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeguroService {
    private final SeguroRepository seguroRepository;
    private final ApoliceRepository apoliceRepository;
    private final CartaoRepository cartaoRepository;
    private final CartaoService cartaoService;
    private final ApoliceMapper apoliceMapper;

    public SeguroService(SeguroRepository seguroRepository, ApoliceRepository apoliceRepository,
                         CartaoRepository cartaoRepository, CartaoService cartaoService, ApoliceMapper apoliceMapper) {
        this.seguroRepository = seguroRepository;
        this.apoliceRepository = apoliceRepository;
        this.cartaoRepository = cartaoRepository;
        this.cartaoService = cartaoService;
        this.apoliceMapper = apoliceMapper;
    }

    @Transactional
    public ApoliceResponseDTO contratarSeguro(ContratarSeguroRequestDTO dto){
        Cartao cartao = cartaoService.findById(dto.getCartaoId());
        if (!(cartao instanceof CartaoCredito cartaoCredito)){
            throw new BusinessException("Seguros só podem ser contratados para cartões de crédito");
        }
        if (cartaoCredito.getApolice() != null){
            throw new BusinessException("Este cartão já possui uma apólice de seguro ativa.");
        }
        Seguro seguro = seguroRepository.findById(dto.getSeguroId())
                .orElseThrow(() -> new ObjectNotFoundException("Tipo de seguro não encontrado! Id: "
                        + dto.getSeguroId()));

        if (seguro.getTipoSeguro() == TipoSeguro.VIAGEM) {
            Long clienteId = cartaoCredito.getConta().getCliente().getId();
            apoliceRepository.findApoliceAtivaByClienteIdAndTipo(clienteId, TipoSeguro.FRAUDE)
                    .ifPresent(apoliceExistente -> {
                        throw new BusinessException("Aviso: Você já possui um seguro contra fraudes. " +
                                "Lembre-se que o seguro fraude é válido apenas no seu estado de residência. " +
                                "Confirme se deseja prosseguir com a contratação do seguro viagem para cobertura nacional/internacional.");
                    });
        }

        BigDecimal valorCustoSeguro;
        BigDecimal valorCobertura;
        String condicoes;
        LocalDate dataFimVigencia;

        if (seguro.getTipoSeguro() == TipoSeguro.FRAUDE){
            valorCustoSeguro = cartaoCredito.getLimiteCredito().multiply(new BigDecimal("0.05"))
                    .setScale(2, RoundingMode.HALF_UP);

            valorCobertura = cartaoCredito.getLimiteCredito().multiply(new BigDecimal("0.90"))
                    .setScale(2,RoundingMode.HALF_UP);

            condicoes = "Cobertura contra fraudes e transações não autorizadas, até o limite de R$ "
                    + valorCobertura;

            dataFimVigencia = LocalDate.now().plusDays(30);

        } else if (seguro.getTipoSeguro() == TipoSeguro.VIAGEM){
            if (dto.getDiasDeViagem() == null || dto.getDiasDeViagem() <= 0) {
                throw new BusinessException
                        ("Para contratar o seguro viagem, é obrigatório informar um número de dias positivo.");
            }
            BigDecimal custoDiario = cartaoCredito.getLimiteCredito().multiply(new BigDecimal("0.01"))
                    .setScale(2, RoundingMode.HALF_UP);
            valorCustoSeguro = custoDiario.multiply(new BigDecimal(dto.getDiasDeViagem()));

            valorCobertura = cartaoCredito.getLimiteCredito();

            condicoes = "Cobertura total de despesas em caso de fraude ou golpe durante viagem valida por "
                    + dto.getDiasDeViagem() + " dias, até o valor máximo de R$ " + valorCobertura;

            dataFimVigencia = LocalDate.now().plusDays(dto.getDiasDeViagem());
        } else {
            throw new BusinessException("Tipo de seguro desconhecido ou não implementado");
        }

        BigDecimal limiteDisponivel = cartaoCredito.getLimiteCredito().subtract(cartaoCredito.getValorGastoMes());
        if (limiteDisponivel.compareTo(valorCustoSeguro) < 0) {
            throw new BusinessException
                    ("Não foi possível contratar o seguro: limite de crédito insuficiente para cobrir o custo do seguro.");
        }
        cartaoCredito.setValorGastoMes(cartaoCredito.getValorGastoMes().add(valorCustoSeguro));

        Apolice apolice = new Apolice();
        apolice.setNumero("AP-" + cartao.getId() + "-" + System.currentTimeMillis());
        apolice.setDataContratacao(LocalDate.now());
        apolice.setDataFimVigencia(dataFimVigencia);
        apolice.setStatus(StatusApolice.ATIVA);
        apolice.setValorApolice(valorCobertura);
        apolice.setCondicoes(condicoes);
        apolice.setSeguro(seguro);
        apolice.setCartaoCoberto(cartaoCredito);

        cartaoCredito.setApolice(apolice);
        cartaoRepository.save(cartaoCredito);

        return apoliceMapper.toDto(apolice);
    }

    @Transactional(readOnly = true)
    public List<SeguroResponseDTO> listarSegurosDisponiveis() {
        return seguroRepository.findAll().stream()
                .map(seguro -> new SeguroResponseDTO(seguro.getId(), seguro.getTipoSeguro(), seguro.getCustoMensal()))
                .collect(Collectors.toList());
    }
}
