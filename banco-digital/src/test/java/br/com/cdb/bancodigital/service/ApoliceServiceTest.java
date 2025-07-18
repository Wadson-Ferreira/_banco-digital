package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ApoliceMapper;
import br.com.cdb.bancodigital.dto.request.RenovarSeguroViagemRequestDTO;
import br.com.cdb.bancodigital.dto.response.ApoliceResponseDTO;
import br.com.cdb.bancodigital.entity.Apolice;
import br.com.cdb.bancodigital.entity.CartaoCredito;
import br.com.cdb.bancodigital.entity.Seguro;
import br.com.cdb.bancodigital.entity.enums.StatusApolice;
import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import br.com.cdb.bancodigital.repository.ApoliceRepository;
import br.com.cdb.bancodigital.repository.CartaoRepository;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApoliceServiceTest {

    @Mock
    private ApoliceRepository apoliceRepository;
    @Mock
    private CartaoRepository cartaoRepository;
    @Mock
    private ApoliceMapper apoliceMapper;

    @InjectMocks
    private ApoliceService apoliceService;

    private Apolice apolice;
    private CartaoCredito cartao;

    @BeforeEach
    void setUp() {
        // Entidade Apolice fraudes
        apolice = new Apolice();
        apolice.setId(100L);
        apolice.setNumero("AP-100");
        apolice.setDataContratacao(LocalDate.now().minusDays(1));
        apolice.setDataFimVigencia(LocalDate.now().plusDays(29));
        apolice.setStatus(StatusApolice.ATIVA);
        apolice.setValorApolice(new BigDecimal("50.00"));
        apolice.setCondicoes("Cobertura contra fraudes");
        // Seguro tipo de fraude
        Seguro seguro = new Seguro();
        seguro.setId(10L);
        seguro.setTipoSeguro(TipoSeguro.FRAUDE);
        apolice.setSeguro(seguro);
        // CartaoCredito vinculado
        cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setLimiteCredito(new BigDecimal("1000.00"));
        cartao.setValorGastoMes(new BigDecimal("50.00"));
        apolice.setCartaoCoberto(cartao);
        cartao.setApolice(apolice);
    }

    // cancelarSeguro
    @Test
    void cancelarSeguro_notFraudType_throwsBusinessException() {
        apolice.getSeguro().setTipoSeguro(TipoSeguro.VIAGEM);
        when(apoliceRepository.findById(100L)).thenReturn(Optional.of(apolice));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> apoliceService.cancelarSeguro(100L));
        assertEquals("Apenas apólices de seguro contra fraude podem ser canceladas.", ex.getMessage());
    }

    @Test
    void cancelarSeguro_notActive_throwsBusinessException() {
        apolice.setStatus(StatusApolice.CANCELADA);
        when(apoliceRepository.findById(100L)).thenReturn(Optional.of(apolice));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> apoliceService.cancelarSeguro(100L));
        assertEquals("Apenas apólices ativas podem ser canceladas", ex.getMessage());
    }

    @Test
    void cancelarSeguro_success_updatesValues() {
        when(apoliceRepository.findById(100L)).thenReturn(Optional.of(apolice));

        apoliceService.cancelarSeguro(100L);

        // Status deve ser CANCELADA
        assertEquals(StatusApolice.CANCELADA, apolice.getStatus());
        // Cartao deve remover apolice
        assertNull(cartao.getApolice());
        // Gasto mensal reduzido corretamente (50 - estorno)
        BigDecimal expectedGasto = new BigDecimal("1.67"); // 50*(1/30) arredondado
        assertEquals(0, cartao.getValorGastoMes().compareTo(expectedGasto));
        verify(apoliceRepository).save(apolice);
        verify(cartaoRepository).save(cartao);
    }

    @Test
    void cancelarSeguro_notFound_throwsObjectNotFoundException() {
        when(apoliceRepository.findById(200L)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class,
                () -> apoliceService.cancelarSeguro(200L));
    }

    // renovarSeguroViagem
    @Test
    void renovarSeguroViagem_notTravelType_throwsBusinessException() {
        // Muda tipo para FRAUDE para erro
        apolice.getSeguro().setTipoSeguro(TipoSeguro.FRAUDE);
        when(apoliceRepository.findById(100L)).thenReturn(Optional.of(apolice));
        RenovarSeguroViagemRequestDTO dto = new RenovarSeguroViagemRequestDTO();
        dto.setDiasAdicionais(5);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> apoliceService.renovarSeguroViagem(100L, dto));
        assertEquals("Esta função é apenas para renovar seguros de viagem.", ex.getMessage());
    }

    @Test
    void renovarSeguroViagem_notActive_throwsBusinessException() {
        apolice.getSeguro().setTipoSeguro(TipoSeguro.VIAGEM);
        apolice.setStatus(StatusApolice.CANCELADA);
        when(apoliceRepository.findById(100L)).thenReturn(Optional.of(apolice));
        RenovarSeguroViagemRequestDTO dto = new RenovarSeguroViagemRequestDTO();
        dto.setDiasAdicionais(3);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> apoliceService.renovarSeguroViagem(100L, dto));
        assertEquals("Apenas apólices ativas podem ser renovadas.", ex.getMessage());
    }

    @Test
    void renovarSeguroViagem_success_returnsDtoAndUpdates() {
        // Prepara apolice viagem
        apolice.getSeguro().setTipoSeguro(TipoSeguro.VIAGEM);
        apolice.setStatus(StatusApolice.ATIVA);
        apolice.setDataFimVigencia(LocalDate.now());
        apolice.setCondicoes("Original");
        cartao.setValorGastoMes(BigDecimal.ZERO);
        cartao.setLimiteCredito(new BigDecimal("1000.00"));

        when(apoliceRepository.findById(100L)).thenReturn(Optional.of(apolice));
        when(apoliceRepository.save(apolice)).thenReturn(apolice);
        when(apoliceMapper.toDto(apolice)).thenReturn(new ApoliceResponseDTO());

        RenovarSeguroViagemRequestDTO dto = new RenovarSeguroViagemRequestDTO();
        dto.setDiasAdicionais(5);
        ApoliceResponseDTO response = apoliceService.renovarSeguroViagem(100L, dto);

        // Verifica alteração de data fim
        assertEquals(LocalDate.now().plusDays(5), apolice.getDataFimVigencia());
        // Verifica condições concatenadas
        assertTrue(apolice.getCondicoes().contains("Renovado por mais 5 dias."));
        // Verifica gasto mensal incrementado (5 dias * 1000*0.01=10/dia =>50)
        assertEquals(0, cartao.getValorGastoMes().compareTo(new BigDecimal("50.00")));
        verify(cartaoRepository).save(cartao);
        verify(apoliceRepository).save(apolice);
        assertNotNull(response);
    }

    // findApolicesByCliente
    @Test
    void findApolicesByCliente_returnsDtoList() {
        when(apoliceRepository.findApolicesAtivasByClienteId(42L))
                .thenReturn(Collections.singletonList(apolice));
        ApoliceResponseDTO dto = new ApoliceResponseDTO();
        when(apoliceMapper.toDto(apolice)).thenReturn(dto);

        var list = apoliceService.findApolicesByCliente(42L);
        assertEquals(1, list.size());
        assertSame(dto, list.getFirst());
    }

    @Test
    void findApolicesByCliente_emptyList() {
        when(apoliceRepository.findApolicesAtivasByClienteId(99L)).thenReturn(Collections.emptyList());
        var list = apoliceService.findApolicesByCliente(99L);
        assertTrue(list.isEmpty());
    }
}
