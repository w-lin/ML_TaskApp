package utils;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailUtil {

    private static AmazonSimpleEmailService SES_CLIENT = new AmazonSimpleEmailServiceClient();
    private static String EMAIL_SENDER = "wenghui.lin@gmail.com";
    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());

    /*
    if the email address is not in AWS SES Email Address Identities list,
    it requires to send a verification email to confirm user subscription
     */
    public static String sendEmailVerificationIfNecessary (String email)
    {
        GetIdentityVerificationAttributesRequest identityVerificationRequest = new GetIdentityVerificationAttributesRequest().withIdentities(email);
        GetIdentityVerificationAttributesResult identityVerificationResponse = SES_CLIENT.getIdentityVerificationAttributes(identityVerificationRequest);
        IdentityVerificationAttributes attributes = identityVerificationResponse.getVerificationAttributes().get(email);
        if(attributes!=null &&  (("Success").equals(attributes.getVerificationStatus())||("Pending").equals(attributes.getVerificationStatus())) )      //Pending | Success | Failed | TemporaryFailure | NotStarted
            return null; //The email has been in the subscription list or email verification is pending

        VerifyEmailAddressRequest verifyEmailRequest = new VerifyEmailAddressRequest().withEmailAddress(email);
        SES_CLIENT.verifyEmailAddress(verifyEmailRequest);
        return "A verification email has been sent to " + email;
    }

    public static void sendEmail(String email, String subject, String body)
    {
        try {
            Destination destination = new Destination().withToAddresses(email);
            Content contentSubject = new Content().withData(subject);
            Body bbody = new Body().withText(new Content().withData(body));
            Message message = new Message().withSubject(contentSubject).withBody(bbody);
            SendEmailRequest request = new SendEmailRequest().withSource(EMAIL_SENDER).withDestination(destination).withMessage(message);
            new AmazonSimpleEmailServiceClient().sendEmail(request);
        }
        catch (Exception e)
        {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }
}
