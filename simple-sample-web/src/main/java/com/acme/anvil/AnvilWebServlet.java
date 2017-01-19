package com.acme.anvil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.acme.anvil.service.ItemLookupLocal;
import com.acme.anvil.service.ItemLookupLocalHome;

public class AnvilWebServlet extends HttpServlet {

	private static final Logger LOG = Logger.getLogger(AnvilWebServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOG.info("Started AnvilWebServlet request");
		InitialContext ic;
		ItemLookupLocalHome lh;
		ItemLookupLocal local;
		try {
			ic = new InitialContext();
			LOG.info("Retrieved initial context");
			lh  = (ItemLookupLocalHome)ic.lookup("ejb/ItemLookupLocal");
			LOG.info("Lookup local: " + lh);
			local = lh.create();
			
			String itemId = req.getParameter("id");
			if(StringUtils.isNotBlank(itemId)) {
				Long id = Long.parseLong(itemId);
				local.lookupItem(id);
			}
		} catch (EJBException e) {
			LOG.log(Level.SEVERE, "Exception creating EJB.", e);
		} catch (CreateException e) {
			LOG.log(Level.SEVERE, "Exception creating EJB.", e);
		} catch (NamingException e) {
			LOG.log(Level.SEVERE, "Exception looking up EJB LocalHome.", e);
		}
		
		
		
	}
}
