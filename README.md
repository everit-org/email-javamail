# email-javamail

JSR 919 (javax.mail) based implementation of [Everit Email API][1].

## Usage

    // Create a session as you like
    
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", "25");
    
    Session session = Session.getInstance(props,
       new javax.mail.Authenticator() {
           protected PasswordAuthentication getPasswordAuthentication() {
             return new PasswordAuthentication(username, password);
           }
       });
    
    // Create an email sender that will use the session
    
    EmailSender emailSender = new JavaMailSender(session);
    
    
    // Send an email with the emailSender instance as it is shown in the
    // documentation of the email-api project.
    
    Email email = constructEmail();
    
    
    // Send the email
    
    email.sendEmail(email);

Find examples about the construction of email structures in the [documentation of email-api][1].

[1]: http://everit.org/email-api/
