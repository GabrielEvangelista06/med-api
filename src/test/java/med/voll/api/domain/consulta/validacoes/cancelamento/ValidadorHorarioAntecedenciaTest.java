package med.voll.api.domain.consulta.validacoes.cancelamento;

import jakarta.persistence.EntityNotFoundException;
import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.consulta.DadosCancelamentoConsulta;
import med.voll.api.domain.consulta.MotivoCancelamento;
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
class ValidadorHorarioAntecedenciaTest
{
	@MockBean
	private ConsultaRepository consultaRepository;

	private final ValidadorHorarioAntecedencia validador;

	public ValidadorHorarioAntecedenciaTest(@Autowired ValidadorHorarioAntecedencia validador)
	{
		this.validador = validador;
	}

	private DadosCancelamentoConsulta criarDadosCancelamentoConsulta()
	{
		return new DadosCancelamentoConsulta(1L, MotivoCancelamento.OUTROS);
	}

	@Test
	@DisplayName("Não deve lançar exceção se a consulta for cancelada com 24 horas de antecedência")
	void validarCenario1()
	{
		// Arrange
		var dadosCancelamentoConsulta = this.criarDadosCancelamentoConsulta();

		var consulta = new Consulta();
		consulta.setId(1L);
		consulta.setData(LocalDateTime.now().plusDays(1).plusSeconds(1));

		when(this.consultaRepository.getReferenceById(dadosCancelamentoConsulta.idConsulta())).thenReturn(consulta);

		// Act & Assert
		assertDoesNotThrow(() -> validador.validar(dadosCancelamentoConsulta));
	}

	@Test
	@DisplayName("Deve lançar exceção se a consulta for cancelada com menos de 24 horas de antecedência")
	void validarCenario2()
	{
		// Arrange
		var dadosCancelamentoConsulta = this.criarDadosCancelamentoConsulta();

		var consulta = new Consulta();
		consulta.setId(1L);
		consulta.setData(LocalDateTime.now().plusHours(23));

		when(this.consultaRepository.getReferenceById(dadosCancelamentoConsulta.idConsulta())).thenReturn(consulta);

		// Act & Assert
		assertThrows(ValidacaoException.class, () -> validador.validar(dadosCancelamentoConsulta));
	}

	@Test
	@DisplayName("Deve lançar exceção se a consulta não existir")
	void validarCenario3()
	{
		// Arrange
		var dadosCancelamentoConsulta = this.criarDadosCancelamentoConsulta();

		when(this.consultaRepository.getReferenceById(dadosCancelamentoConsulta.idConsulta())).thenReturn(null);

		// Act & Assert
		assertThrows(NullPointerException.class, () -> validador.validar(dadosCancelamentoConsulta));
	}
}