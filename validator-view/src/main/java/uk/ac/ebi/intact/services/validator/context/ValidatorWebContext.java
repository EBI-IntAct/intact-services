package uk.ac.ebi.intact.services.validator.context;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton which contains the ValidatorWebContent with all the environment variables to re-use when validating a file.
 * It needs to be unique for the all application so we can re-use the same environment variables when validating a file.
 * Using pre-loaded instances of Ontology manager, cv-mapping and object rules is important to not spend too much time reloading
 * dependency rules and the ontology at each validation..
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Jun-2010</pre>
 */

public class ValidatorWebContext {

    /**
     * The current instance
     */
    private static ValidatorWebContext ourInstance = new ValidatorWebContext();

    /**
     * The validator web content
     */
    private ValidatorWebContent validatorWebContent;

    /**
     * The MailSender
     */
    private MailSender mailSender;

    /**
     * The e-mail sender
     */
    private final String emailSender = "validator-admin-noreply@ebi.ac.uk";

    /**
     * The recipients of the e-mails to send
     */
    private List<String> emailRecipients = new ArrayList<String>();

    /**
     * The subject prefix in the e-mails to send
     */
    private final String emailSubjectPrefix = "[Validator-view]";

    /**
     *
     * @return the current instance of the ValidatorWebContext
     */
    public static ValidatorWebContext getInstance() {

        return ourInstance;
    }

    /**
     * Create a new ValidatorWebContent. It will instantiate the ValidatorWebContent and MailSender
     */
    private ValidatorWebContext(){
        // Initialize Spring for emails
        String[] configFiles = new String[]{"/beans.spring.xml"};
        BeanFactory beanFactory = new ClassPathXmlApplicationContext( configFiles );
        this.mailSender = ( MailSender ) beanFactory.getBean( "mailSender" );

        setUpEMailRecipients();

        try {
            // Create a new ValidatorWebContent
            this.validatorWebContent = new ValidatorWebContent(false);
        } catch (ValidatorWebContextException e) {
            String body = "The validator web content has not been properly initialized. It will rely on a local ontology." + ExceptionUtils.getFullStackTrace(e);

            sendEmail("Problem initializing the validator web content", body);

            try {
                // Create a new ValidatorWebContent looking inot local ontology
                this.validatorWebContent = new ValidatorWebContent(true);
            } catch (ValidatorWebContextException e2) {
                String body2 = "The validator web content has not been properly initialized and it is impossible to find a local ontology to rely on." + ExceptionUtils.getFullStackTrace(e2);

                sendEmail("Problem initializing the validator web content", body2);


            }
        }
    }

    /**
     * The method doesn't need to be synchronized yet as it is only an atomic operation, but it could be modified later.
     * @return The validator web content
     */
    public synchronized ValidatorWebContent getValidatorWebContent() {
        return validatorWebContent;
    }

    /**
     * Set the ValidatorWebContent
     * The method doesn't need to be synchronized yet as it is only an atomic operation, but it could be modified later.
     * @param validatorWebContent
     */
    public synchronized void setValidatorWebContent(ValidatorWebContent validatorWebContent) {
        this.validatorWebContent = validatorWebContent;
    }

    /**
     * Set up the e-mail recipients
     */
    private void setUpEMailRecipients(){
        emailRecipients.clear();

        emailRecipients.add("marine@ebi.ac.uk");
        emailRecipients.add("baranda@ebi.ac.uk");
        emailRecipients.add("skerrien@ebi.ac.uk");
    }

    /**
     * Send an e-mail with a title and body to the different e-mail recipients which were set in the constructor
     * @param title : title of the e-mail
     * @param body : body of the e-mail
     */
    public void sendEmail( String title, String body ) {
        if ( mailSender != null ) {
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setTo( emailRecipients.toArray( new String[]{} ) );
            message.setFrom( emailSender );
            message.setSubject( emailSubjectPrefix + " " + title );
            message.setText( body );
            mailSender.send( message );
        }
    }
}
