package br.com.i2e.core.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.i2e.common.enums.I2EStatus;
import br.com.i2e.common.enums.I2EType;
import br.com.i2e.common.enums.StatusVenda;
import br.com.i2e.common.enums.TipoFrete;
import br.com.i2e.common.enums.TipoPagamento;
import br.com.i2e.common.model.I2EMessage;
import br.com.i2e.common.model.order.EnderecoEntrega;
import br.com.i2e.common.model.order.EntregaVenda;
import br.com.i2e.common.model.order.ItemVenda;
import br.com.i2e.common.model.order.PagamentoVenda;
import br.com.i2e.common.model.order.Venda;
import br.com.i2e.common.util.JsonUtils;
import br.com.i2e.core.repository.AdministradoraCartaoRepository;
import br.com.i2e.core.repository.I2EMessageRepository;
import br.com.i2e.core.repository.SalesOrderRepository;
import br.com.i2e.core.repository.TransportadoraRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service@Transactional
public class SalesOrderService {
	
	private static String I2E_BACKEND_REQUEST_QUEUE = "i2e.request.queue"; 
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private I2EMessageRepository msgRepository;
	@Autowired
	private SalesOrderRepository soRepository;
	@Autowired
	private TransportadoraRepository transportadoraRepository;
	@Autowired
	private AdministradoraCartaoRepository admCartaoRepository;

	public void sendSalesOrder(Venda venda) {
		
		var msg = I2EMessage.get(); 
		msg.setType( I2EType.SEND_SALES_ORDER );
		msg.setStatus( I2EStatus.SENT );
		try {
			msg.setRequest( JsonUtils.toJson( venda ) );

			log.info( "Sendding {} requesto to Backend ", msg.getType() );
			rabbitTemplate.convertAndSend( I2E_BACKEND_REQUEST_QUEUE, JsonUtils.toJson( msg ) );
			
			msgRepository.save( msg );
		} catch ( JsonProcessingException | AmqpException e ) {
			
			e.printStackTrace();
			log.error( "Error on {} request to Backend", msg.getType() );
			msg.setError( e.toString() );
			msgRepository.save( msg );
		}
	}
	
//	@Bean
	public CommandLineRunner testVenda() {
		
		return args -> {
			
			var venda = new Venda();
			venda.setCriado( LocalDateTime.now() );
			venda.setStatus( StatusVenda.RECEBIDO );
			
			var item = new ItemVenda();
			item.setCodigoSku( "1" );
			item.setQuantidade( 1 );
			item.setValorUnitario( BigDecimal.valueOf( Double.valueOf( "576.34" ) ) );
			item.setValorDesconto( BigDecimal.valueOf( Double.valueOf( "100" ) ) );
			venda.setItens( Arrays.asList( new ItemVenda[] { item } ) );
			
			var pag = new PagamentoVenda();
			pag.setTipo( TipoPagamento.CARTAO_CREDITO );
			pag.setValor( BigDecimal.valueOf( Double.valueOf( "476.34" ) ) );
			pag.setNsu( "995544" );
			pag.setAdministradoraCartao( admCartaoRepository.findByCodigo( "1" ) );
			pag.setQuantidadeParcelas( 1 );
			pag.setNumeroCartao( "123456789" );
			venda.setPagamentos( Arrays.asList( new PagamentoVenda[] { pag } ) );
			
			var entrega = new EntregaVenda();
			entrega.setValor( BigDecimal.valueOf( Double.valueOf( "10" ) )  );
			entrega.setTipoFrete( TipoFrete.DESTINATARIO );
			entrega.setPesoBruto( BigDecimal.ZERO );
			entrega.setPesoLiquido( BigDecimal.ZERO );
			entrega.setVolume( BigDecimal.ZERO );
			entrega.setTransportadora( transportadoraRepository.findByCnpj( "24165926000103" ) );
			entrega.setSomaNota( true );
			venda.setEntrega( entrega );
			
			var endereco = new EnderecoEntrega();
			endereco.setBairro( "Teste" );
			endereco.setCep( "82600380" );
			endereco.setCidade( "Curitiba" );
			endereco.setUf( "PR" );
			endereco.setNumero( "1231 NO" );
			entrega.setEndereco( endereco );
			
			soRepository.save( venda );
			sendSalesOrder( venda );
		};
	}
}