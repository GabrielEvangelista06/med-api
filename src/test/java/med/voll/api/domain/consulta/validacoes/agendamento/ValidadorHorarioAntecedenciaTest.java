package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.medico.Especialidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureJsonTesters
class ValidadorHorarioAntecedenciaTest
{
	private final ValidadorHorarioAntecedencia validador = new ValidadorHorarioAntecedencia();

	private DadosAgendamentoConsulta criarDadosAgendamentoConsulta(LocalDateTime data)
	{
		return new DadosAgendamentoConsulta(1L, 2L, data, Especialidade.ORTOPEDIA);
	}

	@Test
	@DisplayName("Deve lançar exceção se consulta for agendada com menos de 30 minutos de antecedência")
	void validarCenario1()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta(LocalDateTime.now().plusMinutes(15));

		// Act & Assert
		assertThrows(ValidacaoException.class, () -> this.validador.validar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Não deve lançar exceção se a consulta for agendada com 30 minutos ou mais de antecedência")
	void validarCenario2()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta(LocalDateTime.now().plusMinutes(30).plusSeconds(1));

		// Act & Assert
		assertDoesNotThrow(() -> this.validador.validar(dadosAgendamentoConsulta));
	}
}