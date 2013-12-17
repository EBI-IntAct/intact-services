package uk.ac.ebi.intact.service.complex.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class WebAppController {
    @Autowired
    RestConnection restConnection;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String HomeController(ModelMap model) {
        model.addAttribute("complex_portal_name", "Intact Complex Portal");
        model.addAttribute("page_title", "Complex Search");
        return "home";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
	public String SearchController(@RequestParam String query,
                                   @RequestParam ( required = false ) String page,
                                   @RequestParam ( required = false ) String filter,
                                   @RequestParam ( required = false ) String type,
                                   ModelMap model,
                                   HttpSession session)
    {
        ComplexRestResult results = restConnection.query(query, page, filter, type);
		session.setAttribute("results", results );
		model.addAttribute("complex_portal_name", "Intact Complex Portal");
        model.addAttribute("page_title", "Complex Results");
        return "results";
	}
    @RequestMapping(value = "/details/{ac}", method = RequestMethod.GET)
    public String DetailsController(@PathVariable String ac,
                                    ModelMap model,
                                    HttpSession session) {
        ComplexDetails details = restConnection.getDetails(ac, QueryTypes.DETAILS.value);
        session.setAttribute("details", details );
        model.addAttribute("complex_portal_name", "Intact Complex Portal");
        model.addAttribute("page_title", "Complex Details");
        return "details";
    }
}