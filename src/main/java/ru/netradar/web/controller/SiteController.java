package ru.netradar.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.netradar.config.properties.SMSQueueProperties;
import ru.netradar.server.acceptor.DGAccepterThread;
import ru.netradar.server.acceptor.sockets.connect.DGLocThread;
import ru.netradar.server.notify.position.PositionTaskDAO;
import ru.netradar.server.storage.DeviceStorage;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Controller
@RequestMapping("/")
public class SiteController {

    private static final Logger LOG = LoggerFactory.getLogger(SiteController.class);


    @Autowired
    private PositionTaskDAO positionTaskDAO;
    @Autowired
    private SMSQueueProperties smsQueueProperties;
    @Autowired
    protected DeviceStorage deviceStorage;


    /*@Autowired
    private OEventPersistenceService oEventService;
*/
   /* @Autowired
    JVMRuntime jvmRuntime;*/
    //sess@Autowired
    //private Basket basket;

    /*  @Autowired
      private OUserPersistenceService userPersistenceService;

  */
    @RequestMapping(method = RequestMethod.GET)
    public String getHome(Model model, Locale locale) {
        LOG.debug("Home view: {}", locale);
        model.addAttribute("testText", "Cur locale is " + locale);
        model.addAttribute("homebasetext", DGLocThread.getCommonInfoStringBuilder(smsQueueProperties.getSender(), positionTaskDAO, deviceStorage));

        //model.addAttribute("oEvents", getOEvents(oEventService.requestAllOEvents(new RequestAllOEventsEvent())));
        return "home";
    }


    //sess@ModelAttribute("basket")
    /*private Basket getBasket() {
        return basket;
    }*/

    @RequestMapping(value = "/errors/403", method = RequestMethod.GET)
    public String get403Error() {
        return "notauth";
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String getAbout(Model model) {
        LOG.debug("about view");
        return "about";
    }


    @RequestMapping(value = "/contact", method = RequestMethod.GET)
    public String getContact(Model model) {
        LOG.debug("contact view");
        return "contact";
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public String getStatus(Model model) {
        LOG.debug("status view");
        final StringBuilder stringBuilder = new StringBuilder("Some stat");//jvmRuntime.makeStatistic();
        if (stringBuilder != null) {
            model.addAttribute("statlog", stringBuilder.toString());
        } else {
            model.addAttribute("statlog", "unknown");
        }
        return "status";
    }


    /**
     * Error page.
     */
    @RequestMapping("/error.html")
    public String error(HttpServletRequest request, Model model) {
        model.addAttribute("errorCode", "Error " + request.getAttribute("javax.servlet.error.status_code"));
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("<ul>");
        while (throwable != null) {
            errorMessage.append("<li>").append(escapeTags(throwable.getMessage())).append("</li>");
            throwable = throwable.getCause();
        }
        errorMessage.append("</ul>");
        model.addAttribute("errorMessage", errorMessage.toString());
        return "error";
    }

    /**
     * Substitute 'less than' and 'greater than' symbols by its HTML entities.
     */
    private static String escapeTags(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
