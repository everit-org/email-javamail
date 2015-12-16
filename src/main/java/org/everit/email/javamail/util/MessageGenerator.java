/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.email.javamail.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.everit.email.Attachment;
import org.everit.email.Email;
import org.everit.email.EmailAddress;

/**
 * Generates JavaMail {@link Message} instance based on {@link Email} structure.
 */
public class MessageGenerator {

  private static final String DEFAULT_CHARSET = "utf-8";

  private String charset = DEFAULT_CHARSET;

  private void addAttachments(final Email email, final MimeMultipart mixedMultipart)
      throws MessagingException {
    for (Attachment attachment : email.attachments) {
      MimeBodyPart attachmentBodyPart = new MimeBodyPart();
      DataSource dataSource = new AttachmentDataSource(attachment);
      attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
      attachmentBodyPart.setDisposition(BodyPart.ATTACHMENT);
      attachmentBodyPart.setFileName(attachment.name);
      mixedMultipart.addBodyPart(attachmentBodyPart);
    }
  }

  private void applyHeadersOnMessage(final MimeMessage message, final Email mailParams)
      throws MessagingException {

    message.setSubject(mailParams.subject, charset);
    message.setFrom(convertAddress(mailParams.from));
    message.setRecipients(RecipientType.TO, convertAddresses(mailParams.recipients.to));
    message.setRecipients(RecipientType.CC, convertAddresses(mailParams.recipients.cc));
    message.setRecipients(RecipientType.BCC, convertAddresses(mailParams.recipients.bcc));
  }

