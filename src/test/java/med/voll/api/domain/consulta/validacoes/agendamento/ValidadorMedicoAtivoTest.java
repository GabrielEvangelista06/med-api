package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.medico.Especialidade;
import med.voll.api.domain.medico.MedicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@SpringBootTest
@Profile("test")
class ValidadorMedicoAtivoTest
{
	@MockBean
	private MedicoRepository medicoRepository;

	private final ValidadorMedicoAtivo validador;

	public ValidadorMedicoAtivoTest(@Autowired ValidadorMedicoAtivo validador)
	{
		this.validador = validador;
	}

	private DadosAgendamentoConsulta criarDadosAgendamentoConsulta()
	{
		return new DadosAgendamentoConsulta(1L, 2L, LocalDateTime.now().plusDays(1), Especialidade.CARDIOLOGIA);
	}

	@Test
	@DisplayName("Não deve lançar exceção se a consulta for agendada com médico ativo")
	void validarConsultaComMedicoAtivo()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta();

		when(this.medicoRepository.findAtivoById(dadosAgendamentoConsulta.idMedico())).thenReturn(true);

		// Act & Assert
		assertDoesNotThrow(() -> validador.validar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Deve lançar exceção se a consulta for agendada com médico inativo")
	void validarConsultaComMedicoInativo()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta();

		when(medicoRepository.findAtivoById(dadosAgendamentoConsulta.idMedico())).thenReturn(false);

		// Act & Assert
		assertThrows(ValidacaoException.class, () -> validador.validar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Não deve lançar exceção se a consulta for agendada sem médico")
	void validarConsultaSemMedico()
	{
		// Arrange
		var dadosAgendamentoConsulta = new DadosAgendamentoConsulta(null, 2L, LocalDateTime.now().plusDays(1), Especialidade.CARDIOLOGIA);

		// Act & Assert
		assertDoesNotThrow(() -> validador.validar(dadosAgendamentoConsulta));
	}
}