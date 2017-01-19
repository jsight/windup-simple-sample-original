package com.acme.anvil.service.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;

import com.acme.anvil.vo.LogEvent;

@Stateless
public class LogEventPublisher {

	private static final Logger LOG = Logger.getLogger(LogEventPublisher.class);
	private static final String QUEUE_JNDI_NAME = "jms/LogEventQueue";
	private static final String QUEUE_FACTORY_JNDI_NAME = "jms/LogEventQueue";

	@Resource
	private TransactionManager transactionManager;
	
	public void publishLogEvent(LogEvent log) throws SystemException, InvalidTransactionException {
		//get a reference to the transaction manager to suspend the current transaction incase of exception.
		//ClientTransactionManager ctm = TransactionHelper.getTransactionHelper().getTransactionManager();
		Transaction saveTx = null;
		try {
			//saveTx = (Transaction) ctm.forceSuspend(); // Forced
			saveTx = transactionManager.suspend();

			try {
				Context ic = getContext();
				QueueSession session = getQueueSession(ic);
				Queue queue = getQueue(ic);
				QueueSender sender = session.createSender(queue);
				ObjectMessage logMsg = session.createObjectMessage(log);

				sender.send(logMsg);
			} catch (JMSException e) {
				LOG.error("Exception sending message.", e);
			} catch (NamingException e) {
				LOG.error("Exception looking up required resource.", e);
			}

		} finally {
			transactionManager.resume(saveTx);
		}
	}

	private static Context getContext() throws NamingException {
		return new InitialContext();
	}

	private static Queue getQueue(Context context) throws NamingException {
		return (Queue) context.lookup(QUEUE_JNDI_NAME);
	}

	private static QueueSession getQueueSession(Context context) throws JMSException, NamingException {
		QueueConnectionFactory cf = (QueueConnectionFactory) context
				.lookup(QUEUE_FACTORY_JNDI_NAME);
		QueueConnection connection = cf.createQueueConnection();
		return (QueueSession) connection.createSession(false, 1);
	}
}
