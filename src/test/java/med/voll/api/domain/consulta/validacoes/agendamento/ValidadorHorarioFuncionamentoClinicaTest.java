package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.medico.Especialidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorHorarioFuncionamentoClinicaTest
{
	private final ValidadorHorarioFuncionamentoClinica validador = new ValidadorHorarioFuncionamentoClinica();

	private DadosAgendamentoConsulta criarDadosAgendamentoConsulta(LocalDateTime data)
	{
		return new DadosAgendamentoConsulta(1L, 2L, data, Especialidade.ORTOPEDIA);
	}

	@Test
	@DisplayName("Não deve lançar exceção se a consulta for agendada dentro do horário de funcionamento da clínica")
	void validarCenario1()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta(LocalDateTime.now().withHour(10).withMinute(0).withSecond(0));

		// Act & Assert
		assertDoesNotThrow(() -> this.validador.validar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Deve lançar exceção se a consulta for agendada fora do horário de funcionamento da clínica")
	void validarCenario2()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta(
				LocalDateTime.now().withHour(6).withMinute(30).withSecond(0).plusDays(1));

		// Act & Assert
		assertThrows(ValidacaoException.class, () -> validador.validar(dadosAgendamentoConsulta));
	}
}