  private InternetAddress convertAddress(
      final EmailAddress emailAddress) {
    try {
      return new InternetAddress(emailAddress.address, emailAddress.personal, charset);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private InternetAddress[] convertAddresses(
      final Collection<EmailAddress> addresses) {

    if (addresses.size() == 0) {
      return new InternetAddress[0];
    }

    InternetAddress[] result = new InternetAddress[addresses.size()];
    int i = 0;
    for (EmailAddress emailAddress : addresses) {
      result[i] = convertAddress(emailAddress);
      i++;
    }
    return result;
  }

  private Multipart createAlternativeMultiPart(final Email email) throws MessagingException {
    MimeMultipart alternativePart = new MimeMultipart("alternative");
    alternativePart.addBodyPart(createTextBodyPart(email));

    BodyPart htmlBodyPart;
    if (email.htmlContent.inlineImageByCidMap.size() == 0) {
      htmlBodyPart = createHtmlBodyPart(email);
    } else {
      htmlBodyPart = new MimeBodyPart();
      htmlBodyPart.setContent(createRelatedMultiPart(email));
    }
    alternativePart.addBodyPart(htmlBodyPart);

    return alternativePart;
  }

  private Multipart createComplexContent(final Email email) throws MessagingException {
    if (email.attachments.size() == 0) {
      if (email.textContent != null && email.htmlContent != null) {
        return createAlternativeMultiPart(email);
      } else {
        // Must be HTML with inline images as there are no attachments and yet this is a complex
        // email

        return createRelatedMultiPart(email);
      }
    } else {
      MimeMultipart mixedMultipart = new MimeMultipart("mixed");

      if (email.htmlContent == null) {
        mixedMultipart.addBodyPart(createTextBodyPart(email));
      } else if (email.textContent == null && email.htmlContent.inlineImageByCidMap.size() == 0) {
        mixedMultipart.addBodyPart(createHtmlBodyPart(email));
      } else {
        MimeBodyPart coverBodyPart = new MimeBodyPart();
        Multipart coverMultipart;
        if (email.textContent != null) {
          coverMultipart = createAlternativeMultiPart(email);
        } else {
          coverMultipart = createRelatedMultiPart(email);
        }
        coverBodyPart.setContent(coverMultipart);
        mixedMultipart.addBodyPart(coverBodyPart);
      }

      addAttachments(email, mixedMultipart);

      return mixedMultipart;
    }
  }

  private BodyPart createHtmlBodyPart(final Email email) throws MessagingException {
    MimeBodyPart htmlBodyPart = new MimeBodyPart();
    htmlBodyPart.setContent(email.htmlContent.html, "text/html; charset=" + charset);
    return htmlBodyPart;
  }

  private Multipart createRelatedMultiPart(final Email email) throws MessagingException {
    MimeMultipart relatedMultipart = new MimeMultipart("related");
    relatedMultipart.addBodyPart(createHtmlBodyPart(email));

    Set<Entry<String, Attachment>> inlineImageEntries =
        email.htmlContent.inlineImageByCidMap.entrySet();

    for (Entry<String, Attachment> inlineImageEntry : inlineImageEntries) {
      String cid = inlineImageEntry.getKey();
      Attachment imageAttachment = inlineImageEntry.getValue();

      MimeBodyPart imageBodyPart = new MimeBodyPart();
      imageBodyPart.setDataHandler(new DataHandler(new AttachmentDataSource(imageAttachment)));
      imageBodyPart.setHeader("Content-ID", "<" + cid + ">");

      relatedMultipart.addBodyPart(imageBodyPart);
    }
    return relatedMultipart;
  }

  private BodyPart createTextBodyPart(final Email email) throws MessagingException {
    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setContent(email.textContent, "text/plain; charset=utf-8");
    return textPart;
  }

  /**
   * Generates a JSR 919 {@link Message} from an {@link Email} structure.
   *
   * @param email
   *          The {@link Email} structure.
   * @return the JSR 919 {@link Message}.
   */
  public Message generateMessage(final Email email) {
    validateMailParams(email);

    MimeMessage message = new MimeMessage((Session) null);
    try {
      applyHeadersOnMessage(message, email);

      if (isSimpleMessage(email)) {
        if (email.htmlContent == null) {
          message.setText(email.textContent, charset);
        } else {
          message.setContent(email.htmlContent.html, "text/html; charset=utf-8");
        }
        return message;
      } else {
        Multipart complexContentMultiPart = createComplexContent(email);
        message.setContent(complexContentMultiPart);
      }
    } catch (MessagingException e) {
      throw new UncheckedMessagingException(e);
    }

    return message;
  }

  private boolean isSimpleMessage(final Email mailParams) {
    if (mailParams.attachments.size() > 0) {
      return false;
    }
    if (mailParams.htmlContent == null) {
      return true;
    }
    if (mailParams.textContent == null
        && mailParams.htmlContent.inlineImageByCidMap.size() == 0) {
      return true;
    }
    return false;
  }

  public void setCharset(final String charset) {
    this.charset = charset;
  }

  private void validateMailParams(final Email email) {
    Objects.requireNonNull(email, "mailParams cannot be null");
    Objects.requireNonNull(email.subject, "Subject must be defined");
    Objects.requireNonNull(email.from, "Email sender must be defined");
    Objects.requireNonNull(email.attachments, "Attachment collection cannot be null");
    Objects.requireNonNull(email.recipients, "Recipients must not be null");
    Objects.requireNonNull(email.recipients.bcc, "BCC collection cannot be null");
    Objects.requireNonNull(email.recipients.cc, "CC collection cannot be null");
    Objects.requireNonNull(email.recipients.to, "Recipients collection cannot be null");

    if (email.recipients.to.size() == 0) {
      throw new IllegalArgumentException("At least one recipient must be defined");
    }

    if (email.htmlContent == null && email.textContent == null) {
      throw new IllegalArgumentException(
          "Neither HTML nor Text content is defined. At least one of them should be declared.");
    }

    if (email.htmlContent != null) {
      if (email.htmlContent.html == null) {
        throw new IllegalArgumentException("Content is not defined for HTML body.");
      }
      if (email.htmlContent.inlineImageByCidMap == null) {
        throw new IllegalArgumentException("Inline image map for html body must not be null");
      }
    }
  }
}
