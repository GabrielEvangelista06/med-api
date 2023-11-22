package med.voll.api.domain.consulta.validacoes.cancelamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.consulta.DadosCancelamentoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component("ValidadorHorarioAntecedenciaCancelamento")
public class ValidadorHorarioAntecedencia implements ValidadorCancelamentoConsulta
{

	@Autowired
	private ConsultaRepository repository;

	@Override
	public void validar(DadosCancelamentoConsulta dados)
	{
		Consulta consulta = this.repository.getReferenceById(dados.idConsulta());
		LocalDateTime agora = LocalDateTime.now();
		long diferencaEmHoras = Duration.between(agora, consulta.getData()).toHours();

		if (diferencaEmHoras < 24)
		{
			throw new ValidacaoException("Consulta só pode ser cancelada com 24 horas de antecedência!");
		}
	}
}
