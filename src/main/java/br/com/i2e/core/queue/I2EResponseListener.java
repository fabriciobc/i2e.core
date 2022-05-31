package br.com.i2e.core.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import br.com.i2e.common.model.I2EMessage;
import br.com.i2e.common.util.JsonUtils;
import br.com.i2e.core.service.I2EMessageService;

@Component
public class I2EResponseListener {

	private static final Logger logger = LoggerFactory.getLogger( I2EResponseListener.class ); 

	public final String SHOP9_RESPONSE_QUEUE = "i2e.response.queue";
	
    @Autowired
    private I2EMessageService messageService; 
    
	@RabbitListener( queues = SHOP9_RESPONSE_QUEUE )
	public void onMessage( @Payload String jsonMessage ) {
		
		var msg =  JsonUtils.fromJson( jsonMessage, I2EMessage.class );
		messageService.receive( msg );
	}
}
