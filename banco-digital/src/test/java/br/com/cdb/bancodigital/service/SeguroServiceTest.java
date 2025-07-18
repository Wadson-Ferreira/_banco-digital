package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ApoliceMapper;
import br.com.cdb.bancodigital.dto.request.ContratarSeguroRequestDTO;
import br.com.cdb.bancodigital.dto.response.ApoliceResponseDTO;
import br.com.cdb.bancodigital.entity.*;
import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.ApoliceRepository;
import br.com.cdb.bancodigital.repository.CartaoRepository;
import br.com.cdb.bancodigital.repository.SeguroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeguroServiceTest {

    @Mock
    private SeguroRepository seguroRepository;
    @Mock
    private ApoliceRepository apoliceRepository;
    @Mock
    private CartaoRepository cartaoRepository;
    @Mock
    private CartaoService cartaoService;
    @Mock
    private ApoliceMapper apoliceMapper;

    @InjectMocks
    private SeguroService seguroService;

    private CartaoCredito cartaoCredito;
    private Seguro seguroFraude;
    private Seguro seguroViagem;

    @BeforeEach
    void setUp() {
        cartaoCredito = new CartaoCredito();
        cartaoCredito.setId(1L);
        cartaoCredito.setLimiteCredito(new BigDecimal("1000.00"));
        cartaoCredito.setValorGastoMes(BigDecimal.ZERO);
        // mock Conta
        Conta conta = mock(Conta.class);
        Cliente cliente = new Cliente();
        cliente.setId(42L);
        lenient().when(conta.getCliente()).thenReturn(cliente);
        cartaoCredito.setConta(conta);

        seguroFraude = new Seguro();
        seguroFraude.setId(10L);
        seguroFraude.setTipoSeguro(TipoSeguro.FRAUDE);
        seguroFraude.setCustoMensal(new BigDecimal("50.00"));

        seguroViagem = new Seguro();
        seguroViagem.setId(20L);
        seguroViagem.setTipoSeguro(TipoSeguro.VIAGEM);
        seguroViagem.setCustoMensal(new BigDecimal("30.00"));
    }

    // Error scenarios

    @Test
    void contratarSeguro_nonCreditCard_throwsBusinessException() {
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(10L);
        when(cartaoService.findById(1L)).thenReturn(mock(CartaoDebito.class));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> seguroService.contratarSeguro(dto));
        assertEquals("Seguros só podem ser contratados para cartões de crédito", ex.getMessage());
    }

    @Test
    void contratarSeguro_cardAlreadyHasApolice_throwsBusinessException() {
        cartaoCredito.setApolice(new Apolice());
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(10L);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> seguroService.contratarSeguro(dto));
        assertEquals("Este cartão já possui uma apólice de seguro ativa.", ex.getMessage());
    }

    @Test
    void contratarSeguro_seguroNotFound_throwsObjectNotFoundException() {
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(99L);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);
        when(seguroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> seguroService.contratarSeguro(dto));
    }

    @Test
    void contratarSeguro_viagemWithoutDays_throwsBusinessException() {
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(20L);
        dto.setDiasDeViagem(0);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);
        when(seguroRepository.findById(20L)).thenReturn(Optional.of(seguroViagem));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> seguroService.contratarSeguro(dto));
        assertTrue(ex.getMessage().contains("obrigatório informar um número de dias positivo"));
    }

    @Test
    void contratarSeguro_viagemExistingFraudPolicy_throwsBusinessException() {
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(20L);
        dto.setDiasDeViagem(5);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);
        when(seguroRepository.findById(20L)).thenReturn(Optional.of(seguroViagem));
        when(apoliceRepository.findApoliceAtivaByClienteIdAndTipo(42L, TipoSeguro.FRAUDE))
                .thenReturn(Optional.of(new Apolice()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> seguroService.contratarSeguro(dto));
        // Verifica mensagem completa do aviso de cobertura pré-existente
        assertTrue(ex.getMessage().contains("Aviso: Você já possui um seguro contra fraudes"));
    }

    @Test
    void contratarSeguro_fraudeLimitInsufficient_throwsBusinessException() {
        cartaoCredito.setValorGastoMes(new BigDecimal("980.00"));
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(10L);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);
        when(seguroRepository.findById(10L)).thenReturn(Optional.of(seguroFraude));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> seguroService.contratarSeguro(dto));
        assertTrue(ex.getMessage().contains("limite de crédito insuficiente"));
    }

    @Test
    void contratarSeguro_viagemLimitInsufficient_throwsBusinessException() {
        cartaoCredito.setValorGastoMes(new BigDecimal("950.00"));
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(20L);
        dto.setDiasDeViagem(60);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);
        when(seguroRepository.findById(20L)).thenReturn(Optional.of(seguroViagem));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> seguroService.contratarSeguro(dto));
        assertTrue(ex.getMessage().contains("limite de crédito insuficiente"));
    }

    // Success scenarios

    @Test
    void contratarSeguro_fraudeSuccess_returnsDto() {
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(10L);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);
        when(seguroRepository.findById(10L)).thenReturn(Optional.of(seguroFraude));
        when(apoliceMapper.toDto(any(Apolice.class))).thenReturn(new ApoliceResponseDTO());

        ApoliceResponseDTO result = seguroService.contratarSeguro(dto);

        verify(cartaoRepository).save(cartaoCredito);
        assertNotNull(result);
    }

    @Test
    void contratarSeguro_viagemSuccess_returnsDto() {
        ContratarSeguroRequestDTO dto = new ContratarSeguroRequestDTO();
        dto.setCartaoId(1L);
        dto.setSeguroId(20L);
        dto.setDiasDeViagem(5);
        when(cartaoService.findById(1L)).thenReturn(cartaoCredito);
        when(seguroRepository.findById(20L)).thenReturn(Optional.of(seguroViagem));
        when(apoliceRepository.findApoliceAtivaByClienteIdAndTipo(42L, TipoSeguro.FRAUDE))
                .thenReturn(Optional.empty());
        when(apoliceMapper.toDto(any(Apolice.class))).thenReturn(new ApoliceResponseDTO());

        ApoliceResponseDTO result = seguroService.contratarSeguro(dto);

        verify(cartaoRepository).save(cartaoCredito);
        assertNotNull(result);
    }

    @Test
    void listarSegurosDisponiveis_returnsDtoList() {
        Seguro s1 = new Seguro(); s1.setId(1L); s1.setTipoSeguro(TipoSeguro.FRAUDE); s1.setCustoMensal(new BigDecimal("20.00"));
        Seguro s2 = new Seguro(); s2.setId(2L); s2.setTipoSeguro(TipoSeguro.VIAGEM); s2.setCustoMensal(new BigDecimal("30.00"));
        when(seguroRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        var list = seguroService.listarSegurosDisponiveis();

        assertEquals(2, list.size());
        assertEquals(1L, list.getFirst().getId());
        assertEquals(TipoSeguro.FRAUDE, list.getFirst().getTipoSeguro());
    }
}
