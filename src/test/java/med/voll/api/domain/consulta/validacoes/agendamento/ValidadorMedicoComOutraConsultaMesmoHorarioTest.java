package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.medico.Especialidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureJsonTesters
class ValidadorMedicoComOutraConsultaMesmoHorarioTest
{
	@MockBean
	private ConsultaRepository consultaRepository;

	private final ValidadorMedicoComOutraConsultaMesmoHorario validador;

	public ValidadorMedicoComOutraConsultaMesmoHorarioTest(@Autowired ValidadorMedicoComOutraConsultaMesmoHorario validador)
	{
		this.validador = validador;
	}

	private DadosAgendamentoConsulta criarDadosAgendamentoConsulta()
	{
		return new DadosAgendamentoConsulta(1L, 2L, LocalDateTime.now().plusDays(1), Especialidade.CARDIOLOGIA);
	}

	@Test
	@DisplayName("Não deve lançar exceção se o médico não possuir outra consulta no mesmo horário")
	void validarCenario1()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta();

		when(this.consultaRepository.existsByMedicoIdAndDataAndMotivoCancelamentoIsNull(dadosAgendamentoConsulta.idMedico(),
				dadosAgendamentoConsulta.data())).thenReturn(false);

		// Act & Assert
		assertDoesNotThrow(() -> validador.validar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Deve lançar exceção se o médico possuir outra consulta no mesmo horário")
	void validarCenario2()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta();

		when(this.consultaRepository.existsByMedicoIdAndDataAndMotivoCancelamentoIsNull(dadosAgendamentoConsulta.idMedico(),
				dadosAgendamentoConsulta.data())).thenReturn(true);

		// Act & Assert
		assertThrows(ValidacaoException.class, () -> validador.validar(dadosAgendamentoConsulta));
	}
}