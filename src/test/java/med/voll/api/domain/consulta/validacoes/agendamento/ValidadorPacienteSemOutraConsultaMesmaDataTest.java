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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureJsonTesters
class ValidadorPacienteSemOutraConsultaMesmaDataTest
{
	@MockBean
	private ConsultaRepository consultaRepository;

	private final ValidadorPacienteSemOutraConsultaMesmaData validador;

	public ValidadorPacienteSemOutraConsultaMesmaDataTest(@Autowired ValidadorPacienteSemOutraConsultaMesmaData validador)
	{
		this.validador = validador;
	}

	private DadosAgendamentoConsulta criarDadosAgendamentoConsulta()
	{
		return new DadosAgendamentoConsulta(1L, 2L, LocalDateTime.now().plusDays(1), Especialidade.CARDIOLOGIA);
	}

	@Test
	@DisplayName("Não deve lançar exceção se o paciente não tiver outra consulta no mesmo dia")
	void validarPacienteSemOutraConsultaMesmaData()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta();

		when(this.consultaRepository.existsByPacienteIdAndDataBetween(dadosAgendamentoConsulta.idPaciente(),
				dadosAgendamentoConsulta.data().withHour(7), dadosAgendamentoConsulta.data().withHour(18))).thenReturn(false);

		// Act & Assert
		assertDoesNotThrow(() -> validador.validar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Deve lançar exceção se o paciente tiver outra consulta no mesmo dia")
	void validarPacienteComOutraConsultaMesmaData()
	{
		// Arrange
		var dadosAgendamentoConsulta = this.criarDadosAgendamentoConsulta();

		when(this.consultaRepository.existsByPacienteIdAndDataBetween(dadosAgendamentoConsulta.idPaciente(),
				dadosAgendamentoConsulta.data().withHour(7), dadosAgendamentoConsulta.data().withHour(18))).thenReturn(true);

		// Act & Assert
		assertThrows(ValidacaoException.class, () -> validador.validar(dadosAgendamentoConsulta));
	}
}